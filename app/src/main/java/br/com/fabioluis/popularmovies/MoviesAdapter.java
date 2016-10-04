/*
 * Copyright (C) 2016 Fabio Luis
 */

package br.com.fabioluis.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.fabioluis.popularmovies.model.Movie;

/**
 * Created by Fabio Luis on 28/09/2016.
 */

public class MoviesAdapter extends ArrayAdapter<Movie> {
    public static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";

    public MoviesAdapter(Context context, List<Movie> movies) {
        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_movies, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_movie);
        Picasso.with(getContext()).load(IMAGE_BASE_URL + movie.getPosterPath()).into(imageView);

        return convertView;
    }
}
