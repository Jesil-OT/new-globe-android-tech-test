package com.bridge.androidtechnicaltest.core

import android.app.Application
import com.bridge.androidtechnicaltest.core.di.databaseModule
import com.bridge.androidtechnicaltest.core.di.networkModule
import com.bridge.androidtechnicaltest.core.di.pupilModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

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