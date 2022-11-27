package com.example.textrecognition.util

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import java.util.Locale.filter
import java.util.stream.Collectors.toList

class TextAnalyzer(
    private val onRecognizeTexts: (texts: List<Text.Element>) -> Unit,
) : ImageAnalysis.Analyzer {
    private val recognizer =
        TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    private val outputTexts = mutableListOf<Text.Element>()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { recognizedTexts ->
                    outputTexts.clear()
                    recognizedTexts.textBlocks.forEach {
                        it.lines.forEach { line ->
//                            val japaneseTexts = line.elements.filter { element -> element.recognizedLanguage == "ja" }
                            outputTexts.addAll(line.elements)
                        }
                    }
                    onRecognizeTexts(
                        outputTexts.distinctBy { outputText -> outputText.text }.toList()
                    )
                }
                .addOnFailureListener { it.printStackTrace() }
                .addOnCompleteListener { imageProxy.close() }
        }
    }
}