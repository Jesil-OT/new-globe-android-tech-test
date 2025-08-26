package com.bridge.androidtechnicaltest.feature.pupil.models

import com.bridge.androidtechnicaltest.data.model.Pupil

sealed class PupilResponse {
    data class Success(val pupils: List<Pupil>) : PupilResponse()
    data class SuccessFromSingleSource(val pupils: List<Pupil>): PupilResponse()
    data class Error(val message: String) : PupilResponse()
    data class Loading(val pupil: List<Pupil>?) : PupilResponse()
}

sealed class AddPupilResponse {
    data class Success(val pupilName: String) : AddPupilResponse()
    data class Error(val message: String) : AddPupilResponse()
    object Loading : AddPupilResponse()
}