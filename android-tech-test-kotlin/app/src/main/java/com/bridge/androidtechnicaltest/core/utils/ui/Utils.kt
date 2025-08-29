package com.bridge.androidtechnicaltest.core.utils.ui

object Utils {
    fun String.trimMultipleSpaces() =
        this.trim().replace("\\s+".toRegex(), "")
}