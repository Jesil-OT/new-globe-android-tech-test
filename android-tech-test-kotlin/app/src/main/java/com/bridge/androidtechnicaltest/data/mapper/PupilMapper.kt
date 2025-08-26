package com.bridge.androidtechnicaltest.data.mapper

import com.bridge.androidtechnicaltest.data.local.model.PupilsEntity
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.network.model.AddPupilsDto
import com.bridge.androidtechnicaltest.data.network.model.PupilsDto

fun Pupil.toPupilToEntity(): PupilsEntity {
    return PupilsEntity(
        pupilId = id,
        name = "$firstName $lastName",
        country = country,
        image = image,
        latitude = latitude,
        longitude = longitude
    )
}

fun PupilsEntity.toPupilEntity(): Pupil {
    return Pupil(
        id = pupilId,
        firstName = name.split(" ").getOrNull(0) ?: "",
        lastName = name.split(" ").getOrNull(1) ?: "",
        country = country,
        image = image,
        latitude = latitude,
        longitude = longitude
    )
}

fun Pupil.toPupil(): PupilsDto {
    return PupilsDto(
        pupilId = id,
        pupilName = "$firstName $lastName",
        pupilCountry = country,
        pupilImage = image,
        latitude = latitude,
        longitude = longitude,
    )
}

fun PupilsDto.toPupilDto(): Pupil {
    return Pupil(
        id = pupilId,
        firstName = pupilName.split(" ").getOrNull(0) ?: "",
        lastName = pupilName.split(" ").getOrNull(1) ?: "",
        country = pupilCountry,
        image = pupilImage,
        latitude = latitude,
        longitude = longitude
    )
}

fun Pupil.toPupilToAddPupilDto(): AddPupilsDto{
    return AddPupilsDto(
        pupilName = "$firstName $lastName",
        pupilCountry = country,
        pupilImage = image,
        latitude = latitude,
        longitude = longitude
    )
}