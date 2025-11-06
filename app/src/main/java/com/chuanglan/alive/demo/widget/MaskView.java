package com.chuanglan.alive.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.chuanglan.alive.demo.R;
import com.chuanglan.alive.demo.utils.ScreenUtils;

import java.io.File;

import androidx.annotation.IntDef;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

/**
 * 自定义拍证件照时的遮罩 View
 */
@SuppressWarnings("unused")
public class MaskView extends View {

    public static final int MASK_TYPE_NONE = 0;
    public static final int MASK_TYPE_ID_CARD_FRONT = 1;
    public static final int MASK_TYPE_ID_CARD_BACK = 2;
    public static final int MASK_TYPE_BANK_CARD = 11;
    public static final int MASK_TYPE_PASSPORT = 21;
    /**
     * 通用 OCR 遮罩类型，驾驶证、行驶证、银行卡等
     */
    public static final int MASK_TYPE_GENERAL = 3;

    @IntDef({MASK_TYPE_NONE, MASK_TYPE_ID_CARD_FRONT, MASK_TYPE_ID_CARD_BACK, MASK_TYPE_BANK_CARD,
            MASK_TYPE_PASSPORT, MASK_TYPE_GENERAL})
    @interface MaskType {

    }

    /**
     * 遮罩取景框边框线的颜色 Color.WHITE
     */
    private int lineColor = Color.WHITE;

    private int maskType = MASK_TYPE_ID_CARD_FRONT;

    /**
     * 卡片遮罩的颜色 #FFFAFAFA #64000000
     */
    private int maskColor = Color.parseColor("#FFFAFAFA");
    private final Paint eraser = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pen = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Rect frame = new Rect();
    private Rect framePassport = new Rect();
    /**
     * 定位图，例如身份证的头像虚线框和国徽虚线框
     */
    private Drawable locatorDrawable;
    /**
     * 预览框距离顶部位置，不设置则默认为竖直居中
     */
    private float previewMarginTop = -1;

    public MaskView(Context context) {
        super(context);
        init();
    }

    public MaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        parseAttrs(attrs);
    }

    public MaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttrs(attrs);
        init();
    }

    private void parseAttrs(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs,
                R.styleable.MaskView, 0, 0);
        int paintColor;
        try {
            maskColor = a.getColor(R.styleable.MaskView_maskColor, Color.parseColor("#FFFAFAFA"));
            previewMarginTop = a.getDimension(R.styleable.MaskView_previewMarginTop, -1);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        locatorDrawable = ResourcesCompat.getDrawable(getResources(),
                R.drawable.ocr_id_card_locator_front, null);

        mPaintLine.setColor(Color.BLUE);
        mPaintLine.setStrokeWidth(6.0F);
    }

    public Rect getFrameRect() {
        if (maskType == MASK_TYPE_NONE) {
            return new Rect(0, 0, getWidth(), getHeight());
        } else if (maskType == MASK_TYPE_PASSPORT) {
            return new Rect(framePassport);
        } else {
            return new Rect(frame);
        }
    }

    public Rect getFrameRectExtend() {
        Rect rect = new Rect(frame);

        int widthExtend = (int) ((frame.right - frame.left) * 0.02f);
        int heightExtend = (int) ((frame.bottom - frame.top) * 0.02f);

        rect.left -= widthExtend;
        rect.right += widthExtend;
        rect.top -= heightExtend;
        rect.bottom += heightExtend;

        return rect;
    }

    public void setMaskType(@MaskType int maskType) {
        this.maskType = maskType;
        switch (maskType) {
            case MASK_TYPE_ID_CARD_FRONT:
                locatorDrawable = ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ocr_id_card_locator_front, null);
                break;
            case MASK_TYPE_ID_CARD_BACK:
                locatorDrawable = ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ocr_id_card_locator_back, null);
                break;
            case MASK_TYPE_PASSPORT:
                break;
            case MASK_TYPE_BANK_CARD:
                break;
            case MASK_TYPE_GENERAL:
                break;
            case MASK_TYPE_NONE:
                break;
            default:
                break;
        }
        invalidate();
    }

    /**
     * 当View还没有绘制完成时，可以调用此方法
     */
    public void setPreviewMarginTop(int margin) {
        previewMarginTop = margin;
    }

    /**
     * 当View绘制完成时，可以调用此方法
     */
    public void setPreviewMarginTopAndInvalidate(int margin) {
        previewMarginTop = margin;

        int height = frame.height();
        int top = ScreenUtils.dpToPx(margin);
        int bottom = height + top;

        frame.top = top;
        frame.bottom = bottom;

        invalidate();
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    public int getMaskType() {
        return maskType;
    }

    public void setOrientation(@CameraView.Orientation int orientation) {
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            if (maskType != MASK_TYPE_PASSPORT) {
                float ratio = h > w ? 0.9f : 0.72f;

                int width = (int) (w * ratio);
                int height = width * 400 / 620;

                int left = (w - width) / 2;
                int top;
                if (previewMarginTop == -1) {
                    top = (h - height) / 2;
                } else {
                    // 计算证件取景框距离顶部的距离
                    top = ScreenUtils.dpToPx((int) previewMarginTop);
                }
                int right = width + left;
                int bottom = height + top;

                frame.left = left;
                frame.top = top;
                frame.right = right;
                frame.bottom = bottom;
            } else {
                float ratio = 0.9f;

                int width = (int) (w * ratio);
                int height = width * 330 / 470;

                int left = (w - width) / 2;
                int top = (h - height) / 2;
                int right = width + left;
                int bottom = height + top;

                framePassport.left = left;
                framePassport.top = top;
                framePassport.right = right;
                framePassport.bottom = bottom;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect frame = this.frame;
        if (maskType == MASK_TYPE_PASSPORT) {
            frame = framePassport;
        }

        int width = frame.width();
        int height = frame.height();

        int left = frame.left;
        int top = frame.top;
        int right = frame.right;
        int bottom = frame.bottom;

        canvas.drawColor(maskColor);
        fillRectRound(left, top, right, bottom, 30, 30, false);
        canvas.drawPath(path, pen);
        canvas.drawPath(path, eraser);

        if (maskType == MASK_TYPE_ID_CARD_FRONT) {
            locatorDrawable.setBounds(
                    (int) (left + 601f / 1006 * width),
                    (int) (top + (110f / 632) * height),
                    (int) (left + (963f / 1006) * width),
                    (int) (top + (476f / 632) * height));
        } else if (maskType == MASK_TYPE_ID_CARD_BACK) {
            locatorDrawable.setBounds(
                    (int) (left + 51f / 1006 * width),
                    (int) (top + (48f / 632) * height),
                    (int) (left + (250f / 1006) * width),
                    (int) (top + (262f / 632) * height));
        } else if (maskType == MASK_TYPE_PASSPORT) {
            locatorDrawable.setBounds(
                    (int) (left + 30f / 1006 * width),
                    (int) (top + (20f / 632) * height),
                    (int) (left + (303f / 1006) * width),
                    (int) (top + (416f / 632) * height));
        }

        if (locatorDrawable != null) {
            locatorDrawable.draw(canvas);
        }

        // 当遮罩类型为通用时，需要画四个角的定位点
        if (maskType == MASK_TYPE_GENERAL) {
            int lineLength = ScreenUtils.dpToPx(24);
            canvas.drawLine(left, top, left + lineLength, top, mPaintLine);
            canvas.drawLine(left, top, left, top + lineLength, mPaintLine);
            canvas.drawLine(left, bottom, left + lineLength, bottom, mPaintLine);
            canvas.drawLine(left, bottom, left, bottom - lineLength, mPaintLine);
            canvas.drawLine(right, top, right, top + lineLength, mPaintLine);
            canvas.drawLine(right, top, right - lineLength, top, mPaintLine);
            canvas.drawLine(right, bottom, right - lineLength, bottom, mPaintLine);
            canvas.drawLine(right, bottom, right, bottom - lineLength, mPaintLine);
        }
    }

    private Path path = new Path();

    private Path fillRectRound(float left, float top, float right, float bottom, float rx, float ry,
                               boolean conformToOriginalPost) {

        path.reset();
        if (rx < 0) {
            rx = 0;
        }
        if (ry < 0) {
            ry = 0;
        }
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2) {
            rx = width / 2;
        }
        if (ry > height / 2) {
            ry = height / 2;
        }
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        path.rQuadTo(0, -ry, -rx, -ry);
        path.rLineTo(-widthMinusCorners, 0);
        path.rQuadTo(-rx, 0, -rx, ry);
        path.rLineTo(0, heightMinusCorners);

        if (conformToOriginalPost) {
            path.rLineTo(0, ry);
            path.rLineTo(width, 0);
            path.rLineTo(0, -ry);
        } else {
            path.rQuadTo(0, ry, rx, ry);
            path.rLineTo(widthMinusCorners, 0);
            path.rQuadTo(rx, 0, rx, -ry);
        }

        path.rLineTo(0, -heightMinusCorners);
        path.close();
        return path;
    }

    {
        // 硬件加速不支持，图层混合。
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        pen.setColor(lineColor);
        pen.setStyle(Paint.Style.STROKE);
        pen.setStrokeWidth(6);

        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private void capture(File file) {

    }
}
