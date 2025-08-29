package com.bridge.androidtechnicaltest.di

import androidx.room.Room
import com.bridge.androidtechnicaltest.data.local.AppDatabase
import com.bridge.androidtechnicaltest.data.local.DatabaseFactory
import com.bridge.androidtechnicaltest.data.local.PupilsDao
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import com.bridge.androidtechnicaltest.data.network.PupilApiServiceImpl
import com.bridge.androidtechnicaltest.data.network.PupilServiceFactory
import com.bridge.androidtechnicaltest.data.network.RetrofitFactory
import com.bridge.androidtechnicaltest.data.repository.CreatePupilRepository
import com.bridge.androidtechnicaltest.data.repository.CreatePupilRepositoryImpl
import com.bridge.androidtechnicaltest.data.repository.DeletePupilRepository
import com.bridge.androidtechnicaltest.data.repository.DeletePupilRepositoryImpl
import com.bridge.androidtechnicaltest.data.repository.EditPupilRepository
import com.bridge.androidtechnicaltest.data.repository.EditPupilRepositoryImpl
import com.bridge.androidtechnicaltest.data.repository.PupilDetailRepository
import com.bridge.androidtechnicaltest.data.repository.PupilDetailRepositoryImpl
import com.bridge.androidtechnicaltest.data.repository.PupilsRepository
import com.bridge.androidtechnicaltest.data.repository.PupilsRepositoryImpl
import com.bridge.androidtechnicaltest.feature.create_pupil.ui.AddPupilViewModel
import com.bridge.androidtechnicaltest.feature.edit_pupil.ui.PupilEditViewModel
import com.bridge.androidtechnicaltest.feature.pupil.ui.PupilViewModel
import com.bridge.androidtechnicaltest.feature.pupil_details.ui.PupilDetailViewModel
import org.koin.android.ext.koin.androidContext
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
    single<CreatePupilRepository> { CreatePupilRepositoryImpl(remoteDataSource = get<PupilApiService>(), localDataSource = get<PupilsDao>()) }
    viewModelOf(::AddPupilViewModel) bind AddPupilViewModel::class
}
val detailPupilModule = module {
    single<PupilDetailRepository> { PupilDetailRepositoryImpl(localDataSource = get<PupilsDao>(), remoteDataSource = get<PupilApiService>()) }
    viewModelOf(::PupilDetailViewModel) bind PupilDetailViewModel::class
}

val editPupilModule = module {
    single<DeletePupilRepository> { DeletePupilRepositoryImpl(remoteDataSource = get<PupilApiService>(), localDataSource = get<PupilsDao>()) }
    single<EditPupilRepository> { EditPupilRepositoryImpl(remoteDataSource = get<PupilApiService>(), localDataSource = get<PupilsDao>()) }
    viewModelOf(::PupilEditViewModel) bind PupilEditViewModel::class
}
