package com.bridge.androidtechnicaltest.core

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

@OptIn(InternalSerializationApi::class)
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T, NetworkError> {
    return withContext(Dispatchers.IO) {
        try {
            val response: Response<T> = apiCall()
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    Result.Success(data = data)
                } ?: run {
//                    val errorResponse: ErrorResponse = convertErrorBody(response.errorBody())
                    val errorResponse: ErrorResponse? = response.errorBody()?.parseErrorResponse()
//                    Result.Error(error = NetworkError.ApiError(errorResponse))
                    Result.Error(error = NetworkError.BadRequest)
                }
            }
            else {
                val errorResponse: ErrorResponse = convertErrorBody(response.errorBody())
//                val errorResponse: ErrorResponse? = response.errorBody()?.parseErrorResponse()
                Result.Error(error = NetworkError.ApiError(response.parseErrorResponse()))
//                 error coming from server
                Result.Error(error = NetworkError.BadRequest)

            }
        }
        catch (e: HttpException) {
//            e.printStackTrace()
            when (e.code()) {
                401 -> Result.Error(error = NetworkError.PayloadTooLarge)
                503 -> Result.Error(error = NetworkError.NotFound(null))
                404 -> Result.Error(error = NetworkError.NotFound(e.message()))
                else -> Result.Error(error = NetworkError.UnknownError)
            }
        }
        catch (e: SocketTimeoutException){
            e.printStackTrace()
            Result.Error(error = NetworkError.ConnectionTimedOut)
        }
        catch (e: IOException) {
            e.printStackTrace()
            Result.Error(error = NetworkError.NoInternetConnection)
        } catch (e: CancellationException) {
            throw e.cause ?: e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(error = NetworkError.UnknownError)
        }
    }
}

private fun ResponseBody.parseErrorResponse(): ErrorResponse {
    return try {
        val gson = Gson()
        gson.fromJson(this.charStream(), ErrorResponse::class.java)
    } catch (e: JsonSyntaxException) {
        defaultErrorResponse()
    } catch (e: Exception) {
        defaultErrorResponse()
    }
}

private fun <T> Response<T>.parseErrorResponse(): String {
    return try {
        val jsonObject = JSONObject(this.errorBody()?.string() ?: "EMPTY")
        jsonObject.getString("title")
    } catch (e: Exception) {
        e.message.toString()
    }
}


private fun convertErrorBody(errorBody: ResponseBody?): ErrorResponse {
    return try{
        val json = errorBody?.source()?.readUtf8()
        return if (!json.isNullOrBlank()) {
            Gson().fromJson(json, ErrorResponse::class.java)
        } else {
            defaultErrorResponse()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        defaultErrorResponse()
    }
}

private fun defaultErrorResponse(): ErrorResponse = ErrorResponse(
    errorType = "Unknown from server",
    errorTitle = "Unknown from server",
    errorStatus = 0,
    errorDetail = "Unknown",
    errorInstance = "Unknown",
    errorAdditionalProp1 = null,
    errorAdditionalProp2 = null,
    errorAdditionalProp3 = null
)

sealed interface Error
typealias RootError = Error

sealed interface NetworkError : Error {
    data class ApiError(
//        val errorResponse: ErrorResponse?
        val response: String
    ) : NetworkError
    object UnknownError : NetworkError
    object ServerError : NetworkError
    object EmptyResponse : NetworkError
    object PayloadTooLarge : NetworkError
    data class NotFound(val message: String?) : NetworkError
    object BadRequest : NetworkError
    object NoInternetConnection : NetworkError
    object ConnectionTimedOut : NetworkError
}

sealed interface Result<out D, out E : RootError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : RootError>(val error: E) : Result<Nothing, E>
}