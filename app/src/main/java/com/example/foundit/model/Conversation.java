package com.example.foundit.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Conversation {
    public int id;
    @SerializedName("lost_item_id")
    public int lostItemId;
    @SerializedName("found_item_id")
    public int foundItemId;
    @SerializedName("lost_item")
    public Item lostItem;
    @SerializedName("found_item")
    public Item foundItem;
    public List<Message> messages;
    @SerializedName("created_at")
    public String createdAt;
    @SerializedName("updated_at")
    public String updatedAt;
}
