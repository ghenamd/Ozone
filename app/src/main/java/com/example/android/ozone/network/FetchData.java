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
    private static final String DATA = "data";
    private static final String CITY = "city";
    private static final String STATE = "state";
    private static final String COUNTRY = "country";
    private static final String CURRENT = "current";
    private static final String WEATHER = "weather";
    private static final String TS = "ts";
    private static final String HU = "hu";
    private static final String IC = "ic";
    private static final String PR = "pr";
    private static final String TP = "tp";
    private static final String WD = "wd";
    private static final String WS = "ws";
    private static final String POLLUTION = "pollution";
    private static final String AQIUS = "aqius";
    private static final String AQICN = "aqicn";
    private static final String LOCATION = "location";
    private static final String COORDINATES = "coordinates";

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
            JSONObject responseObject = baseJsonResponse.getJSONObject(DATA);
            String city = responseObject.getString(CITY);
            String state = responseObject.getString(STATE);
            String country = responseObject.getString(COUNTRY);
            JSONObject currentObject = responseObject.getJSONObject(CURRENT);
            JSONObject weatherObject = currentObject.getJSONObject(WEATHER);
            String ts = weatherObject.getString(TS);
            int hu = weatherObject.getInt(HU);
            String ic = weatherObject.getString(IC);
            int pr = weatherObject.getInt(PR);
            int tp = weatherObject.getInt(TP);
            int wd = weatherObject.getInt(WD);
            double ws = weatherObject.getDouble(WS);
            JSONObject pollutionObject = currentObject.getJSONObject(POLLUTION);
            int aqius = pollutionObject.getInt(AQIUS);
            int aqicn = pollutionObject.getInt(AQICN);
            JSONObject currentLocation = responseObject.getJSONObject(LOCATION);
            JSONArray coordinates = currentLocation.getJSONArray(COORDINATES);
            double lon = coordinates.getDouble(0);
            double lat = coordinates.getDouble(1);
            jsonData = new JsonData(city,state,country,ts,hu,ic,pr,tp,wd,ws,aqius,aqicn,lat,lon);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonData;
    }
}
