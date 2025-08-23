package com.bridge.androidtechnicaltest.data.mapper

import com.bridge.androidtechnicaltest.data.local.model.PupilsEntity
import com.bridge.androidtechnicaltest.data.model.Pupil
import com.bridge.androidtechnicaltest.data.network.model.PupilsDto
import kotlinx.serialization.InternalSerializationApi

fun Pupil.toPupilEntity(): PupilsEntity {
    return PupilsEntity(
        pupilId = id,
        name = "$firstName $lastName",
        country = country,
        image = image,
        latitude = latitude,
        longitude = longitude
    )
}

fun PupilsEntity.fromPupilEntity(): Pupil {
    return Pupil(
        id = pupilId,
        firstName = name.split(" ")[0],
        lastName = name.split(" ")[1],
        country = country,
        image = image,
        latitude = latitude,
        longitude = longitude
    )
}

@OptIn(InternalSerializationApi::class)
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

@OptIn(InternalSerializationApi::class)
fun PupilsDto.toPupilDto(): Pupil {
    return Pupil(
        id = pupilId,
        firstName = pupilName.split(" ")[0],
        lastName = pupilName.split(" ")[1],
        country = pupilCountry,
        image = pupilImage,
        latitude = latitude,
        longitude = longitude
    )
}