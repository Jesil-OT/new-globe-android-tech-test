package com.bridge.androidtechnicaltest

import android.app.Application
import com.bridge.androidtechnicaltest.di.databaseModule
import com.bridge.androidtechnicaltest.di.networkModule
import com.bridge.androidtechnicaltest.di.pupilModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(
                networkModule,
                databaseModule,
                pupilModule
            )
        }
    }
}