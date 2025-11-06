package com.chuanglan.alive.demo.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chuanglan.alive.demo.R;
import com.chuanglan.alive.demo.camera.CameraTimerTask;
import com.chuanglan.alive.demo.camera.ICameraControl;
import com.chuanglan.alive.demo.camera.PermissionCallback;
import com.chuanglan.alive.demo.utils.ImageUtils;
import com.chuanglan.alive.demo.utils.PhotoUtils;
import com.chuanglan.alive.demo.widget.CameraView;
import com.chuanglan.alive.demo.widget.CropView;
import com.chuanglan.alive.demo.widget.MaskView;
import com.chuanglan.alive.demo.widget.OCRCameraLayout;
import com.chuanglan.sdk.tools.LogTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * 身份证 OCR 拍照页面，拍照后返回照片
 */
public class TakePhotoActivity extends Activity {

    private static final String TAG = "TakePhotoActivity<TAG->";
    private static final String CONTENT_TYPE_GENERAL = "general";
    private static final String CONTENT_TYPE_BANK_CARD = "bankCard";
    private static final String CONTENT_TYPE_PASSPORT = "passport";

    public static final String KEY_CONTENT_TYPE = "contentType";
    public static final String ID_CARD_FRONT = "IDCardFront";
    public static final String ID_CARD_BACK = "IDCardBack";

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int PERMISSIONS_REQUEST_CAMERA = 800;
    private static final int PERMISSIONS_EXTERNAL_STORAGE = 801;

    private File outputFile;
    private String contentType;
    private String authToken;
    private boolean isSingle;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private OCRCameraLayout takePictureContainer;
    private OCRCameraLayout cropContainer;
    private LinearLayout llTitle;
    private ImageView lightButton;
    private ImageView ivBack;
    private CameraView cameraView;
    private CropView cropView;
    private MaskView cropMaskView;
    private ImageView takePhotoBtn;
    private TextView tvTopTips;
    private TextView tvTitle;
    private final PermissionCallback permissionCallback = () -> {
        ActivityCompat.requestPermissions(TakePhotoActivity.this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSIONS_REQUEST_CAMERA);
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_card_take_photo);
            takePictureContainer = findViewById(R.id.take_picture_container);
            cameraView = findViewById(R.id.camera_view);
            cameraView.getCameraControl().setPermissionCallback(permissionCallback);
            lightButton = findViewById(R.id.light_button);
            lightButton.setOnClickListener(lightButtonOnClickListener);
            takePhotoBtn = findViewById(R.id.take_photo_button);
            findViewById(R.id.album_button).setOnClickListener(albumButtonOnClickListener);
            takePhotoBtn.setOnClickListener(takeButtonOnClickListener);
            findViewById(R.id.ivRotateRight).setOnClickListener(rotateRightButtonOnClickListener);
            findViewById(R.id.ivRotateLeft).setOnClickListener(rotateLeftButtonOnClickListener);
            cropView = findViewById(R.id.crop_view);
            cropContainer = findViewById(R.id.crop_container);
            cropContainer.findViewById(R.id.tvConfirm).setOnClickListener(cropConfirmButtonListener);
            cropMaskView = cropContainer.findViewById(R.id.crop_mask_view);
            cropContainer.findViewById(R.id.tvCancel).setOnClickListener(cropCancelButtonListener);
            tvTitle = findViewById(R.id.tvTitle);
            llTitle = findViewById(R.id.llTitle);
            tvTopTips = findViewById(R.id.tvTopTips);
            ivBack = findViewById(R.id.img_btn_back);
            ivBack.setOnClickListener(v -> finish());
            setOrientation(getResources().getConfiguration());
            initParams();
            cameraView.setAutoPictureCallback(autoTakePictureCallback);
        } catch (Exception e) {
            finish();
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    private void initParams() {
        contentType = getIntent().getStringExtra(KEY_CONTENT_TYPE);
        isSingle = getIntent().getBooleanExtra("isSingle", false);
        authToken = getIntent().getStringExtra("authToken");

        if (contentType == null) {
            contentType = CONTENT_TYPE_GENERAL;
        }

        String outputPath = ImageUtils.getTempFile(getApplicationContext()).getAbsolutePath();
        if (ID_CARD_FRONT.equals(contentType)) {
            tvTitle.setText(getString(R.string.online_alive_detect_id_card_front_take_photo));
            tvTopTips.setText(getString(R.string.online_alive_detect_adjust_id_card_front));
            outputPath = ImageUtils.getFrontFile(getApplicationContext()).getAbsolutePath();
        } else if (ID_CARD_BACK.equals(contentType)) {
            tvTitle.setText(getString(R.string.online_alive_detect_id_card_back_take_photo));
            tvTopTips.setText(getString(R.string.online_alive_detect_adjust_id_card_back));
            outputPath = ImageUtils.getBackFile(getApplicationContext()).getAbsolutePath();
        }

        LogTool.d(TAG, "outputPath:" + outputPath);
        outputFile = new File(outputPath);

        int maskType;
        switch (contentType) {
            case ID_CARD_FRONT:
                maskType = MaskView.MASK_TYPE_ID_CARD_FRONT;
                break;
            case ID_CARD_BACK:
                maskType = MaskView.MASK_TYPE_ID_CARD_BACK;
                break;
            case CONTENT_TYPE_BANK_CARD:
                maskType = MaskView.MASK_TYPE_BANK_CARD;
                break;
            case CONTENT_TYPE_PASSPORT:
                maskType = MaskView.MASK_TYPE_PASSPORT;
                break;
            case CONTENT_TYPE_GENERAL:
            default:
                maskType = MaskView.MASK_TYPE_NONE;
                cropMaskView.setVisibility(View.INVISIBLE);
                break;
        }

        cameraView.setEnableScan(false);
        cameraView.setMaskType(maskType, this);
        cameraView.setPreviewMarginTop(40);
        cropMaskView.setMaskType(maskType);
    }

    private void showTakePicture() {
        cameraView.getCameraControl().resume();
        updateFlashMode();
        takePictureContainer.setVisibility(View.VISIBLE);
        cropContainer.setVisibility(View.INVISIBLE);
    }

    private void showCrop() {
        cameraView.getCameraControl().pause();
        updateFlashMode();
        takePictureContainer.setVisibility(View.INVISIBLE);
        cropContainer.setVisibility(View.VISIBLE);
    }

    private void updateFlashMode() {
        int flashMode = cameraView.getCameraControl().getFlashMode();
        if (flashMode == ICameraControl.FLASH_MODE_TORCH) {
            lightButton.setImageResource(R.drawable.ocr_light_on);
        } else {
            lightButton.setImageResource(R.drawable.ocr_light_off);
        }
    }

    private final View.OnClickListener albumButtonOnClickListener = v -> {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(TakePhotoActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_EXTERNAL_STORAGE);
                return;
            }
        }
        PhotoUtils.openAlbum(this, REQUEST_CODE_PICK_IMAGE);
    };

    private final View.OnClickListener lightButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (cameraView.getCameraControl().getFlashMode() == ICameraControl.FLASH_MODE_OFF) {
                cameraView.getCameraControl().setFlashMode(ICameraControl.FLASH_MODE_TORCH);
            } else {
                cameraView.getCameraControl().setFlashMode(ICameraControl.FLASH_MODE_OFF);
            }
            updateFlashMode();
        }
    };

    private final View.OnClickListener takeButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            cameraView.takePicture(outputFile, takePictureCallback);
        }
    };

    private final CameraView.OnTakePictureCallback autoTakePictureCallback = this::doConfirmResult;

    private final CameraView.OnTakePictureCallback takePictureCallback = this::doConfirmResult;

    private final View.OnClickListener rotateRightButtonOnClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cropView.rotate(90);
                }
            };

    private final View.OnClickListener rotateLeftButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cropView.rotate(-90);
        }
    };

    private final View.OnClickListener cropCancelButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 释放 cropView中的bitmap;
            cropView.setFilePath(null);
            showTakePicture();
        }
    };

    private final View.OnClickListener cropConfirmButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int maskType = cropMaskView.getMaskType();
            if (maskType == MaskView.MASK_TYPE_ID_CARD_FRONT
                    || maskType == MaskView.MASK_TYPE_ID_CARD_BACK) {

                Rect rect = cropMaskView.getFrameRect();
                Bitmap cropped = cropView.crop(rect);
                doConfirmResult(cropped);
            }
        }
    };

    private void doConfirmResult(final Bitmap bitmap) {
        mHandler.post(() -> {
            cameraView.getCameraControl().pause();
            updateFlashMode();
        });

        new Thread(() -> {
            String filePath = outputFile.getAbsolutePath();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (isSingle) {
                UploadIdCardActivity.actionStart(TakePhotoActivity.this, contentType, authToken);
            } else {
                // 将照片路径返回
                Intent intent = new Intent();
                intent.putExtra("filePath", filePath);
                setResult(RESULT_OK, intent);
                finish();
            }
        }).start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setOrientation(newConfig);
    }

    private void setOrientation(Configuration newConfig) {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int orientation;
        int cameraViewOrientation = CameraView.ORIENTATION_PORTRAIT;
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                cameraViewOrientation = CameraView.ORIENTATION_PORTRAIT;
                orientation = OCRCameraLayout.ORIENTATION_PORTRAIT;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                orientation = OCRCameraLayout.ORIENTATION_HORIZONTAL;
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                    cameraViewOrientation = CameraView.ORIENTATION_HORIZONTAL;
                } else {
                    cameraViewOrientation = CameraView.ORIENTATION_INVERT;
                }
                break;
            default:
                orientation = OCRCameraLayout.ORIENTATION_PORTRAIT;
                cameraView.setOrientation(CameraView.ORIENTATION_PORTRAIT);
                break;
        }
        takePictureContainer.setOrientation(orientation);
        cameraView.setOrientation(cameraViewOrientation);
        cropContainer.setOrientation(orientation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                String filePath = PhotoUtils.getPath(getApplicationContext(), uri);
                cropView.setFilePath(filePath);
                showCrop();
            } else {
                cameraView.getCameraControl().resume();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraView.getCameraControl().refreshPermission();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.online_alive_detect_camera_permission), Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSIONS_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PhotoUtils.openAlbum(this, REQUEST_CODE_PICK_IMAGE);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.online_alive_detect_storage_permission), Toast.LENGTH_SHORT).show();
                }
            default:
                break;
        }
    }

    /**
     * 做一些收尾工作
     */
    private void doClear() {
        CameraTimerTask.cancelAutoFocusTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.doClear();
    }
}
