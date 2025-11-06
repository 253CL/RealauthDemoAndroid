package com.chuanglan.alive.demo.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    private static final String SHARED_PREF = "com.ksc.alive.demo.SharedPref";
    private static SharedPreferences mSharedPreferences;

    public static void put(Context context, String key, String value) {
        checkNullable(context);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String get(Context context, String key, String defaultValue) {
        checkNullable(context);
        return mSharedPreferences.getString(key, defaultValue);
    }

    private static void checkNullable(Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        }
    }
}
