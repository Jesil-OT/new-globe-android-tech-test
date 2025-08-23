package com.bridge.androidtechnicaltest.data.network

import com.bridge.androidtechnicaltest.core.DataError
import com.bridge.androidtechnicaltest.core.NetworkError
import com.bridge.androidtechnicaltest.core.Result
import com.bridge.androidtechnicaltest.core.safeApiCall
import com.bridge.androidtechnicaltest.data.network.model.PupilsDto
import com.bridge.androidtechnicaltest.data.network.model.PupilsResponseDto
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

// To provide this in koin
interface PupilApiService {
    //get all pupils from remote
    @OptIn(InternalSerializationApi::class)
    suspend fun getAllPupils(): Result<PupilsResponseDto, NetworkError>
    //create a new pupil
    @OptIn(InternalSerializationApi::class)
    suspend fun createPupil(pupil: PupilsDto): Result<PupilsDto, NetworkError>
    //update an existing pupil

    //delete an existing pupil

    //get a single pupil
}

class PupilApiServiceImpl(private val networkCall: PupilNetworkCall) : PupilApiService {

    @OptIn(InternalSerializationApi::class)
    override suspend fun getAllPupils(): Result<PupilsResponseDto, NetworkError> {
         return safeApiCall{ networkCall.getPupils() }
    }

    @OptIn(InternalSerializationApi::class)
    override suspend fun createPupil(pupil: PupilsDto): Result<PupilsDto, NetworkError> {
        return safeApiCall { networkCall.createPupil(pupil) }
    }

}