package com.example.foundit.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foundit.R;
import com.example.foundit.model.ItemImage;
import java.util.ArrayList;
import java.util.List;

public class ItemImageAdapter extends RecyclerView.Adapter<ItemImageAdapter.ViewHolder> {
    private final Context context;
    private final List<Object> items = new ArrayList<>(); // Can hold Uri or ItemImage
    private final OnItemClickListener listener;
    private final boolean showRemoveButton;

    public interface OnItemClickListener {
        void onItemClick(Object item, int position);
        void onRemoveClick(int position);
    }

    public ItemImageAdapter(Context context, boolean showRemoveButton, OnItemClickListener listener) {
        this.context = context;
        this.showRemoveButton = showRemoveButton;
        this.listener = listener;
    }

    public void setUriItems(List<Uri> uris) {
        items.clear();
        if (uris != null) items.addAll(uris);
        notifyDataSetChanged();
    }

    public void setItemImages(List<ItemImage> imgList) {
        items.clear();
        if (imgList != null) items.addAll(imgList);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_thumbnail, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object obj = items.get(position);
        if (obj instanceof Uri) {
            Glide.with(context).load((Uri) obj).into(holder.image);
        } else if (obj instanceof ItemImage) {
            Glide.with(context).load(((ItemImage) obj).image_url).into(holder.image);
        }

        if (showRemoveButton) {
            holder.removeButton.setVisibility(View.VISIBLE);
            holder.removeButton.setOnClickListener(v -> {
                if (listener != null) listener.onRemoveClick(position);
            });
        } else {
            holder.removeButton.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(obj, position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageButton removeButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgThumbnail);
            removeButton = itemView.findViewById(R.id.btnRemoveThumb);
        }
    }
}
