package com.example.android.ozone.sync;

import android.content.Context;
import android.util.Log;

import com.example.android.ozone.data.AppDatabase;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.network.FetchData;
import com.example.android.ozone.utils.notification.NotificationUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class OzoneSyncTask {

    private static final String TAG = "OzoneSyncTask";
    //Task to iterate through the database and update all the locations
    public static void ozoneTask(Context context){
        AppDatabase database = AppDatabase.getInstance(context);
        List<JsonData> dataList = database.locationDao().getAllPlaces();
        for (int i = 0; i <dataList.size() ; i++) {
            JsonData data = dataList.get(i);
            String city = data.getCity();
            String state = data.getState();
            String country = data.getState();
            URL url = FetchData.createCityUrl(city, state, country);
            Log.d(TAG, url.toString());
            String reply = null;
            try {
                reply = FetchData.getResponseFromHttpUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            JsonData jsonData = FetchData.extractFeatureFromJson(reply);
            database.locationDao().insertLocation(jsonData);
        }
        NotificationUtils.showNotificationAfterUpdate(context);
    }



}
