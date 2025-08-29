package com.bridge.androidtechnicaltest.feature.pupil.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.core.Resource
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.repository.PupilsRepository
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
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

    private val _pupilsData = MutableStateFlow<List<PupilUI>>(emptyList())
    val pupilsData: StateFlow<List<PupilUI>> = _pupilsData.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        emptyList()
    )

    private val _handleEventState: MutableSharedFlow<PupilOneTimeEvent> = MutableSharedFlow()
    val handleEventState: SharedFlow<PupilOneTimeEvent> = _handleEventState.asSharedFlow()

    val emptyListEvent : MutableSharedFlow<Boolean> = MutableSharedFlow()


    fun observerPupilsChanges() = viewModelScope.launch {
        repository.fetchPupils().collect { uiState ->
            when (uiState) {
                is Resource.Success -> {
                    // loads the initial data from single source
                    val currentPupilList: List<PupilUI> = uiState.data.map { it.toPupilUI() }
                    if (currentPupilList.isEmpty()){
                        emptyListEvent.emit(true)
                    } else {
                        _pupilsData.value = currentPupilList
                        _handleEventState.emit(PupilOneTimeEvent.SuccessEvent)
                    }
                }

                is Resource.Loading -> {
                    _handleEventState.emit(PupilOneTimeEvent.LoadingEvent)
                }

                is Resource.Error -> {
                    _handleEventState.emit(PupilOneTimeEvent.ErrorEvent(uiState.message))
                }

                is Resource.NotFoundData -> {
                    _handleEventState.emit(PupilOneTimeEvent.NotFoundEvent(uiState.message))
                }
            }
        }
    }
}


sealed interface PupilOneTimeEvent{
    object SuccessEvent: PupilOneTimeEvent
    object LoadingEvent: PupilOneTimeEvent
    data class ErrorEvent(val errorMessage: Int) : PupilOneTimeEvent
    data class NotFoundEvent(val errorMessage: Int) : PupilOneTimeEvent
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