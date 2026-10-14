package com.example.foundit.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foundit.ItemDetailActivity;
import com.example.foundit.R;
import com.example.foundit.model.Item;
import com.example.foundit.model.Message;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface OnMessageLongClickListener {
        void onMessageLongClick(Message message, int position);
    }

    private final List<Message> list;
    private final int myId;
    private OnMessageLongClickListener longClickListener;

    public MessageAdapter(List<Message> list, int myId) {
        this.list = list;
        this.myId = myId;
    }

    public MessageAdapter(List<Message> list, int myId, OnMessageLongClickListener longClickListener) {
        this.list = list;
        this.myId = myId;
        this.longClickListener = longClickListener;
    }

    public void setOnMessageLongClickListener(OnMessageLongClickListener listener) {
        this.longClickListener = listener;
    }

    @Override public int getItemViewType(int position) {
        Message m = list.get(position);
        boolean isShare = m.messageType != null && m.messageType.equals("item_share");
        if (m.senderId == myId) {
            return isShare ? 3 : 1;
        } else {
            return isShare ? 4 : 2;
        }
    }

    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 3) {
            return new ShareHolder(inflater.inflate(R.layout.item_message_sent_share, parent, false));
        } else if (viewType == 4) {
            return new ShareHolder(inflater.inflate(R.layout.item_message_received_share, parent, false));
        } else if (viewType == 1) {
            return new TextHolder(inflater.inflate(R.layout.item_message_sent, parent, false));
        } else {
            return new TextHolder(inflater.inflate(R.layout.item_message_received, parent, false));
        }
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message m = list.get(position);
        String timeStr = m.createdAt != null && m.createdAt.length() >= 16 ? m.createdAt.substring(11, 16) : "";

        if (holder instanceof TextHolder) {
            TextHolder th = (TextHolder) holder;
            th.msg.setText(m.message);
            th.time.setText(timeStr);
        } else if (holder instanceof ShareHolder) {
            ShareHolder sh = (ShareHolder) holder;
            sh.time.setText(timeStr);
            Item item = m.item;
            if (item != null) {
                sh.itemName.setText(item.item_name != null ? item.item_name : "Item Report");
                boolean isLost = "LOST".equalsIgnoreCase(item.type);
                sh.badge.setText(isLost ? "LOST" : "FOUND");
                sh.badge.setBackgroundColor(ContextCompat.getColor(sh.itemView.getContext(),
                        isLost ? R.color.foundit_lost : R.color.foundit_found));

                String details = (item.category != null ? item.category : "") +
                        (item.location != null && !item.location.isEmpty() ? " • " + item.location : "");
                sh.details.setText(details);

                if (item.image_url != null && !item.image_url.isEmpty()) {
                    Glide.with(sh.itemView.getContext()).load(item.image_url).into(sh.itemImage);
                } else {
                    sh.itemImage.setImageResource(R.drawable.ic_launcher_foreground);
                }

                sh.viewReport.setOnClickListener(v -> {
                    Context ctx = v.getContext();
                    Intent i = new Intent(ctx, ItemDetailActivity.class);
                    i.putExtra("item_id", item.id);
                    ctx.startActivity(i);
                });
            } else {
                sh.itemName.setText("Item Report (Unavailable)");
                sh.badge.setText("REPORT");
                sh.details.setText("Report details not found");
                sh.itemImage.setImageResource(R.drawable.ic_launcher_foreground);
                sh.viewReport.setEnabled(false);
            }
        }

        if (m.senderId == myId && longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onMessageLongClick(m, position);
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
    }

    @Override public int getItemCount() { return list.size(); }

    static class TextHolder extends RecyclerView.ViewHolder {
        TextView msg, time;
        TextHolder(View v) {
            super(v);
            msg = v.findViewById(R.id.tvMessage);
            time = v.findViewById(R.id.tvTime);
        }
    }

    static class ShareHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView badge, itemName, details, time;
        Button viewReport;
        ShareHolder(View v) {
            super(v);
            itemImage = v.findViewById(R.id.ivItemImage);
            badge = v.findViewById(R.id.tvBadge);
            itemName = v.findViewById(R.id.tvItemName);
            details = v.findViewById(R.id.tvDetails);
            time = v.findViewById(R.id.tvTime);
            viewReport = v.findViewById(R.id.btnViewReport);
        }
    }
}
