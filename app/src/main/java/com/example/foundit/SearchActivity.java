package com.example.foundit;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.*;
import com.example.foundit.adapter.ItemAdapter;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import retrofit2.*;

public class SearchActivity extends BaseActivity {
    EditText search; Spinner type, category; ItemAdapter adapter;
    ProgressBar progress; TextView emptyText;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_search);
        applyWindowInsets(findViewById(R.id.rootSearchLayout));

        search=findViewById(R.id.etSearch);
        type=findViewById(R.id.spType);
        category=findViewById(R.id.spCategory);
        progress=findViewById(R.id.progressSearch);
        emptyText=findViewById(R.id.tvEmptySearch);
        adapter=new ItemAdapter(this,item->openItem(item.id));

        RecyclerView rv=findViewById(R.id.recyclerSearch);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        setupBottomNavigation(R.id.navSearch);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { load(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        type.setOnItemSelectedListener(listener);
        category.setOnItemSelectedListener(listener);

        findViewById(R.id.btnSearch).setOnClickListener(v->load());
    }

    private void load() {
        String t=type.getSelectedItem()==null?"ALL":type.getSelectedItem().toString();
        String cat=category.getSelectedItem()==null?"ALL":category.getSelectedItem().toString();
        progress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        RetrofitClient.api().getItems(session.authHeader(),search.getText().toString().trim(),t,cat)
                .enqueue(new Callback<ItemListResponse>() {
                    @Override public void onResponse(Call<ItemListResponse> c, Response<ItemListResponse> r) {
                        progress.setVisibility(View.GONE);
                        if(r.isSuccessful()&&r.body()!=null) {
                            adapter.setItems(r.body().items);
                            emptyText.setVisibility(r.body().items.isEmpty() ? View.VISIBLE : View.GONE);
                        } else toast("Search failed.");
                    }
                    @Override public void onFailure(Call<ItemListResponse> c, Throwable t){
                        progress.setVisibility(View.GONE);
                        toast("Connection failed.");
                    }
                });
    }
}
