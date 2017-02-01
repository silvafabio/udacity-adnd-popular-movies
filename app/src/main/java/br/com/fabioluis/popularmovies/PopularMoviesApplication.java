package br.com.fabioluis.popularmovies;

import android.app.Application;
import android.os.Build;

import br.com.fabioluis.popularmovies.entrypoints.net.NetworkConnectivityBroadcastReceiver;
import br.com.fabioluis.popularmovies.entrypoints.net.NetworkConnectivityCallback;
import br.com.fabioluis.popularmovies.entrypoints.net.NetworkConnectivityListener;

/**
 * Created by silva on 01/02/2017.
 */

public class PopularMoviesApplication extends Application {

    private static PopularMoviesApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized PopularMoviesApplication getInstance(){
        return mInstance;
    }

    public void setConnectivityListener(NetworkConnectivityListener listener){
        if(Build.VERSION.SDK_INT < 21) {
            NetworkConnectivityBroadcastReceiver.networkConnectivityListener = listener;
        } else {
            NetworkConnectivityCallback.register(listener);
        }
    }
}
