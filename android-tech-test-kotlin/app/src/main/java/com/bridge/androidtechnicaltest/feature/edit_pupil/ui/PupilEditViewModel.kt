package com.bridge.androidtechnicaltest.feature.edit_pupil.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.core.Resource
import com.bridge.androidtechnicaltest.core.utils.ui.RandGenerator
import com.bridge.androidtechnicaltest.core.utils.ui.RandGenerator.getImageType
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.repository.EditPupilRepository
import com.bridge.androidtechnicaltest.feature.edit_pupil.model.EditPupilUI
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PupilEditViewModel(
    private val repository: EditPupilRepository
) : ViewModel() {

    private val _pupil = MutableStateFlow<PupilUI>(PupilUI())
    val pupil = _pupil.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        PupilUI()
    )

    private val _handleEventState = MutableSharedFlow<EditPupilOneTimeEvent>()
    val handleEventState = _handleEventState.asSharedFlow()

    fun getPupilToEdit(pupilId: Int) {
        viewModelScope.launch {
            repository.getPupilToEdit(pupilId)
                .catch { e ->
                    print(e.message)
                }
                .collect { pupil ->
                    _pupil.update {
                        it.copy(
                            pupilId = pupil.id.toString(),
                            pupilName = "${pupil.firstName} ${pupil.lastName}",
                            pupilLocation = pupil.country,
                            pupilImage = pupil.image,
                            pupilCountry = pupil.country,
                            latitude = pupil.latitude.toString(),
                            longitude = pupil.longitude.toString()
                        )
                    }
                }
        }
    }

    fun updatePupil(
        pupilId: Int,
        pupilFirstname: String,
        pupilLastName: String,
        pupilCountry: String,
    ) {
        val editPupil = EditPupilUI(
            pupilId = pupilId,
            pupilFirstName = pupilFirstname,
            pupilLastName = pupilLastName,
            pupilLocation = pupilCountry,
            pupilImage = RandGenerator.pupilPhoto(getImageType(), pupilFirstname, pupilLastName),
            pupilCountry = pupilCountry,
            latitude = RandGenerator.randomLatitude().toString(),
            longitude = RandGenerator.randomLongitude().toString()
        ).toPupil()
        viewModelScope.launch {
            repository.editPupil(pupilId.toInt(), editPupil).collect { response ->
                when (response) {
                    is Resource.Loading -> {
                        _handleEventState.emit(EditPupilOneTimeEvent.LoadingEvent)
                    }
                    is Resource.Error -> {
                        _handleEventState.emit(EditPupilOneTimeEvent.ErrorEvent(response.message))
                    }
                    is Resource.Success -> {
                        _handleEventState.emit(EditPupilOneTimeEvent.SuccessEvent)
                    }
                    is Resource.NotFoundData -> {
                        _handleEventState.emit(EditPupilOneTimeEvent.ErrorEvent(response.message))
                    }
                }
            }
        }
    }

    sealed interface EditPupilOneTimeEvent {
        data class ErrorEvent(val errorMessage: Int) : EditPupilOneTimeEvent
        object LoadingEvent : EditPupilOneTimeEvent
        object SuccessEvent : EditPupilOneTimeEvent
    }


    fun EditPupilUI.toPupil(): Pupil {
        return Pupil(
            id = pupilId.toInt(),
            firstName = pupilFirstName,
            lastName = pupilLastName,
            country = pupilLocation,
            image = pupilImage,
            latitude = latitude.toDouble(),
            longitude = longitude.toDouble()
        )
    }
}