package br.com.fabioluis.popularmovies.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import java.util.Map;
import java.util.Set;

import br.com.fabioluis.popularmovies.R;
import br.com.fabioluis.popularmovies.utils.PollingCheck;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by silva on 19/12/2016.
 */

public class TestUtilities {

    static final Integer MOVIE_ID = 346672;

    public static ContentValues createMovie(){
        ContentValues contentValues = new ContentValues();

        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID, MOVIE_ID);
        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_BACKDROP, "/PIXSMakrO3s2dqA7mCvAAoVR0E.jpg");
        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_FAVORITE, 0);
        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_ORIGINAL_TITLE, "Underworld: Blood Wars");
        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_OVERVIEW, "Underworld: Blood Wars follows Vampire death dealer, Selene, as she fends off brutal attacks from both the Lycan clan and the Vampire faction that betrayed her. With her only allies, David and his father Thomas, she must stop the eternal war between Lycans and Vampires, even if it means she has to make the ultimate sacrifice.");
        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_POPULARITY, 45.020986);
        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_POSTER, "/nHXiMnWUAUba2LZ0dFkNDVdvJ1o.jpg");
        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, "2016-08-02");
        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_TITLE, "Underworld: Blood Wars");
        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_VIDEO, 0);
        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, 4);
        contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_VOTE_COUNT, 300);

        return contentValues;
    }

    public static ContentValues[] createMovies(int movies){
        ContentValues[] returnContentValues = new ContentValues[movies];

        for ( int i = 0; i < movies; i++) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID, i);
            contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_BACKDROP, "/PIXSMakrO3s2dqA7mCvAAoVR0E.jpg");
            contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_FAVORITE, 0);
            contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_ORIGINAL_TITLE, "Underworld: Blood Wars");
            contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_OVERVIEW, "Underworld: Blood Wars follows Vampire death dealer, Selene, as she fends off brutal attacks from both the Lycan clan and the Vampire faction that betrayed her. With her only allies, David and his father Thomas, she must stop the eternal war between Lycans and Vampires, even if it means she has to make the ultimate sacrifice.");
            contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_POPULARITY, 45.020986);
            contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_POSTER, "/nHXiMnWUAUba2LZ0dFkNDVdvJ1o.jpg");
            contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, "2016-08-02");
            contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_TITLE, "Underworld: Blood Wars");
            contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_VIDEO, 0);
            contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, 4);
            contentValues.put(PopularMoviesContract.MoviesEntry.COLUMN_VOTE_COUNT, 300);

            returnContentValues[i] = contentValues;
        }

        return returnContentValues;
    }

    public static ContentValues[] createListsType(ContentValues[] movies){
        final String LIST_TYPE = getTargetContext().getString(R.string.pref_sort_order_upcoming);
        ContentValues[] returnContentValues = new ContentValues[movies.length];

        for ( int i = 0; i < movies.length; i++) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(PopularMoviesContract.ListsEntry.COLUMN_LIST_TYPE, LIST_TYPE);
            contentValues.put(PopularMoviesContract.ListsEntry.COLUMN_MOVIE_KEY,
                    movies[i].getAsString(PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID));

            returnContentValues[i] = contentValues;
        }

        return returnContentValues;
    }


    public static ContentValues createListWithOneMovie(){
        final String LIST_TYPE = getTargetContext().getString(R.string.pref_sort_order_upcoming);
        ContentValues contentValues = new ContentValues();

        contentValues.put(PopularMoviesContract.ListsEntry.COLUMN_LIST_TYPE, LIST_TYPE);
        contentValues.put(PopularMoviesContract.ListsEntry.COLUMN_MOVIE_KEY, MOVIE_ID);

        return contentValues;
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Coluna '" + columnName + "' não encontrada. " + error, idx == -1);

            String expectedValue = entry.getValue().toString();
            String foundedValue = valueCursor.getString(idx);

            if(entry.getValue() instanceof Double){
                assertEquals("Valor '" + foundedValue + "' não bate com o valor esperado '" +
                        expectedValue + "'. " + error,
                        Double.parseDouble(expectedValue), valueCursor.getDouble(idx),0.00001);
            } else {
                assertEquals("Valor '" + foundedValue + "' não bate com o valor esperado '" +
                        expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
            }
        }
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Cursor vazio retornado. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

}
