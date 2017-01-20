/*
 * Copyright (C) 2016 Fabio Luis
 */

package br.com.fabioluis.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        //setupActionBar();

        if (savedInstanceState == null) {
            // Conseguir o background color do tema
            // http://stackoverflow.com/questions/12375766/how-to-get-background-color-from-current-theme-programmatically
            String backgroudColor = "#FFFFFF";
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
            if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                // windowBackground is a color
                int color = typedValue.data;
                // Converte a cor que está em int para hex string para ser usada no HTML
                // http://stackoverflow.com/questions/6539879/how-to-convert-a-color-integer-to-a-hex-string-in-android
                backgroudColor = String.format("#%06X", (0xFFFFFF & color));
            }

            // Pass values to fragment
            // http://stackoverflow.com/questions/12739909/send-data-from-activity-to-fragment-in-android
            Bundle bundle = new Bundle();
            bundle.putString(DetailsFragment.BACKGROUND_COLOR, backgroudColor);
            bundle.putParcelable(DetailsFragment.DETAIL_URI, getIntent().getData());

            DetailsFragment detailsFragment = new DetailsFragment();
            detailsFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_movie_details, detailsFragment)
                    .commit();
        }
    }
}
