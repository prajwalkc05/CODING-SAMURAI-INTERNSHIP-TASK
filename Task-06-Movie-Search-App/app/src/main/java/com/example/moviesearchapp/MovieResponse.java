package com.example.moviesearchapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Response wrapper for TMDb API
public class MovieResponse {
    
    @SerializedName("results")
    private List<Movie> results;

    public List<Movie> getResults() {
        return results;
    }
}