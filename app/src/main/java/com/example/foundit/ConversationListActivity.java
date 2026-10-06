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
            if (c.directItem != null) {
                String other = (c.directItem.user != null && c.directItem.user.name != null)
                        ? c.directItem.user.name : "Reporter";
                i.putExtra("other_user_name", other);
                i.putExtra("item_name", c.directItem.item_name != null ? c.directItem.item_name : "");
                i.putExtra("chat_subtitle", c.directItem.item_name != null ? c.directItem.item_name : "");
            } else {
                boolean isOwnerOfLost = c.lostItem != null && c.lostItem.user_id == session.userId();
                com.example.foundit.model.Item otherItem = isOwnerOfLost ? c.foundItem : c.lostItem;
                String other = (otherItem != null && otherItem.user != null && otherItem.user.name != null)
                        ? otherItem.user.name : "Reporter";
                i.putExtra("other_user_name", other);
                i.putExtra("item_name", (c.lostItem != null && c.lostItem.item_name != null) ? c.lostItem.item_name : "");
            }
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
