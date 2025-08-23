package com.bridge.androidtechnicaltest.ui

import com.bridge.androidtechnicaltest.data.model.Pupil

sealed class PupilUiState {
    data class Success(
        val pupils: List<Pupil>,
        val isStale: Boolean
    ) : PupilUiState()
    data class Error(val message: String) : PupilUiState()
    object Loading : PupilUiState()
}

sealed class AddPupilUiState {
    object Success : AddPupilUiState()
    data class Error(val message: String) : AddPupilUiState()
    object Loading : AddPupilUiState()
}