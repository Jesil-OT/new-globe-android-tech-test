package com.bridge.androidtechnicaltest.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Pupils_table")
data class PupilsEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "pupil_id") val pupilId: Int,
    @ColumnInfo(name = "pupil_name") val name: String,
    @ColumnInfo(name = "pupil_country") val country: String,
    @ColumnInfo(name = "pupil_image") val image: String,
    @ColumnInfo(name = "pupil_latitude")  val latitude: Double,
    @ColumnInfo(name = "pupil_longitude") val longitude: Double
)
