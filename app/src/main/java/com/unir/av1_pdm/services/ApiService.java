package com.unir.av1_pdm.services;

import com.unir.av1_pdm.models.Quote; // O modelo de dados (o que esperamos receber).
import java.util.List; // Esperamos receber uma *lista* de citações.
import retrofit2.Call; // O objeto de chamada do Retrofit.
import retrofit2.http.GET; // Anotação: Indica que é uma chamada HTTP GET.
import retrofit2.http.Path; // Anotação: Indica que uma variável do método substituirá um pedaço da URL.

public interface ApiService {

    // Comentário útil indicando o formato do endpoint completo.
    // Endpoint: https://programming-quotes-api-pi.vercel.app/quotes/author/{authorName}

    // Define um método para a chamada.
    // @GET: A chamada será do tipo GET para o caminho "quotes/author/{authorName}"
    // (o Retrofit irá juntar isso com a BASE_URL definida no RetrofitClient).
    @GET("quotes/author/{authorName}")

    // Declaração do método:
    // Retorna: um objeto "Call" que, quando executado, trará uma "List<Quote>".
    // Nome do método: getQuotesByAuthor
    // Parâmetro:
    // @Path("authorName"): Indica que o valor da variável "authorName"
    // deve substituir a parte "{authorName}" da URL no @GET.
    Call<List<Quote>> getQuotesByAuthor(
            @Path("authorName") String authorName
    );
}