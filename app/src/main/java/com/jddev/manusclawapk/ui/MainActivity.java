package com.jddev.manusclawapk.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import com.jddev.manusclawapk.adapter.TaskAdapter;
import com.jddev.manusclawapk.model.*;
import com.jddev.manusclawapk.service.AgentForegroundService;
import com.jddev.manusclawapk.service.ManusClawAccessibilityService;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private EditText    etTask;
    private TextView    tvStatus;
    private ListView    lvHistory;
    private Button      btnRun;
    private TextView    tvAccessStatus;

    private final List<Task> taskHistory = new ArrayList<>();
    private TaskAdapter adapter;
    private Task        currentTask;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        getWindow().getDecorView().setBackgroundColor(0xFF0d0d0d);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0d0d0d);

        // ── Top bar ──
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF111111);
        topBar.setPadding(24, 20, 24, 20);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("ManusClaw APK");
        tvTitle.setTextColor(0xFF00ff88);
        tvTitle.setTextSize(16);
        tvTitle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvTitle.setLayoutParams(titleLp);
        topBar.addView(tvTitle);

        // Accessibility status dot
        tvAccessStatus = new TextView(this);
        tvAccessStatus.setTextSize(12);
        tvAccessStatus.setTypeface(Typeface.MONOSPACE);
        topBar.addView(tvAccessStatus);

        // Settings button
        Button btnSettings = new Button(this);
        btnSettings.setText("⚙");
        btnSettings.setTextColor(0xFF00ff88);
        btnSettings.setTextSize(16);
        btnSettings.setBackgroundColor(Color.TRANSPARENT);
        btnSettings.setAllCaps(false);
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        topBar.addView(btnSettings);

        root.addView(topBar);

        // ── Access warning banner ──
        LinearLayout warnBanner = new LinearLayout(this);
        warnBanner.setOrientation(LinearLayout.HORIZONTAL);
        warnBanner.setBackgroundColor(0xFF1a1000);
        warnBanner.setPadding(24, 12, 24, 12);
        warnBanner.setGravity(Gravity.CENTER_VERTICAL);
        warnBanner.setId(View.generateViewId());

        TextView tvWarn = new TextView(this);
        tvWarn.setText("⚠  Accessibility not enabled — agent cannot control screen");
        tvWarn.setTextColor(0xFFffcc00);
        tvWarn.setTextSize(11);
        tvWarn.setTypeface(Typeface.MONOSPACE);
        LinearLayout.LayoutParams warnLp = new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvWarn.setLayoutParams(warnLp);
        warnBanner.addView(tvWarn);

        Button btnEnable = new Button(this);
        btnEnable.setText("Enable");
        btnEnable.setTextColor(0xFFffcc00);
        btnEnable.setTextSize(11);
        btnEnable.setBackgroundColor(0xFF2a2000);
        btnEnable.setAllCaps(false);
        btnEnable.setOnClickListener(v -> {
            Toast.makeText(this, "Find 'ManusClaw APK' and toggle it ON", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
        warnBanner.addView(btnEnable);

        root.addView(warnBanner);

        // ── Status line ──
        tvStatus = new TextView(this);
        tvStatus.setText("● idle — enter a task below");
        tvStatus.setTextColor(0xFF666666);
        tvStatus.setTextSize(11);
        tvStatus.setTypeface(Typeface.MONOSPACE);
        tvStatus.setPadding(24, 12, 24, 4);
        root.addView(tvStatus);

        // ── Task history list ──
        lvHistory = new ListView(this);
        lvHistory.setBackgroundColor(0xFF0d0d0d);
        lvHistory.setDividerHeight(1);
        adapter = new TaskAdapter(this, taskHistory);
        lvHistory.setAdapter(adapter);
        LinearLayout.LayoutParams listLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        lvHistory.setLayoutParams(listLp);
        root.addView(lvHistory);

        // ── Input area ──
        LinearLayout inputArea = new LinearLayout(this);
        inputArea.setOrientation(LinearLayout.VERTICAL);
        inputArea.setBackgroundColor(0xFF111111);
        inputArea.setPadding(16, 12, 16, 12);

        etTask = new EditText(this);
        etTask.setHint("Enter task for the agent…\ne.g. 'Open Chrome and search ManusClaw'");
        etTask.setHintTextColor(0xFF444444);
        etTask.setTextColor(0xFFe0e0e0);
        etTask.setTextSize(13);
        etTask.setTypeface(Typeface.MONOSPACE);
        etTask.setBackgroundColor(0xFF1a1a1a);
        etTask.setPadding(20, 16, 20, 16);
        etTask.setMinLines(3);
        etTask.setMaxLines(5);
        etTask.setInputType(InputType.TYPE_CLASS_TEXT
            | InputType.TYPE_TEXT_FLAG_MULTI_LINE
            | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etTask.setGravity(Gravity.TOP);
        inputArea.addView(etTask);

        btnRun = new Button(this);
        btnRun.setText("▶  RUN AGENT");
        btnRun.setTextColor(0xFF0d0d0d);
        btnRun.setTextSize(14);
        btnRun.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        btnRun.setBackgroundColor(0xFF00ff88);
        btnRun.setAllCaps(false);
        LinearLayout.LayoutParams runLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        runLp.topMargin = 10;
        btnRun.setLayoutParams(runLp);
        btnRun.setOnClickListener(v -> runAgent());
        inputArea.addView(btnRun);

        root.addView(inputArea);
        setContentView(root);

        // Start foreground service
        Intent svcIntent = new Intent(this, AgentForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(svcIntent);
        } else {
            startService(svcIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAccessibilityStatus();
        AgentForegroundService.listener = new AgentForegroundService.StatusListener() {
            @Override public void onStatus(String s) { runOnUiThread(() -> setStatus("⚡ " + s, 0xFFffcc00)); }
            @Override public void onDone(String r)   { runOnUiThread(() -> onAgentDone(r)); }
            @Override public void onError(String e)  { runOnUiThread(() -> onAgentError(e)); }
        };
    }

    @Override protected void onPause() {
        super.onPause();
        AgentForegroundService.listener = null;
    }

    private void runAgent() {
        String prompt = etTask.getText().toString().trim();
        if (prompt.isEmpty()) {
            Toast.makeText(this, "Enter a task first", Toast.LENGTH_SHORT).show();
            return;
        }

        AppPrefs prefs = new AppPrefs(this);
        if (prefs.getApiKey().isEmpty()) {
            Toast.makeText(this, "Add your API key in ⚙ Settings first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etTask.getWindowToken(), 0);

        currentTask = new Task(prompt);
        currentTask.status = Task.STATUS_RUNNING;
        taskHistory.add(0, currentTask);
        adapter.notifyDataSetChanged();

        etTask.setText("");
        btnRun.setEnabled(false);
        btnRun.setBackgroundColor(0xFF333333);
        btnRun.setText("⚡ Running…");
        setStatus("⚡ Starting agent — " + prompt, 0xFFffcc00);

        Intent intent = new Intent(this, AgentForegroundService.class);
        intent.setAction(AgentForegroundService.ACTION_RUN);
        intent.putExtra(AgentForegroundService.EXTRA_TASK, prompt);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void onAgentDone(String result) {
        if (currentTask != null) {
            currentTask.status = Task.STATUS_DONE;
            currentTask.result = result;
        }
        adapter.notifyDataSetChanged();
        setStatus("✅ Done: " + result, 0xFF00ff88);
        resetButton();
    }

    private void onAgentError(String err) {
        if (currentTask != null) {
            currentTask.status = Task.STATUS_FAILED;
            currentTask.result = err;
        }
        adapter.notifyDataSetChanged();
        setStatus("❌ " + err, 0xFFff4444);
        resetButton();
    }

    private void resetButton() {
        btnRun.setEnabled(true);
        btnRun.setBackgroundColor(0xFF00ff88);
        btnRun.setText("▶  RUN AGENT");
    }

    private void setStatus(String text, int color) {
        tvStatus.setText(text);
        tvStatus.setTextColor(color);
    }

    private void updateAccessibilityStatus() {
        boolean active = ManusClawAccessibilityService.getInstance() != null;
        tvAccessStatus.setText(active ? "● ACC" : "○ ACC");
        tvAccessStatus.setTextColor(active ? 0xFF00ff88 : 0xFFff4444);
    }
}
