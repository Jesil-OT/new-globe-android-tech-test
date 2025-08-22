package com.bridge.androidtechnicaltest.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bridge.androidtechnicaltest.data.local.AppDatabase

object DatabaseFactory {
    fun provideDatabaseInstance(context: Context) : RoomDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "TechnicalTestDb")
                .fallbackToDestructiveMigration()
                .build()
    }
}