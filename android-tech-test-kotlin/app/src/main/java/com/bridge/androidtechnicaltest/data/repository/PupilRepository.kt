package com.bridge.androidtechnicaltest.data.repository

import com.bridge.androidtechnicaltest.core.DataError
import com.bridge.androidtechnicaltest.data.local.PupilsDao
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.network.PupilApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.InternalSerializationApi
import com.bridge.androidtechnicaltest.core.Result
import com.bridge.androidtechnicaltest.data.mapper.fromPupilEntity
import com.bridge.androidtechnicaltest.data.mapper.toPupil
import com.bridge.androidtechnicaltest.data.mapper.toPupilEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


class PupilsRepositoryImpl(
    private val localDataSource: PupilsDao,
    private val remoteDataSource: PupilApiService
) : PupilsRepository {
    @OptIn(InternalSerializationApi::class)
    override suspend fun getAllPupils(): Result<Flow<List<Pupil>>, String> {
//        val apiResult = async { remoteDataSource.getAllPupils() }

    }

    @OptIn(InternalSerializationApi::class)
    private fun getPupilFromRemote(): Flow<List<Pupil>> {
        return flow {
                when (val pupils = remoteDataSource.getAllPupils()){
                    is Result.Success -> {
                        // save to database
                        val pupilsList = pupils.data.pupils.map { it.toPupil() }.toTypedArray()
                        savePupilsToLocal(*pupilsList)
                        // get from database
                        emitAll(
                            localDataSource.getAllPupils()
                                .map { list -> list.map { it.fromPupilEntity() } }
                                .catch {
                                    // handle internal error
                                    emit(emptyList())
                                }
                        )
                    }
                    is Result.Error -> {
                        when (pupils.error){
                            is DataError.NetworkError.NO_INTERNET_CONNECTION -> {}
                            is DataError.NetworkError.BAD_REQUEST -> {}
                            is DataError.NetworkError.UNAUTHORIZED -> {}
                            is DataError.NetworkError.PAYLOAD_TOO_LARGE -> {}
                            is DataError.NetworkError.EMPTY_RESPONSE -> {}
                            is DataError.NetworkError.UNKNOWN_ERROR -> {}
                            is DataError.NetworkError.SERVER_ERROR -> {}
                            is DataError.NetworkError.ApiError -> {}
                        }
                    }
                }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun savePupilsToLocal(vararg pupils: Pupil){
        localDataSource.insertPupils(
            pupils = pupils.map { it.toPupilEntity() }.toTypedArray()
        )
    }

}

interface PupilsRepository{
    // get all pupils from remote and save in local to display when no network or no internet or server error
    suspend fun getAllPupils():  Result<Flow<List<Pupil>>, String>
}