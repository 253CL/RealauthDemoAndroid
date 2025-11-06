package com.chuanglan.alive.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.chuanglan.alive.demo.R;
import com.chuanglan.alive.demo.constant.IntentExtra;

/**
 * 二要素认证失败页面
 */
public class RealPeopleVerifyFailed extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_people_verify_failed);
        findViewById(R.id.img_btn_back).setOnClickListener(v -> finish());
        TextView tvFailedReason = findViewById(R.id.tvFailedReason);
        String errorMsg = getIntent().getStringExtra(IntentExtra.ERROR_MSG);
        tvFailedReason.setText(errorMsg);
        findViewById(R.id.btnVerifyAgain).setOnClickListener(v -> {
            Intent intent = new Intent(RealPeopleVerifyFailed.this, RealPeopleVerifyActivity.class);
            startActivity(intent);
            finish();
        });
    }
}