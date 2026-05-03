package com.jddev.manusclawapk.agent;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LlmClient {

    public interface Callback {
        void onChunk(String text);
        void onDone(String fullText);
        void onError(String err);
    }

    private final String endpoint;
    private final String apiKey;
    private final String model;

    public LlmClient(String endpoint, String apiKey, String model) {
        this.endpoint = endpoint;
        this.apiKey   = apiKey;
        this.model    = model;
    }

    public void chat(List<JSONObject> messages, Callback cb) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("model", model);
                body.put("max_tokens", 2048);
                body.put("stream", false);
                JSONArray msgs = new JSONArray();
                for (JSONObject m : messages) msgs.put(m);
                body.put("messages", msgs);

                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);

                byte[] data = body.toString().getBytes(StandardCharsets.UTF_8);
                conn.getOutputStream().write(data);

                int code = conn.getResponseCode();
                InputStream is = code < 400 ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code >= 400) {
                    cb.onError("HTTP " + code + ": " + sb);
                    return;
                }

                JSONObject resp = new JSONObject(sb.toString());
                String text = resp.getJSONArray("choices")
                                  .getJSONObject(0)
                                  .getJSONObject("message")
                                  .getString("content");
                cb.onChunk(text);
                cb.onDone(text);

            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        }).start();
    }

    public static JSONObject msg(String role, String content) {
        try {
            JSONObject o = new JSONObject();
            o.put("role", role);
            o.put("content", content);
            return o;
        } catch (Exception e) { return new JSONObject(); }
    }
}
