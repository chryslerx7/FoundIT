package com.example.foundit;

import android.content.*;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.foundit.adapter.ItemAdapter;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import retrofit2.*;

public class MainActivity extends BaseActivity {
    RecyclerView recycler;
    ItemAdapter adapter;
    TextView hello, lostCount, foundCount, emptyText;
    SwipeRefreshLayout refresh;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_main);

        hello=findViewById(R.id.tvHello);
        lostCount=findViewById(R.id.tvLostCount);
        foundCount=findViewById(R.id.tvFoundCount);
        recycler=findViewById(R.id.recyclerItems);
        refresh=findViewById(R.id.swipeRefresh);
        emptyText=findViewById(R.id.tvEmptyItems);

        hello.setText("FoundIT");
        adapter = new ItemAdapter(this, item -> openItem(item.id));
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        setupBottomNavigation(R.id.navHome);

        findViewById(R.id.btnNotifications).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.etHomeSearch).setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));
        refresh.setOnRefreshListener(this::load);

        load();
    }

    @Override protected void onResume() { super.onResume(); if (session != null && session.isLoggedIn()) load(); }

    private void load() {
        refresh.setRefreshing(true);
        RetrofitClient.api().getItems(session.authHeader(), "", "ALL", "ALL")
                .enqueue(new Callback<ItemListResponse>() {
                    @Override public void onResponse(Call<ItemListResponse> c, Response<ItemListResponse> r) {
                        refresh.setRefreshing(false);
                        if (r.isSuccessful() && r.body()!=null) {
                            ItemListResponse x=r.body();
                            adapter.setItems(x.items);
                            emptyText.setVisibility(x.items.isEmpty() ? View.VISIBLE : View.GONE);
                            lostCount.setText("Lost Items\n"+x.lost_count);
                            foundCount.setText("Found Items\n"+x.found_count);
                        } else if (r.code()==401) logoutLocal();
                    }
                    @Override public void onFailure(Call<ItemListResponse> c, Throwable t) {
                        refresh.setRefreshing(false);
                        toast("Could not load reports.");
                    }
                });
    }

    private void logoutLocal() {
        session.clear();
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }
}
