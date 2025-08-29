package com.bridge.androidtechnicaltest.data.sources.local

object DatabaseFactory {
    fun providePupilDao(database: AppDatabase) = database.pupilsDao
}