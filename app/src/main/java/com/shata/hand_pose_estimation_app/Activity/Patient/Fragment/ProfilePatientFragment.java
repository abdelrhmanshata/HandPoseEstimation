package com.shata.hand_pose_estimation_app.Activity.Patient.Fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shata.hand_pose_estimation_app.Activity.Login_Register.LoginActivity;
import com.shata.hand_pose_estimation_app.Models.ModelDataUser;
import com.shata.hand_pose_estimation_app.Models.ModelPatient;
import com.shata.hand_pose_estimation_app.R;
import com.shata.hand_pose_estimation_app.databinding.FragmentPatientProfileBinding;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

public class ProfilePatientFragment extends Fragment {

    FragmentPatientProfileBinding patientProfileBinding;
    ModelPatient patient;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser user = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference patientsReference = database.getReference("Patients");
    DatabaseReference Docter_Patients_Reference = database.getReference("DoctorPatients");

    Uri filePath;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference("UsersImage");

    public ProfilePatientFragment() {
    }

    public ProfilePatientFragment(ModelPatient patient) {
        this.patient = patient;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        patientProfileBinding = FragmentPatientProfileBinding.inflate(getLayoutInflater(), container, false);

        // Permissions to open Camera
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        loadPatientData();

        patientProfileBinding.editDataPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePatientData();
            }
        });

        if (patient != null)
            patientProfileBinding.logout.setVisibility(View.GONE);

        patientProfileBinding.logout.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Logout", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
            getActivity().finish();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        patientProfileBinding.userImage.setOnClickListener(v -> SelectImage());

        return patientProfileBinding.getRoot();
    }

    private void SelectImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(getActivity());
    }

    public void loadPatientData() {
        patientProfileBinding.progressCircle.setVisibility(View.VISIBLE);
        if (patient == null) {
            user = firebaseAuth.getCurrentUser();
            patientsReference
                    .child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelDataUser user = snapshot.getValue(ModelDataUser.class);
                            if (user != null) {
                                Picasso.get().load(user.getUserImageUri().trim())
                                        .into(patientProfileBinding.userImage);
                                patientProfileBinding.userName.setText(user.getUserName());
                                patientProfileBinding.userEmail.setText(user.getUserEmail());
                                patientProfileBinding.userEmail.setEnabled(false);
                                patientProfileBinding.layoutUserEmail.setEndIconMode(TextInputLayout.END_ICON_NONE);
                                patientProfileBinding.userPhoneNumber.setText(user.getUserPhone());
                            } else {
                                Toast.makeText(getActivity(), "User Not Found", Toast.LENGTH_SHORT).show();
                            }
                            patientProfileBinding.progressCircle.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            patientProfileBinding.progressCircle.setVisibility(View.GONE);
                        }
                    });
        } else {
            Docter_Patients_Reference
                    .child(user.getUid())
                    .child(patient.getPatientID())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelPatient patient = snapshot.getValue(ModelPatient.class);
                            if (patient != null) {
                                Picasso.get().load(patient.getPatientImageUri().trim())
                                        .into(patientProfileBinding.userImage);
                                patientProfileBinding.userName.setText(patient.getPatientName());
                                patientProfileBinding.userEmail.setText(patient.getPatientEmail());
                                patientProfileBinding.userPhoneNumber.setText(patient.getPatientPhone());
                            } else {
                                Toast.makeText(getActivity(), "User Not Found", Toast.LENGTH_SHORT).show();
                            }
                            patientProfileBinding.progressCircle.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            patientProfileBinding.progressCircle.setVisibility(View.GONE);
                        }
                    });
        }
    }

    public void savePatientData() {
        patientProfileBinding.progressCircle.setVisibility(View.VISIBLE);
        String Username = Objects.requireNonNull(patientProfileBinding.userName.getText()).toString().trim();
        if (Username.isEmpty()) {
            patientProfileBinding.userName.setError("Username Is Required");
            patientProfileBinding.userName.setFocusable(true);
            patientProfileBinding.userName.requestFocus();
            patientProfileBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        String UserEmail = Objects.requireNonNull(patientProfileBinding.userEmail.getText()).toString().trim() + "";
        if (!UserEmail.isEmpty()) {
            if (!Patterns.EMAIL_ADDRESS.matcher(UserEmail).matches()) {
                patientProfileBinding.userEmail.setError("Please Enter a Valid Email !!!");
                patientProfileBinding.userEmail.setFocusable(true);
                patientProfileBinding.userEmail.requestFocus();
                patientProfileBinding.progressCircle.setVisibility(View.GONE);
                return;
            }
        }

        //Get complete phone number
        String _getUserEnteredPhoneNumber = patientProfileBinding.userPhoneNumber.getText().toString();
        if (TextUtils.isEmpty(_getUserEnteredPhoneNumber)) {
            patientProfileBinding.userPhoneNumber.setError("Phone Number Is Required");
            patientProfileBinding.userPhoneNumber.setFocusable(true);
            patientProfileBinding.userPhoneNumber.requestFocus();
            patientProfileBinding.progressCircle.setVisibility(View.GONE);
            return;
        } else {
            //Remove first zero if entered!
            if (_getUserEnteredPhoneNumber.charAt(0) == '0') {
                _getUserEnteredPhoneNumber = _getUserEnteredPhoneNumber.substring(1);
            }
        }
        //Complete phone number
        String phone = _getUserEnteredPhoneNumber;
        if (_getUserEnteredPhoneNumber.charAt(0) != '+') {
            phone = "+" + "20" + _getUserEnteredPhoneNumber;
        }

        String UserPhone = phone;
        if (UserPhone.isEmpty()) {
            patientProfileBinding.userPhoneNumber.setError("Phone Number Is Required");
            patientProfileBinding.userPhoneNumber.setFocusable(true);
            patientProfileBinding.userPhoneNumber.requestFocus();
            patientProfileBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        if (UserPhone.length() < 11) {
            patientProfileBinding.userPhoneNumber.setError("Please Enter a Valid Phone Number");
            patientProfileBinding.userPhoneNumber.setFocusable(true);
            patientProfileBinding.userPhoneNumber.requestFocus();
            patientProfileBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        if (patient == null) {
            patientsReference
                    .child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelDataUser user = snapshot.getValue(ModelDataUser.class);
                            if (user != null) {
                                snapshot.getRef().child("userName").setValue(Username);
                                snapshot.getRef().child("userPhone").setValue(UserPhone);
                                Toasty.success(getActivity(), "Successfully Updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "User Not Found", Toast.LENGTH_SHORT).show();
                            }
                            patientProfileBinding.progressCircle.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            patientProfileBinding.progressCircle.setVisibility(View.GONE);
                        }
                    });
        } else {
            Docter_Patients_Reference
                    .child(user.getUid())
                    .child(patient.getPatientID())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelPatient patient = snapshot.getValue(ModelPatient.class);
                            if (patient != null) {
                                snapshot.getRef().child("patientName").setValue(Username);
                                snapshot.getRef().child("patientEmail").setValue(UserEmail);
                                snapshot.getRef().child("patientPhone").setValue(UserPhone);
                                Toasty.success(getActivity(), "Successfully Updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "User Not Found", Toast.LENGTH_SHORT).show();
                            }
                            patientProfileBinding.progressCircle.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            patientProfileBinding.progressCircle.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toasty.normal(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
                uploadImage(filePath);
                try {
                    Picasso.get().load(filePath).into(patientProfileBinding.userImage);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("Error", error.getMessage());
            }
        }
    }

    private void uploadImage(Uri filePath) {
        if (filePath != null) {
            ProgressDialog progressDialog
                    = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            StorageReference ref;
            if (patient == null) {
                ref = storageReference
                        .child(user.getUid() + "/UserImage");
            } else {
                ref = storageReference
                        .child(user.getUid() + "/" + patient.getPatientID() + "/UserImage");
            }

            ref.putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (!patientsReference.onDisconnect().cancel().isSuccessful() || !Docter_Patients_Reference.onDisconnect().cancel().isSuccessful()) {
                        if (patient == null) {
                            patientsReference
                                    .child(Objects.requireNonNull(user.getUid()))
                                    .child("userImageUri")
                                    .setValue(uri.toString())
                                    .addOnCompleteListener(task -> {
                                        progressDialog.dismiss();
                                        Toasty.success(getActivity(), "Image Uploading", Toast.LENGTH_SHORT).show();

                                    }).addOnFailureListener(e -> {
                                Toasty.warning(getActivity(), "Image Failure Uploading", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            Docter_Patients_Reference
                                    .child(Objects.requireNonNull(user.getUid()))
                                    .child(patient.getPatientID())
                                    .child("patientImageUri")
                                    .setValue(uri.toString())
                                    .addOnCompleteListener(task -> {
                                        progressDialog.dismiss();
                                        Toasty.success(getActivity(), "Image Uploading", Toast.LENGTH_SHORT).show();
                                    }).addOnFailureListener(e -> {
                                Toasty.warning(getActivity(), "Image Failure Uploading", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        Toasty.error(getActivity(), "Faild", Toast.LENGTH_SHORT).show();
                    }
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Log.d("Failure Error", e.getMessage());
            }).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage("Upload " + (int) progress + "%");
            });
        }
    }
}