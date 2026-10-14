package com.example.foundit;

import android.content.*;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import com.example.foundit.util.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    protected SessionManager session;
    private GestureDetector gestureDetector;
    private View bottomNav;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        setupDoubleTap();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }

    protected void applyWindowInsets(View root) {
        if (root == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            
            View bottomNav = findViewById(R.id.bottomNavContainer);
            if (bottomNav != null) {
                int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
                bottomNav.setPadding(0, 0, 0, bottomPadding);
            }
            
            return insets;
        });
    }

    protected void setupBottomNavigation(int activeId) {
        bottomNav = findViewById(R.id.bottomNavContainer);
        if (bottomNav == null) return;

        ImageButton home = findViewById(R.id.navHome);
        ImageButton search = findViewById(R.id.navSearch);
        ImageButton messages = findViewById(R.id.navMessages);
        FloatingActionButton add = findViewById(R.id.navAdd);
        ImageButton reports = findViewById(R.id.navReports);
        ImageButton profile = findViewById(R.id.navProfile);

        home.setOnClickListener(v -> navigate(MainActivity.class));
        search.setOnClickListener(v -> navigate(SearchActivity.class));
        messages.setOnClickListener(v -> navigate(ConversationListActivity.class));
        add.setOnClickListener(v -> chooseReport());
        reports.setOnClickListener(v -> navigate(MyReportsActivity.class));
        profile.setOnClickListener(v -> navigate(ProfileActivity.class));

        View navBarCard = findViewById(R.id.navBarCard);
        if (navBarCard != null) navBarCard.setZ(0f);
        add.bringToFront();

        int blue = ContextCompat.getColor(this, R.color.foundit_primary);
        if (activeId == R.id.navHome) home.setColorFilter(blue);
        else if (activeId == R.id.navSearch) search.setColorFilter(blue);
        else if (activeId == R.id.navMessages) messages.setColorFilter(blue);
        else if (activeId == R.id.navReports) reports.setColorFilter(blue);
        else if (activeId == R.id.navProfile) profile.setColorFilter(blue);
    }

    private void navigate(Class<?> cls) {
        if (this.getClass() == cls) return;
        Intent i = new Intent(this, cls);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }

    protected void chooseReport() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_choose_report, null);
        sheet.setContentView(sheetView);

        sheetView.findViewById(R.id.btnOptionLost).setOnClickListener(v -> {
            sheet.dismiss();
            Intent i = new Intent(this, ReportActivity.class);
            i.putExtra("type", "LOST");
            startActivity(i);
        });

        sheetView.findViewById(R.id.btnOptionFound).setOnClickListener(v -> {
            sheet.dismiss();
            Intent i = new Intent(this, ReportActivity.class);
            i.putExtra("type", "FOUND");
            startActivity(i);
        });

        sheet.show();
    }

    private void setupDoubleTap() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDoubleTap(MotionEvent e) {
                if (bottomNav != null) {
                    bottomNav.setVisibility(bottomNav.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                    return true;
                }
                return false;
            }
        });
    }

    @Override public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    protected boolean requireLogin() {
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finishAffinity();
            return false;
        }
        return true;
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void openItem(int id) {
        Intent i = new Intent(this, ItemDetailActivity.class);
        i.putExtra("item_id", id);
        startActivity(i);
    }

    protected void registerFcmToken() {
        if (session == null || !session.isLoggedIn()) return;
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            if (token != null && !token.isEmpty()) {
                com.example.foundit.api.RetrofitClient.api().registerDeviceToken(session.authHeader(), new com.example.foundit.model.DeviceTokenRequest(token))
                        .enqueue(new retrofit2.Callback<com.example.foundit.model.ApiMessage>() {
                            @Override public void onResponse(retrofit2.Call<com.example.foundit.model.ApiMessage> c, retrofit2.Response<com.example.foundit.model.ApiMessage> r) {}
                            @Override public void onFailure(retrofit2.Call<com.example.foundit.model.ApiMessage> c, Throwable t) {}
                        });
            }
        });
    }

    public void shareItemInChat(com.example.foundit.model.Item item) {
        if (item == null) return;
        com.example.foundit.api.RetrofitClient.api().getConversations(session.authHeader())
                .enqueue(new retrofit2.Callback<com.example.foundit.model.ConversationListResponse>() {
                    @Override public void onResponse(retrofit2.Call<com.example.foundit.model.ConversationListResponse> c, retrofit2.Response<com.example.foundit.model.ConversationListResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().conversations != null) {
                            List<com.example.foundit.model.Conversation> convs = r.body().conversations;
                            if (convs.isEmpty()) {
                                new MaterialAlertDialogBuilder(BaseActivity.this)
                                        .setTitle("No Conversations")
                                        .setMessage("You have no active conversations. Start a conversation from an item report first.")
                                        .setPositiveButton("OK", null)
                                        .show();
                                return;
                            }

                            String[] names = new String[convs.size()];
                            for (int i = 0; i < convs.size(); i++) {
                                com.example.foundit.model.Conversation conv = convs.get(i);
                                String title = "Chat #" + conv.id;
                                if (conv.directItem != null) {
                                    title = "Chat about " + conv.directItem.item_name;
                                } else if (conv.lostItem != null && conv.foundItem != null) {
                                    title = "Match: " + conv.lostItem.item_name + " & " + conv.foundItem.item_name;
                                }
                                names[i] = title;
                            }

                            new MaterialAlertDialogBuilder(BaseActivity.this)
                                    .setTitle("Choose Conversation")
                                    .setItems(names, (dialog, which) -> {
                                        com.example.foundit.model.Conversation selectedConv = convs.get(which);
                                        confirmAndSendShare(selectedConv, item);
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        } else {
                            toast("Could not load conversations.");
                        }
                    }
                    @Override public void onFailure(retrofit2.Call<com.example.foundit.model.ConversationListResponse> c, Throwable t) {
                        toast("Connection failed.");
                    }
                });
    }

    private void confirmAndSendShare(com.example.foundit.model.Conversation conversation, com.example.foundit.model.Item item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Sharing")
                .setMessage("Share report for \"" + item.item_name + "\" in this conversation?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Share", (d, w) -> {
                    com.example.foundit.api.RetrofitClient.api().sendMessage(session.authHeader(), conversation.id, "Shared an item report.", "item_share", item.id)
                            .enqueue(new retrofit2.Callback<com.example.foundit.model.MessageResponse>() {
                                @Override public void onResponse(retrofit2.Call<com.example.foundit.model.MessageResponse> c, retrofit2.Response<com.example.foundit.model.MessageResponse> r) {
                                    if (r.isSuccessful()) {
                                        toast("Item report shared in chat!");
                                        Intent i = new Intent(BaseActivity.this, ChatActivity.class);
                                        i.putExtra("conversation_id", conversation.id);
                                        startActivity(i);
                                    } else {
                                        toast("Failed to share report.");
                                    }
                                }
                                @Override public void onFailure(retrofit2.Call<com.example.foundit.model.MessageResponse> c, Throwable t) {
                                    toast("Connection failed.");
                                }
                            });
                })
                .show();
    }
}
