package com.example.metronome.ui

import androidx.lifecycle.ViewModel
import com.example.metronome.MetronomeEngine
import com.example.metronome.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Holds all UI state for the metronome screen.
 * Acts as the single source of truth between the UI and the audio engine.
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

    // --- User Actions ---
    fun togglePlayback() {
        if (_isPlaying.value) {
            engine.release()
            _isPlaying.value = false
        } else {
            engine.play(_bpm.value)
            _isPlaying.value = true
        }
    }

    fun adjustBpm(delta: Int) {
        val newBpm = (_bpm.value + delta).coerceIn(Constants.BPM_MIN, Constants.BPM_MAX)
        _bpm.value = newBpm
        if (_isPlaying.value) {
            engine.play(newBpm)
        }
    }

    fun setBpm(newBpm: Int) {
        val clamped = newBpm.coerceIn(Constants.BPM_MIN, Constants.BPM_MAX)
        _bpm.value = clamped
        if (_isPlaying.value) {
            engine.play(clamped)
        }
    }

    // --- Lifecycle ---
    override fun onCleared() {
        super.onCleared()
        engine.release()
    }
}