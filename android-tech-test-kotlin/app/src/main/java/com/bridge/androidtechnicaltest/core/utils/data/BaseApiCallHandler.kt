@file:Suppress("UNCHECKED_CAST")

package com.bridge.androidtechnicaltest.core.utils.data

import com.bridge.androidtechnicaltest.data.sources.network.model.ErrorResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <T> safeApiCall(crossinline apiCall: suspend () -> Response<T>): NetworkResult<T, NetworkError> {
    return withContext(Dispatchers.IO) {
        try {
            val response: Response<T> = apiCall()
            when (response.code()) {
                in 200 until 299 -> {
                    response.body()?.let { body ->
                        NetworkResult.Success(data = body)
                    } ?: run {
                        NetworkResult.Success(data = Unit as? T?: run {
                            throw Exception("Response body is null")
                        })
                    }
                }
                404 -> {
                    NetworkResult.Error(error = NetworkError.NotFound)
                }
                400 -> {
                    NetworkResult.Error(error = NetworkError.BadRequest)
                }
                in 500 until 600 -> {
                    NetworkResult.Error(error = NetworkError.ServiceUnavailable)
                }
                else -> {
                    val errorResponse = response.errorBody()?.string()
                    val gson = Gson()
                    val parsedError = errorResponse?.let {
                        gson.fromJson(it, ErrorResponse::class.java)
                    }
                    NetworkResult.Error(error = NetworkError.ApiError(parsedError?.errorTitle))
                }
            }

        }
        catch (e: SocketTimeoutException) {
            e.printStackTrace()
            NetworkResult.Error(error = NetworkError.ConnectionTimedOut)
        } catch (e: IOException) {
            e.printStackTrace()
            NetworkResult.Error(error = NetworkError.NoInternetConnection)
        } catch (e: CancellationException) {
            throw e.cause ?: e
        } catch (e: Exception) {
            e.printStackTrace()
            NetworkResult.Error(error = NetworkError.UnknownError)
        }
    }
}

sealed interface NetworkError {
    data class ApiError(val message: String?) : NetworkError
    object UnknownError : NetworkError
    object ServerError : NetworkError
    object NotFound : NetworkError
    object BadRequest : NetworkError
    object NoInternetConnection : NetworkError
    object ConnectionTimedOut : NetworkError
    object ServiceUnavailable: NetworkError
}

sealed interface NetworkResult<out D, out E : NetworkError> {
    data class Success<out D>(val data: D) : NetworkResult<D, Nothing>
    data class Error<out E : NetworkError>(val error: E, val errorCode: Int? = null) :
        NetworkResult<Nothing, E>
}