package com.bridge.androidtechnicaltest.data.db;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import kotlinx.coroutines.flow.Flow;


@Dao
public interface PupilDao {

    @Query("SELECT * FROM Pupils ORDER BY name ASC")
    Flow<List<Pupil>> getPupils();
}
