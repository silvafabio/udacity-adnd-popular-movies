package br.com.fabioluis.popularmovies.entrypoints.rest.movies.review;

import java.util.List;

import br.com.fabioluis.popularmovies.model.Review;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by silva on 29/01/2017.
 */

public interface ReviewsFromMovieRestTmdb {

    @GET("{movie_id}/reviews")
    Call<List<Review>> getReviews(@Path("movie_id") String movieId, @Query("api_key") String apiKey);
}
