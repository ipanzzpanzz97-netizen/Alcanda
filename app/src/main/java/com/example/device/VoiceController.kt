package com.example.device

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class VoiceUiState(
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val partialTranscript: String = "",
    val rmsLevel: Float = 0f,
    val speechAvailable: Boolean = false,
    val voiceError: String? = null
)

class VoiceController(private val context: Context) {

    private val _state = MutableStateFlow(
        VoiceUiState(speechAvailable = SpeechRecognizer.isRecognitionAvailable(context))
    )
    val state: StateFlow<VoiceUiState> = _state.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false
    private var onFinalResultCallback: ((String) -> Unit)? = null

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
                setTtsLocale("ms-MY")
            }
        }
    }

    fun setTtsLocale(languageTag: String) {
        if (!ttsReady) return
        val locale = Locale.forLanguageTag(languageTag)
        val res = textToSpeech?.setLanguage(locale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            textToSpeech?.setLanguage(Locale.US)
        }
    }

    fun startListening(languageTag: String, onFinalResult: (String) -> Unit) {
        stopSpeaking()
        onFinalResultCallback = onFinalResult

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = _state.value.copy(
                speechAvailable = false,
                voiceError = "Perkhidmatan pengecaman suara tidak tersedia pada peranti ini."
            )
            return
        }

        speechRecognizer?.cancel()

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _state.value = _state.value.copy(
                            isListening = true,
                            partialTranscript = "",
                            voiceError = null
                        )
                    }

                    override fun onBeginningOfSpeech() = Unit

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _state.value = _state.value.copy(rmsLevel = normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) = Unit

                    override fun onEndOfSpeech() {
                        _state.value = _state.value.copy(rmsLevel = 0f)
                    }

                    override fun onError(error: Int) {
                        if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY ||
                            error == SpeechRecognizer.ERROR_CLIENT
                        ) {
                            speechRecognizer?.destroy()
                            speechRecognizer = null
                        }
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Ralat audio mikrofon."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Kebenaran mikrofon diperlukan."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "Ralat rangkaian suara."
                            SpeechRecognizer.ERROR_NO_MATCH -> "Tiada suara dikesan. Cuba bercakap lagi."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Masa menunggu suara tamat."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Enjin suara sibuk — direset automatik, cuba lagi."
                            else -> "Pengecaman suara terhenti (kod $error)."
                        }
                        _state.value = _state.value.copy(
                            isListening = false,
                            rmsLevel = 0f,
                            voiceError = message
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull().orEmpty().trim()
                        _state.value = _state.value.copy(
                            isListening = false,
                            rmsLevel = 0f,
                            partialTranscript = ""
                        )
                        if (text.isNotEmpty()) {
                            onFinalResultCallback?.invoke(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                        if (partial.isNotEmpty()) {
                            _state.value = _state.value.copy(partialTranscript = partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                })
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        _state.value = _state.value.copy(
            speechAvailable = true,
            isListening = true,
            voiceError = null,
            partialTranscript = ""
        )
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _state.value = _state.value.copy(isListening = false, rmsLevel = 0f)
    }

    fun speak(text: String, languageTag: String) {
        if (!ttsReady || text.isBlank()) return
        setTtsLocale(languageTag)
        val cleanText = text.replace(Regex("[`*_#]"), " ").take(600)
        _state.value = _state.value.copy(isSpeaking = true)
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "novacore_tts")
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        _state.value = _state.value.copy(isSpeaking = false)
    }

    fun clearError() {
        _state.value = _state.value.copy(voiceError = null)
    }

    fun isSpeechAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun resetVoiceEngine(languageTag: String = "ms-MY") {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        stopSpeaking()
        setTtsLocale(languageTag)
        _state.value = _state.value.copy(
            speechAvailable = isSpeechAvailable(),
            isListening = false,
            rmsLevel = 0f,
            partialTranscript = "",
            voiceError = null
        )
    }

    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
