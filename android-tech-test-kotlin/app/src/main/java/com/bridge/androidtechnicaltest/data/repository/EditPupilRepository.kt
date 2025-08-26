package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.core.Result
import com.bridge.androidtechnicaltest.core.utils.ui.asUiText
import com.bridge.androidtechnicaltest.data.local.PupilsDao
import com.bridge.androidtechnicaltest.data.mapper.toPupil
import com.bridge.androidtechnicaltest.data.mapper.toPupilDto
import com.bridge.androidtechnicaltest.data.mapper.toPupilEntity
import com.bridge.androidtechnicaltest.data.mapper.toPupilToEntity
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import com.bridge.androidtechnicaltest.feature.edit_pupil.EditPupilResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class EditPupilRepositoryImpl(
    private val localDataSource: PupilsDao,
    private val remoteDataSource: PupilApiService
) : EditPupilRepository {
    override fun editPupil(
        pupilId: Int,
        pupil: Pupil
    ): Flow<EditPupilResponse> {
        return flow {
            emit(EditPupilResponse.Loading)
            // making call to the server
            when (val pupils = remoteDataSource.editPupil(pupilId, pupil.toPupil())) {
                is Result.Success -> {
                    // save to local Database first since it's the single source of truth
                    val remotePupils = pupils.data
                    // delete from local database to avoid duplication or data inconsistencies
                    localDataSource.deletePupil(pupilId)
                    savePupilsToLocal(remotePupils.toPupilDto())
                    emit(EditPupilResponse.Success)
                }

                is Result.Error -> {
                    emit(EditPupilResponse.Error(message = pupils.error.asUiText()))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getPupilToEdit(pupilId: Int): Flow<Pupil> =
        localDataSource.getPupil(pupilId).flatMapConcat { pupils ->
            flow { emit(pupils.toPupilEntity()) }
        }.flowOn(Dispatchers.IO)


    private suspend fun savePupilsToLocal(pupils: Pupil) =
        withContext(Dispatchers.IO) {
            localDataSource.insertPupils(pupils.toPupilToEntity())

        }
}


interface EditPupilRepository {
    fun editPupil(
        pupilId: Int,
        pupil: Pupil
    ): Flow<EditPupilResponse>

    fun getPupilToEdit(pupilId: Int): Flow<Pupil>
}