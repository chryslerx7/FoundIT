package com.example.foundit;

import android.content.*;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import com.example.foundit.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BaseActivity extends AppCompatActivity {
    protected SessionManager session;
    private GestureDetector gestureDetector;
    private View bottomNav;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        setupDoubleTap();
    }

    protected void setupBottomNavigation(int activeId) {
        bottomNav = findViewById(R.id.bottomNavContainer);
        if (bottomNav == null) return;

        ImageButton home = findViewById(R.id.navHome);
        ImageButton search = findViewById(R.id.navSearch);
        FloatingActionButton add = findViewById(R.id.navAdd);
        ImageButton reports = findViewById(R.id.navReports);
        ImageButton profile = findViewById(R.id.navProfile);

        home.setOnClickListener(v -> navigate(MainActivity.class));
        search.setOnClickListener(v -> navigate(SearchActivity.class));
        add.setOnClickListener(v -> chooseReport());
        reports.setOnClickListener(v -> navigate(MyReportsActivity.class));
        profile.setOnClickListener(v -> navigate(ProfileActivity.class));

        View navBarCard = findViewById(R.id.navBarCard);
        if (navBarCard != null) navBarCard.setZ(0f);
        add.setZ(100f);
        add.bringToFront();

        int blue = ContextCompat.getColor(this, R.color.blue);
        if (activeId == R.id.navHome) home.setColorFilter(blue);
        else if (activeId == R.id.navSearch) search.setColorFilter(blue);
        else if (activeId == R.id.navReports) reports.setColorFilter(blue);
        else if (activeId == R.id.navProfile) profile.setColorFilter(blue);
    }

    private void navigate(Class<?> cls) {
        if (this.getClass() == cls) return;
        Intent i = new Intent(this, cls);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }

    protected void chooseReport() {
        if ("visitor".equalsIgnoreCase(session.role())) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Access Restricted")
                    .setMessage("Please log in with a Student, Teacher, or Staff account to use this feature.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        String[] options = {"I Lost Something", "I Found Something", "Cancel"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("What would you like to report?")
                .setItems(options, (d, which) -> {
                    if (which == 0 || which == 1) {
                        Intent i = new Intent(this, ReportActivity.class);
                        i.putExtra("type", which == 0 ? "LOST" : "FOUND");
                        startActivity(i);
                    }
                }).show();
    }

    private void setupDoubleTap() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDoubleTap(MotionEvent e) {
                if (bottomNav != null) {
                    bottomNav.setVisibility(bottomNav.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                    return true;
                }
                return false;
            }
        });
    }

    @Override public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    protected boolean requireLogin() {
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return false;
        }
        return true;
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void openItem(int id) {
        Intent i = new Intent(this, ItemDetailActivity.class);
        i.putExtra("item_id", id);
        startActivity(i);
    }
}
