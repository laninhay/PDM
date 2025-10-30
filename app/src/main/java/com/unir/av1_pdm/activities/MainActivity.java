// Define o "pacote" (como uma pasta) onde esta classe está localizada.
package com.unir.av1_pdm.activities;

// Importa todas as classes que serão usadas neste arquivo.
import android.content.Intent; // Usado para navegar para outra tela (FavoritesActivity).
import android.os.Bundle; // Usado para salvar o estado da atividade.
import android.util.Log; // Usado para registrar mensagens de debug (Logcat).
import android.widget.Button; // Referência ao componente Botão.
import android.widget.Toast; // Usado para mostrar mensagens curtas (pop-ups).
import androidx.activity.EdgeToEdge; // Permite que o app use a tela inteira (atrás das barras do sistema).
import androidx.appcompat.app.AlertDialog; // Usado para criar caixas de diálogo (pop-ups complexos).
import androidx.appcompat.app.AppCompatActivity; // Classe base para uma tela (Activity) no Android.
import androidx.core.graphics.Insets; // Ajuda a lidar com o espaço das barras do sistema.
import androidx.core.view.ViewCompat; // Utilitário para visualizações (Views).
import androidx.core.view.WindowInsetsCompat; // Ajuda a obter o tamanho das barras do sistema.
import androidx.recyclerview.widget.LinearLayoutManager; // Define como a lista (RecyclerView) irá organizar os itens (verticalmente).
import androidx.recyclerview.widget.RecyclerView; // O componente de lista rolável.
import com.google.android.material.textfield.TextInputEditText; // O campo de texto onde o usuário digita.
import com.unir.av1_pdm.R; // Arquivo gerado pelo Android que contém os IDs de todos os layouts, strings, etc.
import com.unir.av1_pdm.adapter.QuoteAdapter; // O adaptador que gerencia os dados da lista.
import com.unir.av1_pdm.models.Quote; // O modelo de dados (POJO) que representa uma citação.
import com.unir.av1_pdm.services.ApiService; // A interface (Retrofit) que define as chamadas de API.
import com.unir.av1_pdm.services.RetrofitClient; // A classe que configura o Retrofit.
import com.unir.av1_pdm.utils.FavoritesHelper; // Classe utilitária para salvar/ler favoritos.
import com.unir.av1_pdm.utils.TranslateHelper; // Classe utilitária para traduzir o texto.
import java.util.ArrayList; // Estrutura de dados para a lista de citações.
import java.util.List; // Interface para a lista.
import retrofit2.Call; // Objeto do Retrofit que representa uma chamada de API.
import retrofit2.Callback; // Usado para receber a resposta da API (sucesso ou falha).
import retrofit2.Response; // Objeto do Retrofit que contém a resposta da API (dados, código HTTP).

// Declaração da classe MainActivity.
// "extends AppCompatActivity" significa que ela é uma tela.
// "implements QuoteAdapter.OnQuoteClickListener" significa que ela *promete* implementar os métodos
// onFavoriteClick e onTranslateClick, exigidos pela interface do adaptador.
public class MainActivity extends AppCompatActivity implements QuoteAdapter.OnQuoteClickListener {

    // Declaração das variáveis (componentes de UI e dados) que serão usadas na classe.
    private ApiService apiService; // Variável para o serviço da API (Retrofit).
    private RecyclerView recyclerView; // A lista visual.
    private TextInputEditText etTags; // O campo de texto para digitar o autor.
    private Button btnBuscar; // O botão "Buscar".
    private Button btnFavoritos; // O botão "Favoritos".
    private QuoteAdapter adapter; // O adaptador que conecta os dados (lista) ao RecyclerView.
    private List<Quote> currentQuotes = new ArrayList<>(); // A lista de dados (citações) em si.

    // Método principal, chamado quando a tela é criada pela primeira vez.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Chama o método original da classe pai (obrigatório).
        EdgeToEdge.enable(this); // Ativa o modo de tela cheia.
        setContentView(R.layout.activity_main); // "Liga" esta classe Java ao seu arquivo de layout XML (activity_main.xml).

        // Este bloco de código (geralmente padrão) ajusta o preenchimento (padding)
        // da tela para que o conteúdo não fique escondido atrás das barras do sistema (ex: barra de status).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // "Linka" as variáveis Java com os componentes visuais do XML usando seus IDs.
        etTags = findViewById(R.id.etTags);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnFavoritos = findViewById(R.id.btnFavoritos);
        recyclerView = findViewById(R.id.recyclerView);

        // Configura o RecyclerView para exibir os itens em uma lista vertical.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Obtém a instância do serviço de API (Retrofit) pronto para uso.
        apiService = RetrofitClient.getApiService();

        // Cria o adaptador.
        // "this" (este) se refere à própria MainActivity, que implementa a interface de clique.
        // "false" indica que esta *não* é a lista de favoritos (para o ícone de estrela).
        adapter = new QuoteAdapter(currentQuotes, this, false);
        // Define o adaptador criado como o "dono" dos dados do RecyclerView.
        recyclerView.setAdapter(adapter);

        // Define o que acontece quando o botão "Buscar" é clicado.
        btnBuscar.setOnClickListener(v -> {
            // Pega o texto digitado, remove espaços em branco extras (trim).
            String author = etTags.getText().toString().trim();

            // Validação: se o campo estiver vazio...
            if (author.isEmpty()) {
                // Mostra uma mensagem de aviso (Toast).
                Toast.makeText(this, R.string.toast_inform_author, Toast.LENGTH_SHORT).show();
            } else {
                // Se não estiver vazio, chama o método para buscar as citações.
                fetchQuotes(author);
            }
        });

        // Define o que acontece quando o botão "Favoritos" é clicado.
        btnFavoritos.setOnClickListener(v ->
                // Cria uma "Intenção" (Intent) de ir da tela Atual (MainActivity) para a tela de Favoritos (FavoritesActivity).
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class))
        );
    }

    // Método privado responsável por buscar as citações na API.
    private void fetchQuotes(String author) {
        // Formata o nome do autor (remove espaços duplicados).
        String formattedAuthor = author.trim().replaceAll("\\s+", " ");
        if (formattedAuthor.isEmpty()) {
            Toast.makeText(this, R.string.toast_inform_author, Toast.LENGTH_SHORT).show();
            return; // Encerra o método aqui.
        }

        // Capitaliza o nome corretamente (Primeira letra maiúscula, resto minúscula).
        // Isso é uma regra específica desta API que você está usando.
        if (formattedAuthor.length() == 1) {
            formattedAuthor = formattedAuthor.toUpperCase();
        } else {
            formattedAuthor = formattedAuthor.substring(0, 1).toUpperCase() +
                    formattedAuthor.substring(1).toLowerCase();
        }

        // Usa o serviço de API (Retrofit) para chamar o método getQuotesByAuthor.
        // ".enqueue()" faz a chamada de rede de forma *assíncrona* (em uma thread de fundo),
        // para não travar a interface principal do app.
        apiService.getQuotesByAuthor(formattedAuthor).enqueue(new Callback<List<Quote>>() {

            // Callback (retorno) chamado se a API responder com SUCESSO.
            @Override
            public void onResponse(Call<List<Quote>> call, Response<List<Quote>> response) {
                // Logs para depuração (visíveis no Logcat do Android Studio).
                Log.d("API", "Success: " + response.isSuccessful());
                Log.d("API", "Code: " + response.code());
                Log.d("API", "Body: " + response.body());

                // Verifica se a resposta foi bem-sucedida E se o corpo (body) não é nulo E se a lista não está vazia.
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentQuotes.clear(); // Limpa a lista de resultados anteriores.
                    currentQuotes.addAll(response.body()); // Adiciona os novos resultados na lista.
                    adapter.notifyDataSetChanged(); // Avisa o adaptador que os dados mudaram (para atualizar a tela).
                } else {
                    // Se a API não encontrou o autor ou retornou uma lista vazia.
                    Toast.makeText(MainActivity.this, R.string.toast_author_not_found, Toast.LENGTH_SHORT).show();
                }
            }

            // Callback (retorno) chamado se a chamada de rede FALHAR (ex: sem internet, API fora do ar).
            @Override
            public void onFailure(Call<List<Quote>> call, Throwable t) {
                // Loga o erro no Logcat.
                Log.e("API", "Network request failed", t);
                // Mostra uma mensagem de erro para o usuário.
                Toast.makeText(MainActivity.this, getString(R.string.toast_api_error, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método OBRIGATÓRIO (da interface OnQuoteClickListener).
    // Este método é chamado *pelo Adaptador* quando o usuário clica no ícone de favorito.
    @Override
    public void onFavoriteClick(Quote quote, int position) {
        // Tenta adicionar o favorito usando a classe utilitária.
        boolean added = FavoritesHelper.addFavorite(this, quote);

        // Mostra um feedback se foi adicionado ou se já existia.
        Toast.makeText(this,
                added ? R.string.toast_added_favorite : R.string.toast_already_favorite,
                Toast.LENGTH_SHORT).show();
    }

    // Método OBRIGATÓRIO (da interface OnQuoteClickListener).
    // Chamado *pelo Adaptador* quando o usuário clica no ícone de traduzir.
    @Override
    public void onTranslateClick(Quote quote, int position) {
        String originalText = quote.getQuote();

        // Validação de segurança: não tenta traduzir texto nulo, vazio ou inválido.
        if (originalText == null || originalText.trim().isEmpty() || originalText.equals("Citação indisponível")) {
            Toast.makeText(this, "Não é possível traduzir esta citação.", Toast.LENGTH_SHORT).show();
            return; // Encerra o método.
        }

        // Chama o método estático de tradução.
        TranslateHelper.translate(originalText, new TranslateHelper.OnTranslateListener() {

            // Callback de SUCESSO da tradução.
            @Override
            public void onSuccess(String translatedText) {
                if (translatedText != null && !translatedText.trim().isEmpty()) {
                    // Se a tradução veio correta, mostra o diálogo.
                    showTranslationDialog(quote.getAuthor(), translatedText);
                } else {
                    Toast.makeText(MainActivity.this, "Tradução retornou vazia.", Toast.LENGTH_SHORT).show();
                }
            }

            // Callback de ERRO da tradução.
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método privado que cria e exibe o pop-up (AlertDialog) com a tradução.
    private void showTranslationDialog(String author, String translatedText) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.translation_title, author)) // Define o título (ex: "Tradução (Autor)").
                .setMessage(translatedText) // Define o texto principal (a tradução).
                .setPositiveButton(android.R.string.ok, null) // Adiciona um botão "OK" que apenas fecha o diálogo.
                .show(); // Mostra o diálogo na tela.
    }
}