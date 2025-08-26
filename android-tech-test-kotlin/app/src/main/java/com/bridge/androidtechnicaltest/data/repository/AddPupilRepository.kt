package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import com.bridge.androidtechnicaltest.feature.pupil.models.AddPupilResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.bridge.androidtechnicaltest.core.Result
import com.bridge.androidtechnicaltest.core.utils.ui.asUiText
import com.bridge.androidtechnicaltest.data.local.PupilsDao
import com.bridge.androidtechnicaltest.data.mapper.toPupilDto
import com.bridge.androidtechnicaltest.data.mapper.toPupilToAddPupilDto
import com.bridge.androidtechnicaltest.data.mapper.toPupilToEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi

class AddPupilRepositoryImpl(
    private val remoteDataSource: PupilApiService,
    private val localDataSource: PupilsDao
) : AddPupilRepository {

    override fun addPupil(pupil: Pupil): Flow<AddPupilResponse> {
        return flow {
            emit(AddPupilResponse.Loading)
            // making call to the server
            when (val response = remoteDataSource.createPupil(pupil.toPupilToAddPupilDto())) {
                is Result.Success -> {
                    val remotePupil = response.data
                    // save to local Database first since it's the single source of truth
                    savePupilsToLocal(remotePupil.toPupilDto())
                    emit(AddPupilResponse.Success(pupilName = remotePupil.pupilName))
                }

                is Result.Error -> {
                    emit(AddPupilResponse.Error(message = response.error.asUiText()))
                }
            }
        }
    }

    private suspend fun savePupilsToLocal(pupils: Pupil) =
        withContext(Dispatchers.IO) {
            localDataSource.insertPupils(pupils.toPupilToEntity())
        }
}

interface AddPupilRepository {
    // add a new pupil to remote and save to local for current update
    fun addPupil(pupil: Pupil): Flow<AddPupilResponse>
}