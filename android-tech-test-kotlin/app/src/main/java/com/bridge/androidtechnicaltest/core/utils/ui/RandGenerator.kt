package com.bridge.androidtechnicaltest.core.utils.ui

import kotlin.random.Random

object RandGenerator {
    fun pupilPhoto(imageType: String, firstName: String, lastName: String): String =
        "http://lorempixel.com/640/480/$imageType?name=$firstName $lastName"

    fun randomLatitude(): Double =
        Random.nextDouble(-90.000, 90.000)

    fun randomLongitude(): Double =
        Random.nextDouble(-180.000, 180.000)
}