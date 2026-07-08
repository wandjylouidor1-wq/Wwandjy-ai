package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.ChatViewModel
import com.example.ui.CosmicBackground
import com.example.ui.CosmicSurface
import com.example.ui.CosmicSurfaceVariant
import com.example.ui.SlateTextSecondary
import com.example.ui.NeonCyan
import com.example.ui.NeonPink
import com.example.ui.NeonPurple
import com.example.ui.NeonTeal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun WandjyTalk(
    viewModel: ChatViewModel,
    glowColor: Color,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var callMode by remember { mutableStateOf("voice") } // "voice" or "video"
    var showChatPanel by remember { mutableStateOf(false) }

    // Language configuration
    var sourceLang by remember { mutableStateOf("English") }
    var targetLang by remember { mutableStateOf("French") }

    val languages = listOf(
        "English" to Locale.US,
        "Spanish" to Locale("es", "ES"),
        "French" to Locale.FRANCE,
        "German" to Locale.GERMANY,
        "Italian" to Locale.ITALY,
        "Japanese" to Locale.JAPAN,
        "Mandarin" to Locale.CHINA,
        "Portuguese" to Locale("pt", "PT")
    )

    fun getLocaleForName(name: String): Locale {
        return languages.firstOrNull { it.first == name }?.second ?: Locale.US
    }

    var isListening by remember { mutableStateOf(false) }
    var speechText by remember { mutableStateOf("") }
    var translatedSpeechText by remember { mutableStateOf("") }
    
    var aiReplyText by remember { mutableStateOf("") }
    var translatedAiReplyText by remember { mutableStateOf("") }
    
    var isThinkingByAI by remember { mutableStateOf(false) }
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    // In-Call Chat Messages
    var chatInput by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf<List<CallMessage>>(emptyList()) }

    // Initialize TTS with specific target language support
    DisposableEffect(targetLang) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        val selectedLocale = getLocaleForName(targetLang)
        ttsInstance.language = selectedLocale
        tts = ttsInstance

        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    // Speech Recognizer instance configured to source language
    var speechRecognizer: SpeechRecognizer? by remember { mutableStateOf(null) }
    val recognizerIntent = remember(sourceLang) {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, getLocaleForName(sourceLang).language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
    }

    fun startListening() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        isListening = false
                    }
                    override fun onError(error: Int) {
                        isListening = false
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val spokenText = matches[0]
                            speechText = spokenText
                            
                            // Send spoken text to Wandjy to reply and translate!
                            scope.launch {
                                isThinkingByAI = true
                                val response = viewModel.generateWithAI(
                                    prompt = "The caller says in $sourceLang: '$spokenText'. Perform two tasks separated by '||': First, write a conversational, short response in $targetLang. Second, translate their original phrase '$spokenText' to $targetLang.",
                                    systemInstruction = "You are Wandjy Call Translator. Keep answers extremely short and colloquial. Output exactly in format: Reply text || Translated input phrase."
                                )
                                isThinkingByAI = false
                                
                                val split = response.split("||")
                                if (split.size >= 2) {
                                    aiReplyText = split[0].trim()
                                    translatedSpeechText = split[1].trim()
                                } else {
                                    aiReplyText = response
                                    translatedSpeechText = "Translation processed."
                                }
                                
                                // Synthesize voice response in target language
                                if (isTtsReady && !aiReplyText.startsWith("Error:")) {
                                    isSpeaking = true
                                    tts?.speak(aiReplyText, TextToSpeech.QUEUE_FLUSH, null, "wandjy_voice")
                                }
                            }
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            speechText = matches[0]
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
        isListening = true
        tts?.stop()
        isSpeaking = false
        speechRecognizer?.startListening(recognizerIntent)
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening()
        }
    }

    fun checkPermissionAndStart() {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "RadarGlow")
    val orbitScale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "OrbitScale"
    )

    val particlePulse by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ParticlePulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground.copy(alpha = 0.98f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    tts?.stop()
                    speechRecognizer?.destroy()
                    onDismiss()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Call", tint = Color.White)
                }
                
                Text(
                    text = "Translating Live Link",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                // Call Mode toggle (Voice vs Video)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicSurface)
                        .padding(2.dp)
                ) {
                    IconButton(
                        onClick = { callMode = "voice" },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = if (callMode == "voice") glowColor else Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { callMode = "video" },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = if (callMode == "video") glowColor else Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Language Selector selectors
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Speak Language:", color = SlateTextSecondary, fontSize = 9.sp)
                        Text(sourceLang, color = glowColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable {
                            // Quick toggle lang list
                            val curIdx = languages.map { it.first }.indexOf(sourceLang)
                            val nextIdx = (curIdx + 1) % languages.size
                            sourceLang = languages[nextIdx].first
                        })
                    }

                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Their Translated Speak:", color = SlateTextSecondary, fontSize = 9.sp)
                        Text(targetLang, color = NeonTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable {
                            val curIdx = languages.map { it.first }.indexOf(targetLang)
                            val nextIdx = (curIdx + 1) % languages.size
                            targetLang = languages[nextIdx].first
                        })
                    }
                }
            }

            // Central Workspace (Either voice radar sphere or split video call)
            if (callMode == "voice") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radiusBase = size.minDimension / 4f
                        drawCircle(color = glowColor.copy(alpha = 0.08f), radius = radiusBase * 2.2f * orbitScale)
                        drawCircle(color = glowColor.copy(alpha = 0.12f), radius = radiusBase * 1.6f * particlePulse)
                        drawCircle(color = glowColor.copy(alpha = 0.18f), radius = radiusBase * 1.1f * orbitScale)
                    }

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(glowColor, glowColor.copy(alpha = 0.3f), Color.Transparent)))
                            .border(1.dp, glowColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.selectedVibe.value.emoji,
                            fontSize = 32.sp,
                            modifier = Modifier.scale(if (isSpeaking || isListening) particlePulse else 1f)
                        )
                    }
                }
            } else {
                // Split Video Call Simulation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // User Local Camera view box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.DarkGray)
                                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(28.dp))
                                Text("Your Video (Live)", color = Color.LightGray, fontSize = 9.sp)
                            }
                        }

                        // Wandjy remote AI camera view box with neon graphics
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0C0720))
                                .border(1.5.dp, glowColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Pulsing holographic AI Avatar
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(glowColor.copy(alpha = 0.2f))
                                        .border(1.dp, glowColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(viewModel.selectedVibe.value.emoji, fontSize = 20.sp, modifier = Modifier.scale(particlePulse))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Wandjy Hologram", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.Green))
                                    Text("TRANS_FEED_ONLINE", color = Color.Green, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    // Rolling subtitle banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Live Subtitles ($sourceLang -> $targetLang):",
                                color = NeonTeal,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (speechText.isNotBlank()) "\"$speechText\"" else "... Awaiting speech voice ...",
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (translatedSpeechText.isNotBlank()) {
                                Text(
                                    text = "Translation: \"$translatedSpeechText\"",
                                    color = glowColor,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Dual Translation transcription display (Voice Call view)
            if (callMode == "voice") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isListening) "Listening..." else if (isThinkingByAI) "Translating..." else if (isSpeaking) "Vocalizing translation..." else "Tap Mic to Speak",
                        color = glowColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (speechText.isNotBlank()) {
                        Text(
                            text = "Original ($sourceLang): \"$speechText\"",
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (translatedSpeechText.isNotBlank()) {
                        Text(
                            text = "Translated ($targetLang): \"$translatedSpeechText\"",
                            color = NeonTeal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (aiReplyText.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Wandjy Reply ($targetLang):", color = glowColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(aiReplyText, color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }

            // Inside-call text chat panel popup
            if (showChatPanel) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, NeonTeal, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Interactive Text Translator:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showChatPanel = false }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = NeonPink, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Messages list
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(chatMessages) { msg ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (msg.isUser) NeonTeal.copy(alpha = 0.2f) else CosmicSurfaceVariant)
                                            .padding(8.dp)
                                    ) {
                                        Column {
                                            Text(msg.text, color = Color.White, fontSize = 11.sp)
                                            Text("Trans: ${msg.translatedText}", color = glowColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Input field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Type translated text...", color = SlateTextSecondary, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontSize = 11.sp, color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = glowColor,
                                    unfocusedBorderColor = CosmicSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (chatInput.isBlank()) return@IconButton
                                    val text = chatInput
                                    chatInput = ""
                                    scope.launch {
                                        isThinkingByAI = true
                                        val response = viewModel.generateWithAI(
                                            prompt = "Translate this phrase from $sourceLang to $targetLang: '$text'",
                                            systemInstruction = "You are a highly efficient text translator. Translate the text exactly. Do not output anything else."
                                        )
                                        isThinkingByAI = false
                                        val newMessage = CallMessage(text = text, translatedText = response, isUser = true)
                                        chatMessages = chatMessages + newMessage
                                        
                                        // Simulate AI typing back a translated message
                                        delay(1500)
                                        val aiRawReply = "Bonjour mon ami! J'espère que vous passez une excellente journée. Wandjy est prêt."
                                        val aiTransReply = "Hello my friend! I hope you are having an excellent day. Wandjy is ready."
                                        chatMessages = chatMessages + CallMessage(text = aiRawReply, translatedText = aiTransReply, isUser = false)
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(glowColor)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Buttons control bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chat panel toggle button
                IconButton(
                    onClick = { showChatPanel = !showChatPanel },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CosmicSurface)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Text chat", tint = if (showChatPanel) NeonTeal else Color.White)
                }

                // Primary Mic trigger
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (isListening) Color.Red else glowColor)
                        .clickable {
                            if (isListening) {
                                stopListening()
                            } else {
                                checkPermissionAndStart()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Trigger Speech",
                        tint = if (isListening) Color.White else Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Re-play translation audio
                IconButton(
                    onClick = {
                        if (isSpeaking) {
                            tts?.stop()
                            isSpeaking = false
                        } else if (aiReplyText.isNotBlank() && isTtsReady) {
                            isSpeaking = true
                            tts?.speak(aiReplyText, TextToSpeech.QUEUE_FLUSH, null, "wandjy_voice")
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CosmicSurface)
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Vocalize reply",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

data class CallMessage(
    val text: String,
    val translatedText: String,
    val isUser: Boolean
)
