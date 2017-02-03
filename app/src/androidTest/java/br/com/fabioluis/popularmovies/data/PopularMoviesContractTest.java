package br.com.fabioluis.popularmovies.data;

import android.net.Uri;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import br.com.fabioluis.popularmovies.R;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by silva on 19/12/2016.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class PopularMoviesContractTest {
    private static final Integer TESTE_MOVIE_ID = TestUtilities.MOVIE_ID;

    @Test
    public void testMovieBuild() {
        Uri movieUri = PopularMoviesContract.MoviesEntry.buildMovieWithId(TESTE_MOVIE_ID);

        assertNotNull("Erro: Null Uri retornada.", movieUri);
        assertEquals("Erro: Movie Id not properly appended to the end of the Uri",
                String.valueOf(TESTE_MOVIE_ID), movieUri.getLastPathSegment());
        assertEquals("Erro: Movie Id não bate com o resultado esperado",
                movieUri.toString(),
                "content://br.com.fabioluis.popularmovies/movies/346672");
    }

    @Test
    public void testListBuild() {
        final String TESTE_LIST_TYPE = getTargetContext().getString(R.string.pref_sort_order_upcoming);
        Uri listUri = PopularMoviesContract.ListsEntry.buildListWithListType(TESTE_LIST_TYPE);

        assertNotNull("Erro: Null Uri retornada.", listUri);
        assertEquals("Erro: List type not properly appended to the end of the Uri",
                TESTE_LIST_TYPE, listUri.getLastPathSegment());
        assertEquals("Erro: List type não bate com o resultado esperado",
                listUri.toString(),
                "content://br.com.fabioluis.popularmovies/lists/upcoming");
    }

}