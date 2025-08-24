package com.bridge.androidtechnicaltest.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.bridge.androidtechnicaltest.data.local.AppDatabase
import com.bridge.androidtechnicaltest.data.local.DatabaseFactory
import com.bridge.androidtechnicaltest.data.local.PupilsDao
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import com.bridge.androidtechnicaltest.data.network.PupilApiServiceImpl
import com.bridge.androidtechnicaltest.data.network.PupilServiceFactory
import com.bridge.androidtechnicaltest.data.network.RetrofitFactory
import com.bridge.androidtechnicaltest.data.repository.AddPupilRepository
import com.bridge.androidtechnicaltest.data.repository.AddPupilRepositoryImpl
import com.bridge.androidtechnicaltest.data.repository.PupilsRepository
import com.bridge.androidtechnicaltest.data.repository.PupilsRepositoryImpl
import com.bridge.androidtechnicaltest.feature.add_pupil.AddPupilViewModel
import com.bridge.androidtechnicaltest.feature.pupil.ui.PupilViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind

import org.koin.dsl.module

val networkModule = module {
    factory { RetrofitFactory.provideLoggingInterceptor() }
    factory { RetrofitFactory.provideRequestInterceptor() }
    factory { RetrofitFactory.provideOkHttpBuilder(
        loggingInterceptor = get(),
        requestInterceptor = get()
    ) }
    single { RetrofitFactory.provideRetrofit(okHttpClient = get()) }
    single { PupilServiceFactory.providePupilApiService(retrofit = get()) }
    single<PupilApiService> { PupilApiServiceImpl(networkCall = get()) }
}

val databaseModule = module {
//    single  { DatabaseFactory.provideDatabaseInstance(context =  androidContext()) }
    single<AppDatabase> {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "TechnicalTestDb")
            .fallbackToDestructiveMigration()
            .build()
    }
}

val pupilModule = module {
    single<PupilsDao> { DatabaseFactory.providePupilDao(database = get<AppDatabase>()) }
    single<PupilsRepository>{ PupilsRepositoryImpl(localDataSource = get<PupilsDao>(), remoteDataSource = get<PupilApiService>()) }
}

val addPupilModule = module {
    single<AddPupilRepository> { AddPupilRepositoryImpl(remoteDataSource = get<PupilApiService>()) }
}

val viewModelModule = module {
//    viewModelOf(PupilViewModel(repository = get<PupilsRepository>())
    viewModelOf(::PupilViewModel) bind PupilViewModel::class
//    viewModel { AddPupilViewModel(addPupilRepository = get<AddPupilRepository>()) }
    viewModelOf(::AddPupilViewModel) bind AddPupilViewModel::class
}

