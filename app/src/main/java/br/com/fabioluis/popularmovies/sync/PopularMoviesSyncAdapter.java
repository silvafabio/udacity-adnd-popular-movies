package br.com.fabioluis.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import br.com.fabioluis.popularmovies.MainActivity;
import br.com.fabioluis.popularmovies.R;
import br.com.fabioluis.popularmovies.data.PopularMoviesContract;
import br.com.fabioluis.popularmovies.model.Movie;
import br.com.fabioluis.utils.Utils;

/**
 * Created by silva on 27/12/2016.
 */

public class PopularMoviesSyncAdapter extends AbstractThreadedSyncAdapter {

    private final String mLogTag = PopularMoviesSyncAdapter.class.getSimpleName();
    private static final int sSyncInternal = 60 * 180;
    private static final int sSyncFlextime = sSyncInternal / 3;
    private static final long sDayInMillis = 1000 * 60 * 60 * 24;
    private static final int sUpdateNotificationId = 5000;
    private static final String sMdbResults = "results";


    public PopularMoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        int  registrosAtualizados = 0;

        registrosAtualizados += syncByListType(getContext().getString(R.string.pref_sort_order_highest_rated));
        registrosAtualizados += syncByListType(getContext().getString(R.string.pref_sort_order_most_popular));
        registrosAtualizados += syncByListType(getContext().getString(R.string.pref_sort_order_now_playing));
        registrosAtualizados += syncByListType(getContext().getString(R.string.pref_sort_order_upcoming));

        if(registrosAtualizados > 0){
            notifyMovies();
        }

        return;
    }

    public int syncByListType(String listType){
        String retorno = Utils.getMoviesFromApi(listType);
        List<Movie> movies = getMoviesFromJson(retorno);
        updateDb(movies, listType);
        return movies.size();
    }

    public void updateDb(List<Movie> movies, String listType) {
        Vector<ContentValues> moviesVector = new Vector<ContentValues>(movies.size());
        Vector<ContentValues> listTypeVector = new Vector<ContentValues>(movies.size());

        for (Movie movie : movies) {
            ContentValues movieValues = new ContentValues();
            ContentValues listTypeValues = new ContentValues();

            // Só vamos adicionar filmes que tenha poster e backdrop
            if(movie.getPosterPath() != null
                    && !movie.getPosterPath().isEmpty()
                    && movie.getBackdropPath() != null
                    && !movie.getBackdropPath().isEmpty()) {
                movieValues.put(PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movie.getId().longValue());
                movieValues.put(PopularMoviesContract.MoviesEntry.COLUMN_BACKDROP, movie.getBackdropPath());
                movieValues.put(PopularMoviesContract.MoviesEntry.COLUMN_FAVORITE, PopularMoviesContract.MoviesEntry.DEFAULT_FAVORITE_VALUE);
                movieValues.put(PopularMoviesContract.MoviesEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
                movieValues.put(PopularMoviesContract.MoviesEntry.COLUMN_OVERVIEW, movie.getOverview());
                movieValues.put(PopularMoviesContract.MoviesEntry.COLUMN_POPULARITY, movie.getPopularity().floatValue());
                movieValues.put(PopularMoviesContract.MoviesEntry.COLUMN_POSTER, movie.getPosterPath());
                movieValues.put(PopularMoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                movieValues.put(PopularMoviesContract.MoviesEntry.COLUMN_TITLE, movie.getTitle());
                movieValues.put(PopularMoviesContract.MoviesEntry.COLUMN_VIDEO, movie.isVideo());
                movieValues.put(PopularMoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
                movieValues.put(PopularMoviesContract.MoviesEntry.COLUMN_VOTE_COUNT, movie.getVoteCount().longValue());

                listTypeValues.put(PopularMoviesContract.ListsEntry.COLUMN_MOVIE_KEY, movie.getId().longValue());
                listTypeValues.put(PopularMoviesContract.ListsEntry.COLUMN_LIST_TYPE, listType);

                moviesVector.add(movieValues);
                listTypeVector.add(listTypeValues);
            }
        }

        if (moviesVector.size() > 0) {
            //Inserimos os movies
            ContentValues[] moviesArray = new ContentValues[moviesVector.size()];
            moviesVector.toArray(moviesArray);
            getContext().getContentResolver().bulkInsert(PopularMoviesContract.MoviesEntry.CONTENT_URI, moviesArray);

            //Removemos os movies do listType
            getContext().getContentResolver().delete(PopularMoviesContract.ListsEntry.CONTENT_URI,
                    PopularMoviesContract.ListsEntry.COLUMN_LIST_TYPE + " = ?",
                    new String[]{listType});

            //Inserimos os movies do listType
            ContentValues[] listTypeArray = new ContentValues[listTypeVector.size()];
            listTypeVector.toArray(listTypeArray);
            getContext().getContentResolver().bulkInsert(PopularMoviesContract.ListsEntry.CONTENT_URI, listTypeArray);

            //notifyMovies();
        }

    }

    private List<Movie> getMoviesFromJson(String jsonMovies) {
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(jsonMovies);
        JsonElement jsonElement = jsonObject.get(sMdbResults);

        Movie[] movies = gson.fromJson(jsonElement, Movie[].class);
        return Arrays.asList(movies);
    }

    private void notifyMovies() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if (displayNotifications) {
            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= sDayInMillis) {
                Resources resources = context.getResources();
                Bitmap largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher);
                String title = context.getString(R.string.app_name);
                String contentText = context.getString(R.string.popular_movies_updated);

                // NotificationCompatBuilder is a very convenient way to build backward-compatible
                // notifications.  Just throw in some data.
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setColor(resources.getColor(R.color.colorPrimary))
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setLargeIcon(largeIcon)
                                .setContentTitle(title)
                                .setContentText(contentText);

                // Make something interesting happen when the user clicks on the notification.
                // In this case, opening the app is sufficient.
                Intent resultIntent = new Intent(context, MainActivity.class);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(sUpdateNotificationId, mBuilder.build());

                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.commit();
            }
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        PopularMoviesSyncAdapter.configurePeriodicSync(context, sSyncInternal, sSyncFlextime);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
