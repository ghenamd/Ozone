package com.example.android.ozone.dialog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.ozone.R;
import com.example.android.ozone.data.AppDatabase;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.network.FetchData;
import com.example.android.ozone.utils.AppExecutors;
import com.example.android.ozone.utils.OzoneConstants;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MarkerDialog extends AppCompatActivity {

    private double lat;
    private double lon;
    private String location;
    @BindView(R.id.dialog_location)
    TextView mLocation;
    @BindView(R.id.dialog_aqi)
    TextView mDialogAqi;
    @BindView(R.id.dialog_temperature)
    TextView mDialogTemperature;
    AsyncMap mAsyncMap;
    @BindView(R.id.dialog_favourite)
    ImageButton mButton;
    AppDatabase mDatabase;
    private JsonData mJsonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        ButterKnife.bind(this);
        mDatabase = AppDatabase.getInstance(this);
        Bundle bundle = getIntent().getBundleExtra(OzoneConstants.BUNDLE_MAP);
        location = bundle.getString(OzoneConstants.LOCATION);
        lat = bundle.getDouble(OzoneConstants.LAT_MAP);
        lon = bundle.getDouble(OzoneConstants.LON_MAP);
        mAsyncMap = new AsyncMap();
        mAsyncMap.execute();

    }

    private boolean isFavourite(final String place) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mJsonData = mDatabase.locationDao().getLocationByName(place);
            }
        });
        if (mJsonData != null && place.equals(mJsonData.getCity())){
            mButton.setImageResource(R.drawable.ic_star_yellow);
            return true;
        }else{
            return false;
        }
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
            mLocation.setText(location);
            if (data != null) {
                mDialogAqi.setText(getString(R.string.aqi) + data.getAqius());

                StringBuilder builder = new StringBuilder();
                builder.append("Temp: ").append(String.valueOf(data.getTp())).append("C");
                mDialogTemperature.setText(builder);
            } else {
                mDialogAqi.setText(R.string.aqi_data_unavailable);
            }
            setAsFavourite(data);
        }
    }
    private void setAsFavourite(final JsonData data){
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFavourite(location)){
                    mButton.setImageResource(R.drawable.ic_star_white);
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDatabase.locationDao().insertLocation(data);
                        }
                    });
                }else if (!isFavourite(location)){
                    mButton.setImageResource(R.drawable.ic_star_yellow);
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDatabase.locationDao().deleteLocation(data);
                        }
                    });

                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAsyncMap.cancel(true);
    }
}
