package com.shata.hand_pose_estimation_app.Activity.Patient;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;
import com.shata.hand_pose_estimation_app.Activity.Patient.Fragment.HomePatientFragment;
import com.shata.hand_pose_estimation_app.Activity.Patient.Fragment.ProfilePatientFragment;
import com.shata.hand_pose_estimation_app.Activity.Patient.Fragment.ProgressPatientFragment;
import com.shata.hand_pose_estimation_app.Models.ModelPatient;
import com.shata.hand_pose_estimation_app.R;

public class PatientActivity extends AppCompatActivity {

    public static ModelPatient patient = null;
    SpaceNavigationView spaceNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        patient = (ModelPatient) getIntent().getSerializableExtra("PatientSelected");
        spaceNavigationView = (SpaceNavigationView) findViewById(R.id.space);
        Home();
        spaceNavigationView.setCentreButtonIcon(R.drawable.ic_round_home_24);
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("", R.drawable.ic_baseline_settings_24));
        spaceNavigationView.addSpaceItem(new SpaceItem("", R.drawable.ic_outline_assessment_24));
        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                Home();
                spaceNavigationView.setCentreButtonSelectable(true);
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {

                switch (itemIndex) {
                    case 0:
                        Setting();
                        break;
                    case 1:
                        Progress();
                        break;
                }
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
                switch (itemIndex) {
                    case 0:
                        Setting();
                        break;
                    case 2:
                        Progress();
                        break;
                }
            }
        });
    }

    void Home() {
        HomePatientFragment homePatientFragment = new HomePatientFragment(patient);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homePatientFragment).commit();
    }

    void Setting() {
        ProfilePatientFragment profilePatientFragment = new ProfilePatientFragment(patient);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profilePatientFragment).commit();
    }

    void Progress() {
        ProgressPatientFragment progressPatientFragment = new ProgressPatientFragment(patient);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, progressPatientFragment).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        spaceNavigationView.onSaveInstanceState(outState);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

}