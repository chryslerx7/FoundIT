package com.example.foundit.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF = "foundit_session";
    private static final String TOKEN = "token";
    private static final String NAME = "name";
    private static final String ID = "user_id";
    private static final String ROLE = "user_role";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void save(String token, String name, int id, String role) {
        prefs.edit().putString(TOKEN, token).putString(NAME, name).putInt(ID, id).putString(ROLE, role).apply();
    }

    public int userId() {
        return prefs.getInt(ID, -1);
    }

    public String role() {
        return prefs.getString(ROLE, "student");
    }

    public String token() {
        return prefs.getString(TOKEN, "");
    }

    public String authHeader() {
        return "Bearer " + token();
    }

    public String name() {
        return prefs.getString(NAME, "Student");
    }

    public boolean isLoggedIn() {
        return !token().isEmpty();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
