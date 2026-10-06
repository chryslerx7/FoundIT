package com.example.foundit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foundit.adapter.ItemImageAdapter;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import com.example.foundit.util.ItemStatus;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import retrofit2.*;

public class ItemDetailActivity extends BaseActivity {
    int itemId;
    Item currentItem;
    TextView name, type, category, location, date, description, reporter, status;
    ImageView image;
    RecyclerView recyclerThumbnails;
    ItemImageAdapter thumbnailAdapter;
    Button match, resolve, edit, delete, messageReporter;
    boolean openingChat = false;
    View layoutContent, layoutLoading, layoutError;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_item_detail);
        applyWindowInsets(findViewById(R.id.rootDetailLayout));
        itemId = getIntent().getIntExtra("item_id", -1);

        image = findViewById(R.id.imgDetail);
        recyclerThumbnails = findViewById(R.id.recyclerDetailThumbnails);
        name = findViewById(R.id.tvDetailName);
        type = findViewById(R.id.tvDetailType);
        category = findViewById(R.id.tvDetailCategory);
        location = findViewById(R.id.tvDetailLocation);
        date = findViewById(R.id.tvDetailDate);
        description = findViewById(R.id.tvDetailDescription);
        reporter = findViewById(R.id.tvDetailReporter);
        status = findViewById(R.id.tvDetailStatus);
        match = findViewById(R.id.btnMatch);
        messageReporter = findViewById(R.id.btnMessageReporter);
        resolve = findViewById(R.id.btnResolve);
        edit = findViewById(R.id.btnEdit);
        delete = findViewById(R.id.btnDelete);
        
        layoutContent = findViewById(R.id.layoutContent);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutError = findViewById(R.id.layoutError);

        recyclerThumbnails.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        thumbnailAdapter = new ItemImageAdapter(this, false, new ItemImageAdapter.OnItemClickListener() {
            @Override public void onItemClick(Object item, int position) {
                if (item instanceof ItemImage) {
                    Glide.with(ItemDetailActivity.this).load(((ItemImage) item).image_url).into(image);
                }
            }
            @Override public void onRemoveClick(int position) {}
        });
        recyclerThumbnails.setAdapter(thumbnailAdapter);

        setupBottomNavigation(-1);

        match.setOnClickListener(v -> loadMatches());
        messageReporter.setOnClickListener(v -> messageReporter());
        resolve.setOnClickListener(v -> resolve());
        edit.setOnClickListener(v -> edit());
        delete.setOnClickListener(v -> delete());
        findViewById(R.id.btnRetry).setOnClickListener(v -> load());

        load();
    }

    private void load() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.INVISIBLE);
        layoutError.setVisibility(View.GONE);
        
        // Reset visibility of owner buttons to prevent stale state flicker
        resolve.setVisibility(View.GONE);
        edit.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        match.setVisibility(View.GONE);
        messageReporter.setVisibility(View.GONE);
        openingChat = false;

        RetrofitClient.api().getItem(session.authHeader(), itemId)
                .enqueue(new Callback<ItemResponse>() {
                    @Override public void onResponse(Call<ItemResponse> c, Response<ItemResponse> r) {
                        layoutLoading.setVisibility(View.GONE);
                        if(r.isSuccessful() && r.body() != null && r.body().item != null) {
                            bind(r.body().item);
                            layoutContent.setVisibility(View.VISIBLE);
                        } else {
                            layoutError.setVisibility(View.VISIBLE);
                            toast("Item not found.");
                        }
                    }
                    @Override public void onFailure(Call<ItemResponse> c, Throwable t) {
                        layoutLoading.setVisibility(View.GONE);
                        layoutError.setVisibility(View.VISIBLE);
                        toast("Connection failed.");
                    }
                });
    }

    private void bind(Item x) {
        currentItem = x;
        name.setText(x.item_name);
        // Primary badge reflects CURRENT state: RESOLVED wins over LOST/FOUND.
        ItemStatus.applyBadge(this, type, x);
        category.setText("Category: " + x.category);
        location.setText("Location: " + x.location);
        date.setText("Date: " + x.date);
        description.setText("Description: " + x.description);
        reporter.setText(x.user == null ? "" : ("Reported by: " + x.user.name));

        // Image display & thumbnails fallback
        if (x.images != null && !x.images.isEmpty()) {
            Glide.with(this).load(x.images.get(0).image_url).into(image);
            if (x.images.size() > 1) {
                recyclerThumbnails.setVisibility(View.VISIBLE);
                thumbnailAdapter.setItemImages(x.images);
            } else {
                recyclerThumbnails.setVisibility(View.GONE);
            }
        } else if (x.image_url != null && !x.image_url.isEmpty()) {
            Glide.with(this).load(x.image_url).into(image);
            recyclerThumbnails.setVisibility(View.GONE);
        } else {
            image.setImageResource(android.R.drawable.ic_menu_gallery);
            recyclerThumbnails.setVisibility(View.GONE);
        }

        // The primary badge above already communicates RESOLVED; keep the
        // secondary badge hidden to avoid contradictory "RESOLVED + LOST" display.
        status.setVisibility(View.GONE);

        boolean isOwner = x.user_id == session.userId();
        match.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        // Direct reporter messaging: visible only for active reports owned by someone else.
        messageReporter.setVisibility(!isOwner && "ACTIVE".equalsIgnoreCase(x.status) ? View.VISIBLE : View.GONE);
        resolve.setVisibility(isOwner && "ACTIVE".equalsIgnoreCase(x.status) ? View.VISIBLE : View.GONE);
        edit.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        delete.setVisibility(isOwner ? View.VISIBLE : View.GONE);
    }

    private void messageReporter() {
        if (currentItem == null || openingChat) return;
        if ("visitor".equalsIgnoreCase(session.role())) {
            toast("Please log in to contact the user.");
            return;
        }

        openingChat = true;
        messageReporter.setEnabled(false);
        messageReporter.setText("OPENING CHAT...");

        RetrofitClient.api().startDirectConversation(session.authHeader(), itemId)
                .enqueue(new Callback<ConversationResponse>() {
                    @Override public void onResponse(Call<ConversationResponse> c, Response<ConversationResponse> r) {
                        if (r.code() == 404) {
                            // Backend predates v1.1.3: verify the report still exists,
                            // then fall back to the legacy pair conversation.
                            verifyItemThenFallback();
                            return;
                        }
                        resetMessageButton();
                        if (r.isSuccessful() && r.body() != null && r.body().conversation != null) {
                            openChat(r.body().conversation);
                        } else if (r.code() == 422) {
                            toast("You cannot message your own report.");
                        } else if (r.code() == 403) {
                            toast("You are not allowed to contact this reporter.");
                        } else {
                            toast("Could not open chat.");
                        }
                    }
                    @Override public void onFailure(Call<ConversationResponse> c, Throwable t) {
                        resetMessageButton();
                        toast("Connection failed.");
                    }
                });
    }

    private void resetMessageButton() {
        openingChat = false;
        messageReporter.setEnabled(true);
        messageReporter.setText("MESSAGE REPORTER");
    }

    private void verifyItemThenFallback() {
        RetrofitClient.api().getItem(session.authHeader(), itemId)
                .enqueue(new Callback<ItemResponse>() {
                    @Override public void onResponse(Call<ItemResponse> c, Response<ItemResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().item != null) {
                            fallbackLegacyPairing();
                        } else {
                            resetMessageButton();
                            toast("This report is no longer available.");
                        }
                    }
                    @Override public void onFailure(Call<ItemResponse> c, Throwable t) {
                        resetMessageButton();
                        toast("Connection failed.");
                    }
                });
    }

    // Pre-v1.1.3 backend compatibility: pair the viewed report with the
    // viewer's own most recent active opposite-type report via the existing
    // pair-conversation endpoint (firstOrCreate reuses duplicates).
    private void fallbackLegacyPairing() {
        RetrofitClient.api().myReports(session.authHeader())
                .enqueue(new Callback<ItemListResponse>() {
                    @Override public void onResponse(Call<ItemListResponse> c, Response<ItemListResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().items != null && currentItem != null) {
                            String need = "LOST".equalsIgnoreCase(currentItem.type) ? "FOUND" : "LOST";
                            Item mine = null;
                            for (Item it : r.body().items) {
                                if (need.equalsIgnoreCase(it.type) && "ACTIVE".equalsIgnoreCase(it.status)) {
                                    mine = it;
                                    break;
                                }
                            }
                            if (mine != null) {
                                int lostId = "LOST".equalsIgnoreCase(currentItem.type) ? currentItem.id : mine.id;
                                int foundId = "FOUND".equalsIgnoreCase(currentItem.type) ? currentItem.id : mine.id;
                                RetrofitClient.api().startConversation(session.authHeader(), lostId, foundId)
                                        .enqueue(new Callback<ConversationResponse>() {
                                            @Override public void onResponse(Call<ConversationResponse> cc, Response<ConversationResponse> rr) {
                                                resetMessageButton();
                                                if (rr.isSuccessful() && rr.body() != null && rr.body().conversation != null) {
                                                    openChat(rr.body().conversation);
                                                } else {
                                                    toast("Could not open chat.");
                                                }
                                            }
                                            @Override public void onFailure(Call<ConversationResponse> cc, Throwable t) {
                                                resetMessageButton();
                                                toast("Connection failed.");
                                            }
                                        });
                                return;
                            }
                        }
                        resetMessageButton();
                        toast("Could not open chat. Please try again later.");
                    }
                    @Override public void onFailure(Call<ItemListResponse> c, Throwable t) {
                        resetMessageButton();
                        toast("Connection failed.");
                    }
                });
    }

    private void openChat(Conversation conversation) {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("conversation_id", conversation.id);
        String other = (currentItem != null && currentItem.user != null && currentItem.user.name != null)
                ? currentItem.user.name : "Reporter";
        i.putExtra("other_user_name", other);
        i.putExtra("item_name", currentItem != null ? currentItem.item_name : "");
        i.putExtra("chat_subtitle", currentItem != null ? currentItem.item_name : "");
        startActivity(i);
    }

    private void loadMatches() {
        RetrofitClient.api().matches(session.authHeader(), itemId)
                .enqueue(new Callback<ItemListResponse>() {
                    @Override public void onResponse(Call<ItemListResponse> c, Response<ItemListResponse> r) {
                        if (r.code() == 403) {
                            toast("Unauthorized: You can only view matches for your own reports.");
                            return;
                        }
                        if(!r.isSuccessful() || r.body() == null || r.body().items == null) { toast("No matches."); return; }
                        String[] names = new String[r.body().items.size()];
                        for(int i = 0; i < names.length; i++) names[i] = r.body().items.get(i).item_name + " (" + r.body().items.get(i).type + ")";
                        new MaterialAlertDialogBuilder(ItemDetailActivity.this)
                                .setTitle("Possible Match Found")
                                .setItems(names, (d, w) -> {
                                    Intent i = new Intent(ItemDetailActivity.this, PossibleMatchActivity.class);
                                    i.putExtra("my_item_id", itemId);
                                    i.putExtra("other_item_id", r.body().items.get(w).id);
                                    startActivity(i);
                                })
                                .setPositiveButton("Close", null)
                                .show();
                    }
                    @Override public void onFailure(Call<ItemListResponse> c, Throwable t) { toast("Could not find matches."); }
                });
    }

    private void resolve() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Mark as Resolved?")
                .setMessage("Are you sure this report has been successfully resolved?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Mark as Resolved", (d, w) -> {
                    RetrofitClient.api().resolveItem(session.authHeader(), itemId)
                            .enqueue(new Callback<ApiMessage>() {
                                @Override public void onResponse(Call<ApiMessage> c, Response<ApiMessage> r) {
                                    if (r.isSuccessful()) {
                                        toast("Report marked as resolved.");
                                        load();
                                    } else {
                                        if (r.code() == 403) toast("Unauthorized: You do not own this report.");
                                        else toast("Could not resolve.");
                                    }
                                }
                                @Override public void onFailure(Call<ApiMessage> c, Throwable t) {
                                    toast("Connection failed.");
                                }
                            });
                }).show();
    }

    private void delete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Report?")
                .setMessage("Are you sure you want to delete this report? This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    RetrofitClient.api().deleteItem(session.authHeader(), itemId)
                            .enqueue(new Callback<ApiMessage>() {
                                @Override public void onResponse(Call<ApiMessage> c, Response<ApiMessage> r) {
                                    if (r.isSuccessful()) {
                                        toast("Report deleted.");
                                        finish();
                                    } else {
                                        if (r.code() == 403) toast("Unauthorized: You do not own this report.");
                                        else toast("Could not delete.");
                                    }
                                }
                                @Override public void onFailure(Call<ApiMessage> c, Throwable t) {
                                    toast("Connection failed.");
                                }
                            });
                }).show();
    }

    private void edit() {
        Intent i = new Intent(this, ReportActivity.class);
        i.putExtra("edit_item_id", itemId);
        i.putExtra("name", currentItem.item_name);
        i.putExtra("category", currentItem.category);
        i.putExtra("description", currentItem.description);
        i.putExtra("location", currentItem.location);
        i.putExtra("date", currentItem.date);
        i.putExtra("type", currentItem.type);
        i.putExtra("contact", currentItem.contact);
        startActivity(i);
    }
}
