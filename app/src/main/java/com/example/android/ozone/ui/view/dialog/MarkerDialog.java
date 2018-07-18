package com.example.android.ozone.ui.view.dialog;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.ozone.R;
import com.example.android.ozone.data.AppDatabase;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.network.FetchData;
import com.example.android.ozone.utils.executors.AppExecutors;
import com.example.android.ozone.utils.helper.Helper;
import com.example.android.ozone.utils.constants.OzoneConstants;
import com.example.android.ozone.viewModel.QueryByNameViewModel;
import com.example.android.ozone.viewModel.QueryByNameViewModelFactory;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MarkerDialog extends AppCompatActivity {

    private double lat;
    private double lon;
    @BindView(R.id.dialog_location)
    TextView mLocation;
    @BindView(R.id.dialog_aqi)
    TextView mDialogAqi;
    @BindView(R.id.dialog_temperature)
    TextView mDialogTemperature;
    AsyncMap mAsyncMap;
    @BindView(R.id.dialog_favourite)
    ImageButton mButton;
    private AppDatabase mDatabase;
    @BindView(R.id.dialog_error)
    TextView errorText;
    private static final String TAG = "MarkerDialog";
    public JsonData mData = new JsonData();
    private String loc=null;
    public static final String STATION = " Station";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        ButterKnife.bind(this);
        mDatabase = AppDatabase.getInstance(this);
        Bundle bundle = getIntent().getBundleExtra(OzoneConstants.BUNDLE_MAP);
        lat = bundle.getDouble(OzoneConstants.LAT_MAP);
        lon = bundle.getDouble(OzoneConstants.LON_MAP);
        mAsyncMap = new AsyncMap();
        mAsyncMap.execute();
        setAsFavourite();

    }

    public class AsyncMap extends AsyncTask<Void, Void, JsonData> {

        @Override
        protected JsonData doInBackground(Void... voids) {
            URL url = FetchData.createUrl(String.valueOf(lat),
                    String.valueOf(lon));
            Log.d(TAG, "doInBackground: " + url.toString());
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
            if (data != null) {
                mData = data;
                mLocation.setText(data.getCity() + STATION);
                isFavourite(data.getCity());
                mDialogAqi.setText(getString(R.string.aqi) + data.getAqius());
                mButton.setVisibility(View.VISIBLE);
                StringBuilder builder = new StringBuilder();
                builder.append("Temp: ").append(String.valueOf(data.getTp())).append("C");
                mDialogTemperature.setText(builder);
            } else {
                errorText.setText(R.string.aqi_data_unavailable);
            }

        }
    }

    private void setAsFavourite() {
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFavourite(mData.getCity())) {
                    Log.d(TAG, "MarkerDialog " + mData.getCity() + mData.getState()+ mData.getCountry());
                    mButton.setImageResource(R.drawable.ic_star_white);
                    deleteFavorite(mData);
                    Helper.showToastDeleted(getBaseContext(),mData.getCity());
                } else if (!isFavourite(mData.getCity())) {
                    mButton.setImageResource(R.drawable.ic_star_yellow);
                    addFavorite(mData);
                    Helper.showToastInserted(getBaseContext(),mData.getCity());
                }
            }
        });
    }

    private boolean isFavourite(final String place) {
        QueryByNameViewModelFactory factory = new QueryByNameViewModelFactory(mDatabase,mData.getCity());
        QueryByNameViewModel byNameViewModel = ViewModelProviders.of(this,factory).get(QueryByNameViewModel.class);
        byNameViewModel.getPlace().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String str) {
                loc = str;
            }
        });
        if (place.equals(loc)){
            mButton.setImageResource(R.drawable.ic_star_yellow);
            return true;
        }else{
            return false;
        }
    }

    private void deleteFavorite(final JsonData jsonData) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mDatabase != null) {
                    mDatabase.locationDao().deleteLocation(jsonData);
                }
            }
        });
    }

    private void addFavorite(final JsonData jsonData) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.locationDao().insertLocation(jsonData);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAsyncMap.cancel(true);
    }

}
