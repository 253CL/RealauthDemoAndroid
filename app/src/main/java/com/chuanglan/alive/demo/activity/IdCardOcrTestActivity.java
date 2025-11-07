package com.chuanglan.alive.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chuanglan.alive.demo.R;
import com.chuanglan.alive.demo.utils.FileUtils;
import com.chuanglan.alivedetected.api.IdCardApi;
import com.chuanglan.alivedetected.interfaces.IDetectedListener;
import com.chuanglan.sdk.tools.LogTool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class IdCardOcrTestActivity extends Activity {

    private static final String TAG = "IdCardOcrTestActivity<TAG->";
    private static final int REQUEST_CODE_FRONT = 1;
    private static final int REQUEST_CODE_BACK = 2;
    /**
     * default 200kb
     */
    private static final int IMAGE_MAX_BYTE = 200 * 1024;
    private ProgressBar mProgressBar;
    private ImageView ivFront;
    private ImageView ivBack;
    private Bitmap frontBitmap;
    private Bitmap backBitmap;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_card_ocr_test);
        findViewById(R.id.img_btn_back).setOnClickListener(view -> finish());
        ivFront = findViewById(R.id.ivFront);
        ivBack = findViewById(R.id.ivBack);
        mProgressBar = findViewById(R.id.progressBar);
        Button btnRecognize = findViewById(R.id.btnRecognize);
        ivFront.setOnClickListener(v -> startTakePicture(REQUEST_CODE_FRONT, "IDCardFront"));
        ivBack.setOnClickListener(v -> startTakePicture(REQUEST_CODE_BACK, "IDCardBack"));
        btnRecognize.setOnClickListener(v -> recognizeIdCard());
        authToken = getIntent().getStringExtra("authToken");
        findViewById(R.id.tvSingleDetect).setOnClickListener(v ->
                startTakePicture()
        );
    }

    private void startTakePicture() {
        Intent intent = new Intent(this, TakePhotoActivity.class);
        intent.putExtra("contentType", "IDCardFront");
        intent.putExtra("authToken", authToken);
        intent.putExtra("isSingle", true);
        startActivity(intent);
        finish();
    }

    private void startTakePicture(int requestCode, String contentType) {
        Intent intent = new Intent(this, TakePhotoActivity.class);
        intent.putExtra("contentType", contentType);
        startActivityForResult(intent, requestCode);
    }

    private void recognizeIdCard() {
        if (frontBitmap == null || backBitmap == null) {
            Toast.makeText(getApplicationContext(), "请先拍摄身份证！", Toast.LENGTH_SHORT).show();
            return;
        }

        if (frontBitmap.isRecycled() || backBitmap.isRecycled()) {
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        // 传递身份证正反面照片的 base64 编码
        byte[] frontBytes = FileUtils.bitmapToByteArray(frontBitmap);
        byte[] backBytes = FileUtils.bitmapToByteArray(backBitmap);

        IdCardApi.getInstance().doubleSide(authToken, frontBytes, backBytes, new IDetectedListener() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    mProgressBar.setVisibility(View.GONE);
                    runOnUiThread(() -> mProgressBar.setVisibility(View.GONE));
                    Intent intent = new Intent(IdCardOcrTestActivity.this, IdCardOcrResultActivity.class);
                    intent.putExtra("result", result);
                    intent.putExtra("authToken", authToken);
                    startActivity(intent);
                    finish();
                });
                LogTool.d(TAG, "onSuccess() -> result:" + result);
            }

            @Override
            public void onFailed(int errorCode, String errorMsg) {
                LogTool.e(TAG, "onFailed() -> errorCode:" + errorCode + ",errorMsg:" + errorMsg);
                runOnUiThread(() -> {
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        String filePath = data.getStringExtra("filePath");
        if (requestCode == REQUEST_CODE_FRONT) {
            frontBitmap = getBitmapFromFile(filePath);
            ivFront.setImageBitmap(frontBitmap);
        }
        if (requestCode == REQUEST_CODE_BACK) {
            backBitmap = getBitmapFromFile(filePath);
            ivBack.setImageBitmap(backBitmap);
        }
    }

    public Bitmap getBitmapFromFile(String filePath) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap != null ? compressImage(bitmap) : null;
    }

    /**
     * 质量压缩方法
     *
     * @param image image of bitmap
     * @return bitmap object
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里 100 表示不压缩，把压缩后的数据存放到 baos 中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        // 循环判断果如压缩后图片是否大于 200kb,大于继续压缩
        while (baos.toByteArray().length > IMAGE_MAX_BYTE) {
            // 重置 baos 即清空 baos
            baos.reset();
            // 第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            // 这里压缩 options%，把压缩后的数据存放到 baos 中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            // 每次都减少 10
            options -= 10;
            if (options <= 0) {
                break;
            }
        }
        // 把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        // 把ByteArrayInputStream数据生成图片
        return BitmapFactory.decodeStream(isBm, null, null);
    }
}