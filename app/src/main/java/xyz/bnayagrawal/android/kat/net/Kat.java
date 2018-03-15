package xyz.bnayagrawal.android.kat.net;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by bnayagrawal on 14/3/18.
 */

public interface Kat {
    @GET(Url.PATH_SEARCH + "/{query}/")
    Call<String> searchTorrents(@Path("query") String query);

    @GET("/{path}")
    Call<String> getDocument(@Path("path") String path);

    @GET("/{path}/")
    Call<String> getDocumentCategory(@Path("path") String path);
}
