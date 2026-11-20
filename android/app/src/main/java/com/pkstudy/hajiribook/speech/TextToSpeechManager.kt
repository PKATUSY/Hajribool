package com.pkstudy.hajiribook.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    var isEnabled = true
    var currentLanguage: String = "en" // "en" or "hi"
    var speechRate: Float = 1.0f

    // Cooldown per student name to avoid spamming
    private val announcementTimestamps = HashMap<String, Long>()
    private val announcementCooldownMs = 10000L // 10s cooldown

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            applyLanguage(currentLanguage)
            tts?.setSpeechRate(speechRate)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {}
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {}
            })
        } else {
            Log.e("TextToSpeechManager", "TTS initialization failed")
            isInitialized = false
        }
    }

    fun setLanguage(langCode: String) {
        currentLanguage = langCode
        if (isInitialized) {
            applyLanguage(langCode)
        }
    }

    private fun applyLanguage(langCode: String) {
        val locale = when (langCode) {
            "hi" -> Locale("hi", "IN")
            else -> Locale.ENGLISH
        }
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to default
            tts?.setLanguage(Locale.ENGLISH)
        }
    }

    /**
     * Non-blocking announcement of student attendance.
     */
    fun announceAttendance(studentName: String) {
        if (!isEnabled || !isInitialized) return

        val now = System.currentTimeMillis()
        val lastAnnounced = announcementTimestamps[studentName] ?: 0L
        if (now - lastAnnounced < announcementCooldownMs) {
            return // Skip repeat announcement within cooldown
        }
        announcementTimestamps[studentName] = now

        val speechText = if (currentLanguage == "hi") {
            "नमस्ते $studentName। आपकी उपस्थिति दर्ज कर ली गई है।"
        } else {
            "Welcome $studentName. Your attendance has been marked."
        }

        // QUEUE_ADD ensures non-blocking queuing without interrupting active speech abruptly
        tts?.speak(speechText, TextToSpeech.QUEUE_ADD, null, "ATTENDANCE_${System.currentTimeMillis()}")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
