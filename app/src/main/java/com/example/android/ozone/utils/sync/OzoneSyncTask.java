package com.example.android.ozone.utils.sync;

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
    public static void ozoneTask(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        List<JsonData> dataList = database.locationDao().getAllPlaces();
        if (dataList != null && dataList.size() > 0) {
            for (int i = 0; i < dataList.size(); i++) {
                JsonData data = dataList.get(i);
                double lat = data.getLat();
                double lon = data.getLon();

                URL url = FetchData.createUrl(String.valueOf(lat), String.valueOf(lon));
                Log.d(TAG, url.toString());
                String reply = null;
                try {
                    reply = FetchData.getResponseFromHttpUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JsonData jsonData = FetchData.extractFeatureFromJson(reply);
                if (jsonData != null) {
                    database.locationDao().insertLocation(jsonData);
                }
            }
        } else {
            return;
        }
        //After data has been updated show user a notification
        //This has been implemented only for practicing purpose
        NotificationUtils.showNotificationAfterUpdate(context);

    }


}
