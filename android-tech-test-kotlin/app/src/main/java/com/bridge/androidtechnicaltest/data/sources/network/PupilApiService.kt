package com.bridge.androidtechnicaltest.data.sources.network

import com.bridge.androidtechnicaltest.core.utils.data.NetworkError
import com.bridge.androidtechnicaltest.core.utils.data.NetworkResult
import com.bridge.androidtechnicaltest.core.utils.data.safeApiCall
import com.bridge.androidtechnicaltest.data.sources.network.model.AddPupilsDto
import com.bridge.androidtechnicaltest.data.sources.network.model.PupilsDto
import com.bridge.androidtechnicaltest.data.sources.network.model.PupilsResponseDto

// To provide this in koin
interface PupilApiService {
    //get all pupils from remote
    suspend fun getAllPupils(): NetworkResult<PupilsResponseDto, NetworkError>
    //create a new pupil
    suspend fun createPupil(pupil: AddPupilsDto): NetworkResult<PupilsDto, NetworkError>
    //update an existing pupil
    suspend fun editPupil(pupilId: Int, pupil: PupilsDto): NetworkResult<PupilsDto, NetworkError>
    //delete an existing pupil
    suspend fun deletePupil(pupilId: Int): NetworkResult<PupilsDto, NetworkError>
    //get a single pupil
    suspend fun getPupil(pupilId: Int): NetworkResult<PupilsDto, NetworkError>
}

class PupilApiServiceImpl(private val networkCall: PupilNetworkCall) : PupilApiService {

    override suspend fun getAllPupils(): NetworkResult<PupilsResponseDto, NetworkError> {
         return safeApiCall{ networkCall.getPupils() }
    }

    override suspend fun createPupil(pupil: AddPupilsDto): NetworkResult<PupilsDto, NetworkError> {
        return safeApiCall { networkCall.createPupil(pupil) }
    }

    override suspend fun editPupil(pupilId: Int, pupil: PupilsDto): NetworkResult<PupilsDto, NetworkError> {
        return safeApiCall { networkCall.editPupil(pupilId, pupil) }
    }

    override suspend fun deletePupil(pupilId: Int): NetworkResult<PupilsDto, NetworkError> {
        return safeApiCall { networkCall.deletePupil(pupilId) }
    }

    override suspend fun getPupil(pupilId: Int): NetworkResult<PupilsDto, NetworkError> {
        return safeApiCall { networkCall.getPupil(pupilId) }
    }

}