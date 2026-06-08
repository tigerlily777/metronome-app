package com.example.metronome

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.metronome.utils.Constants
import com.example.metronome.utils.Constants.AccentPattern
import com.example.metronome.utils.Constants.Subdivision
import com.example.metronome.utils.Constants.TimeSignature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sin

/**
 * Handles all audio playback for the metronome.
 * Supports time signatures, accent patterns, and subdivisions.
 */
class MetronomeEngine @Inject constructor() {

    private var audioTrack: AudioTrack? = null
    private var tickJob: Job? = null
    private val engineScope = CoroutineScope(Dispatchers.Default)

    private var currentClickInMeasure = 0
    private var timeSignature = TimeSignature.FOUR_FOUR
    private var accentPattern = AccentPattern.FIRST_ACCENT
    private var subdivision = Subdivision.QUARTER

    // Pre-generated audio samples
    private var regularClickSamples: ShortArray? = null
    private var accentClickSamples: ShortArray? = null

    // --- Public API ---

    /**
     * Starts the metronome with all parameters.
     */
    fun play(
        bpm: Int,
        timeSignature: TimeSignature = TimeSignature.FOUR_FOUR,
        accentPattern: AccentPattern = AccentPattern.FIRST_ACCENT,
        subdivision: Subdivision = Subdivision.QUARTER
    ) {
        release()

        this.timeSignature = timeSignature
        this.accentPattern = accentPattern
        this.subdivision = subdivision
        this.currentClickInMeasure = 0

        regularClickSamples = generateClickSamples(Constants.CLICK_FREQUENCY_HZ, isAccent = false)
        accentClickSamples = generateClickSamples(Constants.CLICK_FREQUENCY_ACCENT_HZ, isAccent = true)

        // Interval between each click (including subdivisions)
        val beatIntervalMs = Constants.MS_PER_MINUTE / bpm
        val clickIntervalMs = beatIntervalMs / subdivision.clicksPerBeat

        val totalClicksPerMeasure = timeSignature.beatsPerMeasure * subdivision.clicksPerBeat

        tickJob = engineScope.launch {
            while (isActive) {
                playOnce()
                delay(clickIntervalMs)
                currentClickInMeasure = (currentClickInMeasure + 1) % totalClicksPerMeasure
            }
        }
    }

    /**
     * Stops playback and releases all audio resources.
     */
    fun release() {
        tickJob?.cancel()
        tickJob = null
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        regularClickSamples = null
        accentClickSamples = null
    }

    // --- Private helpers ---

    /**
     * Determines if current click should use accent (first beat of measure, or pattern-based).
     */
    private fun shouldAccent(): Boolean {
        // Only accent on beat boundaries (when click index is divisible by clicksPerBeat)
        if (currentClickInMeasure % subdivision.clicksPerBeat != 0) {
            return false
        }

        val beatInMeasure = currentClickInMeasure / subdivision.clicksPerBeat
        return when (accentPattern) {
            AccentPattern.STANDARD -> false
            AccentPattern.FIRST_ACCENT -> beatInMeasure == 0
            AccentPattern.FIRST_TWO_ACCENT -> beatInMeasure == 0 || beatInMeasure == 1
        }
    }

    /**
     * Plays a single click with appropriate volume and pitch.
     */
    private fun playOnce() {
        val isAccent = shouldAccent()
        val samples = if (isAccent) accentClickSamples else regularClickSamples
        samples ?: return

        if (audioTrack == null) {
            audioTrack = buildAudioTrack(samples)
            audioTrack?.write(samples, 0, samples.size)
        }

        audioTrack?.stop()
        audioTrack?.reloadStaticData()
        audioTrack?.play()

        // Set volume based on accent
        if (isAccent) {
            audioTrack?.setVolume(1.0f)
        } else {
            audioTrack?.setVolume(0.6f)
        }
    }

    /**
     * Generates sine-wave click with envelope.
     */
    private fun generateClickSamples(frequencyHz: Double, isAccent: Boolean): ShortArray {
        val numSamples = (Constants.SAMPLE_RATE * Constants.CLICK_DURATION_MS / 1000.0).toInt()
        return ShortArray(numSamples) { i ->
            val angle = 2.0 * Math.PI * frequencyHz * i / Constants.SAMPLE_RATE
            val envelope = 1.0 - (i.toDouble() / numSamples)
            val amplitudeBoost = if (isAccent) 1.2 else 1.0
            (sin(angle) * envelope * amplitudeBoost * Short.MAX_VALUE).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            ).toShort()
        }
    }

    private fun buildAudioTrack(samples: ShortArray): AudioTrack {
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(Constants.SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
    }
}