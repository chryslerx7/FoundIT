package com.example.foundit.adapter;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foundit.R;
import com.example.foundit.model.Item;
import com.example.foundit.util.ItemStatus;
import java.util.*;
import android.content.Context;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.Holder> {
    public interface OnItemClick { void onClick(Item item); }

    private final Context context;
    private List<Item> items = new ArrayList<>();
    private final OnItemClick listener;

    public ItemAdapter(Context context, OnItemClick listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<Item> items) {
        this.items = items == null ? new ArrayList<>() : items;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new Holder(v);
    }

    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        Item item = items.get(position);
        h.name.setText(item.item_name);
        h.category.setText(item.category == null ? "" : item.category);
        h.location.setText(item.location == null ? "" : "📍 " + item.location);
        h.date.setText(item.date == null ? "" : "📅 " + item.date);
        h.type.setText(ItemStatus.displayLabel(item));
        // Single source of truth: RESOLVED wins over LOST/FOUND (covers Home, Search, My Reports).
        ItemStatus.applyBadge(context, h.type, item);

        if (item.image_url != null && !item.image_url.isEmpty()) {
            Glide.with(context).load(item.image_url).centerCrop().into(h.image);
        } else {
            h.image.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, category, type, location, date;
        Holder(View v) {
            super(v);
            image = v.findViewById(R.id.imgItem);
            name = v.findViewById(R.id.tvItemName);
            category = v.findViewById(R.id.tvCategory);
            type = v.findViewById(R.id.tvType);
            location = v.findViewById(R.id.tvLocation);
            date = v.findViewById(R.id.tvDate);
        }
    }
}
