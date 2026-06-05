package com.example.metronome.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.metronome.utils.Constants

@Composable
fun MetronomeScreen(
    viewModel: MetronomeViewModel = hiltViewModel()
) {
    val bpm by viewModel.bpm.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // BPM display
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

            Spacer(modifier = Modifier.height(48.dp))

            // Fine and coarse adjustment buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { viewModel.adjustBpm(-Constants.BPM_STEP_LARGE) }) {
                    Text("-${Constants.BPM_STEP_LARGE}")
                }
                OutlinedButton(onClick = { viewModel.adjustBpm(-Constants.BPM_STEP_SMALL) }) {
                    Text("-${Constants.BPM_STEP_SMALL}")
                }
                OutlinedButton(onClick = { viewModel.adjustBpm(Constants.BPM_STEP_SMALL) }) {
                    Text("+${Constants.BPM_STEP_SMALL}")
                }
                OutlinedButton(onClick = { viewModel.adjustBpm(Constants.BPM_STEP_LARGE) }) {
                    Text("+${Constants.BPM_STEP_LARGE}")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Play / Stop button
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