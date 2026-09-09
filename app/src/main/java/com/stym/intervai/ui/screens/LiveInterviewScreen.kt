package com.stym.intervai.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stym.intervai.audio.RequestMicrophonePermission
import com.stym.intervai.ui.interview.InterviewState
import com.stym.intervai.ui.interview.InterviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveInterviewScreen(
    viewModel: InterviewViewModel,
    onInterviewFinished: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTtsSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val rmsDb by viewModel.sttManager.rmsDb.collectAsState()

    var manualInputText by remember { mutableStateOf("") }
    var hasMicPermission by remember { mutableStateOf(false) }

    RequestMicrophonePermission(
        onPermissionGranted = { hasMicPermission = true },
        onPermissionDenied = { hasMicPermission = false }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.selectedTopic.displayName, fontWeight = FontWeight.Bold) },
                actions = {
                    OutlinedButton(
                        onClick = { viewModel.endInterviewAndEvaluate() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("End Interview")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper Section: AI Question Display
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "INTERVIEWER QUESTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        when (val state = uiState) {
                            is InterviewState.AIQuestioning -> {
                                Text(
                                    text = state.question,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 24.sp
                                )
                            }
                            is InterviewState.CandidateAnswering -> {
                                Text(
                                    text = "Listening to your response...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            is InterviewState.Evaluating -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Evaluating interview responses...")
                                }
                            }
                            is InterviewState.Finished -> {
                                onInterviewFinished(state.evaluationReport)
                            }
                            is InterviewState.Error -> {
                                Text(
                                    text = "Error: ${state.message}",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            else -> {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // User Response Transcript Preview
                Text(
                    text = "YOUR SPOKEN RESPONSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        val transcript = when (val state = uiState) {
                            is InterviewState.CandidateAnswering -> state.liveTranscript
                            else -> ""
                        }
                        if (transcript.isBlank()) {
                            Text(
                                text = if (isTtsSpeaking) "AI is speaking..." else "Tap Mic or speak to answer...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = transcript,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Lower Section: Mic Control & Manual Fallback Input
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Audio Wave Pulse Indicator
                val scale by animateFloatAsState(
                    targetValue = if (uiState is InterviewState.CandidateAnswering) 1f + (rmsDb / 10f).coerceIn(0f, 0.5f) else 1f,
                    label = "micPulse"
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale)
                        .background(
                            color = if (uiState is InterviewState.CandidateAnswering) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (uiState is InterviewState.CandidateAnswering) {
                                viewModel.stopListeningAndSubmitAnswer()
                            } else {
                                viewModel.startListeningForAnswer()
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = if (uiState is InterviewState.CandidateAnswering) "🎙️ LISTENING" else "🎙️ SPEAK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text Fallback Submit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualInputText,
                        onValueChange = { manualInputText = it },
                        placeholder = { Text("Or type your answer here...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (manualInputText.isNotBlank()) {
                                viewModel.stopListeningAndSubmitAnswer(manualInputText)
                                manualInputText = ""
                            }
                        }
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
}
