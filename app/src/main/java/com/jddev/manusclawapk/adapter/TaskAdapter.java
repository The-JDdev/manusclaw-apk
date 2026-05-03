package com.jddev.manusclawapk.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import com.jddev.manusclawapk.model.Task;
import java.util.List;

public class TaskAdapter extends BaseAdapter {

    private final Context ctx;
    private final List<Task> tasks;

    public TaskAdapter(Context ctx, List<Task> tasks) {
        this.ctx   = ctx;
        this.tasks = tasks;
    }

    @Override public int     getCount()             { return tasks.size(); }
    @Override public Task    getItem(int i)          { return tasks.get(i); }
    @Override public long    getItemId(int i)        { return tasks.get(i).id; }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        Task t = tasks.get(pos);

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(32, 20, 32, 20);
        row.setBackgroundColor(0xFF1a1a1a);

        // Top row: status + timestamp
        LinearLayout top = new LinearLayout(ctx);
        top.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvStatus = new TextView(ctx);
        tvStatus.setText(t.statusLabel());
        tvStatus.setTextSize(11);
        tvStatus.setTypeface(Typeface.MONOSPACE);
        tvStatus.setTextColor(statusColor(t.status));

        TextView tvTime = new TextView(ctx);
        tvTime.setText("  " + formatTs(t.createdAt));
        tvTime.setTextSize(10);
        tvTime.setTextColor(0xFF666666);
        tvTime.setTypeface(Typeface.MONOSPACE);

        top.addView(tvStatus);
        top.addView(tvTime);
        row.addView(top);

        // Prompt
        TextView tvPrompt = new TextView(ctx);
        tvPrompt.setText(t.prompt);
        tvPrompt.setTextSize(13);
        tvPrompt.setTextColor(0xFFe0e0e0);
        tvPrompt.setTypeface(Typeface.MONOSPACE);
        tvPrompt.setPadding(0, 8, 0, 0);
        row.addView(tvPrompt);

        // Result (if any)
        if (t.result != null && !t.result.isEmpty()) {
            View div = new View(ctx);
            div.setBackgroundColor(0xFF333333);
            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
            dp.setMargins(0, 8, 0, 8);
            div.setLayoutParams(dp);
            row.addView(div);

            TextView tvResult = new TextView(ctx);
            tvResult.setText(t.result);
            tvResult.setTextSize(12);
            tvResult.setTextColor(0xFF00ff88);
            tvResult.setTypeface(Typeface.MONOSPACE);
            row.addView(tvResult);
        }

        return row;
    }

    private int statusColor(int status) {
        switch (status) {
            case Task.STATUS_RUNNING: return 0xFFffcc00;
            case Task.STATUS_DONE:    return 0xFF00ff88;
            case Task.STATUS_FAILED:  return 0xFFff4444;
            default:                  return 0xFF888888;
        }
    }

    private String formatTs(long ts) {
        java.text.SimpleDateFormat sdf =
            new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US);
        return sdf.format(new java.util.Date(ts));
    }
}
