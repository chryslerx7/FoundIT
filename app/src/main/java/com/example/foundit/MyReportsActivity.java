package com.example.foundit;

import android.os.Bundle;
import androidx.recyclerview.widget.*;
import com.example.foundit.adapter.ItemAdapter;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import retrofit2.*;

public class MyReportsActivity extends BaseActivity {
    ItemAdapter adapter;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_my_reports);
        adapter=new ItemAdapter(this,item->openItem(item.id));
        RecyclerView rv=findViewById(R.id.recyclerMyReports);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        setupBottomNavigation(R.id.navReports);
    }

    @Override protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        RetrofitClient.api().myReports(session.authHeader()).enqueue(new Callback<ItemListResponse>() {
            @Override public void onResponse(Call<ItemListResponse> c,Response<ItemListResponse> r) {
                if(r.isSuccessful()&&r.body()!=null) adapter.setItems(r.body().items);
                else toast("Could not load reports.");
            }
            @Override public void onFailure(Call<ItemListResponse> c,Throwable t){toast("Connection failed.");}
        });
    }
}
