package com.shata.hand_pose_estimation_app.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.shata.hand_pose_estimation_app.Models.ModelPatient;
import com.shata.hand_pose_estimation_app.R;
import com.shata.hand_pose_estimation_app.databinding.ActivityAddPatientBinding;
import com.shata.hand_pose_estimation_app.databinding.ActivityViewPatientBinding;

public class ViewPatientActivity extends AppCompatActivity {

    ActivityViewPatientBinding viewPatientBinding;

    ModelPatient patient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewPatientBinding = ActivityViewPatientBinding.inflate(getLayoutInflater());
        setContentView(viewPatientBinding.getRoot());

        patient = (ModelPatient) getIntent().getSerializableExtra("ObjectPatient");

        GraphView graphView = findViewById(R.id.graph);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(1,5),
                new DataPoint(2,2),
                new DataPoint(3,4),
                new DataPoint(4,1),
                new DataPoint(5,6)
        });

        graphView.addSeries(series);

        viewPatientBinding.patientTv.setText(patient.toString());


    }

    public void Initialize_variables() {



    }

}