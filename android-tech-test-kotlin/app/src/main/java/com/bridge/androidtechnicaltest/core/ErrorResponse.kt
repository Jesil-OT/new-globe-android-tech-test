package com.bridge.androidtechnicaltest.core

import kotlinx.serialization.Contextual
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@InternalSerializationApi @Serializable
data class ErrorResponse(
    @SerialName("type") val errorType: String,
    @SerialName("title") val errorTitle: String,
    @SerialName("status") val errorStatus: Int,
    @SerialName("detail") val errorDetail: String,
    @SerialName("instance") val errorInstance: String,
    @Contextual
    @SerialName("additionalProp1") val errorAdditionalProp1: Any?,
    @SerialName("additionalProp2") val errorAdditionalProp2: Any?,
    @SerialName("additionalProp3") val errorAdditionalProp3: Any?,
)