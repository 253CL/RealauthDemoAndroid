package com.chuanglan.alive.demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.chuanglan.alive.demo.R;


public class OnlineAliveDetectedSuccessActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alive_detected_success);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("认证结果");
        findViewById(R.id.img_btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.btnToHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}