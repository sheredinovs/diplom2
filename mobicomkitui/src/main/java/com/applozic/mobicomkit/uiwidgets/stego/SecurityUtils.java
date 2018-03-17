package com.applozic.mobicomkit.uiwidgets.stego;

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by kadyr on 03.03.2018.
 */

public class SecurityUtils {
    private static final String APP_PREFERENCES = "stego_chat";
    private static final String APP_PREFERENCES_MODE = "stego_mode";
    static SharedPreferences mSettings;
    static final String MODE_ON = "on";
    static final String MODE_OFF = "off";


    public static boolean isStegoModOn(Context context){
        String mode = null;
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_MODE)) {
            mode = mSettings.getString(APP_PREFERENCES_MODE, "");
        }
        return StringUtils.equals(mode, MODE_ON);
    }

    public static boolean isStegoModOff(Context context){
        String mode = null;
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_MODE)) {
            mode = mSettings.getString(APP_PREFERENCES_MODE, "");
        }
        return (!StringUtils.equals(mode, MODE_ON) || mode == null);
    }

    public static void setEnableStegoMode(Context context){
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_MODE, MODE_ON);
        editor.apply();
    }


    public static void disableStegoMode(Context context){
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_MODE, MODE_OFF);
        editor.apply();
    }
}
