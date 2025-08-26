package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.data.local.PupilsDao
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import kotlinx.coroutines.flow.Flow
import com.bridge.androidtechnicaltest.core.Result
import com.bridge.androidtechnicaltest.data.mapper.toPupilEntity
import com.bridge.androidtechnicaltest.data.mapper.toPupilDto
import com.bridge.androidtechnicaltest.data.mapper.toPupilToEntity
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilResponse
import com.bridge.androidtechnicaltest.core.utils.ui.asUiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PupilsRepositoryImpl(
    private val localDataSource: PupilsDao,
    private val remoteDataSource: PupilApiService
) : PupilsRepository {

    override fun fetchPupils(): Flow<PupilResponse> {
        return flow {
            emit(PupilResponse.Loading)
            // fetch data from network
            when (val pupils = remoteDataSource.getAllPupils()) {
                is Result.Success -> {
                    // save to database
                    val remotePupils = pupils.data.pupils
                    localDataSource.deletePupils()
                    remotePupils.forEach {
                        savePupilsToLocal(it.toPupilDto())
                    }
                    emit(
                        PupilResponse.Success(
                            pupils = getAllPupilsFromSingleSource().first(),
                            isStale = true
                        )
                    )
                }

                is Result.Error -> {
                    val error = pupils.error.asUiText()
                    emit(PupilResponse.Error(message = error))
                    emit(
                        PupilResponse.Success(
                            pupils = getAllPupilsFromSingleSource().first(),
                            isStale = false
                        )
                    )
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun getAllPupilsFromSingleSource(): Flow<List<Pupil>> {
        return localDataSource.getAllPupils()
            .map { localPupil ->
                localPupil.map { it.toPupilEntity() }
            }.distinctUntilChanged()
    }

    private suspend fun savePupilsToLocal(pupils: Pupil) {
        withContext(Dispatchers.IO) {
            localDataSource.insertPupils(pupils.toPupilToEntity())
        }
    }

}

interface PupilsRepository {
    /** get all pupils from remote and
     * save in local to display
     * when no network or no internet or server error
     * show the on from local*/
    fun fetchPupils(): Flow<PupilResponse>

}