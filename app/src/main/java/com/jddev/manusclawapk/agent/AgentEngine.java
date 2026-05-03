package com.jddev.manusclawapk.agent;

import com.jddev.manusclawapk.model.AppPrefs;
import com.jddev.manusclawapk.model.Task;
import com.jddev.manusclawapk.service.ManusClawAccessibilityService;
import org.json.JSONObject;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class AgentEngine {

    public interface ProgressCallback {
        void onStep(String stepText);
        void onDone(String result);
        void onError(String err);
    }

    private static final String SYSTEM_PROMPT =
        "You are ManusClaw, an autonomous AI agent running directly on an Android phone. "
      + "You receive a task and the current screen content, then reason step-by-step to complete it.\n\n"
      + "Available actions (respond with one action per reply in JSON format):\n"
      + "  {\"action\":\"read_screen\"} — get current screen text\n"
      + "  {\"action\":\"tap_text\",\"text\":\"...\"}  — tap the element with this text\n"
      + "  {\"action\":\"tap_coords\",\"x\":540,\"y\":900} — tap at pixel coordinates\n"
      + "  {\"action\":\"type_text\",\"text\":\"...\"}  — type text into focused field\n"
      + "  {\"action\":\"swipe_up\"}  — scroll down\n"
      + "  {\"action\":\"swipe_down\"} — scroll up\n"
      + "  {\"action\":\"press_back\"} — press back\n"
      + "  {\"action\":\"press_home\"} — go to home screen\n"
      + "  {\"action\":\"wait\",\"ms\":1000} — wait milliseconds\n"
      + "  {\"action\":\"done\",\"result\":\"...\"} — task complete, return result\n"
      + "  {\"action\":\"error\",\"reason\":\"...\"}  — cannot complete, explain why\n\n"
      + "Rules:\n"
      + "- Always start with read_screen to understand context.\n"
      + "- Respond ONLY with a single JSON object — no markdown, no explanation.\n"
      + "- After every action you will receive the result, then decide next step.\n"
      + "- Be concise and efficient. Complete tasks in as few steps as possible.";

    private final AppPrefs prefs;
    private final String storagePath;

    public AgentEngine(AppPrefs prefs) {
        this.prefs       = prefs;
        this.storagePath = prefs.getStoragePath();
    }

    public void run(Task task, ProgressCallback cb) {
        new Thread(() -> runInternal(task, cb)).start();
    }

    private void runInternal(Task task, ProgressCallback cb) {
        LlmClient llm = new LlmClient(prefs.getEndpoint(), prefs.getApiKey(), prefs.getModel());
        List<JSONObject> messages = new ArrayList<>();
        messages.add(LlmClient.msg("system", SYSTEM_PROMPT));
        messages.add(LlmClient.msg("user", "Task: " + task.prompt));

        int maxSteps = prefs.getMaxSteps();
        StringBuilder log = new StringBuilder();
        log.append("Task: ").append(task.prompt).append('\n');

        for (int step = 0; step < maxSteps; step++) {
            final int stepNum = step + 1;
            cb.onStep("Step " + stepNum + "/" + maxSteps + " — thinking…");

            final String[] reply = {null};
            final String[] err   = {null};
            final Object   lock  = new Object();

            llm.chat(messages, new LlmClient.Callback() {
                @Override public void onChunk(String t) { reply[0] = t; }
                @Override public void onDone(String t)  { synchronized(lock) { lock.notifyAll(); } }
                @Override public void onError(String e) { err[0] = e; synchronized(lock) { lock.notifyAll(); } }
            });

            synchronized (lock) {
                try { lock.wait(60000); } catch (InterruptedException ignored) {}
            }

            if (err[0] != null) {
                cb.onError("LLM error: " + err[0]);
                return;
            }
            if (reply[0] == null) {
                cb.onError("LLM timeout");
                return;
            }

            String raw = reply[0].trim();
            log.append("\nStep ").append(stepNum).append(" → ").append(raw).append('\n');
            cb.onStep("Step " + stepNum + ": " + raw);

            try {
                JSONObject action = parseAction(raw);
                String act = action.optString("action", "");

                messages.add(LlmClient.msg("assistant", raw));

                String toolResult;
                switch (act) {
                    case "read_screen":
                        toolResult = readScreen();
                        break;
                    case "tap_text":
                        toolResult = tapText(action.optString("text",""));
                        break;
                    case "tap_coords":
                        toolResult = tapCoords(action.optInt("x",540), action.optInt("y",900));
                        break;
                    case "type_text":
                        toolResult = typeText(action.optString("text",""));
                        break;
                    case "swipe_up":
                        toolResult = swipeUp();
                        break;
                    case "swipe_down":
                        toolResult = swipeDown();
                        break;
                    case "press_back":
                        toolResult = pressBack();
                        break;
                    case "press_home":
                        toolResult = pressHome();
                        break;
                    case "wait":
                        int ms = action.optInt("ms", 500);
                        Thread.sleep(Math.min(ms, 5000));
                        toolResult = "waited " + ms + "ms";
                        break;
                    case "done":
                        String result = action.optString("result", "Task completed.");
                        log.append("\nDONE: ").append(result);
                        saveLog(task, log.toString());
                        cb.onDone(result);
                        return;
                    case "error":
                        String reason = action.optString("reason", "Unknown error");
                        cb.onError(reason);
                        return;
                    default:
                        toolResult = "unknown action: " + act;
                }

                log.append("  result: ").append(toolResult).append('\n');
                messages.add(LlmClient.msg("user", "Action result: " + toolResult));

            } catch (Exception e) {
                messages.add(LlmClient.msg("user", "Parse error: " + e.getMessage() + " — raw: " + raw));
            }
        }

        saveLog(task, log.toString());
        cb.onError("Max steps (" + maxSteps + ") reached without completion.");
    }

    private JSONObject parseAction(String raw) throws Exception {
        // Try to extract JSON from markdown code blocks if present
        String cleaned = raw;
        if (cleaned.contains("```")) {
            int start = cleaned.indexOf('{');
            int end   = cleaned.lastIndexOf('}');
            if (start >= 0 && end > start) cleaned = cleaned.substring(start, end+1);
        }
        return new JSONObject(cleaned.trim());
    }

    /* ── Tool implementations ── */

    private String readScreen() {
        ManusClawAccessibilityService svc = ManusClawAccessibilityService.getInstance();
        if (svc == null) return "[Accessibility service not enabled — user must enable it in Settings → Accessibility → ManusClaw APK]";
        String text = svc.dumpScreen();
        List<String> clickable = svc.listClickable();
        StringBuilder sb = new StringBuilder("Screen text:\n").append(text);
        if (!clickable.isEmpty()) {
            sb.append("\n\nClickable elements:\n");
            for (String c : clickable) sb.append("  - ").append(c).append('\n');
        }
        return sb.toString();
    }

    private String tapText(String text) {
        ManusClawAccessibilityService svc = ManusClawAccessibilityService.getInstance();
        if (svc == null) return "accessibility not available";
        boolean ok = svc.tapByText(text);
        try { Thread.sleep(600); } catch (InterruptedException ignored) {}
        return ok ? "tapped \"" + text + "\"" : "text not found: " + text;
    }

    private String tapCoords(int x, int y) {
        ManusClawAccessibilityService svc = ManusClawAccessibilityService.getInstance();
        if (svc == null) return "accessibility not available";
        boolean ok = svc.tap(x, y);
        try { Thread.sleep(600); } catch (InterruptedException ignored) {}
        return ok ? "tapped (" + x + "," + y + ")" : "tap failed";
    }

    private String typeText(String text) {
        ManusClawAccessibilityService svc = ManusClawAccessibilityService.getInstance();
        if (svc == null) return "accessibility not available";
        boolean ok = svc.typeText(text);
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        return ok ? "typed: " + text : "no focused editable field";
    }

    private String swipeUp() {
        ManusClawAccessibilityService svc = ManusClawAccessibilityService.getInstance();
        if (svc == null) return "accessibility not available";
        svc.swipeUp();
        return "swiped up";
    }

    private String swipeDown() {
        ManusClawAccessibilityService svc = ManusClawAccessibilityService.getInstance();
        if (svc == null) return "accessibility not available";
        svc.swipeDown();
        return "swiped down";
    }

    private String pressBack() {
        ManusClawAccessibilityService svc = ManusClawAccessibilityService.getInstance();
        if (svc == null) return "accessibility not available";
        svc.pressBack();
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        return "back pressed";
    }

    private String pressHome() {
        ManusClawAccessibilityService svc = ManusClawAccessibilityService.getInstance();
        if (svc == null) return "accessibility not available";
        svc.pressHome();
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        return "home pressed";
    }

    private void saveLog(Task task, String log) {
        if (storagePath == null || storagePath.isEmpty()) return;
        try {
            File dir = new File(storagePath, "ManusClaw");
            dir.mkdirs();
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File f = new File(dir, "task_" + ts + ".txt");
            FileWriter fw = new FileWriter(f);
            fw.write(log);
            fw.close();
        } catch (Exception ignored) {}
    }
}
