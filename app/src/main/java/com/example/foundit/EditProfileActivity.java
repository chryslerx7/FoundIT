package com.example.foundit;

import android.content.Intent;
import androidx.activity.OnBackPressedCallback;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.*;
import java.io.*;

public class EditProfileActivity extends BaseActivity {
    EditText name, sid, email, password, confirm;
    ImageView profileImg;
    Button update, cancel;
    Uri imageUri;
    String initialName, initialSid, initialEmail;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_edit_profile);

        name = findViewById(R.id.etEditName);
        sid = findViewById(R.id.etEditStudentId);
        email = findViewById(R.id.etEditEmail);
        password = findViewById(R.id.etEditPassword);
        confirm = findViewById(R.id.etEditPasswordConfirm);
        update = findViewById(R.id.btnUpdateProfile);
        cancel = findViewById(R.id.btnCancelProfile);
        profileImg = findViewById(R.id.ivEditProfileImg);

        setupBottomNavigation(-1);

        initialName = getIntent().getStringExtra("name");
        initialEmail = getIntent().getStringExtra("email");
        initialSid = getIntent().getStringExtra("student_id");

        name.setText(initialName);
        email.setText(initialEmail);
        sid.setText(initialSid);

        String currentImg = getIntent().getStringExtra("image_url");
        if (currentImg != null) {
            Glide.with(this).load(currentImg).into(profileImg);
        }

        findViewById(R.id.tvChangePhoto).setOnClickListener(v -> pickImage());
        update.setOnClickListener(v -> update());
        cancel.setOnClickListener(v -> handleCancel());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                handleCancel();
            }
        });
    }

    private boolean hasChanges() {
        return !name.getText().toString().equals(initialName) ||
                !email.getText().toString().equals(initialEmail) ||
                !sid.getText().toString().equals(initialSid) ||
                !password.getText().toString().isEmpty() ||
                imageUri != null;
    }

    private void handleCancel() {
        if (hasChanges()) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("You have unsaved changes. Are you sure you want to cancel?")
                    .setPositiveButton("Discard", (d, w) -> finish())
                    .setNegativeButton("Keep Editing", null)
                    .show();
        } else {
            finish();
        }
    }

    private void pickImage() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("image/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, 200);
    }

    @Override protected void onActivityResult(int rc, int res, Intent d) {
        super.onActivityResult(rc, res, d);
        if (rc == 200 && res == RESULT_OK && d != null && d.getData() != null) {
            imageUri = d.getData();
            profileImg.setImageURI(imageUri);
        }
    }

    private RequestBody text(String value) {
        return RequestBody.create(value == null ? "" : value, MediaType.parse("text/plain"));
    }

    private void update() {
        String n = name.getText().toString().trim();
        String e = email.getText().toString().trim();
        String s = sid.getText().toString().trim();
        String p = password.getText().toString();
        String pc = confirm.getText().toString();

        if (n.isEmpty() || e.isEmpty() || s.isEmpty()) {
            toast("Name, Email, and Student ID are required.");
            return;
        }

        if (!p.isEmpty() && !p.equals(pc)) {
            toast("Passwords do not match.");
            return;
        }

        MultipartBody.Part part = null;
        if (imageUri != null) {
            try {
                InputStream in = getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[8192]; int len;
                while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
                in.close();
                RequestBody body = RequestBody.create(out.toByteArray(), MediaType.parse(getContentResolver().getType(imageUri)));
                part = MultipartBody.Part.createFormData("profile_image", getFileName(imageUri), body);
            } catch (Exception ex) {
                toast("Could not read image."); return;
            }
        }

        update.setEnabled(false);
        RetrofitClient.api().updateProfile(
                session.authHeader(),
                text("PUT"),
                text(n),
                text(s),
                text(e),
                text(p),
                text(pc),
                part
        ).enqueue(new Callback<AuthResponse>() {
            @Override public void onResponse(Call<AuthResponse> c, Response<AuthResponse> r) {
                update.setEnabled(true);
                if (r.isSuccessful() && r.body() != null) {
                    toast("Profile updated.");
                    session.save(session.token(), r.body().user.name, r.body().user.id, r.body().user.role);
                    finish();
                } else toast("Update failed.");
            }
            @Override public void onFailure(Call<AuthResponse> c, Throwable t) {
                update.setEnabled(true);
                toast("Connection failed.");
            }
        });
    }

    private String getFileName(Uri uri) {
        Cursor c = getContentResolver().query(uri, null, null, null, null);
        if (c != null) {
            try {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (c.moveToFirst() && idx >= 0) return c.getString(idx);
            } finally { c.close(); }
        }
        return "profile.jpg";
    }
}
