package com.example.android.ozone.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.ozone.R;
import com.example.android.ozone.constants.OzoneConstants;
import com.example.android.ozone.databinding.FragmentLocationBinding;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.network.AQIntentService;
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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class LocationFragment extends Fragment {

    private static final String TAG = "LocationFragment";
    private double lat;
    private double lon;
    private JsonData mData = new JsonData();
    private FragmentLocationBinding mBinding;

    public LocationFragment() {
        // Required empty public constructor
    }

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_location, container, false);
        initPermissions();
        mBinding.aqius.setText(String.valueOf(mData.getAqius()));


        return mBinding.getRoot();
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

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
            if (resultCode == RESULT_OK) {
                mData = intent.getParcelableExtra("data");

                Log.d(TAG, String.valueOf(mData.getAqius()));

            }
        }
    };
    private void updateUI(){

    }

}
