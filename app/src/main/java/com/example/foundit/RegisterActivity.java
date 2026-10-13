package com.example.foundit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.example.foundit.api.*;
import com.example.foundit.model.*;
import okhttp3.ResponseBody;
import retrofit2.*;

public class RegisterActivity extends BaseActivity {
    EditText name, studentId, email, password, confirm;
    Button register;
    ProgressBar progress;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_register);
        applyWindowInsets(findViewById(R.id.rootRegisterLayout));

        name = findViewById(R.id.etName);
        studentId = findViewById(R.id.etStudentId);
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        confirm = findViewById(R.id.etPasswordConfirm);
        register = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progressRegister);

        password.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) { validatePasswordLive(); }
        });

        confirm.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) { validatePasswordLive(); }
        });

        register.setOnClickListener(v -> doRegister());
    }

    private void validatePasswordLive() {
        String p = password.getText().toString();
        String pc = confirm.getText().toString();
        com.google.android.material.textfield.TextInputLayout tilPass = findViewById(R.id.tilPassword);
        com.google.android.material.textfield.TextInputLayout tilConf = findViewById(R.id.tilPasswordConfirm);

        if (p.isEmpty()) {
            if (tilPass != null) {
                tilPass.setError(null);
                tilPass.setHelperText(null);
            }
        } else if (p.length() < 8) {
            if (tilPass != null) {
                tilPass.setError("Password must be at least 8 characters");
            }
        } else {
            if (tilPass != null) {
                tilPass.setError(null);
                tilPass.setHelperText("Valid password");
                tilPass.setBoxStrokeColor(androidx.core.content.ContextCompat.getColor(this, R.color.foundit_found));
            }
        }

        if (pc.isEmpty()) {
            if (tilConf != null) {
                tilConf.setError(null);
                tilConf.setHelperText(null);
            }
        } else if (!p.equals(pc)) {
            if (tilConf != null) {
                tilConf.setError("Passwords do not match");
            }
        } else {
            if (tilConf != null) {
                tilConf.setError(null);
                tilConf.setHelperText("Passwords match");
                tilConf.setBoxStrokeColor(androidx.core.content.ContextCompat.getColor(this, R.color.foundit_found));
            }
        }
    }

    private void doRegister() {
        String n = name.getText().toString().trim();
        String sid = studentId.getText().toString().trim();
        String e = email.getText().toString().trim();
        String p = password.getText().toString();
        String pc = confirm.getText().toString();

        if (n.isEmpty() || e.isEmpty() || p.isEmpty()) {
            toast("Complete all fields.");
            return;
        }

        boolean isBpsu = false;
        if (e.contains("@")) {
            String domain = e.substring(e.lastIndexOf("@") + 1).toLowerCase(java.util.Locale.US);
            if (domain.equals("bpsu.edu.ph")) {
                isBpsu = true;
            }
        }

        if (isBpsu && sid.isEmpty()) {
            toast("Student ID is required for BPSU student accounts.");
            return;
        }

        if (p.length() < 8) {
            toast("Password must be at least 8 characters.");
            password.requestFocus();
            return;
        }

        if (!p.equals(pc)) {
            toast("Passwords do not match.");
            confirm.requestFocus();
            return;
        }

        setLoading(true);
        RetrofitClient.api().register(new RegisterRequest(n, sid, e, p, pc))
                .enqueue(new Callback<AuthResponse>() {
                    @Override public void onResponse(Call<AuthResponse> c, Response<AuthResponse> r) {
                        setLoading(false);
                        if (r.isSuccessful() && r.body() != null && r.body().token != null) {
                            session.save(r.body().token, r.body().user.name, r.body().user.id, r.body().user.role);
                            registerFcmToken();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finishAffinity();
                        } else {
                            if (r.code() >= 500) {
                                toast("Server error. Please try again later.");
                            } else {
                                String msg = "Registration failed.";
                                try (ResponseBody errorBody = r.errorBody()) {
                                    if (errorBody != null) {
                                        String errorJson = errorBody.string();
                                        if (errorJson.contains("\"message\"")) {
                                            int start = errorJson.indexOf("\"message\":\"") + 11;
                                            int end = errorJson.indexOf("\"", start);
                                            if (start > 10 && end > start) {
                                                msg = errorJson.substring(start, end);
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {}
                                toast(msg);
                            }
                        }
                    }
                    @Override public void onFailure(Call<AuthResponse> c, Throwable t) {
                        setLoading(false);
                        toast("Connection failed: " + t.getMessage());
                    }
                });
    }

    private void setLoading(boolean loading) {
        register.setEnabled(!loading);
        name.setEnabled(!loading);
        studentId.setEnabled(!loading);
        email.setEnabled(!loading);
        password.setEnabled(!loading);
        confirm.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        register.setText(loading ? "CREATING ACCOUNT..." : "REGISTER");
    }
}
