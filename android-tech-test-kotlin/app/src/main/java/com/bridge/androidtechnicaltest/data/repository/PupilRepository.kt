package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.core.DataError
import com.bridge.androidtechnicaltest.core.NetworkError
import com.bridge.androidtechnicaltest.data.local.PupilsDao
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.InternalSerializationApi
import com.bridge.androidtechnicaltest.core.Result
import com.bridge.androidtechnicaltest.data.mapper.fromPupilEntity
import com.bridge.androidtechnicaltest.data.mapper.toPupil
import com.bridge.androidtechnicaltest.data.mapper.toPupilEntity
import com.bridge.androidtechnicaltest.ui.PupilUiState
import com.bridge.androidtechnicaltest.ui.asErrorUiText
import com.bridge.androidtechnicaltest.ui.asUiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
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
    @OptIn(InternalSerializationApi::class)
    override fun fetchPupils(): Flow<PupilUiState> {
        return flow {
            emit(PupilUiState.Loading)
            // Get data from cache first for better UX
            val cachePupils = localDataSource.getAllPupils().firstOrNull()
            cachePupils?.let {
                emit(PupilUiState.Success(pupils = cachePupils.map { it.fromPupilEntity() }, isStale = true))
            }
            // Then data fetch from network
            when (val pupils = remoteDataSource.getAllPupils()) {
                is Result.Success -> {
                    // save to database
                    val remotePupils = pupils.data.pupils
                    savePupilsToLocal(*remotePupils.map { it.toPupil() }.toTypedArray())

                    emitAll(
                        localDataSource.getAllPupils()
                            .map { localPupil ->
                                PupilUiState.Success(pupils = localPupil.map { it.fromPupilEntity() }, isStale = false)
                            }.distinctUntilChanged()
                    )
                }

                is Result.Error -> {
                    if (cachePupils == null) {
                        val error = pupils.error.asUiText()
                        emit(PupilUiState.Error(message = error))
                    } else {
                        emit(PupilUiState.Success(pupils = cachePupils.map { it.fromPupilEntity() }, isStale = true))
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun savePupilsToLocal(vararg pupils: Pupil) {
        withContext(Dispatchers.IO) {
            localDataSource.deletePupils()
            pupils.forEach { pupil ->
                localDataSource.insertPupils(pupil.toPupilEntity())
            }
        }
    }

}

interface PupilsRepository {
    /** get all pupils from remote and
     * save in local to display
     * when no network or no internet or server error*/
    fun fetchPupils(): Flow<PupilUiState>
}