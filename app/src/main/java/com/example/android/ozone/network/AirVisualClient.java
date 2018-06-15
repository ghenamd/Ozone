package com.example.android.ozone.network;

import com.example.android.ozone.model.JsonData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AirVisualClient {

    @GET("{lat}&{lon}")
    Call<JsonData> getLocation(@Path("lat") double d,
                               @Path("lon") double db,
                               @Query("key") String s);
}
