package com.bridge.androidtechnicaltest.data.network.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class PupilsDto(
    @SerializedName("pupilId") val pupilId: Int,
    @SerializedName("name") val pupilName: String,
    @SerializedName("country") val pupilCountry: String,
    @SerializedName("image") val pupilImage: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)
