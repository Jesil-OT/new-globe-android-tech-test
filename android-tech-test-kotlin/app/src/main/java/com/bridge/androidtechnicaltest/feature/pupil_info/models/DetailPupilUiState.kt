package com.bridge.androidtechnicaltest.feature.pupil_info.models

import com.bridge.androidtechnicaltest.data.model.Pupil

sealed class DetailPupilUiState {
    data class Success(
        val pupils: Pupil?,
        val isStale: Boolean?
    ) : DetailPupilUiState()
    data class Error(val message: String) : DetailPupilUiState()
    object Loading : DetailPupilUiState()
}
