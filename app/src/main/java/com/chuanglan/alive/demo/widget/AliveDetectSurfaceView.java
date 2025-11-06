package com.chuanglan.alive.demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.chuanglan.alivedetected.api.OnlineAliveDetectorApi;

/**
 * 圆形SurfaceView
 */
public class AliveDetectSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private int height;
    private int width;

    public AliveDetectSurfaceView(Context context) {
        super(context);
        initView();
    }

    public AliveDetectSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AliveDetectSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    int size = Math.min(view.getMeasuredHeight(), view.getMeasuredWidth());
                    int left = (view.getMeasuredWidth() - size) / 2;
                    int top = 0;
                    Rect rect = new Rect(left, top, size + left, size + top);
                    outline.setOval(rect);
                }
            });
            setClipToOutline(true);
        }
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        OnlineAliveDetectorApi.getInstance().bindSurfaceHolder(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        height = getMeasuredHeight();
        width = getMeasuredWidth();
    }

    @Override
    public void draw(Canvas canvas) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Path path = new Path();
            int size = Math.min(height, width);
            int left = (width - size) / 2;
            int top = 0;
            //设置裁剪的圆心，半径
            int radius = size / 2;
            path.addCircle(radius + left , radius + top, radius, Path.Direction.CCW);
            //裁剪画布，并设置其填充方式
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            canvas.clipPath(path);
        }
        super.draw(canvas);
    }
}
