package com.bridge.androidtechnicaltest.core.utils.ui

import kotlin.random.Random

object RandGenerator {
    fun pupilPhoto(imageType: String, firstName: String, lastName: String): String =
        "http://lorempixel.com/640/480/$imageType?name=$firstName $lastName"

    fun getImageType(): String = listOf(
        "abstract", "technics",
        "business", "cats",
        "sports", "transport", "food"
    ).random()

    fun randomLatitude(): Double =
        Random.nextDouble(-90.00, 90.00)

    fun randomLongitude(): Double =
        Random.nextDouble(-180.00, 180.00)
}