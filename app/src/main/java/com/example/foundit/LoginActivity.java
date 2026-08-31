package com.example.foundit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.example.foundit.api.*;
import com.example.foundit.model.*;
import retrofit2.*;

public class LoginActivity extends BaseActivity {
    EditText email, password;
    Button login;
    TextView register;
    ProgressBar progress;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        login = findViewById(R.id.btnLogin);
        register = findViewById(R.id.tvRegister);
        progress = findViewById(R.id.progressLogin);

        login.setOnClickListener(v -> doLogin());
        register.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void doLogin() {
        String e = email.getText().toString().trim();
        String p = password.getText().toString();

        if (e.isEmpty() || p.isEmpty()) {
            toast("Enter your email and password.");
            return;
        }

        login.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        RetrofitClient.api().login(new LoginRequest(e, p, "FoundIT Android"))
                .enqueue(new Callback<AuthResponse>() {
                    @Override public void onResponse(Call<AuthResponse> c, Response<AuthResponse> r) {
                        login.setEnabled(true);
                        progress.setVisibility(View.GONE);
                        if (r.isSuccessful() && r.body() != null && r.body().token != null) {
                            session.save(r.body().token, r.body().user.name, r.body().user.id, r.body().user.role);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            toast("Login failed. Check your credentials.");
                        }
                    }
                    @Override public void onFailure(Call<AuthResponse> c, Throwable t) {
                        login.setEnabled(true);
                        progress.setVisibility(View.GONE);
                        toast("Connection failed: " + t.getMessage());
                    }
                });
    }
}
