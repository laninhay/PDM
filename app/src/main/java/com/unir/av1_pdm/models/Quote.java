package com.unir.av1_pdm.models;

import com.google.gson.annotations.SerializedName; // Anotação da biblioteca GSON (usada pelo Retrofit).
import java.util.Objects; // Utilitário para comparar objetos.

// Classe de modelo (POJO - Plain Old Java Object).
public class Quote {

    // Anotação do GSON.
    // Diz ao GSON: "Quando você encontrar um campo chamado 'en' no JSON da API,
    // guarde o valor dele nesta variável 'quote'".
    @SerializedName("en")
    private String quote;

    // "Quando encontrar o campo 'author' no JSON, guarde nesta variável 'author'".
    @SerializedName("author")
    private String author;

    // Métodos "Getters" (padrão Java) para permitir que outras classes
    // leiam os valores destas variáveis (que são "private").
    public String getQuote() {
        return quote;
    }

    public String getAuthor() {
        return author;
    }

    // Método "equals" (sobrescrito).
    // É *essencial* para que métodos como "List.contains(quote)" e "List.remove(quote)"
    // (usados no FavoritesHelper) funcionem corretamente.
    // Ele define o que faz duas instâncias de "Quote" serem consideradas "iguais".
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Se for o mesmo objeto na memória, é igual.
        if (o == null || getClass() != o.getClass()) return false; // Se for de classe diferente, é diferente.
        Quote quote1 = (Quote) o; // Converte o objeto "o" para o tipo "Quote".
        // Compara o conteúdo das variáveis "quote" E "author" para ver se são iguais.
        return Objects.equals(quote, quote1.quote) &&
                Objects.equals(author, quote1.author);
    }

    // Método "hashCode" (sobrescrito).
    // É o "parceiro" obrigatório do "equals".
    // Gera um número (hash) baseado no conteúdo dos campos.
    // É usado por coleções (como ArrayList) para otimizar buscas.
    @Override
    public int hashCode() {
        return Objects.hash(quote, author);
    }
}