package com.example.foundit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.User;
import com.example.foundit.util.ThemeManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import retrofit2.*;

public class ProfileActivity extends BaseActivity {
    TextView name, sid, email, role, reportsCount, resolvedCount;
    ImageView profileImg;
    User currentUser;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_profile);

        name = findViewById(R.id.tvProfileName);
        sid = findViewById(R.id.tvProfileStudentId);
        email = findViewById(R.id.tvProfileEmail);
        role = findViewById(R.id.tvProfileRole);
        reportsCount = findViewById(R.id.tvReportsCount);
        resolvedCount = findViewById(R.id.tvResolvedCount);
        profileImg = findViewById(R.id.ivProfileImg);

        setupBottomNavigation(R.id.navProfile);

        findViewById(R.id.btnEditProfile).setOnClickListener(v -> edit());
        findViewById(R.id.btnTheme).setOnClickListener(v -> showThemeDialog());
        findViewById(R.id.btnMyMessages).setOnClickListener(v -> startActivity(new Intent(this, ConversationListActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        // Dummy listeners for study UI
        findViewById(R.id.btnNotifications).setOnClickListener(v -> toast("Notification settings coming soon."));
        findViewById(R.id.btnHelp).setOnClickListener(v -> toast("Support center coming soon."));
        findViewById(R.id.btnAbout).setOnClickListener(v -> toast("FoundIT v1.0"));
    }

    private void showThemeDialog() {
        int currentMode = ThemeManager.getSavedThemeMode(this);
        String[] options = {
                getString(R.string.theme_device_default),
                getString(R.string.theme_light),
                getString(R.string.theme_dark)
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.theme_dialog_title)
                .setSingleChoiceItems(options, currentMode, (dialog, which) -> {
                    dialog.dismiss();
                    if (which != currentMode) {
                        ThemeManager.setThemeMode(ProfileActivity.this, which);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override protected void onResume() {
        super.onResume();
        load();
    }

    private void edit() {
        if (currentUser == null) return;
        Intent i = new Intent(this, EditProfileActivity.class);
        i.putExtra("name", currentUser.name);
        i.putExtra("email", currentUser.email);
        i.putExtra("student_id", currentUser.student_id);
        i.putExtra("image_url", currentUser.profile_image_url);
        startActivity(i);
    }

    private void load() {
        RetrofitClient.api().me(session.authHeader()).enqueue(new Callback<User>() {
            @Override public void onResponse(Call<User> c, Response<User> r) {
                if(r.isSuccessful() && r.body() != null) {
                    currentUser = r.body();
                    name.setText(currentUser.name);
                    sid.setText(currentUser.student_id);
                    email.setText(currentUser.email);
                    role.setText("Role: " + (currentUser.role_name != null ? currentUser.role_name : "User"));
                    reportsCount.setText(String.valueOf(currentUser.reports_count));
                    resolvedCount.setText(String.valueOf(currentUser.resolved_count));

                    if (currentUser.profile_image_url != null && !currentUser.profile_image_url.isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(currentUser.profile_image_url)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_gallery)
                                .into(profileImg);
                    } else {
                        profileImg.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else if (r.code() == 401) {
                    session.clear();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finishAffinity();
                } else {
                    toast("Could not load profile (Error " + r.code() + ")");
                }
            }
            @Override public void onFailure(Call<User> c, Throwable t) {
                toast("Could not load profile.");
            }
        });
    }

    private void logout() {
        RetrofitClient.api().logout(session.authHeader()).enqueue(new Callback<com.example.foundit.model.ApiMessage>() {
            @Override public void onResponse(Call<com.example.foundit.model.ApiMessage> c, Response<com.example.foundit.model.ApiMessage> r) {
                session.clear(); goLogin();
            }
            @Override public void onFailure(Call<com.example.foundit.model.ApiMessage> c, Throwable t) {
                session.clear(); goLogin();
            }
        });
    }

    private void goLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }
}
