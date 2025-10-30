package com.unir.av1_pdm.services;

import retrofit2.Retrofit; // A classe principal do Retrofit.
import retrofit2.converter.gson.GsonConverterFactory; // O conversor que usa GSON para transformar JSON em objetos Java (ex: Quote.java).

public class RetrofitClient {

    // A URL base (raiz) de todos os endpoints da API.
    private static final String BASE_URL = "https://programming-quotes-api-pi.vercel.app/";
    // A variável que guardará a instância única (Singleton) do Retrofit.
    private static Retrofit retrofitInstance;

    // Método privado que *cria* a instância do Retrofit, se ela ainda não existir.
    private static Retrofit getRetrofitInstance() {
        // Padrão Singleton: "if (instância == null) { cria a instância }"
        if (retrofitInstance == null) {
            // "Constrói" (Builder) uma nova instância do Retrofit.
            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // 1. Define a URL base.
                    .addConverterFactory(GsonConverterFactory.create()) // 2. Define o conversor (GSON).
                    .build(); // 3. Cria o objeto Retrofit.
        }
        // Retorna a instância (nova ou a já existente).
        return retrofitInstance;
    }

    // O único método público da classe.
    // É o que as Activities (ex: MainActivity) usam para obter o serviço da API.
    public static ApiService getApiService() {
        // 1. Pega a instância do Retrofit (getRetrofitInstance()).
        // 2. ".create(ApiService.class)" -> O Retrofit "lê" a interface ApiService.java
        //    e *cria automaticamente* uma implementação (lógica) para ela.
        return getRetrofitInstance().create(ApiService.class);
    }
}