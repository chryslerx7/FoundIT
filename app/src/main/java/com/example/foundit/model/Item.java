package com.example.foundit.model;

import java.util.List;

public class Item {
    public int id;
    public int user_id;
    public String item_name;
    public String category;
    public String description;
    public String location;
    public String date;
    public String type;
    public String status;
    public String contact;
    public String image_url;
    public List<ItemImage> images;
    public User user;
    public String created_at;
}
