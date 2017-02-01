package br.com.fabioluis.popularmovies.entrypoints.net;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import br.com.fabioluis.popularmovies.PopularMoviesApplication;

/**
 * Created by silva on 01/02/2017.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NetworkConnectivityCallback extends ConnectivityManager.NetworkCallback {
    public static void register(final NetworkConnectivityListener networkConnectivityListener) {
        ConnectivityManager connectivityManager = (ConnectivityManager) PopularMoviesApplication
                .getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onLost(Network network) {
                networkConnectivityListener.onChange(false);
            }

            @Override
            public void onAvailable(Network network) {
                networkConnectivityListener.onChange(true);
            }
        };
        connectivityManager.registerNetworkCallback(request, callback);
    }
}
