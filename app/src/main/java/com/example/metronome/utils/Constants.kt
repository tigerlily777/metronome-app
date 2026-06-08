package com.example.metronome.utils

object Constants {

    // BPM range
    const val BPM_MIN = 40
    const val BPM_MAX = 240
    const val BPM_DEFAULT = 120
    const val BPM_STEP_SMALL = 1
    const val BPM_STEP_LARGE = 10

    // Audio frequencies
    const val SAMPLE_RATE = 44100
    const val CLICK_FREQUENCY_LOW = 500.0      // For different pitch mode
    const val CLICK_FREQUENCY_NORMAL = 1000.0
    const val CLICK_FREQUENCY_HIGH = 1500.0
    const val CLICK_DURATION_MS = 30
    const val MS_PER_MINUTE = 60_000L

    // Time signature (beat pattern)
    enum class TimeSignature(
        val beatsPerMeasure: Int,
        val displayName: String
    ) {
        TWO_FOUR(2, "2/4"),
        THREE_FOUR(3, "3/4"),
        FOUR_FOUR(4, "4/4"),
        SIX_EIGHT(6, "6/8");

        companion object {
            fun default() = FOUR_FOUR
        }
    }

    // How to emphasize the strong beat
    enum class BeatEmphasis(val displayName: String) {
        VOLUME_ONLY("Louder"),
        PITCH_ONLY("Higher Pitch"),
        VOLUME_AND_PITCH("Louder + Higher"),
        DIFFERENT_PITCH("Lower First");

        companion object {
            fun default() = VOLUME_AND_PITCH
        }
    }

    // Accent pattern
    enum class AccentPattern(val displayName: String) {
        STANDARD("Even"),
        FIRST_ACCENT("Accent 1st"),
        FIRST_TWO_ACCENT("Accent 1st+2nd");

        companion object {
            fun default() = FIRST_ACCENT
        }
    }

    // Subdivision - how many clicks per beat
    enum class Subdivision(val clicksPerBeat: Int, val displayName: String) {
        WHOLE(1, "None"),
        HALF(2, "1/2"),
        QUARTER(4, "1/4"),
        EIGHTH(8, "1/8");

        companion object {
            fun default() = WHOLE
        }
    }
}