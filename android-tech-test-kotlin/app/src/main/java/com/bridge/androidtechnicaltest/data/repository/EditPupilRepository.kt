package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.core.Resource
import com.bridge.androidtechnicaltest.core.utils.data.NetworkError
import com.bridge.androidtechnicaltest.core.utils.data.NetworkResult
import com.bridge.androidtechnicaltest.data.sources.local.PupilsDao
import com.bridge.androidtechnicaltest.data.mapper.fromPupilToEntity
import com.bridge.androidtechnicaltest.data.mapper.toPupil
import com.bridge.androidtechnicaltest.data.mapper.toPupilDto
import com.bridge.androidtechnicaltest.data.mapper.toPupilEntity
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.sources.network.PupilApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

typealias EditResourceFlow = Flow<Resource<Unit>>

class EditPupilRepositoryImpl(
    private val localDataSource: PupilsDao,
    private val remoteDataSource: PupilApiService
) : EditPupilRepository {

    override fun editPupil(
        pupilId: Int,
        pupil: Pupil
    ): EditResourceFlow = flow {
            emit(Resource.Loading)
            // making call to the server
            when (val pupils = remoteDataSource.editPupil(pupilId, pupil.toPupil())) {
                is NetworkResult.Success -> {
                    // save to local Database first since it's the single source of truth
                    val remotePupils = pupils.data
                    savePupilToLocal(remotePupils.toPupilDto())

                    emit(Resource.Success(data = Unit))
                }

                is NetworkResult.Error -> {
                    when (pupils.error) {
                        is NetworkError.NoInternetConnection -> emit(Resource.Error(R.string.no_internet_update_pupil))
                        is NetworkError.ServerError -> emit(Resource.Error(R.string.server_error_update_pupil))
                        is NetworkError.NotFound -> emit(Resource.NotFoundData(R.string.not_found_update_pupil))
                        is NetworkError.ServiceUnavailable -> emit(Resource.Error(R.string.service_error_update_pupil))
                        is NetworkError.ConnectionTimedOut -> emit(Resource.Error(R.string.connection_time_out_update))
                        is NetworkError.BadRequest -> emit(Resource.Error(R.string.bad_request_update_pupil))
                        is NetworkError.ApiError -> emit(Resource.Error(R.string.api_error))
                        is NetworkError.UnknownError -> emit(Resource.Error(R.string.unknown_error))
                    }
                }
            }
        }.flowOn(Dispatchers.IO)

    override fun getPupilToEdit(pupilId: Int): Flow<Pupil> =
        localDataSource.getPupil(pupilId).map {
            it.toPupilEntity()
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
    private suspend fun savePupilToLocal(pupils: Pupil){
        withContext(Dispatchers.IO){
            localDataSource.deletePupil(pupils.id)
            localDataSource.insertPupil(pupils.fromPupilToEntity())
        }
    }
}


interface EditPupilRepository {
    fun editPupil(
        pupilId: Int,
        pupil: Pupil
    ): EditResourceFlow

    fun getPupilToEdit(pupilId: Int): Flow<Pupil>
}