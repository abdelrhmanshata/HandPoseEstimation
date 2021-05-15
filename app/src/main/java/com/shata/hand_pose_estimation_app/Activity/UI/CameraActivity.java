package com.shata.hand_pose_estimation_app.Activity.UI;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shata.hand_pose_estimation_app.Models.ModelHandImage;
import com.shata.hand_pose_estimation_app.Models.ModelPatient;
import com.shata.hand_pose_estimation_app.R;
import com.shata.hand_pose_estimation_app.databinding.ActivityCameraBinding;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class CameraActivity extends AppCompatActivity implements SensorEventListener {

    // Check State ORIENTATIONS Of Output Image
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    public static Bitmap bitmap;
    public static byte[] bytes;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public ActivityCameraBinding cameraBinding;
    ////////////////////////////////
    SensorManager sensorManager;
    Sensor sensor;
    boolean isACCELEROMETERSenserAvailable, itIsNotFirstTime = false;
    float xCurrent, yCurrent;
    float xLast, yLast;
    float xDifference, yDifference;
    float sharkThreshold = 5f;
    Vibrator vibrator;
    ModelPatient patient = null;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Handler mBackgroundHandler;
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        }
    };
    private HandlerThread mBackgroundThread;
    //////////////////////////////////////////////////
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseDatabase DataBase_Image = FirebaseDatabase.getInstance();
    private FirebaseUser user = firebaseAuth.getCurrentUser();
    private StorageReference storageReference = storage.getReference("HandImages");
    private DatabaseReference mDatabaseRef_Image = DataBase_Image.getReference("HandImages");

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraBinding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(cameraBinding.getRoot());

        patient = (ModelPatient) getIntent().getSerializableExtra("ObjectPatient");
        //
        toolbar = findViewById(R.id.toolBar);
        toolbar.setTitle("Camera");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //
        Initialize_variables();
    }

    private void Initialize_variables() {
        // From Java 1.4 You Can Use Keyword Assert To Check Expression True Or False
        assert cameraBinding.textureView != null;
        cameraBinding.textureView.setSurfaceTextureListener(textureListener);
        cameraBinding.btnCapture.setOnClickListener(v -> takePicture());
        //////////////////////////////////////////////////
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isACCELEROMETERSenserAvailable = true;
        } else {
            cameraBinding.upIV.setVisibility(View.INVISIBLE);
            cameraBinding.downIV.setVisibility(View.INVISIBLE);
            cameraBinding.leftIV.setVisibility(View.INVISIBLE);
            cameraBinding.rightIV.setVisibility(View.INVISIBLE);
            isACCELEROMETERSenserAvailable = false;
        }
        cameraBinding.saveIV.setOnClickListener(v -> {
            Toast.makeText(this, "Save", Toast.LENGTH_SHORT).show();
            uploadImage();
        });
        cameraBinding.cancelIV.setOnClickListener(v -> {
            Toast.makeText(CameraActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
            createCameraPreview();
        });
    }

    private void takePicture() {
        if (cameraDevice == null)
            return;
        cameraBinding.layoutTool.setVisibility(View.VISIBLE);
        cameraBinding.btnCapture.setVisibility(View.GONE);
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSize = null;
            if (characteristics != null)
                jpegSize = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);
            // Capture Image With Custom Size
            int width = 640;
            int height = 480;
           /* if (jpegSize != null && jpegSize.length > 0) {
                width = jpegSize[0].getWidth();
                height = jpegSize[0].getHeight();
            }*/
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(cameraBinding.textureView.getSurfaceTexture()));

            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // check Orientation Base On Device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            ImageReader.OnImageAvailableListener readerListener = reader1 -> {
                Image image = null;
                try {
                    image = reader1.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (image != null) {
                        image.close();
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    // Finish Tack Photo
                    Toast.makeText(CameraActivity.this, "Image", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(TestActivity.this, "Saved" + file, Toast.LENGTH_LONG).show();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        cameraBinding.layoutTool.setVisibility(View.INVISIBLE);
        cameraBinding.btnCapture.setVisibility(View.VISIBLE);
        try {
            SurfaceTexture texture = cameraBinding.textureView.getSurfaceTexture();
            assert texture != null;
            //texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            texture.setDefaultBufferSize(640, 480);
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(CameraActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraID = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            Size imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraID, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                //finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isACCELEROMETERSenserAvailable)
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        startBackgroundThread();
        if (cameraBinding.textureView.isAvailable())
            openCamera();
        else
            cameraBinding.textureView.setSurfaceTextureListener(textureListener);
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    protected void onPause() {
        if (isACCELEROMETERSenserAvailable)
            sensorManager.unregisterListener(this);
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////

    private void uploadImage() {
        if (bytes != null) {
            ProgressDialog progressDialog
                    = new ProgressDialog(CameraActivity.this);
            progressDialog.setTitle("Uploading");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            //Create a reference to 'Ex 123456789.jpg'
            String imageID = String.valueOf(System.currentTimeMillis());
            String imageName = imageID + ".jpg";
            StorageReference ref_Storage;
            DatabaseReference ref_Image;
            if (patient == null) {
                ref_Storage = storageReference
                        .child(user.getUid() + "/" + imageName);
                ref_Image = mDatabaseRef_Image
                        .child(Objects.requireNonNull(user.getUid()))
                        .child(imageID);
            } else {
                ref_Storage = storageReference
                        .child(user.getUid() + "/" + patient.getPatientID() + "/" + imageName);
                ref_Image = mDatabaseRef_Image
                        .child(Objects.requireNonNull(user.getUid()))
                        .child(patient.getPatientID())
                        .child(imageID);
            }
            ref_Storage.putBytes(bytes).addOnSuccessListener(taskSnapshot -> {
                ref_Storage.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (!ref_Image.onDisconnect().cancel().isSuccessful()) {
                        ModelHandImage handImage = new ModelHandImage();
                        handImage.setImageID(imageID);
                        handImage.setImageUri(uri.toString());
                        handImage.setImageDate(getCurrentData());
                        ref_Image
                                .setValue(handImage)
                                .addOnCompleteListener(task -> {
                                    progressDialog.dismiss();
                                    Toasty.success(CameraActivity.this, "Image Uploading", Toast.LENGTH_SHORT).show();
                                    createCameraPreview();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toasty.warning(CameraActivity.this, "Image Faild Uploading", Toast.LENGTH_SHORT).show();
                                    createCameraPreview();
                                });
                    } else {
                        createCameraPreview();
                        Toasty.error(CameraActivity.this, "Faild", Toast.LENGTH_SHORT).show();
                    }
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                createCameraPreview();
                Log.d("Failure Error", e.getMessage());
            }).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage("Upload " + (int) progress + "%");
            });
        } else {
            Toast.makeText(this, "bytes == null", Toast.LENGTH_SHORT).show();
        }
    }


    public String getCurrentData() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy/M/dd", Locale.getDefault());
        return mdformat.format(calendar.getTime());
    }

    //////////////////////////////////////////////////////
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        int X = (int) sensorEvent.values[0];
        int Y = (int) sensorEvent.values[1];

        if (X > 0) {
            cameraBinding.rightIV.setImageResource(R.drawable.ic_baseline_arrow_right_24_red);
            //vibratorDevice();
        } else if (X < 0) {
            cameraBinding.leftIV.setImageResource(R.drawable.ic_baseline_arrow_left_24_red);
            //vibratorDevice();
        } else {
            cameraBinding.rightIV.setImageResource(R.drawable.ic_baseline_arrow_right_24_green);
            cameraBinding.leftIV.setImageResource(R.drawable.ic_baseline_arrow_left_24_green);
        }

        if (Y > 0) {
            cameraBinding.upIV.setImageResource(R.drawable.ic_baseline_arrow_up_24_red);
            //vibratorDevice();
        } else if (Y < 0) {
            cameraBinding.downIV.setImageResource(R.drawable.ic_baseline_arrow_down_24_red);
            //vibratorDevice();
        } else {
            cameraBinding.upIV.setImageResource(R.drawable.ic_baseline_arrow_up_24_green);
            cameraBinding.downIV.setImageResource(R.drawable.ic_baseline_arrow_down_24_green);
        }

        xCurrent = sensorEvent.values[0];
        yCurrent = sensorEvent.values[1];
        if (itIsNotFirstTime) {
            xDifference = Math.abs(xLast - xCurrent);
            yDifference = Math.abs(yLast - yCurrent);
            if ((xDifference > sharkThreshold && yDifference > sharkThreshold)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(100);
                }
            }
        }
        xLast = xCurrent;
        yLast = yCurrent;
        itIsNotFirstTime = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    void vibratorDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(10);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        return;
    }
}