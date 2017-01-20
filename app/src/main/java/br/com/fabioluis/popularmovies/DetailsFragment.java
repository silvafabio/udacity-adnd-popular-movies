package br.com.fabioluis.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.fabioluis.popularmovies.data.PopularMoviesContract;
import br.com.fabioluis.popularmovies.model.Review;
import br.com.fabioluis.popularmovies.model.Video;
import br.com.fabioluis.utils.Utils;

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
    private static final String sTextoPadraoCompartilhamento = "\r\n\r\nDescobri este filme através do App do Fábio, muito legal ;-)";
    private static final int sDetailLoader = 0;

    private Uri mUri;
    private String mBackgroundColor;
    private String mMovieId;
    private ArrayList<Video> mVideos;
    private ArrayList<Review> mReviews;
    private int mFavorite;

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

    public DetailsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.details_fragment, menu);
        mMenuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mMenuItem);
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
        //shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        return shareIntent;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mVideos != null && !mVideos.isEmpty()) {
            outState.putParcelableArrayList(sSavedVideosAdapter, mVideos);
        }

        if (mReviews != null && !mReviews.isEmpty()) {
            outState.putParcelableArrayList(sSavedReviewsAdapter, mReviews);
        }

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

            if (mFavorite == 1) {
                mFavoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
            }

            String title = data.getString(COL_TITLE);
            mTitle.setText(title);

            String backdrop = data.getString(COL_BACKDROP);
            Picasso.with(getContext()).load(Utils.BACKDROP_BASE_URL + backdrop).into(mBackdrop);

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
            CarregaVideos carregaVideos = new CarregaVideos();
            carregaVideos.execute(mMovieId);
            CarregaReviews carregaReviews = new CarregaReviews();
            carregaReviews.execute(mMovieId);
        }
    }

    public class CarregaVideos extends AsyncTask<String, Void, List<Video>> {
        private static final String sVideoSite = "YouTube";

        @Override
        protected List<Video> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            String json = Utils.getDataFromApi("videos", params[0]);

            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(json);
            JsonElement jsonElement = jsonObject.get("results");

            Video[] videosRetornados = gson.fromJson(jsonElement, Video[].class);

            List<Video> videos = new ArrayList<>();

            for (Video video : videosRetornados) {
                if (video.getSite() != null && video.getSite().equalsIgnoreCase(sVideoSite)) {
                    videos.add(video);
                }
            }

            return videos;
        }

        @Override
        protected void onPostExecute(List<Video> videosList) {
            if (videosList != null && !videosList.isEmpty()) {
                mVideos.addAll(videosList);
                mVideosAdapter.addAll(videosList);
                mTrailersLabel.setVisibility(View.VISIBLE);
                mMenuItem.setVisible(true);
                mShareActionProvider.setShareIntent(createShareMovieIntent());
            }
        }
    }

    public class CarregaReviews extends AsyncTask<String, Void, List<Review>> {
        @Override
        protected List<Review> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            String json = Utils.getDataFromApi("reviews", params[0]);

            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(json);
            JsonElement jsonElement = jsonObject.get("results");

            Review[] reviews = gson.fromJson(jsonElement, Review[].class);
            return Arrays.asList(reviews);
        }

        @Override
        protected void onPostExecute(List<Review> reviewsList) {
            if (reviewsList != null && !reviewsList.isEmpty()) {
                mReviews.addAll(reviewsList);
                mReviewsAdapter.addAll(reviewsList);
                mReviewsLabel.setVisibility(View.VISIBLE);
            }
        }
    }
}