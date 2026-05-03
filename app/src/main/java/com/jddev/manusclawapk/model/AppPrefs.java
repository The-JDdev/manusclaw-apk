package com.jddev.manusclawapk.model;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPrefs {
    private static final String PREFS = "mca_prefs";
    private final SharedPreferences sp;

    public AppPrefs(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String getApiKey()      { return sp.getString("api_key", ""); }
    public String getEndpoint()    { return sp.getString("endpoint", "https://api.groq.com/openai/v1/chat/completions"); }
    public String getModel()       { return sp.getString("model", "llama3-8b-8192"); }
    public int    getMaxSteps()    { return sp.getInt("max_steps", 10); }
    public boolean isAccessOn()    { return sp.getBoolean("access_on", false); }
    public String getStoragePath() { return sp.getString("storage_path", ""); }

    public void setApiKey(String v)      { sp.edit().putString("api_key", v).apply(); }
    public void setEndpoint(String v)    { sp.edit().putString("endpoint", v).apply(); }
    public void setModel(String v)       { sp.edit().putString("model", v).apply(); }
    public void setMaxSteps(int v)       { sp.edit().putInt("max_steps", v).apply(); }
    public void setAccessOn(boolean v)   { sp.edit().putBoolean("access_on", v).apply(); }
    public void setStoragePath(String v) { sp.edit().putString("storage_path", v).apply(); }
}
