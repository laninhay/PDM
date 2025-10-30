package com.unir.av1_pdm.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unir.av1_pdm.models.Quote;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {

    private static final String PREFS_NAME = "QuoteFavorites";
    private static final String FAVORITES_KEY = "Favorites";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public FavoritesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Pega a lista atual de favoritos
    public List<Quote> getFavorites() {
        String json = sharedPreferences.getString(FAVORITES_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Quote>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Salva a lista de favoritos
    private void saveFavorites(List<Quote> favorites) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(favorites);
        editor.putString(FAVORITES_KEY, json);
        editor.apply();
    }

    // Adiciona um favorito (se ainda não existir)
    public boolean addFavorite(Quote quote) {
        List<Quote> favorites = getFavorites();
        if (!favorites.contains(quote)) { // Usa o método .equals() que definimos no Quote.java
            favorites.add(quote);
            saveFavorites(favorites);
            return true;
        }
        return false; // Já era favorito
    }

    // Remove um favorito
    public void removeFavorite(Quote quote) {
        List<Quote> favorites = getFavorites();
        favorites.remove(quote); // Usa o método .equals()
        saveFavorites(favorites);
    }

    // Verifica se um item é favorito
    public boolean isFavorite(Quote quote) {
        return getFavorites().contains(quote);
    }
}
