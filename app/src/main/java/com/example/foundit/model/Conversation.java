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
    @SerializedName("direct_item_id")
    public Integer directItemId;
    @SerializedName("user_one_id")
    public Integer userOneId;
    @SerializedName("user_two_id")
    public Integer userTwoId;
    @SerializedName("direct_item")
    public Item directItem;
    @SerializedName("user_one")
    public User userOne;
    @SerializedName("user_two")
    public User userTwo;
    public List<Message> messages;
    @SerializedName("created_at")
    public String createdAt;
    @SerializedName("updated_at")
    public String updatedAt;
}
