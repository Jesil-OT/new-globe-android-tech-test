package com.bridge.androidtechnicaltest.data.sources.network

import com.bridge.androidtechnicaltest.data.sources.network.model.AddPupilsDto
import com.bridge.androidtechnicaltest.data.sources.network.model.PupilsDto
import com.bridge.androidtechnicaltest.data.sources.network.model.PupilsResponseDto
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
    @GET("pupils")
    suspend fun getPupils(@Query("page") page: Int = 1): Response<PupilsResponseDto>

    //create a new pupil
    @POST("pupils")
    suspend fun createPupil(@Body pupil: AddPupilsDto): Response<PupilsDto>

    //update an existing pupil
    @PUT("pupils/{pupilId}")
    suspend fun editPupil(@Path("pupilId") pupilId: Int, @Body pupil: PupilsDto): Response<PupilsDto>

    //delete an existing pupil
    @DELETE("pupils/{pupilId}")
    suspend fun deletePupil(@Path("pupilId") pupilId: Int): Response<PupilsDto>

    //get a single pupil
    @GET("pupils/{pupilId}")
    suspend fun getPupil(@Path("pupilId") pupilId: Int): Response<PupilsDto>

}