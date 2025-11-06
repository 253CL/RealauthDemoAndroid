package com.chuanglan.alive.demo.activity;

import android.app.Activity;
import android.os.Bundle;

import com.chuanglan.alive.demo.R;


/**
 * 二要素认证成功页面
 */
public class RealPeopleVerifySuccess extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_people_verify_success);
        findViewById(R.id.img_btn_back).setOnClickListener(v -> finish());
    }
}