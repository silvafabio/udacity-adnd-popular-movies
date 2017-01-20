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

public class MainActivity extends AppCompatActivity implements PopularMoviesFragment.Callback{

    private static final String sPopularMoviesFragmentTag = "PMFTAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, new PopularMoviesFragment())
                    .commit();
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
//        if(item.getItemId() == R.id.action_settings){
//            startActivity(new Intent(this, SettingsActivity.class));
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
     /*       args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();*/
        } else {
            Intent intent = new Intent(this, DetailsActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
