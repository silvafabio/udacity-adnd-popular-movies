package br.com.fabioluis.popularmovies.entrypoints.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by silva on 01/02/2017.
 */

public class NetworkConnectivityBroadcastReceiver extends BroadcastReceiver {
    public static NetworkConnectivityListener networkConnectivityListener;

    public NetworkConnectivityBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isConnected;
        if (connectivityManager == null) {
            isConnected = false;
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            isConnected = networkInfo != null && networkInfo.isConnected();
        }

        if (networkConnectivityListener != null) {
            networkConnectivityListener.onChange(isConnected);
        }
    }
}
