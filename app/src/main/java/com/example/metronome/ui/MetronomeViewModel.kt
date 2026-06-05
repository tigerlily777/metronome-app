package com.example.metronome.ui

import androidx.lifecycle.ViewModel
import com.example.metronome.MetronomeEngine
import com.example.metronome.utils.Constants
import com.example.metronome.utils.Constants.AccentPattern
import com.example.metronome.utils.Constants.TimeSignature
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Manages all UI state for the metronome screen.
 * Acts as the single source of truth between UI and audio engine.
 */
@HiltViewModel
class MetronomeViewModel @Inject constructor(
    private val engine: MetronomeEngine
) : ViewModel() {

    // --- UI State ---

    private val _bpm = MutableStateFlow(Constants.BPM_DEFAULT)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _timeSignature = MutableStateFlow(TimeSignature.default())
    val timeSignature: StateFlow<TimeSignature> = _timeSignature.asStateFlow()

    private val _accentPattern = MutableStateFlow(AccentPattern.default())
    val accentPattern: StateFlow<AccentPattern> = _accentPattern.asStateFlow()

    // Expose all available time signatures and patterns for UI selection
    val availableTimeSignatures = TimeSignature.entries
    val availableAccentPatterns = AccentPattern.entries

    // --- User Actions ---

    fun togglePlayback() {
        if (_isPlaying.value) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        engine.play(
            bpm = _bpm.value,
            timeSignature = _timeSignature.value,
            accentPattern = _accentPattern.value
        )
        _isPlaying.value = true
    }

    private fun stopPlayback() {
        engine.release()
        _isPlaying.value = false
    }

    /**
     * Adjusts BPM by delta amount, staying within valid range.
     * If playing, restart with new BPM.
     */
    fun adjustBpm(delta: Int) {
        val newBpm = (_bpm.value + delta).coerceIn(Constants.BPM_MIN, Constants.BPM_MAX)
        setBpm(newBpm)
    }

    /**
     * Sets BPM to exact value.
     * If playing, restart playback with new BPM.
     */
    fun setBpm(newBpm: Int) {
        val clamped = newBpm.coerceIn(Constants.BPM_MIN, Constants.BPM_MAX)
        _bpm.value = clamped
        if (_isPlaying.value) {
            stopPlayback()
            startPlayback()
        }
    }

    /**
     * Changes the time signature.
     * If playing, restart playback.
     */
    fun setTimeSignature(timeSignature: TimeSignature) {
        _timeSignature.value = timeSignature
        if (_isPlaying.value) {
            stopPlayback()
            startPlayback()
        }
    }

    /**
     * Changes the accent pattern (which beat is emphasized).
     * If playing, restart playback.
     */
    fun setAccentPattern(accentPattern: AccentPattern) {
        _accentPattern.value = accentPattern
        if (_isPlaying.value) {
            stopPlayback()
            startPlayback()
        }
    }

    // --- Lifecycle ---

    override fun onCleared() {
        super.onCleared()
        engine.release()
    }
}