/*
 * Copyright (C) 2016 Fabio Luis
 */

package br.com.fabioluis.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import br.com.fabioluis.popularmovies.entrypoints.rest.movies.MovieFromListRestTmdb;

/**
 * Created by Fabio Luis on 28/09/2016.
 */

public class MoviesAdapter extends CursorAdapter {

    public MoviesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_movies, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String poster = cursor.getString(PopularMoviesFragment.COL_POSTER);

        Picasso.with(context)
                .load(MovieFromListRestTmdb.POSTER_BASE_URL + poster)
                .resize(185, 277)
                .into(viewHolder.imageView);
    }

    public class ViewHolder {
        public final ImageView imageView;

        public ViewHolder(View view) {
            this.imageView = (ImageView) view.findViewById(R.id.list_item_movie);
        }
    }
}
