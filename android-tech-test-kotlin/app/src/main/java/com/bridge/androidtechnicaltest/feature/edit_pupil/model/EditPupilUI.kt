package com.bridge.androidtechnicaltest.feature.edit_pupil.model

data class EditPupilUI(
    val pupilId: Int = 0,
    val pupilFirstName: String = "",
    val pupilLastName: String = "",
    val pupilLocation: String = "",
    val pupilImage: String = "",
    val pupilCountry: String = "",
    val latitude: String = "",
    val longitude: String = ""
)