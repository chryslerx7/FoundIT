package com.example.foundit.model;

import com.google.gson.annotations.SerializedName;

public class Message {
    public int id;
    @SerializedName("conversation_id")
    public int conversationId;
    @SerializedName("sender_id")
    public int senderId;
    public String message;
    @SerializedName("message_type")
    public String messageType;
    @SerializedName("item_id")
    public Integer itemId;
    public Item item;
    public User sender;
    @SerializedName("created_at")
    public String createdAt;
}
