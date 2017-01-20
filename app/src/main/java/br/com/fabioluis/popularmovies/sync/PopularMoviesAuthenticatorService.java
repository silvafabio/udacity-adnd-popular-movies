package br.com.fabioluis.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by silva on 27/12/2016.
 */

public class PopularMoviesAuthenticatorService extends Service {

    private PopularMoviesAuthenticator mPopularMoviesAuthenticator;

    @Override
    public void onCreate() {
        mPopularMoviesAuthenticator = new PopularMoviesAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mPopularMoviesAuthenticator.getIBinder();
    }
}
