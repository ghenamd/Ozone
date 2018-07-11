package com.example.android.ozone.ui.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.android.ozone.R;
import com.example.android.ozone.ui.view.fragment.FavouriteFragment;
import com.example.android.ozone.ui.view.fragment.LocationFragment;
import com.example.android.ozone.ui.view.fragment.MapsFragment;

public class MainActivity extends AppCompatActivity {

    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActionBar = getSupportActionBar();
        mActionBar.setTitle(R.string.location);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        LocationFragment locationFragment = new LocationFragment();
        transaction.replace(R.id.main_container, locationFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(mBottomNavigationView);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener mBottomNavigationView = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.current_location:
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    LocationFragment locationFragment = new LocationFragment();
                    transaction.replace(R.id.main_container, locationFragment);
                    mActionBar.setTitle(R.string.location);
                    transaction.addToBackStack("t");
                    transaction.commit();
                    return true;
                case R.id.map:
                    FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                    MapsFragment mapsFragment = new MapsFragment();
                    transaction1.replace(R.id.main_container, mapsFragment);
                    mActionBar.setTitle(R.string.map);
                    transaction1.commit();
                    transaction1.addToBackStack("t1");
                    return true;
                case R.id.favourite:
                    FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                    FavouriteFragment favouriteFragment = new FavouriteFragment();
                    transaction2.replace(R.id.main_container, favouriteFragment);
                    mActionBar.setTitle(R.string.favourite);
                    transaction2.addToBackStack("t2");
                    transaction2.commit();
                    return true;
            }
            return false;
        }
    };

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
                return true;
            default:
                break;
        }
        return false;
    }
}
