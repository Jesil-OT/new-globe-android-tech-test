package com.bridge.androidtechnicaltest.data.repository

import android.util.Log
import com.bridge.androidtechnicaltest.core.Constants.BREAK
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
import com.bridge.androidtechnicaltest.di.addPupilModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.cancel
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import kotlin.collections.map

class PupilsRepositoryImpl(
    private val localDataSource: PupilsDao,
    private val remoteDataSource: PupilApiService
) : PupilsRepository {

    override fun fetchPupils(): Flow<PupilResponse> = flow {
            // get the cached first
            val cachedPupils = localDataSource.getAllPupils().firstOrNull()
                ?.map { it.toPupilEntity() }

            cachedPupils?.let {
                emit(PupilResponse.SuccessFromSingleSource(it))
            }
            delay(BREAK)
            emit(PupilResponse.Loading(pupil = cachedPupils))
            // fetch data from network
            when (val pupils = remoteDataSource.getAllPupils()) {
                is Result.Success -> {
                    // save to database
                    val remotePupils = pupils.data.pupils
                    localDataSource.deletePupils()
                    savePupilsToLocal(remotePupils.map { it.toPupilDto() })

                    emit(
                        PupilResponse.Success(
                            pupils = getAllPupilsFromSingleSource().first(),
                        )
                    )
                }

                is Result.Error -> {
                    val error = pupils.error.asUiText()
                    emit(PupilResponse.Error(message = error))
                }
            }
        }.flowOn(Dispatchers.IO)

    private fun getAllPupilsFromSingleSource(): Flow<List<Pupil>> {
        return localDataSource.getAllPupils()
            .map { localPupil ->
                localPupil.map { it.toPupilEntity() }
            }.distinctUntilChanged()
    }

    private suspend fun savePupilsToLocal(pupils: List<Pupil>) {
        withContext(Dispatchers.IO) {
            localDataSource.insertPupils(pupils.map { it.toPupilToEntity() })
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