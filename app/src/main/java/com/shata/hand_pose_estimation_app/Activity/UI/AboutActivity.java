package com.shata.hand_pose_estimation_app.Activity.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.shata.hand_pose_estimation_app.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}