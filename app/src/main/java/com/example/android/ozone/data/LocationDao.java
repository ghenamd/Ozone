package com.example.android.ozone.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.android.ozone.model.JsonData;

import java.util.List;


@Dao
public interface LocationDao {

    @Query(" SELECT * FROM location ")
    LiveData<List<JsonData>> getAllLocations();
    @Query("SELECT * FROM location WHERE city = :place")
    JsonData getLocationByName(String place);

    @Insert
    void insertLocation (JsonData data);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateLocation(JsonData data);

}
