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

// Declaração da classe, também implementando a interface do adaptador.
public class FavoritesActivity extends AppCompatActivity implements QuoteAdapter.OnQuoteClickListener {

    private RecyclerView recyclerView; // A lista visual.
    private QuoteAdapter adapter; // O adaptador.
    private List<Quote> favoriteQuotes; // A lista de dados (favoritos).

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Liga esta classe ao layout XML "activity_favorites.xml".
        setContentView(R.layout.activity_favorites);

        // Linka a variável do RecyclerView ao componente do XML.
        recyclerView = findViewById(R.id.recyclerViewFavoritos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configura a barra de título (ActionBar).
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_favorites); // Define o título da tela.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Mostra o botão "voltar" (seta) na barra.
        }
    }

    // Método chamado *toda vez* que o usuário retorna para esta tela.
    // É usado "onResume" em vez de "onCreate" para garantir que a lista
    // seja atualizada caso o usuário remova um item e volte.
    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites(); // Chama o método para carregar (ou recarregar) os favoritos.
    }

    // Método privado que carrega os favoritos do SharedPreferences.
    private void loadFavorites() {
        // Carrega a lista salva usando o FavoritesHelper.
        favoriteQuotes = FavoritesHelper.loadFavorites(this);

        // Cria o adaptador.
        // "this" é o listener.
        // "true" indica que esta *é* a lista de favoritos (o adaptador usará o ícone de remover).
        adapter = new QuoteAdapter(favoriteQuotes, this, true);
        recyclerView.setAdapter(adapter);
    }

    // Método OBRIGATÓRIO (da interface).
    // É chamado pelo adaptador ao clicar no ícone de favorito.
    // IMPORTANTE: Na tela de favoritos, este clique significa REMOVER.
    @Override
    public void onFavoriteClick(Quote quote, int position) {
        // Remove o favorito do SharedPreferences (disco).
        FavoritesHelper.removeFavorite(this, quote);
        // Remove o favorito da lista local (memória).
        favoriteQuotes.remove(position);
        // Notifica o adaptador que o item *específico* foi removido (para uma animação suave).
        adapter.notifyItemRemoved(position);
        // Mostra o feedback de remoção.
        Toast.makeText(this, R.string.toast_removed_favorite, Toast.LENGTH_SHORT).show();
    }

    // Método OBRIGATÓRIO (da interface).
    // Exatamente igual ao da MainActivity.
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

    // Exibe o diálogo com a tradução (igual ao da MainActivity).
    private void showTranslationDialog(String author, String translatedText) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.translation_title, author))
                .setMessage(translatedText)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    // Método chamado quando o usuário clica no botão "voltar" (seta) da ActionBar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Simula o clique no botão "voltar" físico do dispositivo.
        return true;
    }
}