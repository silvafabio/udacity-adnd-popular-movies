package br.com.fabioluis.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import br.com.fabioluis.popularmovies.data.PopularMoviesContract;
import br.com.fabioluis.popularmovies.deserializers.TheMoviesDbResultsDeserializer;
import br.com.fabioluis.popularmovies.entrypoints.rest.movies.review.ReviewsFromMovieRestTmdb;
import br.com.fabioluis.popularmovies.entrypoints.rest.movies.video.VideosFromMovieRestTmdb;
import br.com.fabioluis.popularmovies.model.Review;
import br.com.fabioluis.popularmovies.model.Video;
import br.com.fabioluis.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by silva on 02/01/2017.
 */

public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

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

    static final String DETAIL_URI = "URI";
    static final String BACKGROUND_COLOR = "BACKGROUND_COLOR";

    private static final String[] sDetailsColumns = {
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

    private static final String sLogTag = DetailsFragment.class.getSimpleName();
    private static final String sSavedVideosAdapter = "mVideosAdapter";
    private static final String sSavedReviewsAdapter = "mReviewsAdapter";
    private static final String sSavedTowPanels = "mTwoPanels";
    private static final String sTextoPadraoCompartilhamento = "\r\n\r\nDescobri este filme através do App do Fábio, muito legal ;-)";
    private static final int sDetailLoader = 0;
    private static final String sVideoSite = "YouTube";

    private final String mLogTag = DetailsFragment.class.getSimpleName();
    private Uri mUri;
    private String mBackgroundColor;
    private String mMovieId;
    private ArrayList<Video> mVideos;
    private ArrayList<Review> mReviews;
    private int mFavorite;
    private boolean mTwoPanels;

    private VideosAdapter mVideosAdapter;
    private ReviewsAdapter mReviewsAdapter;
    private ShareActionProvider mShareActionProvider;

    private TextView mTitle;
    private ImageView mBackdrop;
    private ImageView mPoster;
    private TextView mReleaseDate;
    private TextView mUserRating;
    private WebView mWebView;
    private TextView mReviewsLabel;
    private TextView mTrailersLabel;
    private ImageButton mFavoriteButton;
    private MenuItem mMenuItem;
    private TextView mLancamentoLabel;
    private TextView mUserRatingLabel;
    private TextView mFavoriteLabel;

    public DetailsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.details_fragment, menu);
        mMenuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mMenuItem);

        if (mVideos != null && !mVideos.isEmpty()) {
            mMenuItem.setVisible(true);
            mShareActionProvider.setShareIntent(createShareMovieIntent());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle arguments = getArguments();

        if (arguments != null) {
            if (arguments.containsKey(DETAIL_URI)) {
                mUri = arguments.getParcelable(DetailsFragment.DETAIL_URI);
            }

            if (arguments.containsKey(BACKGROUND_COLOR)) {
                mBackgroundColor = getArguments().getString(BACKGROUND_COLOR);
            }
        }

        View view = inflater.inflate(R.layout.fragment_details, container, false);

        mTitle = (TextView) view.findViewById(R.id.movie_title);
        mBackdrop = (ImageView) view.findViewById(R.id.movie_backdrop);
        mPoster = (ImageView) view.findViewById(R.id.movie_thumbnail);
        mReleaseDate = (TextView) view.findViewById(R.id.movie_release_date);
        mUserRating = (TextView) view.findViewById(R.id.movie_user_rating);
        mWebView = (WebView) view.findViewById(R.id.movie_overview);
        mReviewsLabel = (TextView) view.findViewById(R.id.movie_reviews_label);
        mTrailersLabel = (TextView) view.findViewById(R.id.videos_list_label);
        mFavoriteButton = (ImageButton) view.findViewById(R.id.movie_favorite);

        mLancamentoLabel = (TextView) view.findViewById(R.id.movie_release_date_label);
        mUserRatingLabel = (TextView) view.findViewById(R.id.movie_user_rating_label);
        mFavoriteLabel = (TextView) view.findViewById(R.id.movie_favorite_label);

        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trataFavorito();
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(sSavedVideosAdapter)) {
            mVideos = savedInstanceState.getParcelableArrayList(sSavedVideosAdapter);
        } else {
            mVideos = new ArrayList<>();
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(sSavedReviewsAdapter)) {
            mReviews = savedInstanceState.getParcelableArrayList(sSavedReviewsAdapter);
        } else {
            mReviews = new ArrayList<>();
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(sSavedTowPanels)) {
            mTwoPanels = savedInstanceState.getBoolean(sSavedTowPanels);
        }

        mVideosAdapter = new VideosAdapter(getActivity(), mVideos);
        mReviewsAdapter = new ReviewsAdapter(getActivity(), mReviews);

        final ListView videosList = (ListView) view.findViewById(R.id.videos_list);
        videosList.setAdapter(mVideosAdapter);
        ListView reviewsList = (ListView) view.findViewById(R.id.reviews_list);
        reviewsList.setAdapter(mReviewsAdapter);

        videosList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Video video = (Video) videosList.getItemAtPosition(position);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Utils.YOUTUBE_BASE_URL + video.getKey()));
                startActivity(intent);
            }
        });

        return view;
    }

    public void trataFavorito() {
        if (mFavorite == 1) {
            mFavorite = 0;
            mFavoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
        } else {
            mFavorite = 1;
            mFavoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
        }

        Uri uri = PopularMoviesContract.MoviesEntry.buildMovieWithId(Integer.parseInt(mMovieId));

        ContentValues contentValues = new ContentValues();
        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_FAVORITE, mFavorite);

        getContext().getContentResolver().update(uri, contentValues, null, null);
    }

    private Intent createShareMovieIntent() {
        StringBuilder sb = new StringBuilder(Utils.YOUTUBE_BASE_URL)
                .append(mVideosAdapter.getItem(0).getKey())
                .append(sTextoPadraoCompartilhamento);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        return shareIntent;
    }

    public void setUseTowPanels(boolean useTodayLayout) {
        mTwoPanels = useTodayLayout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mVideos != null && !mVideos.isEmpty()) {
            outState.putParcelableArrayList(sSavedVideosAdapter, mVideos);
        }

        if (mReviews != null && !mReviews.isEmpty()) {
            outState.putParcelableArrayList(sSavedReviewsAdapter, mReviews);
        }

        outState.putBoolean(sSavedTowPanels, mTwoPanels);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(sDetailLoader, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    sDetailsColumns,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mMovieId = data.getString(COL_MOVIE_ID);
            mFavorite = data.getInt(COL_FAVORITE);

            mLancamentoLabel.setVisibility(View.VISIBLE);
            mUserRatingLabel.setVisibility(View.VISIBLE);
            mFavoriteLabel.setVisibility(View.VISIBLE);
            mFavoriteButton.setVisibility(View.VISIBLE);

            if (mFavorite == 1) {
                mFavoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
            }

            String title = data.getString(COL_TITLE);
            mTitle.setText(title);

            String backdrop = data.getString(COL_BACKDROP);
            if (mTwoPanels) {
                Picasso.with(getContext()).load(Utils.BACKDROP_BASE_URL_MEDIUM + backdrop).into(mBackdrop);
            } else {
                Picasso.with(getContext()).load(Utils.BACKDROP_BASE_URL + backdrop).into(mBackdrop);
            }

            String poster = data.getString(COL_POSTER);
            Picasso.with(getContext()).load(Utils.POSTER_BASE_URL + poster).into(mPoster);

            String releaseDate = data.getString(COL_RELEASE_DATE);
            try {
                SimpleDateFormat to = new SimpleDateFormat("DD/MM/yyyy");
                SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-DD");
                mReleaseDate.setText(to.format(from.parse(releaseDate)));
            } catch (ParseException pe) {
                mReleaseDate.setText(releaseDate);
            }

            String userRating = data.getString(COL_VOTE_AVERAGE);
            mUserRating.setText(userRating + "/10");


            String overview = data.getString(COL_OVERVIEW);

            StringBuilder html = new StringBuilder();
            html.append("<html><head></head><body style=\"text-align:justify;background-color:");
            html.append(mBackgroundColor);
            html.append(";color:#888888;\">");
            html.append(overview);
            html.append("</body></html>");

            mWebView.setVerticalScrollBarEnabled(false);
            mWebView.loadData(html.toString(), "text/html; charset=utf-8", "utf-8");

            carregaInformacoesOnLine();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void carregaInformacoesOnLine() {
        if (Utils.isOnLine(getContext()) && (mVideos.isEmpty() || mReviews.isEmpty())) {
            carregaVideos();
            carregaReviews();
        }
    }

    private void carregaReviews() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(List.class, new TheMoviesDbResultsDeserializer<Review>())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Utils.MOVIES_DB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ReviewsFromMovieRestTmdb reviewsFromMovie = retrofit.create(ReviewsFromMovieRestTmdb.class);
        Call<List<Review>> call = reviewsFromMovie.getReviews(mMovieId, BuildConfig.MOVIE_DB_API_KEY);

        call.enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response != null
                        && response.isSuccessful()
                        && response.body() != null) {

                    List<Review> reviewsList = response.body();

                    if (reviewsList != null && !reviewsList.isEmpty()) {
                        mReviews.addAll(reviewsList);
                        mReviewsAdapter.addAll(reviewsList);
                        mReviewsLabel.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                Log.e(mLogTag, t.getMessage());
            }
        });
    }

    private void carregaVideos() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(List.class, new TheMoviesDbResultsDeserializer<Video>())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Utils.MOVIES_DB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        VideosFromMovieRestTmdb videosFromMovie = retrofit.create(VideosFromMovieRestTmdb.class);
        Call<List<Video>> call = videosFromMovie.getVideos(mMovieId, BuildConfig.MOVIE_DB_API_KEY);

        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                if (response != null
                        && response.isSuccessful()
                        && response.body() != null) {
                    List<Video> videosRetornados = response.body();
                    List<Video> videos = new ArrayList<>();

                    for (Video video : videosRetornados) {
                        if (video.getSite() != null && video.getSite().equalsIgnoreCase(sVideoSite)) {
                            videos.add(video);
                        }
                    }

                    if (videos != null && !videos.isEmpty()) {
                        mVideos.clear();
                        mVideos.addAll(videos);
                        //mVideosAdapter.clear();
                        //mVideosAdapter.addAll(videos);
                        mVideosAdapter.notifyDataSetChanged();
                        mTrailersLabel.setVisibility(View.VISIBLE);

                        if (mShareActionProvider != null) {
                            mMenuItem.setVisible(true);
                            mShareActionProvider.setShareIntent(createShareMovieIntent());
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                Log.e(mLogTag, t.getMessage());
            }
        });
    }
}
