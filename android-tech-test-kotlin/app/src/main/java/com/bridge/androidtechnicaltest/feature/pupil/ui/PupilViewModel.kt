package com.bridge.androidtechnicaltest.feature.pupil.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.repository.PupilsRepository
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PupilViewModel(
    private val repository: PupilsRepository
) : ViewModel() {

    private val _pupilData = MutableStateFlow<List<PupilUI>>(emptyList())
    val pupilData: StateFlow<List<PupilUI>> = _pupilData.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        emptyList()
    )

    private val _handleEventState: MutableSharedFlow<PupilOneTimeEvent> = MutableSharedFlow()
    val handleEventState: SharedFlow<PupilOneTimeEvent> = _handleEventState.asSharedFlow()

    init {
        observerPupilChanges()
    }

    fun observerPupilChanges() = viewModelScope.launch {
        repository.fetchPupils().collect { uiState ->
            when (uiState) {
                is PupilResponse.Loading -> {
                    val showLoadingType = uiState.pupil?.map { it.toPupilUI() }
                    _handleEventState.emit(PupilOneTimeEvent.LoadingEvent(showLoadingType ?: emptyList()))
                }

                is PupilResponse.Error -> {
                    _handleEventState.emit(PupilOneTimeEvent.ErrorEvent(uiState.message))
                }

                is PupilResponse.Success -> {
                    val syncedPupilList = uiState.pupils.map { it.toPupilUI() }
                    _pupilData.value = syncedPupilList
                    _handleEventState.emit(PupilOneTimeEvent.SuccessEvent(syncedPupilList, successSource = SuccessSource.SYNCED))
                }

                is PupilResponse.SuccessFromSingleSource -> {
                    val currentPupilList = uiState.pupils.map { it.toPupilUI() }
                    _pupilData.value = currentPupilList
                    _handleEventState.emit(PupilOneTimeEvent.SuccessEvent(currentPupilList, successSource = SuccessSource.SINGLE_SOURCE))
                }
            }
        }
    }
}


sealed interface PupilOneTimeEvent{
    data class ErrorEvent(val errorMessage: String) : PupilOneTimeEvent
    data class LoadingEvent(val pupilList: List<PupilUI>): PupilOneTimeEvent
    data class SuccessEvent(val pupils: List<PupilUI>, val successSource: SuccessSource) : PupilOneTimeEvent
}
enum class SuccessSource {
    SYNCED, SINGLE_SOURCE
}

sealed interface PupilUIResponse {
    data class ReturnedWithSyncedList(val syncedList: List<PupilUI>) : PupilUIResponse
    data class ReturnedFromSingleSource(val pupilList: List<PupilUI> = emptyList()) : PupilUIResponse
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