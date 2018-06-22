package com.example.android.ozone.network;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.ozone.data.AppDatabase;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.utils.OzoneConstants;

import java.io.IOException;
import java.net.URL;

public class AQIntentService extends JobIntentService {
   //We use JobIntentService to target Android SDK 26 and up
    private static final String TAG = "AQIntentService";
    public static final String ACTION = "Action";
    public AQIntentService() {
        super();
    }
    private AppDatabase mDb;




    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        mDb = AppDatabase.getInstance(this);
        Bundle bundle = intent.getBundleExtra(OzoneConstants.BUNDLE);
        String lat = String.valueOf(bundle.getDouble(OzoneConstants.LAT));
        String lon = String.valueOf(bundle.getDouble(OzoneConstants.LON));
        URL url = FetchData.createUrl(String.valueOf(lat),String.valueOf(lon));
        Log.d(TAG, url.toString());
        String reply=null;
        try {
            reply = FetchData.getResponseFromHttpUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final JsonData jsonData = FetchData.extractFeatureFromJson(reply);
        mDb.locationDao().insertLocation(jsonData);
         //Send @jsonData to LocationFragment using a BroadCastReceiver.
        Intent senddata = new Intent(ACTION);
        senddata.putExtra("resultCode", Activity.RESULT_OK);
        senddata.putExtra("data",jsonData);
        LocalBroadcastManager.getInstance(this).sendBroadcast(senddata);

    }


}