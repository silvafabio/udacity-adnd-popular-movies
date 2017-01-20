/*
 * Copyright (C) 2016 Fabio Luis
 */

package br.com.fabioluis.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import br.com.fabioluis.popularmovies.sync.PopularMoviesSyncAdapter;

public class MainActivity extends AppCompatActivity implements PopularMoviesFragment.Callback {

    private static final String sPopularMoviesDetailsFragmentTag = "DFTAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailsFragment(), sPopularMoviesDetailsFragmentTag)
                        .commit();
            }

            getSupportActionBar().setElevation(0f);
        } else {
            mTwoPane = false;
        }

        setTitle(R.string.pref_sort_order_default_label);
        PopularMoviesSyncAdapter.initializeSyncAdapter(this);
        //Stetho.initializeWithDefaults(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailsFragment.DETAIL_URI, contentUri);

            DetailsFragment fragment = new DetailsFragment();
            fragment.setArguments(args);
            fragment.setUseTowPanels(true);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, sPopularMoviesDetailsFragmentTag)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailsActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
