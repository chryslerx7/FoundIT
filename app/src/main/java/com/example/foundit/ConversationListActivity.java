package com.example.foundit;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foundit.adapter.ConversationAdapter;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import java.util.ArrayList;
import java.util.List;
import retrofit2.*;

public class ConversationListActivity extends BaseActivity {
    RecyclerView recycler;
    ConversationAdapter adapter;
    List<Conversation> list = new ArrayList<>();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_conversation_list);
        applyWindowInsets(findViewById(R.id.rootConversationListLayout));

        recycler = findViewById(R.id.recyclerConversations);
        adapter = new ConversationAdapter(list, session.userId(), c -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("conversation_id", c.id);
            boolean isOwnerOfLost = c.lostItem.user_id == session.userId();
            i.putExtra("other_user_name", isOwnerOfLost ? c.foundItem.user.name : c.lostItem.user.name);
            i.putExtra("item_name", c.lostItem.item_name);
            startActivity(i);
        });

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        setupBottomNavigation(-1);
        load();
    }

    private void load() {
        RetrofitClient.api().getConversations(session.authHeader()).enqueue(new Callback<ConversationListResponse>() {
            @Override public void onResponse(Call<ConversationListResponse> c, Response<ConversationListResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    list.clear();
                    list.addAll(r.body().conversations);
                    adapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<ConversationListResponse> c, Throwable t) { toast("Connection failed."); }
        });
    }
}
