package com.example.android.ozone.ui.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ozone.R;
import com.example.android.ozone.model.PlaceInfo;
import com.example.android.ozone.ui.view.dialog.MarkerDialog;
import com.example.android.ozone.ui.view.settings.SettingsActivity;
import com.example.android.ozone.utils.constants.OzoneConstants;
import com.example.android.ozone.ui.view.adapter.PlaceAutocompleteAdapter;
import com.example.android.ozone.utils.notification.NotificationUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    @BindView(R.id.text_search_bar)
    AutoCompleteTextView mCompleteTextView;
    @BindView(R.id.gps_icon)
    ImageView mGpsIcon;
    @BindView(R.id.map_info)
    ImageView mMapInfo;
    private FusedLocationProviderClient mFusedLocationClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private static final float ZOOM = 10f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(49.38, -17.39), new LatLng(59.53, 8.96));
    private GoogleApiClient mGoogleApiClient;
    private Marker mMarker;
    private PlaceInfo mPlace;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_maps);
        setTitle(getString(R.string.map_activity));
        ButterKnife.bind(this);
        initGoogleApi();
        initMap();
        showMapInfo();
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(mBottomNavigationView);

    }
    private BottomNavigationView.OnNavigationItemSelectedListener mBottomNavigationView = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.current_location:
                    Intent locationIntent = new Intent(MapActivity.this,LocationActivity.class);
                    startActivity(locationIntent);
                    break;
                case R.id.favourite:
                    Intent favouriteIntent = new Intent(MapActivity.this,FavouriteActivity.class);
                    startActivity(favouriteIntent);
                    break;
            }
            return false;
        }
    };
    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        init();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.app_bar_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.notif:
                NotificationUtils.showNotificationAfterUpdate(this);
            default:
                break;
        }
        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MapActivity.this,R.string.please_check_your_intenet_connection,Toast.LENGTH_SHORT).show();
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
                isGPSEnabled();
            }
        });

    }

    //Hides the keyboard
    private void hideKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initGoogleApi() {
        //Initialize GoogleApiClient to create a new PlaceAutoCompleteAdapter
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this,
                mGoogleApiClient, LAT_LNG_BOUNDS, null);
    }

    //Method to get the typed string and find the location.
    private void locatePlace() {
        String loc = mCompleteTextView.getText().toString();
        Geocoder geocoder = new Geocoder(this);
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

            moveCamera(new LatLng(lat, lon), ZOOM);
        }
    }
    private void showMapInfo(){
        mMapInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.snackbar_message), R.string.map_info_message,Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    //Moves Camera to the selected location
    private void moveCamera(final LatLng latLng, float zoom) {
        if (mMarker != null) {
            mMarker.remove();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .title("Click");
        mMarker = mMap.addMarker(options);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(MapActivity.this, MarkerDialog.class);
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

    //Call it else we get a "Already managing a GoogleApiClient with id 0" error
    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
    }

    /*
     ----------------------Find device location using FusedLocationProviderClient------------
     */
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mFusedLocationClient = LocationServices.
                getFusedLocationProviderClient(this.getApplicationContext());
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            moveCamera(new LatLng(lat, lon), ZOOM);

                        }

                    }
                });
    }
    public void isGPSEnabled(){
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Toast.makeText(MapActivity.this,"Please enable your Gps", Toast.LENGTH_SHORT).show();
        }

    }
    /*
    ---------------------------- google places API autocomplete suggestion-----------
     */


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
                    mPlace.getLon()), ZOOM);
            hideKeyboard();
            places.release();
        }

    };
}
