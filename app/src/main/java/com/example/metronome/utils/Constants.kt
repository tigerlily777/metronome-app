package com.example.metronome.utils

object Constants {

    // BPM range
    const val BPM_MIN = 40
    const val BPM_MAX = 240
    const val BPM_DEFAULT = 120
    const val BPM_STEP_SMALL = 1
    const val BPM_STEP_LARGE = 10

    // Audio
    const val SAMPLE_RATE = 44100
    const val CLICK_FREQUENCY_HZ = 1000.0
    const val CLICK_DURATION_MS = 30
    const val MS_PER_MINUTE = 60_000L
}