package com.example.foundit;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.*;
import com.example.foundit.adapter.ItemAdapter;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import retrofit2.*;

public class MyReportsActivity extends BaseActivity {
    ItemAdapter adapter;
    ProgressBar progress;
    TextView emptyText, errorText;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_my_reports);
        applyWindowInsets(findViewById(R.id.rootMyReportsLayout));

        progress = findViewById(R.id.progressMyReports);
        emptyText = findViewById(R.id.tvEmptyMyReports);
        errorText = findViewById(R.id.tvErrorMyReports);

        adapter = new ItemAdapter(this, item -> openItem(item.id));
        RecyclerView rv = findViewById(R.id.recyclerMyReports);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        setupBottomNavigation(R.id.navReports);
    }

    @Override protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        progress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);

        RetrofitClient.api().myReports(session.authHeader()).enqueue(new Callback<ItemListResponse>() {
            @Override public void onResponse(Call<ItemListResponse> c, Response<ItemListResponse> r) {
                progress.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    adapter.setItems(r.body().items);
                    if (r.body().items.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                    }
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
}
