package com.chuanglan.alive.demo.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.chuanglan.alive.demo.BuildConfig;
import com.chuanglan.alive.demo.R;
import com.chuanglan.alive.demo.http.RetrofitHelper;
import com.chuanglan.alive.demo.utils.ScreenUtils;
import com.chuanglan.alive.demo.widget.LoadingDialog;
import com.chuanglan.sdk.tools.LogTool;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeActivity extends Activity {

    private static final String TAG = "HomeActivity<TAG->";
    private static final int REQUEST_CODE_CAMERA = 10000;
    private static final int REQUEST_CODE_STORAGE = 20000;
    private static final int ID_CARD_OCR = 99999;
    private static final int ALIVE_DETECTED = 88888;
    private int mType;
    private static final String GET_TOKEN_URL = BuildConfig.GET_TOKEN_SERVER + "platform/app/token/get";
    private RetrofitHelper mRetrofit = new RetrofitHelper();
    private LoadingDialog mLoadingDialog;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mLoadingDialog = new LoadingDialog(this);
        findViewById(R.id.btnStartDetected).setOnClickListener(v -> {
            mType = ALIVE_DETECTED;
            getToken();
        });
        findViewById(R.id.btnCardDetected).setOnClickListener(v -> {
            mType = ID_CARD_OCR;
            getToken();
        });
    }

    private void dismissLoading() {
        runOnUiThread(() -> {
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);

        } else {
            if (mType == ID_CARD_OCR) {
                Intent intent = new Intent(this, IdCardOcrTestActivity.class);
                intent.putExtra("authToken", authToken);
                startActivity(intent);
            } else if (mType == ALIVE_DETECTED) {
                Intent intent = new Intent(this, OnlineAliveDetectedTestActivity.class);
                intent.putExtra("authToken", authToken);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    Toast.makeText(getApplicationContext(), "您未授予相机权限，请到设置中开启权限", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 为了安全期间，此接口建议放到服务端进行请求，此处是为了方便测试就直接请求了
     */
    private void getToken() {
        mLoadingDialog.show();
        Map<String, String> bodyMap = new HashMap<>(3);
        bodyMap.put("appKey", BuildConfig.APPID);
        bodyMap.put("appSecret", BuildConfig.APPKEY);
        bodyMap.put("did", String.valueOf(System.currentTimeMillis()));

        Call<ResponseBody> call = mRetrofit.getAuthToken(GET_TOKEN_URL, bodyMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dismissLoading();
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    LogTool.d(TAG, "getToken response" + responseData);
                    String code = json.optString("code");
                    if ("000000".equals(code)) {
                        JSONObject data = json.optJSONObject("data");
                        authToken = data.optString("authToken");
                        checkPermissions();
                    } else {
                        String msg = json.optString("message");
                        ScreenUtils.showToast(getApplicationContext(), msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                LogTool.e(TAG, "onFailure()call=" + t.toString());
                ScreenUtils.showToast(getApplicationContext(), "网络异常");
                dismissLoading();
            }
        });
    }
}
