package com.bridge.androidtechnicaltest.core

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import kotlin.coroutines.cancellation.CancellationException

@OptIn(InternalSerializationApi::class)
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T, NetworkError>{
    return withContext(Dispatchers.IO) {
        try {
            val response: Response<T> = apiCall()
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    Result.Success(data = data)
                } ?: run { Result.Error(error = NetworkError.EmptyResponse) }
            }
            else {
                val errorResponse: ErrorResponse = convertErrorBody(response.errorBody())
                Result.Error(error = NetworkError.ApiError(errorResponse))
            }
        }  catch (e: HttpException){
            e.printStackTrace()
            when (e.code()) {
                    400 -> Result.Error(error = NetworkError.BadRequest)
                    401 -> Result.Error(error = NetworkError.Unauthorized)
                    413 -> Result.Error(error = NetworkError.PayloadTooLarge)
                    500 -> Result.Error(error = NetworkError.ServerError)
                    else -> Result.Error(error = NetworkError.UnknownError)
                }
        }
        catch (e: java.io.IOException){
            e.printStackTrace()
            Result.Error(error = NetworkError.NoInternetConnection)
        }
        catch (e: CancellationException) {
            throw e.cause ?: e
        }
        catch (e: Exception) {
            e.printStackTrace()
            Result.Error(error = NetworkError.UnknownError)
        }
    }
}


@OptIn(InternalSerializationApi::class)
private fun convertErrorBody(errorBody: ResponseBody?): ErrorResponse {
    return try {
        val json = errorBody?.source()?.readUtf8()
        return if (!json.isNullOrBlank()) {
            Gson().fromJson(json, ErrorResponse::class.java)
        } else {
            defaultErrorResponse()
        }
    }
    catch (e: Exception){
        e.printStackTrace()
        defaultErrorResponse()
    }
}

@OptIn(InternalSerializationApi::class)
private fun defaultErrorResponse(): ErrorResponse = ErrorResponse(
    errorType = "Unknown",
    errorTitle = "Unknown",
    errorStatus = 0,
    errorDetail = "Unknown",
    errorInstance = "Unknown",
    errorAdditionalProp1 = null,
    errorAdditionalProp2 = null,
    errorAdditionalProp3 = null
)

sealed interface Error
typealias RootError = Error

sealed interface DataError: Error {
    sealed class NetworkError : DataError {
        object NO_INTERNET_CONNECTION : NetworkError()
        object BAD_REQUEST : NetworkError()
        object UNAUTHORIZED : NetworkError()
        object PAYLOAD_TOO_LARGE : NetworkError()
        object EMPTY_RESPONSE : NetworkError()
        object UNKNOWN_ERROR : NetworkError()
        object SERVER_ERROR : NetworkError()
        data class ApiError @OptIn(InternalSerializationApi::class) constructor(val errorResponse: ErrorResponse?) : NetworkError()
    }
}

@OptIn(InternalSerializationApi::class)
sealed class NetworkError(errorResponse: ErrorResponse?): Error {
//    enum class Network: NetworkError {
//        NO_INTERNET_CONNECTION,
//        BAD_REQUEST,
//        UNAUTHORIZED,
//        PAYLOAD_TOO_LARGE,
//        EMPTY_RESPONSE,
//    }
    @OptIn(InternalSerializationApi::class)
    data class ApiError (val errorResponse: ErrorResponse?): NetworkError(errorResponse)
    object UnknownError: NetworkError(null)
    object ServerError: NetworkError(null)
    object EmptyResponse: NetworkError(null)
    object PayloadTooLarge: NetworkError(null)
    object Unauthorized: NetworkError(null)
    object BadRequest: NetworkError(null)
    object NoInternetConnection: NetworkError(null)
}

sealed interface Result<out D, out E: RootError>{
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: RootError>(val error: E): Result<Nothing, E>
}