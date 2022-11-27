package com.example.textrecognition.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class JaToEngTranslator {
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.JAPANESE)
        .build()
    private val englishJapaneseTranslator = Translation.getClient(options)

    init {
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        englishJapaneseTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // Model downloaded successfully. Okay to start translating.
                // (Set a flag, unhide the translation UI, etc.)
            }
            .addOnFailureListener { exception ->
                // Model couldn’t be downloaded or other internal error.
                // ...
            }
    }

    fun translate(text: String, onSuccess: (String) -> Unit) {
        englishJapaneseTranslator.translate(text)
            .addOnSuccessListener { translatedText ->
                onSuccess(translatedText)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }
}