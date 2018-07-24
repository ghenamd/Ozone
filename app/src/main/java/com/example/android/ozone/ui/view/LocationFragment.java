package com.example.android.ozone.ui.view;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ozone.R;
import com.example.android.ozone.data.AppDatabase;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.network.AQIntentService;
import com.example.android.ozone.ui.view.adapter.LocationAdapter;
import com.example.android.ozone.ui.view.widget.OzoneWidgetIntentService;
import com.example.android.ozone.utils.constants.OzoneConstants;
import com.example.android.ozone.utils.executors.AppExecutors;
import com.example.android.ozone.utils.helper.Helper;
import com.example.android.ozone.utils.sync.OzoneFireBaseJobDispatcher;
import com.example.android.ozone.viewmodel.MainViewModel;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.karumi.dexter.BuildConfig;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.android.ozone.utils.helper.Helper.REQUEST_CHECK_SETTINGS;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class LocationFragment extends Fragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private AppDatabase mDb;
    private JsonData mData;
    private LocationAdapter mAdapter;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.recycler_location)
    RecyclerView mRecyclerView;
    @BindView(R.id.internet_connection)
    TextView noInternet;
    @BindView(R.id.relativeLayout)
    RelativeLayout mRelativeLayout;
    private SharedPreferences mPreferences;

    private long UPDATE_INTERVAL = 10000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

       @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        ButterKnife.bind(this, view);
        setRetainInstance(true);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        mDb = AppDatabase.getInstance(getActivity());
        mAdapter = new LocationAdapter(new JsonData(), getActivity());
        if (Helper.isConnected(getActivity())) {
            mProgressBar.setVisibility(View.VISIBLE);
            initPermissions();
        } else {
            informUserConnectionLost();
        }
        //Hide bottomNavigationView
        final BottomNavigationView bottom_navigation = getActivity().findViewById(R.id.navigation);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && bottom_navigation.isShown()) {
                    bottom_navigation.setVisibility(View.GONE);
                } else if (dy < 0) {
                    bottom_navigation.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        setupViewModel();
        //Initialize FirebaseJobDispatcherSync
        OzoneFireBaseJobDispatcher.initialize(getActivity());
        OzoneWidgetIntentService.startUpdateOzoneWidget(getActivity());
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(AQIntentService.ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    //Request user permission to get the last location
    private void initPermissions() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getActivity(), "Permission Deny", Toast.LENGTH_SHORT).show();
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


    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        getFusedLocationProviderClient(getActivity()).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            // Logic to handle location object
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            Intent AQIntentService = new Intent(getActivity().getApplicationContext(), AQIntentService.class);
                            Bundle bundle = new Bundle();
                            bundle.putDouble(OzoneConstants.LAT, lat);
                            bundle.putDouble(OzoneConstants.LON, lon);
                            AQIntentService.putExtra(OzoneConstants.BUNDLE, bundle);
                            getActivity().startService(AQIntentService);
                        }
                    }
                },
                Looper.myLooper());
    }

    //A BroadcastReceiver is used to get the JsonData object from the AQIntentService
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
            if (resultCode == RESULT_OK) {
                synchronized (this) {
                    mData = intent.getParcelableExtra(getString(R.string.data));
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (mData != null) {
                                mDb.locationDao().insertLocation(mData);
                            }
                        }
                    });
                }
            }
        }
    };

    /**
     * If there is no internet connection we can use a ViewModel to retrieve
     * the last location from the database and populate the UI
     */
    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getLocation().observe(this, new Observer<List<JsonData>>() {
            @Override
            public void onChanged(@Nullable List<JsonData> jsonData) {
                if ((jsonData != null && jsonData.size() > 0)) {

                    JsonData jd = Helper.getFirstListItem(jsonData);
                    if (jd != null) {
                        Helper.populateUi(jd, getActivity(),
                                mAdapter, mRecyclerView, mProgressBar);
                    }
                } else {
                    Helper.settingsRequest(getActivity());
                    startLocationUpdates();
                }
            }
        });

    }


    // Inform user about lost internet connection
    private void informUserConnectionLost() {
        noInternet.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.snackbar_message),
                R.string.connection_lost, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        startLocationUpdates();
                        break;
                    case RESULT_CANCELED:
                        Helper.settingsRequest(getActivity());//keep asking
                        break;
                }
                break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_aqi_key))) {
            setupViewModel();
        }
    }
}
