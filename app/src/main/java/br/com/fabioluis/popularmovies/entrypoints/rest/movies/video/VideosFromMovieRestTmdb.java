package br.com.fabioluis.popularmovies.entrypoints.rest.movies.video;

import java.util.List;

import br.com.fabioluis.popularmovies.model.Video;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by silva on 29/01/2017.
 */

public interface VideosFromMovieRestTmdb {

    String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";

    @GET("{movie_id}/videos")
    Call<List<Video>> getVideos(@Path("movie_id") String movieId, @Query("api_key") String apiKey);
}
