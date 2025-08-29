package com.bridge.androidtechnicaltest.core


sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: Int) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    data class NotFoundData(val message: Int) : Resource<Nothing>()
}