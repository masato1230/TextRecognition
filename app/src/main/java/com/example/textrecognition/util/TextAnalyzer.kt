package com.example.textrecognition.util

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import java.util.Locale.filter

class TextAnalyzer(private val onRecognizeTexts: (texts: List<String>) -> Unit) : ImageAnalysis.Analyzer {
    private val recognizer =
        TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
            recognizer.process(image)
                .addOnSuccessListener { recognizedTexts ->
                    val outputTexts = mutableListOf<String>()
                    recognizedTexts.textBlocks.forEach {
                        val japaneseTexts = it.lines
                            .map { line ->
                                Log.d("Confidence", line.confidence.toString())
                                Log.d("Language", line.recognizedLanguage)
                                return@map line
                            }
                            .filter { line -> line.confidence > 0.5f }
                            .filter { line -> line.recognizedLanguage == "ja" }
                            .map { line ->  line.text }
                        outputTexts.addAll(japaneseTexts)
                        onRecognizeTexts(outputTexts)
                    }
                }
                .addOnFailureListener { it.printStackTrace() }
                .addOnCompleteListener { imageProxy.close() }
        }
    }
}