package com.chuanglan.alive.demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chuanglan.alivedetected.api.IdentityAuthApi;
import com.chuanglan.alivedetected.interfaces.IDetectedListener;
import com.chuanglan.alive.demo.R;
import com.chuanglan.alive.demo.constant.IntentExtra;
import com.chuanglan.alive.demo.utils.IDUtils;
import com.chuanglan.alive.demo.utils.ImageUtils;
import com.chuanglan.alive.demo.utils.SharedPref;
import com.chuanglan.sdk.tools.LogTool;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class RealPeopleVerifyActivity extends Activity {

    private static final String TAG = "RealPeopleVerify<TAG->";
    private ImageView ivIdCard;
    private EditText etName;
    private EditText etIdNum;
    private boolean isContinue;
    private ProgressBar progressBar;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_people_verify);
        ImageView ivBack = findViewById(R.id.img_btn_back);
        progressBar = findViewById(R.id.progressBar);
        ivBack.setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("识别结果");
        etName = findViewById(R.id.etName);
        etIdNum = findViewById(R.id.etIdNum);
        ivIdCard = findViewById(R.id.ivIdCard);
        LinearLayout llTitle = findViewById(R.id.llTitle);
        isContinue = getIntent().getBooleanExtra("isContinue", false);
        authToken = getIntent().getStringExtra("authToken");
        findViewById(R.id.btnVerify).setOnClickListener(v -> toVerify());
        if (!isContinue) {
            initUserInfo();
        } else {
            ivIdCard.setVisibility(View.GONE);
        }
    }

    private void initUserInfo() {
        String name = SharedPref.get(getApplicationContext(), "name", "");
        String idNum = SharedPref.get(getApplicationContext(), "id_num", "");
        etName.setText(name);
        etIdNum.setText(idNum);

        String filePath = ImageUtils.getFrontFile(getApplicationContext()).getAbsolutePath();
        ivIdCard.setImageBitmap(getBitmapFromFile(filePath));
    }

    private void updateUserInfo(String name, String idNum) {
        SharedPref.put(getApplicationContext(), "name", name);
        SharedPref.put(getApplicationContext(), "id_num", idNum);
    }

    public Bitmap getBitmapFromFile(String filePath) {
        try {
            return BitmapFactory.decodeStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void toVerify() {
        // 重新取一下姓名和身份证号码，有可能识别出来的有错误，会手动修改
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String idNum = etIdNum.getText().toString().trim();
        if (!IDUtils.isIdNumber(idNum)) {
            Toast.makeText(getApplicationContext(), "请输入正确的身份证号码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新一下用户的姓名和身份证号码，因为用户可能手动修改
        updateUserInfo(name, idNum);

        setProgressBar(true);

        IdentityAuthApi.getInstance().verify(authToken, name, idNum, new IDetectedListener() {
            @Override
            public void onSuccess(String result) {
                setProgressBar(false);
                LogTool.d(TAG, "json:" + result);
                parseDataOnSuccess(result);
            }

            @Override
            public void onFailed(int errorCode, String errorMsg) {
                LogTool.e(TAG, "onFailure():call=" + errorCode);
                setProgressBar(false);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setProgressBar(boolean visible) {
        runOnUiThread(() -> {
            if (visible) {
                showLoading();
            } else {
                dismissLoading();
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void dismissLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void parseDataOnSuccess(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String code = jsonObject.optString("code");
            if ("000000".equals(code)) {
                String data = jsonObject.getString("data");
                JSONObject dataObject = new JSONObject(data);
                String result = dataObject.optString("result");
                if (!TextUtils.isEmpty(result)) {
                    if ("01".equals(result)) {
                        onSucceedCallback();
                    } else {
                        jumpToRealPeopleVerifyFailedPage(result);
                    }
                } else {
                    final String dataStr = jsonObject.optString("data");
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), dataStr, Toast.LENGTH_SHORT).show();
                        dismissLoading();
                    });
                }
            } else {
                String message = jsonObject.optString("message");
                jumpToRealPeopleVerifyFailedPage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onSucceedCallback() {
        if (isContinue) {
            startAliveDetected();
        } else {
            Intent intent = new Intent(this, RealPeopleVerifySuccess.class);
            startActivity(intent);
            finish();
        }
    }

    private void onFailedCallback(String reason) {
        Intent intent = new Intent(this, RealPeopleVerifyFailed.class);
        intent.putExtra(IntentExtra.ERROR_MSG, reason);
        startActivity(intent);
        finish();
    }

    private void startAliveDetected() {

    }

    private void jumpToRealPeopleVerifyFailedPage(String result) {
        String errorMsg = "认证失败";
        if ("02".equals(result)) {
            errorMsg = "姓名、身份证号码不一致";
        } else if (!TextUtils.isEmpty(result)) {
            errorMsg = result;
        }

        onFailedCallback(errorMsg);
    }

    public static void actionStart(Context context, String authToken) {
        try {
            Intent intent = new Intent(context, RealPeopleVerifyActivity.class);
            intent.putExtra("authToken", authToken);
            if (!(context instanceof Activity)) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}