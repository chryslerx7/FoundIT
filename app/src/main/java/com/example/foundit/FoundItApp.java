package com.example.foundit;

import android.app.Application;
import com.example.foundit.util.ThemeManager;

public class FoundItApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applyTheme(this);
    }
}
