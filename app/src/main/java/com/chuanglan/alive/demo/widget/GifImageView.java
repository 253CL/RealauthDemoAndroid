package com.chuanglan.alive.demo.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;


import com.chuanglan.alive.demo.R;

import java.util.Arrays;
import java.util.List;


public class GifImageView extends ImageView {

    static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    static final List<String> SUPPORTED_RESOURCE_TYPE_NAMES = Arrays.asList("raw", "drawable", "mipmap");
    private static final int DEFAULT_MOVIE_DURATION = 1000;
    private int mMovieResourceId;
    private Movie mMovie;
    private long mMovieStart;
    private int mCurrentAnimationTime = 0;
    private float mLeft;
    private float mTop;
    private float mScale;
    private int mMeasuredMovieWidth;
    private int mMeasuredMovieHeight;
    private boolean mVisible = true;
    private volatile boolean mPaused = false;
    private Path mClipPath;

    public GifImageView(Context context) {
        this(context, null);
    }

    public GifImageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.styleable.CustomTheme_gifViewStyle);
    }

    public GifImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setViewAttributes(context, attrs, defStyle);

        // 判断布局文件中是否设置了 src 属性，如果有就先默认设置一张静态图片，
        // 否则一进去没有设置 gif 图片是空白的，用户体验不好
        int resourceId = getResourceId(this, attrs);
        if (resourceId > 0) {
            super.setImageResource(resourceId);
        }
    }

    private static int getResourceId(ImageView view, AttributeSet attrs) {
        final int resId = attrs.getAttributeResourceValue(ANDROID_NS, "src", 0);
        if (resId > 0) {
            final String resourceTypeName = view.getResources().getResourceTypeName(resId);
            if (SUPPORTED_RESOURCE_TYPE_NAMES.contains(resourceTypeName)) {
                return resId;
            }
        }
        return 0;
    }

    private void setViewAttributes(Context context, AttributeSet attrs, int defStyle) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // 从描述文件中读出gif的值，创建出Movie实例
        final TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.GifImageView, defStyle, R.style.Widget_GifView);
        mMovieResourceId = array.getResourceId(R.styleable.GifImageView_gif, -1);
        mPaused = array.getBoolean(R.styleable.GifImageView_paused, false);
        array.recycle();
        if (mMovieResourceId != -1) {
            mMovie = Movie.decodeStream(getResources().openRawResource(mMovieResourceId));
        }
    }

    /**
     * 设置gif图资源
     *
     * @param resId 资源id
     */
    @Override
    public void setImageResource(int resId) {
        // 判断资源类型，如果不是 gif 则走父类的普通图片方法
        if (!setResource(this, resId)) {
            mMovie = null;
            super.setImageResource(resId);
        }
    }

    public void setOriginImageResource(int resId) {
        mMovie = null;
        super.setImageResource(resId);
    }

    private boolean setResource(ImageView view, int resId) {
        Resources res = view.getResources();
        if (res != null) {
            try {
                final String resourceTypeName = res.getResourceTypeName(resId);
                if (!SUPPORTED_RESOURCE_TYPE_NAMES.contains(resourceTypeName)) {
                    return false;
                }

                // 清除设置的 src 图片资源
                super.setImageResource(0);
                this.mMovieResourceId = resId;
                mMovie = Movie.decodeStream(getResources().openRawResource(
                        mMovieResourceId));
                requestLayout();
                return true;
            } catch (Resources.NotFoundException ignored) {
                //ignored
            }
        }
        return false;
    }

    public void setMovie(Movie movie) {
        this.mMovie = movie;
        requestLayout();
    }

    public Movie getMovie() {
        return mMovie;
    }

    public void setMovieTime(int time) {
        mCurrentAnimationTime = time;
        invalidate();
    }

    /**
     * 设置暂停
     *
     * @param paused
     */
    public void setPaused(boolean paused) {
        this.mPaused = paused;
        if (!paused) {
            mMovieStart = android.os.SystemClock.uptimeMillis()
                    - mCurrentAnimationTime;
        }
        invalidate();
    }

    /**
     * 判断gif图是否停止了
     */
    public boolean isPaused() {
        return this.mPaused;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMovie != null) {
            int movieWidth = mMovie.width();
            int movieHeight = mMovie.height();
            int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
            float scaleW = (float) movieWidth / (float) maximumWidth;
            mScale = 1f / scaleW;
            mMeasuredMovieWidth = maximumWidth;
            mMeasuredMovieHeight = (int) (movieHeight * mScale);
            setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);
        } else {
//            setMeasuredDimension(getSuggestedMinimumWidth(),
//                    getSuggestedMinimumHeight());
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;
        mTop = (getHeight() - mMeasuredMovieHeight) / 2f;
        mVisible = getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie != null) {
            canvas.save();
            int width = getWidth();
            int height = getHeight();

            if (mClipPath == null) {
                mClipPath = new Path();
            } else {
                mClipPath.reset();
            }
            mClipPath.addCircle((float) width / 2, (float) height / 2, (float) width / 2, Path.Direction.CW);
            canvas.clipPath(mClipPath);

            if (!mPaused) {
                updateAnimationTime();
                drawMovieFrame(canvas);
                invalidateView();
            } else {
                drawMovieFrame(canvas);
            }

            canvas.restore();
        } else {
            super.onDraw(canvas);
        }
    }

    private void invalidateView() {
        if (mVisible) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                postInvalidateOnAnimation();
            } else {
                invalidate();
            }
        }
    }

    private void updateAnimationTime() {
        long now = android.os.SystemClock.uptimeMillis();
        // 如果第一帧，记录起始时间
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        // 取出动画的时长
        int dur = mMovie.duration();
        if (dur == 0) {
            dur = DEFAULT_MOVIE_DURATION;
        }
        // 算出需要显示第几帧
        mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
    }

    private void drawMovieFrame(Canvas canvas) {
        // 设置要显示的帧，绘制即可
        mMovie.setTime(mCurrentAnimationTime);
        canvas.save();
        canvas.scale(mScale, mScale);
        mMovie.draw(canvas, mLeft / mScale, mTop / mScale);
        canvas.restore();
    }

    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        mVisible = screenState == SCREEN_STATE_ON;
        invalidateView();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }
}

