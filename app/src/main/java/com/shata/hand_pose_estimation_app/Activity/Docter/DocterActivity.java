package com.shata.hand_pose_estimation_app.Activity.Docter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shata.hand_pose_estimation_app.Activity.Login_Register.LoginActivity;
import com.shata.hand_pose_estimation_app.Activity.Patient.PatientActivity;
import com.shata.hand_pose_estimation_app.Activity.UI.AboutActivity;
import com.shata.hand_pose_estimation_app.Adapters.Adapter_All_Patient;
import com.shata.hand_pose_estimation_app.Models.ModelDataUser;
import com.shata.hand_pose_estimation_app.Models.ModelPatient;
import com.shata.hand_pose_estimation_app.R;
import com.shata.hand_pose_estimation_app.databinding.ActivityDocterBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DocterActivity extends AppCompatActivity implements Adapter_All_Patient.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static String NameSearch = "";

    ActivityDocterBinding docterBinding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DrawerLayout drawerLayout;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser user = firebaseAuth.getCurrentUser();
    DatabaseReference mDatabaseRef = database.getReference("DoctorPatients");
    DatabaseReference reference = database.getReference("Doctors");
    ValueEventListener mDBListener;
    List<ModelPatient> mAllPatients;
    Adapter_All_Patient allPatientAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    ImageView UserImage;
    TextView UserName, UserEmail;
    ActionBarDrawerToggle actionBarDrawerToggle;

    // This Method is for Search in RealTime
    private final TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, int start, int before, int count) {
            mDBListener = mDatabaseRef
                    .child(user.getUid())
                    .orderByChild("patientName")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mAllPatients.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                ModelPatient patient = snapshot.getValue(ModelPatient.class);
                                if (patient != null && patient.getPatientName().toLowerCase().contains(s))
                                    mAllPatients.add(patient);
                            }
                            allPatientAdapter.notifyDataSetChanged();
                            if (mAllPatients.isEmpty())
                                docterBinding.patientRecyclerviewImage.setVisibility(View.VISIBLE);
                            else
                                docterBinding.patientRecyclerviewImage.setVisibility(View.GONE);
                            NameSearch = "";
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(DocterActivity.this, "Error!" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        docterBinding = ActivityDocterBinding.inflate(getLayoutInflater());
        setContentView(docterBinding.getRoot());

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Docter");
        setSupportActionBar(toolbar);

        Initialize_variables();
        loadInfoUser();
        loadingAllPatient();

        navigationView.setNavigationItemSelectedListener(menuItem -> {

            switch (menuItem.getItemId()) {

                case R.id.profile:
                    startActivity(new Intent(DocterActivity.this, DocterProfileActivity.class));
                    this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    break;

                case R.id.info:
                    startActivity(new Intent(DocterActivity.this, AboutActivity.class));
                    this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    break;

                case R.id.logout:
                    Toast.makeText(DocterActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                    finish();
                    startActivity(new Intent(DocterActivity.this, LoginActivity.class));
                    this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    break;
            }
            return true;
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        docterBinding.addNewPatient.setOnClickListener(v ->
        {
            startActivity(new Intent(this, Add_Patient_Activity.class));
            this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void Initialize_variables() {
        drawerLayout = findViewById(R.id.activityDocter);
        navigationView = findViewById(R.id.nav_view);
        View headerLayout = navigationView.getHeaderView(0);
        UserImage = headerLayout.findViewById(R.id.userImage);
        UserName = headerLayout.findViewById(R.id.userName);
        UserEmail = headerLayout.findViewById(R.id.userEmail);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        docterBinding.patientRecyclerview.setHasFixedSize(true);
        docterBinding.patientRecyclerview.setLayoutManager(new LinearLayoutManager(this));

        mAllPatients = new ArrayList<>();

        allPatientAdapter = new Adapter_All_Patient(this, mAllPatients);
        docterBinding.patientRecyclerview.setAdapter(allPatientAdapter);
        allPatientAdapter.setOnItemClickListener(this);

        docterBinding.patientSearch.addTextChangedListener(searchTextWatcher);
    }

    public void loadInfoUser() {
        reference.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelDataUser user = snapshot.getValue(ModelDataUser.class);
                        if (user != null) {
                            Picasso.get().load(user.getUserImageUri().trim())
                                    .into(UserImage);
                            UserName.setText(user.getUserName());
                            UserEmail.setText(user.getUserEmail());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void loadingAllPatient() {
        mDBListener = mDatabaseRef
                .child(user.getUid())
                .orderByChild("patientName")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mAllPatients.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ModelPatient patient = snapshot.getValue(ModelPatient.class);
                            if (patient != null)
                                mAllPatients.add(patient);
                        }
                        allPatientAdapter.notifyDataSetChanged();
                        if (mAllPatients.isEmpty())
                            docterBinding.patientRecyclerviewImage.setVisibility(View.VISIBLE);
                        else
                            docterBinding.patientRecyclerviewImage.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DocterActivity.this, "Error!" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onItem_Product_Click(int position) {
        ModelPatient patient = mAllPatients.get(position);
        Intent intentViewPatient = new Intent(this, PatientActivity.class);
        intentViewPatient.putExtra("PatientSelected", patient);
        startActivity(intentViewPatient);
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        allPatientAdapter.notifyDataSetChanged();
        docterBinding.patientSearch.setText(NameSearch + "");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(() -> {
            mSwipeRefreshLayout.setRefreshing(false);
            loadingAllPatient();
        }, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
        mAllPatients.clear();
    }
}