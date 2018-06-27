package com.example.android.ozone.ui.view.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ozone.R;
import com.example.android.ozone.dialog.MarkerDialog;
import com.example.android.ozone.model.PlaceInfo;
import com.example.android.ozone.utils.OzoneConstants;
import com.example.android.ozone.utils.PlaceAutocompleteAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
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
    private PlaceInfo mPlace;

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
        Toast.makeText(getActivity(),"Map is ready",Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        if (isConnected()) {
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        init();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(),R.string.please_check_your_intenet_connection,Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(getActivity(), MarkerDialog.class);
                Bundle bundle =  new Bundle();
                bundle.putString(OzoneConstants.LOCATION,marker.getTitle());
                bundle.putDouble(OzoneConstants.LAT_MAP, marker.getPosition().latitude);
                bundle.putDouble(OzoneConstants.LON_MAP,marker.getPosition().longitude);
                intent.putExtra(OzoneConstants.BUNDLE_MAP,bundle);
                startActivity(intent);
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
    ---------------------------- google places API autocomplete suggestion-----------
     */

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideKeyboard();
            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mPlaceDetailsCallback);

        }
    };
    private ResultCallback<PlaceBuffer> mPlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }
            final Place place = places.get(0);

            try {
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                mPlace.setLat(place.getViewport().getCenter().latitude);
                mPlace.setLon(place.getViewport().getCenter().longitude);
            } catch (Exception e) {
                e.printStackTrace();
            }
            moveCamera(new LatLng(mPlace.getLat(),
                    mPlace.getLon()), ZOOM, mPlace.getName());
            hideKeyboard();
            places.release();
        }

    };
}
