package com.example.android.ozone.dialog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.android.ozone.R;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.network.FetchData;
import com.example.android.ozone.utils.OzoneConstants;

import java.io.IOException;
import java.net.URL;

public class MarkerDialog extends AppCompatActivity {
    private double lat;
    private double lon;
    private TextView mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        mLocation = findViewById(R.id.dialog_location);

        Bundle bundle = getIntent().getBundleExtra(OzoneConstants.BUNDLE_MAP);
        lat = bundle.getDouble(OzoneConstants.LAT_MAP);
        lon = bundle.getDouble(OzoneConstants.LON_MAP);
        new AsyncMap().execute();
    }

    public class AsyncMap extends AsyncTask<Void, Void, JsonData> {

        @Override
        protected JsonData doInBackground(Void... voids) {
            URL url = FetchData.createUrl(String.valueOf(lat),
                    String.valueOf(lon));
            String reply = null;
            try {
                reply = FetchData.getResponseFromHttpUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return FetchData.extractFeatureFromJson(reply);
        }

        @Override
        protected void onPostExecute(JsonData data) {

            mLocation.setText(data.getCity());
        }
    }


}
