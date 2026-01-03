package com.example.moviesearchapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// Retrofit Interface for API calls
public interface ApiService {

    // Search for movies
    @GET("search/movie")
    Call<MovieResponse> searchMovie(
        @Query("api_key") String apiKey,
        @Query("query") String query
    );
}