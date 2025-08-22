package com.bridge.androidtechnicaltest.ui

import com.bridge.androidtechnicaltest.core.NetworkError
import com.bridge.androidtechnicaltest.core.Result

fun NetworkError.asUiText(): String{
    return when(this){
        is NetworkError.NoInternetConnection -> "No Internet Connection"
        is NetworkError.BadRequest -> "Bad Request"
        is NetworkError.Unauthorized -> "Unauthorized"
        is NetworkError.PayloadTooLarge -> "Payload Too Large"
        is NetworkError.EmptyResponse -> "Empty Response"
        is NetworkError.UnknownError -> "Unknown Error"
        is NetworkError.ServerError -> "Server Error"
        is NetworkError.ApiError -> "Api Error"
    }
}

fun Result.Error<NetworkError>.asErrorUiText(): String {
    return error.asUiText()
}

//fun Result.Error<DataError>.asErrorUiText(): String {
//    return error.asUiText()
//}