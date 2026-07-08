package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.Creation
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.BorderStroke

@Composable
fun WandjyWeb(
    viewModel: ChatViewModel,
    glowColor: Color
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val creations by viewModel.allCreations.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("builder") } // "builder", "gigs", or "published"

    // Builder State
    var webName by remember { mutableStateOf("") }
    var webCategory by remember { mutableStateOf("E-Commerce Store") }
    var webTheme by remember { mutableStateOf("Cyberpunk Violet") }
    var webDesc by remember { mutableStateOf("") }
    var isGeneratingWeb by remember { mutableStateOf(false) }

    var generatedCode by remember { mutableStateOf("") }
    var generatedAppCode by remember { mutableStateOf("") }
    var activePreviewTab by remember { mutableStateOf("browser") } // "browser", "app_live", "code", "compose_code"

    // Publishing Animation States
    var isPublishingWeb by remember { mutableStateOf(false) }
    var publishingStep by remember { mutableStateOf("") }
    var showPublishedDialog by remember { mutableStateOf(false) }
    var lastPublishedUrl by remember { mutableStateOf("") }

    // Dialog State for viewing a live published website
    var selectedViewWebsite by remember { mutableStateOf<Creation?>(null) }

    // Client Gigs State
    var selectedGigContact by remember { mutableStateOf<String?>(null) }
    var draftProposalText by remember { mutableStateOf("") }
    var isDraftingProposal by remember { mutableStateOf(false) }

    val categories = listOf("E-Commerce Store", "Freelance Portfolio", "Local Restaurant", "Fitness & Gym")
    val themes = listOf(
        "Cyberpunk Violet" to NeonPurple,
        "Ocean Teal" to NeonTeal,
        "Volt Cyber" to NeonCyan,
        "Neon Rose" to NeonPink
    )

    // Freelance Clients List
    val gigLeads = listOf(
        GigLead(
            clientName = "Lucille Duplantier",
            business = "L'Amour Vintage Apparel",
            budget = "$2,800",
            needs = "Requires an elegant catalog with filter options, dark warm background theme, and responsive contact form for custom styling inquiries.",
            languages = "English, French"
        ),
        GigLead(
            clientName = "Dr. Raymond Vance",
            business = "Vance Family Orthodontics",
            budget = "$1,500",
            needs = "Needs a clean modern booking landing page with dental service card grids, staff biographies, and an active appointment inquiry module.",
            languages = "English, Spanish"
        ),
        GigLead(
            clientName = "Marco Rossi",
            business = "Rossi's Stone Oven Pizza",
            budget = "$1,950",
            needs = "Needs a cozy warm pizza menu with category dividers, real-time online reservation slots, and a responsive location interactive block.",
            languages = "English, Italian"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground)
            .padding(16.dp)
    ) {
        // Selector Header: Web Builder vs Gigs Marketplace vs Published Websites
        TabRow(
            selectedTabIndex = when (activeSubTab) {
                "builder" -> 0
                "gigs" -> 1
                else -> 2
            },
            containerColor = CosmicSurface,
            contentColor = glowColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeSubTab == "builder",
                onClick = { activeSubTab = "builder" },
                text = { Text("AI Builder", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = activeSubTab == "gigs",
                onClick = { activeSubTab = "gigs" },
                text = { Text("Freelance Gigs", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = activeSubTab == "published",
                onClick = { activeSubTab = "published" },
                text = { Text("My Free Sites", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        when (activeSubTab) {
            "builder" -> {
                // WEB BUILDER WORKSPACE
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Design Website Settings",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            // Site Name Input
                            OutlinedTextField(
                                value = webName,
                                onValueChange = { webName = it },
                                placeholder = { Text("e.g. Astro Gadgets Inc", color = SlateTextSecondary, fontSize = 12.sp) },
                                label = { Text("Website Name / Domain", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(fontSize = 12.sp, color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = glowColor,
                                    unfocusedBorderColor = CosmicSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Site category picker
                            Text("Category:", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                categories.take(3).forEach { cat ->
                                    val isSelected = webCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) glowColor.copy(alpha = 0.2f) else CosmicSurfaceVariant)
                                            .border(1.dp, if (isSelected) glowColor else Color.Transparent, RoundedCornerShape(6.dp))
                                            .clickable { webCategory = cat }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cat.split(" ").first(),
                                            color = if (isSelected) glowColor else Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Site Description Input
                            OutlinedTextField(
                                value = webDesc,
                                onValueChange = { webDesc = it },
                                placeholder = { Text("e.g. An elegant electronics storefront selling glowing retro neon components, futuristic keycaps, and holographic posters...", color = SlateTextSecondary, fontSize = 12.sp) },
                                label = { Text("Describe Your Site Layout & Sections", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                textStyle = TextStyle(fontSize = 12.sp, color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = glowColor,
                                    unfocusedBorderColor = CosmicSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Generate Web & App Button
                            Button(
                                onClick = {
                                    if (webName.isBlank() || webDesc.isBlank()) {
                                        Toast.makeText(context, "Please configure website name & description", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    scope.launch {
                                        isGeneratingWeb = true
                                        val sysInstructionWeb = """
                                            You are Wandjy AI Website Compiler & UI Architect.
                                            Compile responsive, complete, beautiful HTML5 and CSS3 code. Use Google Fonts (e.g. Syne, Space Grotesk) and inline responsive Tailwind configurations.
                                            Output ONLY clean raw HTML block starting with <!DOCTYPE html> and closing with </html>.
                                            Do not write explanations, side notes, or conversational text.
                                        """.trimIndent()

                                        val sysInstructionApp = """
                                            You are Wandjy AI Mobile App Compiler & Jetpack Compose Architect.
                                            Compile a complete, modern, gorgeous Kotlin Jetpack Compose screen. Provide rich styling, beautiful Material 3 cards, state hooks, and edge-to-edge support.
                                            Output ONLY clean, raw Kotlin Jetpack Compose code starting with // Kotlin App Code.
                                            Do not write explanations, markdown blocks, or conversational text. Use elegant Material 3 icons and theme colors.
                                        """.trimIndent()

                                        val webJob = launch {
                                            val response = viewModel.generateWithAI(
                                                prompt = "Create a gorgeous HTML/CSS webpage titled '$webName' for a '$webCategory' styled in '$webTheme' theme. Layout details: $webDesc",
                                                systemInstruction = sysInstructionWeb
                                            )
                                            if (!response.startsWith("Error:")) {
                                                generatedCode = response
                                            } else {
                                                generatedCode = "<!-- Failed to generate website -->"
                                            }
                                        }

                                        val appJob = launch {
                                            val response = viewModel.generateWithAI(
                                                prompt = "Create a gorgeous Kotlin Jetpack Compose mobile screen titled '$webName' for a '$webCategory' app styled in '$webTheme' theme. Layout details: $webDesc",
                                                systemInstruction = sysInstructionApp
                                            )
                                            if (!response.startsWith("Error:")) {
                                                generatedAppCode = response
                                            } else {
                                                generatedAppCode = "// Failed to generate mobile app code"
                                            }
                                        }

                                        webJob.join()
                                        appJob.join()

                                        isGeneratingWeb = false
                                        Toast.makeText(context, "Successfully compiled BOTH Web & App bundles!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = glowColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                            ) {
                                if (isGeneratingWeb) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Compiling Web & App...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generate Website & Mobile App", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                if (generatedCode.isNotBlank()) {
                    item {
                        // PREVIEW CONTAINER
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, glowColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Sub headers
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Project Compilation Output:",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CosmicSurfaceVariant)
                                            .padding(2.dp)
                                    ) {
                                        listOf(
                                            "browser" to "💻 Web",
                                            "app_live" to "📱 App",
                                            "code" to "📄 HTML",
                                            "compose_code" to "📦 Compose"
                                        ).forEach { (tabId, label) ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (activePreviewTab == tabId) glowColor else Color.Transparent)
                                                    .clickable { activePreviewTab = tabId }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (activePreviewTab == tabId) Color.Black else Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                when (activePreviewTab) {
                                    "code" -> {
                                        // Code render box for HTML
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(300.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF0F0E17))
                                                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                                item {
                                                    Text(
                                                        text = generatedCode,
                                                        color = NeonTeal,
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    "compose_code" -> {
                                        // Code render box for Jetpack Compose
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(300.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF0F0E17))
                                                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                                item {
                                                    Text(
                                                        text = generatedAppCode,
                                                        color = NeonCyan,
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    "app_live" -> {
                                        // Simulated Smartphone Preview
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(240.dp)
                                                    .height(420.dp)
                                                    .clip(RoundedCornerShape(28.dp))
                                                    .background(Color(0xFF0F0E17))
                                                    .border(4.dp, Color(0xFF2E2B4E), RoundedCornerShape(28.dp))
                                            ) {
                                                Column(modifier = Modifier.fillMaxSize()) {
                                                    // Notch
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 6.dp),
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .width(60.dp)
                                                                .height(12.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(Color.Black)
                                                        )
                                                    }

                                                    // Status bar
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 14.dp, vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("Wandjy Mobile", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                        Text("5G • 🔋 100% • 12:00 PM", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    // App screen body
                                                    Column(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp)
                                                    ) {
                                                        // Top Bar
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(glowColor.copy(alpha = 0.15f))
                                                                .padding(6.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(Icons.Default.Menu, contentDescription = null, tint = glowColor, modifier = Modifier.size(12.dp))
                                                            Text(text = webName, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = glowColor, modifier = Modifier.size(12.dp))
                                                        }

                                                        Spacer(modifier = Modifier.height(10.dp))

                                                        // Hero Card
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(95.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(
                                                                    Brush.verticalGradient(
                                                                        colors = listOf(
                                                                            Color(0xFF2D124D),
                                                                            Color(0xFF100522)
                                                                        )
                                                                    )
                                                                )
                                                                .border(1.dp, glowColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                                .padding(8.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                Text(
                                                                    text = "$webName Native App",
                                                                    color = Color.White,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                                Text(
                                                                    text = "Category: $webCategory",
                                                                    color = NeonCyan,
                                                                    fontSize = 7.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Text(
                                                                    text = webDesc.take(45) + "...",
                                                                    color = Color.LightGray,
                                                                    fontSize = 7.sp,
                                                                    textAlign = TextAlign.Center,
                                                                    lineHeight = 10.sp
                                                                )
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(10.dp))

                                                        Text("EXPLORE NATIVE SERVICES:", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            listOf(
                                                                "Fast Load" to Icons.Default.Bolt,
                                                                "Security" to Icons.Default.CheckCircle,
                                                                "Support" to Icons.Default.Email
                                                            ).forEach { (feat, icon) ->
                                                                Card(
                                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B192A)),
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .height(62.dp)
                                                                        .border(1.dp, Color(0xFF2E2B4E), RoundedCornerShape(6.dp))
                                                                ) {
                                                                    Column(
                                                                        modifier = Modifier.padding(4.dp),
                                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                                        verticalArrangement = Arrangement.Center
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = icon,
                                                                            contentDescription = null,
                                                                            tint = glowColor,
                                                                            modifier = Modifier.size(12.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.height(2.dp))
                                                                        Text(text = feat, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                                        Text(text = "Enabled", color = Color.Gray, fontSize = 5.sp)
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(12.dp))

                                                        Button(
                                                            onClick = {
                                                                Toast.makeText(context, "Native Android App Action Triggered!", Toast.LENGTH_SHORT).show()
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = glowColor),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(26.dp),
                                                            contentPadding = PaddingValues(0.dp)
                                                        ) {
                                                            Text("Launch Android Application", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }

                                                    // Gesture indicator
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(bottom = 8.dp),
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .width(70.dp)
                                                                .height(3.dp)
                                                                .clip(RoundedCornerShape(2.dp))
                                                                .background(Color.Gray)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        // RESPONSIVE BROWSER SIMULATOR
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(300.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White)
                                                .border(2.dp, CosmicSurfaceVariant, RoundedCornerShape(12.dp))
                                        ) {
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                // Mock Browser address bar
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color(0xFFE2E8F0))
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Yellow))
                                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Green))
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(20.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color.White)
                                                            .padding(horizontal = 8.dp),
                                                        contentAlignment = Alignment.CenterStart
                                                    ) {
                                                        Text(
                                                            text = "https://www.${webName.lowercase().replace(" ", "")}.com",
                                                            color = Color.Gray,
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                                }

                                                // Rendered Layout Simulation
                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxWidth()
                                                        .background(Color(0xFF0F0E17)) // Match simulated responsive dark core
                                                        .padding(14.dp)
                                                ) {
                                                    // Header Row
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(text = webName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            Text("Home", color = NeonTeal, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                            Text("Services", color = Color.LightGray, fontSize = 8.sp)
                                                            Text("Pricing", color = Color.LightGray, fontSize = 8.sp)
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(14.dp))

                                                    // Hero Section
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(
                                                                Brush.verticalGradient(
                                                                    colors = listOf(
                                                                        Color(0xFF331B6B),
                                                                        Color(0xFF150A2F)
                                                                    )
                                                                )
                                                            )
                                                            .padding(12.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text(
                                                                text = "Welcome to $webName",
                                                                color = Color.White,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                textAlign = TextAlign.Center
                                                            )
                                                            Text(
                                                                text = webDesc.take(65) + "...",
                                                                color = Color.LightGray,
                                                                fontSize = 9.sp,
                                                                textAlign = TextAlign.Center,
                                                                modifier = Modifier.padding(vertical = 4.dp)
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(NeonTeal)
                                                                    .clickable {
                                                                        Toast.makeText(context, "Call to action triggered!", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                                            ) {
                                                                Text("Explore Products", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(12.dp))

                                                    // Sample feature grid
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        repeat(2) { index ->
                                                            Card(
                                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B192A)),
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .border(1.dp, Color(0xFF2E2B4E), RoundedCornerShape(6.dp))
                                                            ) {
                                                                Column(modifier = Modifier.padding(6.dp)) {
                                                                    Icon(
                                                                        imageVector = if (index == 0) Icons.Default.Bolt else Icons.Default.Shield,
                                                                        contentDescription = null,
                                                                        tint = NeonTeal,
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.height(4.dp))
                                                                    Text(
                                                                        text = if (index == 0) "Fast Integration" else "Secure Protocols",
                                                                        color = Color.White,
                                                                        fontSize = 8.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                    Text(
                                                                        text = "Highly customized and optimized for multiple viewports.",
                                                                        color = Color.LightGray,
                                                                        fontSize = 7.sp,
                                                                        lineHeight = 10.sp
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                    // Interactive Free Publishing Section
                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = CosmicSurfaceVariant, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(14.dp))

                                    if (isPublishingWeb) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, NeonTeal.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CircularProgressIndicator(
                                                    color = NeonTeal,
                                                    strokeWidth = 2.dp,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "Deploying Website Free...",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = publishingStep,
                                                        color = NeonCyan,
                                                        fontSize = 10.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    isPublishingWeb = true
                                                    publishingStep = "Contacting Wandjy Edge Router..."
                                                    kotlinx.coroutines.delay(800)
                                                    publishingStep = "Allocating static cloud container..."
                                                    kotlinx.coroutines.delay(1000)
                                                    publishingStep = "Compiling HTML5/Tailwind build bundles..."
                                                    kotlinx.coroutines.delay(900)
                                                    publishingStep = "Publishing free subdomain..."
                                                    kotlinx.coroutines.delay(800)
                                                    publishingStep = "Injecting operational logs & SSL/TLS certificate..."
                                                    kotlinx.coroutines.delay(800)

                                                    val finalDomainName = webName.lowercase().replace(" ", "").replace("[^a-z0-9]".toRegex(), "")
                                                    val creation = Creation(
                                                        type = "website",
                                                        prompt = webDesc,
                                                        title = webName,
                                                        style = webTheme,
                                                        lyrics = generatedCode
                                                    )
                                                    viewModel.saveCreation(creation)
                                                    lastPublishedUrl = "https://$finalDomainName.wandjy.sh"
                                                    isPublishingWeb = false
                                                    showPublishedDialog = true
                                                    activeSubTab = "published"
                                                    Toast.makeText(context, "Website successfully published to $lastPublishedUrl!", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Public,
                                                contentDescription = "Publish",
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Publish Website (Free Hosting)",
                                                color = Color.Black,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
        "gigs" -> {
                // CLIENT GIGS MARKETPLACE WORKSPACE
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(gigLeads) { gig ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(glowColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = gig.clientName.take(1),
                                            color = glowColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = gig.clientName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = gig.business, color = SlateTextSecondary, fontSize = 10.sp)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonTeal.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = gig.budget, color = NeonTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Project Specific Needs:",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = gig.needs,
                                color = SlateTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Requested Languages: ${gig.languages}",
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = {
                                        selectedGigContact = gig.clientName
                                        scope.launch {
                                            isDraftingProposal = true
                                            val response = viewModel.generateWithAI(
                                                prompt = "Draft a professional freelance web design pitch proposal for: ${gig.clientName} of ${gig.business}. Project details: ${gig.needs}. Budget: ${gig.budget}.",
                                                systemInstruction = "You are Wandjy AI freelance marketing agent. Draft highly convincing, professional web design proposals detailing timeline, tech stacks, and user-centric features."
                                            )
                                            isDraftingProposal = false
                                            draftProposalText = response
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = glowColor),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Apply & Pitch with AI", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (selectedGigContact != null && draftProposalText.isNotBlank()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NeonTeal, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "AI Compiled Proposal ($selectedGigContact):",
                                        color = NeonTeal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = {
                                        selectedGigContact = null
                                        draftProposalText = ""
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = NeonPink, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = draftProposalText,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Proposal sent! Client is reviewing...", Toast.LENGTH_LONG).show()
                                            scope.launch {
                                                draftProposalText = "Lucille Duplantier: 'Wow, this proposal looks incredible and extremely professional! Let's sign the agreement. Can we start on Monday?'"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
                                    ) {
                                        Text("Submit Pitch", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        "published" -> {
                WandjyPublishedWebsites(creations, viewModel, glowColor, context) { website ->
                    selectedViewWebsite = website
                }
            }
        }

        // Success dialog when a website is published
        if (showPublishedDialog) {
            AlertDialog(
                onDismissRequest = { showPublishedDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = GlowGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Website Live & Online!", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Your responsive HTML5/Tailwind website is officially hosted on Wandjy's high-speed cloud edge nodes free of charge.",
                            color = SlateTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GlowGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("FREE PUBLIC INSTANT URL:", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lastPublishedUrl,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPublishedDialog = false
                            clipboardManager.setText(AnnotatedString(lastPublishedUrl))
                            Toast.makeText(context, "URL Copied!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Copy URL", color = NeonTeal, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPublishedDialog = false }) {
                        Text("Awesome", color = Color.White)
                    }
                },
                containerColor = CosmicSurface
            )
        }

        // Dialog for viewing a live published website
        selectedViewWebsite?.let { website ->
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { selectedViewWebsite = null },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CosmicBackground),
                    color = CosmicBackground
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Browser Bar Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CosmicSurface)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { selectedViewWebsite = null }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = website.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "🟢 Status: Online • Free Wandjy Cloud Hosting",
                                        color = GlowGreen,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = {
                                    val finalDomain = website.title.lowercase().replace(" ", "").replace("[^a-z0-9]".toRegex(), "")
                                    val url = "https://$finalDomain.wandjy.sh"
                                    clipboardManager.setText(AnnotatedString(url))
                                    Toast.makeText(context, "Copied URL to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", tint = NeonTeal, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Simulated Browser Core
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFF0F0E17))
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = website.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Home", color = NeonTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("Services", color = Color.LightGray, fontSize = 10.sp)
                                        Text("Pricing", color = Color.LightGray, fontSize = 10.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Hero Section
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFF331B6B),
                                                    Color(0xFF150A2F)
                                                )
                                            )
                                        )
                                        .padding(18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(NeonTeal.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = website.style, color = NeonTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Welcome to ${website.title}",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = website.prompt,
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NeonTeal)
                                                .clickable {
                                                    Toast.makeText(context, "Call to action triggered!", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text("Explore Products", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Features Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    listOf(
                                        "Lightning Speed" to "Your page compiles and delivers under 50ms using decentralized routing.",
                                        "Zero Maintenance" to "No updates required. Security and updates managed by Wandjy Cloud."
                                    ).forEachIndexed { index, (feat, desc) ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B192A)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(1.dp, Color(0xFF2E2B4E), RoundedCornerShape(8.dp))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Icon(
                                                    imageVector = if (index == 0) Icons.Default.Bolt else Icons.Default.Cloud,
                                                    contentDescription = null,
                                                    tint = NeonTeal,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(text = feat, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = desc, color = Color.LightGray, fontSize = 9.sp, lineHeight = 12.sp)
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
}

@Composable
fun WandjyPublishedWebsites(
    creations: List<Creation>,
    viewModel: ChatViewModel,
    glowColor: Color,
    context: android.content.Context,
    onViewWebsite: (Creation) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val publishedSites = creations.filter { it.type == "website" }

    if (publishedSites.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(glowColor.copy(alpha = 0.12f))
                        .border(1.5.dp, glowColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "No sites",
                        tint = glowColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Websites Published Yet",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Design a webpage using our AI Web Builder and publish it instantly with 1-click free cloud hosting!",
                    color = SlateTextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Free Cloud Hosting",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Active responsive sites hosted on Wandjy network.",
                            color = SlateTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GlowGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${publishedSites.size} ONLINE",
                            color = GlowGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            items(publishedSites) { site ->
                val subdomain = site.title.lowercase().replace(" ", "").replace("[^a-z0-9]".toRegex(), "")
                val publicUrl = "https://$subdomain.wandjy.sh"

                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(glowColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = glowColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = site.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = site.style,
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Green pulse online indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GlowGreen.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(GlowGreen)
                                )
                                Text(
                                    text = "LIVE",
                                    color = GlowGreen,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = site.prompt,
                            color = SlateTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // URL copy bar
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(publicUrl))
                                    Toast.makeText(context, "Copied URL to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = publicUrl,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy URL",
                                    tint = glowColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: View Site & Delete (Unpublish)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onViewWebsite(site) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = glowColor),
                                border = BorderStroke(1.dp, glowColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "View Live Site",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.deleteCreation(site.id)
                                    Toast.makeText(context, "Website successfully unpublished & hosting removed.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPink),
                                border = BorderStroke(1.dp, NeonPink),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Stop Free Hosting",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class GigLead(
    val clientName: String,
    val business: String,
    val budget: String,
    val needs: String,
    val languages: String
)
