package com.bridge.androidtechnicaltest.feature.edit_pupil

sealed interface EditPupilResponse{
    object Success: EditPupilResponse
    data class Error(val message: String): EditPupilResponse
    object Loading: EditPupilResponse

}