package com.bridge.androidtechnicaltest.feature.pupil_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.data.repository.PupilDetailRepository
import com.bridge.androidtechnicaltest.feature.pupil_info.models.DetailPupilUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PupilDetailViewModel(
    private val repository: PupilDetailRepository
): ViewModel() {
    private val _pupilDetailUiState = MutableLiveData<DetailPupilUiState>()
    val pupilDetailUiState: LiveData<DetailPupilUiState> = _pupilDetailUiState

    fun getPupil(pupilId: Int){
        viewModelScope.launch {
            repository.getPupil(pupilId).collectLatest { details ->
                _pupilDetailUiState.value = details
            }
        }
    }
}