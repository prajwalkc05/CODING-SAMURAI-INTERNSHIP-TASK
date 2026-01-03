package com.example.moviesearchapp;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

// Movie Model Class (POJO)
// Implements Serializable to pass object between Activities
public class Movie implements Serializable {
    
    // SerializedName matches the JSON key from TMDb API
    @SerializedName("title")
    private String title;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("vote_average")
    private float voteAverage;

    @SerializedName("overview")
    private String overview;

    // Getters
    public String getTitle() {
        return title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public float getVoteAverage() {
        return voteAverage;
    }

    public String getOverview() {
        return overview;
    }
}