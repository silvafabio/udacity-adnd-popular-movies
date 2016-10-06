/*
 * Copyright (C) 2016 Fabio Luis
 */

package br.com.fabioluis.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import br.com.fabioluis.popularmovies.model.Movie;
import br.com.fabioluis.utils.Utils;


public class PopularMoviesFragment extends Fragment {

    private MoviesAdapter mMoviesAdapter;
    private String sortOrder;

    public PopularMoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(savedInstanceState != null){
            sortOrder = savedInstanceState.getString("sortOrder");
        } else {
            sortOrder = getString(R.string.pref_sort_order_default);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popular_movies, container, false);

        mMoviesAdapter = new MoviesAdapter(getActivity(), new ArrayList<Movie>());

        GridView gridView = (GridView) view.findViewById(R.id.movies_grid);
        gridView.setAdapter(mMoviesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), MovieDetails.class)
                        .putExtra("movie", mMoviesAdapter.getItem(i));
                startActivity(intent);
                mMoviesAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.popular_movies, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings_highest_rated) {
            sortOrder = getString(R.string.pref_sort_order_highest_rated);
            updateMovies();
        } else if (item.getItemId() == R.id.action_settings_most_popular) {
            sortOrder = getString(R.string.pref_sort_order_most_popular);
            updateMovies();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("sortOrder", sortOrder);
    }

    public void updateMovies() {
        if (Utils.isOnLine(getContext())) {
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
//            String sort = PreferenceManager.getDefaultSharedPreferences(getActivity())
//                    .getString(getString(R.string.pref_sort_order_key),
//                            getString(R.string.pref_sort_order_default));
            fetchMoviesTask.execute(sortOrder);
        } else {
            Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected List<Movie> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            final String MOVIES_DB_BASE_URL = "https://api.themoviedb.org/3/movie/";
            HttpsURLConnection httpsURLConnection = null;
            BufferedReader bufferedReader = null;

            //https://api.themoviedb.org/3/movie/popular
            //https://api.themoviedb.org/3/movie/top_rated
            Uri uri = Uri.parse(MOVIES_DB_BASE_URL).buildUpon()
                    .appendPath(params[0])
                    .appendQueryParameter("api_key", BuildConfig.MOVIE_DB_API_KEY)
                    .build();

            try {
                URL url = new URL(uri.toString());
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.connect();

                InputStream inputStream = httpsURLConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line + "\n");
                }

                if (stringBuffer.length() == 0) {
                    return null;
                }

                return getMoviesFromJson(stringBuffer.toString());
            } catch (IOException ioe) {
                Log.e(LOG_TAG, "Error ", ioe);
                return null;
            } finally {
                if (httpsURLConnection != null) {
                    httpsURLConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException ioe) {
                        Log.e(LOG_TAG, "Error closing stream", ioe);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if ((movies != null) && (!movies.isEmpty())) {
                mMoviesAdapter.clear();
                mMoviesAdapter.addAll(movies);
            }
        }

        private List<Movie> getMoviesFromJson(String jsonMovies) {
            final String MDB_RESULTS = "results";

            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(jsonMovies);
            JsonElement jsonElement = jsonObject.get(MDB_RESULTS);

            Movie[] movies = gson.fromJson(jsonElement, Movie[].class);
            return Arrays.asList(movies);
        }
    }
}
