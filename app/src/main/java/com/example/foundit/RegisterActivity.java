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

        name = findViewById(R.id.etName);
        studentId = findViewById(R.id.etStudentId);
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        confirm = findViewById(R.id.etPasswordConfirm);
        register = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progressRegister);

        register.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String n=name.getText().toString().trim(), sid=studentId.getText().toString().trim();
        String e=email.getText().toString().trim(), p=password.getText().toString();
        String pc=confirm.getText().toString();

        if (n.isEmpty() || sid.isEmpty() || e.isEmpty() || p.isEmpty()) {
            toast("Complete all fields.");
            return;
        }
        if (!p.equals(pc)) {
            toast("Passwords do not match.");
            return;
        }

        register.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        RetrofitClient.api().register(new RegisterRequest(n,sid,e,p,pc))
                .enqueue(new Callback<AuthResponse>() {
                    @Override public void onResponse(Call<AuthResponse> c, Response<AuthResponse> r) {
                        register.setEnabled(true);
                        progress.setVisibility(View.GONE);
                        if (r.isSuccessful() && r.body() != null && r.body().token != null) {
                            session.save(r.body().token, r.body().user.name, r.body().user.id, r.body().user.role);
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
                                            // Simple way to extract the "message" field value from JSON
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
                        register.setEnabled(true);
                        progress.setVisibility(View.GONE);
                        toast("Connection failed: " + t.getMessage());
                    }
                });
    }
}
