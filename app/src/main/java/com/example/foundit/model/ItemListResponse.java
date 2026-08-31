package com.example.foundit.model;

import java.util.List;

public class ItemListResponse {
    public boolean success;
    public List<Item> items;
    public int total;
    public int lost_count;
    public int found_count;
}
