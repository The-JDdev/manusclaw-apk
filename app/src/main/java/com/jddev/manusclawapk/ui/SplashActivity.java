package com.jddev.manusclawapk.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.*;

public class SplashActivity extends Activity {

    private static final String ASCII =
        "  __  __                       ____  _\n" +
        " |  \\/  | __ _ _ __  _   _ ___/ ___|| | __ ___      __\n" +
        " | |\\/| |/ _` | '_ \\| | | / __\\___ \\| |/ _` \\ \\ /\\ / /\n" +
        " | |  | | (_| | | | | |_| \\__ \\___) | | (_| |\\ V  V /\n" +
        " |_|  |_|\\__,_|_| |_|\\__,_|___/____/|_|\\__,_| \\_/\\_/\n" +
        "\n" +
        "       APK  ·  Autonomous On-Device Agent\n" +
        "       Accessibility · Storage · LLM · No PC";

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);

        getWindow().getDecorView().setBackgroundColor(0xFF0d0d0d);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(0xFF0d0d0d);

        TextView tvAscii = new TextView(this);
        tvAscii.setText(ASCII);
        tvAscii.setTextColor(0xFF00ff88);
        tvAscii.setTextSize(9.5f);
        tvAscii.setTypeface(Typeface.MONOSPACE);
        tvAscii.setGravity(Gravity.CENTER);
        tvAscii.setPadding(16, 0, 16, 0);
        root.addView(tvAscii);

        TextView tvSub = new TextView(this);
        tvSub.setText("\nv1.0.0  ·  by The-JDdev (SHS Shobuj)\ngithub.com/The-JDdev/manusclaw-apk");
        tvSub.setTextColor(0xFF666666);
        tvSub.setTextSize(11);
        tvSub.setTypeface(Typeface.MONOSPACE);
        tvSub.setGravity(Gravity.CENTER);
        tvSub.setPadding(0, 16, 0, 0);
        root.addView(tvSub);

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setIndeterminate(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(400,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 56;
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        pb.setLayoutParams(lp);
        root.addView(pb);

        setContentView(root);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 2200);
    }
}
