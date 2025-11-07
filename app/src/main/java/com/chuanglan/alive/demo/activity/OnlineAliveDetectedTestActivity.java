package com.chuanglan.alive.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chuanglan.alive.demo.R;
import com.chuanglan.sdk.constant.LogConstant;
import com.chuanglan.sdk.tools.LogTool;

import androidx.annotation.Nullable;

/**
 * @author yychen
 * @date 2021/9/15 16:54
 */
public class OnlineAliveDetectedTestActivity extends Activity {
    private RelativeLayout mBackRootLayout, mPrivacyText, mCheckboxRootLayout, mPrivacyRootLayout;
    private Button mStartBtn;
    private CheckBox mCheckBox;
    private TextView mTipText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_alive_detected_test);
            initView();
            setListener();
        } catch (Exception e) {
            e.printStackTrace();
            finishtask();
        }
    }

    private void initView() {
        mBackRootLayout = findViewById(R.id.face_verify_titlebar_back_root);
        mStartBtn = findViewById(R.id.face_verify_start);
        mPrivacyText = findViewById(R.id.face_verify_privacy_text_rootlayout);
        mCheckboxRootLayout = findViewById(R.id.face_verify_privacy_checkbox_rootlayout);
        mCheckBox = findViewById(R.id.face_verify_privacy_checkbox);
        mPrivacyRootLayout = findViewById(R.id.face_verify_privacy_rootlayout);
        mTipText = findViewById(R.id.face_verify_privacy_tip);
    }

    private void setListener() {
        mBackRootLayout.setOnClickListener(view -> {
            finishtask();
        });
        mStartBtn.setOnClickListener(view -> {
            if (mCheckBox.isChecked()) {
                Intent intent = new Intent(OnlineAliveDetectedTestActivity.this, OnlineAliveDetectedActivity.class);
                String authToken = getIntent().getStringExtra("authToken");
                intent.putExtra("authToken", authToken);
                startActivity(intent);
                finishtask();
            } else {
                mTipText.setVisibility(View.VISIBLE);
                //协议未勾选时，抖动提醒
                mPrivacyRootLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.face_verify_transla_checkbox));
            }
        });
        mCheckboxRootLayout.setOnClickListener(view -> mCheckBox.performClick());
        mPrivacyText.setOnClickListener(view -> {
            Intent intent = new Intent(OnlineAliveDetectedTestActivity.this, PrivacyActivity.class);
            startActivity(intent);
        });
        mCheckBox.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                mTipText.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 解决进入后台后显任务列表显示两个APP的问题
     */
    private void finishtask() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                finish();
            }
        } catch (Exception e) {
            LogTool.d(LogConstant.EXCEPTION_LOGTAG, "GuidanceActivity finishtask Exception-->", e);
            e.printStackTrace();
        }
    }
}
