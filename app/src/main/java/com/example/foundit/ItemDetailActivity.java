package com.example.foundit;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import retrofit2.*;

public class ItemDetailActivity extends BaseActivity {
    int itemId;
    Item currentItem;
    TextView name, type, category, location, date, description, reporter, status;
    ImageView image;
    Button match, resolve, edit, delete;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_item_detail);
        itemId=getIntent().getIntExtra("item_id",-1);

        image=findViewById(R.id.imgDetail); name=findViewById(R.id.tvDetailName);
        type=findViewById(R.id.tvDetailType); category=findViewById(R.id.tvDetailCategory);
        location=findViewById(R.id.tvDetailLocation); date=findViewById(R.id.tvDetailDate);
        description=findViewById(R.id.tvDetailDescription); reporter=findViewById(R.id.tvDetailReporter);
        status=findViewById(R.id.tvDetailStatus);
        match=findViewById(R.id.btnMatch); resolve=findViewById(R.id.btnResolve);
        edit=findViewById(R.id.btnEdit); delete=findViewById(R.id.btnDelete);

        setupBottomNavigation(-1);

        match.setOnClickListener(v->loadMatches());
        resolve.setOnClickListener(v->resolve());
        edit.setOnClickListener(v->edit());
        delete.setOnClickListener(v->delete());

        load();
    }

    private void load() {
        RetrofitClient.api().getItem(session.authHeader(),itemId)
                .enqueue(new Callback<ItemResponse>() {
                    @Override public void onResponse(Call<ItemResponse> c,Response<ItemResponse> r) {
                        if(r.isSuccessful()&&r.body()!=null&&r.body().item!=null) bind(r.body().item);
                        else toast("Item not found.");
                    }
                    @Override public void onFailure(Call<ItemResponse> c,Throwable t){toast("Connection failed.");}
                });
    }

    private void bind(Item x) {
        currentItem = x;
        name.setText(x.item_name);
        type.setText(x.type);
        type.setBackgroundColor("FOUND".equalsIgnoreCase(x.type)?Color.rgb(46,173,103):Color.rgb(227,74,74));
        category.setText("Category: "+x.category);
        location.setText("Location: "+x.location);
        date.setText("Date: "+x.date);
        description.setText("Description: "+x.description);
        reporter.setText(x.user==null?"":("Reported by: "+x.user.name));
        if(x.image_url!=null&&!x.image_url.isEmpty()) Glide.with(this).load(x.image_url).into(image);

        if ("RESOLVED".equalsIgnoreCase(x.status)) {
            status.setVisibility(View.VISIBLE);
        } else {
            status.setVisibility(View.GONE);
        }

        boolean isOwner = x.user_id == session.userId();
        resolve.setVisibility(isOwner && "ACTIVE".equalsIgnoreCase(x.status) ? View.VISIBLE : View.GONE);
        edit.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        delete.setVisibility(isOwner ? View.VISIBLE : View.GONE);
    }

    private void loadMatches() {
        RetrofitClient.api().matches(session.authHeader(),itemId)
                .enqueue(new Callback<ItemListResponse>() {
                    @Override public void onResponse(Call<ItemListResponse> c,Response<ItemListResponse> r) {
                        if(!r.isSuccessful()||r.body()==null||r.body().items==null) {toast("No matches.");return;}
                        String[] names=new String[r.body().items.size()];
                        for(int i=0;i<names.length;i++) names[i]=r.body().items.get(i).item_name+" ("+r.body().items.get(i).type+")";
                        new AlertDialog.Builder(ItemDetailActivity.this).setTitle("Possible Match Found")
                                .setItems(names,(d,w)-> {
                                    Intent i = new Intent(ItemDetailActivity.this, PossibleMatchActivity.class);
                                    i.putExtra("my_item_id", itemId);
                                    i.putExtra("other_item_id", r.body().items.get(w).id);
                                    startActivity(i);
                                }).setPositiveButton("Close",null).show();
                    }
                    @Override public void onFailure(Call<ItemListResponse> c,Throwable t){toast("Could not find matches.");}
                });
    }

    private void resolve() {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Resolved?")
                .setMessage("Are you sure you want to mark this item as resolved?\n\nYou will no longer treat this report as an active Lost/Found report.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes, Mark as Resolved", (d, w) -> {
                    RetrofitClient.api().resolveItem(session.authHeader(), itemId)
                            .enqueue(new Callback<ApiMessage>() {
                                @Override public void onResponse(Call<ApiMessage> c, Response<ApiMessage> r) {
                                    if (r.isSuccessful()) {
                                        toast("Report marked as resolved.");
                                        load();
                                    } else toast("Could not resolve.");
                                }
                                @Override public void onFailure(Call<ApiMessage> c, Throwable t) {
                                    toast("Connection failed.");
                                }
                            });
                }).show();
    }

    private void delete() {
        new AlertDialog.Builder(this).setTitle("Delete Report")
                .setMessage("Are you sure you want to delete this report? This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    RetrofitClient.api().deleteItem(session.authHeader(), itemId)
                            .enqueue(new Callback<ApiMessage>() {
                                @Override public void onResponse(Call<ApiMessage> c, Response<ApiMessage> r) {
                                    if (r.isSuccessful()) {
                                        toast("Report deleted.");
                                        finish();
                                    } else toast("Could not delete.");
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
