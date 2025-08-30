package com.bridge.androidtechnicaltest.core.utils.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <reified T> safeApiCall(crossinline apiCall: suspend () -> Response<T>): NetworkResult<T, NetworkError> {
    return withContext(Dispatchers.IO) {
        try {
            val response: Response<T> = apiCall()
            when (response.code()) {
                in 200 until 299 -> {
                    response.body()?.let { body ->
                        NetworkResult.Success(data = body)
                    } ?: run {
                        // Handle the case where the response body is null
                        NetworkResult.Success(Unit as T)
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
                    NetworkResult.Error(error = NetworkError.ApiError)
                }
            }

        } catch (e: SocketTimeoutException) {
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
    object ApiError : NetworkError
    object UnknownError : NetworkError
    object ServerError : NetworkError
    object NotFound : NetworkError
    object BadRequest : NetworkError
    object NoInternetConnection : NetworkError
    object ConnectionTimedOut : NetworkError
    object ServiceUnavailable : NetworkError
}

sealed interface NetworkResult<out D, out E : NetworkError> {
    data class Success<out D>(val data: D) : NetworkResult<D, Nothing>
    data class Error<out E : NetworkError>(val error: E) :
        NetworkResult<Nothing, E>
}