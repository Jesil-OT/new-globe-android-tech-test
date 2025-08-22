package com.bridge.androidtechnicaltest.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

object DatabaseFactory {
    fun provideDatabaseInstance(context: Context) : RoomDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "TechnicalTestDb")
                .fallbackToDestructiveMigration()
                .build()
    }
}