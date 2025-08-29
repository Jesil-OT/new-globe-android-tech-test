package com.bridge.androidtechnicaltest.feature.pupil_details.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.core.Resource
import com.bridge.androidtechnicaltest.data.repository.DeletePupilRepository
import com.bridge.androidtechnicaltest.data.repository.PupilDetailRepository
import com.bridge.androidtechnicaltest.feature.pupil_details.models.DeletePupilResponse
import com.bridge.androidtechnicaltest.feature.pupil_details.models.DetailPupilUI
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PupilDetailViewModel(
    private val pupilDetailsRepository: PupilDetailRepository,
    private val deletePupilRepository: DeletePupilRepository
) : ViewModel() {
    private val _pupilDetails = MutableStateFlow(DetailPupilUI())
    val pupilDetails: StateFlow<DetailPupilUI> = _pupilDetails.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        DetailPupilUI()
    )
    val isPupilFound = MutableStateFlow(true)

    private val _handleEventState: MutableSharedFlow<PupilDetailOneTimeEvent> = MutableSharedFlow()
    val handleEventState: SharedFlow<PupilDetailOneTimeEvent> = _handleEventState.asSharedFlow()

    private val _deleteEventState: MutableSharedFlow<DeleteOneTimeEvent> = MutableSharedFlow()
    val deleteEventState: SharedFlow<DeleteOneTimeEvent> = _deleteEventState.asSharedFlow()

    fun observerPupilChanges(pupilId: Int) = viewModelScope.launch {
        pupilDetailsRepository.getPupil(pupilId).collect { uiState ->
            when (uiState) {
                is Resource.Success -> {
                    val uiState = uiState.data
                    _pupilDetails.update {
                        it.copy(
                            pupilId = uiState.id.toString(),
                            pupilName = "${uiState.firstName} ${uiState.lastName}",
                            pupilLocation = "${uiState.longitude}, ${uiState.latitude}",
                            pupilCountry = uiState.country,
                            pupilImage = uiState.image,
                        )
                    }
                    isPupilFound.value = true
                    _handleEventState.emit(PupilDetailOneTimeEvent.SuccessEvent)
                }

                is Resource.Loading -> {
                    _handleEventState.emit(PupilDetailOneTimeEvent.LoadingEvent)
                    isPupilFound.value = false
                }

                is Resource.NotFoundData -> {
                    _handleEventState.emit(PupilDetailOneTimeEvent.NotFoundEvent(uiState.message))
                    isPupilFound.value = false
                }

                is Resource.Error -> {
                    _handleEventState.emit(PupilDetailOneTimeEvent.ErrorEvent(uiState.message))
                    isPupilFound.value = true
                }
            }
        }
    }

    fun deletePupil(pupilId: Int) = viewModelScope.launch {
        deletePupilRepository.deletePupil(pupilId).collect { uiState ->
            when (uiState) {
                is Resource.Loading -> {
                    _deleteEventState.emit(DeleteOneTimeEvent.LoadingEvent)
                    Log.d("PupilDetailsViewModel", "delete state loading:")
                    isPupilFound.value = false
                }

                is Resource.Success -> {
                    Log.d("PupilDetailsViewModel", "delete state success:")
                    _deleteEventState.emit(DeleteOneTimeEvent.SuccessEvent)
                    isPupilFound.value = false
                }

                is Resource.Error -> {
                    _deleteEventState.emit(DeleteOneTimeEvent.DeleteErrorEvent(uiState.message))
                    Log.d("PupilDetailsViewModel", "delete state error:")
                    isPupilFound.value = true
                }

                is Resource.NotFoundData -> {
                    // code will never be reached if the pupil has been deleted
                }
            }
        }
    }
}

sealed interface PupilDetailOneTimeEvent {
    data class ErrorEvent(val errorMessage: Int) : PupilDetailOneTimeEvent
    data class NotFoundEvent(val errorMessage: Int) : PupilDetailOneTimeEvent
    object LoadingEvent : PupilDetailOneTimeEvent
    object SuccessEvent : PupilDetailOneTimeEvent
}

sealed interface DeleteOneTimeEvent{
    data class DeleteErrorEvent(val errorMessage: Int) : DeleteOneTimeEvent
    object LoadingEvent : DeleteOneTimeEvent
    object SuccessEvent : DeleteOneTimeEvent
}