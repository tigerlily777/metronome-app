package com.example.metronome

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.metronome.utils.Constants
import javax.inject.Inject
import kotlin.math.sin

/**
 * Handles all audio playback for the metronome.
 * This class is responsible solely for sound generation and timing.
 * It has no knowledge of the UI layer.
 */
class MetronomeEngine @Inject constructor(){

    private var audioTrack: AudioTrack? = null

    // --- Public API ---

    fun play(bpm: Int) {
        release()
        val clickSamples = generateClickSamples()
        audioTrack = buildAudioTrack(clickSamples)
        audioTrack?.write(clickSamples, 0, clickSamples.size)
        schedulePlayback(bpm)
    }

    fun release() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }

    // --- Private helpers ---

    /**
     * Generates a short sine-wave click with a fade-out envelope to avoid popping.
     */
    private fun generateClickSamples(): ShortArray {
        val numSamples = (Constants.SAMPLE_RATE * Constants.CLICK_DURATION_MS / 1000.0).toInt()
        return ShortArray(numSamples) { i ->
            val angle = 2.0 * Math.PI * Constants.CLICK_FREQUENCY_HZ * i / Constants.SAMPLE_RATE
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

    private fun schedulePlayback(bpm: Int) {
        val intervalMs = Constants.MS_PER_MINUTE / bpm
        audioTrack?.setPositionNotificationPeriod((Constants.SAMPLE_RATE * intervalMs / 1000).toInt())
        audioTrack?.play()
    }
}