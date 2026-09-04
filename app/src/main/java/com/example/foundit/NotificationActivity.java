package com.example.foundit;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.*;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.NotificationItem;
import java.util.*;
import retrofit2.*;

public class NotificationActivity extends BaseActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_notification);
        applyWindowInsets(findViewById(R.id.rootNotificationLayout));
        RecyclerView rv=findViewById(R.id.recyclerNotifications);
        rv.setLayoutManager(new LinearLayoutManager(this));

        setupBottomNavigation(-1);

        load(rv);
    }
    private void load(RecyclerView rv) {
        RetrofitClient.api().notifications(session.authHeader()).enqueue(new Callback<List<NotificationItem>>() {
            @Override public void onResponse(Call<List<NotificationItem>> c,Response<List<NotificationItem>> r) {
                if(r.isSuccessful()&&r.body()!=null) {
                    rv.setAdapter(new NAdapter(r.body()));
                }
            }
            @Override public void onFailure(Call<List<NotificationItem>> c,Throwable t){toast("Could not load notifications.");}
        });
    }
    class NAdapter extends RecyclerView.Adapter<NAdapter.H> {
        List<NotificationItem> list;
        NAdapter(List<NotificationItem> l){list=l;}
        @Override public H onCreateViewHolder(ViewGroup p,int v){return new H(LayoutInflater.from(NotificationActivity.this).inflate(R.layout.notification_card,p,false));}
        @Override public void onBindViewHolder(H h,int pos){NotificationItem n=list.get(pos);h.title.setText(n.title);h.msg.setText(n.message);h.itemView.setOnClickListener(v->{
            RetrofitClient.api().markNotificationRead(session.authHeader(),n.id).enqueue(new Callback<com.example.foundit.model.ApiMessage>(){
                public void onResponse(Call<com.example.foundit.model.ApiMessage> c,Response<com.example.foundit.model.ApiMessage> r){}
                public void onFailure(Call<com.example.foundit.model.ApiMessage> c,Throwable t){}
            });
        });}
        @Override public int getItemCount(){return list.size();}
        class H extends RecyclerView.ViewHolder{TextView title,msg;H(View v){super(v);title=v.findViewById(R.id.tvNotificationTitle);msg=v.findViewById(R.id.tvNotificationMessage);}}
    }
}
