package com.bridge.androidtechnicaltest.data.network

import retrofit2.Retrofit

object PupilServiceFactory {
    fun providePupilApiService(retrofit: Retrofit): PupilNetworkCall {
        return retrofit.create(PupilNetworkCall::class.java)
    }
}