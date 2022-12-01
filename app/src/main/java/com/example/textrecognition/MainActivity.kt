package com.example.textrecognition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.textrecognition.components.CameraPreview
import com.example.textrecognition.ui.theme.TextRecognitionTheme
import com.google.mlkit.vision.text.Text

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
                    var recognizedTexts by remember { mutableStateOf(listOf<Text.Line>()) }

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
fun OverlayContent(
    recognizedTextElements: List<Text.Line>,
    imageSize: Size = Size(1080f, 1920f),
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp

    with(LocalDensity.current) {
        val scaleFactor = (screenHeightDp * density) / imageSize.height
        val offsetXDp = (screenHeightDp * imageSize.width / imageSize.height - screenWidthDp) / 2

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            recognizedTextElements.forEach { textElement ->
                textElement.boundingBox?.let { rect ->
                    val topLeft = Offset(
                        x = (rect.left * scaleFactor / density) - offsetXDp,
                        y = (rect.top * scaleFactor) / density,
                    )
                    val size = Size(
                        width = (rect.right - rect.left) * scaleFactor,
                        height = (rect.bottom - rect.top) * scaleFactor,
                    )
                    val color = when (textElement.confidence) {
                        in 0f..0.8f -> Color.Black
                        else -> Color.Magenta
                    }
                    Box(
                        modifier = Modifier

                    ) {
                        val initialTextStyle = MaterialTheme.typography.body1.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        var textStyle by remember { mutableStateOf(initialTextStyle) }
                        var readyToDraw by remember { mutableStateOf(false) }
                        Text(
                            text = textElement.text,
                            color = color,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier
                                .offset(topLeft.x.dp, topLeft.y.dp)
                                .height(size.height.toDp())
                                .width(size.width.toDp())
                                .rotate(textElement.angle + 90)
                                .background(Color.White.copy(alpha = 0.7f))
                                .drawWithContent {
                                    if (readyToDraw) drawContent()
                                },
                            onTextLayout = { textLayoutResult ->
                                if (textLayoutResult.didOverflowHeight) {
                                    textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.8)
                                } else {
                                    readyToDraw = true
                                }
                            },
                            style = textStyle,
                        )
                    }
                }
            }
        }
    }
}
