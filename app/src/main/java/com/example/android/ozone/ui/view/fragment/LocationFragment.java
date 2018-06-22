package com.example.android.ozone.ui.view.fragment;

import android.Manifest;
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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.example.android.ozone.utils.AppExecutors;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private static final String TAG = "LocationFragment";

    private AppDatabase mDb;
    private JsonData mData = new JsonData();
    //private FusedLocationProviderClient mFusedLocationClient;
    private LocationAdapter mAdapter;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.recycler_location)
    RecyclerView mRecyclerView;
    @BindView(R.id.internet_connection)
    TextView noInternet;
    @BindView(R.id.relativeLayout)
    RelativeLayout mRelativeLayout;
    private LinearLayoutManager manager;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;
    public LocationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        mDb = AppDatabase.getInstance(getActivity());
        manager = new LinearLayoutManager(getActivity().getBaseContext());
        ButterKnife.bind(this, view);
        mAdapter = new LocationAdapter();
        if (isConnected()) {
            mProgressBar.setVisibility(View.VISIBLE);
            initPermissions();
        } else if (!isConnected()) {
            noInternet.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                    "Connection Lost", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        deletePlace();
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

    private void getLastLocation(){
        mFusedLocationClient = LocationServices.
                getFusedLocationProviderClient(getActivity());
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                                Log.d(TAG, "onComplete: " + String.valueOf(mLocation.getLongitude()));
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    //Helper method to populate the UI
    private void populateUi(JsonData jsonData) {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity().getBaseContext());
        List<JsonData> dataList = new ArrayList<>();
        dataList.add(jsonData);
        mAdapter.addData(dataList);
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
                if ((jsonData != null)) {

                }
            }
        });
    }
    private void deletePlace(){

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<JsonData> loc = mAdapter.getLocations();
                        mDb.locationDao().deleteLocation(loc.get(position));
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);
    }
}
