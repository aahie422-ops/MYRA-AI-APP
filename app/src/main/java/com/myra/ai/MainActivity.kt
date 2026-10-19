
package com.myra.ai

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("hi", "IN")
            }
        }

        setContent {
            MyraScreen(
                onSpeak = { text ->
                    tts?.speak(
                        text,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "myra_reply"
                    )
                }
            )
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun MyraScreen(onSpeak: (String) -> Unit) {
    var message by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(listOf("MYRA AI: नमस्ते! मैं MYRA AI हूँ। मुझसे कुछ पूछिए।"))
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()

        if (!spoken.isNullOrBlank()) {
            message = spoken
        }
    }

    fun sendMessage() {
        val text = message.trim()
        if (text.isEmpty()) return

        messages = messages + "आप: $text"
        val reply = when {
            text.contains("hello", true) ||
            text.contains("नमस्ते") ||
            text.contains("હેલો", true) ->
                "नमस्ते दोस्त! मैं MYRA AI हूँ। बताइए, आपकी क्या मदद करूँ?"

            text.contains("तुम कौन", true) ||
            text.contains("who are you", true) ->
                "मैं MYRA AI हूँ, आपका स्मार्ट असिस्टेंट।"

            text.contains("समय", true) ->
                "अभी का समय जानने के लिए अपने फोन की घड़ी देखें।"

            else ->
                "आपने पूछा: $text। अभी मेरी ऑनलाइन AI सेवा कनेक्ट नहीं है। उसे जोड़ने के बाद मैं बेहतर जवाब दे सकूँगी।"
        }

        messages = messages + "MYRA AI: $reply"
        onSpeak(reply)
        message = ""
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B1020))
                .padding(16.dp)
        ) {
            Text(
                "MYRA AI",
                color = Color(0xFF80D8FF),
                fontSize = 30.sp
            )

            Text(
                "Your Smart Assistant",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { item ->
                    Surface(
                        color = if (item.startsWith("आप:"))
                            Color(0xFF183A5A)
                        else
                            Color(0xFF202940),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            item,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box {
                            if (message.isEmpty()) {
                                Text("Message MYRA...", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    }
                )

                Button(onClick = {
                    if (SpeechRecognizer.isRecognitionAvailable(context)) {
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
                                "बोलिए..."
                            )
                        }
                        speechLauncher.launch(intent)
                    } else {
                        messages = messages + "MYRA AI: इस फोन पर speech recognition उपलब्ध नहीं है।"
                    }
                }) {
                    Text("🎙")
                }

                Button(onClick = { sendMessage() }) {
                    Text("Send")
                }
            }
        }
    }
}
