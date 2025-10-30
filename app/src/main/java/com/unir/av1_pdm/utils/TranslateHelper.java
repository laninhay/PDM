package com.unir.av1_pdm.utils;

import android.os.Handler; // Usado para enviar código de volta para a Main Thread (Thread de UI).
import android.os.Looper; // Acessa a Main Thread.
import org.json.JSONObject; // Usado para "ler" (parse) a resposta JSON manualmente.
import java.io.BufferedReader; // Usado para ler a resposta da conexão.
import java.io.InputStreamReader; // Converte o stream de bytes da resposta em caracteres.
import java.net.HttpURLConnection; // A classe para fazer conexões HTTP.
import java.net.URL; // Representa uma URL.
import java.net.URLEncoder; // Usado para formatar texto para ser seguro em uma URL (ex: " " vira "%20").

public class TranslateHelper {

    // Interface (contrato) de "callback" (retorno).
    // A Activity que chamar o "translate" deve implementar isso
    // para receber a resposta (sucesso ou erro) de forma assíncrona.
    public interface OnTranslateListener {
        void onSuccess(String translatedText);
        void onError(String errorMessage);
    }

    // Método estático (público) para iniciar a tradução.
    public static void translate(final String text, final OnTranslateListener listener) {

        // **MUITO IMPORTANTE**: Cria uma nova Thread (tarefa de fundo).
        // O Android *proíbe* chamadas de rede na "Main Thread" (a thread de UI),
        // pois isso travaria a tela do app.
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                // Codifica o texto para ser seguro na URL.
                String encodedText = URLEncoder.encode(text, "UTF-8");
                // Monta a URL completa da API de tradução.
                String urlString = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=en|pt";
                URL url = new URL(urlString);

                // Abre a conexão HTTP.
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET"); // Define o método como GET.
                // Algumas APIs exigem um "User-Agent" para não bloquearem a requisição.
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) YourApp/1.0");

                // Pega o código de resposta (ex: 200 = OK, 404 = Not Found).
                int responseCode = connection.getResponseCode();
                BufferedReader reader;

                // Se a resposta foi OK (200), lê o fluxo de entrada (sucesso).
                if (responseCode == 200) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    // Se deu erro, lê o fluxo de erro (para ver a mensagem de erro da API).
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }

                // Lê a resposta linha por linha e a constrói em um StringBuilder (mais eficiente que concatenar Strings).
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close(); // Fecha o leitor.

                // Converte a resposta (String) em um objeto JSON para facilitar a leitura.
                JSONObject jsonResponse = new JSONObject(response.toString());
                // Pega o objeto "responseData" de dentro do JSON.
                JSONObject responseData = jsonResponse.getJSONObject("responseData");
                // Pega o campo "translatedText" de dentro do "responseData".
                String translatedText = responseData.getString("translatedText");

                // Se a tradução veio válida...
                if (translatedText != null && !translatedText.trim().isEmpty()) {

                    // **MUITO IMPORTANTE**: Envia o resultado de volta para a Main Thread.
                    // *Apenas* a Main Thread pode atualizar a UI (mostrar Toast, AlertDialog, etc.).
                    new Handler(Looper.getMainLooper()).post(() ->
                            listener.onSuccess(translatedText) // Chama o callback de sucesso.
                    );
                } else {
                    // Se a tradução veio vazia.
                    new Handler(Looper.getMainLooper()).post(() ->
                            listener.onError("Tradução vazia.")
                    );
                }

            } catch (Exception e) {
                // Se ocorreu qualquer erro (rede, JSON inválido, etc.).
                new Handler(Looper.getMainLooper()).post(() ->
                        listener.onError("Erro na tradução: " + e.getMessage())
                );
            } finally {
                // O bloco "finally" é executado *sempre* (com ou sem erro).
                if (connection != null) {
                    connection.disconnect(); // Garante que a conexão será fechada.
                }
            }
        }).start(); // Inicia a Thread (executa a tarefa de fundo).
    }
}