package com.shata.hand_pose_estimation_app.Activity.UI;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.shata.hand_pose_estimation_app.databinding.ActivityImageBinding;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class ImageActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    Uri imageURI;
    ModelPatient patient = null;
    ActivityImageBinding imageBinding;
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
        imageBinding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(imageBinding.getRoot());
        //
        patient = (ModelPatient) getIntent().getSerializableExtra("ObjectPatient");
        //
        toolbar = findViewById(R.id.toolBar);
        toolbar.setTitle("Choose Image");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //

        selectImage();

        imageBinding.acceptIV.setOnClickListener(v -> {
            uploadImage();
        });

        imageBinding.cancelIV.setOnClickListener(v -> {
            selectImage();
        });
    }

    public void selectImage() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CAMERA_PERMISSION);
            return;
        }
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageURI = data.getData();
            Picasso.get().load(imageURI)
                    .into(imageBinding.imageView);
        }
    }

    private void uploadImage() {
        if (imageURI != null) {
            ProgressDialog progressDialog
                    = new ProgressDialog(ImageActivity.this);
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
            ref_Storage.putFile(imageURI).addOnSuccessListener(taskSnapshot -> {
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
                                    onBackPressed();
                                    Toasty.success(ImageActivity.this, "Image Uploading", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    onBackPressed();
                                    Toasty.warning(ImageActivity.this, "Image Faild Uploading", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        onBackPressed();
                        Toasty.error(ImageActivity.this, "Faild", Toast.LENGTH_SHORT).show();
                    }
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                onBackPressed();
                Log.d("Failure Error", e.getMessage());
            }).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage("Upload " + (int) progress + "%");
            });
        } else {
            Toast.makeText(this, "imageURI == null", Toast.LENGTH_SHORT).show();
        }
    }

    public String getCurrentData() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy/M/dd", Locale.getDefault());
        return mdformat.format(calendar.getTime());
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