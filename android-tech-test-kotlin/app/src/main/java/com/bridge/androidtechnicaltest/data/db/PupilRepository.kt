package com.bridge.androidtechnicaltest.data.db

import com.bridge.androidtechnicaltest.data.local.AppDatabase
import io.reactivex.Single

interface IPupilRepository {
    fun getOrFetchPupils() : Single<PupilList>
}
class PupilRepository(val database: AppDatabase, val pupilApi: PupilApi): IPupilRepository {

    override fun getOrFetchPupils(): Single<PupilList> {
        // TODO("Continue with the implementation here")
        return Single.just(PupilList(mutableListOf()))
    }
}