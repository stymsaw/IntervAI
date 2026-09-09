package com.stym.intervai.ui.interview

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stym.intervai.audio.SttManager
import com.stym.intervai.audio.SttState
import com.stym.intervai.audio.TtsManager
import com.stym.intervai.data.api.GroqApiService
import com.stym.intervai.data.model.ChatMessage
import com.stym.intervai.data.model.GroqChatRequest
import com.stym.intervai.data.model.InterviewTopic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class InterviewState {
    object Idle : InterviewState()
    object Setup : InterviewState()
    data class AIQuestioning(val question: String) : InterviewState()
    data class CandidateAnswering(val liveTranscript: String) : InterviewState()
    object Evaluating : InterviewState()
    data class Finished(val evaluationReport: String) : InterviewState()
    data class Error(val message: String) : InterviewState()
}

class InterviewViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = "InterviewViewModel"

    val ttsManager = TtsManager(application)
    val sttManager = SttManager(application)
    private val apiService = GroqApiService.create()

    private val _uiState = MutableStateFlow<InterviewState>(InterviewState.Idle)
    val uiState: StateFlow<InterviewState> = _uiState.asStateFlow()

    private val conversationHistory = mutableListOf<ChatMessage>()
    var selectedTopic: InterviewTopic = InterviewTopic.ANDROID_ARCHITECTURE
        private set

    init {
        observeSttState()
        setupTtsCallback()
    }

    fun startInterview(topic: InterviewTopic) {
        selectedTopic = topic
        conversationHistory.clear()
        conversationHistory.add(ChatMessage("system", topic.systemPrompt))
        
        _uiState.value = InterviewState.Setup
        fetchNextAIQuestion()
    }

    private fun fetchNextAIQuestion() {
        viewModelScope.launch {
            _uiState.value = InterviewState.AIQuestioning("Thinking of next question...")
            
            val result = com.stym.intervai.data.AppLogger.runCatchingCentralized(
                tag = tag,
                actionMessage = "Failed to fetch AI Question from Groq API"
            ) {
                val request = GroqChatRequest(messages = conversationHistory.toList())
                apiService.getChatCompletion(request = request)
            }

            result.onSuccess { response ->
                val aiReply = response.choices.firstOrNull()?.message?.content ?: "Could you please elaborate on your experience?"
                conversationHistory.add(ChatMessage("assistant", aiReply))
                _uiState.value = InterviewState.AIQuestioning(aiReply)
                ttsManager.speak(aiReply)
            }.onFailure { exception ->
                _uiState.value = InterviewState.Error(exception.localizedMessage ?: "Failed to connect to Groq AI")
            }
        }
    }

    private fun setupTtsCallback() {
        ttsManager.onSpeechCompleted = {
            // Once AI finishes speaking out question, automatically enable microphone for candidate answer
            viewModelScope.launch {
                if (_uiState.value is InterviewState.AIQuestioning) {
                    startListeningForAnswer()
                }
            }
        }
    }

    fun stopTtsSpeaking() {
        ttsManager.stop()
    }

    fun startListeningForAnswer() {
        ttsManager.stop()
        _uiState.value = InterviewState.CandidateAnswering("")
        sttManager.startListening()
    }

    fun stopListeningAndSubmitAnswer(manualText: String? = null) {
        sttManager.stopListening()
        val answer = manualText ?: when (val state = sttManager.state.value) {
            is SttState.FinalResult -> state.text
            is SttState.PartialResult -> state.text
            else -> ""
        }

        if (answer.isNotBlank()) {
            conversationHistory.add(ChatMessage("user", answer))
            fetchNextAIQuestion()
        } else {
            _uiState.value = InterviewState.Error("No answer recognized. Please speak or type your response.")
        }
    }

    private fun observeSttState() {
        viewModelScope.launch {
            sttManager.state.collectLatest { state ->
                when (state) {
                    is SttState.PartialResult -> {
                        if (_uiState.value is InterviewState.CandidateAnswering) {
                            _uiState.value = InterviewState.CandidateAnswering(state.text)
                        }
                    }
                    is SttState.FinalResult -> {
                        if (_uiState.value is InterviewState.CandidateAnswering) {
                            _uiState.value = InterviewState.CandidateAnswering(state.text)
                        }
                    }
                    is SttState.Error -> {
                        Log.w(tag, "STT Warning: ${state.message}")
                    }
                    else -> {}
                }
            }
        }
    }

    fun endInterviewAndEvaluate() {
        ttsManager.stop()
        sttManager.stopListening()
        
        viewModelScope.launch {
            _uiState.value = InterviewState.Evaluating
            
            val result = com.stym.intervai.data.AppLogger.runCatchingCentralized(
                tag = tag,
                actionMessage = "Failed to generate evaluation report from Groq API"
            ) {
                val evalPrompt = ChatMessage(
                    "system",
                    "The interview session has concluded. Evaluate the candidate's answers based on the conversation history. Provide a structured review with: 1) Overall Score (1-10), 2) Key Strengths, 3) Areas for Improvement, and 4) Actionable Recommendations."
                )
                val evalHistory = conversationHistory + evalPrompt
                apiService.getChatCompletion(request = GroqChatRequest(messages = evalHistory, maxTokens = 600))
            }

            result.onSuccess { response ->
                val evalReport = response.choices.firstOrNull()?.message?.content ?: "Unable to generate evaluation."
                _uiState.value = InterviewState.Finished(evalReport)
            }.onFailure { exception ->
                _uiState.value = InterviewState.Error("Evaluation failed: ${exception.localizedMessage}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        sttManager.destroy()
    }
}
