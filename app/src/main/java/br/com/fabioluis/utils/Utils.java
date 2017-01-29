/*
 * Copyright (C) 2016 Fabio Luis
 */

package br.com.fabioluis.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


/**
 * This Class encapsulates the most commonly used methods
 */

public class Utils {
    public static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    public static final String BACKDROP_BASE_URL = "http://image.tmdb.org/t/p/w300/";
    public static final String BACKDROP_BASE_URL_MEDIUM = "http://image.tmdb.org/t/p/w780/";
    public static final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";
    public static final String MOVIES_DB_BASE_URL = "https://api.themoviedb.org/3/movie/";

    public static boolean isOnLine(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return ((networkInfo != null) && (networkInfo.isConnected()));
        }
    }
}
