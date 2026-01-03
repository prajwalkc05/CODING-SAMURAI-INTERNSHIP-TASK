package com.example.moviesearchapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class MovieDetailsActivity extends AppCompatActivity {

    private ImageView ivDetailPoster;
    private TextView tvDetailTitle, tvDetailReleaseDate, tvDetailRating, tvDetailOverview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Initialize UI components
        ivDetailPoster = findViewById(R.id.ivDetailPoster);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailReleaseDate = findViewById(R.id.tvDetailReleaseDate);
        tvDetailRating = findViewById(R.id.tvDetailRating);
        tvDetailOverview = findViewById(R.id.tvDetailOverview);

        // Get Movie object from Intent
        if (getIntent().hasExtra("movie_data")) {
            Movie movie = (Movie) getIntent().getSerializableExtra("movie_data");

            if (movie != null) {
                // Populate data
                tvDetailTitle.setText(movie.getTitle());
                tvDetailReleaseDate.setText("Release Date: " + (movie.getReleaseDate() != null ? movie.getReleaseDate() : "N/A"));
                tvDetailRating.setText("Rating: " + movie.getVoteAverage() + "/10");
                tvDetailOverview.setText(movie.getOverview());

                // Load large poster
                String imageUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(ivDetailPoster);
            }
        }
    }
}