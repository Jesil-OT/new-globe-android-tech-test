package com.bridge.androidtechnicaltest.data.network.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@InternalSerializationApi @Serializable
data class PupilsResponseDto(
    @SerializedName("items") val pupils: List<PupilsDto>,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("itemCount") val itemCount: Int,
    @SerializedName("totalPages") val totalPages: Int
)
