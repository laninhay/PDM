package com.unir.av1_pdm.models;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class Quote {

    @SerializedName("en")
    private String quote;

    @SerializedName("author")
    private String author;

    // Getters
    public String getQuote() {
        return quote;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quote quote1 = (Quote) o;
        return Objects.equals(quote, quote1.quote) &&
                Objects.equals(author, quote1.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quote, author);
    }
}