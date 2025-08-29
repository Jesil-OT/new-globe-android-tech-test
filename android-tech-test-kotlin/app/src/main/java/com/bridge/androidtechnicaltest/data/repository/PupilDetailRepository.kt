package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.core.Resource
import com.bridge.androidtechnicaltest.core.utils.data.NetworkError
import com.bridge.androidtechnicaltest.core.utils.data.NetworkResult
import com.bridge.androidtechnicaltest.data.mapper.fromPupilToEntity
import com.bridge.androidtechnicaltest.data.mapper.toPupilDto
import com.bridge.androidtechnicaltest.data.mapper.toPupilEntity
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.sources.local.PupilsDao
import com.bridge.androidtechnicaltest.data.sources.network.PupilApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

typealias DetailsResourceFlow = Flow<Resource<Pupil>>

const val DETAIL_TAG = "PupilDetailRepository"

class PupilDetailRepositoryImpl(
    private val localDataSource: PupilsDao,
    private val remoteDataSource: PupilApiService
) : PupilDetailRepository {

    private fun getPupilsFromSingleSource(pupilId: Int): Flow<Pupil> =
        localDataSource.getPupil(pupilId)
            .map { localPupil ->
                localPupil.toPupilEntity()
            }.distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    override fun getPupil(pupilId: Int): DetailsResourceFlow = flow {
        // get from cached first
        val cachedPupil = localDataSource.getPupil(pupilId).firstOrNull()

        emit(Resource.Success(data = cachedPupil?.toPupilEntity() ?: emptyPupil()))

        Timber.tag(DETAIL_TAG).d("pupilId: first the pupil id to track pupil changes $pupilId")

        Timber.tag(DETAIL_TAG).d("This is the single source of truth data: $cachedPupil")

        // fetch data from network
        emit(Resource.Loading)
        when (val pupil = remoteDataSource.getPupil(pupilId)) {
            is NetworkResult.Success -> {
                //save to database
                val remotePupil = pupil.data
                // delete from local database to avoid duplication or data inconsistencies
                savePupilToLocal(remotePupil.toPupilDto())
                // emit the new data
                Timber.tag(TAG).d("data from network truth data: $remotePupil")
                emit(
                    Resource.Success(
                        data = getPupilsFromSingleSource(pupilId).first(),
                    )
                )
            }

            is NetworkResult.Error -> {
                when (pupil.error) {
                    is NetworkError.NoInternetConnection -> emit(Resource.Error(R.string.no_internet_error_pupil_detail))
                    is NetworkError.ServerError -> emit(Resource.Error(R.string.swipe_server_error))
                    is NetworkError.NotFound -> {
                        localDataSource.deletePupil(pupilId)
                        emit(Resource.NotFoundData(R.string.not_found_details))
                    }
                    is NetworkError.ServiceUnavailable -> emit(Resource.Error(R.string.swipe_service_unavailable))
                    is NetworkError.ConnectionTimedOut -> emit(Resource.Error(R.string.swipe_connection_timed_out))
                    is NetworkError.BadRequest -> emit(Resource.Error(R.string.bad_request))
                    is NetworkError.ApiError -> emit(Resource.Error(R.string.api_error))
                    is NetworkError.UnknownError -> emit(Resource.Error(R.string.unknown_error))
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    /** approach based facts
     * The primary key must remain stable.
     *
     * Since the endpoint changes any of the pupil's properties
     * including pupil id we use this approach to update the pupil
     *
     * why? If the endpoint changes the ID, Room considers it a new row..
     * so that leads to data inconsistencies.
     * */
    private suspend fun savePupilToLocal(pupils: Pupil) {
        withContext(Dispatchers.IO) {
            localDataSource.deletePupil(pupils.id)
            localDataSource.insertPupil(pupils.fromPupilToEntity())
        }
    }

    private fun emptyPupil() =
        Pupil(
            id = 0,
            firstName = "",
            lastName = "",
            country = "",
            image = "",
            latitude = 0.0,
            longitude = 0.0
        )
}

interface PupilDetailRepository {

    fun getPupil(pupilId: Int): DetailsResourceFlow

}

