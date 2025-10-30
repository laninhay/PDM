package com.unir.av1_pdm.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.unir.av1_pdm.R;
import com.unir.av1_pdm.adapter.QuoteAdapter;
import com.unir.av1_pdm.models.Quote;
import com.unir.av1_pdm.utils.FavoritesHelper;
import com.unir.av1_pdm.utils.TranslateHelper;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity implements QuoteAdapter.OnQuoteClickListener {

    private RecyclerView recyclerView;
    private QuoteAdapter adapter;
    private List<Quote> favoriteQuotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites); // Layout que você já tinha

        recyclerView = findViewById(R.id.recyclerViewFavoritos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Título da Activity
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_favorites);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Botão de voltar
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites(); // Carrega/Recarrega os favoritos sempre que a tela é exibida
    }

    private void loadFavorites() {
        favoriteQuotes = FavoritesHelper.loadFavorites(this);
        adapter = new QuoteAdapter(favoriteQuotes, this, true); // true = é lista de favoritos
        recyclerView.setAdapter(adapter);
    }

    // Clique no ícone de favorito (aqui significa REMOVER)
    @Override
    public void onFavoriteClick(Quote quote, int position) {
        FavoritesHelper.removeFavorite(this, quote);
        favoriteQuotes.remove(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(this, R.string.toast_removed_favorite, Toast.LENGTH_SHORT).show();
    }

    // Clique no ícone de traduzir
    @Override
    public void onTranslateClick(Quote quote, int position) {
        TranslateHelper.translate(quote.getQuote(), new TranslateHelper.OnTranslateListener() {
            @Override
            public void onSuccess(String translatedText) {
                showTranslationDialog(quote.getAuthor(), translatedText);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(FavoritesActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Exibe o diálogo com a tradução
    private void showTranslationDialog(String author, String translatedText) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.translation_title, author))
                .setMessage(translatedText)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Trata o clique no botão de voltar da ActionBar
        return true;
    }
}