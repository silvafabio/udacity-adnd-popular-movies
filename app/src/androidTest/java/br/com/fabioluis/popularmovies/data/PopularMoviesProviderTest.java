package br.com.fabioluis.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.com.fabioluis.popularmovies.R;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by silva on 19/12/2016.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class PopularMoviesProviderTest {

    public static final String LOG_TAG = PopularMoviesProviderTest.class.getSimpleName();

    private static final Integer TESTE_MOVIE_ID = TestUtilities.MOVIE_ID;


    // content://br.com.fabioluis.popularmovies/movies
    private static final Uri TEST_MOVIES_DIR = PopularMoviesContract.MoviesEntry.CONTENT_URI;
    // content://br.com.fabioluis.popularmovies/movies/346672
    private static final Uri TEST_MOVIES_WITH_ID_ITEM = PopularMoviesContract.MoviesEntry.buildMovieWithId(TESTE_MOVIE_ID);
    // content://br.com.fabioluis.popularmovies/lists
    private static final Uri TEST_LISTS_DIR = PopularMoviesContract.ListsEntry.CONTENT_URI;

    @BeforeClass
    public static void setUp() throws Exception {
        deleteAllRecords();
    }

    @Test
    public void testBuildUriMatcher() throws Exception {
        // content://br.com.fabioluis.popularmovies/lists/UPCOMING
        final String TESTE_LIST_TYPE = getTargetContext().getString(R.string.pref_sort_order_upcoming);
        final Uri TEST_LISTS_WITH_TYPE_DIR = PopularMoviesContract.ListsEntry.buildListWithListType(TESTE_LIST_TYPE);

        UriMatcher testMatcher = PopularMoviesProvider.buildUriMatcher();

        assertEquals("Erro: URI TEST_MOVIES_DIR incorreta.",
                testMatcher.match(TEST_MOVIES_DIR), PopularMoviesProvider.MOVIE);

        assertEquals("Erro: URI TEST_MOVIES_WITH_ID_ITEM incorreta.",
                testMatcher.match(TEST_MOVIES_WITH_ID_ITEM), PopularMoviesProvider.MOVIE_WITH_ID);

        assertEquals("Erro: URI TEST_LISTS_DIR incorreta.",
                testMatcher.match(TEST_LISTS_DIR), PopularMoviesProvider.LISTS);

        assertEquals("Erro: URI TEST_LISTS_WITH_TYPE_DIR incorreta.",
                testMatcher.match(TEST_LISTS_WITH_TYPE_DIR), PopularMoviesProvider.LISTS_WITH_LIST_TYPE);
    }

    public static void deleteAllRecordsFromProvider() {
        getTargetContext().getContentResolver().delete(
                PopularMoviesContract.ListsEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = getTargetContext().getContentResolver().query(
                PopularMoviesContract.ListsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Erro: Registros não deletados da tabela Lists", 0, cursor.getCount());
        cursor.close();

        getTargetContext().getContentResolver().delete(
                PopularMoviesContract.MoviesEntry.CONTENT_URI,
                null,
                null
        );

        cursor = getTargetContext().getContentResolver().query(
                PopularMoviesContract.MoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Erro: Registros não deletados da tabela Movies", 0, cursor.getCount());
        cursor.close();
    }

    public static void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Test
    public void testProviderRegistry() {
        PackageManager pm = getTargetContext().getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(getTargetContext().getPackageName(),
                PopularMoviesProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Erro: PopularMoviesProvider registrado com autoridade : " + providerInfo.authority +
                            " ao invés de: " + PopularMoviesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, PopularMoviesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Erro: PopularMoviesProvider não registrado no " + getTargetContext().getPackageName(),
                    false);
        }
    }

    @Test
    public void testGetType() {
        final String TESTE_LIST_TYPE = getTargetContext().getString(R.string.pref_sort_order_upcoming);

        // content://br.com.fabioluis.popularmovies/movies
        String type = getTargetContext().getContentResolver().getType(PopularMoviesContract.MoviesEntry.CONTENT_URI);
        // vnd.android.cursor.dir/br.com.fabioluis.popularmovies/movies
        assertEquals("Erro: MoviesEntry CONTENT_URI deveria retornar MoviesEntry.CONTENT_TYPE",
                PopularMoviesContract.MoviesEntry.CONTENT_TYPE, type);

        // content://br.com.fabioluis.popularmovies/movies/#
        type = getTargetContext().getContentResolver().getType(PopularMoviesContract.MoviesEntry.buildMovieWithId(TESTE_MOVIE_ID));
        // vnd.android.cursor.dir/br.com.fabioluis.popularmovies/movies
        assertEquals("Erro: MoviesEntry CONTENT_URI deveria retornar MoviesEntry.CONTENT_ITEM_TYPE",
                PopularMoviesContract.MoviesEntry.CONTENT_ITEM_TYPE, type);

        // content://br.com.fabioluis.popularmovies/lists
        type = getTargetContext().getContentResolver().getType(PopularMoviesContract.ListsEntry.CONTENT_URI);
        // vnd.android.cursor.dir/br.com.fabioluis.popularmovies/lists
        assertEquals("Erro: ListsEntry CONTENT_URI deveria retornar ListsEntry.CONTENT_TYPE",
                PopularMoviesContract.ListsEntry.CONTENT_TYPE, type);

        // content://br.com.fabioluis.popularmovies/lists
        type = getTargetContext().getContentResolver().getType(PopularMoviesContract.ListsEntry.buildListWithListType(TESTE_LIST_TYPE));
        // vnd.android.cursor.dir/br.com.fabioluis.popularmovies/lists
        assertEquals("Erro: ListsEntry CONTENT_URI deveria retornar ListsEntry.CONTENT_TYPE",
                PopularMoviesContract.ListsEntry.CONTENT_TYPE, type);
    }

    @Test
    public void testBasicMoviesQuery() {
        SQLiteDatabase db = new PopularMoviesDbHelper(getTargetContext()).getWritableDatabase();
        ContentValues testValues = TestUtilities.createMovie();

        long movieRowId = db.insert(PopularMoviesContract.MoviesEntry.TABLE_NAME, null, testValues);

        // Testamos se o registro foi inserido
        assertTrue(movieRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor moviesCursor = getTargetContext().getContentResolver().query(
                PopularMoviesContract.MoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMoviesQuery", moviesCursor, testValues);
    }

    @Test
    public void testBasicListsQuery() {
        deleteAllRecords();
        SQLiteDatabase db = new PopularMoviesDbHelper(getTargetContext()).getWritableDatabase();
        ContentValues testValues = TestUtilities.createListWithOneMovie();

        long listsRowId = db.insert(PopularMoviesContract.ListsEntry.TABLE_NAME, null, testValues);

        // Testamos se o registro foi inserido
        assertTrue(listsRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor listsCursor = getTargetContext().getContentResolver().query(
                PopularMoviesContract.ListsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicListsQuery", listsCursor, testValues);
    }

    @Test
    public void testUpdateMovies() {
        deleteAllRecords();
        ContentValues testValues = TestUtilities.createMovie();

        Uri locationUri = getTargetContext().getContentResolver().
                insert(PopularMoviesContract.MoviesEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        // Testamos se o registro foi inserido
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "Id do registro: " + locationRowId);

        ContentValues updatedValues = new ContentValues(testValues);
        updatedValues.put(PopularMoviesContract.MoviesEntry.COLUMN_POPULARITY, 0);

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor moviesCursor = getTargetContext().getContentResolver().
                query(PopularMoviesContract.MoviesEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        moviesCursor.registerContentObserver(tco);

        int count = getTargetContext().getContentResolver().update(
                PopularMoviesContract.MoviesEntry.CONTENT_URI,
                updatedValues,
                PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID + "= ?",
                new String[]{Long.toString(locationRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // Students: If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        moviesCursor.unregisterContentObserver(tco);
        moviesCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = getTargetContext().getContentResolver().query(
                PopularMoviesContract.MoviesEntry.CONTENT_URI,
                null,   // projection
                PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = " + locationRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateMovies.  Erro validando movie entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    @Test
    public void testInsertReadProvider() {
        final String LIST_TYPE = getTargetContext().getString(R.string.pref_sort_order_upcoming);
        deleteAllRecords();
        ContentValues movieValues = TestUtilities.createMovie();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        getTargetContext().getContentResolver().registerContentObserver(PopularMoviesContract.MoviesEntry.CONTENT_URI, true, tco);
        Uri movieUri = getTargetContext().getContentResolver().insert(PopularMoviesContract.MoviesEntry.CONTENT_URI, movieValues);

        // Did our content observer get called?  Students:  If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        getTargetContext().getContentResolver().unregisterContentObserver(tco);

        long locationRowId = ContentUris.parseId(movieUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor movieCursor = getTargetContext().getContentResolver().query(
                PopularMoviesContract.MoviesEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating LocationEntry.",
                movieCursor, movieValues);

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues listValues = TestUtilities.createListWithOneMovie();
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        getTargetContext().getContentResolver().registerContentObserver(PopularMoviesContract.ListsEntry.CONTENT_URI, true, tco);

        Uri weatherInsertUri = getTargetContext().getContentResolver()
                .insert(PopularMoviesContract.ListsEntry.CONTENT_URI, listValues);
        assertTrue(weatherInsertUri != null);

        // Did our content observer get called?  Students:  If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        getTargetContext().getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor listCursor = getTargetContext().getContentResolver().query(
                PopularMoviesContract.ListsEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating WeatherEntry insert.",
                listCursor, listValues);

        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        listValues.putAll(movieValues);

        // Get the joined Weather and Location data
        listCursor = getTargetContext().getContentResolver().query(
                PopularMoviesContract.ListsEntry.buildListWithListType(LIST_TYPE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location Data.",
                listCursor, listValues);
    }

    @Test
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver listsObserver = TestUtilities.getTestContentObserver();
        getTargetContext().getContentResolver().registerContentObserver(
                PopularMoviesContract.ListsEntry.CONTENT_URI, true, listsObserver);

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        getTargetContext().getContentResolver().registerContentObserver(
                PopularMoviesContract.MoviesEntry.CONTENT_URI, true, movieObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        listsObserver.waitForNotificationOrFail();
        movieObserver.waitForNotificationOrFail();

        getTargetContext().getContentResolver().unregisterContentObserver(listsObserver);
        getTargetContext().getContentResolver().unregisterContentObserver(movieObserver);
    }

    @Test
    public void testBulkInsert() {
        int movies = 10;
        deleteAllRecords();
        // first, let's create a location value
        ContentValues[] moviesValues = TestUtilities.createMovies(movies);
        int moviesCount = getTargetContext().getContentResolver().
                bulkInsert(PopularMoviesContract.MoviesEntry.CONTENT_URI, moviesValues);

        // Verify we got a row back.
        assertEquals(moviesCount, movies);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = getTargetContext().getContentResolver().query(
                PopularMoviesContract.MoviesEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        assertEquals(cursor.getCount(), movies);

        cursor.moveToFirst();
        for ( int i = 0; i < movies; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert. Error validating LocationEntry.",
                    cursor, moviesValues[i]);
        }

        // Now we can bulkInsert some weather.  In fact, we only implement BulkInsert for weather
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] listTypeValues = TestUtilities.createListsType(moviesValues);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver listTypeObserver = TestUtilities.getTestContentObserver();
        getTargetContext().getContentResolver().registerContentObserver(
                PopularMoviesContract.ListsEntry.CONTENT_URI, true, listTypeObserver);

        int insertCount = getTargetContext().getContentResolver().
                bulkInsert(PopularMoviesContract.ListsEntry.CONTENT_URI, listTypeValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        listTypeObserver.waitForNotificationOrFail();
        getTargetContext().getContentResolver().unregisterContentObserver(listTypeObserver);

        assertEquals(insertCount, movies);

        // A cursor is your primary interface to the query results.
        cursor = getTargetContext().getContentResolver().query(
                PopularMoviesContract.ListsEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                PopularMoviesContract.ListsEntry.COLUMN_MOVIE_KEY + " ASC"  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), movies);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < movies; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating WeatherEntry " + i,
                    cursor, listTypeValues[i]);
        }
        cursor.close();
    }
}