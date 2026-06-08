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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.metronome.utils.Constants
import androidx.compose.ui.tooling.preview.Preview
import com.example.metronome.ui.theme.MetronomeTheme


@Composable
fun MetronomeScreen(
    viewModel: MetronomeViewModel = hiltViewModel()
) {
    val bpm by viewModel.bpm.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val timeSignature by viewModel.timeSignature.collectAsStateWithLifecycle()
    val accentPattern by viewModel.accentPattern.collectAsStateWithLifecycle()
    val subdivision by viewModel.subdivision.collectAsStateWithLifecycle()
    val beatEmphasis by viewModel.beatEmphasis.collectAsStateWithLifecycle()

    var bpmInputText by remember(bpm) { mutableStateOf(bpm.toString()) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // --- Time Signature Visualization ---
            TimeMeterDisplay(timeSignature = timeSignature)

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

            // --- BPM Control Section ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Slider
                Slider(
                    value = bpm.toFloat(),
                    onValueChange = { viewModel.setBpm(it.toInt()) },
                    valueRange = Constants.BPM_MIN.toFloat()..Constants.BPM_MAX.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Text input
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

                // Fine and coarse adjustment buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.adjustBpm(-Constants.BPM_STEP_LARGE) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("-${Constants.BPM_STEP_LARGE}")
                    }
                    Button(
                        onClick = { viewModel.adjustBpm(-Constants.BPM_STEP_SMALL) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("-${Constants.BPM_STEP_SMALL}")
                    }
                    Button(
                        onClick = { viewModel.adjustBpm(Constants.BPM_STEP_SMALL) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("+${Constants.BPM_STEP_SMALL}")
                    }
                    Button(
                        onClick = { viewModel.adjustBpm(Constants.BPM_STEP_LARGE) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("+${Constants.BPM_STEP_LARGE}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Time Signature Selection ---
            SectionHeader("Time Signature")
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

            // --- Accent Pattern Selection ---
            SectionHeader("Accent Pattern")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                viewModel.availableAccentPatterns.forEach { pattern ->
                    FilterChip(
                        selected = accentPattern == pattern,
                        onClick = { viewModel.setAccentPattern(pattern) },
                        label = { Text(pattern.displayName, fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- Subdivision Selection ---
            SectionHeader("Clicks Per Beat")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                viewModel.availableSubdivisions.forEach { sub ->
                    FilterChip(
                        selected = subdivision == sub,
                        onClick = { viewModel.setSubdivision(sub) },
                        label = { Text(sub.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- Beat Emphasis Selection ---
            SectionHeader("First Beat Style")
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Split into two rows for better layout
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = beatEmphasis == Constants.BeatEmphasis.VOLUME_ONLY,
                        onClick = { viewModel.setBeatEmphasis(Constants.BeatEmphasis.VOLUME_ONLY) },
                        label = { Text("Louder", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = beatEmphasis == Constants.BeatEmphasis.PITCH_ONLY,
                        onClick = { viewModel.setBeatEmphasis(Constants.BeatEmphasis.PITCH_ONLY) },
                        label = { Text("Higher", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = beatEmphasis == Constants.BeatEmphasis.VOLUME_AND_PITCH,
                        onClick = { viewModel.setBeatEmphasis(Constants.BeatEmphasis.VOLUME_AND_PITCH) },
                        label = { Text("Both", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = beatEmphasis == Constants.BeatEmphasis.DIFFERENT_PITCH,
                        onClick = { viewModel.setBeatEmphasis(Constants.BeatEmphasis.DIFFERENT_PITCH) },
                        label = { Text("Lower", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Shows the time signature as a visual meter (e.g., 4/4 displays 4 quarter notes)
 */
@Composable
private fun TimeMeterDisplay(timeSignature: Constants.TimeSignature) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(timeSignature.beatsPerMeasure) {
            Text(
                text = "♩",
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Reusable section header
 */
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        textAlign = TextAlign.Start
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MetronomeScreenPreview() {
    MetronomeTheme {
        // For preview, we can't use hiltViewModel() directly
        // This is just for visual preview purposes
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                TimeMeterDisplay(timeSignature = Constants.TimeSignature.FOUR_FOUR)

                Text(
                    text = "120",
                    fontSize = 96.sp,
                    style = MaterialTheme.typography.displayLarge
                )

                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {},
                    modifier = Modifier.size(width = 160.dp, height = 56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}