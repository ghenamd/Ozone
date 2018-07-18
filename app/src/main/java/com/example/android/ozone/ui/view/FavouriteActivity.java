package com.example.android.ozone.ui.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.ozone.R;
import com.example.android.ozone.data.AppDatabase;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.ui.view.adapter.FavouriteAdapter;
import com.example.android.ozone.ui.view.dialog.DetailsDialog;
import com.example.android.ozone.ui.view.settings.SettingsActivity;
import com.example.android.ozone.utils.executors.AppExecutors;
import com.example.android.ozone.utils.helper.Helper;
import com.example.android.ozone.utils.constants.OzoneConstants;
import com.example.android.ozone.utils.notification.NotificationUtils;
import com.example.android.ozone.viewModel.MainViewModel;
import com.example.android.ozone.ui.view.widget.OzoneWidgetIntentService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavouriteActivity extends AppCompatActivity implements FavouriteAdapter.OnLocationClicked,
        SharedPreferences.OnSharedPreferenceChangeListener{
    @BindView(R.id.fav_frag_recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.no_fav_text)
    TextView noFavText;
    private AppDatabase mDatabase;
    private FavouriteAdapter mFavouriteAdapter;
    private SharedPreferences mPreferences;
    public static List<JsonData> favData = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        setTitle(getString(R.string.favourite_activity));
        ButterKnife.bind(this);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        mDatabase = AppDatabase.getInstance(this);
        //Code picked from stackoverflow and adjusted for this app
        final BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(mBottomNavigationView);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && navigationView.isShown()) {
                    navigationView.setVisibility(View.GONE);
                } else if (dy < 0) {
                    navigationView.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        setupViewModel();
        deletePlace();
        OzoneWidgetIntentService.startUpdateOzoneWidget(this.getBaseContext());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mBottomNavigationView = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.map:
                    Intent locationIntent = new Intent(FavouriteActivity.this,MapActivity.class);
                    startActivity(locationIntent);
                    break;
                case R.id.current_location:
                    Intent favouriteIntent = new Intent(FavouriteActivity.this,LocationActivity.class);
                    startActivity(favouriteIntent);
                    break;
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
    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getLocation().observe(this, new Observer<List<JsonData>>() {
            @Override
            public void onChanged(@Nullable List<JsonData> jsonData) {
                if ((jsonData != null)) {
                    favData = jsonData;
                    populateUi(jsonData);
                } else {
                    noFavText.setVisibility(View.VISIBLE);
                    noFavText.setText(R.string.no_fav_available);
                }
            }
        });

    }

    private void deletePlace() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                List<JsonData> jd = mFavouriteAdapter.getData();
                JsonData jsd = jd.get(viewHolder.getAdapterPosition());
                Helper.showToastDeleted(getApplicationContext(),jsd.getCity());
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<JsonData> jsonData = mFavouriteAdapter.getData();
                        mDatabase.locationDao().deleteLocation(jsonData.get(position));

                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    //Helper method to populate the Ui
    private void populateUi(List<JsonData> data) {
        mFavouriteAdapter = new FavouriteAdapter(new ArrayList<JsonData>(),this,this);
        LinearLayoutManager manager = new LinearLayoutManager(this.getBaseContext());
        mFavouriteAdapter.addData(data);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mFavouriteAdapter);
    }

    @Override
    public void onItemClicked(JsonData data) {
        Intent intent = new Intent(this, DetailsDialog.class);
        intent.putExtra(OzoneConstants.DETAILS, data);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_aqi_key))){
            populateUi(favData);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
