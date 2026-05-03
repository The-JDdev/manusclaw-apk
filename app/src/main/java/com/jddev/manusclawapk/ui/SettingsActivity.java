package com.jddev.manusclawapk.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.jddev.manusclawapk.model.AppPrefs;

public class SettingsActivity extends Activity {

    private AppPrefs prefs;
    private EditText etApiKey, etEndpoint, etModel, etMaxSteps, etStorage;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        prefs = new AppPrefs(this);

        getWindow().getDecorView().setBackgroundColor(0xFF0d0d0d);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(0xFF0d0d0d);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 40, 32, 40);
        root.setBackgroundColor(0xFF0d0d0d);

        addHeader(root, "⚙  SETTINGS");

        // ── LLM Section ──
        addSectionLabel(root, "LLM / API");

        addLabel(root, "API Key (Groq / OpenAI compatible)");
        etApiKey = addField(root, "gsk_…", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etApiKey.setText(prefs.getApiKey());

        addLabel(root, "API Endpoint");
        etEndpoint = addField(root, "https://api.groq.com/openai/v1/chat/completions", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        etEndpoint.setText(prefs.getEndpoint());

        addLabel(root, "Model");
        etModel = addField(root, "llama3-8b-8192", InputType.TYPE_CLASS_TEXT);
        etModel.setText(prefs.getModel());

        addLabel(root, "Max agent steps");
        etMaxSteps = addField(root, "10", InputType.TYPE_CLASS_NUMBER);
        etMaxSteps.setText(String.valueOf(prefs.getMaxSteps()));

        addDivider(root);

        // ── Permissions Section ──
        addSectionLabel(root, "PERMISSIONS");

        Button btnAccessibility = addButton(root, "🔓  Enable Accessibility Service", 0xFF1a3a2a);
        btnAccessibility.setOnClickListener(v -> {
            Toast.makeText(this, "Enable 'ManusClaw APK' in the list", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });

        Button btnStorage = addButton(root, "📁  Grant Storage Access", 0xFF1a2a3a);
        btnStorage.setOnClickListener(v -> grantStorage());

        addLabel(root, "Storage path (for task logs)");
        etStorage = addField(root, "/sdcard/ManusClaw", InputType.TYPE_CLASS_TEXT);
        etStorage.setText(prefs.getStoragePath().isEmpty()
            ? Environment.getExternalStorageDirectory().getAbsolutePath()
            : prefs.getStoragePath());

        addDivider(root);

        // ── Groq free models info ──
        addSectionLabel(root, "FREE GROQ MODELS");
        addInfoBox(root,
            "llama3-8b-8192       ← fast, free\n" +
            "llama3-70b-8192      ← smarter, free\n" +
            "mixtral-8x7b-32768   ← long context\n" +
            "gemma2-9b-it         ← Google\n\n" +
            "Get free key at: console.groq.com\n" +
            "No billing required for free tier.");

        addDivider(root);

        // ── Donation ──
        addSectionLabel(root, "SUPPORT DEVELOPMENT");
        addInfoBox(root,
            "USDT TRC20: TH75J4zaMPwhyR3QxEFdwTCgU2Pp3yPUEr\n" +
            "bKash:      01310211442\n" +
            "WMT:        T202226490170\n" +
            "WMZ:        Z430378899900");

        // Save button
        Button btnSave = addButton(root, "💾  SAVE SETTINGS", 0xFF003322);
        btnSave.setOnClickListener(v -> save());

        sv.addView(root);
        setContentView(sv);
    }

    private void save() {
        prefs.setApiKey(etApiKey.getText().toString().trim());
        prefs.setEndpoint(etEndpoint.getText().toString().trim());
        prefs.setModel(etModel.getText().toString().trim());
        try { prefs.setMaxSteps(Integer.parseInt(etMaxSteps.getText().toString().trim())); }
        catch (Exception e) { prefs.setMaxSteps(10); }
        prefs.setStoragePath(etStorage.getText().toString().trim());
        Toast.makeText(this, "Saved ✓", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void grantStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } else {
                Toast.makeText(this, "Storage access already granted ✓", Toast.LENGTH_SHORT).show();
            }
        } else {
            requestPermissions(new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 42);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        if (code == 42) {
            boolean ok = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED;
            Toast.makeText(this, ok ? "Storage granted ✓" : "Storage denied — logs won't be saved",
                Toast.LENGTH_SHORT).show();
        }
    }

    /* ── UI helpers ── */

    private void addHeader(LinearLayout p, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(18);
        tv.setTextColor(0xFF00ff88);
        tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tv.setPadding(0, 0, 0, 24);
        p.addView(tv);
    }

    private void addSectionLabel(LinearLayout p, String text) {
        TextView tv = new TextView(this);
        tv.setText("── " + text + " ──────────────────");
        tv.setTextSize(11);
        tv.setTextColor(0xFF00ff88);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setPadding(0, 16, 0, 12);
        p.addView(tv);
    }

    private void addLabel(LinearLayout p, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(0xFF888888);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setPadding(0, 12, 0, 4);
        p.addView(tv);
    }

    private EditText addField(LinearLayout p, String hint, int inputType) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setHintTextColor(0xFF444444);
        et.setTextColor(0xFFe0e0e0);
        et.setTextSize(13);
        et.setTypeface(Typeface.MONOSPACE);
        et.setInputType(inputType);
        et.setBackgroundColor(0xFF1a1a1a);
        et.setPadding(20, 14, 20, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = 4;
        et.setLayoutParams(lp);
        p.addView(et);
        return et;
    }

    private Button addButton(LinearLayout p, String text, int bg) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(0xFF00ff88);
        btn.setTextSize(13);
        btn.setTypeface(Typeface.MONOSPACE);
        btn.setBackgroundColor(bg);
        btn.setAllCaps(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 8;
        lp.bottomMargin = 8;
        btn.setLayoutParams(lp);
        p.addView(btn);
        return btn;
    }

    private void addInfoBox(LinearLayout p, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(11);
        tv.setTextColor(0xFF666666);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setBackgroundColor(0xFF111111);
        tv.setPadding(20, 14, 20, 14);
        p.addView(tv);
    }

    private void addDivider(LinearLayout p) {
        View v = new View(this);
        v.setBackgroundColor(0xFF222222);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(0, 20, 0, 4);
        v.setLayoutParams(lp);
        p.addView(v);
    }
}
