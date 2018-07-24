package com.example.android.ozone.ui.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ozone.R;
import com.example.android.ozone.ui.view.adapter.PlaceAutocompleteAdapter;
import com.example.android.ozone.ui.view.dialog.MarkerDialog;
import com.example.android.ozone.utils.constants.OzoneConstants;
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
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.LOCATION_SERVICE;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    @BindView(R.id.text_search_bar)
    AutoCompleteTextView mCompleteTextView;
    @BindView(R.id.gps_icon)
    ImageView mGpsIcon;
    public static final String ADDRESS_KEY = "address_key";
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private static final String TAG = "MapsFragment";
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private static final float ZOOM = 10f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(49.38, -17.39), new LatLng(59.53, 8.96));
    private GoogleApiClient mGoogleApiClient;
    private Marker mMarker;
    private Address mAddress;

       @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        ButterKnife.bind(this, view);
        setRetainInstance(true);
        if (savedInstanceState != null) {
            mAddress = savedInstanceState.getParcelable(ADDRESS_KEY);
        }
        initGoogleApi();
        initMap();
        return view;
    }


    private void initMap() {
        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mMapFragment == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mMapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mMapFragment).commit();
        }
        mMapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
           Toast.makeText(getActivity(),"Map is ready",Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        locatePlace();
        initPlaceAutoComplete();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), R.string.please_check_your_intenet_connection, Toast.LENGTH_SHORT).show();
    }

    private void initPlaceAutoComplete() {
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
        //When the gps icon is clicked it moves camera to the devices current location
        mGpsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
                isGPSEnabled();
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
            mAddress = addressList.get(0);
            double lat = mAddress.getLatitude();
            double lon = mAddress.getLongitude();
            moveCameraTo(new LatLng(lat, lon), ZOOM);
        }
    }

    //Moves Camera to the selected location
    private void moveCameraTo(final LatLng latLng, float zoom) {
        if (mMarker != null) {
            mMarker.remove();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        mMarker = mMap.addMarker(options);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(getActivity(), MarkerDialog.class);
                Bundle bundle = new Bundle();
                bundle.putString(OzoneConstants.LOCATION, marker.getTitle());
                bundle.putDouble(OzoneConstants.LAT_MAP, marker.getPosition().latitude);
                bundle.putDouble(OzoneConstants.LON_MAP, marker.getPosition().longitude);
                intent.putExtra(OzoneConstants.BUNDLE_MAP, bundle);
                startActivity(intent);
                return true;
            }
        });
        //Hides the keyboard
        hideKeyboard();
    }

    //Call it else we get a "Already managing a GoogleApiClient with id 0" error
    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    /*
     ----------------------Find device location using FusedLocationProviderClient------------
     */
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.
                getFusedLocationProviderClient(getActivity());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            moveCameraTo(new LatLng(lat, lon), ZOOM);

                        }

                    }
                });
    }

    public void isGPSEnabled() {
        LocationManager service = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Toast.makeText(getActivity(), R.string.please_enable_gps, Toast.LENGTH_SHORT).show();
        }

    }

    //Save marker position
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(ADDRESS_KEY, mAddress);
        }
        super.onSaveInstanceState(outState);
    }
}
