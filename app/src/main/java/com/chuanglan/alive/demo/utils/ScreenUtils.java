package com.chuanglan.alive.demo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class ScreenUtils {

    private static final int SCREEN_1280X720 = 1280 * 720;
    private static Handler mainHandler;

    /**
     * 获取屏幕宽度
     *
     * @param context 上下文对象
     * @param isDp    单位是否是dp，true：单位dp；false：单位px
     * @return
     */
    public static int getScreenWidth(Context context, boolean isDp) {
        int screenWidth = 0;
        int winWidth;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = wm.getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        if (point.x > point.y) {
            winWidth = point.y;
        } else {
            winWidth = point.x;
        }
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        if (!isDp) {
            return winWidth;
        }
        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        screenWidth = (int) (winWidth / density);// 屏幕高度(dp)
        return screenWidth;
    }

    /**
     * 获取屏幕高度
     *
     * @param context 上下文对象
     * @param isDp    单位是否是dp，true：单位dp；false：单位px
     * @return
     */
    public static int getScreenHeight(Context context, boolean isDp) {
        int screenHeight = 0;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;       // 屏幕高度（像素）
        if (!isDp) {
            return height;
        }

        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        screenHeight = (int) (height / density);// 屏幕高度(dp)
        return screenHeight;
    }

    /**
     * dp转换成px
     */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static void showToast(final Context context, final String msg) {
        mainHandler = new Handler(Looper.getMainLooper());
        execute(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean is1280x720(Context context) {
        return SCREEN_1280X720 == getScreenTotalPixels(context);
    }

    /**
     * 获取屏幕总像素
     */
    public static int getScreenTotalPixels(Context context) {
        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                Display display = context.getDisplay();
                display.getRealMetrics(dm);
            } catch (NoSuchMethodError e) {
                DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                int screenWidth = displayMetrics.widthPixels;
                int screenHeight = displayMetrics.heightPixels;
                return screenWidth * screenHeight;
            }
        } else {
            wm.getDefaultDisplay().getMetrics(dm);
        }

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        return width * height;
    }

    private static void execute(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

}