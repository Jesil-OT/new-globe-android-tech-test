package com.bridge.androidtechnicaltest.feature.add_pupil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.core.utils.ui.RandGenerator
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.repository.AddPupilRepository
import com.bridge.androidtechnicaltest.feature.pupil.models.AddPupilResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class AddPupilViewModel(
    private val addPupilRepository: AddPupilRepository
) : ViewModel() {
    private val _responseUiState = MutableStateFlow<AddPupilUIResponse>(AddPupilUIResponse.Idle)
    val responseUiState = _responseUiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        AddPupilUIResponse.Idle
    )

    fun addPupil(
        pupilFirstName: String,
        pupilLastName: String,
        pupilCountry: String,
    ) {
        val newPupil = Pupil(
            id = 0,
            firstName = pupilFirstName.trim(),
            lastName = pupilLastName.trim(),
            country = pupilCountry,
            image = RandGenerator.pupilPhoto(getImageType(), pupilFirstName.trim(), pupilLastName.trim()),
            latitude = RandGenerator.randomLatitude(),
            longitude = RandGenerator.randomLongitude()
        )
        viewModelScope.launch {
            addPupilRepository.addPupil(pupil = newPupil).collect { response ->
                when (response) {
                    is AddPupilResponse.Loading -> {
                        _responseUiState.value = AddPupilUIResponse.Loading
                    }

                    is AddPupilResponse.Error -> {
                        _responseUiState.value =
                            AddPupilUIResponse.ErrorAddingPupil(response.message)
                    }

                    is AddPupilResponse.Success -> {
                        _responseUiState.value = AddPupilUIResponse.PupilAdded(response.pupilName)
                    }
                }
            }
        }
    }


    private fun getImageType(): String = listOf(
        "abstract", "technics",
        "business", "cats",
        "sports", "transport", "food"
    ).random()

}

sealed interface AddPupilUIResponse {
    object Idle : AddPupilUIResponse
    data class PupilAdded(val pupilName: String) : AddPupilUIResponse
    data class ErrorAddingPupil(val errorMessage: String) : AddPupilUIResponse
    object Loading : AddPupilUIResponse
}