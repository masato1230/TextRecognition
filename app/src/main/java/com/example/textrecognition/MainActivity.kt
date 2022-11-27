package com.example.textrecognition

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.textrecognition.components.AutoResizeText
import com.example.textrecognition.components.CameraPreview
import com.example.textrecognition.components.FontSizeRange
import com.example.textrecognition.ui.theme.TextRecognitionTheme
import com.example.textrecognition.util.JaToEngTranslator
import com.google.mlkit.vision.text.Text

class MainActivity : ComponentActivity() {
    val translator = JaToEngTranslator()

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
                    OverlayContent(recognizedTexts, translator)
                }
            }
        }
    }
}

@Composable
fun OverlayContent(
    recognizedTextElements: List<Text.Line>,
    translator: JaToEngTranslator,
    imageSize: Size = Size(1080f, 1920f),
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp

    with(LocalDensity.current) {
        val scaleFactor = (screenHeightDp * density) / imageSize.height
        val offsetXDp = (screenHeightDp * imageSize.width / imageSize.height - screenWidthDp) / 2

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    recognizedTextElements.forEach { textElement ->
                        textElement.boundingBox?.let { rect ->
                            rotate(textElement.angle + 90) {
                                drawPath(
                                    path = Path().apply {
                                        moveTo(
                                            x = rect.left * scaleFactor - offsetXDp * density,
                                            y = rect.top * scaleFactor
                                        )
                                        lineTo(
                                            x = rect.right * scaleFactor - offsetXDp * density,
                                            y = rect.top * scaleFactor
                                        )
                                        lineTo(
                                            x = rect.right * scaleFactor - offsetXDp * density,
                                            y = rect.bottom * scaleFactor
                                        )
                                        lineTo(
                                            x = rect.left * scaleFactor - offsetXDp * density,
                                            y = rect.bottom * scaleFactor
                                        )
                                        close()
                                    },
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
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
                            .offset(topLeft.x.dp, topLeft.y.dp)
                            .height(size.height.toDp())
                            .width(size.width.toDp())
                            .clickable {
                                clipboardManager.setText(AnnotatedString(textElement.text))
                                Toast
                                    .makeText(
                                        context,
                                        "Copied ${textElement.text}",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                            .rotate(textElement.angle + 90)
                    ) {
                        var text by remember { mutableStateOf(textElement.text) }
                        val textStyleBody1 = MaterialTheme.typography.body1
                        var textStyle by remember { mutableStateOf(textStyleBody1) }
                        var readyToDraw by remember { mutableStateOf(false) }

                        translator.translate(textElement.text) {
                            Log.d("Result", it)
                            text = it
                        }
                        AutoResizeText(
                            text = text,
                            color = color,
                            style = textStyle,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(),
                            fontSizeRange = FontSizeRange(min = 5.sp, max = 30.sp),
                        )
                    }
                }
            }
        }
    }
}
