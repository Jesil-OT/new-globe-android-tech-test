package com.bridge.androidtechnicaltest.data.network

import retrofit2.Retrofit

object PupilServiceFactory {
    fun providePupilApiService(retrofit: Retrofit): PupilApiService {
        return retrofit.create(PupilApiService::class.java)
    }
}