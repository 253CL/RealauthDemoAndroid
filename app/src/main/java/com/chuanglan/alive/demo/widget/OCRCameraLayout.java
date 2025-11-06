package com.chuanglan.alive.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.chuanglan.alive.demo.R;
import com.chuanglan.alive.demo.utils.ScreenUtils;


public class OCRCameraLayout extends FrameLayout {

    private static final String TAG = "OCRCameraLayout<TAG->";
    public static int ORIENTATION_PORTRAIT = 0;
    public static int ORIENTATION_HORIZONTAL = 1;

    private int orientation = ORIENTATION_PORTRAIT;
    private View contentView;
    private View topTipsView;
    private View leftDownView;
    private View rightUpView;
    private View bottomButtonView;
    private View cropButtonView;

    private int contentViewId;
    private int topTipsViewId;
    private int leftDownViewId;
    private int rightUpViewId;
    private int bottomButtonViewId;
    private int cropButtonViewId;
    /**
     * OCR 扫描页面底部三个按钮的背景框
     */
    private final Rect backgroundRect = new Rect();
    private final Paint mPaint = new Paint();

    public void setOrientation(int orientation) {
        if (this.orientation == orientation) {
            return;
        }
        this.orientation = orientation;
        requestLayout();
    }

    public OCRCameraLayout(Context context) {
        super(context);
    }

    public OCRCameraLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(attrs);
    }

    public OCRCameraLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttrs(attrs);
    }

    {
        setWillNotDraw(false);
    }

    private void parseAttrs(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs,
                R.styleable.OCRCameraLayout, 0, 0);
        int paintColor;
        try {
            contentViewId = a.getResourceId(R.styleable.OCRCameraLayout_contentView, -1);
            topTipsViewId = a.getResourceId(R.styleable.OCRCameraLayout_topTipsView, -1);
            leftDownViewId = a.getResourceId(R.styleable.OCRCameraLayout_leftDownView, -1);
            rightUpViewId = a.getResourceId(R.styleable.OCRCameraLayout_rightUpView, -1);
            bottomButtonViewId = a.getResourceId(R.styleable.OCRCameraLayout_bottomButtonView, -1);
            cropButtonViewId = a.getResourceId(R.styleable.OCRCameraLayout_cropButtonView, -1);
            paintColor = a.getColor(R.styleable.OCRCameraLayout_paintColor, Color.WHITE);
        } finally {
            a.recycle();
        }

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(paintColor);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (contentViewId != -1) {
            contentView = findViewById(contentViewId);
        }
        if (topTipsViewId != -1) {
            topTipsView = findViewById(topTipsViewId);
        }
        if (leftDownViewId != -1) {
            leftDownView = findViewById(leftDownViewId);
        }
        if (rightUpViewId != -1) {
            rightUpView = findViewById(rightUpViewId);
        }
        if (bottomButtonViewId != -1) {
            bottomButtonView = findViewById(bottomButtonViewId);
        }
        if (cropButtonViewId != -1) {
            cropButtonView = findViewById(cropButtonViewId);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getWidth();
        int height = getHeight();
        int left;
        int top;

        if (r < b) {
            int contentHeight = width * 4 / 3;
            int heightLeft = height - contentHeight;
            // old
            //contentView.layout(l, t, r, contentHeight);

            if (contentView != null) {
                // top 固定写死为 0 是为了不让 contentView 下移，否则会有白边
                contentView.layout(l, 0, r, contentHeight);
            }

            backgroundRect.left = 0;
            backgroundRect.top = contentHeight;
            backgroundRect.right = width;
            backgroundRect.bottom = height;

            // layout topTipsView
            if (topTipsView != null) {
                int measuredWidth = topTipsView.getMeasuredWidth();
                int measuredHeight = topTipsView.getMeasuredHeight();
                left = (width - measuredWidth) / 2;
                top = ScreenUtils.dpToPx(90);
                int right = left + measuredWidth;
                int bottom = top + measuredHeight;
                topTipsView.layout(left, top, right, bottom);
            }

            // layout leftDownView
            if (leftDownView != null) {
                MarginLayoutParams leftDownViewLayoutParams = (MarginLayoutParams) leftDownView.getLayoutParams();
                left = leftDownViewLayoutParams.leftMargin;
                //top = contentHeight + (heightLeft - leftDownView.getMeasuredHeight()) / 2;
                top = contentHeight + ScreenUtils.dpToPx(32);
                leftDownView.layout(left, top, left + leftDownView.getMeasuredWidth(), top + leftDownView.getMeasuredHeight());
            }

            // layout rightUpView
            if (rightUpView != null) {
                MarginLayoutParams rightUpViewLayoutParams = (MarginLayoutParams) rightUpView.getLayoutParams();
                left = width - rightUpView.getMeasuredWidth() - rightUpViewLayoutParams.rightMargin;
                //top = contentHeight + (heightLeft - rightUpView.getMeasuredHeight()) / 2;
                top = contentHeight + ScreenUtils.dpToPx(32);
                rightUpView.layout(left, top, left + rightUpView.getMeasuredWidth(), top + rightUpView.getMeasuredHeight());
            }

            // layout bottomButtonView
            if (bottomButtonView != null) {
                // 先暂时将布局上移 280px
                int addTop = 280;
                // 如果是 1280* 720 的屏幕再减去 80
                if (ScreenUtils.is1280x720(getContext())) {
                    addTop -= 80;
                }
                int measuredHeight = bottomButtonView.getMeasuredHeight();
                top = contentHeight + (heightLeft - measuredHeight) / 2;
                bottomButtonView.layout(0, top - addTop, r, top + measuredHeight - addTop);
            }

            // layout cropButtonView
            if (cropButtonView != null) {
                int measuredHeight = cropButtonView.getMeasuredHeight();
                top = contentHeight + (heightLeft - measuredHeight) / 2;
                cropButtonView.layout(0, top, r, top + measuredHeight);
            }

        } else {
            int contentWidth = height * 4 / 3;
            int widthLeft = width - contentWidth;
            contentView.layout(l, t, contentWidth, height);

            backgroundRect.left = contentWidth;
            backgroundRect.top = 0;
            backgroundRect.right = width;
            backgroundRect.bottom = height;

            // layout centerView
            if (topTipsView != null) {
                left = contentWidth + (widthLeft - topTipsView.getMeasuredWidth()) / 2;
                top = (height - topTipsView.getMeasuredHeight()) / 2;
                topTipsView.layout(left, top, left + topTipsView.getMeasuredWidth(), top + topTipsView.getMeasuredHeight());
            }

            // layout leftDownView
            if (leftDownView != null) {
                MarginLayoutParams leftDownViewLayoutParams = (MarginLayoutParams) leftDownView.getLayoutParams();
                left = contentWidth + (widthLeft - leftDownView.getMeasuredWidth()) / 2;
                top = height - leftDownView.getMeasuredHeight() - leftDownViewLayoutParams.bottomMargin;
                leftDownView.layout(left, top, left + leftDownView.getMeasuredWidth(), top + leftDownView.getMeasuredHeight());
            }

            // layout rightUpView
            if (rightUpView != null) {
                MarginLayoutParams rightUpViewLayoutParams = (MarginLayoutParams) rightUpView.getLayoutParams();
                left = contentWidth + (widthLeft - rightUpView.getMeasuredWidth()) / 2;
                top = rightUpViewLayoutParams.topMargin;
                rightUpView.layout(left, top, left + rightUpView.getMeasuredWidth(), top + rightUpView.getMeasuredHeight());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(backgroundRect, mPaint);
    }
}
