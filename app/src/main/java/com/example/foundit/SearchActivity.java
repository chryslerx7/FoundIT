package com.example.foundit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.*;
import com.example.foundit.adapter.ItemAdapter;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import retrofit2.*;
import java.util.Calendar;
import java.util.Locale;

public class SearchActivity extends BaseActivity {
    EditText search;
    Spinner type, category;
    ItemAdapter adapter;
    ProgressBar progress;
    TextView emptyText, errorText;
    Button btnDateFilter, btnClearDate;
    String selectedDate = null;
    int requestSequence = 0;
    Call<ItemListResponse> currentCall;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_search);
        applyWindowInsets(findViewById(R.id.rootSearchLayout));

        search = findViewById(R.id.etSearch);
        type = findViewById(R.id.spType);
        category = findViewById(R.id.spCategory);
        progress = findViewById(R.id.progressSearch);
        emptyText = findViewById(R.id.tvEmptySearch);
        errorText = findViewById(R.id.tvErrorSearch);
        btnDateFilter = findViewById(R.id.btnDateFilter);
        btnClearDate = findViewById(R.id.btnClearDate);

        adapter = new ItemAdapter(this, item -> openItem(item.id));

        RecyclerView rv = findViewById(R.id.recyclerSearch);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        setupBottomNavigation(R.id.navSearch);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { load(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        type.setOnItemSelectedListener(listener);
        category.setOnItemSelectedListener(listener);

        findViewById(R.id.btnSearch).setOnClickListener(v -> load());
        btnDateFilter.setOnClickListener(v -> showDatePicker());
        btnClearDate.setOnClickListener(v -> {
            selectedDate = null;
            btnDateFilter.setText("Filter by Date: All");
            btnClearDate.setVisibility(View.GONE);
            load();
        });

        load();
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (selectedDate != null) {
            try {
                String[] parts = selectedDate.split("-");
                if (parts.length == 3) {
                    cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
                }
            } catch (Exception ignored) {}
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day);
            btnDateFilter.setText("Date: " + selectedDate);
            btnClearDate.setVisibility(View.VISIBLE);
            load();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void load() {
        String t = type.getSelectedItem() == null ? "ALL" : type.getSelectedItem().toString();
        String cat = category.getSelectedItem() == null ? "ALL" : category.getSelectedItem().toString();
        String queryText = search.getText() != null ? search.getText().toString().trim() : "";

        // Cancel previous call if still running
        if (currentCall != null) {
            try { currentCall.cancel(); } catch (Exception ignored) {}
        }

        final int currentReqId = ++requestSequence;

        // States: LOADING
        progress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);

        currentCall = RetrofitClient.api().getItems(session.authHeader(), queryText, t, cat, selectedDate);
        currentCall.enqueue(new Callback<ItemListResponse>() {
            @Override public void onResponse(Call<ItemListResponse> c, Response<ItemListResponse> r) {
                // Stale request protection
                if (currentReqId != requestSequence) return;

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
                    toast("Search failed.");
                }
            }
            @Override public void onFailure(Call<ItemListResponse> c, Throwable t) {
                if (currentReqId != requestSequence) return;
                if (c.isCanceled()) return; // Ignored if cancelled deliberately

                progress.setVisibility(View.GONE);
                errorText.setVisibility(View.VISIBLE);
                toast("Connection failed.");
            }
        });
    }
}
