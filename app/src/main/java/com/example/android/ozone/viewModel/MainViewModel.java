package com.example.android.ozone.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.android.ozone.data.AppDatabase;
import com.example.android.ozone.model.JsonData;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<JsonData>> location;
    private List<JsonData> mDataList;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase appDatabase = AppDatabase.getInstance(this.getApplication());
        location = appDatabase.locationDao().getAllLocations();
        mDataList = appDatabase.locationDao().getAllPlaces();

    }

    public LiveData<List<JsonData>> getLocation() {
        return location;
    }
    public List<JsonData> getDataList() {
        return mDataList;
    }
}
