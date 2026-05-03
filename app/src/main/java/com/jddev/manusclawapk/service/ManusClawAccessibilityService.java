package com.jddev.manusclawapk.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.ArrayList;
import java.util.List;

public class ManusClawAccessibilityService extends AccessibilityService {

    private static ManusClawAccessibilityService instance;

    public static ManusClawAccessibilityService getInstance() { return instance; }

    @Override
    public void onServiceConnected() {
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    /* ── Public API for AgentEngine ── */

    /** Dump all visible text from current screen. */
    public String dumpScreen() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return "[screen unavailable]";
        StringBuilder sb = new StringBuilder();
        collectText(root, sb, 0);
        root.recycle();
        return sb.length() == 0 ? "[empty screen]" : sb.toString().trim();
    }

    /** List of clickable node descriptions on screen. */
    public List<String> listClickable() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        List<String> result = new ArrayList<>();
        if (root == null) return result;
        collectClickable(root, result);
        root.recycle();
        return result;
    }

    /** Tap at coordinates (x, y). */
    public boolean tap(int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, 100);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(stroke).build();
        final boolean[] success = {false};
        dispatchGesture(gesture, new GestureResultCallback() {
            @Override public void onCompleted(GestureDescription g) { success[0] = true; }
        }, null);
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        return success[0];
    }

    /** Tap the first node whose text contains the given label (case-insensitive). */
    public boolean tapByText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;
        List<AccessibilityNodeInfo> found = root.findAccessibilityNodeInfosByText(text);
        root.recycle();
        if (found == null || found.isEmpty()) return false;
        AccessibilityNodeInfo node = found.get(0);
        Rect r = new Rect();
        node.getBoundsInScreen(r);
        node.recycle();
        return tap(r.centerX(), r.centerY());
    }

    /** Type text into focused field. */
    public boolean typeText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;
        AccessibilityNodeInfo focused = findFocused(root);
        root.recycle();
        if (focused == null) return false;
        Bundle args = new Bundle();
        args.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        boolean ok = focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        focused.recycle();
        return ok;
    }

    /** Swipe up (scroll down). */
    public boolean swipeUp() {
        Path path = new Path();
        path.moveTo(540, 1200);
        path.lineTo(540, 400);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 300))
                .build();
        dispatchGesture(gesture, null, null);
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        return true;
    }

    /** Swipe down (scroll up). */
    public boolean swipeDown() {
        Path path = new Path();
        path.moveTo(540, 400);
        path.lineTo(540, 1200);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 300))
                .build();
        dispatchGesture(gesture, null, null);
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        return true;
    }

    /** Press back. */
    public void pressBack() { performGlobalAction(GLOBAL_ACTION_BACK); }

    /** Press home. */
    public void pressHome() { performGlobalAction(GLOBAL_ACTION_HOME); }

    /** Open recent apps. */
    public void pressRecents() { performGlobalAction(GLOBAL_ACTION_RECENTS); }

    /* ── Helpers ── */

    private void collectText(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null) return;
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        if (text != null && text.length() > 0) {
            for (int i = 0; i < depth; i++) sb.append("  ");
            sb.append(text).append('\n');
        } else if (desc != null && desc.length() > 0) {
            for (int i = 0; i < depth; i++) sb.append("  ");
            sb.append('[').append(desc).append(']').append('\n');
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectText(child, sb, depth + 1);
                child.recycle();
            }
        }
    }

    private void collectClickable(AccessibilityNodeInfo node, List<String> result) {
        if (node == null) return;
        if (node.isClickable()) {
            CharSequence text = node.getText();
            CharSequence desc = node.getContentDescription();
            String label = text != null ? text.toString() : (desc != null ? desc.toString() : "");
            if (!label.isEmpty()) {
                Rect r = new Rect();
                node.getBoundsInScreen(r);
                result.add(label + " @(" + r.centerX() + "," + r.centerY() + ")");
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectClickable(child, result);
                child.recycle();
            }
        }
    }

    private AccessibilityNodeInfo findFocused(AccessibilityNodeInfo node) {
        if (node == null) return null;
        if (node.isFocused() && node.isEditable()) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            AccessibilityNodeInfo found = findFocused(child);
            if (found != null) { if (child != null) child.recycle(); return found; }
            if (child != null) child.recycle();
        }
        return null;
    }
}
