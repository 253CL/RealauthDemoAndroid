package com.chuanglan.alive.demo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.chuanglan.alive.demo.R;
import com.chuanglan.sdk.async.MainHandler;

import java.lang.ref.WeakReference;


public class CircleScanView extends View {

    private Paint paint;
    private float radius;
    private int centerX;
    private int centerY;
    private Bitmap detectBitmap;
    private Runnable detectRunnable;
    private boolean startDetectAnim;
    private int detectAnimTop;

    public CircleScanView(Context context) {
        super(context);
        init();
    }

    public CircleScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setDither(true);

        detectBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cl_alive_detect_anim);
        detectAnimTop = -detectBitmap.getHeight();
    }

    public void startDetectAnim() {
        if (!startDetectAnim) {
            changeDetectAnimState(true);
        }
    }

    public void stopDetectAnim() {
        if (startDetectAnim) {
            changeDetectAnimState(false);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        radius = (getMeasuredWidth() - paint.getStrokeWidth() * 2) / 2; //圆半径
        centerX = getMeasuredWidth() / 2;    //圆心
        centerY = getMeasuredHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        //设置裁剪的圆心，半径
        path.addCircle(centerX, centerY, radius, Path.Direction.CCW);
        //裁剪画布，并设置其填充方式
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.clipPath(path);

        if (startDetectAnim) {
            canvas.drawBitmap(detectBitmap, 0, detectAnimTop, paint);
        }
    }

    private void changeDetectAnimState(boolean start) {
        this.startDetectAnim = start;
        if (startDetectAnim) {
            setVisibility(VISIBLE);
            detectAnimTop = -detectBitmap.getHeight();
            if (null == detectRunnable) {
                detectRunnable = new DetectRunnable(this);
//                detectRunnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        detectAnimTop += 8;
//                        if (detectAnimTop >= radius * 2) {
//                            detectAnimTop = -detectBitmap.getHeight();
//                        }
//                        postInvalidate();
//                        MainHandler.postDelayed(detectRunnable, 20);
//                    }
//                };
            }
            MainHandler.post(detectRunnable);
        } else {
            setVisibility(INVISIBLE);
            MainHandler.removeMessage(detectRunnable);
            MainHandler.removeAll();
        }
    }

    class DetectRunnable implements Runnable {

        private WeakReference<CircleScanView> weakReference;

        public DetectRunnable(CircleScanView circleScanView) {
            weakReference = new WeakReference<>(circleScanView);
        }

        @Override
        public void run() {
            detectAnimTop += 8;
            if (detectAnimTop >= radius * 2) {
                detectAnimTop = -detectBitmap.getHeight();
            }
            postInvalidate();
            MainHandler.postDelayed(detectRunnable, 20);
        }
    }

}
