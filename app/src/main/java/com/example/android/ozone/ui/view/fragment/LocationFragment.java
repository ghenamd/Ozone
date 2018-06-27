package com.example.android.ozone.ui.view.fragment;

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
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ozone.R;
import com.example.android.ozone.ViewModel.MainViewModel;
import com.example.android.ozone.data.AppDatabase;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.network.AQIntentService;
import com.example.android.ozone.ui.view.adapter.LocationAdapter;
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

    private AppDatabase mDb;
    private JsonData mData = new JsonData();
    private LocationAdapter mAdapter;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.recycler_location)
    RecyclerView mRecyclerView;
    @BindView(R.id.internet_connection)
    TextView noInternet;
    @BindView(R.id.relativeLayout)
    RelativeLayout mRelativeLayout;
    //    @BindView(R.id.location_fav)
//    ImageButton mButton;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean databaseIsNotEmpty;

    public LocationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_app_bar,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.app_bar_favourite:

                return true;
            case R.id.app_bar_settings:

                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        ButterKnife.bind(this, view);
        mDb = AppDatabase.getInstance(getActivity());
        mAdapter = new LocationAdapter();
        if (isConnected()) {
            mProgressBar.setVisibility(View.VISIBLE);
            initPermissions();
        } else if (!isConnected()) {
            checkIfAppdatabaseIsEmpty();
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

    //A BroadcastReceiver is used to get the JsonData object from the AQIntentService
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
            if (resultCode == RESULT_OK) {
                synchronized (getActivity()) {
                    mData = intent.getParcelableExtra("data");
                    List<JsonData> list = new ArrayList<>();
                    list.add(mData);
                    populateUi(list);
                }
            }
        }
    };

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
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
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

    /**
     * If there is no internet connection we can use a ViewModel to retrieve
     * the last location from the database and populate the UI
     * In case Appdatabase is empty we inform the user that the internet connection is lost
     */
    private void checkIfAppdatabaseIsEmpty() {
        MainViewModel viewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        viewModel.getLocation().observe(this, new Observer<List<JsonData>>() {
            @Override
            public void onChanged(@Nullable List<JsonData> jsonData) {
                if ((jsonData != null)) {
                    populateUi(jsonData);
                } else {
                    informUserConnectionLost();
                }
            }
        });
    }

//    private void saveToFavourite() {
//        mButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AppExecutors.getInstance().diskIO().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        mDb.locationDao().insertLocation(mData);
//                    }
//                });
//
//            }
//        });
//    }

   // method to inform user about lost internet connection
    private void informUserConnectionLost() {
        noInternet.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                R.string.connection_lost, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    /*
    -------------------------------Helper Methods --------------------------------------------------
     */
    //Helper method to check if there is Internet connection
    private boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = Objects.requireNonNull(manager).getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }
    //Helper method to populate the UI
    private void populateUi(List<JsonData> jsonData) {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity().getBaseContext());
        mAdapter.addData(jsonData);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setAdapter(mAdapter);
        mProgressBar.setVisibility(View.INVISIBLE);
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
}
