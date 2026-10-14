package com.example.foundit;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import com.example.foundit.util.ThemeManager;

public class FoundItApp extends Application {
    private static Activity resumedActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applyTheme(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
            @Override public void onActivityStarted(Activity activity) {}
            @Override public void onActivityResumed(Activity activity) {
                resumedActivity = activity;
            }
            @Override public void onActivityPaused(Activity activity) {
                if (resumedActivity == activity) {
                    resumedActivity = null;
                }
            }
            @Override public void onActivityStopped(Activity activity) {}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override public void onActivityDestroyed(Activity activity) {}
        });
    }

    public static Activity getResumedActivity() {
        return resumedActivity;
    }
}
