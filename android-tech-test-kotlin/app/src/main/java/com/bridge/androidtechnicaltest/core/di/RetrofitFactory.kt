package com.bridge.androidtechnicaltest.core.di

import com.bridge.androidtechnicaltest.core.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitFactory {
    fun provideLoggingInterceptor(): HttpLoggingInterceptor{
        return HttpLoggingInterceptor().also {
            it.level = HttpLoggingInterceptor.Level.BODY
        }
    }

    fun provideRequestInterceptor(): Interceptor {
        val requestId = "dda7feeb-20af-415e-887e-afc43f245624"
        val userAgent = "Bridge Android Tech Test"
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .addHeader("X-Request-ID", requestId)
                .addHeader("User-Agent", userAgent)
                .build()
            chain.proceed(newRequest)
        }
    }

    fun provideOkHttpBuilder(
        loggingInterceptor: HttpLoggingInterceptor,
        requestInterceptor: Interceptor
    ): OkHttpClient.Builder {
        return OkHttpClient.Builder().also {
            it.readTimeout(Constants.API_REQUEST_TIMEOUT, TimeUnit.SECONDS)
            it.writeTimeout(Constants.API_REQUEST_TIMEOUT, TimeUnit.SECONDS)
            it.connectTimeout(Constants.API_REQUEST_TIMEOUT, TimeUnit.SECONDS)
            it.addInterceptor(requestInterceptor)
            it.addInterceptor(loggingInterceptor)
        }
    }

    fun provideRetrofit(
        okHttpClient: OkHttpClient.Builder,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }
}