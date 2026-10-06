package com.example.foundit.adapter;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foundit.R;
import com.example.foundit.model.Message;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.H> {
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
        return list.get(position).senderId == myId ? 1 : 2;
    }

    @NonNull @Override public H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                viewType == 1 ? R.layout.item_message_sent : R.layout.item_message_received, parent, false);
        return new H(v);
    }

    @Override public void onBindViewHolder(@NonNull H holder, int position) {
        Message m = list.get(position);
        holder.msg.setText(m.message);
        holder.time.setText(m.createdAt != null && m.createdAt.length() >= 16 ? m.createdAt.substring(11, 16) : "");

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

    static class H extends RecyclerView.ViewHolder {
        TextView msg, time;
        H(View v) {
            super(v);
            msg = v.findViewById(R.id.tvMessage);
            time = v.findViewById(R.id.tvTime);
        }
    }
}
