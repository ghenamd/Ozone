package com.example.android.ozone.ui.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ozone.R;
import com.example.android.ozone.ViewModel.MainViewModel;
import com.example.android.ozone.data.AppDatabase;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.network.AQIntentService;
import com.example.android.ozone.ui.ui.adapter.LocationAdapter;
import com.example.android.ozone.utils.AppExecutors;
import com.example.android.ozone.utils.OzoneConstants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.BuildConfig;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class LocationFragment extends Fragment {

    private double lat;
    private double lon;
    private AppDatabase mDb;
    private JsonData mData = new JsonData();
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationAdapter mAdapter;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.recycler_location)
    RecyclerView mRecyclerView;
    @BindView(R.id.internet_connection)
    TextView noInternet;
    private LinearLayoutManager manager;

    public LocationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        manager = new LinearLayoutManager(getActivity().getBaseContext());
        ButterKnife.bind(this, view);
        mAdapter = new LocationAdapter();
        mDb = AppDatabase.getInstance(getActivity());
        if (isConnected()) {
            mProgressBar.setVisibility(View.VISIBLE);
            initPermissions();
            setupViewModel();
        } else if(!isConnected()) {
            noInternet.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    //Request user permission to get the last location
    private void initPermissions() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        getLastLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getActivity(), "Permession Deny", Toast.LENGTH_SHORT).show();
                        if (response.isPermanentlyDenied()) {
                            // navigate to app settings
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient = LocationServices.
                getFusedLocationProviderClient(getActivity().getApplicationContext());
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            lat = location.getLatitude();
                            lon = location.getLongitude();
                            Intent AQIntentService = new Intent(getActivity(), AQIntentService.class);
                            Bundle bundle = new Bundle();
                            bundle.putDouble(OzoneConstants.LAT, lat);
                            bundle.putDouble(OzoneConstants.LON, lon);
                            AQIntentService.putExtra(OzoneConstants.BUNDLE, bundle);
                            getActivity().startService(AQIntentService);
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(AQIntentService.ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    //A BroadcastReceiver is used to get the JsonData object from the AQIntentService
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
            if (resultCode == RESULT_OK) {
                synchronized (getActivity()) {
                    mData = intent.getParcelableExtra("data");
                    populateUi(mData);
                    insertLocation(mData);

                }
            }
        }
    };

    //Helper method to check if there is Internet connection
    private boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = Objects.requireNonNull(manager).getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }

    //Helper method to insert current location into database
    private void insertLocation(final JsonData data) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.locationDao().insertLocation(data);

            }

        });
    }

    //Helper method to populate the UI
    private void populateUi(JsonData jsonData) {
        List<JsonData> mDataList = new ArrayList<>();
        mDataList.add(jsonData);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity().getBaseContext());
        mAdapter.addData(mDataList);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setAdapter(mAdapter);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * If there is no internet connection we can use a ViewModel to retrieve
     * the last location from the database and populate the UI
     */
    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        viewModel.getLocation().observe(this, new Observer<List<JsonData>>() {
            @Override
            public void onChanged(@Nullable List<JsonData> jsonData) {
                if ((jsonData !=null )) {

                }
            }
        });
    }
}
