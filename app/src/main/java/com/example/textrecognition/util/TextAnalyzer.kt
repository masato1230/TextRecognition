package com.example.textrecognition.util

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions

class TextAnalyzer(private val onRecognizeText: (text: Text) -> Unit) : ImageAnalysis.Analyzer {
    private val recognizer =
        TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
            recognizer.process(image)
                .addOnSuccessListener { recognizedText ->
                    Log.d("Output", recognizedText.text)
                    recognizedText?.let {
                        onRecognizeText(it)
                    }
                }
                .addOnFailureListener { it.printStackTrace() }
                .addOnCompleteListener { imageProxy.close() }
        }
    }
}