package com.example.android.ozone.ui.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.ozone.R;
import com.example.android.ozone.data.AppDatabase;
import com.example.android.ozone.model.JsonData;
import com.example.android.ozone.ui.view.adapter.FavouriteAdapter;
import com.example.android.ozone.ui.view.dialog.DetailsDialog;
import com.example.android.ozone.ui.view.widget.OzoneWidgetIntentService;
import com.example.android.ozone.utils.constants.OzoneConstants;
import com.example.android.ozone.utils.executors.AppExecutors;
import com.example.android.ozone.utils.helper.Helper;
import com.example.android.ozone.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavouriteFragment extends Fragment implements FavouriteAdapter.OnLocationClicked,
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);
        ButterKnife.bind(this, view);
        setRetainInstance(true);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        mDatabase = AppDatabase.getInstance(getActivity());
        setupViewModel();
        deletePlace();
        OzoneWidgetIntentService.startUpdateOzoneWidget(getActivity());
        return view;
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
                Helper.showToastDeleted(getActivity(),jsd.getCity());
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
        mFavouriteAdapter = new FavouriteAdapter(new ArrayList<JsonData>(),this,getActivity());
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        mFavouriteAdapter.addData(data);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mFavouriteAdapter);
    }

    @Override
    public void onItemClicked(JsonData data) {
        Intent intent = new Intent(getActivity(), DetailsDialog.class);
        intent.putExtra(OzoneConstants.DETAILS, data);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_aqi_key))){
            setupViewModel();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
