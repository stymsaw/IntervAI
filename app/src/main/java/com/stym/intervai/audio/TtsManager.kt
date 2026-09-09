package com.stym.intervai.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private val tag = "TtsManager"
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    var onSpeechCompleted: (() -> Unit)? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(tag, "TTS Language US is not supported or missing data")
            } else {
                _isInitialized.value = true
                setupUtteranceListener()
                Log.d(tag, "TTS Engine Initialized successfully")
            }
        } else {
            Log.e(tag, "TTS Initialization failed with status: $status")
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                onSpeechCompleted?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                Log.e(tag, "TTS Utterance Error: $utteranceId")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                Log.e(tag, "TTS Utterance Error code $errorCode for: $utteranceId")
            }
        })
    }

    fun speak(text: String, utteranceId: String = "INTERVAI_TTS_ID") {
        if (!_isInitialized.value) {
            Log.w(tag, "TTS not initialized yet")
            return
        }
        stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        _isInitialized.value = false
    }
}
