package de.gmuth.ipp.attributes

/**
 * Copyright (c) 2020-2025 Gerhard Muth
 */

enum class Orientation(val code: Int) {
    Portrait(3),
    Landscape(4),
    ReverseLandscape(5),
    ReversePortrait(6),
    None(7); // PWG 5100.13

    companion object {
        @JvmStatic
        fun fromInt(code: Int) = Orientation.entries.single { it.code == code }
    }
}