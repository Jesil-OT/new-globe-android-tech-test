package com.bridge.androidtechnicaltest.core

import android.app.Application
import com.bridge.androidtechnicaltest.core.di.addPupilModule
import com.bridge.androidtechnicaltest.core.di.databaseModule
import com.bridge.androidtechnicaltest.core.di.detailPupilModule
import com.bridge.androidtechnicaltest.core.di.editPupilModule
import com.bridge.androidtechnicaltest.core.di.networkModule
import com.bridge.androidtechnicaltest.core.di.pupilModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        startKoin {
            androidContext(applicationContext)
            modules(
                networkModule,
                databaseModule,
                pupilModule,
                addPupilModule,
                detailPupilModule,
                editPupilModule
            )
        }
    }
}