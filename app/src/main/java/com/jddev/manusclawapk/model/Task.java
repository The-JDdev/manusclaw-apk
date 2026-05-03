package com.jddev.manusclawapk.model;

public class Task {
    public static final int STATUS_PENDING  = 0;
    public static final int STATUS_RUNNING  = 1;
    public static final int STATUS_DONE     = 2;
    public static final int STATUS_FAILED   = 3;

    public long   id;
    public String prompt;
    public String result;
    public int    status;
    public long   createdAt;

    public Task(String prompt) {
        this.prompt    = prompt;
        this.status    = STATUS_PENDING;
        this.createdAt = System.currentTimeMillis();
        this.id        = createdAt;
    }

    public String statusLabel() {
        switch (status) {
            case STATUS_RUNNING: return "⚡ Running";
            case STATUS_DONE:    return "✅ Done";
            case STATUS_FAILED:  return "❌ Failed";
            default:             return "⏳ Pending";
        }
    }
}
