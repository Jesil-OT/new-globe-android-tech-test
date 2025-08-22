package com.bridge.androidtechnicaltest.di

import com.bridge.androidtechnicaltest.data.db.IPupilRepository
import com.bridge.androidtechnicaltest.data.db.PupilRepository
import com.bridge.androidtechnicaltest.data.local.DatabaseFactory
import com.bridge.androidtechnicaltest.data.network.PupilServiceFactory
import com.bridge.androidtechnicaltest.data.network.RetrofitFactory

import org.koin.dsl.module

val networkModule = module {
//    factory { PupilAPIFactory.retrofitPupil() }
    factory { RetrofitFactory.provideLoggingInterceptor() }
    factory { RetrofitFactory.provideRequestInterceptor() }
    factory { RetrofitFactory.provideOkHttpBuilder(
        loggingInterceptor = get(),
        requestInterceptor = get()
    ) }
    single { RetrofitFactory.provideRetrofit(okHttpClient = get()) }
}

val databaseModule = module {
    factory { DatabaseFactory.provideDatabaseInstance(context =  get()) }
    single<IPupilRepository>{ PupilRepository(get(), get()) }
}

val pupilModule = module {
    single { PupilServiceFactory.providePupilApiService(retrofit = get()) }
}

