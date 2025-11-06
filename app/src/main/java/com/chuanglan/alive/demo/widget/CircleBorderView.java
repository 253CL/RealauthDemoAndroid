package com.chuanglan.alive.demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.chuanglan.sdk.async.MainHandler;
import com.chuanglan.sdk.utils.ScreenUtils;

public class CircleBorderView extends View {
    private int color = Color.parseColor("#B6D0FF");
    private int bgColor = Color.parseColor("#D6D6D6");
    private int[] recodingColor = new int[]{Color.parseColor("#2D77FF"),
            Color.parseColor("#7F2DFF"), Color.parseColor("#7F2DFF")};
    private Paint paint;
    private Paint arcPaint;
    private Paint bgPaint;
    private int centerX;
    private int centerY;
    private float radius;
    private long recodingTimeMillis = 0;
    private int sweepAngle = 0;
    private boolean startRecoding;
    private Runnable updateProgressRunnable;
    private RectF oval;

    public CircleBorderView(Context context) {
        super(context);
        init();
    }

    public CircleBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleBorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(ScreenUtils.dp2px(getContext(), 4));
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE); // 设置空心
        paint.setColor(color);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setAntiAlias(true);
        bgPaint.setStrokeWidth(ScreenUtils.dp2px(getContext(), 4));
        bgPaint.setDither(true);
        bgPaint.setStyle(Paint.Style.STROKE); // 设置空心
        bgPaint.setColor(bgColor);

        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setAntiAlias(true);
        arcPaint.setStrokeWidth(ScreenUtils.dp2px(getContext(), 6));
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setStrokeJoin(Paint.Join.ROUND);
        arcPaint.setDither(true);
        arcPaint.setStyle(Paint.Style.STROKE); // 设置空心

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        radius = (getMeasuredWidth() - paint.getStrokeWidth() * 2) / 2; //圆半径
        centerX = getMeasuredWidth() / 2;    //圆心
        centerY = getMeasuredHeight() / 2;
        if (null == oval) {
            SweepGradient mSweepGradient = new SweepGradient(centerX, centerY, recodingColor, null);
            Matrix matrix = new Matrix();
            matrix.setRotate(88f, centerX, centerY);
            mSweepGradient.setLocalMatrix(matrix);
            arcPaint.setShader(mSweepGradient);
            oval = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        }
    }

    public void setRecodingState(long recodingTimeMillis, boolean startRecoding) {
        this.recodingTimeMillis = recodingTimeMillis;
        if (startRecoding) {
            startRecodingAnim();
        } else {
            if (null != updateProgressRunnable) {
                MainHandler.removeMessage(updateProgressRunnable);
            }
            updateProgressRunnable = null;
        }
        this.startRecoding = startRecoding;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (startRecoding) {
            canvas.drawCircle(centerX, centerY, radius, bgPaint);// 画圆
            canvas.drawArc(oval, 90, sweepAngle, false, arcPaint);
        } else {
            canvas.drawCircle(centerX, centerY, radius, paint);// 画圆
        }
    }

    private void startRecodingAnim() {
        if (null == updateProgressRunnable) {
            sweepAngle = 0;
            long firstTime = recodingTimeMillis / 4 * 3;
            long secondTime = recodingTimeMillis - firstTime;
            updateProgressRunnable = new Runnable() {
                @Override
                public void run() {
                    if (sweepAngle < 270) {
                        sweepAngle += 2;
                        postInvalidate();
                        MainHandler.postDelayed(updateProgressRunnable, firstTime / (270 / 2));
                    } else if (sweepAngle < 360) {
                        sweepAngle += 5;
                        postInvalidate();
                        MainHandler.postDelayed(updateProgressRunnable, secondTime / (90 / 5));
                    }
                }
            };
        }
        MainHandler.postDelayed(updateProgressRunnable, 30);
    }

}