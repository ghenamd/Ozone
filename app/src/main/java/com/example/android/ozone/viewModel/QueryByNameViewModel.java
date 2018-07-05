package com.example.android.ozone.viewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.ozone.data.AppDatabase;

public class QueryByNameViewModel extends ViewModel {

    private LiveData<String> place;

    public QueryByNameViewModel(AppDatabase database, String loc) {
        place = database.locationDao().getCityFromDb(loc);
    }

    public LiveData<String> getPlace() {
        return place;
    }
}
