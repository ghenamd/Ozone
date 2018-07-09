package com.example.android.ozone.ui.view.fragment;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
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
import com.example.android.ozone.utils.AppExecutors;
import com.example.android.ozone.viewModel.MainViewModel;
import com.example.android.ozone.widget.OzoneWidgetIntentService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavouriteFragment extends Fragment {

    @BindView(R.id.fav_frag_recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.no_fav_text)
    TextView noFavText;
    private AppDatabase mDatabase;
    private FavouriteAdapter mFavouriteAdapter;
    public static List<JsonData> favData = new ArrayList<>();

    public FavouriteFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);
        ButterKnife.bind(this, view);
        mDatabase = AppDatabase.getInstance(getActivity());
        final BottomNavigationView bottom_navigation = getActivity().findViewById(R.id.navigation);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && bottom_navigation.isShown()) {
                    bottom_navigation.setVisibility(View.GONE);
                } else if (dy < 0 ) {
                    bottom_navigation.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        setupViewModel();
        OzoneWidgetIntentService.startUpdateOzoneWidget(getActivity().getBaseContext());
        deletePlace();
        return view;
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        viewModel.getLocation().observe(this, new Observer<List<JsonData>>() {
            @Override
            public void onChanged(@Nullable List<JsonData> jsonData) {
                if ((jsonData != null)) {
                   populateUi(jsonData);
                }else {
                    noFavText.setVisibility(View.VISIBLE);
                    noFavText.setText(R.string.no_fav_available);
                }
            }
        });

       favData = viewModel.getDataList();

    }

    private void deletePlace(){
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
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
    private void populateUi(List<JsonData> data){
        mFavouriteAdapter = new FavouriteAdapter(new ArrayList<JsonData>());
        LinearLayoutManager manager = new LinearLayoutManager(getActivity().getBaseContext());
        mFavouriteAdapter.addData(data);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mFavouriteAdapter);
    }

}
