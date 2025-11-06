package com.chuanglan.alive.demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chuanglan.alive.demo.R;
import com.chuanglan.alive.demo.constant.IntentExtra;
import com.chuanglan.alive.demo.utils.ImageUtils;
import com.chuanglan.alive.demo.utils.SharedPref;
import com.chuanglan.alivedetected.api.IdCardApi;
import com.chuanglan.alivedetected.interfaces.IDetectedListener;
import com.chuanglan.sdk.tools.LogTool;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * 上传身份证照片页面
 */
public class UploadIdCardActivity extends Activity {

    private static final String TAG = "UploadIdCardActivity<TAG->";
    private static final String FRONT = "front";
    private static final String BACK = "back";
    private String mCurrentType;
    private String authToken;
    private ImageView ivBack;
    private ImageView ivIdCard;
    private ImageView ivIdCardBack;
    private TextView tvBack;
    private String mFilePath;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_id_card);
        tvBack = findViewById(R.id.tvBack);
        progressBar = findViewById(R.id.progressBar);
        Button btnNext = findViewById(R.id.btnNext);
        ivIdCard = findViewById(R.id.ivIdCard);
        Button btnTakeAgain = findViewById(R.id.btnTakeAgain);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(getString(R.string.online_alive_detect_upload_id_card));
        LinearLayout llTitle = findViewById(R.id.llTitle);
        ivIdCardBack = findViewById(R.id.ivBack);
        ivBack = findViewById(R.id.img_btn_back);
        ivBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> {
            setProgressBar(true);
            requestAndRecognizeIdCard();
        });
        btnTakeAgain.setOnClickListener(v -> {
            if (FRONT.equals(mCurrentType)) {
                jumpToIdCardOcrActivity(IntentExtra.ID_CARD_FRONT);
            } else if (BACK.equals(mCurrentType)) {
                jumpToIdCardOcrActivity(IntentExtra.ID_CARD_BACK);
            }
        });
        updateTopTipsView();
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
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void updateTopTipsView() {
        mCurrentType = getIntent().getStringExtra(IntentExtra.SIDE_TYPE);
        authToken = getIntent().getStringExtra("authToken");

        if (BACK.equals(mCurrentType)) {
            ivIdCardBack.setImageResource(R.drawable.icon_upload_back_blue);
            tvBack.setTextColor(Color.parseColor("#3F75FC"));
        }

        mFilePath = ImageUtils.getTempFile(getApplicationContext()).getAbsolutePath();
        if (FRONT.equals(mCurrentType)) {
            mFilePath = ImageUtils.getFrontFile(getApplicationContext()).getAbsolutePath();
        } else if (BACK.equals(mCurrentType)) {
            mFilePath = ImageUtils.getBackFile(getApplicationContext()).getAbsolutePath();
        }
        LogTool.d(TAG, "mFilePath:" + mFilePath);
        ivIdCard.setImageBitmap(getBitmapFromFile(mFilePath));
    }

    public Bitmap getBitmapFromFile(String filePath) {
        try {
            return BitmapFactory.decodeStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void requestAndRecognizeIdCard() {
        File tempImage = new File(mFilePath);
        byte[] imageBytes = ImageUtils.fileToByteArray(tempImage);

        IdCardApi.getInstance().singleSide(authToken, mCurrentType, imageBytes, new IDetectedListener() {
            @Override
            public void onSuccess(String result) {
                LogTool.d(TAG, "json:" + result);
                setProgressBar(false);
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

    private void parseDataOnSuccess(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String code = jsonObject.optString("code");

            if ("000000".equals(code)) {
                String data = jsonObject.getString("data");
                JSONObject dataObject = new JSONObject(data);
                String msg = dataObject.optString("msg");
                if (isEmpty(msg) || "不完整".equals(msg)) {
                    if (FRONT.equals(mCurrentType)) {
                        String name = dataObject.optString("name");
                        String idCardNo = dataObject.optString("id_card_no");

                        SharedPref.put(getApplicationContext(), "name", name);
                        SharedPref.put(getApplicationContext(), "id_num", idCardNo);

                        moveOnRecognizeIdCardBackSide(name, idCardNo);

                    } else if (BACK.equals(mCurrentType)) {
                        String issuingDate = dataObject.optString("issuing_date");
                        String expireDate = dataObject.optString("expire_date");
                        String issuingAuthority = dataObject.optString("issuing_authority");
                        LogTool.d(TAG, "签发日期：" + issuingDate + "，过期日期：" + expireDate + "，签发机关：" + issuingAuthority);

                        RealPeopleVerifyActivity.actionStart(this, authToken);
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), msg + "，请重新拍摄", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } else {
                String message = jsonObject.optString("message");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "code:" + code + ",message:" + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isEmpty(String str) {
        return str == null || "".equals(str) || str.trim().length() == 0 || "null".equals(str);
    }

    private void moveOnRecognizeIdCardBackSide(String name, String idNum) {
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(idNum)) {
            jumpToIdCardOcrActivity(IntentExtra.ID_CARD_BACK);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "识别失败，请重新拍摄！", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void jumpToIdCardOcrActivity(String side) {
        Intent intent = new Intent(this, TakePhotoActivity.class);
        intent.putExtra("contentType", side);
        intent.putExtra("isSingle", true);
        intent.putExtra("authToken", authToken);
        startActivity(intent);
        finish();
    }

    public static void actionStart(Context context, String contentType, String authToken) {
        String side = FRONT;
        if (IntentExtra.ID_CARD_BACK.equals(contentType)) {
            side = BACK;
        }

        try {
            Intent intent = new Intent(context, UploadIdCardActivity.class);
            intent.putExtra(IntentExtra.SIDE_TYPE, side);
            intent.putExtra("authToken", authToken);
            context.startActivity(intent);
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}