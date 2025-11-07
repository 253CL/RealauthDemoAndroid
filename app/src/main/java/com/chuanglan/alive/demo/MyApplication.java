package com.chuanglan.alive.demo;

import android.app.Application;

import com.chuanglan.sdk.CLBaseManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 设置调试模式，可以输出日志，方便调试，生产环境请关闭此开关
        CLBaseManager.setDebuggable(true);
        //CLBaseManager.setWarnDeprecated(true);
        CLBaseManager.init(getApplicationContext(), BuildConfig.APPID);
    }
}