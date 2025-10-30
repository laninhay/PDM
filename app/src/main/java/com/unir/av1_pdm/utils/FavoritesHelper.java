package com.unir.av1_pdm.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unir.av1_pdm.models.Quote;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesHelper {

    private static final String PREFS_NAME = "QuotePrefs";
    private static final String FAVORITES_KEY = "Favorites";
    private static final Gson gson = new Gson();

    // Salva a lista inteira de favoritos
    private static void saveFavorites(Context context, List<Quote> favorites) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = gson.toJson(favorites);
        editor.putString(FAVORITES_KEY, json);
        editor.apply();
    }

    // Carrega a lista inteira de favoritos
    public static List<Quote> loadFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(FAVORITES_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Quote>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Adiciona um favorito
    public static boolean addFavorite(Context context, Quote quote) {
        List<Quote> favorites = loadFavorites(context);
        if (!favorites.contains(quote)) {
            favorites.add(quote);
            saveFavorites(context, favorites);
            return true; // Adicionado com sucesso
        }
        return false; // Já existia
    }

    // Remove um favorito
    public static void removeFavorite(Context context, Quote quote) {
        List<Quote> favorites = loadFavorites(context);
        if (favorites.contains(quote)) {
            favorites.remove(quote);
            saveFavorites(context, favorites);
        }
    }
}