package com.bridge.androidtechnicaltest.feature.create_pupil.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.core.Resource
import com.bridge.androidtechnicaltest.core.utils.ui.RandGenerator
import com.bridge.androidtechnicaltest.core.utils.ui.RandGenerator.getImageType
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.repository.CreatePupilRepository
import com.bridge.androidtechnicaltest.feature.create_pupil.model.CreatePupilUI
import com.bridge.androidtechnicaltest.feature.create_pupil.ui.AddPupilOneTimeEvent.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AddPupilViewModel(
    private val createPupilRepository: CreatePupilRepository
) : ViewModel() {
    private val _handleEventState = MutableSharedFlow<AddPupilOneTimeEvent>()
    val handleEventState = _handleEventState.asSharedFlow()

    fun addPupil(
        pupilFirstName: String,
        pupilLastName: String,
        pupilCountry: String,
    ) {
        val newPupil = CreatePupilUI(
            pupilFirstName = pupilFirstName,
            pupilLastName = pupilLastName,
            pupilCountry = pupilCountry,
            pupilImage = RandGenerator.pupilPhoto(getImageType(), pupilFirstName, pupilLastName),
            latitude = RandGenerator.randomLatitude().toString().take(5),
            longitude = RandGenerator.randomLongitude().toString().take(5)
        ).toPupil()
        viewModelScope.launch {
            createPupilRepository.createPupil(pupil = newPupil).collect { response ->
                when (response) {
                    is Resource.Loading -> {
                        _handleEventState.emit(LoadingEvent)
                    }

                    is Resource.Error -> {
                        _handleEventState.emit(ErrorEvent(response.message))
                    }

                    is Resource.Success -> {
                        _handleEventState.emit(PupilAddedEvent(response.data))
                    }

                    is Resource.NotFoundData -> {
                        // code will never be reached
                    }
                }
            }
        }
    }

}

sealed interface AddPupilOneTimeEvent {
    data class PupilAddedEvent(val pupilName: String) : AddPupilOneTimeEvent
    data class ErrorEvent(val errorMessage: Int) : AddPupilOneTimeEvent
    object LoadingEvent : AddPupilOneTimeEvent
}

fun CreatePupilUI.toPupil(): Pupil{
    return Pupil(
        id = 0,
        firstName = pupilFirstName,
        lastName = pupilLastName,
        country = pupilCountry,
        image = pupilImage,
        latitude = latitude.toDouble(),
        longitude = longitude.toDouble()
    )
}