package com.bridge.androidtechnicaltest.feature.edit_pupil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bridge.androidtechnicaltest.core.utils.ui.RandGenerator
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.repository.EditPupilRepository
import com.bridge.androidtechnicaltest.feature.pupil.models.PupilUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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

    private val _responseUiState = MutableStateFlow<EditPupilUIResponse>(EditPupilUIResponse.Idle)
    val responseUiState = _responseUiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        EditPupilUIResponse.Idle
    )

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
        pupilId: String, pupilFirstname: String,
        pupilLastName: String,
        pupilCountry: String, pupilImage: String
    ) {
        val editPupil = EditPupilUI(
            pupilId = pupilId.toString(),
            pupilFirstName = pupilFirstname,
            pupilLastName = pupilLastName,
            pupilLocation = pupilCountry,
            pupilImage = pupilImage,
            pupilCountry = pupilCountry,
            latitude = RandGenerator.randomLatitude().toString(),
            longitude = RandGenerator.randomLongitude().toString()
        ).toPupil()
        viewModelScope.launch {
            repository.editPupil(pupilId.toInt(), editPupil).collect { response ->
                when (response) {
                    is EditPupilResponse.Loading -> {
                        _responseUiState.value = EditPupilUIResponse.Loading
                    }
                    is EditPupilResponse.Error -> {
                        _responseUiState.value =
                            EditPupilUIResponse.ErrorEditingPupil(response.message)
                    }
                    is EditPupilResponse.Success -> {
                        _responseUiState.value = EditPupilUIResponse.PupilEdited
                    }
                }
            }
        }
    }

    sealed interface EditPupilUIResponse {
        object Idle : EditPupilUIResponse
        object PupilEdited : EditPupilUIResponse
        data class ErrorEditingPupil(val errorMessage: String) : EditPupilUIResponse
        object Loading : EditPupilUIResponse
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