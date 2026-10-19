package com.myra.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

private var speaker: TextToSpeech? = null
private var speechRecognizer: SpeechRecognizer? = null

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    speaker = TextToSpeech(this) { status ->
        if (status == TextToSpeech.SUCCESS) {
            speaker?.setLanguage(Locale("en", "IN"))
        }
    }

    setContent {
        MyraApp(
            speak = { message -> speakText(message) },
            startListening = { onResult, onError ->
                startVoiceRecognition(onResult, onError)
            },
            stopListening = { stopVoiceRecognition() }
        )
    }
}

private fun speakText(message: String) {
    speaker?.speak(
        message,
        TextToSpeech.QUEUE_FLUSH,
        null,
        "MYRA_SPEECH"
    )
}

private fun startVoiceRecognition(
    onResult: (String) -> Unit,
    onError: (String) -> Unit
) {
    if (!SpeechRecognizer.isRecognitionAvailable(this)) {
        onError("Speech recognition is not available on this device.")
        return
    }

    stopVoiceRecognition()

    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

    speechRecognizer?.setRecognitionListener(
        object : android.speech.RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                onError("Voice recognition stopped. Tap MIC to try again.")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )
                val text = matches?.firstOrNull().orEmpty()
                if (text.isNotBlank()) {
                    onResult(text)
                } else {
                    onError("I could not hear that. Please try again.")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    )

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "gu-IN")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "gu-IN")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    }

    try {
        speechRecognizer?.startListening(intent)
    } catch (e: Exception) {
        onError("Could not start voice recognition.")
    }
}

private fun stopVoiceRecognition() {
    try {
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
    } catch (_: Exception) {
    }
    speechRecognizer = null
}

override fun onDestroy() {
    stopVoiceRecognition()
    speaker?.stop()
    speaker?.shutdown()
    speaker = null
    super.onDestroy()
}

}

private val BackgroundTop = Color(0xFF071126)
private val BackgroundBottom = Color(0xFF020611)
private val NeonBlue = Color(0xFF28B8FF)
private val SoftBlue = Color(0xFF83E0FF)
private val TextWhite = Color(0xFFF2F8FF)

@Composable
private fun MyraApp(
speak: (String) -> Unit,
startListening: ((String) -> Unit, (String) -> Unit) -> Unit,
stopListening: () -> Unit
) {
var currentPage by remember { mutableStateOf("home") }
var userName by remember { mutableStateOf("Jayesh") }
var listening by remember { mutableStateOf(false) }
var recognizedText by remember { mutableStateOf("Tap MIC and speak to MYRA") }
var statusText by remember { mutableStateOf("YOUR AI ASSISTANT") }

val context = androidx.compose.ui.platform.LocalContext.current

val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) {
        listening = true
        statusText = "LISTENING..."
        startListening(
            { result ->
                recognizedText = result
                listening = false
                statusText = "VOICE RECEIVED"
            },
            { error ->
                recognizedText = error
                listening = false
                statusText = "READY"
            }
        )
    } else {
        recognizedText = "Microphone permission is required to use voice input."
        statusText = "MICROPHONE PERMISSION NEEDED"
    }
}

MaterialTheme {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundBottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BackgroundTop,
                            Color(0xFF081B35),
                            BackgroundBottom
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MYRA",
                            color = TextWhite,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 5.sp
                        )
                        Text(
                            text = "PERSONAL AI",
                            color = SoftBlue,
                            fontSize = 10.sp,
                            letterSpacing = 3.sp
                        )
                    }

                    Text(
                        text = "● ONLINE",
                        color = Color(0xFF62F5B0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (currentPage == "home") {
                    Text(
                        text = greetingForTime(),
                        color = TextWhite,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "I'm MYRA. How can I help you, $userName?",
                        color = Color(0xFFB5C8E3),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    HologramOrb(listening = listening)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = statusText,
                        color = SoftBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = recognizedText,
                        color = Color(0xFFD2DDF0),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (listening) {
                                stopListening()
                                listening = false
                                statusText = "READY"
                            } else {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (granted) {
                                    listening = true
                                    statusText = "LISTENING..."
                                    startListening(
                                        { result ->
                                            recognizedText = result
                                            listening = false
                                            statusText = "VOICE RECEIVED"
                                        },
                                        { error ->
                                            recognizedText = error
                                            listening = false
                                            statusText = "READY"
                                        }
                                    )
                                } else {
                                    permissionLauncher.launch(
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                }
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (listening) {
                                Color(0xFFFF496C)
                            } else {
                                Color(0xFF0879D9)
                            }
                        ),
                        modifier = Modifier.size(86.dp)
                    ) {
                        Text(
                            text = if (listening) "STOP" else "MIC",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (listening) {
                            "Tap to stop listening"
                        } else {
                            "Tap the microphone to speak"
                        },
                        color = Color(0xFF91A9C8),
                        fontSize = 12.sp
                    )
                } else if (currentPage == "settings") {
                    Text(
                        text = "SETTINGS",
                        color = TextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Your profile name",
                        color = SoftBlue,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = userName,
                        onValueChange = { userName = it },
                        singleLine = true,
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF102440),
                            unfocusedContainerColor = Color(0xFF102440),
                            focusedIndicatorColor = NeonBlue,
                            unfocusedIndicatorColor = Color(0xFF456080),
                            focusedLabelColor = SoftBlue,
                            unfocusedLabelColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Microphone permission is requested when you tap MIC.",
                        color = Color(0xFFB5C8E3),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Other permissions and advanced assistant features are not configured yet.",
                        color = Color(0xFFB5C8E3),
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "ABOUT MYRA",
                        color = TextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "MYRA AI",
                        color = SoftBlue,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your personal assistant interface.",
                        color = Color(0xFFB5C8E3),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Voice recognition depends on a compatible speech recognition service on your device.",
                        color = Color(0xFF91A9C8),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF0B1930),
                            RoundedCornerShape(22.dp)
                        )
                        .border(
                            1.dp,
                            Color(0xFF183A5D),
                            RoundedCornerShape(22.dp)
                        )
                        .padding(vertical = 15.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        label = "HOME",
                        selected = currentPage == "home",
                        onClick = { currentPage = "home" }
                    )

                    BottomNavItem(
                        label = "SETTINGS",
                        selected = currentPage == "settings",
                        onClick = { currentPage = "settings" }
                    )

                    BottomNavItem(
                        label = "ABOUT",
                        selected = currentPage == "about",
                        onClick = { currentPage = "about" }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

}

@Composable
private fun HologramOrb(listening: Boolean) {
val transition = rememberInfiniteTransition(label = "myra_orb")

val pulse by transition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
        animation = tween(
            durationMillis = 1800,
            easing = FastOutSlowInEasing
        ),
        repeatMode = RepeatMode.Reverse
    ),
    label = "orb_pulse"
)

Box(
    modifier = Modifier.size(245.dp),
    contentAlignment = Alignment.Center
) {
    Box(
        modifier = Modifier
            .size(235.dp)
            .scale(pulse)
            .border(
                width = 1.dp,
                color = NeonBlue.copy(alpha = 0.6f),
                shape = CircleShape
            )
    )

    Box(
        modifier = Modifier
            .size(195.dp)
            .border(
                width = 2.dp,
                color = SoftBlue.copy(alpha = 0.65f),
                shape = CircleShape
            )
    )

    Box(
        modifier = Modifier
            .size(155.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF80E8FF),
                        Color(0xFF138BFF),
                        Color(0xFF102C83),
                        Color(0xFF071126)
                    )
                ),
                CircleShape
            )
            .border(
                width = 2.dp,
                color = Color(0xFF9DEBFF),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (listening) "•••" else "M",
            color = Color.White,
            fontSize = if (listening) 34.sp else 56.sp,
            fontWeight = FontWeight.ExtraLight
        )
    }

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 18.dp)
            .size(9.dp)
            .background(SoftBlue, CircleShape)
    )

    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 28.dp, bottom = 42.dp)
            .size(7.dp)
            .background(Color.White, CircleShape)
    )

    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 15.dp)
            .size(6.dp)
            .background(NeonBlue, CircleShape)
    )
}

}

@Composable
private fun BottomNavItem(
label: String,
selected: Boolean,
onClick: () -> Unit
) {
Column(
modifier = Modifier
.clickable(onClick = onClick)
.padding(horizontal = 8.dp),
horizontalAlignment = Alignment.CenterHorizontally
) {
Text(
text = when (label) {
"HOME" -> "⌂"
"SETTINGS" -> "⚙"
else -> "ⓘ"
},
color = if (selected) SoftBlue else Color(0xFF8DA5C3),
fontSize = 23.sp
)

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = label,
        color = if (selected) SoftBlue else Color(0xFF8DA5C3),
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold
    )
}

}

private fun greetingForTime(): String {
val hour = SimpleDateFormat("HH", Locale.getDefault())
.format(Date())
.toIntOrNull() ?: 12

return when (hour) {
    in 5..11 -> "Good Morning, ${"Jayesh"}!"
    in 12..16 -> "Good Afternoon, Jayesh!"
    else -> "Good Evening, Jayesh!"
}

}
