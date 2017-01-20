package br.com.fabioluis.popularmovies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by silva on 12/12/2016.
 */

public class PopularMoviesContract {

    public static final String CONTENT_AUTHORITY = "br.com.fabioluis.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_LISTS = "lists";

    public static final class ListsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LISTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LISTS;

        public static final String TABLE_NAME = "lists";

        public static final String COLUMN_MOVIE_KEY = "movie_id";
        public static final String COLUMN_LIST_TYPE = "list_type";

        public static String getListFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildListWithListType(String listType) {
            return CONTENT_URI.buildUpon().appendPath(listType).build();
        }
    }


    public static final class MoviesEntry implements BaseColumns {

        public static final Integer DEFAULT_FAVORITE_VALUE = 0;

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_BACKDROP = "backdrop";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VIDEO = "video";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_FAVORITE = "favorite";

        public static Integer getIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static Uri buildMovieWithId(Integer movieId) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(movieId)).build();
        }
    }
}