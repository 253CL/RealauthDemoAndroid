package com.chuanglan.alive.demo.utils;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by x-sir on 4/19/21 :)
 * Function:
 */
public class WindowUtils {

    /**
     * 设置当前窗口亮度
     *
     * @param isOpen
     */
    public static void setWindowBrightness(Activity context, boolean isOpen) {
        Window window = context.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = isOpen
                ? WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
                : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;

        window.setAttributes(lp);
    }

}
