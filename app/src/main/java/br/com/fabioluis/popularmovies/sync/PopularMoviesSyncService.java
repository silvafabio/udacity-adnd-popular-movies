package br.com.fabioluis.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by silva on 27/12/2016.
 */

public class PopularMoviesSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static PopularMoviesSyncAdapter sPopularMoviesSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock){
            if(sPopularMoviesSyncAdapter == null){
                sPopularMoviesSyncAdapter = new PopularMoviesSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sPopularMoviesSyncAdapter.getSyncAdapterBinder();
    }
}
