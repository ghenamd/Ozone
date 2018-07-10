package com.example.android.ozone.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
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

    @Query(" SELECT * FROM location ")
    List<JsonData> getAllPlaces();

    @Query("SELECT * FROM location WHERE city LIKE :city")
    JsonData getLocationByName(String city);

    @Query("SELECT city FROM location WHERE city LIKE :place")
    LiveData<String> getCityFromDb (String place);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLocation(JsonData data);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateLocation(JsonData data);

    @Delete
    int deleteLocation(JsonData data);

    @Query("DELETE FROM location")
    void deleteAll();

}
