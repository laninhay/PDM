package com.unir.av1_pdm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.unir.av1_pdm.R;
import com.unir.av1_pdm.adapter.QuoteAdapter;
import com.unir.av1_pdm.models.Quote;
import com.unir.av1_pdm.services.ApiService;
import com.unir.av1_pdm.services.RetrofitClient;
import com.unir.av1_pdm.utils.FavoritesHelper;
import com.unir.av1_pdm.utils.TranslateHelper;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements QuoteAdapter.OnQuoteClickListener {

    private ApiService apiService;
    private RecyclerView recyclerView;
    private TextInputEditText etTags;
    private Button btnBuscar;
    private Button btnFavoritos;
    private QuoteAdapter adapter;
    private List<Quote> currentQuotes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etTags = findViewById(R.id.etTags);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnFavoritos = findViewById(R.id.btnFavoritos);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        apiService = RetrofitClient.getApiService();

        adapter = new QuoteAdapter(currentQuotes, this, false);
        recyclerView.setAdapter(adapter);

        btnBuscar.setOnClickListener(v -> {
            String author = etTags.getText().toString().trim();
            if (author.isEmpty()) {
                Toast.makeText(this, R.string.toast_inform_author, Toast.LENGTH_SHORT).show();
            } else {
                fetchQuotes(author);
            }
        });

        btnFavoritos.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class))
        );
    }

    private void fetchQuotes(String author) {
        // Formata o nome do autor com segurança
        String formattedAuthor = author.trim().replaceAll("\\s+", " ");
        if (formattedAuthor.isEmpty()) {
            Toast.makeText(this, R.string.toast_inform_author, Toast.LENGTH_SHORT).show();
            return;
        }

        // Capitaliza corretamente (evita crash em nomes de 1 caractere)
        if (formattedAuthor.length() == 1) {
            formattedAuthor = formattedAuthor.toUpperCase();
        } else {
            formattedAuthor = formattedAuthor.substring(0, 1).toUpperCase() +
                    formattedAuthor.substring(1).toLowerCase();
        }

        apiService.getQuotesByAuthor(formattedAuthor).enqueue(new Callback<List<Quote>>() {
            @Override
            public void onResponse(Call<List<Quote>> call, Response<List<Quote>> response) {
                // ✅ Sempre loga a resposta real
                Log.d("API", "Success: " + response.isSuccessful());
                Log.d("API", "Code: " + response.code());
                Log.d("API", "Body: " + response.body());

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentQuotes.clear();
                    currentQuotes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_author_not_found, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Quote>> call, Throwable t) {
                // ✅ Log correto do erro (sem NullPointerException)
                Log.e("API", "Network request failed", t);
                Toast.makeText(MainActivity.this, getString(R.string.toast_api_error, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFavoriteClick(Quote quote, int position) {
        boolean added = FavoritesHelper.addFavorite(this, quote);
        Toast.makeText(this,
                added ? R.string.toast_added_favorite : R.string.toast_already_favorite,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTranslateClick(Quote quote, int position) {
        String originalText = quote.getQuote();
        // ✅ Evita traduzir textos inválidos
        if (originalText == null || originalText.trim().isEmpty() || originalText.equals("Citação indisponível")) {
            Toast.makeText(this, "Não é possível traduzir esta citação.", Toast.LENGTH_SHORT).show();
            return;
        }

        TranslateHelper.translate(originalText, new TranslateHelper.OnTranslateListener() {
            @Override
            public void onSuccess(String translatedText) {
                if (translatedText != null && !translatedText.trim().isEmpty()) {
                    showTranslationDialog(quote.getAuthor(), translatedText);
                } else {
                    Toast.makeText(MainActivity.this, "Tradução retornou vazia.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTranslationDialog(String author, String translatedText) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.translation_title, author))
                .setMessage(translatedText)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}