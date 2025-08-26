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
import com.bridge.androidtechnicaltest.data.repository.EditPupilRepository
import com.bridge.androidtechnicaltest.data.repository.EditPupilRepositoryImpl
import com.bridge.androidtechnicaltest.data.repository.PupilDetailRepository
import com.bridge.androidtechnicaltest.data.repository.PupilDetailRepositoryImpl
import com.bridge.androidtechnicaltest.data.repository.PupilsRepository
import com.bridge.androidtechnicaltest.data.repository.PupilsRepositoryImpl
import com.bridge.androidtechnicaltest.feature.add_pupil.AddPupilViewModel
import com.bridge.androidtechnicaltest.feature.edit_pupil.PupilEditViewModel
import com.bridge.androidtechnicaltest.feature.pupil.ui.PupilViewModel
import com.bridge.androidtechnicaltest.feature.pupil_info.PupilDetailViewModel
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
    single<AppDatabase> {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "TechnicalTestDb")
            .fallbackToDestructiveMigration()
            .build()
    }
    single<PupilsDao> { DatabaseFactory.providePupilDao(database = get<AppDatabase>()) }
}

val pupilModule = module {
    single<PupilsRepository>{ PupilsRepositoryImpl(localDataSource = get<PupilsDao>(), remoteDataSource = get<PupilApiService>()) }
    viewModelOf(::PupilViewModel) bind PupilViewModel::class
}

val addPupilModule = module {
    single<AddPupilRepository> { AddPupilRepositoryImpl(remoteDataSource = get<PupilApiService>(), localDataSource = get<PupilsDao>()) }
    viewModelOf(::AddPupilViewModel) bind AddPupilViewModel::class
}
val detailPupilModule = module {
    single<PupilDetailRepository> { PupilDetailRepositoryImpl(localDataSource = get<PupilsDao>(), remoteDataSource = get<PupilApiService>()) }
    viewModelOf(::PupilDetailViewModel) bind PupilDetailViewModel::class
}

val editPupilModule = module {
    single<EditPupilRepository> { EditPupilRepositoryImpl(remoteDataSource = get<PupilApiService>(), localDataSource = get<PupilsDao>()) }
    viewModelOf(::PupilEditViewModel) bind PupilEditViewModel::class
}
