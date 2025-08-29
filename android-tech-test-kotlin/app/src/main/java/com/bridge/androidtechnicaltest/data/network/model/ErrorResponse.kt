package com.bridge.androidtechnicaltest.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ErrorResponse(
    @SerialName("type") val errorType: String,
    @SerialName("title") val errorTitle: String,
    @SerialName("status") val errorStatus: Int,
    @SerialName("detail") val errorDetail: String,
    @SerialName("instance") val errorInstance: String,
    @SerialName("additionalProp1") val errorAdditionalProp1: JsonElement? = null,
    @SerialName("additionalProp2") val errorAdditionalProp2: JsonElement? = null,
    @SerialName("additionalProp3") val errorAdditionalProp3: JsonElement? = null,
)