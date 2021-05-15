package com.shata.hand_pose_estimation_app.Activity.Docter;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shata.hand_pose_estimation_app.Models.ModelPatient;
import com.shata.hand_pose_estimation_app.R;
import com.shata.hand_pose_estimation_app.databinding.ActivityAddPatientBinding;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class Add_Patient_Activity extends AppCompatActivity {

    RadioButton selectedGender;
    ActivityAddPatientBinding patientBinding;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser user = firebaseAuth.getCurrentUser();
    FirebaseDatabase DataBase_Patient = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseRef_Patient = DataBase_Patient.getReference("DoctorPatients/" + user.getUid());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        patientBinding = ActivityAddPatientBinding.inflate(getLayoutInflater());
        setContentView(patientBinding.getRoot());

        // Toolbar
        patientBinding.toolbar.setTitle("Patient Activity");
        setSupportActionBar(patientBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        patientBinding.AddDataPatient.setOnClickListener(v -> continueRegister());
    }

    public void continueRegister() {

        patientBinding.progressCircle.setVisibility(View.VISIBLE);

        String PatientName = Objects.requireNonNull(patientBinding.patientName.getText()).toString().trim();

        if (PatientName.isEmpty()) {
            patientBinding.patientName.setError("Patient Name Is Required");
            patientBinding.patientName.setFocusable(true);
            patientBinding.patientName.requestFocus();
            patientBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        String PatientAge = Objects.requireNonNull(patientBinding.patientAge.getText()).toString().trim();

        if (PatientAge.isEmpty()) {
            patientBinding.patientAge.setError("Patient Age Is Required");
            patientBinding.patientAge.setFocusable(true);
            patientBinding.patientAge.requestFocus();
            patientBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        String PatientEmail = Objects.requireNonNull(patientBinding.patientEmail.getText()).toString().trim() + "";
        if (!PatientEmail.isEmpty()) {
            if (!Patterns.EMAIL_ADDRESS.matcher(PatientEmail).matches()) {
                patientBinding.patientEmail.setError("Please Enter a Valid Email !!!");
                patientBinding.patientEmail.setFocusable(true);
                patientBinding.patientEmail.requestFocus();
                patientBinding.progressCircle.setVisibility(View.GONE);
                return;
            }
        }

        //Get complete phone number
        String _getUserEnteredPhoneNumber = Objects.requireNonNull(patientBinding.patientPhoneNumber.getText()).toString().trim();
        if (TextUtils.isEmpty(_getUserEnteredPhoneNumber)) {
            patientBinding.patientPhoneNumber.setError("Phone Number Is Required");
            patientBinding.patientPhoneNumber.setFocusable(true);
            patientBinding.patientPhoneNumber.requestFocus();
            patientBinding.progressCircle.setVisibility(View.GONE);
            return;
        } else {
            //Remove first zero if entered!
            if (_getUserEnteredPhoneNumber.charAt(0) == '0') {
                _getUserEnteredPhoneNumber = _getUserEnteredPhoneNumber.substring(1);
            }
        }
        //Complete phone number
        String PatientPhoneNumber = "+" + "20" + _getUserEnteredPhoneNumber;

        if (PatientPhoneNumber.length() < 11) {
            patientBinding.patientPhoneNumber.setError("Please Enter a Valid Phone Number");
            patientBinding.patientPhoneNumber.setFocusable(true);
            patientBinding.patientPhoneNumber.requestFocus();
            patientBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        String PatientGender = "";
        if (patientBinding.radioGroupGender.getCheckedRadioButtonId() == -1) {
            Toasty.info(this, "Please Select Gender", Toast.LENGTH_SHORT).show();
            patientBinding.progressCircle.setVisibility(View.GONE);
            return;
        } else {
            selectedGender = findViewById(patientBinding.radioGroupGender.getCheckedRadioButtonId());
            if (selectedGender.getId() == R.id.male) {
                PatientGender = "Male";
            } else {
                PatientGender = "Female";
            }
        }

        String patientUID = mDatabaseRef_Patient.push().getKey();

        ModelPatient patient = new ModelPatient();
        patient.setPatientID(patientUID);
        patient.setPatientName(PatientName);
        patient.setPatientAge(PatientAge);
        patient.setPatientEmail(PatientEmail);
        patient.setPatientPhone(PatientPhoneNumber);
        patient.setPatientGender(PatientGender);

        if (PatientGender.contains("Male"))
            patient.setPatientImageUri("https://firebasestorage.googleapis.com/v0/b/hand-pose-estimation-c7958.appspot.com/o/user_m.png?alt=media&token=e4cea38b-a484-4a83-874e-72d30e91fdbc");
        else
            patient.setPatientImageUri("https://firebasestorage.googleapis.com/v0/b/hand-pose-estimation-c7958.appspot.com/o/user_f.png?alt=media&token=163a3db9-8185-4bb8-a207-fb08ab7d12fa");

        mDatabaseRef_Patient
                .child(patientUID)
                .setValue(patient)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        patientBinding.progressCircle.setVisibility(View.GONE);
                        Toasty.success(Add_Patient_Activity.this, "Add Patint Successfully", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(Add_Patient_Activity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show());
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