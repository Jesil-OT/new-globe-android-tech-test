package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.data.mapper.toPupil
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import com.bridge.androidtechnicaltest.ui.AddPupilUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.bridge.androidtechnicaltest.core.Result
import com.bridge.androidtechnicaltest.ui.asUiText
import kotlinx.serialization.InternalSerializationApi

class AddPupilRepositoryImpl(private val remoteDataSource: PupilApiService): AddPupilRepository {
    @OptIn(InternalSerializationApi::class)
    override fun addPupil(pupil: Pupil): Flow<AddPupilUiState> {
        return flow {
            emit(AddPupilUiState.Loading)

            when(val response = remoteDataSource.createPupil(pupil.toPupil())){
                is Result.Success -> {
                    emit(AddPupilUiState.Success)
                }
                is Result.Error -> {
                    emit(AddPupilUiState.Error(message = response.error.asUiText()))
                }
            }
        }
    }
}

interface AddPupilRepository{
    // add a new pupil to remote and
    fun addPupil(pupil: Pupil): Flow<AddPupilUiState>
}