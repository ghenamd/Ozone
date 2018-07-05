package com.example.android.ozone.viewModel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.android.ozone.data.AppDatabase;

public class QueryByNameViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private AppDatabase mDb;
    private String place;

    public QueryByNameViewModelFactory(AppDatabase db, String place) {
        mDb = db;
        this.place = place;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new QueryByNameViewModel(mDb,place);
    }

}
