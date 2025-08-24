package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.data.local.PupilsDao
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.InternalSerializationApi
import com.bridge.androidtechnicaltest.core.Result
import com.bridge.androidtechnicaltest.data.mapper.fromPupilEntity
import com.bridge.androidtechnicaltest.data.mapper.toPupilDto
import com.bridge.androidtechnicaltest.data.mapper.toPupilEntity
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUiState
import com.bridge.androidtechnicaltest.core.utils.asUiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PupilsRepositoryImpl(
    private val localDataSource: PupilsDao,
    private val remoteDataSource: PupilApiService
) : PupilsRepository {

    override fun fetchPupils(): Flow<PupilUiState> {
        return flow {
            emit(PupilUiState.Loading)
            // fetch data from network
            when (val pupils = remoteDataSource.getAllPupils()) {
                is Result.Success -> {
                    // save to database
                    val remotePupils = pupils.data.pupils
                    localDataSource.deletePupils()
                    remotePupils.forEach {
                        savePupilsToLocal(it.toPupilDto())
                    }

                    emitAll(
                        localDataSource.getAllPupils()
                            .map { localPupil ->
                                PupilUiState.Success(
                                    pupils = localPupil.map { it.fromPupilEntity() },
                                    isStale = true
                                )
                            }.distinctUntilChanged()
                    )
                }

                is Result.Error -> {
                    val error = pupils.error.asUiText()
                    emit(PupilUiState.Error(message = error))
                    emitAll(
                        localDataSource.getAllPupils()
                            .map { localPupil ->
                                PupilUiState.Success(
                                    pupils = localPupil.map { it.fromPupilEntity() },
                                    isStale = false
                                )
                            }.distinctUntilChanged()
                    )
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun savePupilsToLocal(pupils: Pupil) {
        withContext(Dispatchers.IO) {
            localDataSource.insertPupils(pupils.toPupilEntity())
        }
    }

}

interface PupilsRepository {
    /** get all pupils from remote and
     * save in local to display
     * when no network or no internet or server error*/
    fun fetchPupils(): Flow<PupilUiState>
}