package com.unir.av1_pdm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.unir.av1_pdm.R;
import com.unir.av1_pdm.models.Quote;
import java.util.List;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {

    private final List<Quote> quoteList;
    private final OnQuoteClickListener listener;
    private final boolean isFavoriteList; // Flag para mudar o ícone

    // Interface para os cliques
    public interface OnQuoteClickListener {
        void onFavoriteClick(Quote quote, int position);
        void onTranslateClick(Quote quote, int position);
    }

    public QuoteAdapter(List<Quote> quoteList, OnQuoteClickListener listener, boolean isFavoriteList) {
        this.quoteList = quoteList;
        this.listener = listener;
        this.isFavoriteList = isFavoriteList;
    }

    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quote, parent, false);
        return new QuoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        Quote quote = quoteList.get(position);
        holder.bind(quote, listener, isFavoriteList);
    }

    @Override
    public int getItemCount() {
        return quoteList.size();
    }

    // ViewHolder
    static class QuoteViewHolder extends RecyclerView.ViewHolder {

        TextView txtContent;
        TextView txtAuthor;
        ImageButton btnFavorite;
        ImageButton btnTranslate;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtAuthor = itemView.findViewById(R.id.txtAuthor);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnTranslate = itemView.findViewById(R.id.btnTranslate);
        }

        public void bind(final Quote quote, final OnQuoteClickListener listener, boolean isFavoriteList) {

            // --- CORREÇÃO 2: Verificação de 'null' ---
            String content = (quote.getQuote() != null) ? quote.getQuote() : "Citação indisponível";
            String author = (quote.getAuthor() != null) ? quote.getAuthor() : "Autor desconhecido";

            txtContent.setText(String.format("“%s”", content));
            txtAuthor.setText(String.format("- %s", author));
            // --- FIM DA CORREÇÃO 2 ---

            // Define o ícone de favorito (adicionar ou remover)
            if (isFavoriteList) {
                btnFavorite.setImageResource(R.drawable.ic_favorite_remove);
            } else {
                btnFavorite.setImageResource(R.drawable.ic_favorite_add);
            }

            // Define os listeners de clique
            btnFavorite.setOnClickListener(v -> listener.onFavoriteClick(quote, getAdapterPosition()));
            btnTranslate.setOnClickListener(v -> listener.onTranslateClick(quote, getAdapterPosition()));
        }
    }
}