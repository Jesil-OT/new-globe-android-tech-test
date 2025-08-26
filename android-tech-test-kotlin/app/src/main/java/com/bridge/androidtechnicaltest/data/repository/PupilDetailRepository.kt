package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.core.Result
import com.bridge.androidtechnicaltest.core.utils.ui.asUiText
import com.bridge.androidtechnicaltest.data.local.PupilsDao
import com.bridge.androidtechnicaltest.data.mapper.toPupilEntity
import com.bridge.androidtechnicaltest.data.mapper.toPupilDto
import com.bridge.androidtechnicaltest.data.mapper.toPupilToEntity
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import com.bridge.androidtechnicaltest.feature.pupil_info.models.DetailPupilUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PupilDetailRepositoryImpl(
    private val localDataSource: PupilsDao,
    private val remoteDataSource: PupilApiService
): PupilDetailRepository {

    override fun getPupil(pupilId: Int): Flow<DetailPupilUiState> = flow {
        emit(DetailPupilUiState.Loading)
        // fetch data from network
        when(val pupil = remoteDataSource.getPupil(pupilId)){
            is Result.Success -> {
                //save to database
                val remotePupil = pupil.data
                // delete from local database to avoid duplication or data inconsistencies
                localDataSource.deletePupil(pupilId)
                savePupilsToLocal(remotePupil.toPupilDto())

                val localPupil = localDataSource.getPupil(pupilId)
                val pupil = localPupil.map { pupil ->
                    DetailPupilUiState.Success(
                        pupils = pupil.toPupilEntity(),
                        isStale = true
                    )
                }.distinctUntilChanged()
                emitAll(pupil)
            }
            is Result.Error -> {
                val error = pupil.error.asUiText()
                emit(DetailPupilUiState.Error(message = error))
                val localPupil = localDataSource.getPupil(pupilId)
                val pupil = localPupil.map {
                    DetailPupilUiState.Success(
                        pupils = it.toPupilEntity(),
                        isStale = false
                    )
                }.distinctUntilChanged()
                emitAll(pupil)
            }
        }
    }

    override fun deletePupil(pupilId: Int): Flow<DetailPupilUiState> = flow {
        emit(DetailPupilUiState.Loading)

        //delete from network first
        when(val pupil = remoteDataSource.deletePupil(pupilId)){
            is Result.Success -> {
                //delete from database
                localDataSource.deletePupil(pupilId)
                emit(
                    DetailPupilUiState.Success(
                        pupils = null,
                        isStale = null
                    )
                )
            }
            is Result.Error -> {
                val error = pupil.error.asUiText()
                emit(DetailPupilUiState.Error(message = error))
            }
        }
    }

    private suspend fun savePupilsToLocal(pupils: Pupil){
        withContext(Dispatchers.IO){
            localDataSource.insertPupils(pupils.toPupilToEntity())
        }
    }
}

interface PupilDetailRepository{
    /**
     * get a single pupil from remote and
     * save in local to display
     * when no network or no internet or server error
     * show the on from local*/
    fun getPupil(pupilId: Int): Flow<DetailPupilUiState>

    fun deletePupil(pupilId: Int): Flow<DetailPupilUiState>
}

