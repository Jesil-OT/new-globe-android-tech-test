package com.bridge.androidtechnicaltest.core.utils

import com.bridge.androidtechnicaltest.core.NetworkError
import com.bridge.androidtechnicaltest.core.Result
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
fun NetworkError.asUiText(): String{
    return when(this){
        is NetworkError.NoInternetConnection -> "No Internet Connection"
        is NetworkError.BadRequest -> "Bad Request"
        is NetworkError.Unauthorized -> "Unauthorized"
        is NetworkError.PayloadTooLarge -> "Payload Too Large"
        is NetworkError.EmptyResponse -> "Empty Response"
        is NetworkError.UnknownError -> "Unknown Error"
        is NetworkError.ServerError -> "Server Error"
        is NetworkError.NotFound -> "Not Found"
        is NetworkError.ApiError -> { this.errorResponse?.errorTitle ?: "Api Error" }
    }
}

fun Result.Error<NetworkError>.asErrorUiText(): String {
    return error.asUiText()
}

//fun Result.Error<DataError>.asErrorUiText(): String {
//    return error.asUiText()
//}