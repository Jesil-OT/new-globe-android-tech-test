package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.core.Resource
import com.bridge.androidtechnicaltest.core.utils.data.NetworkError
import com.bridge.androidtechnicaltest.core.utils.data.NetworkResult
import com.bridge.androidtechnicaltest.data.local.PupilsDao
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

typealias DeleteResourceFlow = Flow<Resource<Unit>>

class DeletePupilRepositoryImpl(
    private val localDataSource: PupilsDao,
    private val remoteDataSource: PupilApiService
) : DeletePupilRepository {

    override fun deletePupil(pupilId: Int): DeleteResourceFlow = flow {
        emit(Resource.Loading)

        //delete from network first
        when (val pupil = remoteDataSource.deletePupil(pupilId)) {
            is NetworkResult.Success -> {
                //delete from database
                localDataSource.deletePupil(pupilId)
                emit(Resource.Success(Unit))
            }

            is NetworkResult.Error -> {
                when (pupil.error) {
                    is NetworkError.NoInternetConnection -> emit(Resource.Error(R.string.no_internet_error_delete_pupil))
                    is NetworkError.NotFound -> emit(Resource.NotFoundData(R.string.not_found_delete_pupil))
                    is NetworkError.ServerError -> emit(Resource.Error(R.string.server_error_delete_pupil))
                    is NetworkError.ServiceUnavailable -> emit(Resource.Error(R.string.service_error))
                    is NetworkError.ConnectionTimedOut -> emit(Resource.Error(R.string.swipe_connection_timed_out))
                    is NetworkError.BadRequest -> emit(Resource.Error(R.string.bad_request_delete_pupil))
                    is NetworkError.ApiError -> emit(Resource.Error(R.string.api_error))
                    is NetworkError.UnknownError -> emit(Resource.Error(R.string.unknown_error))
                }
            }
        }
    }
}


interface DeletePupilRepository {
    fun deletePupil(pupilId: Int): DeleteResourceFlow
}