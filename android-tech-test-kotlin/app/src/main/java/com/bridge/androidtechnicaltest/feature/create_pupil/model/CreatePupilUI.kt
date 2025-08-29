package com.bridge.androidtechnicaltest.feature.create_pupil.model

data class CreatePupilUI(
    val pupilFirstName: String = "",
    val pupilLastName: String = "",
    val pupilLocation: String = "",
    val pupilImage: String = "",
    val pupilCountry: String = "",
    val latitude: String = "",
    val longitude: String = ""
)