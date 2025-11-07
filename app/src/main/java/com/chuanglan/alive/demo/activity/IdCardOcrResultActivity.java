package com.chuanglan.alive.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chuanglan.alive.demo.R;
import com.chuanglan.alive.demo.constant.IntentExtra;
import com.chuanglan.alive.demo.entities.IdCardEntity;
import com.chuanglan.alive.demo.utils.IDUtils;
import com.chuanglan.alivedetected.api.IdentityAuthApi;
import com.chuanglan.alivedetected.interfaces.IDetectedListener;
import com.chuanglan.sdk.tools.LogTool;
import com.google.gson.Gson;

import org.json.JSONObject;

public class IdCardOcrResultActivity extends Activity {
    private static final String TAG = "IdCardOcrResultActivity<TAG->";
    private String name;
    private String idCardNo;
    private TextView tvName;
    private TextView tvSex;
    private TextView tvNation;
    private TextView tvBirthday;
    private TextView tvAddress;
    private TextView tvIdNum;
    private TextView tvIssuingAuthority;
    private TextView tvExpireDate;
    private ProgressBar progressBar;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_card_ocr_result);
        findViewById(R.id.img_btn_back).setOnClickListener(v -> finish());
        initViews();
        initData();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvSex = findViewById(R.id.tvSex);
        tvNation = findViewById(R.id.tvNation);
        tvBirthday = findViewById(R.id.tvBirthday);
        tvAddress = findViewById(R.id.tvAddress);
        tvIdNum = findViewById(R.id.tvIdNum);
        tvIssuingAuthority = findViewById(R.id.tvIssuingAuthority);
        tvExpireDate = findViewById(R.id.tvExpireDate);
        progressBar = findViewById(R.id.progressBar);
        findViewById(R.id.btnVerify).setOnClickListener(v -> toVerify());
    }

    private void toVerify() {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!IDUtils.isIdNumber(idCardNo)) {
            Toast.makeText(getApplicationContext(), "请输入正确的身份证号码", Toast.LENGTH_SHORT).show();
            return;
        }

        setProgressBar(true);

        IdentityAuthApi.getInstance().verify(authToken, name, idCardNo, new IDetectedListener() {
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

    private void initData() {
        try {
            Intent intent = getIntent();
            String result = intent.getStringExtra("result");
            authToken = intent.getStringExtra("authToken");
            IdCardEntity idCardEntity = new Gson().fromJson(result, IdCardEntity.class);
            IdCardEntity.DataBean data = idCardEntity.getData();
            if (data == null) {
                Toast.makeText(getApplicationContext(), "数据缺失", Toast.LENGTH_SHORT).show();
                return;
            }
            IdCardEntity.DataBean.FrontBean front = data.getFront();
            IdCardEntity.DataBean.BackBean back = data.getBack();
            Object frontMsg = front.getMsg();
            if (frontMsg != null && !"不完整".equals(frontMsg)) {
                Toast.makeText(getApplicationContext(), "正面" + frontMsg, Toast.LENGTH_SHORT).show();
            } else {
                name = front.getName();
                String address = front.getAddress();
                String sex = front.getSex();
                String nation = front.getNation();
                String birthday = front.getBrith_day();
                idCardNo = front.getId_card_no();

                tvName.setText(name);
                tvSex.setText(sex);
                tvNation.setText(nation);
                tvBirthday.setText(birthday);
                tvAddress.setText(address);
                tvIdNum.setText(idCardNo);
            }

            Object backMsg = back.getMsg();
            if (backMsg != null && !"不完整".equals(backMsg)) {
                Toast.makeText(getApplicationContext(), "背面" + backMsg, Toast.LENGTH_SHORT).show();
            } else {
                String issuingAuthority = back.getIssuing_authority();
                String issuingDate = back.getIssuing_date();
                String expireDate = back.getExpire_date();
                tvIssuingAuthority.setText(issuingAuthority);
                tvExpireDate.setText(issuingDate + "-" + expireDate);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        Intent intent = new Intent(this, OnlineAliveDetectedSuccessActivity.class);
        startActivity(intent);
        finish();
    }

    private void onFailedCallback(String reason) {
        Intent intent = new Intent(this, RealPeopleVerifyFailed.class);
        intent.putExtra(IntentExtra.ERROR_MSG, reason);
        startActivity(intent);
        finish();
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
}