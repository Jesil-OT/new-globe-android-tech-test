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
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T, DataError.NetworkError>{
    return withContext(Dispatchers.IO) {
        try {
            val response: Response<T> = apiCall()
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    Result.Success(data = data)
                } ?: run {
                    Result.Error(error = DataError.NetworkError.EMPTY_RESPONSE)
                }
            }
            else {
                val errorResponse: ErrorResponse = convertErrorBody(response.errorBody())
                Result.Error(error = DataError.NetworkError.ApiError(errorResponse))
            }
        }  catch (e: HttpException){
            e.printStackTrace()
            when (e.code()) {
                    400 -> Result.Error(error = DataError.NetworkError.BAD_REQUEST)
                    401 -> Result.Error(error = DataError.NetworkError.UNAUTHORIZED)
                    413 -> Result.Error(error = DataError.NetworkError.PAYLOAD_TOO_LARGE)
                    500 -> Result.Error(error = DataError.NetworkError.SERVER_ERROR)
                    else -> Result.Error(error = DataError.NetworkError.UNKNOWN_ERROR)
                }
        }
        catch (e: java.io.IOException){
            e.printStackTrace()
            Result.Error(error = DataError.NetworkError.NO_INTERNET_CONNECTION)
        }
        catch (e: CancellationException) {
            throw e.cause ?: e
        }
        catch (e: Exception) {
            e.printStackTrace()
            Result.Error(error = DataError.NetworkError.UNKNOWN_ERROR)
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

sealed interface Result<out D, out E>{
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E>(val error: E): Result<Nothing, E>
}