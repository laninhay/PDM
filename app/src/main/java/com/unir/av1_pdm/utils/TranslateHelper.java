package com.unir.av1_pdm.utils;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TranslateHelper {

    public interface OnTranslateListener {
        void onSuccess(String translatedText);
        void onError(String errorMessage);
    }

    public static void translate(final String text, final OnTranslateListener listener) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String encodedText = URLEncoder.encode(text, "UTF-8");
                String urlString = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=en|pt";
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) YourApp/1.0");

                int responseCode = connection.getResponseCode();
                BufferedReader reader;
                if (responseCode == 200) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Log para depuração (opcional)
                // Log.d("TranslateAPI", "Response: " + response.toString());

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject responseData = jsonResponse.getJSONObject("responseData");
                String translatedText = responseData.getString("translatedText");

                if (translatedText != null && !translatedText.trim().isEmpty()) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            listener.onSuccess(translatedText)
                    );
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            listener.onError("Tradução vazia.")
                    );
                }

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        listener.onError("Erro na tradução: " + e.getMessage())
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}