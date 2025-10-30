package com.unir.av1_pdm.utils;

import android.content.Context; // Necessário para acessar o SharedPreferences.
import android.content.SharedPreferences; // A classe para salvar dados.
import com.google.gson.Gson; // Biblioteca para converter objetos Java <-> String JSON.
import com.google.gson.reflect.TypeToken; // Usado para dizer ao GSON o tipo de uma lista (List<Quote>).
import com.unir.av1_pdm.models.Quote; // O modelo de dados.
import java.lang.reflect.Type; // Usado pelo TypeToken.
import java.util.ArrayList; // A lista.
import java.util.List; // A interface da lista.

// Classe "Helper" (Ajudante). Métodos "static" significam que não precisamos
// criar uma instância dela (ex: new FavoritesHelper()), podemos chamar
// os métodos diretamente (ex: FavoritesHelper.loadFavorites(...)).
public class FavoritesHelper {

    // Constantes (valores fixos) para o nome do arquivo de preferências
    // e a chave onde a lista será salva. Isso evita erros de digitação.
    private static final String PREFS_NAME = "QuotePrefs";
    private static final String FAVORITES_KEY = "Favorites";
    // Instância única do GSON para conversão (é seguro reutilizá-la).
    private static final Gson gson = new Gson();

    // Método privado para *salvar* a lista no disco (SharedPreferences).
    private static void saveFavorites(Context context, List<Quote> favorites) {
        // Pega o arquivo de preferências "QuotePrefs" no modo privado (só o app acessa).
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Pede um "editor" para fazer modificações.
        SharedPreferences.Editor editor = prefs.edit();

        // Converte a lista de objetos (List<Quote>) em uma *única string* no formato JSON.
        // (SharedPreferences só salva tipos primitivos, como String, int, boolean).
        String json = gson.toJson(favorites);

        // Salva a string JSON na chave "Favorites".
        editor.putString(FAVORITES_KEY, json);
        // "apply()" salva as mudanças em segundo plano (assíncrono).
        editor.apply();
    }

    // Método público para *carregar* (ler) a lista do disco.
    public static List<Quote> loadFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Tenta ler a string JSON da chave "Favorites".
        // Se a chave não existir (ex: primeira vez que o app abre), retorna "null".
        String json = prefs.getString(FAVORITES_KEY, null);

        if (json == null) {
            // Se for nulo (nada salvo), retorna uma lista nova e vazia.
            return new ArrayList<>();
        }

        // Se achou uma string JSON, precisamos convertê-la de volta para List<Quote>.
        // Precisamos dizer ao GSON qual é o tipo de destino (uma Lista de Quotes).
        Type type = new TypeToken<ArrayList<Quote>>() {}.getType();

        // Converte a string JSON de volta para o objeto List<Quote>.
        return gson.fromJson(json, type);
    }

    // Método público para *adicionar* um favorito.
    // Retorna "boolean" (true/false) para indicar se o item foi realmente adicionado.
    public static boolean addFavorite(Context context, Quote quote) {
        // 1. Carrega a lista atual salva.
        List<Quote> favorites = loadFavorites(context);

        // 2. Verifica se a citação já *não* está na lista.
        //    (Isso usa o método "Quote.equals()" que definimos no modelo).
        if (!favorites.contains(quote)) {
            // 3. Se não contém, adiciona na lista.
            favorites.add(quote);
            // 4. Salva a lista *modificada* de volta no disco.
            saveFavorites(context, favorites);
            return true; // Retorna true (adicionado com sucesso).
        }
        return false; // Retorna false (já existia).
    }

    // Método público para *remover* um favorito.
    public static void removeFavorite(Context context, Quote quote) {
        // 1. Carrega a lista atual.
        List<Quote> favorites = loadFavorites(context);

        // 2. Verifica se a citação *está* na lista. (O "remove" já faz isso, mas "contains" é uma boa prática).
        if (favorites.contains(quote)) {
            // 3. Remove o item (usando "Quote.equals()").
            favorites.remove(quote);
            // 4. Salva a lista modificada.
            saveFavorites(context, favorites);
        }
    }
}