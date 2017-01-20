package br.com.fabioluis.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by silva on 12/12/2016.
 */

public class PopularMoviesDbHelper extends SQLiteOpenHelper {

    private static final int sDatabaseVersion = 8;
    static final String DATABASE_NAME = "popular-movies.db";

    public PopularMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, sDatabaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + PopularMoviesContract.MoviesEntry.TABLE_NAME + " (" +
                PopularMoviesContract.MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE ON CONFLICT IGNORE, " +
                PopularMoviesContract.MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL , " +
                PopularMoviesContract.MoviesEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                PopularMoviesContract.MoviesEntry.COLUMN_RELEASE_DATE + " INTEGER NOT NULL, " +
                PopularMoviesContract.MoviesEntry.COLUMN_POSTER + " BLOB, " +
                PopularMoviesContract.MoviesEntry.COLUMN_BACKDROP + " BLOB, " +
                PopularMoviesContract.MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                PopularMoviesContract.MoviesEntry.COLUMN_VIDEO + " INTEGER NOT NULL, " +
                PopularMoviesContract.MoviesEntry.COLUMN_VOTE_COUNT + " INTEGER NOT NULL, " +
                PopularMoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                PopularMoviesContract.MoviesEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                PopularMoviesContract.MoviesEntry.COLUMN_FAVORITE + " INTEGER NOT NULL DEFAULT 0); ";

        final String SQL_CREATE_LISTS_TABLE = "CREATE TABLE " + PopularMoviesContract.ListsEntry.TABLE_NAME + " (" +
                PopularMoviesContract.ListsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PopularMoviesContract.ListsEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                PopularMoviesContract.ListsEntry.COLUMN_LIST_TYPE + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + PopularMoviesContract.ListsEntry.COLUMN_MOVIE_KEY + ") " +
                " REFERENCES " + PopularMoviesContract.MoviesEntry.TABLE_NAME + " (" +
                PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID + "), " +

                " UNIQUE (" + PopularMoviesContract.ListsEntry.COLUMN_MOVIE_KEY + ", " +
                PopularMoviesContract.ListsEntry.COLUMN_LIST_TYPE + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_LISTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion <= 7) {
            db.execSQL("DROP TABLE IF EXISTS " + PopularMoviesContract.ListsEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + PopularMoviesContract.MoviesEntry.TABLE_NAME);
            onCreate(db);
        }
    }
}
