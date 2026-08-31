package com.example.foundit.model;

import com.google.gson.annotations.SerializedName;

public class Message {
    public int id;
    @SerializedName("conversation_id")
    public int conversationId;
    @SerializedName("sender_id")
    public int senderId;
    public String message;
    public User sender;
    @SerializedName("created_at")
    public String createdAt;
}
