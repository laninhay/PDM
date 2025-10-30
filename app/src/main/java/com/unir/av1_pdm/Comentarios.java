package com.unir.av1_pdm;

public class Comentarios {

    /*

    Passo 0: Planejamento e Configuração Inicial
Antes de escrever qualquer código Java, você precisa preparar seu projeto.

Permissão de Internet (Obrigatório para API): Seu app vai acessar a internet (API), então ele precisa de permissão.

Arquivo: app/src/main/AndroidManifest.xml

Adicione esta linha (antes da tag <application>):

XML

<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        ...
    </application>
</manifest>
Adicionar Dependências (As "Ferramentas"): Você precisa dizer ao Gradle quais bibliotecas externas seu app vai usar.

Arquivo: gradle/libs.versions.toml (para organizar as versões)

Arquivo: app/build.gradle.kts (para "importar" as bibliotecas)

No libs.versions.toml:

Ini, TOML

[versions]
# ... (outras versões)
retrofit = "3.0.0" # Define a versão do Retrofit (o nome "retrofit" é um apelido)
recyclerview = "1.4.0" # Versão do RecyclerView

[libraries]
# ... (outras bibliotecas)
# Apelido 'retrofit' (para a biblioteca principal)
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
# Apelido 'gson' (para o conversor de JSON)
gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
# Apelido 'recyclerview'
recyclerview = { group = "androidx.recyclerview", name = "recyclerview", version.ref = "recyclerview" }
No app/build.gradle.kts:

Kotlin

dependencies {
    // ... (appcompat, material, etc.)

    // Dependência para o RecyclerView (listas)
    implementation(libs.recyclerview)

    // Dependências do Retrofit (API)
    implementation(libs.retrofit) // O Retrofit em si
    implementation(libs.gson)    // O conversor GSON (JSON <-> Java)
}
(Após isso, o Android Studio pedirá para "Sync Now". Clique nisso.)

Passo 1: Configurando a API (Retrofit)
Este é o núcleo da busca de dados. Você precisa de 3 arquivos Java para fazer o Retrofit funcionar.

Conceitos-Chave:

O que é uma API? Pense nela como um "garçom" de um restaurante. Você (seu app) não vai até a cozinha (o servidor) pegar os dados. Você faz um pedido (Requisição) ao garçom (API) e ele traz o seu prato (Resposta/Dados).

Como achar Endpoints? Um "Endpoint" é um "prato" específico do menu. Você os encontra na documentação da API. A API que você usou (programming-quotes-api-pi) tem uma documentação que diz: "Se você quiser citações por autor, use o caminho /quotes/author/{authorName}".

O que é @GET? É o método do pedido. @GET é o mais comum, significa "Garçom, me traga (GET) informações". (Outros comuns são @POST = "Garçom, leve este novo dado para a cozinha").

O que é @Path? É um substituto na URL. Em @GET("quotes/author/{authorName}"), o {authorName} é uma variável. @Path("authorName") em um parâmetro de método diz ao Retrofit: "Pegue o valor desta variável Java e coloque-o no lugar de {authorName} na URL".

Os 3 Arquivos de Configuração:

1. O Modelo (POJO) - Ex: MeuPojo.java (No seu app, é Quote.java)

É um "molde" Java que tem exatamente os mesmos campos que o JSON da API.

Java

package com.exemplo.models;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

// POJO = Plain Old Java Object. Apenas armazena dados.
public class MeuPojo {

    // @SerializedName diz ao GSON: "No JSON, o campo chama 'en',
    // mas na minha classe Java, eu quero chamá-lo de 'quote'".
    @SerializedName("en")
    private String quote;

    // Se o nome no JSON for idêntico ao da variável, @SerializedName é opcional.
    @SerializedName("author")
    private String author;

    // Getters para permitir que outras classes leiam os dados.
    public String getQuote() {
        return quote;
    }

    public String getAuthor() {
        return author;
    }

    // (Importante ter .equals() e .hashCode() se você for usar em listas
    // para verificar se um item já existe, como no seu FavoritesHelper)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeuPojo meuPojo = (MeuPojo) o;
        return Objects.equals(quote, meuPojo.quote) && Objects.equals(author, meuPojo.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quote, author);
    }
}
2. A Interface da API - Ex: ApiService.java

O "contrato" ou "menu" que lista os Endpoints que seu app vai usar.

Java

package com.exemplo.services;

import com.exemplo.models.MeuPojo; // O "molde" que esperamos receber
import java.util.List; // Esperamos uma *lista* desses moldes
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    // @GET: Método HTTP. O caminho é *relativo* à BASE_URL.
    @GET("quotes/author/{authorName}")
    Call<List<MeuPojo>> getQuotesByAuthor(

        // @Path: Diz ao Retrofit para substituir "{authorName}" na URL
        // pelo valor da variável "authorName" passada para este método.
        @Path("authorName") String authorName
    );

    // --- Exemplo de outro tipo de Endpoint ---
    // Se a API fosse: https://api.com/quotes?tags=technology
    // O "?tags=technology" é uma "Query Parameter".
    // Em Retrofit, seria:
    //
    // @GET("quotes")
    // Call<List<MeuPojo>> getQuotesByTag(
    //     @Query("tags") String tagName
    // );
}
3. O Cliente Retrofit - Ex: RetrofitClient.java

A "fábrica" que constrói o Retrofit (usando o padrão Singleton).

Java

package com.exemplo.services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // 1. A URL base (raiz) de todos os Endpoints da API.
    private static final String BASE_URL = "https://programming-quotes-api-pi.vercel.app/";

    // 2. A instância única (Singleton) do Retrofit.
    private static Retrofit retrofitInstance;

    // 3. O método que "constrói" a instância (se ela não existir).
    private static Retrofit getRetrofitInstance() {
        if (retrofitInstance == null) {
            // Se for a primeira vez, cria a instância.
            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // Define a URL base
                    .addConverterFactory(GsonConverterFactory.create()) // Define o conversor (JSON -> Java)
                    .build(); // Constrói
        }
        return retrofitInstance; // Retorna a instância (nova ou a antiga)
    }

    // 4. O único método público, que as Activities usarão.
    public static ApiService getApiService() {
        // Pede ao Retrofit para "implementar" nossa interface ApiService.
        return getRetrofitInstance().create(ApiService.class);
    }
}
Passo 2: Configurando a Lista (RecyclerView + Adapter)
Agora que temos como buscar os dados, precisamos de um lugar para exibi-los.

1. O Layout do Item - Ex: item_meu_layout.xml (No seu app, é item_quote.xml)

Este XML representa como um item da lista vai se parecer.

XML

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:id="@+id/txtItemContent" android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textSize="18sp"
        android:textStyle="italic" />

    <TextView
        android:id="@+id/txtItemAuthor" android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:textColor="#757575" />

    <ImageButton
        android:id="@+id/btnItemFavorite" android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/ic_favorite_add" />

</LinearLayout>
2. O Layout da Activity - Ex: activity_main.xml

O layout da tela principal, que contém o RecyclerView.

XML

<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/meuRecyclerView" android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintTop_toBottomOf="@+id/btnBuscar" app:layout_constraintBottom_toBottomOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
3. O Adaptador (Adapter) - Ex: MeuAdapter.java (No seu app, é QuoteAdapter.java)

Esta é a classe mais importante para listas. É o "gerente" que conecta seus dados (a List<MeuPojo>) ao RecyclerView.

Java

package com.exemplo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.exemplo.R; // R.id.xxx, R.layout.xxx
import com.exemplo.models.MeuPojo;
import java.util.List;

// 1. A classe DEVE herdar (extends) de RecyclerView.Adapter
//    e especificar qual ViewHolder ela usará (MeuViewHolder, criado abaixo).
public class MeuAdapter extends RecyclerView.Adapter<MeuAdapter.MeuViewHolder> {

    // 2. Os dados que o adapter vai gerenciar (a lista de Pojos).
    private List<MeuPojo> listaDeDados;
    // (Opcional, mas recomendado) Um "Ouvinte" (Listener) para os cliques
    private OnItemClickListener listener;

    // 3. (Opcional) Interface para cliques
    // A Activity vai "implementar" isso para saber quando um clique aconteceu.
    public interface OnItemClickListener {
        void onFavoriteClick(MeuPojo item);
        // (Pode adicionar outros cliques, ex: onTranslateClick, onItemClick)
    }

    // 4. Construtor: Recebe a lista de dados e o "ouvinte" (a Activity).
    public MeuAdapter(List<MeuPojo> listaDeDados, OnItemClickListener listener) {
        this.listaDeDados = listaDeDados;
        this.listener = listener;
    }

    // 5. MÉTODO OBRIGATÓRIO 1: onCreateViewHolder
    //    Chamado pelo RecyclerView quando ele precisa criar um *novo* item visual.
    //    (Ele só cria alguns; depois, ele os reutiliza).
    @NonNull
    @Override
    public MeuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Infla" (cria) o layout XML do *item* (item_meu_layout.xml)
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meu_layout, parent, false);

        // Retorna um novo ViewHolder "segurando" esse item.
        return new MeuViewHolder(itemView);
    }

    // 6. MÉTODO OBRIGATÓRIO 2: onBindViewHolder
    //    Chamado pelo RecyclerView quando ele quer *preencher* um item
    //    com os dados de uma posição específica.
    @Override
    public void onBindViewHolder(@NonNull MeuViewHolder holder, int position) {
        // Pega o objeto de dados correto da lista.
        MeuPojo itemAtual = listaDeDados.get(position);

        // Chama o método "bind" do ViewHolder para preencher a UI.
        holder.bind(itemAtual, listener);
    }

    // 7. MÉTODO OBRIGATÓRIO 3: getItemCount
    //    Chamado pelo RecyclerView para saber quantos itens existem *no total*.
    @Override
    public int getItemCount() {
        return listaDeDados.size();
    }


    // 8. O ViewHolder (Classe Interna)
    //    É um "porta-objetos" (Holder) que "segura" (holds) os componentes
    //    de UI (TextViews, etc.) de *um* item da lista (item_meu_layout.xml).
    //    Isso evita o uso repetitivo e lento do "findViewById".
    class MeuViewHolder extends RecyclerView.ViewHolder {

        // 9. Declara os componentes de UI que estão no XML do item.
        TextView txtContent;
        TextView txtAuthor;
        ImageButton btnFavorite;

        // 10. Construtor do ViewHolder.
        public MeuViewHolder(@NonNull View itemView) {
            super(itemView);

            // 11. Faz o "findViewById" *apenas uma vez* (aqui).
            txtContent = itemView.findViewById(R.id.txtItemContent);
            txtAuthor = itemView.findViewById(R.id.txtItemAuthor);
            btnFavorite = itemView.findViewById(R.id.btnItemFavorite);
        }

        // 12. (Opcional, mas recomendado) Método "bind" (ligar).
        //     Preenche os componentes de UI com os dados do objeto Pojo.
        public void bind(MeuPojo item, OnItemClickListener listener) {
            txtContent.setText(item.getQuote());
            txtAuthor.setText(item.getAuthor());

            // 13. Configura os cliques
            btnFavorite.setOnClickListener(v -> {
                // Quando clicado, avisa o "ouvinte" (a Activity)
                // passando o item que foi clicado.
                listener.onFavoriteClick(item);
            });
        }
    }
}
Passo 3: Ligando a API e o RecyclerView na Activity
Agora, na MainActivity.java, vamos juntar tudo.

Java

package com.exemplo.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager; // Importante
import androidx.recyclerview.widget.RecyclerView;
import com.exemplo.R;
import com.exemplo.adapter.MeuAdapter; // Nosso Adapter
import com.exemplo.models.MeuPojo; // Nosso Pojo
import com.exemplo.services.ApiService; // Nossa Interface API
import com.exemplo.services.RetrofitClient; // Nosso Cliente Retrofit
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 1. A Activity deve "implementar" a interface de clique do Adapter
public class MainActivity extends AppCompatActivity implements MeuAdapter.OnItemClickListener {

    // 2. Variáveis de classe
    private RecyclerView meuRecyclerView;
    private MeuAdapter meuAdapter;
    private List<MeuPojo> minhaListaDeDados = new ArrayList<>(); // Lista local
    private ApiService apiService; // Para chamar a API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Liga ao XML

        // 3. Inicializa o serviço de API
        apiService = RetrofitClient.getApiService();

        // 4. Configura o RecyclerView
        // a. Acha o RecyclerView no layout
        meuRecyclerView = findViewById(R.id.meuRecyclerView);

        // b. Cria o Adapter (passando a lista e "this" (a Activity) como ouvinte)
        meuAdapter = new MeuAdapter(minhaListaDeDados, this);

        // c. Define o Gerenciador de Layout (LayoutManager)
        // (Obrigatório! Diz ao RecyclerView como organizar os itens: vertical, horizontal, grid)
        meuRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Lista vertical

        // d. Define o Adapter no RecyclerView
        meuRecyclerView.setAdapter(meuAdapter);

        // 5. (Exemplo) Busca os dados quando a tela é criada
        buscarDadosDaApi("algumAutor"); // Chama o método de busca
    }

    // 6. Método para buscar os dados (a chamada assíncrona)
    private void buscarDadosDaApi(String autor) {
        // Usa o ApiService para criar a "chamada"
        Call<List<MeuPojo>> call = apiService.getQuotesByAuthor(autor);

        // 7. ".enqueue()" - A Mágica do Retrofit
        //    Executa a chamada de rede em uma Thread de Fundo (não trava a UI).
        //    Quando a resposta (boa ou ruim) chegar, ela chama um dos callbacks abaixo.
        call.enqueue(new Callback<List<MeuPojo>>() {

            // 8. CHAMADO EM CASO DE SUCESSO (API respondeu, código 200-299)
            @Override
            public void onResponse(Call<List<MeuPojo>> call, Response<List<MeuPojo>> response) {
                // Verifica se a resposta foi bem-sucedida E se ela tem dados (body)
                if (response.isSuccessful() && response.body() != null) {

                    // 9. Atualiza a lista local
                    minhaListaDeDados.clear(); // Limpa dados antigos
                    minhaListaDeDados.addAll(response.body()); // Adiciona os novos dados

                    // 10. AVISA O ADAPTER QUE OS DADOS MUDARAM!
                    // (Obrigatório! Isso faz a tela redesenhar a lista)
                    meuAdapter.notifyDataSetChanged();

                } else {
                    // API respondeu, mas com erro (ex: 404 - Não Encontrado)
                    Toast.makeText(MainActivity.this, "Autor não encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            // 11. CHAMADO EM CASO DE FALHA (Ex: Sem internet, API fora do ar)
            @Override
            public void onFailure(Call<List<MeuPojo>> call, Throwable t) {
                // Mostra o erro para o usuário (ou loga no Logcat)
                Toast.makeText(MainActivity.this, "Falha na rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 12. MÉTODO OBRIGATÓRIO (da interface OnItemClickListener)
    //     Isto é o que acontece quando o Adapter "avisa" que um clique ocorreu.
    @Override
    public void onFavoriteClick(MeuPojo item) {
        // Aqui você colocaria a lógica de salvar nos favoritos (SharedPreferences)
        Toast.makeText(this, "Favoritando: " + item.getAuthor(), Toast.LENGTH_SHORT).show();

        // (Chamaria o Passo 5 - SharedPreferences)
        // Ex: FavoritesHelper.addFavorite(this, item);
    }
}
Passo 4: Navegando entre Telas (Intent)
Intent (Intenção) é como o Android lida com navegação e ações.

Crie a Segunda Activity:

Clique com o botão direito na pasta activities > New > Activity > Empty Views Activity.

Dê um nome, ex: FavoritesActivity.

O Android Studio cria FavoritesActivity.java e activity_favorites.xml.

Registre a Activity no Manifesto (Automático, mas verifique):

O Android Studio deve fazer isso automaticamente, mas é bom saber.

Arquivo: app/src/main/AndroidManifest.xml

Veja se a nova activity está lá:

XML

<application ...>
    <activity
        android:name=".activities.MainActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>

    <activity
        android:name=".activities.FavoritesActivity"
        android:exported="false" /> </application>
O Código da Chamada (Intent):

Na MainActivity.java, coloque isso no clique de um botão:

Java

// Na MainActivity.java

// Supondo que você tenha um "btnIrParaFavoritos"
Button btnIrParaFavoritos = findViewById(R.id.btnFavoritos);

btnIrParaFavoritos.setOnClickListener(v -> {

    // 1. Cria a "Intenção" (Intent)
    //    Dizendo "Eu quero ir DAQUI (MainActivity.this)
    //    PARA LÁ (FavoritesActivity.class)"
    Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);

    // 2. (Opcional) Enviar dados para a próxima tela
    //    Ex: Enviar o nome do autor que foi buscado
    // intent.putExtra("CHAVE_DO_DADO", "Valor a ser enviado");

    // 3. Executa a intenção (inicia a nova tela)
    startActivity(intent);
});

// --- Para "receber" o dado na FavoritesActivity ---
// no onCreate() da FavoritesActivity:
// String autor = getIntent().getStringExtra("CHAVE_DO_DADO");
Passo 5: Salvando Dados (SharedPreferences)
Usado para salvar dados simples e pequenos (configurações, login, listas pequenas). Para listas complexas, usamos o GSON para converter a List<MeuPojo> em uma String JSON (pois o SharedPreferences só salva Strings, e não Listas).

O seu FavoritesHelper.java é o exemplo perfeito e completo desta lógica.

Ex: FavoritesHelper.java (Genérico)

Java

package com.exemplo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.exemplo.models.MeuPojo;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesHelper {

    // 1. Constantes para evitar erros de digitação
    private static final String PREFS_NAME = "MeuAppPrefs";
    private static final String FAVORITES_KEY = "MeusFavoritos";
    // 2. Instância do GSON (converte Java <-> JSON)
    private static final Gson gson = new Gson();

    // 3. MÉTODO PARA SALVAR
    private static void saveFavorites(Context context, List<MeuPojo> favorites) {
        // Pega o "arquivo" de preferências
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Pede um "editor" para modificar
        SharedPreferences.Editor editor = prefs.edit();

        // Converte a Lista<MeuPojo> em UMA String JSON
        String json = gson.toJson(favorites);

        // Salva a String JSON na chave "FAVORITES_KEY"
        editor.putString(FAVORITES_KEY, json);

        // Aplica (salva) as mudanças
        editor.apply();
    }

    // 4. MÉTODO PARA CARREGAR
    public static List<MeuPojo> loadFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Tenta ler a String JSON. Se não existir, retorna "null".
        String json = prefs.getString(FAVORITES_KEY, null);

        if (json == null) {
            // Se for nulo (app abriu pela 1ª vez), retorna uma lista nova e vazia.
            return new ArrayList<>();
        }

        // Se achou um JSON, converte de volta para List<MeuPojo>
        // (Precisa do TypeToken para dizer ao GSON que é uma *Lista* de Pojos)
        Type type = new TypeToken<ArrayList<MeuPojo>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // 5. MÉTODOS "HELPER" (AJUDANTES)

    // Adiciona um item (retorna true se foi adicionado, false se já existia)
    public static boolean addFavorite(Context context, MeuPojo item) {
        // Carrega a lista atual
        List<MeuPojo> favorites = loadFavorites(context);

        // Verifica se o item JÁ NÃO ESTÁ na lista (usa o .equals() do Pojo)
        if (!favorites.contains(item)) {
            favorites.add(item); // Adiciona
            saveFavorites(context, favorites); // Salva a lista modificada
            return true;
        }
        return false; // Já existia
    }

    // Remove um item
    public static void removeFavorite(Context context, MeuPojo item) {
        List<MeuPojo> favorites = loadFavorites(context);
        // Remove o item (usa o .equals() do Pojo)
        favorites.remove(item);
        // Salva a lista modificada
        saveFavorites(context, favorites);
    }
}

     */

}
