package com.chuanglan.alive.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.chuanglan.alive.demo.R;

import androidx.annotation.Nullable;

/**
 * @author yychen
 * @date 2021/9/15 16:54
 */
public class OnlineAliveDetectedTestActivity extends Activity {

    private String authToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alive_detected_test);
        findViewById(R.id.img_btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btnStartDetected).setOnClickListener(v -> {
            Intent intent = new Intent(OnlineAliveDetectedTestActivity.this, OnlineAliveDetectedActivity.class);
            intent.putExtra("authToken", authToken);
            startActivity(intent);
            finish();
        });
        authToken = getIntent().getStringExtra("authToken");
    }
}
