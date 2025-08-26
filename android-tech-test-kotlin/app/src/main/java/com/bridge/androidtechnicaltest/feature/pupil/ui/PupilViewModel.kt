package com.bridge.androidtechnicaltest.feature.pupil.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.repository.PupilsRepository
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilResponse
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PupilViewModel(
    private val repository: PupilsRepository
) : ViewModel() {

    private val _response: MutableStateFlow<PupilUIResponse> =
        MutableStateFlow(PupilUIResponse.ReturnedFromSingleSource())
    val response = _response.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        PupilUIResponse.ReturnedFromSingleSource()
    )

    init {
        observerPupilChanges()
    }

    fun observerPupilChanges() = viewModelScope.launch {
        repository.fetchPupils().collect { uiState ->
            when (uiState) {
                is PupilResponse.Loading -> {
                    val showLoadingType = uiState.pupil?.map { it.toPupilUI() }
                    _response.value = PupilUIResponse.Loading(showLoadingType ?: emptyList())
                }

                is PupilResponse.Error -> {
                    _response.value = PupilUIResponse.ErrorWhileGettingSynced(uiState.message)
                }

                is PupilResponse.Success -> {
                    val syncedPupilList = uiState.pupils.map { it.toPupilUI() }
                    _response.value = PupilUIResponse.ReturnedWithSyncedList(syncedPupilList)
                }

                is PupilResponse.SuccessFromSingleSource -> {
                    val currentPupilList = uiState.pupils.map { it.toPupilUI() }
                    _response.value = PupilUIResponse.ReturnedFromSingleSource(currentPupilList)
                }
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

sealed interface PupilUIResponse {
    data class ReturnedWithSyncedList(val syncedList: List<PupilUI>) : PupilUIResponse
    data class ErrorWhileGettingSynced(val errorMessage: String) : PupilUIResponse
    data class ReturnedFromSingleSource(val pupilList: List<PupilUI> = emptyList()) : PupilUIResponse
    data class Loading(val pupilList: List<PupilUI>) : PupilUIResponse
}