package com.jddev.manusclawapk.service;

import android.app.*;
import android.content.Intent;
import android.os.*;
import com.jddev.manusclawapk.agent.AgentEngine;
import com.jddev.manusclawapk.model.AppPrefs;
import com.jddev.manusclawapk.model.Task;
import com.jddev.manusclawapk.ui.MainActivity;

public class AgentForegroundService extends Service {

    public static final String ACTION_RUN  = "RUN";
    public static final String EXTRA_TASK  = "task_prompt";
    private static final String CHAN_ID    = "mca_agent";
    private static final int    NOTIF_ID   = 1;

    public static AgentForegroundService instance;
    public interface StatusListener { void onStatus(String s); void onDone(String r); void onError(String e); }
    public static StatusListener listener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        createChannel();
        startForeground(NOTIF_ID, buildNotif("ManusClaw agent running…"));

        if (intent != null && ACTION_RUN.equals(intent.getAction())) {
            String prompt = intent.getStringExtra(EXTRA_TASK);
            if (prompt != null && !prompt.isEmpty()) runTask(prompt);
        }
        return START_STICKY;
    }

    private void runTask(String prompt) {
        Task task = new Task(prompt);
        AppPrefs prefs = new AppPrefs(this);
        AgentEngine engine = new AgentEngine(prefs);
        engine.run(task, new AgentEngine.ProgressCallback() {
            @Override public void onStep(String s) {
                updateNotif(s);
                if (listener != null) new Handler(Looper.getMainLooper()).post(() -> listener.onStatus(s));
            }
            @Override public void onDone(String r) {
                updateNotif("Done: " + r);
                if (listener != null) new Handler(Looper.getMainLooper()).post(() -> listener.onDone(r));
            }
            @Override public void onError(String e) {
                updateNotif("Error: " + e);
                if (listener != null) new Handler(Looper.getMainLooper()).post(() -> listener.onError(e));
            }
        });
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHAN_ID,
                "ManusClaw Agent", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    private Notification buildNotif(String text) {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = new Notification.Builder(this, CHAN_ID)
            .setContentTitle("ManusClaw APK")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pi)
            .setOngoing(true);
        return b.build();
    }

    private void updateNotif(String text) {
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.notify(NOTIF_ID, buildNotif(text));
    }

    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public void onDestroy() { super.onDestroy(); instance = null; }
}
