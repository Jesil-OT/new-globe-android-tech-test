package com.bridge.androidtechnicaltest.core

import android.app.Application
import com.bridge.androidtechnicaltest.di.addPupilModule
import com.bridge.androidtechnicaltest.di.databaseModule
import com.bridge.androidtechnicaltest.di.detailPupilModule
import com.bridge.androidtechnicaltest.di.editPupilModule
import com.bridge.androidtechnicaltest.di.networkModule
import com.bridge.androidtechnicaltest.di.pupilModule
import de.hdodenhof.circleimageview.BuildConfig
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