package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.ChatMessage
import com.example.data.database.ChatThread
import com.example.data.database.Creation
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.components.WandjyPhotoRenderer
import com.example.ui.components.WandjyVideoRenderer
import com.example.ui.components.WandjyTalk
import com.example.ui.components.WandjyBeats
import com.example.ui.components.WandjyStudio
import com.example.ui.components.WandjyStudy
import com.example.ui.components.WandjyWeb
import com.example.ui.components.WandjyWebsiteHome
import com.example.ui.components.WandjyDeploymentConsole
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Cosmic Slate Theme Colors
val CosmicBackground = Color(0xFF0F0F16)
val CosmicSurface = Color(0xFF161622)
val CosmicSurfaceVariant = Color(0xFF1E1E2F)
val NeonCyan = Color(0xFF00F0FF)
val NeonTeal = Color(0xFF00FFC2)
val NeonPurple = Color(0xFF9E00FF)
val NeonPink = Color(0xFFFF007A)
val GlowGreen = Color(0xFF39FF14)
val SlateTextSecondary = Color(0xFF8E8EA8)

@Composable
fun WandjyApp(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    isExpandedScreen: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // ViewModel state bindings
    val allThreads by viewModel.allThreads.collectAsStateWithLifecycle()
    val activeThreadId by viewModel.activeThreadId.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val starredMessages by viewModel.starredMessages.collectAsStateWithLifecycle()
    val selectedVibe by viewModel.selectedVibe.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // Website navigation: "home", "chat", "beats", "studio", "study", "web", "starred"
    var activeTab by remember { mutableStateOf("home") }
    var showVoiceCallOverlay by remember { mutableStateOf(false) }
    var showDeploymentConsole by remember { mutableStateOf(false) }
    
    // Dialog triggers
    var threadToRename by remember { mutableStateOf<ChatThread?>(null) }
    var renameTitleValue by remember { mutableStateOf("") }

    // Dynamic color glow matching selected vibe
    val vibeGlowColor = when (selectedVibe) {
        ChatViewModel.Vibe.CLASSIC -> NeonCyan
        ChatViewModel.Vibe.CREATIVE -> NeonPurple
        ChatViewModel.Vibe.DEVELOPER -> NeonTeal
        ChatViewModel.Vibe.ZEN -> NeonPink
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackground)
    ) { innerPadding ->
        // Handle Rename dialog
        if (threadToRename != null) {
            AlertDialog(
                onDismissRequest = { threadToRename = null },
                title = { Text("Rename Chat Session", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = renameTitleValue,
                        onValueChange = { renameTitleValue = it },
                        label = { Text("New Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = vibeGlowColor,
                            unfocusedBorderColor = SlateTextSecondary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            threadToRename?.let {
                                if (renameTitleValue.isNotBlank()) {
                                    viewModel.renameThread(it.id, renameTitleValue.trim())
                                }
                            }
                            threadToRename = null
                        }
                    ) {
                        Text("Save", color = vibeGlowColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { threadToRename = null }) {
                        Text("Cancel", color = SlateTextSecondary)
                    }
                },
                containerColor = CosmicSurfaceVariant
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CosmicBackground)
        ) {
            // Web Browser Bar with integrated Web Address input & SSL locks
            WandjyBrowserBar(
                activeTab = activeTab,
                onNavigateToTab = { activeTab = it },
                glowColor = vibeGlowColor,
                isThinking = isGenerating,
                onDeploymentClick = { showDeploymentConsole = true }
            )

            // Dynamic view loading depending on the website's active URL route
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    "home" -> {
                        WandjyWebsiteHome(
                            glowColor = vibeGlowColor,
                            onNavigateToTab = { activeTab = it }
                        )
                    }
                    "chat" -> {
                        if (isExpandedScreen) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .width(300.dp)
                                        .fillMaxHeight()
                                        .background(CosmicSurface)
                                        .drawBehind {
                                            drawLine(
                                                color = CosmicSurfaceVariant,
                                                start = Offset(size.width, 0f),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = 2f
                                            )
                                        }
                                ) {
                                    Text(
                                        text = "Conversations",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )

                                    Button(
                                        onClick = { viewModel.createNewChat() },
                                        colors = ButtonDefaults.buttonColors(containerColor = vibeGlowColor.copy(alpha = 0.15f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .border(1.dp, vibeGlowColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "New Chat", tint = vibeGlowColor)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Start New Chat", color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    var sidebarTab by remember { mutableStateOf("threads") }
                                    TabRow(
                                        selectedTabIndex = if (sidebarTab == "threads") 0 else 1,
                                        containerColor = Color.Transparent,
                                        contentColor = vibeGlowColor,
                                        indicator = { tabPositions ->
                                            TabRowDefaults.SecondaryIndicator(
                                                Modifier.tabIndicatorOffset(tabPositions[if (sidebarTab == "threads") 0 else 1]),
                                                color = vibeGlowColor
                                            )
                                        }
                                    ) {
                                        Tab(
                                            selected = sidebarTab == "threads",
                                            onClick = { sidebarTab = "threads" },
                                            text = { Text("Threads", fontWeight = FontWeight.Bold) }
                                        )
                                        Tab(
                                            selected = sidebarTab == "starred",
                                            onClick = { sidebarTab = "starred" },
                                            text = { Text("Saved", fontWeight = FontWeight.Bold) }
                                        )
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        if (sidebarTab == "threads") {
                                            ThreadListScreen(
                                                threads = allThreads,
                                                activeThreadId = activeThreadId,
                                                onSelectThread = { viewModel.setActiveThread(it) },
                                                onDeleteThread = { viewModel.deleteThread(it) },
                                                onRenameThread = { thread ->
                                                    threadToRename = thread
                                                    renameTitleValue = thread.title
                                                }
                                            )
                                        } else {
                                            StarredMessagesPanel(
                                                starredMessages = starredMessages,
                                                onToggleStar = { id, active -> viewModel.toggleStar(id, active) },
                                                onSelectThread = { threadId -> viewModel.setActiveThread(threadId) }
                                            )
                                        }
                                    }
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    ChatViewport(
                                        viewModel = viewModel,
                                        activeMessages = activeMessages,
                                        isGenerating = isGenerating,
                                        errorMessage = errorMessage,
                                        selectedVibe = selectedVibe,
                                        vibeGlowColor = vibeGlowColor,
                                        onVoiceClick = { showVoiceCallOverlay = true }
                                    )
                                }
                            }
                        } else {
                            ChatViewport(
                                viewModel = viewModel,
                                activeMessages = activeMessages,
                                isGenerating = isGenerating,
                                errorMessage = errorMessage,
                                selectedVibe = selectedVibe,
                                vibeGlowColor = vibeGlowColor,
                                onVoiceClick = { showVoiceCallOverlay = true }
                            )
                        }
                    }
                    "beats" -> {
                        WandjyBeats(
                            viewModel = viewModel,
                            glowColor = vibeGlowColor
                        )
                    }
                    "studio" -> {
                        WandjyStudio(
                            viewModel = viewModel,
                            glowColor = vibeGlowColor
                        )
                    }
                    "study" -> {
                        WandjyStudy(
                            viewModel = viewModel,
                            glowColor = vibeGlowColor
                        )
                    }
                    "web" -> {
                        WandjyWeb(
                            viewModel = viewModel,
                            glowColor = vibeGlowColor
                        )
                    }
                    "starred" -> {
                        var savedTab by remember { mutableStateOf("history") }
                        Column(modifier = Modifier.fillMaxSize()) {
                            TabRow(
                                selectedTabIndex = if (savedTab == "history") 0 else 1,
                                containerColor = CosmicSurface,
                                contentColor = vibeGlowColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Tab(
                                    selected = savedTab == "history",
                                    onClick = { savedTab = "history" },
                                    text = { Text("Chat History", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                )
                                Tab(
                                    selected = savedTab == "starred",
                                    onClick = { savedTab = "starred" },
                                    text = { Text("Saved Answers", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                )
                            }

                            if (savedTab == "history") {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Button(
                                        onClick = {
                                            viewModel.createNewChat()
                                            activeTab = "chat"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = vibeGlowColor.copy(alpha = 0.15f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .border(1.dp, vibeGlowColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "New Chat", tint = vibeGlowColor)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Start New Wandjy Chat", color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    ThreadListScreen(
                                        threads = allThreads,
                                        activeThreadId = activeThreadId,
                                        onSelectThread = {
                                            viewModel.setActiveThread(it)
                                            activeTab = "chat"
                                        },
                                        onDeleteThread = { viewModel.deleteThread(it) },
                                        onRenameThread = { thread ->
                                            threadToRename = thread
                                            renameTitleValue = thread.title
                                        }
                                    )
                                }
                            } else {
                                StarredMessagesPanel(
                                    starredMessages = starredMessages,
                                    onToggleStar = { id, active -> viewModel.toggleStar(id, active) },
                                    onSelectThread = { threadId ->
                                        viewModel.setActiveThread(threadId)
                                        activeTab = "chat"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showVoiceCallOverlay) {
            WandjyTalk(
                viewModel = viewModel,
                glowColor = vibeGlowColor,
                onDismiss = { showVoiceCallOverlay = false }
            )
        }

        if (showDeploymentConsole) {
            WandjyDeploymentConsole(
                onDismiss = { showDeploymentConsole = false },
                glowColor = vibeGlowColor
            )
        }
    }
}

@Composable
fun WandjyBrowserBar(
    activeTab: String,
    onNavigateToTab: (String) -> Unit,
    glowColor: Color,
    isThinking: Boolean,
    onDeploymentClick: () -> Unit
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val currentUrl = when (activeTab) {
        "home" -> "https://www.wandjy.sh/"
        "chat" -> "https://www.wandjy.sh/chat"
        "beats" -> "https://www.wandjy.sh/beats"
        "studio" -> "https://www.wandjy.sh/studio"
        "study" -> "https://www.wandjy.sh/study"
        "web" -> "https://www.wandjy.sh/builder"
        "starred" -> "https://www.wandjy.sh/saved"
        else -> "https://www.wandjy.sh/"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CosmicSurface)
    ) {
        // TOP Row: Web Browser Bar Window Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Window controls (Red, Yellow, Green dots) - desktop look
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF27C93F)))
            }

            // Browser Nav Buttons: Back, Forward, Refresh
            IconButton(
                onClick = { if (activeTab != "home") onNavigateToTab("home") },
                enabled = activeTab != "home",
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Home",
                    tint = if (activeTab != "home") Color.White else SlateTextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = { /* Simulated forward */ },
                enabled = false,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Forward",
                    tint = SlateTextSecondary.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = { 
                    Toast.makeText(context, "Reloading secure connection to edge router...", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Centered URL Input Address Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CosmicSurfaceVariant)
                    .border(0.5.dp, glowColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .clickable {
                        Toast.makeText(context, "Connected to SSL Secured Node: $currentUrl", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "SSL Secure",
                        tint = GlowGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentUrl,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy URL",
                        tint = SlateTextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Deployment Store / Cloud console button
            IconButton(
                onClick = onDeploymentClick,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CosmicSurfaceVariant)
                    .border(0.5.dp, glowColor.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Cloud Console",
                    tint = glowColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Secure network pulse dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isThinking) glowColor.copy(alpha = pulseAlpha) else GlowGreen)
                    .border(0.5.dp, if (isThinking) glowColor else GlowGreen.copy(alpha = 0.5f), CircleShape)
            )
        }

        // Horizontal Divider below Address Bar
        HorizontalDivider(color = CosmicSurfaceVariant, thickness = 1.dp)

        // BOTTOM Row: Website Responsive Top Navbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigateToTab("home") }
            ) {
                Text(
                    text = "wandjy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(glowColor.copy(alpha = 0.2f))
                        .border(0.5.dp, glowColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "AI",
                        color = glowColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Scrollable pills for website tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                val menuItems = listOf(
                    Triple("home", "Home", Icons.Default.Home),
                    Triple("chat", "Chat Portal", Icons.Default.ChatBubble),
                    Triple("web", "Web Builder", Icons.Default.Code),
                    Triple("beats", "Beats Studio", Icons.Default.MusicNote),
                    Triple("studio", "Art Studio", Icons.Default.PhotoCamera),
                    Triple("study", "Tutor Hub", Icons.Default.School),
                    Triple("starred", "Saved", Icons.Default.Star)
                )

                items(menuItems) { (tabKey, label, icon) ->
                    val isSelected = activeTab == tabKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) glowColor.copy(alpha = 0.15f) else Color.Transparent)
                            .border(0.5.dp, if (isSelected) glowColor else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { onNavigateToTab(tabKey) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) glowColor else SlateTextSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else SlateTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Glowing bottom edge line for the entire browser/website header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(glowColor.copy(alpha = 0.1f), glowColor, glowColor.copy(alpha = 0.1f))
                    )
                )
        )
    }
}

@Composable
fun WandjyHeader(glowColor: Color, isThinking: Boolean, onDeploymentClick: () -> Unit = {}) {
    // Elegant header banner with dynamic neon state indicators
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CosmicSurface)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "wandjy",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(glowColor.copy(alpha = 0.2f))
                        .border(0.5.dp, glowColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "AI",
                        color = glowColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "Your intelligent companion",
                fontSize = 11.sp,
                color = SlateTextSecondary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Deployment Console Trigger
            IconButton(
                onClick = onDeploymentClick,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CosmicSurfaceVariant)
                    .border(0.5.dp, glowColor.copy(alpha = 0.3f), CircleShape)
                    .testTag("deployment_console_trigger_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Store Console",
                    tint = glowColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Status orb
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CosmicSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isThinking) glowColor.copy(alpha = pulseAlpha) else GlowGreen)
                        .border(
                            1.dp,
                            if (isThinking) glowColor else GlowGreen.copy(alpha = 0.5f),
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isThinking) "Thinking..." else "Active",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ChatViewport(
    viewModel: ChatViewModel,
    activeMessages: List<ChatMessage>,
    isGenerating: Boolean,
    errorMessage: String?,
    selectedVibe: ChatViewModel.Vibe,
    vibeGlowColor: Color,
    onVoiceClick: () -> Unit
) {
    val scrollState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Automatically scroll to bottom when new messages arrive
    LaunchedEffect(activeMessages.size, isGenerating) {
        if (activeMessages.isNotEmpty()) {
            scrollState.animateScrollToItem(activeMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground)
    ) {
        if (activeMessages.isEmpty()) {
            // First Launch / Cold Start - Personality Vibe picker + suggestions
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Wandjy's Personality",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Carousel of Vibes
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    items(ChatViewModel.Vibe.entries) { vibe ->
                        val isSelected = vibe == selectedVibe
                        val cardBorderColor = if (isSelected) vibeGlowColor else CosmicSurfaceVariant
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CosmicSurfaceVariant else CosmicSurface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .width(150.dp)
                                .height(130.dp)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = cardBorderColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.selectVibe(vibe) }
                                .testTag("vibe_card_${vibe.vibeName}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = vibe.emoji, fontSize = 24.sp)
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = vibeGlowColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = vibe.displayName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = vibe.subtitle,
                                        color = SlateTextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Staggered Prompt Suggestions for current vibe
                Text(
                    text = "Start with a suggestion:",
                    color = SlateTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                selectedVibe.promptSuggestions.forEach { suggestion ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.sendMessage(suggestion)
                                keyboardController?.hide()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Idea", tint = vibeGlowColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = suggestion,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        } else {
            // Message Thread viewport
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeMessages) { message ->
                    ChatMessageRow(
                        message = message,
                        onToggleStar = { id, isStarred ->
                            viewModel.toggleStar(id, isStarred)
                        },
                        glowColor = vibeGlowColor,
                        vibeEmoji = selectedVibe.emoji
                    )
                }

                if (isGenerating) {
                    item {
                        TypingIndicatorRow(emoji = selectedVibe.emoji, glowColor = vibeGlowColor)
                    }
                }

                if (errorMessage != null) {
                    item {
                        ErrorCard(errorText = errorMessage!!, onDismiss = { viewModel.clearError() })
                    }
                }
            }
        }

        // Custom Dynamic Bottom Text Input bar
        ChatInputBar(
            onSendMessage = { text, attachmentUri, attachmentType ->
                viewModel.sendMessage(text, attachmentUri, attachmentType)
                keyboardController?.hide()
            },
            isGenerating = isGenerating,
            glowColor = vibeGlowColor,
            onVoiceClick = onVoiceClick
        )
    }
}

@Composable
fun ChatMessageRow(
    message: ChatMessage,
    onToggleStar: (Long, Boolean) -> Unit,
    glowColor: Color,
    vibeEmoji: String
) {
    val isUser = message.role == "user"
    val isSystemError = message.role == "error"

    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CosmicSurface)
                    .border(1.dp, glowColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (isSystemError) "⚠️" else vibeEmoji, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isUser) CosmicSurfaceVariant else if (isSystemError) Color(0xFF331118) else CosmicSurface
                    )
                    .border(
                        width = 1.dp,
                        color = if (isUser) glowColor.copy(alpha = 0.3f) else if (isSystemError) Color.Red.copy(alpha = 0.3f) else CosmicSurfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    if (message.attachmentUri != null) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .size(width = 220.dp, height = 140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, glowColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        ) {
                            val creation = Creation(
                                type = message.attachmentType ?: "photo",
                                prompt = "Chat Attachment",
                                title = "Shared Media",
                                style = "Chat",
                                lyrics = "UPLOAD_URI:${message.attachmentUri}"
                            )
                            if (message.attachmentType == "video") {
                                WandjyVideoRenderer(
                                    creation = creation,
                                    speedMultiplier = 1f,
                                    density = 0.5f,
                                    particleSize = 4f,
                                    waveStyle = "Sinusoidal Wave",
                                    colorTheme = "Teal Pulse",
                                    glitchEffect = false
                                )
                            } else {
                                WandjyPhotoRenderer(
                                    creation = creation,
                                    brightness = 0f,
                                    tintShift = 0f,
                                    borderStyle = "Classic Thin",
                                    sticker = "None",
                                    signature = ""
                                )
                            }
                        }
                    }

                    SelectionContainer {
                        Text(
                            text = message.text,
                            color = if (isSystemError) Color(0xFFFFB4AB) else Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))

                    // Inner actions row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                            fontSize = 9.sp,
                            color = SlateTextSecondary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Copy button
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy message text",
                                tint = SlateTextSecondary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Wandjy Message", message.text)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                            )

                            // Star / StarBorder Button
                            Icon(
                                imageVector = if (message.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Star answer",
                                tint = if (message.isStarred) Color.Yellow else SlateTextSecondary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable {
                                        onToggleStar(message.id, !message.isStarred)
                                    }
                                    .testTag("star_button_${message.id}")
                            )
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(glowColor.copy(alpha = 0.15f))
                    .border(1.dp, glowColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👤", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ChatInputBar(
    onSendMessage: (String, String?, String?) -> Unit,
    isGenerating: Boolean,
    glowColor: Color,
    onVoiceClick: () -> Unit
) {
    var textValue by remember { mutableStateOf("") }
    var attachedUri by remember { mutableStateOf<String?>(null) }
    var attachedType by remember { mutableStateOf<String?>(null) } // "photo" or "video"
    var showMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            attachedUri = it.toString()
            attachedType = "photo"
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            attachedUri = it.toString()
            attachedType = "video"
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = CosmicSurface,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Render attachment preview bar if a file is loaded
            attachedUri?.let { uri ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth()
                        .border(1.dp, glowColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (attachedType == "photo") Icons.Default.Image else Icons.Default.PlayCircle,
                                contentDescription = "Attachment",
                                tint = glowColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (uri.startsWith("preset_")) "Preset: " + uri.removePrefix("preset_") else "Custom Media Attached",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ready to share with AI",
                                    color = SlateTextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                attachedUri = null
                                attachedType = null
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(glowColor.copy(alpha = 0.12f))
                        .border(1.5.dp, glowColor.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Live Voice Conversation",
                        tint = glowColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(glowColor.copy(alpha = 0.12f))
                            .border(1.5.dp, glowColor.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach Media File",
                            tint = NeonPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(CosmicSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Upload Custom Photo", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Photo, contentDescription = null, tint = NeonCyan) },
                            onClick = {
                                showMenu = false
                                photoLauncher.launch("image/*")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Upload Custom Video", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.VideoCall, contentDescription = null, tint = NeonPink) },
                            onClick = {
                                showMenu = false
                                videoLauncher.launch("video/*")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Cyber City Photo (Preset)", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GlowGreen) },
                            onClick = {
                                showMenu = false
                                attachedUri = "preset_cyber"
                                attachedType = "photo"
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Cosmic Nebula Photo (Preset)", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonPurple) },
                            onClick = {
                                showMenu = false
                                attachedUri = "preset_nebula"
                                attachedType = "photo"
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Quantum Portal Video (Preset)", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan) },
                            onClick = {
                                showMenu = false
                                attachedUri = "preset_video_portal"
                                attachedType = "video"
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    placeholder = { Text("Ask wandjy or research media...", color = SlateTextSecondary, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = glowColor,
                        unfocusedBorderColor = CosmicSurfaceVariant,
                        focusedContainerColor = CosmicBackground,
                        unfocusedContainerColor = CosmicBackground
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if ((textValue.isNotBlank() || attachedUri != null) && !isGenerating) {
                                onSendMessage(textValue.trim(), attachedUri, attachedType)
                                textValue = ""
                                attachedUri = null
                                attachedType = null
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if ((textValue.isNotBlank() || attachedUri != null) && !isGenerating) {
                            onSendMessage(textValue.trim(), attachedUri, attachedType)
                            textValue = ""
                            attachedUri = null
                            attachedType = null
                        }
                    },
                    containerColor = if ((textValue.isNotBlank() || attachedUri != null) && !isGenerating) glowColor else CosmicSurfaceVariant,
                    contentColor = if ((textValue.isNotBlank() || attachedUri != null) && !isGenerating) Color.Black else SlateTextSecondary,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ThreadListScreen(
    threads: List<ChatThread>,
    activeThreadId: String?,
    onSelectThread: (String) -> Unit,
    onDeleteThread: (String) -> Unit,
    onRenameThread: (ChatThread) -> Unit
) {
    if (threads.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = "No history",
                    tint = SlateTextSecondary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "No Conversations Yet",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Your AI chats will appear here.",
                    color = SlateTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(threads) { thread ->
                val isActive = thread.id == activeThreadId
                val vibeColor = when (thread.vibe) {
                    "Classic" -> NeonCyan
                    "Creative" -> NeonPurple
                    "Developer" -> NeonTeal
                    else -> NeonPink
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) CosmicSurfaceVariant else CosmicSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isActive) vibeColor else CosmicSurfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectThread(thread.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(vibeColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = thread.vibe,
                                    color = vibeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = thread.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault()).format(Date(thread.timestamp)),
                                color = SlateTextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        Row {
                            IconButton(onClick = { onRenameThread(thread) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename Session", tint = SlateTextSecondary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { onDeleteThread(thread.id) },
                                modifier = Modifier.testTag("delete_thread_${thread.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Session", tint = NeonPink, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StarredMessagesPanel(
    starredMessages: List<ChatMessage>,
    onToggleStar: (Long, Boolean) -> Unit,
    onSelectThread: (String) -> Unit
) {
    if (starredMessages.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.StarOutline,
                    contentDescription = "No saved cards",
                    tint = SlateTextSecondary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "No Saved Answers",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Star any response to save it here for instant reference.",
                    color = SlateTextSecondary,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(starredMessages) { message ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Starred", tint = Color.Yellow, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (message.role == "user") "Your Prompt" else "Wandjy Answer",
                                    color = if (message.role == "user") NeonCyan else NeonTeal,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row {
                                IconButton(onClick = { onSelectThread(message.threadId) }) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = "Open Session", tint = NeonCyan, modifier = Modifier.size(14.dp))
                                }
                                IconButton(onClick = { onToggleStar(message.id, false) }) {
                                    Icon(Icons.Default.Star, contentDescription = "Unstar", tint = Color.Yellow, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.text,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(message.timestamp)),
                            color = SlateTextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorRow(emoji: String, glowColor: Color) {
    // Elegant pulsing animation mimicking typing
    val infiniteTransition = rememberInfiniteTransition(label = "PulseDots")
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot1"
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot2"
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CosmicSurface)
                .border(1.dp, glowColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(CosmicSurface)
                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(6.dp * dot1Scale).clip(CircleShape).background(glowColor))
                Box(modifier = Modifier.size(6.dp * dot2Scale).clip(CircleShape).background(glowColor))
                Box(modifier = Modifier.size(6.dp * dot3Scale).clip(CircleShape).background(glowColor))
            }
        }
    }
}

@Composable
fun ErrorCard(errorText: String, onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF331118)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Red.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Error, contentDescription = "Error", tint = Color(0xFFFFB4AB))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = errorText,
                    color = Color(0xFFFFB4AB),
                    fontSize = 13.sp
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFFFFB4AB))
            }
        }
    }
}
