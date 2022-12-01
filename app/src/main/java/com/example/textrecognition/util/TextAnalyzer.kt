package com.example.textrecognition.util

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions

class TextAnalyzer(
    private val onRecognizeTexts: (texts: List<Text.Line>) -> Unit,
) : ImageAnalysis.Analyzer {
    private val recognizer =
        TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    private val outputTexts = mutableListOf<Text.Line>()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { recognizedTexts ->
                    outputTexts.clear()
                    recognizedTexts.textBlocks.forEach { block ->
                        outputTexts.addAll(block.lines)
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