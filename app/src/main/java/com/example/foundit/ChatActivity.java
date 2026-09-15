package com.example.foundit;

import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foundit.adapter.MessageAdapter;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import java.util.ArrayList;
import java.util.List;
import retrofit2.*;

public class ChatActivity extends BaseActivity {
    int conversationId;
    RecyclerView recycler;
    MessageAdapter adapter;
    List<Message> messages = new ArrayList<>();
    EditText etMessage;
    Handler handler = new Handler();
    Runnable refreshRunnable = new Runnable() {
        @Override public void run() {
            loadMessages();
            handler.postDelayed(this, 3000);
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_chat);
        applyChatWindowInsets(findViewById(R.id.rootChatLayout));

        conversationId = getIntent().getIntExtra("conversation_id", -1);
        String otherUser = getIntent().getStringExtra("other_user_name");
        String item = getIntent().getStringExtra("item_name");

        ((TextView)findViewById(R.id.tvChatWith)).setText(otherUser);
        ((TextView)findViewById(R.id.tvChatMatch)).setText("Possible Match: " + item);

        recycler = findViewById(R.id.recyclerMessages);
        etMessage = findViewById(R.id.etMessage);

        adapter = new MessageAdapter(messages, session.userId());
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    @Override protected void onResume() {
        super.onResume();
        handler.postDelayed(refreshRunnable, 3000);
    }

    @Override protected void onPause() {
        super.onPause();
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
                }
            }
            @Override public void onFailure(Call<MessageListResponse> c, Throwable t) {}
        });
    }

    private void sendMessage() {
        String msg = etMessage.getText().toString().trim();
        if (msg.isEmpty()) return;

        etMessage.setText("");
        RetrofitClient.api().sendMessage(session.authHeader(), conversationId, msg).enqueue(new Callback<MessageResponse>() {
            @Override public void onResponse(Call<MessageResponse> c, Response<MessageResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    loadMessages();
                } else toast("Failed to send.");
            }
            @Override public void onFailure(Call<MessageResponse> c, Throwable t) { toast("Connection failed."); }
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
                // Keep the original 12dp padding (converted to pixels) and add the inset
                int density = (int) getResources().getDisplayMetrics().density;
                int basePadding = 12 * density;
                bottomContainer.setPadding(basePadding, basePadding, basePadding, bottomPadding > 0 ? bottomPadding : basePadding);
            }

            return insets;
        });
    }
}
