package com.bridge.androidtechnicaltest.feature.pupil.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.repository.PupilsRepository
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUiState
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PupilViewModel(
    private val repository: PupilsRepository
) : ViewModel() {

    private val _pupilUIState = MutableLiveData<PupilUiState>()
    val pupilUIState: LiveData<PupilUiState> = _pupilUIState

    init {
        getAllPupils()
    }

    fun getAllPupils() {
        viewModelScope.launch {
            repository.fetchPupils().collectLatest { uiState ->
                _pupilUIState.value = uiState
            }
        }
    }

}

fun Pupil.toPupilUI(): PupilUI {
    return PupilUI(
        pupilId = id.toString(),
        pupilName = "$firstName  $lastName",
        pupilLocation = "$longitude, $latitude",
        pupilCountry = country,
        pupilImage = image

    )
}