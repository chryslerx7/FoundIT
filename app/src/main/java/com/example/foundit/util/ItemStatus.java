package com.example.foundit.util;

import android.content.Context;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.foundit.R;
import com.example.foundit.model.Item;

import java.util.Locale;

/**
 * Single source of truth for item status badge display.
 *
 * Priority:
 *   IF resolved  -> RESOLVED
 *   ELSE IF lost -> LOST
 *   ELSE         -> FOUND
 *
 * The original Lost/Found {@code type} is retained internally; only the
 * displayed badge reflects the current {@code status}.
 */
public final class ItemStatus {

    private ItemStatus() {}

    public static boolean isResolved(Item item) {
        return item != null && "RESOLVED".equalsIgnoreCase(item.status);
    }

    public static boolean isResolved(String status) {
        return "RESOLVED".equalsIgnoreCase(status);
    }

    /** Pure logic (unit-testable): resolved wins over type. */
    public static String displayLabel(String type, String status) {
        if (isResolved(status)) return "RESOLVED";
        if (type == null) return "";
        String t = type.trim().toUpperCase(Locale.US);
        if ("FOUND".equals(t)) return "FOUND";
        if ("LOST".equals(t)) return "LOST";
        return t;
    }

    public static String displayLabel(Item item) {
        if (item == null) return "";
        return displayLabel(item.type, item.status);
    }

    /** Resolved uses the existing premium primary color (theme-aware via values-night). */
    public static int badgeBackgroundRes(Item item) {
        return badgeBackgroundRes(item == null ? null : item.type, item == null ? null : item.status);
    }

    public static int badgeBackgroundRes(String type, String status) {
        if (isResolved(status)) return R.drawable.bg_resolved_badge;
        if ("FOUND".equalsIgnoreCase(type)) return R.drawable.bg_green_badge;
        return R.drawable.bg_red_badge;
    }

    /** Applies label + theme-aware rounded badge background consistently everywhere. */
    public static void applyBadge(Context context, TextView badge, Item item) {
        if (badge == null) return;
        badge.setText(displayLabel(item));
        badge.setBackgroundResource(badgeBackgroundRes(item));
        if (context != null) {
            badge.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }
    }
}
