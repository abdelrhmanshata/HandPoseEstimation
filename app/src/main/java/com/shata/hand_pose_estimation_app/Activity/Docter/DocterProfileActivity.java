package com.shata.hand_pose_estimation_app.Activity.Docter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.shata.hand_pose_estimation_app.R;

public class DocterProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docter_profile);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}