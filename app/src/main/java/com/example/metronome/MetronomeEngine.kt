package com.example.metronome

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.metronome.utils.Constants
import com.example.metronome.utils.Constants.AccentPattern
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
 * Generates clicks with different pitches based on beat position and accent pattern.
 */
class MetronomeEngine @Inject constructor() {

    private var audioTrack: AudioTrack? = null
    private var tickJob: Job? = null
    private val engineScope = CoroutineScope(Dispatchers.Default)

    private var currentBeatInMeasure = 0
    private var timeSignature = TimeSignature.FOUR_FOUR
    private var accentPattern = AccentPattern.FIRST_ACCENT

    // Pre-generated audio samples for regular and accent clicks
    private var regularClickSamples: ShortArray? = null
    private var accentClickSamples: ShortArray? = null

    // --- Public API ---

    /**
     * Starts the metronome with specified BPM, time signature, and accent pattern.
     */
    fun play(
        bpm: Int,
        timeSignature: TimeSignature = TimeSignature.FOUR_FOUR,
        accentPattern: AccentPattern = AccentPattern.FIRST_ACCENT
    ) {
        release()

        this.timeSignature = timeSignature
        this.accentPattern = accentPattern
        this.currentBeatInMeasure = 0

        // Pre-generate both click types for efficiency
        regularClickSamples = generateClickSamples(Constants.CLICK_FREQUENCY_HZ)
        accentClickSamples = generateClickSamples(Constants.CLICK_FREQUENCY_ACCENT_HZ)

        val intervalMs = Constants.MS_PER_MINUTE / bpm

        tickJob = engineScope.launch {
            while (isActive) {
                playOnce()
                delay(intervalMs)
                currentBeatInMeasure = (currentBeatInMeasure + 1) % timeSignature.beatsPerMeasure
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
     * Determines if the current beat should use the accent (higher pitch).
     */
    private fun shouldAccent(): Boolean {
        return when (accentPattern) {
            AccentPattern.STANDARD -> false
            AccentPattern.FIRST_ACCENT -> currentBeatInMeasure == 0
            AccentPattern.FIRST_TWO_ACCENT -> currentBeatInMeasure == 0 || currentBeatInMeasure == 1
        }
    }

    /**
     * Plays a single click, with optional volume boost for accent beats.
     */
    private fun playOnce() {
        val isAccent = shouldAccent()
        val samples = if (isAccent) accentClickSamples else regularClickSamples
        samples ?: return

        // If AudioTrack hasn't been created yet, create it now
        if (audioTrack == null) {
            audioTrack = buildAudioTrack(samples)
            audioTrack?.write(samples, 0, samples.size)
        }

        audioTrack?.stop()
        audioTrack?.reloadStaticData()
        audioTrack?.play()

        // Boost volume for accent beats
        if (isAccent) {
            audioTrack?.setVolume(1.0f)  // Max volume for accent
        } else {
            audioTrack?.setVolume(0.2f)  // 60% volume for regular beats
        }
    }

    /**
     * Generates a sine-wave click at the specified frequency with fade-out envelope.
     */
    private fun generateClickSamples(frequencyHz: Double): ShortArray {
        val numSamples = (Constants.SAMPLE_RATE * Constants.CLICK_DURATION_MS / 1000.0).toInt()
        return ShortArray(numSamples) { i ->
            val angle = 2.0 * Math.PI * frequencyHz * i / Constants.SAMPLE_RATE
            val envelope = 1.0 - (i.toDouble() / numSamples)
            (sin(angle) * envelope * Short.MAX_VALUE).toInt().toShort()
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