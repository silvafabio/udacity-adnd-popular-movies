package br.com.fabioluis.popularmovies.entrypoints.rest.movies;

import java.util.List;

import br.com.fabioluis.popularmovies.model.Movie;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by silva on 29/01/2017.
 */

public interface MovieFromListRestTmdb {

    @GET("{list}")
    Call<List<Movie>> getMovies(@Path("list") String list, @Query("api_key") String apiKey);
}
