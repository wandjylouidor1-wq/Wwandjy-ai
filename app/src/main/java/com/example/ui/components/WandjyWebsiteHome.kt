package com.example.ui.components

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CosmicBackground
import com.example.ui.CosmicSurface
import com.example.ui.CosmicSurfaceVariant
import com.example.ui.NeonCyan
import com.example.ui.NeonPink
import com.example.ui.NeonPurple
import com.example.ui.NeonTeal
import com.example.ui.GlowGreen
import com.example.ui.SlateTextSecondary
import com.example.ui.ChatViewModel

@Composable
fun WandjyWebsiteHome(
    glowColor: Color,
    onNavigateToTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackground),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // Hero Landing Section
        item {
            WandjyHeroSection(glowColor = glowColor, onNavigateToTab = onNavigateToTab)
        }

        // Section Divider
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXPLORE THE SUITE",
                    color = glowColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                HorizontalDivider(color = CosmicSurfaceVariant, thickness = 1.dp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Modular Tools Cards (Grid Layout in items)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WandjyServiceRow(
                    title = "💻 AI Web & Page Builder",
                    desc = "Design custom HTML web screens, build mobile pages, and publish them instantly to free 1-click cloud hosting.",
                    accentColor = NeonTeal,
                    btnText = "Launch Builder Console",
                    onClick = { onNavigateToTab("web") }
                )
                
                WandjyServiceRow(
                    title = "💬 Active Chat Companion",
                    desc = "Converse with distinct personalities (Classic, Creative, Developer, Zen) and explore real-time simulated Voice Calling.",
                    accentColor = NeonCyan,
                    btnText = "Open Chatroom",
                    onClick = { onNavigateToTab("chat") }
                )

                WandjyServiceRow(
                    title = "🎵 Beats & Synthesizer Studio",
                    desc = "Generate modular ambient sounds, custom synthesizer beats, and play back live acoustic soundtracks.",
                    accentColor = NeonPurple,
                    btnText = "Compose Soundtracks",
                    onClick = { onNavigateToTab("beats") }
                )

                WandjyServiceRow(
                    title = "🎨 Creative Photography & Art Studio",
                    desc = "Render custom avatars, produce digital visual elements, and export photographic canvas assets directly.",
                    accentColor = NeonPink,
                    btnText = "Open Studio Canvas",
                    onClick = { onNavigateToTab("studio") }
                )

                WandjyServiceRow(
                    title = "📚 AI Tutor & Homework Helper",
                    desc = "Clarify intricate subjects, view math formulas, learn Kotlin/Compose logic, and compile study files.",
                    accentColor = GlowGreen,
                    btnText = "Access Tutor Desk",
                    onClick = { onNavigateToTab("study") }
                )
            }
        }

        // Live Edge Router Terminal
        item {
            Spacer(modifier = Modifier.height(28.dp))
            WandjyEdgeRouterMonitor(glowColor = glowColor)
        }

        // Published Showreel Section
        item {
            Spacer(modifier = Modifier.height(28.dp))
            WandjyShowreelSection(glowColor = glowColor, onNavigateToTab = onNavigateToTab)
        }

        // Footer Section
        item {
            Spacer(modifier = Modifier.height(40.dp))
            WandjyWebsiteFooter(glowColor = glowColor)
        }
    }
}

@Composable
fun WandjyHeroSection(
    glowColor: Color,
    onNavigateToTab: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.15f),
                        CosmicSurface
                    ),
                    center = Offset(200f, 100f),
                    radius = 800f
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(glowColor.copy(alpha = 0.4f), CosmicSurfaceVariant.copy(alpha = 0.1f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Elegant top tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(glowColor.copy(alpha = 0.12f))
                    .border(0.5.dp, glowColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GlowGreen)
                    )
                    Text(
                        text = "NEW VERSION 4.0 WEB FRAMEWORK",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "The Multi-Agent\nWeb Playground",
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Assemble, compile, and host custom websites on the fly while generating custom art assets, synthesized ambient soundscapes, and tutoring nodes in a modular, responsive workspace.",
                fontSize = 13.sp,
                color = SlateTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action CTAs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onNavigateToTab("web") },
                    colors = ButtonDefaults.buttonColors(containerColor = glowColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = CosmicBackground, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Web Builder",
                        color = CosmicBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { onNavigateToTab("chat") },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant),
                    border = BorderStroke(1.dp, glowColor.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.ChatBubble, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interactive Portal",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WandjyServiceRow(
    title: String,
    desc: String,
    accentColor: Color,
    btnText: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = desc,
                color = SlateTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = btnText,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = accentColor, modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Composable
fun WandjyEdgeRouterMonitor(glowColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    
    // Pulse animation for radar status
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha"
    )

    // Grid animation phases for wave canvas
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Edge Network Router Console",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Host Node: global-edge-router-4.wandjy.sh",
                        color = SlateTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(14.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = GlowGreen.copy(alpha = pulseAlpha), radius = size.minDimension / 2f * pulseSize)
                            drawCircle(color = GlowGreen, radius = size.minDimension / 2.5f)
                        }
                    }
                    Text(
                        text = "SECURED (SSL)",
                        color = GlowGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas drawing real-time latency sinus waves
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CosmicSurfaceVariant)
                    .border(0.5.dp, CosmicSurfaceVariant, RoundedCornerShape(10.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    // Draw grid lines
                    val gridCols = 10
                    for (i in 1..gridCols) {
                        val x = width * (i.toFloat() / gridCols)
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 1f
                        )
                    }
                    val gridRows = 5
                    for (i in 1..gridRows) {
                        val y = height * (i.toFloat() / gridRows)
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                    }

                    // Plot latency sine waves
                    val path1 = Path()
                    val path2 = Path()
                    
                    val points = 100
                    for (i in 0..points) {
                        val x = width * (i.toFloat() / points)
                        
                        // First wave (cyan)
                        val angle1 = (i.toFloat() / points) * 4f * Math.PI.toFloat() + phase
                        val y1 = height / 2f + Math.sin(angle1.toDouble()).toFloat() * 18f
                        if (i == 0) path1.moveTo(x, y1) else path1.lineTo(x, y1)

                        // Second wave (purple, out of phase)
                        val angle2 = (i.toFloat() / points) * 6f * Math.PI.toFloat() - phase * 1.5f
                        val y2 = height / 2f + Math.cos(angle2.toDouble()).toFloat() * 12f
                        if (i == 0) path2.moveTo(x, y2) else path2.lineTo(x, y2)
                    }

                    drawPath(
                        path = path1,
                        color = NeonCyan.copy(alpha = 0.6f),
                        style = Stroke(width = 3f)
                    )

                    drawPath(
                        path = path2,
                        color = NeonPurple.copy(alpha = 0.5f),
                        style = Stroke(width = 2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Server details grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ACTIVE SOCKETS", color = SlateTextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("2,401 open / secure", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("AVERAGE RESPONSE", color = SlateTextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("14ms globally", color = GlowGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("EDGE POP REGION", color = SlateTextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("AWS us-east-1 pod", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WandjyShowreelSection(
    glowColor: Color,
    onNavigateToTab: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            text = "PREVIEW SUCCESS STORIES",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
        Text(
            text = "Active client web interfaces generated and hosted free:",
            color = SlateTextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToTab("web") }
                    .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("L'Amour Vintage", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("E-Commerce Store", color = NeonPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Warm dark retro layout with custom inventory blocks.", color = SlateTextSecondary, fontSize = 9.sp, lineHeight = 13.sp)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToTab("web") }
                    .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Rossi's Pizza", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Local Restaurant", color = NeonTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Cosy interactive stone baked pizza menu and booking table.", color = SlateTextSecondary, fontSize = 9.sp, lineHeight = 13.sp)
                }
            }
        }
    }
}

@Composable
fun WandjyWebsiteFooter(glowColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CosmicSurface)
            .padding(vertical = 24.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text("Wandjy AI Core", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("• HTML Web Compiler", color = SlateTextSecondary, fontSize = 11.sp)
                Text("• Audio Synth Node", color = SlateTextSecondary, fontSize = 11.sp)
                Text("• Dynamic Art Portal", color = SlateTextSecondary, fontSize = 11.sp)
                Text("• Interactive Homework", color = SlateTextSecondary, fontSize = 11.sp)
            }

            Column {
                Text("Developer Network", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("• Edge Routing API", color = SlateTextSecondary, fontSize = 11.sp)
                Text("• Secure Sandbox VPS", color = SlateTextSecondary, fontSize = 11.sp)
                Text("• SSL Certificates", color = SlateTextSecondary, fontSize = 11.sp)
                Text("• Dev Console Logging", color = SlateTextSecondary, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = CosmicSurfaceVariant, thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "© 2026 Wandjy AI Inc. All rights reserved.",
            color = SlateTextSecondary,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Powered by Google Gemini and Jetpack Compose Multiplatform.",
            color = SlateTextSecondary.copy(alpha = 0.6f),
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
