package com.bridge.androidtechnicaltest.feature.add_pupil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.repository.AddPupilRepository
import com.bridge.androidtechnicaltest.feature.pupil.models.AddPupilUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class AddPupilViewModel(
    private val addPupilRepository: AddPupilRepository
): ViewModel() {
    private val _addPupilUiEvents = MutableSharedFlow<AddPupilUiEvents>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val addPupilUiEvents : SharedFlow<AddPupilUiEvents> = _addPupilUiEvents.asSharedFlow()

    fun addPupil(uiAction: AddPupilUiAction){
        when(uiAction){
            is AddPupilUiAction.AddPupil -> {
                val pupil = Pupil(
                    id = Random.nextInt(0,10_000),
                    firstName = uiAction.pupilFirstName,
                    lastName = uiAction.pupilLastName,
                    country = uiAction.pupilCountry,
                    image = uiAction.pupilImage,
                    latitude = uiAction.pupilLatitude,
                    longitude = uiAction.pupilLongitude
                )
                addPupil(pupil)
            }
        }
    }

    private fun addPupil(pupil: Pupil){
        viewModelScope.launch {
            try {
                addPupilRepository.addPupil(pupil).collect { state ->
                    when (state) {
                        is AddPupilUiState.Loading -> {
                            _addPupilUiEvents.emit(AddPupilUiEvents.Loading)
                        }

                        is AddPupilUiState.Success -> {
                            _addPupilUiEvents.emit(AddPupilUiEvents.PupilAddedSuccessfully)
                        }

                        is AddPupilUiState.Error -> {
                            _addPupilUiEvents.emit(AddPupilUiEvents.PupilFailedToAdd(state.message))
                        }
                    }
                }
            }
            catch (e: Exception){
                _addPupilUiEvents.emit(AddPupilUiEvents.PupilFailedToAdd(e.message.toString()))
            }
        }
    }


}

sealed interface AddPupilUiAction{
    data class AddPupil(
        val pupilFirstName: String,
        val pupilLastName: String,
        val pupilCountry: String,
        val pupilImage: String,
        val pupilLatitude: Double,
        val pupilLongitude: Double
    ): AddPupilUiAction
}

sealed interface AddPupilUiEvents{
    object Loading : AddPupilUiEvents
    object PupilAddedSuccessfully : AddPupilUiEvents
    data class PupilFailedToAdd(val message: String) : AddPupilUiEvents
}