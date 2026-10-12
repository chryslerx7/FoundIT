package com.example.foundit;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
    Button btnTabLost, btnTabFound, btnTabResolved, btnTabDrafts;
    SwipeRefreshLayout swipeRefresh;
    List<Item> allReports = new ArrayList<>();
    int currentTab = 0; // 0 = LOST, 1 = FOUND, 2 = RESOLVED, 3 = DRAFTS

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
        btnTabDrafts = findViewById(R.id.btnTabDrafts);
        swipeRefresh = findViewById(R.id.swipeMyReportsRefresh);

        adapter = new ItemAdapter(this, item -> {
            if ("DRAFT".equals(item.status)) {
                android.content.Intent i = new android.content.Intent(this, ReportActivity.class);
                i.putExtra("draft_id", item.draftId);
                startActivity(i);
            } else {
                openItem(item.id);
            }
        });
        adapter.setOnItemLongClick(item -> {
            if ("DRAFT".equals(item.status)) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle("Delete Draft?")
                        .setMessage("Are you sure you want to delete this draft?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            com.example.foundit.util.ReportDraftManager.deleteDraft(this, item.draftId);
                            load();
                            toast("Draft deleted.");
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        RecyclerView rv = findViewById(R.id.recyclerMyReports);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        setupBottomNavigation(R.id.navReports);

        btnTabLost.setOnClickListener(v -> { currentTab = 0; updateTabs(); filterAndDisplay(); });
        btnTabFound.setOnClickListener(v -> { currentTab = 1; updateTabs(); filterAndDisplay(); });
        btnTabResolved.setOnClickListener(v -> { currentTab = 2; updateTabs(); filterAndDisplay(); });
        btnTabDrafts.setOnClickListener(v -> { currentTab = 3; updateTabs(); filterAndDisplay(); });
        swipeRefresh.setOnRefreshListener(this::load);
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

        btnTabDrafts.setBackgroundColor(currentTab == 3 ? primaryColor : surfaceElevated);
        btnTabDrafts.setTextColor(currentTab == 3 ? Color.WHITE : textPrimary);
    }

    private void load() {
        if (currentTab == 3) {
            progress.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
            filterAndDisplay();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);

        RetrofitClient.api().myReports(session.authHeader()).enqueue(new Callback<ItemListResponse>() {
            @Override public void onResponse(Call<ItemListResponse> c, Response<ItemListResponse> r) {
                progress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
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
                swipeRefresh.setRefreshing(false);
                errorText.setVisibility(View.VISIBLE);
                toast("Connection failed.");
            }
        });
    }

    private void filterAndDisplay() {
        List<Item> filtered = new ArrayList<>();
        if (currentTab == 3) {
            List<com.example.foundit.util.ReportDraftManager.ReportDraft> drafts = 
                com.example.foundit.util.ReportDraftManager.getDrafts(this, session.userId());
            for (com.example.foundit.util.ReportDraftManager.ReportDraft d : drafts) {
                Item item = new Item();
                item.draftId = d.draftId;
                item.item_name = (d.name == null || d.name.isEmpty()) ? "Untitled draft" : d.name;
                item.category = d.category;
                item.type = d.type;
                item.status = "DRAFT";
                item.date = d.date;
                item.location = d.location;
                if (d.imageUriStrings != null && !d.imageUriStrings.isEmpty()) {
                    item.image_url = d.imageUriStrings.get(0);
                }
                filtered.add(item);
            }
            adapter.setItems(filtered);
            if (filtered.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                emptyText.setText("No saved drafts found.");
            } else {
                emptyText.setVisibility(View.GONE);
            }
            return;
        }

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
