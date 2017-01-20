package br.com.fabioluis.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import br.com.fabioluis.popularmovies.R;

/**
 * Created by silva on 12/12/2016.
 */

public class PopularMoviesProvider extends ContentProvider {
    static final int MOVIE = 100;
    static final int MOVIE_WITH_ID = 101;
    static final int LISTS = 200;
    static final int LISTS_WITH_LIST_TYPE = 201;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String sMovieNotFavorite = "0";
    private static final String sMovieFavorite = "1";
    private static final SQLiteQueryBuilder sMoviesQueryBuilder;
    private static final SQLiteQueryBuilder sListsQueryBuilder;

    private PopularMoviesDbHelper mPopularMoviesHelper;

    static {
        sListsQueryBuilder = new SQLiteQueryBuilder();

        sListsQueryBuilder.setTables(
                PopularMoviesContract.ListsEntry.TABLE_NAME + " INNER JOIN " +
                        PopularMoviesContract.MoviesEntry.TABLE_NAME +
                        " ON " + PopularMoviesContract.ListsEntry.TABLE_NAME +
                        "." + PopularMoviesContract.ListsEntry.COLUMN_MOVIE_KEY +
                        " = " + PopularMoviesContract.MoviesEntry.TABLE_NAME +
                        "." + PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID);
    }

    static {
        sMoviesQueryBuilder = new SQLiteQueryBuilder();
        sMoviesQueryBuilder.setTables(PopularMoviesContract.MoviesEntry.TABLE_NAME);
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PopularMoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PopularMoviesContract.PATH_MOVIES, MOVIE);
        matcher.addURI(authority, PopularMoviesContract.PATH_MOVIES + "/#", MOVIE_WITH_ID);
        matcher.addURI(authority, PopularMoviesContract.PATH_LISTS, LISTS);
        matcher.addURI(authority, PopularMoviesContract.PATH_LISTS + "/*", LISTS_WITH_LIST_TYPE);

        return matcher;
    }

    private static final String sMovieIdSelection =
            PopularMoviesContract.MoviesEntry.TABLE_NAME +
                    "." + PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final String sMovieFavoriteSelection =
            PopularMoviesContract.MoviesEntry.TABLE_NAME +
                    "." + PopularMoviesContract.MoviesEntry.COLUMN_FAVORITE + " = ? ";

    private Cursor getMovieById(
            Uri uri, String[] projection, String sortOrder) {
        Integer movie_id = PopularMoviesContract.MoviesEntry.getIdFromUri(uri);

        return sMoviesQueryBuilder.query(mPopularMoviesHelper.getReadableDatabase(),
                projection,
                sMovieIdSelection,
                new String[]{Integer.toString(movie_id)},
                null,
                null,
                sortOrder
        );
    }

    private static final String sListsListSelection =
            PopularMoviesContract.ListsEntry.TABLE_NAME +
                    "." + PopularMoviesContract.ListsEntry.COLUMN_LIST_TYPE + " = ? ";

    private Cursor getMoviesByList(
            Uri uri, String[] projection, String sortOrder) {
        String list = PopularMoviesContract.ListsEntry.getListFromUri(uri);

        if (list.equalsIgnoreCase(getContext().getString(R.string.pref_sort_order_favorites))) {
            return sMoviesQueryBuilder.query(mPopularMoviesHelper.getReadableDatabase(),
                    projection,
                    sMovieFavoriteSelection,
                    new String[]{sMovieFavorite},
                    null,
                    null,
                    sortOrder
            );
        } else {
            return sListsQueryBuilder.query(mPopularMoviesHelper.getReadableDatabase(),
                    projection,
                    sListsListSelection,
                    new String[]{list},
                    null,
                    null,
                    sortOrder
            );
        }
    }

    @Override
    public boolean onCreate() {
        mPopularMoviesHelper = new PopularMoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return PopularMoviesContract.MoviesEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return PopularMoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            case LISTS:
                return PopularMoviesContract.ListsEntry.CONTENT_TYPE;
            case LISTS_WITH_LIST_TYPE:
                return PopularMoviesContract.ListsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIE_WITH_ID: {
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            }
            case MOVIE: {
                retCursor = mPopularMoviesHelper.getReadableDatabase().query(
                        PopularMoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case LISTS_WITH_LIST_TYPE: {
                retCursor = getMoviesByList(uri, projection, sortOrder);
                break;
            }
            case LISTS: {
                retCursor = mPopularMoviesHelper.getReadableDatabase().query(
                        PopularMoviesContract.ListsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mPopularMoviesHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(PopularMoviesContract.MoviesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PopularMoviesContract.MoviesEntry.buildMovieWithId(
                            values.getAsInteger(PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LISTS: {
                long _id = db.insert(PopularMoviesContract.ListsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PopularMoviesContract.ListsEntry.buildListWithListType(
                            values.getAsString(PopularMoviesContract.ListsEntry.COLUMN_LIST_TYPE));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = mPopularMoviesHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";

        switch (match) {
            case LISTS:
                rowsDeleted = sqLiteDatabase.delete(PopularMoviesContract.ListsEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case MOVIE:
                rowsDeleted = sqLiteDatabase.delete(PopularMoviesContract.MoviesEntry.TABLE_NAME,
                        sMovieFavoriteSelection,
                        new String[]{sMovieNotFavorite});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = mPopularMoviesHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = sqLiteDatabase.update(PopularMoviesContract.MoviesEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            case MOVIE_WITH_ID:
                Integer movieId = PopularMoviesContract.MoviesEntry.getIdFromUri(uri);
                rowsUpdated = sqLiteDatabase.update(PopularMoviesContract.MoviesEntry.TABLE_NAME,
                        values, sMovieIdSelection, new String[]{movieId.toString()});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mPopularMoviesHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;

        switch (match) {
            case MOVIE: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PopularMoviesContract.MoviesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            case LISTS: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PopularMoviesContract.ListsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            default:
                return super.bulkInsert(uri, values);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }
}
