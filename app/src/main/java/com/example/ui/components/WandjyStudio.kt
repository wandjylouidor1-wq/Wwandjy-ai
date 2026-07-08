package com.example.ui.components

import android.graphics.Paint
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
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
import kotlin.math.sin
import kotlin.random.Random

// Serialized Configuration Models for local updates
data class PhotoEditConfig(
    val brightness: Float = 0f,      // -0.5f to 0.5f
    val tintShift: Float = 0f,       // 0f to 360f
    val borderStyle: String = "Classic Thin",
    val sticker: String = "None",
    val signature: String = ""
) {
    fun serialize(): String {
        return "PHOTO_CFG:bright=$brightness;tint=$tintShift;border=$borderStyle;sticker=$sticker;sig=$signature"
    }

    companion object {
        fun deserialize(encoded: String): PhotoEditConfig {
            if (!encoded.startsWith("PHOTO_CFG:")) return PhotoEditConfig(signature = encoded)
            var bright = 0f
            var tint = 0f
            var border = "Classic Thin"
            var stickerVal = "None"
            var sig = ""
            val clean = encoded.removePrefix("PHOTO_CFG:")
            val parts = clean.split(";")
            parts.forEach { part ->
                val kv = part.split("=")
                if (kv.size == 2) {
                    val k = kv[0]
                    val v = kv[1]
                    when (k) {
                        "bright" -> bright = v.toFloatOrNull() ?: 0f
                        "tint" -> tint = v.toFloatOrNull() ?: 0f
                        "border" -> border = v
                        "sticker" -> stickerVal = v
                        "sig" -> sig = v
                    }
                }
            }
            return PhotoEditConfig(bright, tint, border, stickerVal, sig)
        }
    }
}

data class VideoEditConfig(
    val speed: Float = 1.0f,
    val density: Float = 0.6f,
    val size: Float = 6.0f,
    val waveStyle: String = "Sinusoidal Wave",
    val colorTheme: String = "Teal Pulse",
    val glitchEffect: Boolean = false
) {
    fun serialize(): String {
        return "VIDEO_CFG:speed=$speed;density=$density;size=$size;wave=$waveStyle;color=$colorTheme;glitch=$glitchEffect"
    }

    companion object {
        fun deserialize(encoded: String): VideoEditConfig {
            if (!encoded.startsWith("VIDEO_CFG:")) return VideoEditConfig()
            var speedVal = 1.0f
            var densityVal = 0.6f
            var sizeVal = 6.0f
            var wave = "Sinusoidal Wave"
            var color = "Teal Pulse"
            var glitch = false
            val clean = encoded.removePrefix("VIDEO_CFG:")
            val parts = clean.split(";")
            parts.forEach { part ->
                val kv = part.split("=")
                if (kv.size == 2) {
                    val k = kv[0]
                    val v = kv[1]
                    when (k) {
                        "speed" -> speedVal = v.toFloatOrNull() ?: 1.0f
                        "density" -> densityVal = v.toFloatOrNull() ?: 0.6f
                        "size" -> sizeVal = v.toFloatOrNull() ?: 6.0f
                        "wave" -> wave = v
                        "color" -> color = v
                        "glitch" -> glitch = v.toBoolean()
                    }
                }
            }
            return VideoEditConfig(speedVal, densityVal, sizeVal, wave, color, glitch)
        }
    }
}

@Composable
fun WandjyStudio(
    viewModel: ChatViewModel,
    glowColor: Color
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val creations by viewModel.allCreations.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("photo") } // "photo" or "video"
    var promptInput by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    // Selected creation for display & active editing
    var currentlySelectedCreation by remember { mutableStateOf<Creation?>(null) }
    var showEditorPanel by remember { mutableStateOf(false) }

    // Working Photo edit states
    var editPhotoBrightness by remember { mutableFloatStateOf(0f) }
    var editPhotoTintShift by remember { mutableFloatStateOf(0f) }
    var editPhotoBorder by remember { mutableStateOf("Classic Thin") }
    var editPhotoSticker by remember { mutableStateOf("None") }
    var editPhotoSig by remember { mutableStateOf("") }

    // Working Video edit states
    var editVideoSpeed by remember { mutableFloatStateOf(1.0f) }
    var editVideoDensity by remember { mutableFloatStateOf(0.6f) }
    var editVideoSize by remember { mutableFloatStateOf(6.0f) }
    var editVideoWaveStyle by remember { mutableStateOf("Sinusoidal Wave") }
    var editVideoColorTheme by remember { mutableStateOf("Teal Pulse") }
    var editVideoGlitch by remember { mutableStateOf(false) }

    // Sync editing panel states when selected creation changes
    LaunchedEffect(currentlySelectedCreation) {
        currentlySelectedCreation?.let { creation ->
            if (creation.type == "photo") {
                val cfg = PhotoEditConfig.deserialize(creation.lyrics)
                editPhotoBrightness = cfg.brightness
                editPhotoTintShift = cfg.tintShift
                editPhotoBorder = cfg.borderStyle
                editPhotoSticker = cfg.sticker
                editPhotoSig = cfg.signature
            } else {
                val cfg = VideoEditConfig.deserialize(creation.lyrics)
                editVideoSpeed = cfg.speed
                editVideoDensity = cfg.density
                editVideoSize = cfg.size
                editVideoWaveStyle = cfg.waveStyle
                editVideoColorTheme = cfg.colorTheme
                editVideoGlitch = cfg.glitchEffect
            }
        }
    }

    val photoBorders = listOf("Classic Thin", "Cyberpunk Neon", "Retro Filmstrip", "Cosmic Hologram")
    val photoStickers = listOf("None", "Cyberpunk Grid", "Analog CRT Scanlines", "Matrix Rain Overlay", "Vaporwave Glitch Lines")

    val videoWaves = listOf("Sinusoidal Wave", "Square/Pulse Wave", "Sawtooth Glitch Wave")
    val videoThemes = listOf("Teal Pulse", "Royal Violet", "Emerald Glitch", "Neon Pink Plasma", "Golden Sun", "Chroma Rainbow")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground)
            .padding(16.dp)
    ) {
        // Selection Switcher: Photo Studio vs Video Studio
        TabRow(
            selectedTabIndex = if (activeTab == "photo") 0 else 1,
            containerColor = CosmicSurface,
            contentColor = glowColor,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTab == "photo",
                onClick = {
                    activeTab = "photo"
                    currentlySelectedCreation = null
                    showEditorPanel = false
                },
                text = { Text("Photo Studio", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.PhotoCamera, contentDescription = "Photo") }
            )
            Tab(
                selected = activeTab == "video",
                onClick = {
                    activeTab = "video"
                    currentlySelectedCreation = null
                    showEditorPanel = false
                },
                text = { Text("Video Studio", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.Videocam, contentDescription = "Video") }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Left-right layout for wider screens or sequential stack for compact
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                // Prompter suite
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (activeTab == "photo") "AI Photo Composition Engine" else "AI Cinematic Video Synth",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = {
                                Text(
                                    text = if (activeTab == "photo") "Describe the photo masterpiece to compose..." else "Describe the looping motion graphic scene to render...",
                                    color = SlateTextSecondary,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(75.dp)
                                .testTag("studio_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = glowColor,
                                unfocusedBorderColor = CosmicSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Compile/Generate Button
                        Button(
                            onClick = {
                                if (promptInput.isBlank()) {
                                    Toast.makeText(context, "Please describe your vision first", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                scope.launch {
                                    isGenerating = true
                                    delay(1800)
                                    isGenerating = false

                                    val title = if (promptInput.length > 20) promptInput.take(18) + "..." else promptInput
                                    val serializedConfig = if (activeTab == "photo") {
                                        PhotoEditConfig(borderStyle = "Classic Thin", sticker = "None", signature = "").serialize()
                                    } else {
                                        VideoEditConfig(waveStyle = "Sinusoidal Wave", colorTheme = "Teal Pulse").serialize()
                                    }

                                    val creation = Creation(
                                        type = activeTab,
                                        prompt = promptInput,
                                        title = "$title Masterpiece",
                                        style = if (activeTab == "photo") "Cyberpunk Violet" else "Teal Pulse",
                                        lyrics = serializedConfig
                                    )
                                    val insertedId = viewModel.saveCreation(creation)
                                    currentlySelectedCreation = creation.copy(id = insertedId)
                                    showEditorPanel = true
                                    promptInput = ""
                                    Toast.makeText(context, "AI Studio Render Complete!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = glowColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("render_button")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Rendering Algorithmic Grid...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            } else {
                                Icon(if (activeTab == "photo") Icons.Default.FilterFrames else Icons.Default.MovieFilter, contentDescription = "Run", tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (activeTab == "photo") "Compose AI Photo" else "Synthesize AI Video Loop", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = CosmicSurfaceVariant, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Or Upload / Share Custom Media:",
                            color = SlateTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        val photoPickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri ->
                            uri?.let {
                                scope.launch {
                                    val creation = Creation(
                                        type = "photo",
                                        prompt = "Custom Uploaded Photo",
                                        title = "Uploaded Photo " + (System.currentTimeMillis() % 10000),
                                        style = "Uploaded",
                                        lyrics = "UPLOAD_URI:$it"
                                    )
                                    val insertedId = viewModel.saveCreation(creation)
                                    currentlySelectedCreation = creation.copy(id = insertedId)
                                    showEditorPanel = true
                                    Toast.makeText(context, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        val videoPickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri ->
                            uri?.let {
                                scope.launch {
                                    val creation = Creation(
                                        type = "video",
                                        prompt = "Custom Uploaded Video",
                                        title = "Uploaded Video " + (System.currentTimeMillis() % 10000),
                                        style = "Uploaded",
                                        lyrics = "UPLOAD_URI:$it"
                                    )
                                    val insertedId = viewModel.saveCreation(creation)
                                    currentlySelectedCreation = creation.copy(id = insertedId)
                                    showEditorPanel = true
                                    Toast.makeText(context, "Video uploaded successfully!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    if (activeTab == "photo") {
                                        photoPickerLauncher.launch("image/*")
                                    } else {
                                        videoPickerLauncher.launch("video/*")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("upload_media_button")
                            ) {
                                Icon(
                                    imageVector = if (activeTab == "photo") Icons.Default.CloudUpload else Icons.Default.VideoCall,
                                    contentDescription = "Upload",
                                    tint = glowColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeTab == "photo") "Upload Photo" else "Upload Video",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        val presetUri = if (activeTab == "photo") {
                                            val presets = listOf("preset_cyber", "preset_nebula", "preset_ocean")
                                            presets.random()
                                        } else {
                                            val presets = listOf("preset_video_portal", "preset_video_heartbeat")
                                            presets.random()
                                        }
                                        val presetTitle = if (presetUri.contains("cyber")) "cyber_city_neon.jpg"
                                            else if (presetUri.contains("nebula")) "deep_space_nebula.png"
                                            else if (presetUri.contains("ocean")) "synthwave_sunset_ocean.png"
                                            else if (presetUri.contains("portal")) "hyperjump_warp_tunnel.mp4"
                                            else "algorithmic_heartbeat.mp4"

                                        val creation = Creation(
                                            type = activeTab,
                                            prompt = "Simulated high-fidelity media upload",
                                            title = presetTitle,
                                            style = "Uploaded Preset",
                                            lyrics = "UPLOAD_URI:$presetUri"
                                        )
                                        val insertedId = viewModel.saveCreation(creation)
                                        currentlySelectedCreation = creation.copy(id = insertedId)
                                        showEditorPanel = true
                                        Toast.makeText(context, "Preset visual loaded into Studio!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, glowColor.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(38.dp)
                                    .testTag("upload_preset_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Preset",
                                    tint = NeonPink,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeTab == "photo") "Quick Photo Preset" else "Quick Video Preset",
                                    color = SlateTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Display Zone (If a creation is chosen)
            currentlySelectedCreation?.let { currentCreation ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, glowColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                            ) {
                                if (currentCreation.type == "photo") {
                                    WandjyPhotoRenderer(
                                        creation = currentCreation,
                                        brightness = editPhotoBrightness,
                                        tintShift = editPhotoTintShift,
                                        borderStyle = editPhotoBorder,
                                        sticker = editPhotoSticker,
                                        signature = editPhotoSig
                                    )
                                } else {
                                    WandjyVideoRenderer(
                                        creation = currentCreation,
                                        speedMultiplier = editVideoSpeed,
                                        density = editVideoDensity,
                                        particleSize = editVideoSize,
                                        waveStyle = editVideoWaveStyle,
                                        colorTheme = editVideoColorTheme,
                                        glitchEffect = editVideoGlitch
                                    )
                                }

                                // Header overlay
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(currentCreation.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Double-tap or slide below to edit in real-time", color = SlateTextSecondary, fontSize = 10.sp)
                                    }
                                    Row {
                                        IconButton(
                                            onClick = { showEditorPanel = !showEditorPanel },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(CosmicSurfaceVariant, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = if (showEditorPanel) Icons.Default.EditOff else Icons.Default.Edit,
                                                contentDescription = "Edit Toggle",
                                                tint = glowColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { currentlySelectedCreation = null; showEditorPanel = false },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            // Interactive Editor Console Panel
                            AnimatedVisibility(
                                visible = showEditorPanel,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CosmicSurfaceVariant)
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = "Wandjy Real-Time Editor Pro",
                                        color = glowColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    if (currentCreation.type == "photo") {
                                        // Brightness/Exposure
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Exposure:", color = Color.White, fontSize = 11.sp, modifier = Modifier.width(70.dp))
                                            Slider(
                                                value = editPhotoBrightness,
                                                onValueChange = { editPhotoBrightness = it },
                                                valueRange = -0.5f..0.5f,
                                                colors = SliderDefaults.colors(thumbColor = glowColor, activeTrackColor = glowColor),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("${(editPhotoBrightness * 100).toInt()}%", color = SlateTextSecondary, fontSize = 11.sp, modifier = Modifier.width(35.dp), textAlign = TextAlign.End)
                                        }

                                        // Tint Hue Shift
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Hue Shift:", color = Color.White, fontSize = 11.sp, modifier = Modifier.width(70.dp))
                                            Slider(
                                                value = editPhotoTintShift,
                                                onValueChange = { editPhotoTintShift = it },
                                                valueRange = 0f..360f,
                                                colors = SliderDefaults.colors(thumbColor = glowColor, activeTrackColor = glowColor),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("${editPhotoTintShift.toInt()}°", color = SlateTextSecondary, fontSize = 11.sp, modifier = Modifier.width(35.dp), textAlign = TextAlign.End)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Border style Selector
                                        Text("Border Style Frame:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(photoBorders) { border ->
                                                val sel = editPhotoBorder == border
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (sel) glowColor.copy(alpha = 0.2f) else CosmicSurface)
                                                        .border(1.dp, if (sel) glowColor else Color.Transparent, RoundedCornerShape(6.dp))
                                                        .clickable { editPhotoBorder = border }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(border, color = if (sel) glowColor else Color.White, fontSize = 10.sp)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Stickers & Overlays
                                        Text("Overlay Matrix Filters:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(photoStickers) { sticker ->
                                                val sel = editPhotoSticker == sticker
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (sel) glowColor.copy(alpha = 0.2f) else CosmicSurface)
                                                        .border(1.dp, if (sel) glowColor else Color.Transparent, RoundedCornerShape(6.dp))
                                                        .clickable { editPhotoSticker = sticker }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(sticker, color = if (sel) glowColor else Color.White, fontSize = 10.sp)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Text Signature overlay
                                        OutlinedTextField(
                                            value = editPhotoSig,
                                            onValueChange = { editPhotoSig = it },
                                            placeholder = { Text("Write custom text overlay signature...", color = SlateTextSecondary, fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth().height(42.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = glowColor,
                                                unfocusedBorderColor = CosmicSurface
                                            ),
                                            shape = RoundedCornerShape(6.dp)
                                        )

                                    } else {
                                        // Video Editor Sliders
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Play Speed:", color = Color.White, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                                            Slider(
                                                value = editVideoSpeed,
                                                onValueChange = { editVideoSpeed = it },
                                                valueRange = 0.2f..3.0f,
                                                colors = SliderDefaults.colors(thumbColor = glowColor, activeTrackColor = glowColor),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("${(editVideoSpeed * 100).toInt()}%", color = SlateTextSecondary, fontSize = 11.sp, modifier = Modifier.width(35.dp), textAlign = TextAlign.End)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Density:", color = Color.White, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                                            Slider(
                                                value = editVideoDensity,
                                                onValueChange = { editVideoDensity = it },
                                                valueRange = 0.1f..1.5f,
                                                colors = SliderDefaults.colors(thumbColor = glowColor, activeTrackColor = glowColor),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("${(editVideoDensity * 100).toInt()}%", color = SlateTextSecondary, fontSize = 11.sp, modifier = Modifier.width(35.dp), textAlign = TextAlign.End)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Particle Size:", color = Color.White, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                                            Slider(
                                                value = editVideoSize,
                                                onValueChange = { editVideoSize = it },
                                                valueRange = 2f..15f,
                                                colors = SliderDefaults.colors(thumbColor = glowColor, activeTrackColor = glowColor),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("${editVideoSize.toInt()}dp", color = SlateTextSecondary, fontSize = 11.sp, modifier = Modifier.width(35.dp), textAlign = TextAlign.End)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Wave style selector
                                        Text("Oscillator Waveform Style:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(videoWaves) { wave ->
                                                val sel = editVideoWaveStyle == wave
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (sel) glowColor.copy(alpha = 0.2f) else CosmicSurface)
                                                        .border(1.dp, if (sel) glowColor else Color.Transparent, RoundedCornerShape(6.dp))
                                                        .clickable { editVideoWaveStyle = wave }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(wave, color = if (sel) glowColor else Color.White, fontSize = 10.sp)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Color themes selector
                                        Text("Plasma Theme Spectrum:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(videoThemes) { theme ->
                                                val sel = editVideoColorTheme == theme
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (sel) glowColor.copy(alpha = 0.2f) else CosmicSurface)
                                                        .border(1.dp, if (sel) glowColor else Color.Transparent, RoundedCornerShape(6.dp))
                                                        .clickable { editVideoColorTheme = theme }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(theme, color = if (sel) glowColor else Color.White, fontSize = 10.sp)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Glitch switch
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Analog Glitch Sync Distortion:", color = Color.White, fontSize = 11.sp)
                                            Switch(
                                                checked = editVideoGlitch,
                                                onCheckedChange = { editVideoGlitch = it },
                                                colors = SwitchDefaults.colors(checkedThumbColor = glowColor, checkedTrackColor = glowColor.copy(alpha = 0.4f))
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Action bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    val updatedSerialized = if (currentCreation.type == "photo") {
                                                        PhotoEditConfig(
                                                            brightness = editPhotoBrightness,
                                                            tintShift = editPhotoTintShift,
                                                            borderStyle = editPhotoBorder,
                                                            sticker = editPhotoSticker,
                                                            signature = editPhotoSig
                                                        ).serialize()
                                                    } else {
                                                        VideoEditConfig(
                                                            speed = editVideoSpeed,
                                                            density = editVideoDensity,
                                                            size = editVideoSize,
                                                            waveStyle = editVideoWaveStyle,
                                                            colorTheme = editVideoColorTheme,
                                                            glitchEffect = editVideoGlitch
                                                        ).serialize()
                                                    }

                                                    val updatedCreation = currentCreation.copy(
                                                        lyrics = updatedSerialized,
                                                        style = if (currentCreation.type == "photo") editPhotoBorder else editVideoColorTheme
                                                    )
                                                    viewModel.saveCreation(updatedCreation)
                                                    currentlySelectedCreation = updatedCreation
                                                    Toast.makeText(context, "Creative changes saved successfully!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = glowColor),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.Black, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Save Studio Edits", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (currentCreation.type == "photo") {
                                                    editPhotoBrightness = 0f
                                                    editPhotoTintShift = 0f
                                                    editPhotoBorder = "Classic Thin"
                                                    editPhotoSticker = "None"
                                                    editPhotoSig = ""
                                                } else {
                                                    editVideoSpeed = 1.0f
                                                    editVideoDensity = 0.6f
                                                    editVideoSize = 6.0f
                                                    editVideoWaveStyle = "Sinusoidal Wave"
                                                    editVideoColorTheme = "Teal Pulse"
                                                    editVideoGlitch = false
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, CosmicSurface),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Reset", color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // History Header
            item {
                Text(
                    text = if (activeTab == "photo") "Photo Studio History & Gallery:" else "Video Studio History & Gallery:",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Gallery / History list
            val galleryItems = creations.filter { it.type == activeTab }

            if (galleryItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (activeTab == "photo") Icons.Default.Collections else Icons.Default.VideoLibrary,
                                contentDescription = "Empty",
                                tint = SlateTextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (activeTab == "photo") "No photos composed yet. Let's make some!" else "No cinematic loops rendered yet. Start generating!",
                                color = SlateTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(galleryItems) { item ->
                    val isSelected = currentlySelectedCreation?.id == item.id
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isSelected) glowColor else CosmicSurfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                currentlySelectedCreation = item
                                showEditorPanel = true
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
                                Icon(
                                    imageVector = if (item.type == "photo") Icons.Default.Image else Icons.Default.PlayCircle,
                                    contentDescription = "Visual",
                                    tint = glowColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Prompt: ${item.prompt}",
                                        color = SlateTextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        currentlySelectedCreation = item
                                        showEditorPanel = true
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = glowColor, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = {
                                    if (isSelected) {
                                        currentlySelectedCreation = null
                                        showEditorPanel = false
                                    }
                                    viewModel.deleteCreation(item.id)
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

// Custom Generative Photo Canvas Renderer
@Composable
fun WandjyPhotoRenderer(
    creation: Creation,
    brightness: Float,
    tintShift: Float,
    borderStyle: String,
    sticker: String,
    signature: String
) {
    // Check if it's an uploaded image or preset
    val isUpload = creation.lyrics.startsWith("UPLOAD_URI:")
    val uploadUri = if (isUpload) creation.lyrics.removePrefix("UPLOAD_URI:") else null

    // Cyberpunk Violet (default primary/secondary)
    val baseHue1 = 280f
    val baseHue2 = 180f

    // Determine colors with live hue shift
    val primaryColor = Color.hsv(hue = (baseHue1 + tintShift) % 360f, saturation = 0.85f, value = 1.0f)
    val secondaryColor = Color.hsv(hue = (baseHue2 + tintShift) % 360f, saturation = 0.80f, value = 1.0f)

    Box(modifier = Modifier.fillMaxSize()) {
        if (isUpload && uploadUri != null && !uploadUri.startsWith("preset_")) {
            // Load and draw the uploaded image using Coil's rememberAsyncImagePainter
            androidx.compose.foundation.Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uploadUri)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = "Uploaded Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (!isUpload || uploadUri == null) {
                // Background slate void
                drawRect(color = Color(0xFF0F0B1E))

                // Ambient lights
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.28f), Color.Transparent)
                    ),
                    radius = size.maxDimension * 0.45f,
                    center = Offset(size.width * 0.35f, size.height * 0.38f)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(secondaryColor.copy(alpha = 0.22f), Color.Transparent)
                    ),
                    radius = size.maxDimension * 0.40f,
                    center = Offset(size.width * 0.75f, size.height * 0.68f)
                )

                // Algorithmic lines & abstract shapes generated from prompt hash
                val linesSeed = creation.prompt.hashCode()
                val totalShapes = 14 + (linesSeed % 10).coerceAtLeast(0)

                for (i in 0 until totalShapes) {
                    val shapeX = ((linesSeed + i * 270) % size.width.toInt()).toFloat()
                    val shapeY = ((linesSeed + i * 190) % size.height.toInt()).toFloat()
                    val radius = 30f + ((linesSeed + i * 14) % 90).coerceAtLeast(0)

                    if (i % 3 == 0) {
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.35f),
                            radius = radius,
                            center = Offset(shapeX, shapeY),
                            style = Stroke(width = 2.5f)
                        )
                    } else if (i % 3 == 1) {
                        drawLine(
                            color = secondaryColor.copy(alpha = 0.3f),
                            start = Offset(shapeX - radius * 1.5f, shapeY),
                            end = Offset(shapeX + radius * 1.5f, shapeY + radius),
                            strokeWidth = 3f
                        )
                    } else {
                        drawRect(
                            color = primaryColor.copy(alpha = 0.12f),
                            topLeft = Offset(shapeX - radius, shapeY - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                    }
                }
            } else {
                // It is a preset upload, render high-fidelity procedural scenes!
                when (uploadUri) {
                    "preset_cyber" -> {
                        // Cyber City procedural background
                        drawRect(color = Color(0xFF07040D))

                        // Draw perspective grid
                        val gridYStart = height * 0.4f
                        val points = 16
                        for (i in 0..points) {
                            val f = i.toFloat() / points
                            drawLine(
                                color = NeonPink.copy(alpha = 0.15f),
                                start = Offset(width / 2, gridYStart),
                                end = Offset(width * (f * 2.5f - 0.75f), height),
                                strokeWidth = 1.5f
                            )
                        }

                        // Cyberpunk towers
                        val r = kotlin.random.Random(1337)
                        for (i in 0..8) {
                            val tW = 40f + r.nextFloat() * 60f
                            val tH = 100f + r.nextFloat() * 140f
                            val tX = r.nextFloat() * (width - tW)
                            val tY = height - tH

                            // Tower body
                            drawRect(
                                color = Color(0xFF130E26),
                                topLeft = Offset(tX, tY),
                                size = Size(tW, tH)
                            )
                            // Outer glowing border
                            drawRect(
                                color = primaryColor.copy(alpha = 0.4f),
                                topLeft = Offset(tX, tY),
                                size = Size(tW, tH),
                                style = Stroke(width = 2f)
                            )

                            // Tiny window dots
                            for (wy in (tY + 10f).toInt()..(height - 10f).toInt() step 16) {
                                for (wx in (tX + 10f).toInt()..(tX + tW - 10f).toInt() step 12) {
                                    if (r.nextBoolean()) {
                                        drawCircle(
                                            color = Color(0xFFFFD54F).copy(alpha = 0.6f),
                                            radius = 2f,
                                            center = Offset(wx.toFloat(), wy.toFloat())
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "preset_nebula" -> {
                        // Space Nebula procedural background
                        drawRect(color = Color(0xFF03020A))

                        // Radiant nebula glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonPurple.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            radius = width * 0.6f,
                            center = Offset(width * 0.5f, height * 0.4f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonPink.copy(alpha = 0.35f), Color.Transparent)
                            ),
                            radius = width * 0.4f,
                            center = Offset(width * 0.3f, height * 0.6f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonTeal.copy(alpha = 0.3f), Color.Transparent)
                            ),
                            radius = width * 0.45f,
                            center = Offset(width * 0.7f, height * 0.5f)
                        )

                        // Sparking tiny white/cyan stars
                        val r = kotlin.random.Random(9999)
                        for (i in 0..35) {
                            val sx = r.nextFloat() * width
                            val sy = r.nextFloat() * height
                            val srad = 1.5f + r.nextFloat() * 2f
                            val salpha = 0.3f + r.nextFloat() * 0.7f
                            drawCircle(
                                color = Color.White.copy(alpha = salpha),
                                radius = srad,
                                center = Offset(sx, sy)
                            )
                        }
                    }
                    "preset_ocean" -> {
                        // Synthwave sunset ocean scene
                        drawRect(color = Color(0xFF0A0014))

                        // Giant orange-pink sun
                        val sunRadius = height * 0.28f
                        val sunCenter = Offset(width / 2, height * 0.45f)
                        drawCircle(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF3D00), Color(0xFFFFC107))
                            ),
                            radius = sunRadius,
                            center = sunCenter
                        )

                        // Draw horizon line
                        val horizonY = height * 0.45f
                        drawLine(
                            color = NeonPink,
                            start = Offset(0f, horizonY),
                            end = Offset(width, horizonY),
                            strokeWidth = 3f
                        )

                        // Sunset sunbeam slits (horizontal lines of background color slicing the sun)
                        for (lineY in (horizonY - sunRadius).toInt()..horizonY.toInt() step 20) {
                            val thickness = 4f + (horizonY - lineY) * 0.05f
                            drawLine(
                                color = Color(0xFF0A0014),
                                start = Offset(0f, lineY.toFloat()),
                                end = Offset(width, lineY.toFloat()),
                                strokeWidth = thickness
                            )
                        }

                        // Perspective Ocean lines
                        val oceanLines = 10
                        for (i in 0..oceanLines) {
                            val progress = i.toFloat() / oceanLines
                            val oceanY = horizonY + (height - horizonY) * progress
                            drawLine(
                                color = NeonTeal.copy(alpha = progress * 0.6f),
                                start = Offset(0f, oceanY),
                                end = Offset(width, oceanY),
                                strokeWidth = 1.5f
                            )
                        }
                    }
                }
            }

            // Always apply a live color tint filter
            if (tintShift != 0f) {
                drawRect(
                    color = Color.hsv(tintShift, 0.45f, 1f).copy(alpha = 0.12f),
                    blendMode = androidx.compose.ui.graphics.BlendMode.Color
                )
            }

            // --- Apply Stickers / Overlay Filters ---
            val linesSeed = creation.prompt.hashCode()
            when (sticker) {
                "Cyberpunk Grid" -> {
                    // Draw a 3D prospective grid in the bottom area
                    val gridYStart = size.height * 0.5f
                    val perspectivePoints = 12
                    for (xIndex in 0..perspectivePoints) {
                        val fraction = xIndex.toFloat() / perspectivePoints
                        drawLine(
                            color = primaryColor.copy(alpha = 0.25f),
                            start = Offset(size.width * 0.5f, gridYStart),
                            end = Offset(size.width * (fraction * 2f - 0.5f), size.height),
                            strokeWidth = 2f
                        )
                    }
                    var lineY = gridYStart
                    while (lineY < size.height) {
                        val alphaFactor = (lineY - gridYStart) / (size.height - gridYStart)
                        drawLine(
                            color = primaryColor.copy(alpha = alphaFactor * 0.25f),
                            start = Offset(0f, lineY),
                            end = Offset(size.width, lineY),
                            strokeWidth = 2f
                        )
                        lineY += (size.height - lineY) * 0.35f + 4f
                    }
                }
                "Analog CRT Scanlines" -> {
                    val lineSpacing = 12f
                    var currentY = 0f
                    while (currentY < size.height) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, currentY),
                            end = Offset(size.width, currentY),
                            strokeWidth = 2.5f
                        )
                        currentY += lineSpacing
                    }
                }
                "Matrix Rain Overlay" -> {
                    val random = Random(linesSeed)
                    val cols = 20
                    for (c in 0 until cols) {
                        val colX = (size.width / cols) * c + 10f
                        val dropCount = 5 + random.nextInt(8)
                        var startY = random.nextFloat() * size.height
                        for (d in 0 until dropCount) {
                            val alpha = (1f - (d.toFloat() / dropCount)) * 0.35f
                            drawCircle(
                                color = Color.Green.copy(alpha = alpha),
                                radius = 4f,
                                center = Offset(colX, (startY + d * 18f) % size.height)
                            )
                        }
                    }
                }
                "Vaporwave Glitch Lines" -> {
                    val random = Random(linesSeed)
                    repeat(6) {
                        val barY = random.nextFloat() * size.height
                        val barH = 5f + random.nextFloat() * 18f
                        val barW = 40f + random.nextFloat() * 150f
                        val barX = random.nextFloat() * (size.width - barW)
                        val color = if (it % 2 == 0) NeonPink else NeonCyan
                        drawRect(
                            color = color.copy(alpha = 0.35f),
                            topLeft = Offset(barX, barY),
                            size = Size(barW, barH)
                        )
                    }
                }
            }

            // --- Apply Border Style Frames ---
            when (borderStyle) {
                "Classic Thin" -> {
                    drawRect(
                        color = primaryColor.copy(alpha = 0.5f),
                        style = Stroke(width = 4f),
                        size = size
                    )
                }
                "Cyberpunk Neon" -> {
                    // Outer frame
                    drawRect(
                        color = NeonPink.copy(alpha = 0.7f),
                        style = Stroke(width = 8f),
                        size = size
                    )
                    // Inner glowing wire frame
                    drawRect(
                        color = NeonCyan.copy(alpha = 0.7f),
                        style = Stroke(width = 2.5f),
                        topLeft = Offset(10f, 10f),
                        size = Size(size.width - 20f, size.height - 20f)
                    )
                }
                "Retro Filmstrip" -> {
                    val stripW = 28f
                    // Draw black side blocks
                    drawRect(color = Color.Black, size = Size(stripW, size.height))
                    drawRect(color = Color.Black, size = Size(stripW, size.height), topLeft = Offset(size.width - stripW, 0f))

                    // Draw film sprocket squares
                    var sprocketY = 15f
                    while (sprocketY < size.height) {
                        drawRect(color = Color.White.copy(alpha = 0.7f), topLeft = Offset(8f, sprocketY), size = Size(12f, 12f))
                        drawRect(color = Color.White.copy(alpha = 0.7f), topLeft = Offset(size.width - 20f, sprocketY), size = Size(12f, 12f))
                        sprocketY += 40f
                    }
                }
                "Cosmic Hologram" -> {
                    // Dotted/Dashed matrix frame
                    val steps = 18
                    for (s in 0..steps) {
                        val frac = s.toFloat() / steps
                        drawCircle(color = NeonTeal, radius = 4f, center = Offset(size.width * frac, 6f))
                        drawCircle(color = NeonTeal, radius = 4f, center = Offset(size.width * frac, size.height - 6f))
                        drawCircle(color = NeonTeal, radius = 4f, center = Offset(6f, size.height * frac))
                        drawCircle(color = NeonTeal, radius = 4f, center = Offset(size.width - 6f, size.height * frac))
                    }
                }
            }

            // --- Exposure/Brightness post-overlay ---
            if (brightness > 0f) {
                drawRect(
                    color = Color.White.copy(alpha = brightness * 0.45f),
                    size = size
                )
            } else if (brightness < 0f) {
                drawRect(
                    color = Color.Black.copy(alpha = -brightness * 0.45f),
                    size = size
                )
            }

            // Draw signature text overlay
            if (signature.isNotBlank()) {
                val paint = Paint().apply {
                    color = primaryColor.toArgb()
                    textSize = 42f
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    strokeWidth = 2.5f
                    typeface = android.graphics.Typeface.MONOSPACE
                }
                // draw shadow text
                val shadowPaint = Paint().apply {
                    color = Color.Black.toArgb()
                    textSize = 42f
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    typeface = android.graphics.Typeface.MONOSPACE
                }
                drawContext.canvas.nativeCanvas.drawText(
                    signature,
                    32f + 2f,
                    size.height - 32f + 2f,
                    shadowPaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    signature,
                    32f,
                    size.height - 32f,
                    paint
                )
            }
        }
    }
}

// Custom Generative Looping 60fps Video Canvas Renderer
@Composable
fun WandjyVideoRenderer(
    creation: Creation,
    speedMultiplier: Float,
    density: Float,
    particleSize: Float,
    waveStyle: String,
    colorTheme: String,
    glitchEffect: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VideoRenderer")
    val timeState by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VideoTimer"
    )

    // Palette theme selection
    val primaryColor = when (colorTheme) {
        "Royal Violet" -> NeonPurple
        "Emerald Glitch" -> GlowGreen
        "Neon Pink Plasma" -> NeonPink
        "Golden Sun" -> Color(0xFFFFB300)
        "Chroma Rainbow" -> NeonCyan
        else -> NeonCyan // Teal Pulse
    }
    val secondaryColor = when (colorTheme) {
        "Royal Violet" -> NeonPink
        "Emerald Glitch" -> NeonTeal
        "Neon Pink Plasma" -> NeonPurple
        "Golden Sun" -> NeonPink
        "Chroma Rainbow" -> NeonPink
        else -> NeonPurple
    }

    // Check if it's an upload or preset
    val isUpload = creation.lyrics.startsWith("UPLOAD_URI:")
    val uploadUri = if (isUpload) creation.lyrics.removePrefix("UPLOAD_URI:") else null

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val t = timeState * speedMultiplier

        // Background space void
        drawRect(color = Color(0xFF07040C))

        if (!isUpload || uploadUri == null) {
            val random = Random(creation.prompt.hashCode())

            // --- Render Flying Particles ---
            val particleCount = (42 * density).toInt().coerceAtLeast(5)
            for (i in 0 until particleCount) {
                val offsetSeed = (creation.prompt.hashCode() + i * 360)
                val initialX = (offsetSeed % width.toInt()).toFloat()
                val speedFactor = 1.5f + (offsetSeed % 5)
                val currentY = (offsetSeed + t * speedFactor) % height

                // Draw particle with glow aura
                drawCircle(
                    color = primaryColor.copy(alpha = 0.15f),
                    radius = particleSize * 2.5f,
                    center = Offset(initialX, currentY)
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.55f),
                    radius = particleSize * 0.7f,
                    center = Offset(initialX, currentY)
                )
            }

            // --- Render Wave oscillator ---
            val waveAmplitude = 48f
            val numPoints = 50
            val waveColor = secondaryColor

            for (i in 0 until numPoints) {
                val x1 = (width / numPoints) * i
                val x2 = (width / numPoints) * (i + 1)

                val fraction1 = i.toDouble() / numPoints
                val fraction2 = (i + 1).toDouble() / numPoints

                // Handle different waveforms
                val waveVal1 = when (waveStyle) {
                    "Square/Pulse Wave" -> {
                        val angle = fraction1 * 2.0 * Math.PI * 2.0 + (t * 0.08)
                        if (sin(angle) >= 0f) 1.0 else -1.0
                    }
                    "Sawtooth Glitch Wave" -> {
                        val angle = (fraction1 * 2.0 + (t * 0.02)) % 1.0
                        (angle * 2.0 - 1.0)
                    }
                    else -> { // "Sinusoidal Wave"
                        val angle = fraction1 * 2.0 * Math.PI * 2.0 + (t * 0.08)
                        sin(angle)
                    }
                }

                val waveVal2 = when (waveStyle) {
                    "Square/Pulse Wave" -> {
                        val angle = fraction2 * 2.0 * Math.PI * 2.0 + (t * 0.08)
                        if (sin(angle) >= 0f) 1.0 else -1.0
                    }
                    "Sawtooth Glitch Wave" -> {
                        val angle = (fraction2 * 2.0 + (t * 0.02)) % 1.0
                        (angle * 2.0 - 1.0)
                    }
                    else -> {
                        val angle = fraction2 * 2.0 * Math.PI * 2.0 + (t * 0.08)
                        sin(angle)
                    }
                }

                val y1 = (height / 2f) + waveVal1.toFloat() * waveAmplitude
                val y2 = (height / 2f) + waveVal2.toFloat() * waveAmplitude

                drawLine(
                    color = waveColor.copy(alpha = 0.65f),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 4f
                )
            }

            // Center orb pulse
            val centerPulse = 0.85f + sin(t * 0.12f).toFloat() * 0.15f
            drawCircle(
                color = primaryColor.copy(alpha = 0.15f),
                radius = (height / 4f) * centerPulse,
                center = Offset(width / 2f, height / 2f)
            )
            drawCircle(
                color = secondaryColor.copy(alpha = 0.35f),
                radius = (height / 5.2f) * centerPulse,
                center = Offset(width / 2f, height / 2f),
                style = Stroke(width = 3f)
            )
        } else {
            // It is an uploaded video or video preset loop!
            when (uploadUri) {
                "preset_video_portal" -> {
                    // Hyperjump warp speed wormhole
                    val ringsCount = 10
                    for (i in 0 until ringsCount) {
                        val ringProgress = ((t * 0.15f + i.toFloat()) % ringsCount) / ringsCount
                        val rRadius = (width * 0.8f) * ringProgress
                        val rAlpha = (1f - ringProgress) * 0.5f
                        val rStroke = 1f + 12f * (1f - ringProgress)
                        
                        drawCircle(
                            color = primaryColor.copy(alpha = rAlpha),
                            radius = rRadius.coerceAtLeast(1f),
                            center = Offset(width / 2, height / 2),
                            style = Stroke(width = rStroke)
                        )
                        
                        // Rotated dash dots
                        val dots = 12
                        for (d in 0 until dots) {
                            val angle = (d.toFloat() / dots) * 2.0 * Math.PI + (t * 0.01)
                            val dx = width / 2 + Math.cos(angle).toFloat() * rRadius
                            val dy = height / 2 + Math.sin(angle).toFloat() * rRadius
                            drawCircle(
                                color = secondaryColor.copy(alpha = rAlpha * 0.8f),
                                radius = 4f + 4f * (1f - ringProgress),
                                center = Offset(dx, dy)
                            )
                        }
                    }
                }
                "preset_video_heartbeat" -> {
                    // Concentric radar scanning beat
                    val scaleFactor = 0.6f + sin(t * 0.15f).toFloat() * 0.12f
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.1f),
                        radius = height * 0.45f * scaleFactor,
                        center = Offset(width / 2, height / 2)
                    )
                    drawCircle(
                        color = secondaryColor.copy(alpha = 0.4f),
                        radius = height * 0.35f * scaleFactor,
                        center = Offset(width / 2, height / 2),
                        style = Stroke(width = 3.5f)
                    )
                    
                    // Radar sweep line
                    val sweepAngle = (t * 0.05) % (2.0 * Math.PI)
                    val sx = width / 2 + Math.cos(sweepAngle).toFloat() * (height * 0.35f * scaleFactor)
                    val sy = height / 2 + Math.sin(sweepAngle).toFloat() * (height * 0.35f * scaleFactor)
                    drawLine(
                        color = primaryColor,
                        start = Offset(width / 2, height / 2),
                        end = Offset(sx, sy),
                        strokeWidth = 3f
                    )
                    
                    // Draw digital signal bars on the side
                    for (i in 0..12) {
                        val barH = 15f + sin(t * 0.2f + i).toFloat() * 25f
                        drawRect(
                            color = primaryColor.copy(alpha = 0.5f),
                            topLeft = Offset(24f + i * 14f, height - 40f - barH),
                            size = Size(8f, barH)
                        )
                    }
                }
                else -> {
                    // Display glowing camera viewfinder telemetry for custom uploaded video
                    drawRect(color = Color(0xFF03010A))
                    
                    // Viewfinder corner marks
                    val pad = 30f
                    val len = 40f
                    // Top-Left
                    drawLine(color = primaryColor, start = Offset(pad, pad), end = Offset(pad + len, pad), strokeWidth = 3f)
                    drawLine(color = primaryColor, start = Offset(pad, pad), end = Offset(pad, pad + len), strokeWidth = 3f)
                    // Top-Right
                    drawLine(color = primaryColor, start = Offset(width - pad, pad), end = Offset(width - pad - len, pad), strokeWidth = 3f)
                    drawLine(color = primaryColor, start = Offset(width - pad, pad), end = Offset(width - pad, pad + len), strokeWidth = 3f)
                    // Bottom-Left
                    drawLine(color = primaryColor, start = Offset(pad, height - pad), end = Offset(pad + len, height - pad), strokeWidth = 3f)
                    drawLine(color = primaryColor, start = Offset(pad, height - pad), end = Offset(pad, height - pad - len), strokeWidth = 3f)
                    // Bottom-Right
                    drawLine(color = primaryColor, start = Offset(width - pad, height - pad), end = Offset(width - pad - len, height - pad), strokeWidth = 3f)
                    drawLine(color = primaryColor, start = Offset(width - pad, height - pad), end = Offset(width - pad, height - pad - len), strokeWidth = 3f)
                    
                    // Dynamic oscilloscope particle line representing active video frames
                    val numOscPoints = 30
                    for (i in 0 until numOscPoints) {
                        val ox = pad + ((width - pad * 2) / numOscPoints) * i
                        val oy = height / 2f + sin(t * 0.12f + i * 0.4f).toFloat() * 32f
                        drawCircle(
                            color = secondaryColor.copy(alpha = 0.7f),
                            radius = particleSize.coerceAtLeast(3f),
                            center = Offset(ox, oy)
                        )
                        if (i > 0) {
                            val prevX = pad + ((width - pad * 2) / numOscPoints) * (i - 1)
                            val prevY = height / 2f + sin(t * 0.12f + (i - 1) * 0.4f).toFloat() * 32f
                            drawLine(
                                color = secondaryColor.copy(alpha = 0.35f),
                                start = Offset(prevX, prevY),
                                end = Offset(ox, oy),
                                strokeWidth = 2f
                            )
                        }
                    }
                    
                    // Playhead indicator sliding horizontally
                    val playheadX = pad + ((t * 8f) % (width - pad * 2))
                    drawLine(
                        color = Color.Red,
                        start = Offset(playheadX, pad),
                        end = Offset(playheadX, height - pad),
                        strokeWidth = 2.5f
                    )
                    
                    // REC blinking text
                    val blink = (t.toInt() % 12) < 6
                    if (blink) {
                        drawCircle(
                            color = Color.Red,
                            radius = 6f,
                            center = Offset(pad + 18f, pad + 18f)
                        )
                    }
                }
            }
        }

        // Draw glitch line splits if active
        if (glitchEffect) {
            val glitchChance = (t.toInt() % 10) == 0
            if (glitchChance) {
                val r = Random(t.toInt())
                repeat(4) {
                    val gy = r.nextFloat() * height
                    val gh = 6f
                    drawLine(
                        color = Color.Red.copy(alpha = 0.6f),
                        start = Offset(0f, gy),
                        end = Offset(width, gy),
                        strokeWidth = gh
                    )
                }
            }
        }
    }
}
