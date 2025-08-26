package com.bridge.androidtechnicaltest.feature.pupil.models

import com.bridge.androidtechnicaltest.data.model.Pupil

sealed class PupilResponse {
    data class Success(
        val pupils: List<Pupil>,
        val isStale: Boolean
    ) : PupilResponse()

    data class Error(val message: String) : PupilResponse()

    object Loading : PupilResponse()
}

sealed class AddPupilResponse {
    data class Success(val pupilName: String) : AddPupilResponse()
    data class Error(val message: String) : AddPupilResponse()
    object Loading : AddPupilResponse()
}