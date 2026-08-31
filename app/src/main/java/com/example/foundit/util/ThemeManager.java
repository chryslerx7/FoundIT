package com.example.foundit.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREF_NAME = "foundit_theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static final int THEME_DEVICE = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static int getSavedThemeMode(Context context) {
        return getPrefs(context).getInt(KEY_THEME_MODE, THEME_DEVICE);
    }

    public static void setThemeMode(Context context, int mode) {
        getPrefs(context).edit().putInt(KEY_THEME_MODE, mode).apply();
        applyThemeMode(mode);
    }

    public static void applyTheme(Context context) {
        int mode = getSavedThemeMode(context);
        applyThemeMode(mode);
    }

    public static void applyThemeMode(int mode) {
        switch (mode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_DEVICE:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static String getThemeName(int mode) {
        switch (mode) {
            case THEME_LIGHT:
                return "Light";
            case THEME_DARK:
                return "Dark";
            case THEME_DEVICE:
            default:
                return "Device Default";
        }
    }
}
