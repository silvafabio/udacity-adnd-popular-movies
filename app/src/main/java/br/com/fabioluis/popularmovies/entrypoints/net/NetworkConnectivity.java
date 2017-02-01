package br.com.fabioluis.popularmovies.entrypoints.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import br.com.fabioluis.popularmovies.PopularMoviesApplication;

/**
 * Created by silva on 31/01/2017.
 */
public class NetworkConnectivity {

    public static boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) PopularMoviesApplication
                .getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }
}
