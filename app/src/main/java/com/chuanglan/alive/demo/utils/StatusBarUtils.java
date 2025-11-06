package com.chuanglan.alive.demo.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class StatusBarUtils {

    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static final int SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT = 0x00000010;

    /**
     * 默认使用深色状态栏模式
     */
    public static void tint(Activity activity) {
        tint(activity, true);
    }

    /**
     * @param activity
     * @param useDark  深色模式下，状态栏文字和图标都为白色，否则为黑色
     */
    public static void tint(Activity activity, boolean useDark) {
        if (activity == null) {
            return;
        }
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.TRANSPARENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            setStatusTextColor(useDark, activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            setStatusTextColor(useDark, activity);
        }
    }

    /**
     * @param activity                 current activity object
     * @param useThemeStatusBarColor   是否要状态栏的颜色，不设置则为透明色
     * @param withoutUseStatusBarColor 是否不需要使用状态栏为暗色调
     */
    public static void setStatusBar(Activity activity, boolean useThemeStatusBarColor, boolean withoutUseStatusBarColor) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        // 5.0 及以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);

            if (useThemeStatusBarColor) {
                window.setStatusBarColor(Color.WHITE);
            } else {
                window.setStatusBarColor(Color.TRANSPARENT);
            }

            // 4.4 到 5.0
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = window.getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !withoutUseStatusBarColor) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void reMeasure(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(metrics);
        } else {
            display.getMetrics(metrics);
        }

        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
    }

    /**
     * 设置状态栏文字色值为深色调
     *
     * @param useDark  是否使用深色调(深色调状态栏字体为黑色，浅色调字体为白色)
     * @param activity current activity object
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setStatusTextColor(boolean useDark, Activity activity) {
        if (isFlyme()) {
            // 魅族
            processFlyMe(useDark, activity);
        } else if (isMIUI()) {
            // 小米
            setMIUIStatusTextColor(activity, useDark);
        } else if (Build.MANUFACTURER.equalsIgnoreCase("OPPO")) {
            // OPPO
            setOPPOStatusTextColor(useDark, activity);
        } else {
            // 其他
            setOtherStatusTextColor(activity, useDark);
        }
    }

    /**
     * 判断手机是否是魅族
     */
    private static boolean isFlyme() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * 改变魅族的状态栏字体为黑色，要求FlyMe4以上
     */
    private static void processFlyMe(boolean useDark, Activity activity) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        try {
            Class<?> instance = Class.forName("android.view.WindowManager$LayoutParams");
            int value = instance.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON").getInt(lp);
            Field field = instance.getDeclaredField("meizuFlags");
            field.setAccessible(true);
            int origin = field.getInt(lp);
            if (useDark) {
                field.set(lp, origin | value);
            } else {
                field.set(lp, (~value) & origin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断手机是否是小米
     */
    private static boolean isMIUI() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        } catch (final IOException e) {
            return false;
        }
    }

    /**
     * 小米手机更改状态栏颜色
     */
    private static void setMIUIStatusTextColor(Activity activity, boolean useDark) {
        // 6.0 后小米状态栏用的原生的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setOtherStatusTextColor(activity, useDark);
        } else {
            processMIUI(useDark, activity);
        }
    }

    /**
     * 其他手机更改状态栏字体颜色
     */
    private static void setOtherStatusTextColor(Activity activity, boolean useDark) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();

        if (useDark) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        //setContentPadding(activity);
    }

    /**
     * 设置导航栏高度
     */
    private static void setContentPadding(Activity activity) {
        View view = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        view.setPadding(0, 0, 0, getStatusBarHeight(activity));
    }

    /**
     * 改变小米的状态栏字体颜色为黑色, 要求 MIUI6 以上 useDark 为真时表示黑色字体
     */
    private static void processMIUI(boolean useDark, Activity activity) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag;
            @SuppressLint("PrivateApi")
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), useDark ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置OPPO手机状态栏字体为黑色(colorOS3.0,6.0以下部分手机)
     *
     * @param useDark
     * @param activity
     */
    private static void setOPPOStatusTextColor(boolean useDark, Activity activity) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        int visibility = decorView.getSystemUiVisibility();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (useDark) {
                if (visibility == View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) {
                    visibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                } else {
                    visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
            } else {
                visibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (useDark) {
                visibility |= SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT;
            } else {
                visibility &= ~SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT;
            }
        }

        decorView.setSystemUiVisibility(visibility);
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private static class BuildProperties {

        private final Properties properties;

        private BuildProperties() throws IOException {
            properties = new Properties();
            properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
        }

        public boolean containsKey(final Object key) {
            return properties.containsKey(key);
        }

        public boolean containsValue(final Object value) {
            return properties.containsValue(value);
        }

        public Set<Map.Entry<Object, Object>> entrySet() {
            return properties.entrySet();
        }

        public String getProperty(final String name) {
            return properties.getProperty(name);
        }

        public String getProperty(final String name, final String defaultValue) {
            return properties.getProperty(name, defaultValue);
        }

        public boolean isEmpty() {
            return properties.isEmpty();
        }

        public Enumeration<Object> keys() {
            return properties.keys();
        }

        public Set<Object> keySet() {
            return properties.keySet();
        }

        public int size() {
            return properties.size();
        }

        public Collection<Object> values() {
            return properties.values();
        }

        public static BuildProperties newInstance() throws IOException {
            return new BuildProperties();
        }
    }
}
