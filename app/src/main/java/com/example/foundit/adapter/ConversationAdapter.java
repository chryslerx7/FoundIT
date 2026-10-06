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
    OnConversationLongClickListener longClickListener;

    public interface OnItemClick { void onClick(Conversation c); }

    public interface OnConversationLongClickListener {
        void onConversationLongClick(Conversation c, int position);
    }

    public ConversationAdapter(List<Conversation> list, int myId, OnItemClick click) {
        this.list = list;
        this.myId = myId;
        this.click = click;
    }

    public void setOnConversationLongClickListener(OnConversationLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull @Override public H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new H(v);
    }

    @Override public void onBindViewHolder(@NonNull H holder, int position) {
        Conversation c = list.get(position);
        String otherUser;
        String itemName;
        if (c.directItem != null) {
            com.example.foundit.model.User other = null;
            if (c.userOne != null && c.userTwo != null) {
                other = (c.userOne.id == myId) ? c.userTwo : c.userOne;
            }
            if (other != null && other.name != null) {
                otherUser = other.name;
            } else {
                otherUser = (c.directItem.user != null && c.directItem.user.name != null)
                        ? c.directItem.user.name : "Reporter";
            }
            itemName = c.directItem.item_name != null ? c.directItem.item_name : "";
        } else {
            boolean isOwnerOfLost = c.lostItem != null && c.lostItem.user_id == myId;
            com.example.foundit.model.Item otherItem = isOwnerOfLost ? c.foundItem : c.lostItem;
            otherUser = (otherItem != null && otherItem.user != null && otherItem.user.name != null)
                    ? otherItem.user.name : "Reporter";
            itemName = (c.lostItem != null && c.lostItem.item_name != null) ? c.lostItem.item_name : "";
        }

        holder.user.setText(otherUser);
        holder.item.setText("Item: " + itemName);
        if (c.messages != null && !c.messages.isEmpty()) {
            holder.last.setText(c.messages.get(0).message);
        } else holder.last.setText("No messages yet.");

        holder.itemView.setOnClickListener(v -> click.onClick(c));
        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onConversationLongClick(c, holder.getBindingAdapterPosition());
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
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
