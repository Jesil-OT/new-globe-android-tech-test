package com.bridge.androidtechnicaltest.data.network

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
    suspend fun getAllPupils(): Result<PupilsResponseDto, NetworkError>
    //create a new pupil
    suspend fun createPupil(pupil: PupilsDto): Result<PupilsDto, NetworkError>
    //update an existing pupil

    //delete an existing pupil
    suspend fun deletePupil(pupilId: Int): Result<PupilsDto, NetworkError>
    //get a single pupil
    suspend fun getPupil(pupilId: Int): Result<PupilsDto, NetworkError>
}

class PupilApiServiceImpl(private val networkCall: PupilNetworkCall) : PupilApiService {

    override suspend fun getAllPupils(): Result<PupilsResponseDto, NetworkError> {
         return safeApiCall{ networkCall.getPupils() }
    }

    override suspend fun createPupil(pupil: PupilsDto): Result<PupilsDto, NetworkError> {
        return safeApiCall { networkCall.createPupil(pupil) }
    }

    override suspend fun deletePupil(pupilId: Int): Result<PupilsDto, NetworkError> {
        return safeApiCall { networkCall.deletePupil(pupilId) }
    }

    override suspend fun getPupil(pupilId: Int): Result<PupilsDto, NetworkError> {
        return safeApiCall { networkCall.getPupil(pupilId) }
    }

}