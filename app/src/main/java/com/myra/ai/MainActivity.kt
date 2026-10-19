
package com.myra.ai

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale

private val Background = Color(0xFF050916)
private val NeonBlue = Color(0xFF35D9FF)
private val PurpleBlue = Color(0xFF6965FF)

class MainActivity : ComponentActivity() {

    private var speaker: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        speaker = android.speech.tts.TextToSpeech(this) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                speaker?.language = Locale("en", "IN")
            }
        }

        setContent {
            MaterialTheme {
                MyraApp(
                    speak = { message ->
                        speaker?.speak(
                            message,
                            android.speech.tts.TextToSpeech.QUEUE_FLUSH,
                            null,
                            "MYRA_GREETING"
                        )
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        speaker?.stop()
        speaker?.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun MyraApp(speak: (String) -> Unit) {
    var micOn by remember { mutableStateOf(false) }
    var listeningText by remember { mutableStateOf("TAP TO CONNECT") }
    var page by remember { mutableStateOf("home") }
    var userName by remember { mutableStateOf("Jayesh") }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val words = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()

        listeningText = words ?: "I'M READY TO HELP"
        micOn = false
    }

    fun greeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeGreeting = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }

        return "$timeGreeting, Sir! Muje Jayesh Boss ne banaya hai aur mera naam MYRA hai. Main aapki kya madad kar sakti hoon?"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF101A36),
                        Background,
                        Color(0xFF080B19)
                    )
                )
            )
    ) {
        when (page) {
            "home" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(42.dp))

                    Text(
                        "MYRA AI",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 5.sp
                    )

                    Text(
                        "YOUR PERSONAL AI ASSISTANT",
                        color = NeonBlue,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.weight(1f))

                    Hologram()

                    Spacer(Modifier.height(12.dp))

                    Text(
                        if (micOn) "MYRA IS LISTENING" else listeningText,
                        color = if (micOn) NeonBlue else Color.LightGray,
                        fontSize = 13.sp,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonBlue, PurpleBlue)
                                ),
                                CircleShape
                            )
                            .clickable {
                                if (micOn) {
                                    micOn = false
                                    listeningText = "TAP TO CONNECT"
                                } else {
                                    micOn = true
                                    listeningText = "MYRA IS LISTENING"
                                    speak(greeting())

                                    try {
                                        val intent = Intent(
                                            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                                        ).apply {
                                            putExtra(
                                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                            )
                                            putExtra(
                                                RecognizerIntent.EXTRA_LANGUAGE,
                                                "hi-IN"
                                            )
                                            putExtra(
                                                RecognizerIntent.EXTRA_PROMPT,
                                                "Boliye, main sun rahi hoon"
                                            )
                                        }

                                        speechLauncher.launch(intent)
                                    } catch (_: Exception) {
                                        micOn = false
                                        listeningText =
                                            "SPEECH SERVICE UNAVAILABLE"
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (micOn) "■" else "🎙",
                            fontSize = 32.sp,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        if (micOn) "TAP TO TURN OFF" else "TAP TO SPEAK",
                        color = Color.White,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 28.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BottomOption("⚙", "Settings") {
                            page = "settings"
                        }

                        BottomOption("ⓘ", "About") {
                            page = "about"
                        }
                    }
                }
            }

            "settings" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        "SETTINGS",
                        color = NeonBlue,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(24.dp))

                    Text("PROFILE", color = Color.LightGray)

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Your name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "PERMISSIONS",
                        color = NeonBlue,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Microphone\nCamera\nPhone and calls\nContacts\nSMS\nLocation\nGallery and files\nCall handling\nBattery optimization\nDisplay over other apps\nNotifications\nNotification access\nAccessibility\nDefault assist",
                        color = Color.White,
                        lineHeight = 27.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Permission controls will be connected in a later step.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = { page = "home" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("BACK TO MYRA")
                    }
                }
            }

            "about" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Hologram()

                    Text(
                        "MYRA AI",
                        color = NeonBlue,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "A personal voice assistant created by Jayesh Boss.",
                        color = Color.White,
                        fontSize = 16.sp
                    )

                    Spacer(Modifier.height(28.dp))

                    Button(onClick = { page = "home" }) {
                        Text("BACK")
                    }
                }
            }
        }
    }
}

@Composable
private fun Hologram() {
    val transition = rememberInfiniteTransition(label = "hologram")

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 7000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        val cx = size.width / 2
        val cy = size.height / 2
        val radius = size.minDimension * 0.25f * pulse

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x8835D9FF),
                    Color(0x44365DFF),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = radius * 2.1f
            ),
            radius = radius * 2.1f,
            center = Offset(cx, cy)
        )

        for (i in 0..3) {
            val ringRadius = radius * (0.85f + i * 0.22f)

            drawCircle(
                color = if (i % 2 == 0) NeonBlue else PurpleBlue,
                radius = ringRadius,
                center = Offset(cx, cy),
                style = Stroke(
                    width = if (i == 0) 3.dp.toPx() else 1.2.dp.toPx()
                )
            )
        }

        drawArc(
            color = Color.White.copy(alpha = 0.85f),
            startAngle = rotation,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(
                cx - radius * 1.3f,
                cy - radius * 1.3f
            ),
            size = Size(radius * 2.6f, radius * 2.6f),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    NeonBlue,
                    Color(0xFF2455CB)
                )
            ),
            radius = radius * 0.72f,
            center = Offset(cx, cy)
        )

        for (i in 0..11) {
            val angle = Math.toRadians(rotation + i * 30.0)
            val orbit = radius * 1.55f
            val x = cx + kotlin.math.cos(angle).toFloat() * orbit
            val y = cy + kotlin.math.sin(angle).toFloat() * orbit

            drawCircle(
                color = NeonBlue.copy(alpha = 0.8f),
                radius = 2.5.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun BottomOption(
    symbol: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                Color(0xFF17213B),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 26.dp, vertical = 12.dp)
    ) {
        Text(
            symbol,
            fontSize = 24.sp,
            color = NeonBlue
        )

        Text(
            label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}
