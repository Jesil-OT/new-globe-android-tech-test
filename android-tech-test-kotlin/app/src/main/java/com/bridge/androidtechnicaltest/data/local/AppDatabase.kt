package com.bridge.androidtechnicaltest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bridge.androidtechnicaltest.data.local.model.PupilsEntity

@Database(entities = [PupilsEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val pupilsDao: PupilsDao
}