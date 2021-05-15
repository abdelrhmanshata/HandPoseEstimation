package com.shata.hand_pose_estimation_app.Activity.Login_Register;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shata.hand_pose_estimation_app.Activity.Docter.DocterActivity;
import com.shata.hand_pose_estimation_app.Activity.Patient.PatientActivity;
import com.shata.hand_pose_estimation_app.Models.ModelDataUser;
import com.shata.hand_pose_estimation_app.Models.ModelUsers;
import com.shata.hand_pose_estimation_app.R;
import com.shata.hand_pose_estimation_app.databinding.ActivityRegisterBinding;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding registerBinding;
    private Animation animation;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(registerBinding.getRoot());

        // IInitialize All variables in Register Layout
        Initialize_variables();

        registerBinding.continueRegister.setOnClickListener(v -> {
            continueRegister();
        });

        registerBinding.userLogin.setOnClickListener(v -> {
            onBackPressed();
        });

    }

    public void Initialize_variables() {

        setSupportActionBar(registerBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        firebaseAuth = FirebaseAuth.getInstance();

        animation = AnimationUtils.loadAnimation(this, R.anim.down_to_up);
        registerBinding.cv.setAnimation(animation);

        String text = "Aready have an account ? Login here";
        SpannableString spannableString = new SpannableString(text);
        UnderlineSpan underlineSpan = new UnderlineSpan();
        ForegroundColorSpan mBlue = new ForegroundColorSpan(Color.BLUE);
        spannableString.setSpan(underlineSpan, 25, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(mBlue, 25, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerBinding.userLogin.setText(spannableString);
    }

    public void continueRegister() {

        registerBinding.progressCircle.setVisibility(View.VISIBLE);

        String Username = Objects.requireNonNull(registerBinding.userName.getText()).toString().trim();

        if (Username.isEmpty()) {
            registerBinding.userName.setError("Username Is Required");
            registerBinding.userName.setFocusable(true);
            registerBinding.userName.requestFocus();
            registerBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        String UserAge = Objects.requireNonNull(registerBinding.userAge.getText()).toString().trim();

        if (UserAge.isEmpty()) {
            registerBinding.userAge.setError("User Age Is Required");
            registerBinding.userAge.setFocusable(true);
            registerBinding.userAge.requestFocus();
            registerBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        String UserEmail = Objects.requireNonNull(registerBinding.userEmail.getText()).toString().trim();
        if (UserEmail.isEmpty()) {
            registerBinding.userEmail.setError("Email Is Required");
            registerBinding.userEmail.setFocusable(true);
            registerBinding.userEmail.requestFocus();
            registerBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(UserEmail).matches()) {
            registerBinding.userEmail.setError("Please Enter a Valid Email !!!");
            registerBinding.userEmail.setFocusable(true);
            registerBinding.userEmail.requestFocus();
            registerBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        String UserPassword = Objects.requireNonNull(registerBinding.userPassword.getText()).toString().trim();
        String ConfirmPassword = Objects.requireNonNull(registerBinding.confirmPassword.getText()).toString().trim();
        String checkPassword = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=S+$)" +           //no white spaces
                //".{8,}" +               //at least 8 characters
                "$";

        if (UserPassword.isEmpty()) {
            registerBinding.userPassword.setError("Password Is Required");
            registerBinding.userPassword.setFocusable(true);
            registerBinding.userPassword.requestFocus();
            registerBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        if (UserPassword.length() < 8) {
            registerBinding.userPassword.setError("Minimum Lenght Of Password Should be 8");
            registerBinding.userPassword.setFocusable(true);
            registerBinding.userPassword.requestFocus();
            registerBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        if (UserPassword.matches(checkPassword)) {
            registerBinding.userPassword.setError("Password must not contain spaces !!!");
            registerBinding.userPassword.setFocusable(true);
            registerBinding.userPassword.requestFocus();
            registerBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        if (!ConfirmPassword.equals(UserPassword)) {
            registerBinding.confirmPassword.setError("Password don't match");
            registerBinding.confirmPassword.setFocusable(true);
            registerBinding.confirmPassword.requestFocus();
            registerBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        //Get complete phone number
        String _getUserEnteredPhoneNumber = registerBinding.userPhoneNumber.getText().toString();
        if (TextUtils.isEmpty(_getUserEnteredPhoneNumber)) {
            registerBinding.userPhoneNumber.setError("Phone Number Is Required");
            registerBinding.userPhoneNumber.setFocusable(true);
            registerBinding.userPhoneNumber.requestFocus();
            registerBinding.progressCircle.setVisibility(View.GONE);
            return;
        } else {
            //Remove first zero if entered!
            if (_getUserEnteredPhoneNumber.charAt(0) == '0') {
                _getUserEnteredPhoneNumber = _getUserEnteredPhoneNumber.substring(1);
            }
        }
        //Complete phone number
        String UserPhone = "+" + "20" + _getUserEnteredPhoneNumber;

        if (UserPhone.isEmpty()) {
            registerBinding.userPhoneNumber.setError("Phone Number Is Required");
            registerBinding.userPhoneNumber.setFocusable(true);
            registerBinding.userPhoneNumber.requestFocus();
            registerBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        if (UserPhone.length() < 11) {
            registerBinding.userPhoneNumber.setError("Please Enter a Valid Phone Number");
            registerBinding.userPhoneNumber.setFocusable(true);
            registerBinding.userPhoneNumber.requestFocus();
            registerBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        String Gender = "";
        RadioButton selectedGender;
        if (registerBinding.radioGroupGender.getCheckedRadioButtonId() == -1) {
            Toasty.info(RegisterActivity.this, "Please Select Gender", Toast.LENGTH_SHORT).show();
            return;
        } else {
            selectedGender = findViewById(registerBinding.radioGroupGender.getCheckedRadioButtonId());
            if (selectedGender.getId() == R.id.male) {
                Gender = "Male";
            } else {
                Gender = "Female";
            }
        }

        ModelDataUser modelDataUser = new ModelDataUser();
        modelDataUser.setUserName(Username);
        modelDataUser.setUserAge(UserAge);
        modelDataUser.setUserEmail(UserEmail);
        modelDataUser.setUserPassword(UserPassword);
        modelDataUser.setUserPhone(UserPhone);
        modelDataUser.setUserGender(Gender);
        RegisterUser(modelDataUser);
    }

    public void RegisterUser(ModelDataUser modelDataUser) {
        registerBinding.progressCircle.setVisibility(View.VISIBLE);

        firebaseAuth.createUserWithEmailAndPassword(modelDataUser.getUserEmail(), modelDataUser.getUserPassword())
                .addOnCompleteListener(task -> {

                    registerBinding.progressCircle.setVisibility(View.GONE);
                    if (task.isSuccessful()) {

                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference reference = database.getReference("Users");
                        DatabaseReference databaseReference;

                        if (user != null) {
                            modelDataUser.setUserID(user.getUid());
                            modelDataUser.setUserType(LoginActivity.UserType);
                            if (LoginActivity.UserType.toLowerCase().contains("docter")) {
                                if (modelDataUser.getUserGender().contains("Male"))
                                    modelDataUser.setUserImageUri("https://firebasestorage.googleapis.com/v0/b/hand-pose-estimation-c7958.appspot.com/o/doctor_m.png?alt=media&token=9bc48ff1-cc84-48ae-bc55-ebbf5533df8b");
                                else
                                    modelDataUser.setUserImageUri("https://firebasestorage.googleapis.com/v0/b/hand-pose-estimation-c7958.appspot.com/o/doctor_f.png?alt=media&token=dc3b6013-dbe7-4059-906f-a06af1e5a78c");

                                databaseReference = database.getReference("Doctors");
                                databaseReference.child(modelDataUser.getUserID()).setValue(modelDataUser);
                            } else if (LoginActivity.UserType.toLowerCase().contains("patient")) {
                                if (modelDataUser.getUserGender().contains("Male"))
                                    modelDataUser.setUserImageUri("https://firebasestorage.googleapis.com/v0/b/hand-pose-estimation-c7958.appspot.com/o/user_m.png?alt=media&token=e4cea38b-a484-4a83-874e-72d30e91fdbc");
                                else
                                    modelDataUser.setUserImageUri("https://firebasestorage.googleapis.com/v0/b/hand-pose-estimation-c7958.appspot.com/o/user_f.png?alt=media&token=163a3db9-8185-4bb8-a207-fb08ab7d12fa");

                                databaseReference = database.getReference("Patients");
                                databaseReference.child(modelDataUser.getUserID()).setValue(modelDataUser);
                            }

                            ModelUsers modelUsers = new ModelUsers(modelDataUser.getUserID(), modelDataUser.getUserType());
                            // add info from Class User in dataBase
                            reference.child(modelUsers.getUID()).setValue(modelUsers);

                            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(modelDataUser.getUserName())
                                    .build();

                            user.updateProfile(profile).addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()) {
                                    Toasty.success(RegisterActivity.this, "User Registered Successfull", Toast.LENGTH_SHORT).show();
                                    if (LoginActivity.UserType.toLowerCase().contains("docter")) {
                                        startActivity(new Intent(RegisterActivity.this, DocterActivity.class));
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    } else {
                                        startActivity(new Intent(RegisterActivity.this, PatientActivity.class));
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    }
                                    finish();
                                }
                            });
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toasty.info(RegisterActivity.this, "You Are Already Registered ...", Toast.LENGTH_SHORT).show();
                        } else if (task.getException() instanceof FirebaseNetworkException) {
                            Toasty.warning(RegisterActivity.this, "No Connectionn ... ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toasty.error(RegisterActivity.this, "Exception -> " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e -> {
            registerBinding.progressCircle.setVisibility(View.GONE);
            if (e instanceof FirebaseAuthUserCollisionException) {
                Toasty.info(RegisterActivity.this, "You Are Already Registered ...", Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseNetworkException) {
                Toasty.warning(RegisterActivity.this, "No Connectionn ... ", Toast.LENGTH_SHORT).show();
            } else {
                Toasty.error(RegisterActivity.this, "Exception -> " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        startActivity(new Intent(this, LoginActivity.class));
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
        return;
    }
}