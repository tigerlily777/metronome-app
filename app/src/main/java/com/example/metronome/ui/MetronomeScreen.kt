package com.example.metronome.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.metronome.utils.Constants

@Composable
fun MetronomeScreen(
    viewModel: MetronomeViewModel = hiltViewModel()
) {
    val bpm by viewModel.bpm.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val timeSignature by viewModel.timeSignature.collectAsStateWithLifecycle()
    val accentPattern by viewModel.accentPattern.collectAsStateWithLifecycle()

    // Local state for text input
    var bpmInputText by remember(bpm) { mutableStateOf(bpm.toString()) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- Visual feedback: flashing background ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        color = if (isPlaying)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♩",
                    fontSize = 48.sp,
                    color = if (isPlaying)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- BPM Display ---
            Text(
                text = bpm.toString(),
                fontSize = 96.sp,
                style = MaterialTheme.typography.displayLarge
            )

            Text(
                text = "BPM",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // --- BPM Slider ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = bpm.toFloat(),
                    onValueChange = { viewModel.setBpm(it.toInt()) },
                    valueRange = Constants.BPM_MIN.toFloat()..Constants.BPM_MAX.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- BPM Input Field ---
            OutlinedTextField(
                value = bpmInputText,
                onValueChange = { newValue ->
                    bpmInputText = newValue
                    newValue.toIntOrNull()?.let { viewModel.setBpm(it) }
                },
                label = { Text("Enter BPM") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .align(Alignment.CenterHorizontally),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Time Signature Selection ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Time Signature",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.availableTimeSignatures.forEach { sig ->
                        FilterChip(
                            selected = timeSignature == sig,
                            onClick = { viewModel.setTimeSignature(sig) },
                            label = { Text(sig.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- Accent Pattern Selection ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Beat Pattern",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.availableAccentPatterns.forEach { pattern ->
                        FilterChip(
                            selected = accentPattern == pattern,
                            onClick = { viewModel.setAccentPattern(pattern) },
                            label = { Text(pattern.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Play / Stop Button ---
            Button(
                onClick = { viewModel.togglePlayback() },
                modifier = Modifier.size(width = 160.dp, height = 56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isPlaying) "Stop" else "Start",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * Helper composable for visual feedback box
 */
@Composable
private fun Box(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier,
        contentAlignment = contentAlignment
    ) {
        content()
    }
}