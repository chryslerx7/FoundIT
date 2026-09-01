package com.example.foundit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import com.example.foundit.util.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Keep the splash screen for a moment to show the tagline
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkAuthAndNavigate();
        }, 2000); // 2 seconds delay
    }

    private void checkAuthAndNavigate() {
        SessionManager session = new SessionManager(this);
        Intent intent;
        if (session.isLoggedIn()) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
