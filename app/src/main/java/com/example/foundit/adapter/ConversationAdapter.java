package com.example.foundit.adapter;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foundit.R;
import com.example.foundit.model.Conversation;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.H> {
    List<Conversation> list;
    int myId;
    OnItemClick click;

    public interface OnItemClick { void onClick(Conversation c); }

    public ConversationAdapter(List<Conversation> list, int myId, OnItemClick click) {
        this.list = list;
        this.myId = myId;
        this.click = click;
    }

    @NonNull @Override public H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new H(v);
    }

    @Override public void onBindViewHolder(@NonNull H holder, int position) {
        Conversation c = list.get(position);
        boolean isOwnerOfLost = c.lostItem.user_id == myId;
        String otherUser = isOwnerOfLost ? c.foundItem.user.name : c.lostItem.user.name;

        holder.user.setText(otherUser);
        holder.item.setText("Item: " + c.lostItem.item_name);
        if (c.messages != null && !c.messages.isEmpty()) {
            holder.last.setText(c.messages.get(0).message);
        } else holder.last.setText("No messages yet.");

        holder.itemView.setOnClickListener(v -> click.onClick(c));
    }

    @Override public int getItemCount() { return list.size(); }

    static class H extends RecyclerView.ViewHolder {
        TextView user, item, last;
        H(View v) {
            super(v);
            user = v.findViewById(R.id.tvConvUser);
            item = v.findViewById(R.id.tvConvItem);
            last = v.findViewById(R.id.tvConvLastMsg);
        }
    }
}
