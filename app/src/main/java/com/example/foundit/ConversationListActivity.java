package com.example.foundit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foundit.adapter.ConversationAdapter;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import retrofit2.*;

public class ConversationListActivity extends BaseActivity {
    RecyclerView recycler;
    ConversationAdapter adapter;
    List<Conversation> list = new ArrayList<>();
    TextView emptyView;
    boolean deleting = false;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_conversation_list);
        applyWindowInsets(findViewById(R.id.rootConversationListLayout));

        recycler = findViewById(R.id.recyclerConversations);
        emptyView = findViewById(R.id.tvEmptyConversations);
        adapter = new ConversationAdapter(list, session.userId(), c -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("conversation_id", c.id);
            if (c.directItem != null) {
                com.example.foundit.model.User otherUser = null;
                if (c.userOne != null && c.userTwo != null) {
                    otherUser = (c.userOne.id == session.userId()) ? c.userTwo : c.userOne;
                }
                String other;
                if (otherUser != null && otherUser.name != null) {
                    other = otherUser.name;
                } else {
                    other = (c.directItem.user != null && c.directItem.user.name != null)
                            ? c.directItem.user.name : "Reporter";
                }
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
        adapter.setOnConversationLongClickListener((c, pos) -> confirmDeleteConversation(c, pos));

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        setupBottomNavigation(-1);
        load();
    }

    private void updateEmptyState() {
        boolean empty = list.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void confirmDeleteConversation(Conversation c, int position) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Conversation?")
                .setMessage("This conversation will be removed from your Messages list. The other participant will still be able to see their conversation.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> deleteConversation(c, position, (AlertDialog) d))
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getColor(R.color.foundit_lost)));
        dialog.show();
    }

    private void deleteConversation(Conversation c, int position, AlertDialog dialog) {
        if (deleting) return;
        deleting = true;
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        RetrofitClient.api().deleteConversation(session.authHeader(), c.id).enqueue(new Callback<ApiMessage>() {
            @Override public void onResponse(Call<ApiMessage> c2, Response<ApiMessage> r) {
                deleting = false;
                if (r.isSuccessful()) {
                    dialog.dismiss();
                    int index = list.indexOf(c);
                    if (index < 0) index = position;
                    if (index >= 0 && index < list.size()) {
                        list.remove(index);
                        adapter.notifyItemRemoved(index);
                    } else {
                        load();
                    }
                    updateEmptyState();
                    toast("Conversation deleted.");
                } else {
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    if (r.code() == 403) toast("Unauthorized to delete this conversation.");
                    else toast("Could not delete conversation.");
                }
            }
            @Override public void onFailure(Call<ApiMessage> c2, Throwable t) {
                deleting = false;
                if (dialog.isShowing()) {
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
                toast("Connection failed.");
            }
        });
    }

    private void load() {
        RetrofitClient.api().getConversations(session.authHeader()).enqueue(new Callback<ConversationListResponse>() {
            @Override public void onResponse(Call<ConversationListResponse> c, Response<ConversationListResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    list.clear();
                    list.addAll(r.body().conversations);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                }
            }
            @Override public void onFailure(Call<ConversationListResponse> c, Throwable t) { toast("Connection failed."); }
        });
    }
}
