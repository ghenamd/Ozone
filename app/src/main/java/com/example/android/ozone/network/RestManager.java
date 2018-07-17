package com.example.android.ozone.network;

import com.example.android.ozone.utils.constants.OzoneConstants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestManager {
    private AirVisualClient mClient;

    public AirVisualClient getClient() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(OzoneConstants.AIR_VISUAL_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mClient = retrofit.create(AirVisualClient.class);
        return mClient;
    }
}
