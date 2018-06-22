package com.example.android.ozone.ui.view.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.ozone.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    @BindView(R.id.text_search_bar)
     EditText mEditText;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private static final String TAG = "MapsFragment";
    private static final float ZOOM =10f ;


    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        ButterKnife.bind(this,view);
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
        if (isConnected()){
        googleMap.setMyLocationEnabled(true);}
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        getTextFromSearchBar();
    }

    private void getTextFromSearchBar() {
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        //Hides the keyboard
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void locatePlace() {
        String loc = mEditText.getText().toString();
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> addressList = new ArrayList<>();
        try{
            addressList = geocoder.getFromLocationName(loc,1);
        }
        catch (IOException e){
            Log.d(TAG, "locatePlace: " + e.getMessage());
        }
        if (addressList.size()>0){
            Address address = addressList.get(0);
            double lat = address.getLatitude();
            double lon = address.getLongitude();
            String place = address.getLocality();
            moveCamera(new LatLng(lat,lon),ZOOM,place);
        }
    }
    private void moveCamera(LatLng latLng, float zoom,String title){
       mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .title(title);
        mMap.addMarker(options);
        //Hides the keyboard
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    //Helper method to check if there is Internet connection
    private boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = Objects.requireNonNull(manager).getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }


}
