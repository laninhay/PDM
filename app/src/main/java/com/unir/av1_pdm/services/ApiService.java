package com.unir.av1_pdm.services;

import com.unir.av1_pdm.models.Quote;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    // Endpoint: https://programming-quotes-api-pi.vercel.app/quotes/author/{authorName}
    @GET("quotes/author/{authorName}")
    Call<List<Quote>> getQuotesByAuthor(
            @Path("authorName") String authorName
    );
}