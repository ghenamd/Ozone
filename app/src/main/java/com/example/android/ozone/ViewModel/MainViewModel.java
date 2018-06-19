package com.example.android.ozone.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.android.ozone.data.AppDatabase;
import com.example.android.ozone.model.JsonData;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private LiveData<List<JsonData>> location;
    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase appDatabase = AppDatabase.getInstance(this.getApplication());
        location = appDatabase.locationDao().getAllLocations();
    }

    public LiveData<List<JsonData>> getLocation() {
        return location;
    }
}
