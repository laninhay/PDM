package com.unir.av1_pdm.adapter;

import android.view.LayoutInflater; // Usado para "inflar" (criar) um layout XML e transformá-lo em um objeto View.
import android.view.View; // A classe base para todos os componentes visuais (botões, textos, etc.).
import android.view.ViewGroup; // Um contêiner de Views (como o LinearLayout).
import android.widget.ImageButton; // Um botão que contém apenas uma imagem.
import android.widget.TextView; // Um componente para exibir texto.
import androidx.annotation.NonNull; // Anotação que indica que um valor/retorno não pode ser nulo.
import androidx.recyclerview.widget.RecyclerView; // A classe base para o Adaptador.
import com.unir.av1_pdm.R; // Acesso aos IDs.
import com.unir.av1_pdm.models.Quote; // O modelo de dados.
import java.util.List; // A lista de dados.

// A classe herda (extends) do Adaptador padrão do RecyclerView.
// Ela usa um "ViewHolder" (QuoteViewHolder) para gerenciar os itens individuais.
public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {

    // Variáveis finais (não podem ser alteradas após a criação do adaptador).
    private final List<Quote> quoteList; // A lista de dados (citações).
    private final OnQuoteClickListener listener; // A referência para a Activity (MainActivity ou FavoritesActivity) que "ouve" os cliques.
    private final boolean isFavoriteList; // Flag para saber qual ícone de favorito mostrar.

    // Interface (contrato) que define quais métodos a Activity (o "listener")
    // deve implementar para receber os eventos de clique do adaptador.
    public interface OnQuoteClickListener {
        void onFavoriteClick(Quote quote, int position);
        void onTranslateClick(Quote quote, int position);
    }

    // Construtor do Adaptador.
    // Recebe a lista de dados, o "listener" (a Activity) e a flag.
    public QuoteAdapter(List<Quote> quoteList, OnQuoteClickListener listener, boolean isFavoriteList) {
        this.quoteList = quoteList;
        this.listener = listener;
        this.isFavoriteList = isFavoriteList;
    }

    // Método chamado pelo RecyclerView quando ele precisa criar um *novo* layout de item
    // (ex: quando os primeiros itens aparecem na tela).
    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Infla" o XML "item_quote.xml" e o transforma em um objeto View.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quote, parent, false);
        // Retorna um novo ViewHolder (o controlador do item) contendo a View inflada.
        return new QuoteViewHolder(view);
    }

    // Método chamado pelo RecyclerView quando ele precisa *reutilizar* um layout de item
    // para mostrar novos dados (ex: ao rolar a tela, um item que saiu de cima é
    // reutilizado para mostrar um novo item que está entrando embaixo).
    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        // Pega o objeto de dados (Quote) correto para esta posição na lista.
        Quote quote = quoteList.get(position);
        // Chama o método "bind" do ViewHolder para preencher o layout com os dados desta citação.
        holder.bind(quote, listener, isFavoriteList);
    }

    // Método chamado pelo RecyclerView para saber quantos itens existem *no total* na lista.
    @Override
    public int getItemCount() {
        return quoteList.size();
    }

    // Classe interna (static) que representa *um* item visual da lista (um card).
    // Ela "segura" (holds) as referências para os componentes de UI dentro do item (os TextViews, os botões).
    static class QuoteViewHolder extends RecyclerView.ViewHolder {

        // Variáveis para os componentes *dentro* do "item_quote.xml".
        TextView txtContent;
        TextView txtAuthor;
        ImageButton btnFavorite;
        ImageButton btnTranslate;

        // Construtor do ViewHolder.
        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView); // "itemView" é o layout (LinearLayout) raiz do "item_quote.xml".

            // Linka as variáveis Java com os componentes do XML.
            txtContent = itemView.findViewById(R.id.txtContent);
            txtAuthor = itemView.findViewById(R.id.txtAuthor);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnTranslate = itemView.findViewById(R.id.btnTranslate);
        }

        // Método que preenche os dados (bind) no layout do item.
        public void bind(final Quote quote, final OnQuoteClickListener listener, boolean isFavoriteList) {

            // --- CORREÇÃO: Verificação de 'null' ---
            // Verifica se os dados da API não vieram nulos antes de usá-los,
            // evitando um "crash" (NullPointerException).
            String content = (quote.getQuote() != null) ? quote.getQuote() : "Citação indisponível";
            String author = (quote.getAuthor() != null) ? quote.getAuthor() : "Autor desconhecido";

            // Define os textos, formatando-os (com aspas e hífen).
            txtContent.setText(String.format("“%s”", content));
            txtAuthor.setText(String.format("- %s", author));
            // --- FIM DA CORREÇÃO ---

            // Define o ícone de favorito correto (adicionar ou remover)
            // baseado na flag "isFavoriteList".
            if (isFavoriteList) {
                // Se estamos na lista de favoritos, mostra o ícone de remover (estrela cheia).
                btnFavorite.setImageResource(R.drawable.ic_favorite_remove);
            } else {
                // Se estamos na lista principal, mostra o ícone de adicionar (estrela vazia).
                btnFavorite.setImageResource(R.drawable.ic_favorite_add);
            }

            // Define os listeners de clique para os botões.
            // Quando o botão é clicado...
            btnFavorite.setOnClickListener(v ->
                    // ...chama o método "onFavoriteClick" do "listener" (que é a Activity).
                    // O adaptador não sabe o *que* fazer (adicionar ou remover), ele só
                    // *avisa* a Activity que o clique aconteceu.
                    listener.onFavoriteClick(quote, getAdapterPosition())
            );

            // Mesma lógica para o botão de traduzir.
            btnTranslate.setOnClickListener(v ->
                    listener.onTranslateClick(quote, getAdapterPosition())
            );
        }
    }
}