package com.bridge.androidtechnicaltest.data.local

object DatabaseFactory {
    fun providePupilDao(database: AppDatabase) = database.pupilsDao
}