package com.example.android.ozone.network;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.utils.constants.OzoneConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class FetchData {

    private static final String TAG = "FetchData";


    public FetchData() {
    }


    public static URL createUrl(String lat,String lon){
        Uri buildUri = Uri.parse(OzoneConstants.AIR_VISUAL_API).buildUpon()
                .appendQueryParameter(OzoneConstants.PARAM_LAT,lat)
                .appendQueryParameter(OzoneConstants.PARAM_LON,lon)
                .appendQueryParameter(OzoneConstants.PARAM_KEY,OzoneConstants.API_KEY)
                .build();

        URL url = null;
        try{
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "Error with creating URL ", e);
        }
        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
   //Method to extract JsonObjects from json
    public static JsonData extractFeatureFromJson (String json){
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        JsonData jsonData= null;
        try{
            JSONObject baseJsonResponse = new JSONObject(json);
            JSONObject responseObject = baseJsonResponse.getJSONObject("data");
            String city = responseObject.getString("city");
            String state = responseObject.getString("state");
            String country = responseObject.getString("country");
            JSONObject currentObject = responseObject.getJSONObject("current");
            JSONObject weatherObject = currentObject.getJSONObject("weather");
            String ts = weatherObject.getString("ts");
            int hu = weatherObject.getInt("hu");
            String ic = weatherObject.getString("ic");
            int pr = weatherObject.getInt("pr");
            int tp = weatherObject.getInt("tp");
            int wd = weatherObject.getInt("wd");
            double ws = weatherObject.getDouble("ws");
            JSONObject pollutionObject = currentObject.getJSONObject("pollution");
            int aqius = pollutionObject.getInt("aqius");
            int aqicn = pollutionObject.getInt("aqicn");
            JSONObject currentLocation = responseObject.getJSONObject("location");
            JSONArray coordinates = currentLocation.getJSONArray("coordinates");
            double lon = coordinates.getDouble(0);
            double lat = coordinates.getDouble(1);
            jsonData = new JsonData(city,state,country,ts,hu,ic,pr,tp,wd,ws,aqius,aqicn,lat,lon);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Problem parsing the location JSON results", e);
        }
        return jsonData;
    }
}
