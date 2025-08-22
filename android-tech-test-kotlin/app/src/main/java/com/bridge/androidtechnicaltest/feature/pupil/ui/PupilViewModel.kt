package com.bridge.androidtechnicaltest.feature.pupil.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.data.repository.PupilsRepository
import com.bridge.androidtechnicaltest.ui.PupilUiState
import kotlinx.coroutines.launch

class PupilViewModel(
    private val repository: PupilsRepository
): ViewModel() {

    private val _pupilUIState = MutableLiveData<PupilUiState>()
    val pupilUIState: LiveData<PupilUiState> = _pupilUIState

    init {
        getAllPupils()
    }

    private fun getAllPupils() {
        viewModelScope.launch {
            repository.fetchPupils().collect { uiState ->
                _pupilUIState.value = uiState
            }
        }
    }
}