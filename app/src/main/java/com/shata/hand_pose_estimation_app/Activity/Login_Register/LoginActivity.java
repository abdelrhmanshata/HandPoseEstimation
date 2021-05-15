package com.shata.hand_pose_estimation_app.Activity.Login_Register;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shata.hand_pose_estimation_app.Activity.Docter.DocterActivity;
import com.shata.hand_pose_estimation_app.Activity.Patient.PatientActivity;
import com.shata.hand_pose_estimation_app.Models.ModelDataUser;
import com.shata.hand_pose_estimation_app.Models.ModelUsers;
import com.shata.hand_pose_estimation_app.R;
import com.shata.hand_pose_estimation_app.databinding.ActivityLoginBinding;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    private final static int RC_SIGN_IN = 123;
    public static String UserType = "";

    ActivityLoginBinding loginBinding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference = database.getReference("Users");

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private AlertDialog alertDialog_RecoverPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(loginBinding.getRoot());

        UserType = getIntent().getStringExtra("Type");

        // IInitialize All variables in Login Layout
        Initialize_variables();

        String text = "Have not account? Register";
        SpannableString spannableString = new SpannableString(text);
        UnderlineSpan underlineSpan = new UnderlineSpan();
        ForegroundColorSpan mBlue = new ForegroundColorSpan(Color.BLUE);
        spannableString.setSpan(underlineSpan, 18, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(mBlue, 18, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        loginBinding.userRegister.setText(spannableString);

        loginBinding.userLogin.setOnClickListener(v -> loginAccount());
        loginBinding.loginFaceBoook.setOnClickListener(v -> loginFaceBookAccount());
        loginBinding.loginGoogle.setOnClickListener(v -> loginGoogleAccount());
        loginBinding.loginTwitter.setOnClickListener(v -> loginTwitterAccount());
        loginBinding.forgetPassword.setOnClickListener(v -> {
            showDialog_RecoverPassword();
        });
        loginBinding.userRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    public void Initialize_variables() {

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void loginAccount() {

        String UserEmail = Objects.requireNonNull(loginBinding.userEmail.getText()).toString().trim();
        String UserPassword = Objects.requireNonNull(loginBinding.userPassword.getText()).toString().trim();

        loginBinding.progressCircle.setVisibility(View.VISIBLE);

        if (UserEmail.isEmpty()) {
            loginBinding.userEmail.setError("Email Is Required");
            loginBinding.userEmail.requestFocus();
            loginBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(UserEmail).matches()) {
            loginBinding.userEmail.setError("Please Enter a Valid Email");
            loginBinding.userEmail.requestFocus();
            loginBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        if (UserPassword.isEmpty()) {
            loginBinding.userPassword.setError("Password Is Required");
            loginBinding.userPassword.requestFocus();
            loginBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        if (UserPassword.length() < 8) {
            loginBinding.userPassword.setError("Minimum Lenght Of Password Should be 8");
            loginBinding.userPassword.requestFocus();
            loginBinding.progressCircle.setVisibility(View.GONE);
            return;
        }

        mAuth.signInWithEmailAndPassword(UserEmail, UserPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reference
                                .child(mAuth.getCurrentUser().getUid())
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        ModelUsers user = snapshot.getValue(ModelUsers.class);
                                        if (user != null) {
                                            loginBinding.progressCircle.setVisibility(View.GONE);
                                            if (user.getUserType().toLowerCase().contains("docter")) {
                                                startActivity(new Intent(LoginActivity.this, DocterActivity.class));
                                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                            } else if (user.getUserType().toLowerCase().contains("patient")) {
                                                startActivity(new Intent(LoginActivity.this, PatientActivity.class));
                                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                            }
                                            loginBinding.progressCircle.setVisibility(View.GONE);
                                            finish();
                                        } else {
                                            loginBinding.progressCircle.setVisibility(View.GONE);
                                            Toast.makeText(LoginActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        loginBinding.progressCircle.setVisibility(View.GONE);
                                        Log.d("Login",error.getMessage());
                                    }
                                });
/*
                if (mAuth.getCurrentUser().isEmailVerified()) {
                    finish();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                } else {
                    Toasty.info(LoginActivity.this, "Please Verified Email \n Verification Code Sent To " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                }
*/
                    } else if (task.getException() instanceof FirebaseNetworkException) {
                        Toasty.error(LoginActivity.this, "No Connectionn ... ", Toast.LENGTH_SHORT).show();
                    } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        Toasty.info(LoginActivity.this, "This User Not Found ... ", Toast.LENGTH_SHORT).show();
                    } else if ((task.getException() instanceof FirebaseAuthInvalidCredentialsException)) {
                        Toasty.warning(LoginActivity.this, "This Password Not Right ... ", Toast.LENGTH_SHORT).show();
                    } else if (task.getException() instanceof FirebaseTooManyRequestsException) {
                        Toasty.custom(LoginActivity.this, "You Try Enter Password Many Times \n Try After One Minute", getResources().getDrawable(R.drawable.ic_timer_24), Toast.LENGTH_SHORT, true).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error+->" + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                    loginBinding.progressCircle.setVisibility(View.GONE);
                });
    }

    private void loginFaceBookAccount() {
        Toasty.info(this, "Login Facebook Soon...", Toast.LENGTH_SHORT, true).show();
    }

    private void loginGoogleAccount() {
        Toasty.info(this, "Login Google Soon...", Toast.LENGTH_SHORT, true).show();
       /* loginBinding.progressCircle.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);*/
    }

    private void loginTwitterAccount() {
        Toasty.warning(this, "LoginTwitter Soon...", Toast.LENGTH_SHORT).show();
    }

    public void showDialog_RecoverPassword() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        View layout = getLayoutInflater().inflate(R.layout.activity_recover_password, null);

        TextInputEditText userEmail = layout.findViewById(R.id.userEmail);
        Button btRecover = layout.findViewById(R.id.btRecover);
        Button btCancel = layout.findViewById(R.id.btCancel);
        ProgressBar progressBar = layout.findViewById(R.id.progressCircle);

        btRecover.setOnClickListener(view -> RecoverPassword(userEmail, progressBar));

        btCancel.setOnClickListener(v -> {
            alertDialog_RecoverPassword.dismiss();
            alertDialog_RecoverPassword.cancel();
        });

        alertBuilder.setView(layout);
        alertDialog_RecoverPassword = alertBuilder.create();
        alertDialog_RecoverPassword.show();
    }

    public void RecoverPassword(TextInputEditText userEmail, ProgressBar progressBar) {

        String EmailAddress = Objects.requireNonNull(userEmail.getText()).toString().trim();
        progressBar.setVisibility(View.VISIBLE);

        if (EmailAddress.isEmpty()) {
            userEmail.setError("Email Is Required");
            userEmail.setFocusable(true);
            userEmail.requestFocus();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(EmailAddress).matches()) {
            userEmail.setError("Please Enter a Valid Email");
            userEmail.setFocusable(true);
            userEmail.requestFocus();
            progressBar.setVisibility(View.GONE);
            return;
        }

        mAuth.sendPasswordResetEmail(EmailAddress).addOnCompleteListener(task -> {

            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toasty.info(LoginActivity.this, "Check the messages on the e-mail : \n" + EmailAddress, Toast.LENGTH_SHORT).show();
                alertDialog_RecoverPassword.dismiss();
                alertDialog_RecoverPassword.cancel();
            } else {
                Toasty.error(LoginActivity.this, "Falid ..." + Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toasty.error(LoginActivity.this, "Error ->" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = (GoogleSignInAccount) task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);
            } catch (Throwable e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Error -->" + e.getMessage(), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Error -->" + e, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        // if user is singin frist time
                        if (Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getAdditionalUserInfo()).isNewUser()) {

                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            DatabaseReference databaseReference;
                            if (user != null) {

                                ModelDataUser modelDataUser = new ModelDataUser();
                                modelDataUser.setUserID(user.getUid());
                                modelDataUser.setUserEmail(user.getEmail());
                                modelDataUser.setUserName(user.getDisplayName());
                                modelDataUser.setUserImageUri(user.getPhotoUrl().toString());
                                modelDataUser.setUserType(UserType);
                                modelDataUser.setUserPhone(user.getPhoneNumber());

                                if (UserType.toLowerCase().contains("docter")) {
                                    databaseReference = database.getReference("Docter");
                                    databaseReference.child(modelDataUser.getUserID()).setValue(modelDataUser);
                                } else if (UserType.toLowerCase().contains("patient")) {
                                    databaseReference = database.getReference("Patient");
                                    databaseReference.child(modelDataUser.getUserID()).setValue(modelDataUser);
                                }

                                ModelUsers modelUsers = new ModelUsers(modelDataUser.getUserID(), modelDataUser.getUserType());
                                // add info from Class User in dataBase
                                reference.child(modelUsers.getUID()).setValue(modelUsers);
                            }
                        }

                        loginBinding.progressCircle.setVisibility(View.GONE);

                        if (UserType.toLowerCase().contains("docter")) {
                            startActivity(new Intent(LoginActivity.this, DocterActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        } else {
                            startActivity(new Intent(LoginActivity.this, PatientActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                        finish();

                    } else {
                        // If sign in fails, display a message to the user.
                        loginBinding.progressCircle.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Login Failed ...", Toast.LENGTH_SHORT).show();
                        Toast.makeText(LoginActivity.this, "Error -> " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
            loginBinding.progressCircle.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this, "Error ->" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}