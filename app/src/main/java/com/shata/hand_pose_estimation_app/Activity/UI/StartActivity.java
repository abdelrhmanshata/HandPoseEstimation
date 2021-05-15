package com.shata.hand_pose_estimation_app.Activity.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shata.hand_pose_estimation_app.Activity.Docter.DocterActivity;
import com.shata.hand_pose_estimation_app.Activity.Login_Register.LoginActivity;
import com.shata.hand_pose_estimation_app.Activity.Patient.PatientActivity;
import com.shata.hand_pose_estimation_app.Models.ModelUsers;
import com.shata.hand_pose_estimation_app.R;
import com.shata.hand_pose_estimation_app.databinding.ActivityStartBinding;

public class StartActivity extends AppCompatActivity {

    ActivityStartBinding startBinding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startBinding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(startBinding.getRoot());

        if (firebaseUser != null) {
            startBinding.constraintLayoutStart.setBackgroundColor(getResources().getColor(R.color.white));
            startBinding.layoutWhoLogging.setVisibility(View.GONE);

            Thread welcome = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                databaseReference
                        .child(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ModelUsers user = snapshot.getValue(ModelUsers.class);
                                if (user != null) {
                                    if (user.getUserType().toLowerCase().contains("docter")) {
                                        startActivity(new Intent(StartActivity.this, DocterActivity.class));
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    } else if (user.getUserType().toLowerCase().contains("patient")) {
                                        startActivity(new Intent(StartActivity.this, PatientActivity.class));
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    }
                                    finish();
                                } else {
                                    Toast.makeText(StartActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(StartActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            });
            welcome.start();
        } else {
            startBinding.constraintLayoutStart.setBackgroundColor(getResources().getColor(R.color.LigtGreen));
            startBinding.layoutIsLogging.setVisibility(View.GONE);
            startBinding.btDocterStart.setOnClickListener(v ->
                    {
                        Toast.makeText(StartActivity.this, "Docter", Toast.LENGTH_SHORT).show();
                        Intent intentDocter = new Intent(StartActivity.this, LoginActivity.class);
                        intentDocter.putExtra("Type", "Docter");
                        startActivity(intentDocter);
                    }
            );

            startBinding.btPatientStart.setOnClickListener(v ->
                    {
                        Toast.makeText(StartActivity.this, "Patient", Toast.LENGTH_SHORT).show();
                        Intent intentDocter = new Intent(StartActivity.this, LoginActivity.class);
                        intentDocter.putExtra("Type", "Patient");
                        startActivity(intentDocter);
                    }
            );
        }
    }
}