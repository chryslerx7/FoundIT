package com.example.foundit;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import retrofit2.*;

public class PossibleMatchActivity extends BaseActivity {
    Item myItem, otherItem;
    View layoutMy, layoutOther;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_possible_match);

        layoutMy = findViewById(R.id.layoutMyItem);
        layoutOther = findViewById(R.id.layoutOtherItem);

        int myId = getIntent().getIntExtra("my_item_id", -1);
        int otherId = getIntent().getIntExtra("other_item_id", -1);

        loadItem(myId, true);
        loadItem(otherId, false);

        findViewById(R.id.btnMessageUser).setOnClickListener(v -> startChat());
        setupBottomNavigation(-1);
    }

    private void loadItem(int id, boolean isMy) {
        RetrofitClient.api().getItem(session.authHeader(), id).enqueue(new Callback<ItemResponse>() {
            @Override public void onResponse(Call<ItemResponse> c, Response<ItemResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().item != null) {
                    if (isMy) {
                        myItem = r.body().item;
                        bind(layoutMy, myItem);
                    } else {
                        otherItem = r.body().item;
                        bind(layoutOther, otherItem);
                    }
                }
            }
            @Override public void onFailure(Call<ItemResponse> c, Throwable t) {}
        });
    }

    private void bind(View v, Item x) {
        TextView name = v.findViewById(R.id.tvItemName);
        TextView loc = v.findViewById(R.id.tvLocation);
        TextView date = v.findViewById(R.id.tvDate);
        TextView type = v.findViewById(R.id.tvType);
        ImageView img = v.findViewById(R.id.imgItem);

        name.setText(x.item_name);
        loc.setText(x.location);
        date.setText(x.date);
        type.setText(x.type);
        type.setBackgroundColor("FOUND".equalsIgnoreCase(x.type) ? Color.rgb(46, 173, 103) : Color.rgb(227, 74, 74));
        if (x.image_url != null && !x.image_url.isEmpty()) Glide.with(this).load(x.image_url).into(img);
    }

    private void startChat() {
        if (myItem == null || otherItem == null) return;
        if ("visitor".equalsIgnoreCase(session.role())) {
            toast("Please log in to contact the user.");
            return;
        }

        int lostId = "LOST".equalsIgnoreCase(myItem.type) ? myItem.id : otherItem.id;
        int foundId = "FOUND".equalsIgnoreCase(myItem.type) ? myItem.id : otherItem.id;

        RetrofitClient.api().startConversation(session.authHeader(), lostId, foundId).enqueue(new Callback<ConversationResponse>() {
            @Override public void onResponse(Call<ConversationResponse> c, Response<ConversationResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    Intent i = new Intent(PossibleMatchActivity.this, ChatActivity.class);
                    i.putExtra("conversation_id", r.body().conversation.id);
                    i.putExtra("other_user_name", "LOST".equalsIgnoreCase(myItem.type) ? otherItem.user.name : myItem.user.name);
                    i.putExtra("item_name", myItem.item_name);
                    startActivity(i);
                } else toast("Could not start chat.");
            }
            @Override public void onFailure(Call<ConversationResponse> c, Throwable t) { toast("Connection failed."); }
        });
    }
}
