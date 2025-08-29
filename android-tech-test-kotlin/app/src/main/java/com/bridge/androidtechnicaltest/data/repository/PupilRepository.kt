package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.core.Resource
import com.bridge.androidtechnicaltest.core.utils.data.NetworkError
import com.bridge.androidtechnicaltest.core.utils.data.NetworkResult
import com.bridge.androidtechnicaltest.data.local.PupilsDao
import com.bridge.androidtechnicaltest.data.mapper.toPupilDto
import com.bridge.androidtechnicaltest.data.mapper.toPupilEntity
import com.bridge.androidtechnicaltest.data.mapper.fromPupilToEntity
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

typealias PupilResourceFlow = Flow<Resource<List<Pupil>>>

class PupilsRepositoryImpl(
    private val localDataSource: PupilsDao,
    private val remoteDataSource: PupilApiService
) : PupilsRepository {

    override fun fetchPupils() : PupilResourceFlow = flow {
        // get the cached first
        val cachedPupils = localDataSource.getAllPupils().firstOrNull()
            ?.filterNotNull()?.map { it.toPupilEntity() }
        cachedPupils?.let { pupils ->
            emit(Resource.Success(pupils))
        }
        // fetch data from network
        emit(Resource.Loading)
        when (val pupils = remoteDataSource.getAllPupils()) {
            is NetworkResult.Success -> {
                // save to database
                val remotePupils = pupils.data.pupils
                savePupilsToLocal(remotePupils.map { it.toPupilDto() })
                // emit the new data
                emit(Resource.Success(getAllPupilsFromSingleSource().first()))
            }

            is NetworkResult.Error -> {
                when(pupils.error){
                        is NetworkError.NoInternetConnection -> emit(Resource.Error(message = R.string.no_internet_error_pupil_list))
                        is NetworkError.ServerError -> emit(Resource.Error(message = R.string.swipe_server_error))
                        is NetworkError.NotFound -> {
                            localDataSource.deletePupils()
                            emit(Resource.NotFoundData(message = R.string.main_screen_no_pupil_found))
                        }
                        is NetworkError.ServiceUnavailable -> emit(Resource.Error(message = R.string.swipe_service_unavailable))
                        is NetworkError.ConnectionTimedOut -> emit(Resource.Error(message = R.string.swipe_connection_timed_out))
                        is NetworkError.BadRequest -> emit(Resource.Error(message = R.string.bad_request))
                        is NetworkError.ApiError -> emit(Resource.Error(message = R.string.api_error))
                        is NetworkError.UnknownError -> emit(Resource.Error(message = R.string.unknown_error))
                    }
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun getAllPupilsFromSingleSource(): Flow<List<Pupil>> {
        return localDataSource.getAllPupils()
            .map { localPupils ->
                localPupils.map { it.toPupilEntity() }
            }.distinctUntilChanged()
    }

    private suspend fun savePupilsToLocal(pupils: List<Pupil>) {
        withContext(Dispatchers.IO) {
            localDataSource.deletePupils()
            localDataSource.insertPupils(pupils.map { it.fromPupilToEntity() })
        }
    }

}

interface PupilsRepository {

//    fun fetchPupils(): Flow<PupilResponse>
    fun fetchPupils(): PupilResourceFlow

}