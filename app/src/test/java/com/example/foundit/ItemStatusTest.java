package com.example.foundit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.foundit.model.Item;
import com.example.foundit.util.ItemStatus;

import org.junit.Test;

public class ItemStatusTest {

    private static Item item(String type, String status) {
        Item i = new Item();
        i.type = type;
        i.status = status;
        return i;
    }

    @Test public void activeLost_showsLost() {
        assertEquals("LOST", ItemStatus.displayLabel(item("LOST", "ACTIVE")));
    }

    @Test public void activeFound_showsFound() {
        assertEquals("FOUND", ItemStatus.displayLabel(item("FOUND", "ACTIVE")));
    }

    @Test public void resolvedLost_showsResolved() {
        assertEquals("RESOLVED", ItemStatus.displayLabel(item("LOST", "RESOLVED")));
    }

    @Test public void resolvedFound_showsResolved() {
        assertEquals("RESOLVED", ItemStatus.displayLabel(item("FOUND", "RESOLVED")));
    }

    @Test public void resolvedIsCaseInsensitive() {
        assertEquals("RESOLVED", ItemStatus.displayLabel(item("lost", "resolved")));
        assertTrue(ItemStatus.isResolved(item("LOST", "resolved")));
    }

    @Test public void activeIsNotResolved() {
        assertFalse(ItemStatus.isResolved(item("LOST", "ACTIVE")));
        assertFalse(ItemStatus.isResolved(item("FOUND", null)));
    }
}
