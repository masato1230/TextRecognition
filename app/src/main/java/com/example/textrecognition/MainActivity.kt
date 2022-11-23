package com.example.textrecognition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.textrecognition.components.CameraPreview
import com.example.textrecognition.ui.theme.TextRecognitionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextRecognitionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var recognizedTexts by remember { mutableStateOf(listOf<String>()) }

                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onRecognizeTexts = { recognizedTexts = it },
                    )
                    OverlayContent(recognizedTexts)
                }
            }
        }
    }
}

@Composable
fun OverlayContent(recognizedTexts: List<String>) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(20.dp),
        ) {
            items(recognizedTexts) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body1,
                    color = Color.White,
                )
            }
        }
    }
}
