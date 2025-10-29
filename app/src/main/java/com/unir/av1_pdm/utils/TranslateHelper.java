package com.unir.av1_pdm.utils;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Classe utilitária para traduzir texto usando a API Lingva Translate.
 * Exemplo de uso:
 * TranslateHelper.translate("Hello world", new TranslateHelper.OnTranslateListener() {
 *     @Override
 *     public void onSuccess(String translatedText) {
 *         // atualizar UI aqui
 *     }
 *     @Override
 *     public void onError(String errorMessage) {
 *         // tratar erro
 *     }
 * });
 */
public class TranslateHelper {

    public interface OnTranslateListener {
        void onSuccess(String translatedText);
        void onError(String errorMessage);
    }

    public static void translate(final String text, final OnTranslateListener listener) {
        // Executa a tradução em uma thread separada
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String encoded = URLEncoder.encode(text, "UTF-8");
                    URL url = new URL("https://lingva.ml/api/v1/en/pt/" + encoded);
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        result.append(line);
                    }
                    in.close();

                    final String translatedText = result.toString().replace("\"", "").trim();

                    // Volta para a thread principal (UI)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSuccess(translatedText);
                        }
                    });

                } catch (final Exception e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError("Erro na tradução: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }
}

