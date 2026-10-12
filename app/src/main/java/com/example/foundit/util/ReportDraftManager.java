package com.example.foundit.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReportDraftManager {
    private static final String PREF_NAME = "foundit_report_drafts";
    private static final String KEY_DRAFTS = "drafts_json";

    public static class ReportDraft {
        public String draftId;
        public int userId;
        public String type; // LOST or FOUND
        public String name;
        public String category;
        public String description;
        public String location;
        public String date;
        public String contact;
        public ArrayList<String> imageUriStrings;
        public long lastEditedTime;
    }

    public static void saveDraft(Context context, ReportDraft draft) {
        if (context == null || draft == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        List<ReportDraft> drafts = getAllDrafts(context);
        
        drafts.removeIf(d -> d.draftId != null && d.draftId.equals(draft.draftId));
        drafts.add(draft);
        
        String json = new Gson().toJson(drafts);
        prefs.edit().putString(KEY_DRAFTS, json).apply();
    }

    public static List<ReportDraft> getDrafts(Context context, int userId) {
        List<ReportDraft> all = getAllDrafts(context);
        List<ReportDraft> userDrafts = new ArrayList<>();
        for (ReportDraft d : all) {
            if (d.userId == userId) {
                userDrafts.add(d);
            }
        }
        return userDrafts;
    }

    public static ReportDraft getDraft(Context context, String draftId) {
        if (draftId == null) return null;
        List<ReportDraft> all = getAllDrafts(context);
        for (ReportDraft d : all) {
            if (d.draftId != null && d.draftId.equals(draftId)) {
                return d;
            }
        }
        return null;
    }

    public static void deleteDraft(Context context, String draftId) {
        if (context == null || draftId == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        List<ReportDraft> drafts = getAllDrafts(context);
        drafts.removeIf(d -> d.draftId != null && d.draftId.equals(draftId));
        String json = new Gson().toJson(drafts);
        prefs.edit().putString(KEY_DRAFTS, json).apply();
    }

    private static List<ReportDraft> getAllDrafts(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_DRAFTS, "null");
        if (json == null || json.equals("null")) {
            return new ArrayList<>();
        }
        try {
            Type type = new TypeToken<List<ReportDraft>>() {}.getType();
            List<ReportDraft> list = new Gson().fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
