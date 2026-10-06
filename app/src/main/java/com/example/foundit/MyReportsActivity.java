package com.example.foundit;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.*;
import com.example.foundit.adapter.ItemAdapter;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import retrofit2.*;
import java.util.ArrayList;
import java.util.List;

public class MyReportsActivity extends BaseActivity {
    ItemAdapter adapter;
    ProgressBar progress;
    TextView emptyText, errorText;
    Button btnTabLost, btnTabFound, btnTabResolved;
    List<Item> allReports = new ArrayList<>();
    int currentTab = 0; // 0 = LOST, 1 = FOUND, 2 = RESOLVED

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_my_reports);
        applyWindowInsets(findViewById(R.id.rootMyReportsLayout));

        progress = findViewById(R.id.progressMyReports);
        emptyText = findViewById(R.id.tvEmptyMyReports);
        errorText = findViewById(R.id.tvErrorMyReports);
        btnTabLost = findViewById(R.id.btnTabLost);
        btnTabFound = findViewById(R.id.btnTabFound);
        btnTabResolved = findViewById(R.id.btnTabResolved);

        adapter = new ItemAdapter(this, item -> openItem(item.id));
        RecyclerView rv = findViewById(R.id.recyclerMyReports);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        setupBottomNavigation(R.id.navReports);

        btnTabLost.setOnClickListener(v -> { currentTab = 0; updateTabs(); filterAndDisplay(); });
        btnTabFound.setOnClickListener(v -> { currentTab = 1; updateTabs(); filterAndDisplay(); });
        btnTabResolved.setOnClickListener(v -> { currentTab = 2; updateTabs(); filterAndDisplay(); });
    }

    @Override protected void onResume() {
        super.onResume();
        load();
    }

    private void updateTabs() {
        int primaryColor = getColor(R.color.foundit_primary);
        int surfaceElevated = getColor(R.color.foundit_surface_elevated);
        int textPrimary = getColor(R.color.foundit_text_primary);

        btnTabLost.setBackgroundColor(currentTab == 0 ? primaryColor : surfaceElevated);
        btnTabLost.setTextColor(currentTab == 0 ? Color.WHITE : textPrimary);

        btnTabFound.setBackgroundColor(currentTab == 1 ? primaryColor : surfaceElevated);
        btnTabFound.setTextColor(currentTab == 1 ? Color.WHITE : textPrimary);

        btnTabResolved.setBackgroundColor(currentTab == 2 ? primaryColor : surfaceElevated);
        btnTabResolved.setTextColor(currentTab == 2 ? Color.WHITE : textPrimary);
    }

    private void load() {
        progress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);

        RetrofitClient.api().myReports(session.authHeader()).enqueue(new Callback<ItemListResponse>() {
            @Override public void onResponse(Call<ItemListResponse> c, Response<ItemListResponse> r) {
                progress.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null && r.body().items != null) {
                    allReports = r.body().items;
                    filterAndDisplay();
                    errorText.setVisibility(View.GONE);
                } else {
                    errorText.setVisibility(View.VISIBLE);
                    toast("Could not load reports.");
                }
            }
            @Override public void onFailure(Call<ItemListResponse> c, Throwable t) {
                progress.setVisibility(View.GONE);
                errorText.setVisibility(View.VISIBLE);
                toast("Connection failed.");
            }
        });
    }

    private void filterAndDisplay() {
        List<Item> filtered = new ArrayList<>();
        for (Item item : allReports) {
            String status = item.status == null ? "ACTIVE" : item.status;
            String type = item.type == null ? "LOST" : item.type;

            if (currentTab == 2) {
                // RESOLVED tab
                if ("RESOLVED".equalsIgnoreCase(status)) {
                    filtered.add(item);
                }
            } else if (currentTab == 1) {
                // FOUND tab
                if (!"RESOLVED".equalsIgnoreCase(status) && "FOUND".equalsIgnoreCase(type)) {
                    filtered.add(item);
                }
            } else {
                // LOST tab (default 0)
                if (!"RESOLVED".equalsIgnoreCase(status) && "LOST".equalsIgnoreCase(type)) {
                    filtered.add(item);
                }
            }
        }

        adapter.setItems(filtered);
        if (filtered.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            if (currentTab == 2) {
                emptyText.setText("No resolved reports found.");
            } else if (currentTab == 1) {
                emptyText.setText("No active found reports found.");
            } else {
                emptyText.setText("No active lost reports found.");
            }
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }
}
