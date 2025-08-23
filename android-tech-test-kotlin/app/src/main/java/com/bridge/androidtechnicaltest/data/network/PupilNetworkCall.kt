package com.bridge.androidtechnicaltest.data.network

import com.bridge.androidtechnicaltest.data.network.model.PupilsDto
import com.bridge.androidtechnicaltest.data.network.model.PupilsResponseDto
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PupilNetworkCall {
    //get all pupils from remote
    @OptIn(InternalSerializationApi::class)
    @GET("pupils")
    suspend fun getPupils(@Query("page") page: Int = 1): Response<PupilsResponseDto>

    //create a new pupil
    @OptIn(InternalSerializationApi::class)
    @POST("pupils")
    suspend fun createPupil(@Body pupil: PupilsDto): Response<PupilsDto>

    //update an existing pupil
    @OptIn(InternalSerializationApi::class)
    @PUT("pupils/{pupilId}")
    suspend fun editPupil(@Path("pupilId") pupilId: Int, @Body pupil: PupilsDto): Response<PupilsDto>

    //delete an existing pupil
    @OptIn(InternalSerializationApi::class)
    @DELETE("pupils/{pupilId}")
    suspend fun deletePupil(@Path("pupilId") pupilId: Int): Response<PupilsDto>

    //get a single pupil
    @OptIn(InternalSerializationApi::class)
    @GET("pupils/{pupilId}")
    suspend fun getPupil(@Path("pupilId") pupilId: Int): Response<PupilsDto>

}