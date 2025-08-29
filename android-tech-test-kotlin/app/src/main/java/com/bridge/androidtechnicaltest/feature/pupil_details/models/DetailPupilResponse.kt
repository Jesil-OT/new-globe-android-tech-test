package com.bridge.androidtechnicaltest.feature.pupil_details.models

import com.bridge.androidtechnicaltest.data.model.Pupil

sealed class DetailPupilResponse {
    data class InitialData(val pupil: Pupil) : DetailPupilResponse()
    object Loading : DetailPupilResponse()
    data class Error(val message: String) : DetailPupilResponse()
    data class NotFoundData(val message: String) : DetailPupilResponse()
}

sealed interface DeletePupilResponse{
    object Success : DeletePupilResponse
    data class Error(val message: String) : DeletePupilResponse
    object Loading : DeletePupilResponse
}
