package com.example.android.ozone.ui.view.fragment;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.ozone.R;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.network.FetchData;
import com.example.android.ozone.utils.PlaceAutocompleteAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    @BindView(R.id.text_search_bar)
    AutoCompleteTextView mCompleteTextView;
    @BindView(R.id.gps_icon)
    ImageView mGpsIcon;
    private FusedLocationProviderClient mFusedLocationClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private static final String TAG = "MapsFragment";
    private static final float ZOOM = 10f;
    private static final LatLngBounds LAT_LNG_BOUNDS =
            new LatLngBounds(new LatLng(49.38, -17.39), new LatLng(59.53, 8.96));
    private GoogleApiClient mGoogleApiClient;
    private Marker mMarker;


    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        ButterKnife.bind(this, view);
        initGoogleApi();
        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mMapFragment == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mMapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mMapFragment).commit();
        }
        mMapFragment.getMapAsync(this);

        // Inflate the layout for this fragment
        return view;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (isConnected()) {
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        init();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void init() {
        mCompleteTextView.setAdapter(mPlaceAutocompleteAdapter);
        mCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent) {
                if (action == EditorInfo.IME_ACTION_SEARCH || action == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    locatePlace();
                }
                return false;
            }
        });
        //When the gps icon is clicked it returns the map to the devices current location
        mGpsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });

    }

    //Hides the keyboard
    private void hideKeyboard() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initGoogleApi() {
        //Initialize GoogleApiClient to create a new PlaceAutoCompleteAdapter
        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(getActivity(),
                mGoogleApiClient, LAT_LNG_BOUNDS, null);
    }

    //Method to get the typed string and find the location.
    private void locatePlace() {
        String loc = mCompleteTextView.getText().toString();
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocationName(loc, 1);
        } catch (IOException e) {
            Log.d(TAG, "locatePlace: " + e.getMessage());
        }
        if (addressList.size() > 0) {
            Address address = addressList.get(0);
            double lat = address.getLatitude();
            double lon = address.getLongitude();
            String place = address.getLocality();
            moveCamera(new LatLng(lat, lon), ZOOM, place);
        }
    }

    //Moves Camera to the selected location
    private void moveCamera(final LatLng latLng, float zoom, String title) {
        if (mMarker != null) {
            mMarker.remove();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .title(title);
        mMarker = mMap.addMarker(options);


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        URL url = FetchData.createUrl(String.valueOf(mMarker.getPosition().latitude),
                                String.valueOf(mMarker.getPosition().longitude));
                        String reply = null;
                        try {
                            reply = FetchData.getResponseFromHttpUrl(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        JsonData jsonData = FetchData.extractFeatureFromJson(reply);
                        createCustomDialog(jsonData);
                    }
                });
                return true;
            }
        });

        //Hides the keyboard
        hideKeyboard();
    }


    //Helper method to check if there is Internet connection
    private boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = Objects.requireNonNull(manager).getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }

    //Call it else we get a "Already managing a GoogleApiClient with id 0" error
    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    /*
     ----------------------find device location using FusedLocationProviderClient------------
     */
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
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
                            moveCamera(new LatLng(lat, lon), ZOOM, getString(R.string.my_location));

                        }
                    }
                });
    }

    /*
     ------------- Create an AlertDialog to show AQI of the place when a marker is clicked----------------
     */
    public void createCustomDialog(JsonData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_dialog, null);
        TextView aqi = view.findViewById(R.id.dialog_aqi);
        aqi.setText(data.getAqius());
        builder.setView(view);
        builder.show();
    }

    public class AsyncMap extends AsyncTask<Void, Void, JsonData> {

        @Override
        protected JsonData doInBackground(Void... voids) {
            URL url = FetchData.createUrl(String.valueOf(mMarker.getPosition().latitude),
                    String.valueOf(mMarker.getPosition().longitude));
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
            super.onPostExecute(data);
            createCustomDialog(data);

        }
    }

}
