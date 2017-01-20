package br.com.fabioluis.popularmovies.data;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by silva on 14/12/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PopularMoviesDbHelperTest {

    public static final String LOG_TAG = PopularMoviesDbHelperTest.class.getSimpleName();


    @BeforeClass
    public static void setUp() {
        deleteTheDatabase();
    }

    static void deleteTheDatabase() {
        getTargetContext().deleteDatabase(PopularMoviesDbHelper.DATABASE_NAME);
    }
    /*
   @Test
    public void testPermissoesNecessarias(){
        int leitura = ContextCompat.checkSelfPermission(getTargetContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        assertEquals(PackageManager.PERMISSION_GRANTED, leitura);

        int escrita = ContextCompat.checkSelfPermission(getTargetContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        assertEquals(PackageManager.PERMISSION_GRANTED, escrita);
    }*/

    @Test
    public void testCreateDb() throws Throwable {
        // Colocamos no Hash as tebelas que queremos verificar se foram criadas
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(PopularMoviesContract.MoviesEntry.TABLE_NAME);
        tableNameHashSet.add(PopularMoviesContract.ListsEntry.TABLE_NAME);

        // Conectamos no banco
        SQLiteDatabase db = new PopularMoviesDbHelper(getTargetContext()).getWritableDatabase();

        // Verificamos se a conexão foi bem sucedida, caso contrário o banco não foi criado corretamente
        assertEquals(true, db.isOpen());

        // Buscamos no banco o nome de todas as tabelas criadas
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        // Verificamos se existem tabelas criadas
        assertTrue("Erro: Nenhuma tabela criada", c.moveToFirst());

        // Removemos do Hash as tabelas encontradas
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // Se o hash não está vazio é porque não encontramos alguma tabela criada no banco
        assertTrue("Erro: Algumas tabelas não criadas",
                tableNameHashSet.isEmpty());

        // Coletamos todas as colunas da tabela Lists
        c = db.rawQuery("PRAGMA table_info(" + PopularMoviesContract.ListsEntry.TABLE_NAME + ")",
                null);

        // Verificamos se existem colunas retornadas, caso contrário a tabela foi criada sem colunas
        assertTrue("Erro: Não conseguimos pegar as informações da tabela Lists.",
                c.moveToFirst());

        // Criamos um hash com as colunas que queremos verificar na tabela List
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(PopularMoviesContract.ListsEntry.COLUMN_LIST_TYPE);
        locationColumnHashSet.add(PopularMoviesContract.ListsEntry.COLUMN_MOVIE_KEY);

        // Removemos do hash as colunas encontradas
        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // Se o hash não está vazio é porque não encontramos alguma colunas na tabela Lists
        assertTrue("Erro: A tabela Lists não tem todos os campos necessários",
                locationColumnHashSet.isEmpty());

        // Coletamos todas as colunas da tabela Movies
        c = db.rawQuery("PRAGMA table_info(" + PopularMoviesContract.MoviesEntry.TABLE_NAME + ")",
                null);

        // Verificamos se existem colunas retornadas, caso contrário a tabela foi criada sem colunas
        assertTrue("Erro: Não conseguimos pegar as informações da tabela Movies.",
                c.moveToFirst());

        // Criamos um hash com as colunas que queremos verificar na tabela Movies
        locationColumnHashSet.clear();
        locationColumnHashSet.add(PopularMoviesContract.MoviesEntry.COLUMN_BACKDROP);
        locationColumnHashSet.add(PopularMoviesContract.MoviesEntry.COLUMN_FAVORITE);
        locationColumnHashSet.add(PopularMoviesContract.MoviesEntry.COLUMN_MOVIE_ID);
        locationColumnHashSet.add(PopularMoviesContract.MoviesEntry.COLUMN_ORIGINAL_TITLE);
        locationColumnHashSet.add(PopularMoviesContract.MoviesEntry.COLUMN_OVERVIEW);
        locationColumnHashSet.add(PopularMoviesContract.MoviesEntry.COLUMN_POPULARITY);
        locationColumnHashSet.add(PopularMoviesContract.MoviesEntry.COLUMN_POSTER);
        locationColumnHashSet.add(PopularMoviesContract.MoviesEntry.COLUMN_RELEASE_DATE);
        locationColumnHashSet.add(PopularMoviesContract.MoviesEntry.COLUMN_TITLE);
        locationColumnHashSet.add(PopularMoviesContract.MoviesEntry.COLUMN_VIDEO);
        locationColumnHashSet.add(PopularMoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE);
        locationColumnHashSet.add(PopularMoviesContract.MoviesEntry.COLUMN_VOTE_COUNT);

        // Removemos do hash as colunas encontradas
        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // Se o hash não está vazio é porque não encontramos alguma colunas na tabela Movies
        assertTrue("Erro: A tabela Movies não tem todos os campos necessários",
                locationColumnHashSet.isEmpty());

        db.close();
    }

    @Test
    public void testMoviesTable(){
        insertMovie();
    }

    public void insertMovie(){
        SQLiteDatabase db = new PopularMoviesDbHelper(getTargetContext()).getWritableDatabase();
        ContentValues testValues = TestUtilities.createMovie();

        long movieRowId = db.insert(PopularMoviesContract.MoviesEntry.TABLE_NAME, null, testValues);

        // Testamos se o registro foi inserido
        assertTrue(movieRowId != -1);

        // Buscamos os valores inseridos na tabela
        Cursor cursor = db.query(
                PopularMoviesContract.MoviesEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Verificamos se existem registros
        assertTrue( "Erro: Nenhum registro retornado da tabela Movies", cursor.moveToFirst() );

        // Verificamos se os dados retornados são do registro que incluímos.
        TestUtilities.validateCurrentRecord("Error: Validação do registro na tabela Movies falhou!",
                cursor, testValues);

        // Asseguramos que só existe um registro retornado.
        assertFalse( "Error: More than one record returned from location query", cursor.moveToNext() );

        cursor.close();
        db.close();
    }

    @Test
    public void testListsTable(){
        SQLiteDatabase db = new PopularMoviesDbHelper(getTargetContext()).getWritableDatabase();

        // Geramos os dados fictícios
        ContentValues testValues = TestUtilities.createListWithOneMovie();

        // Inserimos o registro fictício
        long rowId = db.insert(PopularMoviesContract.ListsEntry.TABLE_NAME, null, testValues);

        // Verificamos se ele foi inserido corretamente
        assertTrue(rowId != -1);

        // Buscamos os valores inseridos na tabela
        Cursor cursor = db.query(
                PopularMoviesContract.ListsEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Verificamos se existem registros
        assertTrue( "Erro: Nenhum registro retornado da tabela Movies", cursor.moveToFirst() );

        // Verificamos se os dados retornados são do registro que incluímos.
        TestUtilities.validateCurrentRecord("Error: Validação do registro na tabela Movies falhou!",
                cursor, testValues);

        // Asseguramos que só existe um registro retornado.
        assertFalse( "Error: More than one record returned from location query", cursor.moveToNext() );

        cursor.close();
        db.close();
    }


}