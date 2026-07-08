package com.example.ui.components

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.Creation
import com.example.ui.ChatViewModel
import com.example.ui.CosmicBackground
import com.example.ui.CosmicSurface
import com.example.ui.CosmicSurfaceVariant
import com.example.ui.NeonCyan
import com.example.ui.NeonPink
import com.example.ui.NeonPurple
import com.example.ui.NeonTeal
import com.example.ui.GlowGreen
import com.example.ui.SlateTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// SongGenre configuration model covering all styles in the world
data class SongGenre(
    val name: String,
    val category: String,
    val color: Color,
    val bpm: Int,
    val description: String
)

// Procedural Beat Synthesis Engine with real-time modulations
class WandjyAudioSynth {
    private var audioTrack: AudioTrack? = null
    @Volatile var isPlaying = false
    private var synthThread: Thread? = null

    // Live tuning variables modified on the fly by sliders
    @Volatile var bpmModifier: Float = 1.0f
    @Volatile var pitchModifier: Float = 1.0f
    @Volatile var reverbFactor: Float = 0.0f

    fun start(genre: String) {
        if (isPlaying) stop()
        isPlaying = true

        val baseBpm = when {
            genre.contains("Retro") || genre.contains("Cyber") || genre.contains("EDM") -> 124
            genre.contains("Techno") || genre.contains("Metal") || genre.contains("Drum") -> 138
            genre.contains("HipHop") || genre.contains("Lofi") || genre.contains("Rap") -> 82
            genre.contains("Jazz") || genre.contains("Ambient") || genre.contains("Classic") -> 68
            else -> 100
        }

        synthThread = Thread {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )
            audioTrack = track
            try {
                track.play()
            } catch (e: Exception) {
                return@Thread
            }

            val samples = ShortArray(bufferSize)
            var phase = 0.0
            var tick = 0

            // Arpeggio base notes
            val notes = when {
                genre.contains("Retro") || genre.contains("Cyber") -> doubleArrayOf(110.0, 130.8, 146.8, 164.8, 196.0, 220.0)
                genre.contains("Techno") || genre.contains("Metal") || genre.contains("Drum") -> doubleArrayOf(55.0, 65.4, 73.4, 82.4, 65.4, 55.0)
                genre.contains("HipHop") || genre.contains("Rap") -> doubleArrayOf(130.8, 164.8, 196.0, 246.9, 196.0, 164.8)
                else -> doubleArrayOf(261.6, 293.7, 329.6, 392.0, 329.6, 261.6) // Ambient, Jazz, Classical
            }

            // Simple delay/reverb buffer
            val echoDelay = (sampleRate * 0.25f).toInt() // 250ms echo
            val reverbBuffer = FloatArray(echoDelay)
            var reverbIndex = 0

            while (isPlaying) {
                val currentBpm = baseBpm * bpmModifier
                val samplesPerBeat = ((sampleRate * 60) / currentBpm).toInt().coerceAtLeast(100)
                val samplesPerStep = (samplesPerBeat / 4).coerceAtLeast(25)

                for (i in samples.indices) {
                    val currentStep = (tick / samplesPerStep) % 16

                    // Procedural kick
                    val kickTrigger = (tick % samplesPerBeat == 0)
                    var kickVal = 0.0
                    if (kickTrigger) {
                        val kickAge = (tick % samplesPerBeat).toDouble() / sampleRate
                        if (kickAge < 0.15) {
                            val kickFreq = 145.0 * Math.exp(-kickAge * 45.0)
                            kickVal = Math.sin(2.0 * Math.PI * kickFreq * kickAge) * (1.0 - kickAge / 0.15)
                        }
                    }

                    // Procedural hi-hat click
                    var hatVal = 0.0
                    val hatTrigger = (tick % (samplesPerBeat / 2) == samplesPerBeat / 4)
                    if (hatTrigger && !genre.contains("Ambient") && !genre.contains("Classical")) {
                        val hatAge = (tick % (samplesPerBeat / 2)).toDouble() / sampleRate
                        if (hatAge < 0.04) {
                            hatVal = (Math.random() * 2.0 - 1.0) * Math.exp(-hatAge * 140.0) * 0.12
                        }
                    }

                    // Synth Oscillator
                    val noteIndex = (currentStep % notes.size)
                    val targetFreq = notes[noteIndex] * pitchModifier

                    val noteAge = (tick % samplesPerStep).toDouble() / sampleRate
                    val synthEnvelope = Math.max(0.0, 1.0 - noteAge * 4.0)

                    val waveVal = if (genre.contains("Techno") || genre.contains("Metal")) {
                        if (Math.sin(phase) > 0) 1.0 else -1.0
                    } else if (genre.contains("HipHop") || genre.contains("Jazz")) {
                        // Triangle wave
                        (Math.abs((phase % (2.0 * Math.PI)) / Math.PI - 1.0) * 2.0 - 1.0)
                    } else {
                        // Pure sine
                        Math.sin(phase)
                    }

                    val synthVal = waveVal * synthEnvelope * 0.22

                    phase += (2.0 * Math.PI * targetFreq) / sampleRate
                    if (phase > 2.0 * Math.PI) {
                        phase -= 2.0 * Math.PI
                    }

                    // Mix and apply reverb delay
                    val drySample = (kickVal * 0.5 + hatVal * 0.25 + synthVal * 0.35)
                    val delayedSample = reverbBuffer[reverbIndex]
                    
                    val wetSample = drySample + delayedSample * reverbFactor
                    
                    // Write dry and wet combination back to the delay circular feedback buffer
                    reverbBuffer[reverbIndex] = (drySample + delayedSample * 0.4f).toFloat()
                    reverbIndex = (reverbIndex + 1) % echoDelay

                    val clipped = Math.max(-1.0, Math.min(1.0, wetSample))
                    samples[i] = (clipped * Short.MAX_VALUE).toInt().toShort()

                    tick++
                }
                if (isPlaying) {
                    try {
                        track.write(samples, 0, samples.size)
                    } catch (e: Exception) {
                        break
                    }
                }
            }
        }
        synthThread?.start()
    }

    fun stop() {
        isPlaying = false
        audioTrack?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {}
        }
        audioTrack = null
        synthThread = null
    }
}

// Complete globally cataloged list of song styles
val worldGenres = listOf(
    // Electronic
    SongGenre("Synthwave Retro", "Electronic", NeonPurple, 120, "80s retro synth nostalgic dreamscapes"),
    SongGenre("Acid Techno", "Electronic", NeonTeal, 138, "Heavy acid lines & high energy hypnotism"),
    SongGenre("EDM / Progressive", "Electronic", NeonCyan, 128, "Massive festival drops & uplifts"),
    SongGenre("Drum & Bass", "Electronic", NeonPink, 174, "Lightning breakbeats & deep bass pulses"),
    SongGenre("Dubstep / Riddim", "Electronic", GlowGreen, 140, "Heavy metallic robotic wobble chops"),
    SongGenre("Deep House", "Electronic", NeonCyan, 122, "Warm electric piano & deep club kicks"),
    
    // Hip Hop / Urban
    SongGenre("Lo-Fi HipHop", "Hip-Hop", NeonTeal, 80, "Cozy tape hiss, crackle beats & chill keys"),
    SongGenre("Boom Bap Rap", "Hip-Hop", NeonPurple, 90, "Vintage dusty samples & MPC drum grooves"),
    SongGenre("Drill / Trap", "Hip-Hop", NeonPink, 142, "Fast hi-hat rolls & heavy slide 808s"),
    SongGenre("R&B / Soul", "Hip-Hop", NeonCyan, 95, "Smooth sensual chords & urban vocal grooves"),
    SongGenre("Afrobeats", "Hip-Hop", GlowGreen, 112, "West African grooves & rhythmic syncopation"),
    SongGenre("Dancehall Reggae", "Hip-Hop", GlowGreen, 98, "Seductive riddims, roots offbeats & island bass"),

    // Pop / Modern
    SongGenre("Pop Anthem", "Pop", NeonPink, 118, "Bright commercial hooks, guitars & catchy beats"),
    SongGenre("K-Pop", "Pop", NeonPurple, 124, "Blazing futuristic beats & high energy shifts"),
    SongGenre("Latin Reggaeton", "Pop", NeonTeal, 95, "Dembow beats, tropical synths & urban club grooves"),
    SongGenre("Electro-Pop", "Pop", NeonCyan, 115, "Glossy vocal synths & dancey electronic drums"),

    // Rock / Metal
    SongGenre("Acoustic Indie", "Rock", NeonPink, 84, "Melancholic acoustic picking & warm chords"),
    SongGenre("Classic Rock", "Rock", NeonPurple, 120, "Overdriven vintage guitars & organic drumming"),
    SongGenre("Heavy Metal / Djent", "Rock", NeonTeal, 140, "Extremely heavy chugging riffs & double-kick blasts"),
    SongGenre("Punk / Grunge", "Rock", NeonPink, 145, "Raw rebellious power chords & thrashing beats"),

    // traditional / ambient
    SongGenre("Jazz / Blues", "Traditional", NeonCyan, 78, "Soulful swinging saxes & warm brass chords"),
    SongGenre("Classical Orchestra", "Traditional", Color.White, 72, "Epic orchestral strings, brass & grand piano"),
    SongGenre("Folk / Country", "Traditional", NeonPink, 92, "Plucky acoustic banjos & slide resonator guitar"),
    SongGenre("Ambient Zen", "Traditional", Color.White, 60, "Binaural slow pads & meditation crystal bells"),
    SongGenre("Cinematic Epic", "Traditional", NeonPurple, 105, "Dramatic brass & high stakes orchestral trailers")
)

@Volatile
var activeSynthInstance: WandjyAudioSynth? = null

@Composable
fun WandjyBeats(
    viewModel: ChatViewModel,
    glowColor: Color
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val creations by viewModel.allCreations.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Creator, 1: Now Playing, 2: Library
    var activeCategory by remember { mutableStateOf("Electronic") } // Genre sorting

    // Create parameters
    var inputMode by remember { mutableStateOf("Write") }
    var musicPrompt by remember { mutableStateOf("") }
    var customGenreInput by remember { mutableStateOf("") }
    var selectedGenreModel by remember { mutableStateOf(worldGenres.first()) }
    var isGeneratingSong by remember { mutableStateOf(false) }

    // Audio synthesizer reference (Persistent across composable lifecycle via safe static pointer)
    val synth = remember {
        val active = activeSynthInstance ?: WandjyAudioSynth().also { activeSynthInstance = it }
        active
    }

    var activePlayingCreation by remember { mutableStateOf<Creation?>(null) }
    var isSynthPlaying by remember { mutableStateOf(synth.isPlaying) }

    // Live controller variables mapped to synth
    var liveBpmModifier by remember { mutableFloatStateOf(synth.bpmModifier) }
    var livePitchModifier by remember { mutableFloatStateOf(synth.pitchModifier) }
    var liveReverbFactor by remember { mutableFloatStateOf(synth.reverbFactor) }

    // Voice simulation
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceTimer by remember { mutableIntStateOf(0) }

    // Sync state periodically
    LaunchedEffect(synth.isPlaying) {
        while (true) {
            isSynthPlaying = synth.isPlaying
            delay(500)
        }
    }

    // Dynamic timer for voice transcription
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            voiceTimer = 0
            while (isRecordingVoice && voiceTimer < 5) {
                delay(1000)
                voiceTimer++
            }
            if (isRecordingVoice) {
                isRecordingVoice = false
                musicPrompt = "A beautiful afrobeats track mixed with emotional classical strings and electronic drops"
                Toast.makeText(context, "Voice transcribed: '$musicPrompt'", Toast.LENGTH_LONG).show()
            }
        }
    }

    val categories = listOf("Electronic", "Hip-Hop", "Pop", "Rock", "Traditional")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground)
    ) {
        // Applet sub tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = CosmicSurface,
            contentColor = glowColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("AI Creator", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AudioFile, contentDescription = "Creator") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Now Playing", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.MusicNote, contentDescription = "Player") }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Music Library", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (activeTab) {
            0 -> {
                // Creator Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "AI Vocal & Music Composer",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "Describe your song vision or transcribe lyrics via voice. Wandjy AI will synthesize 4 distinct mixes with structured arrangements and high-fidelity beats!",
                                    color = SlateTextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                TabRow(
                                    selectedTabIndex = if (inputMode == "Write") 0 else 1,
                                    containerColor = CosmicSurfaceVariant,
                                    contentColor = glowColor,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Tab(
                                        selected = inputMode == "Write",
                                        onClick = { inputMode = "Write" },
                                        text = { Text("Write Prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    )
                                    Tab(
                                        selected = inputMode == "Voice",
                                        onClick = { inputMode = "Voice" },
                                        text = { Text("Voice Input", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    )
                                }

                                if (inputMode == "Write") {
                                    OutlinedTextField(
                                        value = musicPrompt,
                                        onValueChange = { musicPrompt = it },
                                        placeholder = { Text("Describe theme, mood, or lyrics (e.g., A cybernetic highway under a synthwave sunset with neon rain)...", color = SlateTextSecondary, fontSize = 12.sp) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .testTag("beats_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = glowColor,
                                            unfocusedBorderColor = CosmicSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CosmicSurfaceVariant)
                                            .border(1.dp, glowColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                            .clickable { isRecordingVoice = !isRecordingVoice },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isRecordingVoice) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Recording Voice... 0:0$voiceTimer", color = NeonPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                WandjyRhythmBar(glowColor = glowColor)
                                            }
                                        } else {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.Mic, contentDescription = "Mic", tint = glowColor, modifier = Modifier.size(24.dp))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = if (musicPrompt.isBlank()) "Tap to Record Lyrics or Theme" else "Transcribed: '$musicPrompt'",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Global Genre Explorer Section
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Browse Songs & Beats Genres of the World:",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Horizontal Category filter chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(categories) { cat ->
                                    val isSelected = activeCategory == cat
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { activeCategory = cat },
                                        label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = glowColor.copy(alpha = 0.2f),
                                            selectedLabelColor = glowColor,
                                            containerColor = CosmicSurface,
                                            labelColor = Color.White
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            selectedBorderColor = glowColor,
                                            borderColor = CosmicSurfaceVariant
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Grid list of genres under active Category
                            val filteredGenres = worldGenres.filter { it.category == activeCategory }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    filteredGenres.forEach { genre ->
                                        val isSelected = selectedGenreModel.name == genre.name && customGenreInput.isEmpty()
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) genre.color.copy(alpha = 0.15f) else Color.Transparent)
                                                .border(1.dp, if (isSelected) genre.color else Color.Transparent, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    selectedGenreModel = genre
                                                    customGenreInput = ""
                                                }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(genre.color)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(genre.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(genre.description, color = SlateTextSecondary, fontSize = 10.sp)
                                            }
                                            Text("${genre.bpm} BPM", color = genre.color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Custom genre text input
                    item {
                        OutlinedTextField(
                            value = customGenreInput,
                            onValueChange = { customGenreInput = it },
                            placeholder = { Text("Or enter custom type (e.g. Flamenco, Synth-Metal, Drill-Jazz)...", color = SlateTextSecondary, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 12.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = glowColor,
                                unfocusedBorderColor = CosmicSurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Create Action
                    item {
                        Button(
                            onClick = {
                                if (musicPrompt.isBlank()) {
                                    Toast.makeText(context, "Please describe the song vibe first", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val genreToUse = if (customGenreInput.isNotBlank()) customGenreInput else selectedGenreModel.name
                                scope.launch {
                                    isGeneratingSong = true
                                    val sysInstruction = """
                                        You are Wandjy AI, a premier songwriting wizard.
                                        Compose structured, beautiful lyrics and separate EXACTLY 4 song variations (Anthology mixes) using '---' as delimiter.
                                        
                                        Format precisely like this:
                                        ---
                                        MIX_1: Celestial Radio Cut
                                        Style: $genreToUse
                                        Lyrics:
                                        [Intro]
                                        (Upbeat synths rising)
                                        [Verse 1]
                                        Riding on a ray of light, in the synthetic neon night...
                                        [Chorus]
                                        ---
                                        MIX_2: Heavy Cyber Bass Re-Mix
                                        Style: Acid Techno
                                        Lyrics:
                                        [Beat Drop]
                                        ---
                                        MIX_3: Sunset Nostalgia Groove
                                        Style: Lo-Fi HipHop
                                        Lyrics:
                                        [Verse]
                                        ---
                                        MIX_4: Cinematic Acoustic Lounge
                                        Style: Ambient Zen
                                        Lyrics:
                                        [Outro]
                                    """.trimIndent()

                                    val response = viewModel.generateWithAI(
                                        prompt = "Compose 4 dynamic song mixes about: $musicPrompt",
                                        systemInstruction = sysInstruction
                                    )
                                    isGeneratingSong = false

                                    if (!response.startsWith("Error:") && response.contains("MIX_")) {
                                        val segments = response.split("---").map { it.trim() }.filter { it.isNotBlank() }
                                        var lastCreatedSong: Creation? = null
                                        segments.forEach { seg ->
                                            val lines = seg.split("\n")
                                            val titleLine = lines.firstOrNull { it.contains("MIX_") } ?: "Mix Cut"
                                            val styleLine = lines.firstOrNull { it.contains("Style:") } ?: "Style: $genreToUse"

                                            val finalTitle = titleLine.replace("MIX_1:", "").replace("MIX_2:", "").replace("MIX_3:", "").replace("MIX_4:", "").trim()
                                            val finalStyle = styleLine.replace("Style:", "").trim()
                                            val finalLyrics = lines.filter { !it.contains("MIX_") && !it.contains("Style:") && !it.contains("Lyrics:") }.joinToString("\n").trim()

                                            if (finalLyrics.isNotBlank()) {
                                                val c = Creation(
                                                    type = "song",
                                                    prompt = musicPrompt,
                                                    title = finalTitle,
                                                    style = finalStyle,
                                                    lyrics = finalLyrics
                                                )
                                                val savedId = viewModel.saveCreation(c)
                                                lastCreatedSong = c.copy(id = savedId)
                                            }
                                        }
                                        if (lastCreatedSong != null) {
                                            activePlayingCreation = lastCreatedSong
                                            synth.start(lastCreatedSong!!.style)
                                            isSynthPlaying = true
                                            musicPrompt = ""
                                            // Real-time navigation to listening player page!
                                            activeTab = 1
                                            Toast.makeText(context, "Generated 4 Songs! Navigated to player.", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        // Standard fallback
                                        val singleCreation = Creation(
                                            type = "song",
                                            prompt = musicPrompt,
                                            title = "$genreToUse Synthesis",
                                            style = genreToUse,
                                            lyrics = response
                                        )
                                        val savedId = viewModel.saveCreation(singleCreation)
                                        activePlayingCreation = singleCreation.copy(id = savedId)
                                        synth.start(genreToUse)
                                        isSynthPlaying = true
                                        musicPrompt = ""
                                        activeTab = 1
                                        Toast.makeText(context, "Song generated successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = glowColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("generate_song_button")
                        ) {
                            if (isGeneratingSong) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesizing 4 Vocal Mixes...", color = Color.Black, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Gen", tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate 4 Dynamic Songs", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }

            1 -> {
                // Now Playing Tab (Full screen tactile record player)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    activePlayingCreation?.let { currentTrack ->
                        val themeColor = when {
                            currentTrack.style.contains("Retro") || currentTrack.style.contains("Cyber") -> NeonPurple
                            currentTrack.style.contains("Techno") || currentTrack.style.contains("Metal") -> NeonTeal
                            currentTrack.style.contains("HipHop") -> NeonCyan
                            else -> NeonPink
                        }

                        // Title metadata header
                        Text(
                            text = "Now Playing & Synthesizing",
                            color = themeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = currentTrack.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Vibe: ${currentTrack.style}",
                            color = SlateTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Spinning record player Canvas
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape)
                                .border(2.dp, themeColor.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // Infinite rotation animation representing physical record spin
                            val infiniteTransition = rememberInfiniteTransition(label = "VinylSpin")
                            val spinAngle by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(if (isSynthPlaying) 3200 else 1000000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "SpinAngle"
                            )

                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(spinAngle)
                            ) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                // 1. Main outer black vinyl body
                                drawCircle(color = Color(0xFF14131A), radius = size.width / 2f)

                                // 2. Concentric audio groove rings
                                repeat(5) { rIndex ->
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.08f),
                                        radius = (size.width / 2f) * (0.4f + rIndex * 0.12f),
                                        style = Stroke(width = 1.5f)
                                    )
                                }

                                // 3. Central glowing album art sticker
                                drawCircle(color = themeColor, radius = size.width * 0.18f)
                                drawCircle(color = Color.Black, radius = size.width * 0.14f)

                                // Spindle hole
                                drawCircle(color = Color(0xFF0F0F16), radius = 12f)
                            }

                            // Dynamic pulse aura
                            if (isSynthPlaying) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(2.dp, themeColor.copy(alpha = 0.15f), CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Audio Deck Control buttons (Tactile row)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Stop synth
                            IconButton(
                                onClick = {
                                    synth.stop()
                                    isSynthPlaying = false
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(CosmicSurface, CircleShape)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                            }

                            // Play Pause Toggle
                            IconButton(
                                onClick = {
                                    if (isSynthPlaying) {
                                        synth.stop()
                                        isSynthPlaying = false
                                    } else {
                                        synth.start(currentTrack.style)
                                        // Restore live settings
                                        synth.bpmModifier = liveBpmModifier
                                        synth.pitchModifier = livePitchModifier
                                        synth.reverbFactor = liveReverbFactor
                                        isSynthPlaying = true
                                    }
                                },
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(themeColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isSynthPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "PlayPause",
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Next track from creations library
                            IconButton(
                                onClick = {
                                    val songs = creations.filter { it.type == "song" }
                                    val idx = songs.indexOfFirst { it.id == currentTrack.id }
                                    if (songs.isNotEmpty() && idx != -1) {
                                        val nextIdx = (idx + 1) % songs.size
                                        val nextSong = songs[nextIdx]
                                        activePlayingCreation = nextSong
                                        synth.stop()
                                        synth.start(nextSong.style)
                                        // restore mods
                                        synth.bpmModifier = liveBpmModifier
                                        synth.pitchModifier = livePitchModifier
                                        synth.reverbFactor = liveReverbFactor
                                        isSynthPlaying = true
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(CosmicSurface, CircleShape)
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live synthesis fine tuners / Equalizer Pro
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Real-Time Synthesis Synthesizer Pro:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Live BPM Adjuster
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("BPM/Speed:", color = Color.White, fontSize = 10.sp, modifier = Modifier.width(75.dp))
                                    Slider(
                                        value = liveBpmModifier,
                                        onValueChange = {
                                            liveBpmModifier = it
                                            synth.bpmModifier = it
                                        },
                                        valueRange = 0.5f..2.0f,
                                        colors = SliderDefaults.colors(thumbColor = themeColor, activeTrackColor = themeColor),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("${(liveBpmModifier * 100).toInt()}%", color = SlateTextSecondary, fontSize = 10.sp, modifier = Modifier.width(35.dp), textAlign = TextAlign.End)
                                }

                                // Live Pitch Adjuster
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Vocal Key:", color = Color.White, fontSize = 10.sp, modifier = Modifier.width(75.dp))
                                    Slider(
                                        value = livePitchModifier,
                                        onValueChange = {
                                            livePitchModifier = it
                                            synth.pitchModifier = it
                                        },
                                        valueRange = 0.4f..1.8f,
                                        colors = SliderDefaults.colors(thumbColor = themeColor, activeTrackColor = themeColor),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("x${String.format("%.1f", livePitchModifier)}", color = SlateTextSecondary, fontSize = 10.sp, modifier = Modifier.width(35.dp), textAlign = TextAlign.End)
                                }

                                // Echo / Reverb Factor Adjuster
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Echo Delay:", color = Color.White, fontSize = 10.sp, modifier = Modifier.width(75.dp))
                                    Slider(
                                        value = liveReverbFactor,
                                        onValueChange = {
                                            liveReverbFactor = it
                                            synth.reverbFactor = it
                                        },
                                        valueRange = 0.0f..0.85f,
                                        colors = SliderDefaults.colors(thumbColor = themeColor, activeTrackColor = themeColor),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("${(liveReverbFactor * 100).toInt()}%", color = SlateTextSecondary, fontSize = 10.sp, modifier = Modifier.width(35.dp), textAlign = TextAlign.End)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // scrolling structured lyrics karaoke console
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CosmicSurface)
                                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Synthesized Lyrics & Rhythm:", color = themeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    WandjyRhythmBar(glowColor = themeColor)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(modifier = Modifier.fillMaxSize()) {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        item {
                                            Text(
                                                text = currentTrack.lyrics,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                lineHeight = 18.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } ?: run {
                        // Empty player fallback state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.MusicNote, contentDescription = "None", tint = SlateTextSecondary, modifier = Modifier.size(54.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No track active in player", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Go to Library or Creator to play a song", color = SlateTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            2 -> {
                // Library & Playlist Tab
                val librarySongs = creations.filter { it.type == "song" }
                if (librarySongs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LibraryMusic, contentDescription = "Empty", tint = SlateTextSecondary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Your synthesized music library is empty", color = Color.White, fontSize = 13.sp)
                            Text("Go create some dynamic songs!", color = SlateTextSecondary, fontSize = 11.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(librarySongs) { song ->
                            val isPlayingThis = activePlayingCreation?.id == song.id && isSynthPlaying
                            val themeColor = when {
                                song.style.contains("Retro") || song.style.contains("Cyber") -> NeonPurple
                                song.style.contains("Techno") || song.style.contains("Metal") -> NeonTeal
                                song.style.contains("HipHop") -> NeonCyan
                                else -> NeonPink
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = if (isPlayingThis) themeColor else CosmicSurfaceVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        activePlayingCreation = song
                                        synth.stop()
                                        synth.start(song.style)
                                        synth.bpmModifier = liveBpmModifier
                                        synth.pitchModifier = livePitchModifier
                                        synth.reverbFactor = liveReverbFactor
                                        isSynthPlaying = true
                                        // Auto-redirect to player
                                        activeTab = 1
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(themeColor.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isPlayingThis) Icons.Default.VolumeUp else Icons.Default.MusicNote,
                                                contentDescription = "Song",
                                                tint = themeColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = song.title,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Vibe: ${song.style} • Prompt: ${song.prompt}",
                                                color = SlateTextSecondary,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = {
                                            if (isPlayingThis) {
                                                synth.stop()
                                                isSynthPlaying = false
                                            }
                                            viewModel.deleteCreation(song.id)
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonPink, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WandjyRhythmBar(glowColor: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { i ->
            val infiniteTransition = rememberInfiniteTransition(label = "RhythmBarAnim")
            val heightMultiplier by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(300 + i * 120, easing = FastOutLinearInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "RhythmBarHeightVal"
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(14.dp * heightMultiplier)
                    .clip(CircleShape)
                    .background(glowColor)
            )
        }
    }
}
