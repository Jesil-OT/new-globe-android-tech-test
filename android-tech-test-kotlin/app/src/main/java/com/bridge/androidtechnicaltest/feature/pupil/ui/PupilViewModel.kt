package com.bridge.androidtechnicaltest.feature.pupil.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.repository.PupilsRepository
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PupilViewModel(
    private val repository: PupilsRepository
) : ViewModel() {

    private val _pupilResponse = MutableSharedFlow<PupilResponse>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val pupilResponse: SharedFlow<PupilResponse> = _pupilResponse

    init {
        getAllPupils()
    }

    fun getAllPupils() {
        viewModelScope.launch {
            repository.fetchPupils().collectLatest { uiState ->
                _pupilResponse.emit(uiState)
            }
        }
    }

}

fun Pupil.toPupilUI(): PupilUI {
    return PupilUI(
        pupilId = id.toString(),
        pupilName = "$firstName $lastName",
        pupilLocation = "$longitude, $latitude",
        pupilCountry = country,
        pupilImage = image

    )
}