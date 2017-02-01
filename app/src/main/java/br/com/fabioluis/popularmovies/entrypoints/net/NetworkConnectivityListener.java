package br.com.fabioluis.popularmovies.entrypoints.net;

/**
 * Created by silva on 01/02/2017.
 */

public interface NetworkConnectivityListener {

    void onChange(boolean isConnected);
}
