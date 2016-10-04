/*
 * Copyright (C) 2016 Fabio Luis
 */

package br.com.fabioluis.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import br.com.fabioluis.popularmovies.model.Movie;

public class MovieDetails extends AppCompatActivity {
    public static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        setupActionBar();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_movie_details, new PlaceholderFragment())
                    .commit();
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class PlaceholderFragment extends Fragment {
        public PlaceholderFragment() {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_details, container, false);
            Intent intent = getActivity().getIntent();

            if ((intent != null) && (intent.hasExtra("movie"))) {
                Movie movie = intent.getParcelableExtra("movie");

                TextView originalTitle = (TextView) view.findViewById(R.id.movie_original_title);
                originalTitle.setText(movie.getTitle());

                ImageView thumbnail = (ImageView) view.findViewById(R.id.movie_thumbnail);
                Picasso.with(getContext()).load(IMAGE_BASE_URL + movie.getPosterPath()).into(thumbnail);

                String date;

                try {
                    SimpleDateFormat to = new SimpleDateFormat("MM/DD/yyyy");
                    SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-DD");
                    date = to.format(from.parse(movie.getReleaseDate()));
                } catch (ParseException pe) {
                    date = movie.getReleaseDate();
                }

                TextView releaseDate = (TextView) view.findViewById(R.id.movie_release_date);
                releaseDate.setText(date);


                TextView userRating = (TextView) view.findViewById(R.id.movie_user_rating);
                userRating.setText(String.valueOf(movie.getVoteAverage()) + "/10");

                TextView overview = (TextView) view.findViewById(R.id.movie_overview);
                overview.setText(movie.getOverview());
            }

            return view;
        }
    }
}
