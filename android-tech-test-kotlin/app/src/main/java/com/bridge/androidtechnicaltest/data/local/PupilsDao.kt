package com.bridge.androidtechnicaltest.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bridge.androidtechnicaltest.data.local.model.PupilsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PupilsDao {

    // insert data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPupils(pupils: PupilsEntity)

    // delete a single item if it doesn't exist in remote
    @Query("DELETE FROM Pupils_table WHERE pupil_id = :pupilId")
    suspend fun deletePupil(pupilId: Int)

    // delete all items
    @Query("DELETE FROM Pupils_table")
    suspend fun deletePupils()

    // updated an item
    @Update
    suspend fun updatePupils(pupil: PupilsEntity)

    //get all items
    @Query("SELECT * FROM Pupils_table")
    fun getAllPupils(): Flow<List<PupilsEntity>>

    //get a single item
    @Query("SELECT * FROM Pupils_table WHERE pupil_id = :pupilId")
    suspend fun getPupil(pupilId: Int): Flow<PupilsEntity>

}