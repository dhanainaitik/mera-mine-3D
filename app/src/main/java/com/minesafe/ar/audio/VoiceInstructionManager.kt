package com.minesafe.ar.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class VoiceInstructionManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var currentLanguage = Locale.ENGLISH

    init {
        tts = TextToSpeech(context, this)
    }

    fun setLanguage(languageCode: String) {
        currentLanguage = if (languageCode == "hi") Locale.Builder().setLanguage("hi").setRegion("IN").build() else Locale.ENGLISH
        
        if (isReady) {
            val result = tts?.setLanguage(currentLanguage)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("VoiceManager", "Language $languageCode is not supported on this device's TTS. Falling back to English.")
                tts?.setLanguage(Locale.ENGLISH)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            setLanguage(currentLanguage.language)
        } else {
            Log.e("VoiceManager", "Initialization Failed!")
        }
    }

    fun speak(instruction: String) {
        if (isReady) {
            tts?.speak(instruction, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
