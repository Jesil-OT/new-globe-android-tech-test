package com.bridge.androidtechnicaltest.core.utils

import com.bridge.androidtechnicaltest.core.NetworkError
import com.bridge.androidtechnicaltest.core.Result
import kotlinx.serialization.InternalSerializationApi

fun NetworkError.asUiText(): String{
    return when(this){
        is NetworkError.NoInternetConnection -> "A network error occurred. Please check your internet connection so you can be in sync."
        is NetworkError.BadRequest -> "Bad Request"
        is NetworkError.PayloadTooLarge -> "Payload Too Large"
        is NetworkError.EmptyResponse -> "Pupil was not found"
        is NetworkError.UnknownError -> "Unknown Error"
        is NetworkError.ServerError -> "server-side network issue, please try again later"
        is NetworkError.NotFound -> {
            "Pupil ${this.message}"
        }
        is NetworkError.ConnectionTimedOut -> "The server took too long to respond. Please check your network and try again."
        is NetworkError.ApiError -> {
//            this.errorResponse?.errorTitle ?: "Api Error!!!!"
            this.response
        }
    }
}

fun Result.Error<NetworkError>.asErrorUiText(): String {
    return error.asUiText()
}

//fun Result.Error<DataError>.asErrorUiText(): String {
//    return error.asUiText()
//}