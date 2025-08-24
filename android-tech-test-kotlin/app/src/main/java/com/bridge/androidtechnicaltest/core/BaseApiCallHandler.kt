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
                    404 -> Result.Error(error = NetworkError.NotFound)
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

sealed class NetworkError(errorResponse: ErrorResponse?): Error {
    data class ApiError (val errorResponse: ErrorResponse?): NetworkError(errorResponse)
    object UnknownError: NetworkError(null)
    object ServerError: NetworkError(null)
    object EmptyResponse: NetworkError(null)
    object PayloadTooLarge: NetworkError(null)
    object NotFound: NetworkError(null)
    object Unauthorized: NetworkError(null)
    object BadRequest: NetworkError(null)
    object NoInternetConnection: NetworkError(null)
}

sealed interface Result<out D, out E: RootError>{
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: RootError>(val error: E): Result<Nothing, E>
}