package com.example.foundit;

import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foundit.adapter.MessageAdapter;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import retrofit2.*;

public class ChatActivity extends BaseActivity {
    public static ChatActivity activeInstance = null;
    public int conversationId;
    int pendingItemId = -1;
    View layoutPendingCard;
    ImageView ivPendingImage;
    TextView tvPendingBadge, tvPendingName;
    RecyclerView recycler;
    MessageAdapter adapter;
    List<Message> messages = new ArrayList<>();
    EditText etMessage;
    Handler handler = new Handler();
    String cachedFcmToken = null;
    Runnable refreshRunnable = new Runnable() {
        @Override public void run() {
            loadMessages();
            sendPresenceHeartbeat();
            handler.postDelayed(this, 3000);
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_chat);
        applyChatWindowInsets(findViewById(R.id.rootChatLayout));

        conversationId = getIntent().getIntExtra("conversation_id", -1);
        pendingItemId = getIntent().getIntExtra("item_id", -1);
        String otherUser = getIntent().getStringExtra("other_user_name");
        String item = getIntent().getStringExtra("item_name");

        ((TextView)findViewById(R.id.tvChatWith)).setText(otherUser);
        String subtitle = getIntent().getStringExtra("chat_subtitle");
        if (subtitle == null) subtitle = "Possible Match: " + item;
        ((TextView)findViewById(R.id.tvChatMatch)).setText(subtitle);

        recycler = findViewById(R.id.recyclerMessages);
        etMessage = findViewById(R.id.etMessage);
        layoutPendingCard = findViewById(R.id.layoutPendingCard);
        ivPendingImage = findViewById(R.id.ivPendingImage);
        tvPendingBadge = findViewById(R.id.tvPendingBadge);
        tvPendingName = findViewById(R.id.tvPendingName);

        findViewById(R.id.btnRemovePending).setOnClickListener(v -> {
            pendingItemId = -1;
            layoutPendingCard.setVisibility(View.GONE);
        });

        if (pendingItemId != -1) {
            loadPendingItemPreview(pendingItemId);
        }

        adapter = new MessageAdapter(messages, session.userId(), (msg, pos) -> confirmDeleteMessage(msg, pos));
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void loadPendingItemPreview(int itemId) {
        RetrofitClient.api().getItem(session.authHeader(), itemId).enqueue(new Callback<ItemResponse>() {
            @Override public void onResponse(Call<ItemResponse> c, Response<ItemResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().item != null) {
                    Item item = r.body().item;
                    tvPendingName.setText(item.item_name);
                    boolean isLost = "LOST".equalsIgnoreCase(item.type);
                    tvPendingBadge.setText(isLost ? "LOST" : "FOUND");
                    tvPendingBadge.setBackgroundColor(ContextCompat.getColor(ChatActivity.this,
                            isLost ? R.color.foundit_lost : R.color.foundit_found));
                    if (item.image_url != null && !item.image_url.isEmpty()) {
                        Glide.with(ChatActivity.this).load(item.image_url).into(ivPendingImage);
                    } else {
                        ivPendingImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                    layoutPendingCard.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onFailure(Call<ItemResponse> c, Throwable t) {}
        });
    }

    @Override protected void onResume() {
        super.onResume();
        activeInstance = this;
        sendPresenceHeartbeat();
        handler.postDelayed(refreshRunnable, 3000);
    }

    private void sendPresenceHeartbeat() {
        if (cachedFcmToken != null) {
            sendPresenceToServer(cachedFcmToken);
        } else {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
                if (token != null && !token.isEmpty()) {
                    cachedFcmToken = token;
                    sendPresenceToServer(token);
                }
            });
        }
    }

    private void sendPresenceToServer(String token) {
        RetrofitClient.api().sendPresence(session.authHeader(), conversationId, token)
                .enqueue(new Callback<ApiMessage>() {
                    @Override public void onResponse(Call<ApiMessage> c, Response<ApiMessage> r) {}
                    @Override public void onFailure(Call<ApiMessage> c, Throwable t) {}
                });
    }

    @Override protected void onPause() {
        super.onPause();
        if (activeInstance == this) {
            activeInstance = null;
        }
        handler.removeCallbacks(refreshRunnable);
    }

    private void loadMessages() {
        RetrofitClient.api().getMessages(session.authHeader(), conversationId).enqueue(new Callback<MessageListResponse>() {
            @Override public void onResponse(Call<MessageListResponse> c, Response<MessageListResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    int oldSize = messages.size();
                    messages.clear();
                    messages.addAll(r.body().messages);
                    adapter.notifyDataSetChanged();
                    if (messages.size() > oldSize) recycler.scrollToPosition(messages.size() - 1);
                } else if (r.code() == 403 || r.code() == 404 || r.code() == 410) {
                    toast("Conversation no longer active. Start a new conversation to contact this user.");
                    finish();
                }
            }
            @Override public void onFailure(Call<MessageListResponse> c, Throwable t) {}
        });
    }

    private void sendMessage() {
        String msg = etMessage.getText().toString().trim();
        if (pendingItemId == -1 && msg.isEmpty()) return;

        etMessage.setText("");
        if (pendingItemId != -1) {
            RetrofitClient.api().sendMessage(session.authHeader(), conversationId, msg.isEmpty() ? "Shared an item report." : msg, "item_share", pendingItemId)
                    .enqueue(new Callback<MessageResponse>() {
                        @Override public void onResponse(Call<MessageResponse> c, Response<MessageResponse> r) {
                            if (r.isSuccessful() && r.body() != null) {
                                pendingItemId = -1;
                                if (layoutPendingCard != null) layoutPendingCard.setVisibility(View.GONE);
                                loadMessages();
                            } else {
                                toast("Failed to share report.");
                            }
                        }
                        @Override public void onFailure(Call<MessageResponse> c, Throwable t) {
                            toast("Connection failed.");
                        }
                    });
        } else {
            RetrofitClient.api().sendMessage(session.authHeader(), conversationId, msg).enqueue(new Callback<MessageResponse>() {
                @Override public void onResponse(Call<MessageResponse> c, Response<MessageResponse> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        loadMessages();
                    } else if (r.code() == 410 || r.code() == 403 || r.code() == 404 || r.code() == 422) {
                        toast("Conversation no longer active. Start a new conversation to contact this user.");
                    } else toast("Failed to send.");
                }
                @Override public void onFailure(Call<MessageResponse> c, Throwable t) { toast("Connection failed."); }
            });
        }
    }

    private void confirmDeleteMessage(Message msg, int pos) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete message?")
                .setMessage("Are you sure you want to delete this message?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> deleteMessage(msg, pos))
                .show();
    }

    private void deleteMessage(Message msg, int pos) {
        RetrofitClient.api().deleteMessage(session.authHeader(), msg.id).enqueue(new Callback<ApiMessage>() {
            @Override public void onResponse(Call<ApiMessage> c, Response<ApiMessage> r) {
                if (r.isSuccessful()) {
                    toast("Message deleted.");
                    if (pos >= 0 && pos < messages.size()) {
                        messages.remove(pos);
                        adapter.notifyItemRemoved(pos);
                    } else {
                        loadMessages();
                    }
                } else {
                    if (r.code() == 403) {
                        toast("Unauthorized to delete this message.");
                    } else {
                        toast("Failed to delete message.");
                    }
                }
            }

            @Override public void onFailure(Call<ApiMessage> c, Throwable t) {
                toast("Connection failed.");
            }
        });
    }

    private void applyChatWindowInsets(View root) {
        if (root == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            View header = findViewById(R.id.chatHeader);
            if (header != null) {
                header.setPadding(header.getPaddingLeft(), systemBars.top, header.getPaddingRight(), header.getPaddingBottom());
            }

            View bottomContainer = findViewById(R.id.bottomChatContainer);
            if (bottomContainer != null) {
                int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
                int density = (int) getResources().getDisplayMetrics().density;
                int basePadding = 12 * density;
                bottomContainer.setPadding(basePadding, basePadding, basePadding, bottomPadding > 0 ? bottomPadding : basePadding);
            }

            return insets;
        });
    }
}
