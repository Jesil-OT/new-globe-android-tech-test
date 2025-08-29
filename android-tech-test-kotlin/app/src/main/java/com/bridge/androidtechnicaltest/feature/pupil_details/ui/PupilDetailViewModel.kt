package com.bridge.androidtechnicaltest.feature.pupil_details.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.core.Resource
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.repository.DeletePupilRepository
import com.bridge.androidtechnicaltest.data.repository.PupilDetailRepository
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
import timber.log.Timber

const val DETAIL_VIEWMODEL = "PupilDetailViewModel"

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
                    val pupilDetail = uiState.data
                    if (pupilDetail.emptyDetailsUI()) {
                        isPupilFound.value = false
                        _pupilDetails.value = DetailPupilUI()
                        _handleEventState.emit(PupilDetailOneTimeEvent.NotFoundEvent(R.string.not_found_details))
                    } else {
                        _pupilDetails.update {
                            it.copy(
                                pupilId = pupilDetail.id.toString(),
                                pupilName = "${pupilDetail.firstName} ${pupilDetail.lastName}",
                                pupilLocation = "${pupilDetail.longitude}, ${pupilDetail.latitude}",
                                pupilCountry = pupilDetail.country,
                                pupilImage = pupilDetail.image,
                            )
                        }
                        isPupilFound.value = true
                        _handleEventState.emit(PupilDetailOneTimeEvent.SuccessEvent)
                        Timber.tag(DETAIL_VIEWMODEL)
                            .d("observerPupilChanges: PupilDetailOneTimeEvent.SuccessEvent called")
                    }
                }

                is Resource.Loading -> {
                    _handleEventState.emit(PupilDetailOneTimeEvent.LoadingEvent)
                    isPupilFound.value = false
                    Timber.tag(DETAIL_VIEWMODEL)
                        .d("observerPupilChanges: PupilDetailOneTimeEvent.LoadingEvent called")
                }

                is Resource.NotFoundData -> {
                    _handleEventState.emit(PupilDetailOneTimeEvent.NotFoundEvent(uiState.message))
                    _pupilDetails.value = DetailPupilUI()
                    isPupilFound.value = false
                    Timber.tag(DETAIL_VIEWMODEL)
                        .d("observerPupilChanges: PupilDetailOneTimeEvent.NotFoundEvent called")
                }

                is Resource.Error -> {
                    _handleEventState.emit(PupilDetailOneTimeEvent.ErrorEvent(uiState.message))
                    isPupilFound.value = true
                    Timber.tag(DETAIL_VIEWMODEL)
                        .d("observerPupilChanges: PupilDetailOneTimeEvent.ErrorEvent called")
                }
            }
        }
    }

    fun deletePupil(pupilId: Int) = viewModelScope.launch {
        deletePupilRepository.deletePupil(pupilId).collect { uiState ->
            when (uiState) {
                is Resource.Loading -> {
                    _deleteEventState.emit(DeleteOneTimeEvent.LoadingEvent)
                    Timber.tag(DETAIL_VIEWMODEL).d("delete state loading:")
                    isPupilFound.value = false
                }

                is Resource.Success -> {
                    Timber.tag(DETAIL_VIEWMODEL).d("delete state success:")
                    isPupilFound.value = false
                    _deleteEventState.emit(DeleteOneTimeEvent.SuccessEvent)
                }

                is Resource.Error -> {
                    _deleteEventState.emit(DeleteOneTimeEvent.DeleteErrorEvent(uiState.message))
                    Timber.tag(DETAIL_VIEWMODEL).d("delete state error:")
                    isPupilFound.value = true
                }

                is Resource.NotFoundData -> {
                    _deleteEventState.emit(DeleteOneTimeEvent.NotFoundEvent(uiState.message))
                    isPupilFound.value = true
                }
            }
        }
    }

    private fun Pupil.emptyDetailsUI(
    ): Boolean =
        id.toString().isEmpty() &&
                firstName.isEmpty() &&
                lastName.isEmpty() && country.isEmpty() &&
                image.isEmpty() &&
                latitude.toString().isEmpty() &&
                longitude.toString().isEmpty()

}

sealed interface PupilDetailOneTimeEvent {
    data class ErrorEvent(val errorMessage: Int) : PupilDetailOneTimeEvent
    data class NotFoundEvent(val errorMessage: Int) : PupilDetailOneTimeEvent
    object LoadingEvent : PupilDetailOneTimeEvent
    object SuccessEvent : PupilDetailOneTimeEvent
}

sealed interface DeleteOneTimeEvent {
    data class DeleteErrorEvent(val errorMessage: Int) : DeleteOneTimeEvent
    data class NotFoundEvent(val errorMessage: Int) : DeleteOneTimeEvent
    object LoadingEvent : DeleteOneTimeEvent
    object SuccessEvent : DeleteOneTimeEvent
}