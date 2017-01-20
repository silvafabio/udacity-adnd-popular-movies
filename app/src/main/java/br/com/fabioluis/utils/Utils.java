/*
 * Copyright (C) 2016 Fabio Luis
 */

package br.com.fabioluis.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import br.com.fabioluis.popularmovies.BuildConfig;


/**
 * This Class encapsulates the most commonly used methods
 */

public class Utils {
    public static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    public static final String BACKDROP_BASE_URL = "http://image.tmdb.org/t/p/w300/";
    public static final String BACKDROP_BASE_URL_MEDIUM = "http://image.tmdb.org/t/p/w780/";
    public static final String BACKDROP_BASE_URL_BIG = "http://image.tmdb.org/t/p/w1280/";
    public static final String BACKDROP_BASE_URL_ORIGINAL = "http://image.tmdb.org/t/p/original/";
    public static final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";

    private static final String sLogTag = Utils.class.getSimpleName();
    private static final String sMoviesDbBaseUrl = "https://api.themoviedb.org/3/movie/";
    private static final String sApiLanguage = "pt-BR";
    private static final String sApiRegion = "BR";

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

    private static String getFromApi(Uri uri) {
        HttpsURLConnection httpsUrlConnection = null;
        BufferedReader bufferedReader = null;

        try {
            URL finalUrl = new URL(uri.toString());
            httpsUrlConnection = (HttpsURLConnection) finalUrl.openConnection();
            httpsUrlConnection.setRequestMethod("GET");
            httpsUrlConnection.connect();

            InputStream inputStream = httpsUrlConnection.getInputStream();
            StringBuffer stringBuffer = new StringBuffer();

            if (inputStream == null) {
                return null;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }

            if (stringBuffer.length() == 0) {
                return null;
            }
            return stringBuffer.toString();
        } catch (IOException ioe) {
            Log.e(sLogTag, "Error ", ioe);
            return null;
        } finally {
            if (httpsUrlConnection != null) {
                httpsUrlConnection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (final IOException ioe) {
                    Log.e(sLogTag, "Error closing stream", ioe);
                }
            }
        }
    }

    public static String getMoviesFromApi(String listType) {
        //https://api.themoviedb.org/3/movie/now_playing
        //https://api.themoviedb.org/3/movie/popular
        //https://api.themoviedb.org/3/movie/top_rated
        //https://api.themoviedb.org/3/movie/upcoming
        Uri uri = Uri.parse(sMoviesDbBaseUrl).buildUpon()
                .appendPath(listType)
                .appendQueryParameter("api_key", BuildConfig.MOVIE_DB_API_KEY)
                //.appendQueryParameter("language", sApiLanguage)
                //.appendQueryParameter("region", sApiRegion)
                .build();

        return getFromApi(uri);
    }

    public static String getDataFromApi(String type, String param) {
        Uri uri = Uri.parse(sMoviesDbBaseUrl).buildUpon()
                .appendPath(param)
                .appendPath(type)
                //.appendQueryParameter("language", sApiLanguage)
                .appendQueryParameter("api_key", BuildConfig.MOVIE_DB_API_KEY)
                .build();

        return getFromApi(uri);
    }
}
