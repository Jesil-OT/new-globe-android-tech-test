package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.core.Resource
import com.bridge.androidtechnicaltest.core.utils.data.NetworkError
import com.bridge.androidtechnicaltest.core.utils.data.NetworkResult
import com.bridge.androidtechnicaltest.data.local.PupilsDao
import com.bridge.androidtechnicaltest.data.mapper.toPupilDto
import com.bridge.androidtechnicaltest.data.mapper.toCreateDto
import com.bridge.androidtechnicaltest.data.mapper.fromPupilToEntity
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

typealias CreateResourceFlow = Flow<Resource<String>>

class CreatePupilRepositoryImpl(
    private val remoteDataSource: PupilApiService,
    private val localDataSource: PupilsDao
) : CreatePupilRepository {

    override fun createPupil(pupil: Pupil): CreateResourceFlow = flow {
        emit(Resource.Loading)
        // making call to the server
        when (val response = remoteDataSource.createPupil(pupil.toCreateDto())) {
            is NetworkResult.Success -> {
                val remotePupil = response.data.toPupilDto()
                // save to local Database first since it's the single source of truth
                localDataSource.insertPupil(remotePupil.fromPupilToEntity())

                emit(
                    Resource.Success(data = "${remotePupil.firstName} ${remotePupil.lastName}")
                )
            }

            is NetworkResult.Error -> {
                when (response.error) {
                    is NetworkError.NoInternetConnection -> emit(Resource.Error(R.string.no_internet_create_pupil))
                    is NetworkError.ServerError -> emit(Resource.Error(R.string.server_error_create_pupil))
                    is NetworkError.ServiceUnavailable -> emit(Resource.Error(R.string.service_error_create_pupil))
                    is NetworkError.ConnectionTimedOut -> emit(Resource.Error(R.string.connection_time_out_create))
                    is NetworkError.BadRequest -> emit(Resource.Error(R.string.bad_request_create_pupil))
                    is NetworkError.ApiError -> emit(Resource.Error(R.string.api_error))
                    is NetworkError.UnknownError -> emit(Resource.Error(R.string.unknown_error))
                    else -> emit(Resource.Error(R.string.nothing_happens))
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}


interface CreatePupilRepository {
    // add a new pupil to remote and save to local for current update
    fun createPupil(pupil: Pupil): CreateResourceFlow
}