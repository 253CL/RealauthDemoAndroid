package com.chuanglan.alive.demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chuanglan.alive.demo.R;
import com.chuanglan.alive.demo.utils.CameraUtils;
import com.chuanglan.alive.demo.utils.MediaPlayerUtils;
import com.chuanglan.alive.demo.utils.StatusBarUtils;
import com.chuanglan.alive.demo.utils.WindowUtils;
import com.chuanglan.alive.demo.widget.CircleBorderView;
import com.chuanglan.alive.demo.widget.CircleScanView;
import com.chuanglan.alive.demo.widget.CountTimeProgressView;
import com.chuanglan.alive.demo.widget.GifImageView;
import com.chuanglan.alive.demo.widget.MyAlertDialog;
import com.chuanglan.alivedetected.api.OnlineAliveDetectorApi;
import com.chuanglan.alivedetected.entity.OnlineAliveBean;
import com.chuanglan.alivedetected.entity.SdkConfiguration;
import com.chuanglan.alivedetected.interfaces.IAliveDetectedListener;
import com.chuanglan.sdk.tools.LogTool;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OnlineAliveDetectedActivity extends Activity {

    private static final String TAG = "OnlineAliveDetected<TAG->";
    private static final String ACTION_BLINK_EYES = "0";
    private static final String ACTION_TURN_HEAD_TO_UP = "4";
    private static final String ACTION_TURN_HEAD_TO_DOWN = "5";
    private static final String ACTION_OPEN_MOUTH = "6";
    private static final String ACTION_SHAKE_HEAD = "7";

    private boolean isOpenSound = true;
    private boolean isVisible;

    private int mActionSecond = 2;
    private int mTimeout = 30;

    private TextView tvStep1;
    private TextView tvStep2;
    private TextView tvStep3;
    private TextView tvTitle;
    private TextView tvDetectTips;
    private ImageView ivFaceOutLine;
    private ImageView ivSound;
    private ImageView ivBack;
    private MyAlertDialog alertDialog;
    private SurfaceView ttvPreview;
    private CircleBorderView circleRingBgView;
    private CircleScanView circleScanView;
    private GifImageView gifImageView;
    private CountTimeProgressView pvCountTime;
    private List<OnlineAliveBean> mActions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            StatusBarUtils.tint(this, true);
            setContentView(R.layout.activity_online_alive_detected);
            initView();
            initListener();
            initData();
            WindowUtils.setWindowBrightness(this, true);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void initView() {
        pvCountTime = findViewById(R.id.pvCountTime);
        ivSound = findViewById(R.id.ivSound);
        tvTitle = findViewById(R.id.tvTitle);
        tvDetectTips = findViewById(R.id.tvDetectTips);
        ttvPreview = findViewById(R.id.ttvPreview);
        circleRingBgView = findViewById(R.id.roundView);
        ivFaceOutLine = findViewById(R.id.ivFaceOutLine);
        circleScanView = findViewById(R.id.csView);
        tvStep1 = findViewById(R.id.tvStep1);
        tvStep2 = findViewById(R.id.tvStep2);
        tvStep3 = findViewById(R.id.tvStep3);
        gifImageView = findViewById(R.id.gifImageView);
        ivBack = findViewById(R.id.img_btn_back);
        tvTitle.setText("活体检测");
    }

    private void initListener() {
        ivBack.setOnClickListener(v -> finish());
        ivSound.setOnClickListener(v -> {
            isOpenSound = !isOpenSound;
            if (isOpenSound) {
                ivSound.setImageResource(R.drawable.cl_online_alive_blue);
            } else {
                ivSound.setImageResource(R.drawable.cl_online_alive_gray);
            }
        });

        // 设置倒计时 View 的监听
        pvCountTime.addOnEndListener(new CountTimeProgressView.OnEndListener() {
            @Override
            public void onAnimationEnd() {
                LogTool.e(TAG, "onAnimationEnd()");
            }

            @Override
            public void onClick(long overageTime) {

            }
        });
    }

    private void initData() {
        String authToken = getIntent().getStringExtra("authToken");

        SdkConfiguration configuration = new SdkConfiguration.Builder()
                // 建议从服务端调用获取 authToken 接口获取
                .setAuthToken(authToken)
                // 设置动作数量 1-3 分别代表 1-3 个动作，4 代表 1-3 个动作随机，不设置默认是 4
                .setActionSize(1)
                // 每个动作的执行时间，单位为秒
                .setActionSecond(mActionSecond)
                // 安全级别 0：低 1：高 (不设置默认为0) 低级别随机动作，高级别将包含局部和全脸动作
                .setSecurityLevel(0)
                // 设置当前 Activity 对象
                .setActivity(this)
                // 人脸检测超时时间，不大于 120s，且不小于 10s，不设置默认为 30s
                .setTimeout(mTimeout)
                // 设置相机配置，比如相机预览的大小等等
                .setICameraConfig(this::getCameraParameters)
                .build();

        OnlineAliveDetectorApi.getInstance().start(configuration, new IAliveDetectedListener() {
            /**
             * 当准备好的时候
             */
            @Override
            public void onReady() {
                // 开始倒计时
                runOnUiThread(() -> {
                    startCountDownAnimation();
                    circleRingBgView.setRecodingState(0, false);
                });
            }

            /**
             * 当收到下发的动作序列时
             * @param actions 动作序列列表
             */
            @Override
            public void onReceivedActions(List<OnlineAliveBean> actions) {
                mActions = actions;
                // 根据动作序列数量显示底部的指示器
                runOnUiThread(() -> showIndicator());
            }

            /**
             * 当提示动作改变时的回调，比如提示做下一个动作时
             * @param index 动作下标，从 0 开始
             */
            @Override
            public void onActionChanged(final int index) {
                runOnUiThread(() -> {
                    // 当开始提示第一个动作时，显示动画进度条
                    if (index == 0) {
                        // 每个动作时长 * 动作数 * 1000 计算出总的毫秒数
                        long duration = mActionSecond * mActions.size() * 1000L;
                        // 显示进度条动画
                        circleRingBgView.setRecodingState(duration, true);
                    }
                    updateIndicatorGifTipSound(index);
                });
            }

            /**
             * 当提示状态变化时的回调
             * @param tip 提示信息
             */
            @Override
            public void onStateTipChanged(String tip) {
                runOnUiThread(() -> tvDetectTips.setText(tip));
            }

            /**
             * 当脸部状态变化时
             * @param isFullFace true：预览框检测到全脸，false 不是全脸
             */
            @Override
            public void onFaceStateChanged(boolean isFullFace) {
                runOnUiThread(() ->
                        ivFaceOutLine.setImageResource(isFullFace
                                ? R.drawable.face_border_white
                                : R.drawable.face_border_red)
                );
            }

            /**
             * 人脸已经就位，准备提示动作指令
             */
            @Override
            public void onFaceReady() {
                // 人脸到位取消倒计时显示，此处逻辑可根据自己的业务逻辑进行修改
                runOnUiThread(() -> {
                    if (null != pvCountTime && pvCountTime.isRunning()) {
                        pvCountTime.cancelCountTimeAnimation();
                        pvCountTime.setVisibility(View.INVISIBLE);
                    }
                });
            }

            /**
             * 服务端开始检测的回调，这个时候可以显示一些动画或者 Loading
             */
            @Override
            public void onStartDetect() {
                runOnUiThread(() -> startDetectAnimation());
            }

            /**
             * 服务端检测完成的回调，这个时候可以停止显示动画或者 Loading
             */
            @Override
            public void onDetectComplete() {
                runOnUiThread(() -> stopDetectAnimation());
            }

            /**
             * 活体检测通过
             */
            @Override
            public void onPassed(String data) {
                LogTool.d(TAG, "onPassed() -> data: " + data);
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    String actionVerify = jsonObject.optString("action_verify");
                    double score = jsonObject.optDouble("score");

                    if ("pass".equals(actionVerify)) {
                        Intent intent = new Intent(OnlineAliveDetectedActivity.this, OnlineAliveDetectedSuccessActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        runOnUiThread(() -> videoDetectFail("认证失败", "动作验证未通过，请按提示完成动作"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errCode, String errMsg) {
                runOnUiThread(() -> videoDetectFail("认证失败", errMsg));
            }

            @Override
            public void onOverTime() {
                LogTool.e(TAG, "onOverTime()");
                runOnUiThread(() -> {
                    // 30s 超时处理
                    if (isVisible) {
                        showAlertDialog(getString(R.string.cl_online_alive_detect_preview_timeout), "");
                    }
                });
            }
        });
    }

    /**
     * 设置相机预览的参数，需要获取当前手机相机支持的预览的尺寸，然后根据自己的布局大小去遍历找到适合自己当前相机预览的大小，
     * 然后设置给相机，如果设置的是相机不支持的预览的尺寸，会导致设置相机参数失败！！！
     *
     * @param camera
     * @return
     */
    private Camera.Size getCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();

        // 此处需要注意，布局中 surfaceView 的宽高要一定有值，不要使用 wrap_content，否则获取的值可能为 0
        // 另外，设置的宽高显示出来不一定是设置的宽高，如果不是，可能是因为遍历出来相机的预览尺寸不支持
        int minWidth = ttvPreview.getWidth();
        int minHeight = ttvPreview.getHeight();
        Camera.Size previewSize = CameraUtils.getOptimalSize(mSupportedPreviewSizes, minWidth, minHeight);

        // likewise for the camera object itself.
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        LogTool.d(TAG, "previewSize.width: " + previewSize.width + " height: " + previewSize.height);
        camera.setParameters(parameters);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) ttvPreview.getLayoutParams();
        float ratio = previewSize.height * 1.0f / previewSize.width;
        if (previewSize.width < previewSize.height) {
            ratio = previewSize.width * 1.0f / previewSize.height;
        }

        if (ratio > 1) {
            lp.height = minHeight;
            lp.width = (int) (lp.height * ratio);
        } else {
            lp.width = minWidth;
            lp.height = (int) (lp.width / ratio);
        }

        // 注意因为相机支持的预览尺寸不同，有可能设置的布局与预览出来的位置会不一样
        ttvPreview.setLayoutParams(lp);

        return previewSize;
    }

    private void startCountDownAnimation() {
        pvCountTime.setStartAngle(0);
        // 设置倒计时时间 30 秒
        pvCountTime.setCountTime(1000 * mTimeout);
        pvCountTime.startCountTimeAnimation();
    }

    private void showIndicator() {
        // 最多只有 3 个动作
        int actionSize = mActions != null ? mActions.size() : 0;
        switch (actionSize) {
            case 1:
                tvStep1.setVisibility(View.VISIBLE);
                break;
            case 2:
                tvStep1.setVisibility(View.VISIBLE);
                tvStep2.setVisibility(View.VISIBLE);
                break;
            case 3:
                tvStep1.setVisibility(View.VISIBLE);
                tvStep2.setVisibility(View.VISIBLE);
                tvStep3.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    /**
     * 更新指示器、Gif、动作提示语，动作提示音
     *
     * @param currentActionIndex 当前动作下标
     */
    private void updateIndicatorGifTipSound(int currentActionIndex) {
        updateIndicator(currentActionIndex);
        updateGif(currentActionIndex);
        updateActionTip(currentActionIndex);
        if (isOpenSound) {
            playSounds(currentActionIndex);
        }
    }

    private void updateActionTip(int currentActionIndex) {
        if (mActions == null || mActions.isEmpty() || currentActionIndex >= mActions.size()) {
            return;
        }
        tvDetectTips.setText(mActions.get(currentActionIndex).getMessage());
    }

    private void updateIndicator(int currentActionIndex) {
        switch (currentActionIndex) {
            case 1:
                tvStep1.setText("1");
                tvStep2.setText("2");
                setTextViewFocus(tvStep2, true);
                break;
            case 2:
                tvStep1.setText("1");
                tvStep2.setText("2");
                setTextViewFocus(tvStep2, true);
                tvStep3.setText("3");
                setTextViewFocus(tvStep3, true);
                break;
            default:
                break;
        }
    }

    private void resetIndicator() {
        tvStep1.setText("1");
        tvStep2.setText("");
        tvStep3.setText("");

        setTextViewFocus(tvStep1, true);
        setTextViewFocus(tvStep2, false);
        setTextViewFocus(tvStep3, false);

        tvStep1.setVisibility(View.GONE);
        tvStep2.setVisibility(View.GONE);
        tvStep3.setVisibility(View.GONE);

        gifImageView.setOriginImageResource(R.drawable.cl_online_alive_default);
        gifImageView.setVisibility(View.INVISIBLE);
    }

    private void updateGif(int currentActionIndex) {
        if (mActions == null || mActions.isEmpty() || currentActionIndex >= mActions.size()) {
            return;
        }

        gifImageView.setVisibility(View.VISIBLE);

        switch (mActions.get(currentActionIndex).getCode()) {
            case ACTION_BLINK_EYES:
                gifImageView.setImageResource(R.drawable.online_blink_eyes);
                break;
            case ACTION_TURN_HEAD_TO_UP:
                gifImageView.setImageResource(R.drawable.online_turn_head_to_up);
                break;
            case ACTION_TURN_HEAD_TO_DOWN:
                gifImageView.setImageResource(R.drawable.online_turn_head_to_down);
                break;
            case ACTION_OPEN_MOUTH:
                gifImageView.setImageResource(R.drawable.online_open_mouth);
                break;
            case ACTION_SHAKE_HEAD:
                gifImageView.setImageResource(R.drawable.online_shake_head);
                break;
            default:
                break;
        }
    }

    private void playSounds(int currentActionIndex) {
        if (mActions == null || mActions.isEmpty() || currentActionIndex >= mActions.size()) {
            return;
        }

        switch (mActions.get(currentActionIndex).getCode()) {
            case ACTION_BLINK_EYES:
                MediaPlayerUtils.getInstance().play(this, "online_blink_eyes.mp3");
                break;
            case ACTION_TURN_HEAD_TO_UP:
                MediaPlayerUtils.getInstance().play(this, "online_turn_head_to_up.mp3");
                break;
            case ACTION_TURN_HEAD_TO_DOWN:
                MediaPlayerUtils.getInstance().play(this, "online_turn_head_to_down.mp3");
                break;
            case ACTION_OPEN_MOUTH:
                MediaPlayerUtils.getInstance().play(this, "online_open_mouth.mp3");
                break;
            case ACTION_SHAKE_HEAD:
                MediaPlayerUtils.getInstance().play(this, "online_shake_head.mp3");
                break;
            default:
                break;
        }
    }

    private void setTextViewFocus(TextView tv, boolean focusable) {
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (focusable) {
                drawable = getResources().getDrawable(R.drawable.cl_circle_tv_focus, null);
            } else {
                drawable = getResources().getDrawable(R.drawable.cl_circle_tv_un_focus, null);
            }
        } else {
            if (focusable) {
                drawable = getResources().getDrawable(R.drawable.cl_circle_tv_focus);
            } else {
                drawable = getResources().getDrawable(R.drawable.cl_circle_tv_un_focus);
            }
        }
        tv.setBackground(drawable);
    }

    private void startDetectAnimation() {
        resetIndicator();
        mActions.clear();
        circleScanView.startDetectAnim();
    }

    private void stopDetectAnimation() {
        circleScanView.stopDetectAnim();
    }

    private void videoDetectFail(String title, String msg) {
        showAlertDialog(title, msg);
        if (pvCountTime.isRunning()) {
            pvCountTime.cancelCountTimeAnimation();
        }
    }

    private void showAlertDialog(String title, String msg) {
        if (null != alertDialog && alertDialog.isShowing() || isFinishing()) {
            return;
        }
        alertDialog = new MyAlertDialog(this, title, msg);
        alertDialog.show();
        alertDialog.setCancelClickListener(v -> {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            finish();
        });
        // 重新开始录制
        alertDialog.setConfirmClickListener(v -> {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            resetViewState();
            OnlineAliveDetectorApi.getInstance().tryAgain();
        });
    }

    private void resetViewState() {
        if (null != alertDialog && alertDialog.isShowing()) {
            return;
        }
        pvCountTime.setVisibility(View.VISIBLE);
        resetIndicator();
        mActions.clear();
        circleRingBgView.setRecodingState(0, false);
        startCountDownAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
        resetViewState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pvCountTime != null) {
            pvCountTime.cancelCountTimeAnimation();
        }
    }

    public static void start(Context context) {
        try {
            Intent starter = new Intent(context, OnlineAliveDetectedActivity.class);
            starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(starter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
