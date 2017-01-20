/*
 * Copyright (C) 2016 Fabio Luis
 */

package br.com.fabioluis.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import br.com.fabioluis.popularmovies.data.PopularMoviesContract;


public class PopularMoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int COL_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_FAVORITE = 2;
    static final int COL_BACKDROP = 3;
    static final int COL_ORIGINAL_TITLE = 4;
    static final int COL_OVERVIEW = 5;
    static final int COL_POPULARITY = 6;
    static final int COL_POSTER = 7;
    static final int COL_RELEASE_DATE = 8;
    static final int COL_TITLE = 9;
    static final int COL_VIDEO = 10;
    static final int COL_VOTE_AVERAGE = 11;
    static final int COL_VOTE_COUNT = 12;

    private static final String[] sMovieColumns = {
            PopularMoviesContract.MoviesEntry.TABLE_NAME + "." + PopularMoviesContract.MoviesEntry._ID,
            PopularMoviesContract.MoviesEntry.TABLE_NAME + "." + PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            PopularMoviesContract.MoviesEntry.COLUMN_FAVORITE,
            PopularMoviesContract.MoviesEntry.COLUMN_BACKDROP,
            PopularMoviesContract.MoviesEntry.COLUMN_ORIGINAL_TITLE,
            PopularMoviesContract.MoviesEntry.COLUMN_OVERVIEW,
            PopularMoviesContract.MoviesEntry.COLUMN_POPULARITY,
            PopularMoviesContract.MoviesEntry.COLUMN_POSTER,
            PopularMoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
            PopularMoviesContract.MoviesEntry.COLUMN_TITLE,
            PopularMoviesContract.MoviesEntry.COLUMN_VIDEO,
            PopularMoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
            PopularMoviesContract.MoviesEntry.COLUMN_VOTE_COUNT
    };

    private static final String sLogTag = PopularMoviesFragment.class.getSimpleName();
    private static final String sSelectedListType = "listType";
    private static final String sSelectedPosition = "selected_position";
    private static final int sPopularMoviesLoader = 0;

    private MoviesAdapter mMoviesAdapter;
    private String mListType;
    private GridView mGridView;

    private int mPosition = GridView.INVALID_POSITION;

    private SharedPreferences mPreferencias;

    public PopularMoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mPreferencias = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popular_movies, container, false);

        mMoviesAdapter = new MoviesAdapter(getActivity(), null, 0);

        mGridView = (GridView) view.findViewById(R.id.movies_grid);
        mGridView.setAdapter(mMoviesAdapter);

        TextView nenhumRegistroEncontrado = (TextView) view.findViewById(R.id.empty_grid_view);
        mGridView.setEmptyView(nenhumRegistroEncontrado);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);

                if (cursor != null) {
                    ((Callback) getActivity()).onItemSelected(PopularMoviesContract.MoviesEntry.
                            buildMovieWithId(cursor.getInt(COL_MOVIE_ID)));
                }
                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(sSelectedListType)) {
            mListType = savedInstanceState.getString(sSelectedListType);
        } else {
            mListType = getString(R.string.pref_sort_order_default);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(sSelectedPosition)) {
            mPosition = savedInstanceState.getInt(sSelectedPosition);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(sPopularMoviesLoader, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.popular_movies, menu);

        // http://stackoverflow.com/questions/29433550/how-can-we-use-menu-items-outside-onoptionitemselected-like-in-oncreate
        MenuItem receberNotificacoes = menu.findItem(R.id.receber_notificacoes);
        receberNotificacoes.setChecked(mPreferencias.getBoolean(
                getContext().getString(R.string.pref_enable_notifications_key), false)
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.receber_notificacoes) {
            SharedPreferences.Editor editor = mPreferencias.edit();

            // http://stackoverflow.com/questions/6239163/android-checkable-menu-item
            if (item.isChecked()) {
                item.setChecked(false);
                editor.putBoolean(getContext().getString(R.string.pref_enable_notifications_key), false);
                Toast.makeText(getContext(), R.string.notificacoes_desabilitadas, Toast.LENGTH_SHORT).show();
            } else {
                item.setChecked(true);
                editor.putBoolean(getContext().getString(R.string.pref_enable_notifications_key), true);
                Toast.makeText(getContext(), R.string.notificacoes_habilitadas, Toast.LENGTH_SHORT).show();
            }

            editor.commit();
        } else if (item.getItemId() == R.id.action_settings_highest_rated) {
            updateMovies(getString(R.string.pref_sort_order_highest_rated));
            getActivity().setTitle(R.string.pref_sort_order_highest_rated_label);
            return true;
        } else if (item.getItemId() == R.id.action_settings_most_popular) {
            updateMovies(getString(R.string.pref_sort_order_most_popular));
            getActivity().setTitle(R.string.pref_sort_order_most_popular_label);
            return true;
        } else if (item.getItemId() == R.id.action_settings_now_playing) {
            updateMovies(getString(R.string.pref_sort_order_now_playing));
            getActivity().setTitle(R.string.pref_sort_order_now_playing_label);
            return true;
        } else if (item.getItemId() == R.id.action_settings_upcoming) {
            updateMovies(getString(R.string.pref_sort_order_upcoming));
            getActivity().setTitle(R.string.pref_sort_order_upcoming_label);
            return true;
        } else if (item.getItemId() == R.id.action_settings_favorites) {
            updateMovies(getString(R.string.pref_sort_order_favorites));
            getActivity().setTitle(R.string.pref_sort_order_favorites_label);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(sSelectedPosition, mPosition);
        }

        outState.putString(sSelectedListType, mListType);

        super.onSaveInstanceState(outState);
    }

    public void updateMovies(String listTypeSelected) {
        if (!mListType.equalsIgnoreCase(listTypeSelected)) {
            mListType = listTypeSelected;
            mPosition = GridView.INVALID_POSITION;
            getLoaderManager().restartLoader(sPopularMoviesLoader, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = PopularMoviesContract.ListsEntry.buildListWithListType(mListType);
        return new CursorLoader(getActivity(), uri, sMovieColumns, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMoviesAdapter.swapCursor(data);
        mGridView.smoothScrollToPosition(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.swapCursor(null);
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }
}
