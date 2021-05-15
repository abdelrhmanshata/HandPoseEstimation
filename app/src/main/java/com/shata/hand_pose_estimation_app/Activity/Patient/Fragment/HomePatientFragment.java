package com.shata.hand_pose_estimation_app.Activity.Patient.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.shata.hand_pose_estimation_app.Activity.UI.CameraActivity;
import com.shata.hand_pose_estimation_app.Activity.UI.ImageActivity;
import com.shata.hand_pose_estimation_app.Models.ModelPatient;
import com.shata.hand_pose_estimation_app.R;
import com.shata.hand_pose_estimation_app.databinding.FragmentPatientHomeBinding;

import java.util.Objects;

public class HomePatientFragment extends Fragment {

    FragmentPatientHomeBinding patientHomeBinding;
    ModelPatient patient = null;

    public HomePatientFragment() {
    }

    public HomePatientFragment(ModelPatient patient) {
        this.patient = patient;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        patientHomeBinding = FragmentPatientHomeBinding.inflate(getLayoutInflater(), container, false);
        Initialize_variables();
        return patientHomeBinding.getRoot();
    }

    private void Initialize_variables() {

        patientHomeBinding.layoutTakePhoto.setOnClickListener(v -> {
            startActivity(new Intent(getActivity().getApplicationContext(), CameraActivity.class).putExtra("ObjectPatient", patient));
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        patientHomeBinding.layoutUploadPhoto.setOnClickListener(v -> {
            startActivity(new Intent(Objects.requireNonNull(getActivity()).getApplicationContext(), ImageActivity.class).putExtra("ObjectPatient", patient));
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

    }
}