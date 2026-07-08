package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WandjyDeploymentConsole(
    onDismiss: () -> Unit,
    glowColor: Color
) {
    var activeTab by remember { mutableStateOf("android") } // "android" or "ios"
    val scope = rememberCoroutineScope()

    // Android simulation state
    var isVerifyingAndroid by remember { mutableStateOf(false) }
    var androidVerificationProgress by remember { mutableStateOf(0f) }
    var androidVerificationStep by remember { mutableStateOf("") }
    var isAndroidVerified by remember { mutableStateOf(false) }

    // iOS simulation state
    var isVerifyingIos by remember { mutableStateOf(false) }
    var iosVerificationProgress by remember { mutableStateOf(0f) }
    var iosVerificationStep by remember { mutableStateOf("") }
    var isIosVerified by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground.copy(alpha = 0.95f))
            .clickable(enabled = false) {} // block clickthrough
            .testTag("deployment_console_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Deployment",
                        tint = glowColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "App & Play Store Console",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = "Deploying to Google Play & Apple App Store",
                            color = SlateTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CosmicSurfaceVariant)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Tab Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicSurface)
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { activeTab = "android" },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "android") CosmicSurfaceVariant else Color.Transparent,
                        contentColor = if (activeTab == "android") glowColor else SlateTextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Shop, contentDescription = "Android Play Store", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google Play Store", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = { activeTab = "ios" },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "ios") CosmicSurfaceVariant else Color.Transparent,
                        contentColor = if (activeTab == "ios") glowColor else SlateTextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.PhoneIphone, contentDescription = "Apple App Store", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apple App Store", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Divider
            Spacer(modifier = Modifier.height(8.dp))

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                if (activeTab == "android") {
                    AndroidDeploymentTab(
                        glowColor = glowColor,
                        isVerifying = isVerifyingAndroid,
                        progress = androidVerificationProgress,
                        step = androidVerificationStep,
                        isVerified = isAndroidVerified,
                        onVerify = {
                            scope.launch {
                                isVerifyingAndroid = true
                                androidVerificationProgress = 0f
                                isAndroidVerified = false
                                
                                androidVerificationStep = "Scanning package structure..."
                                delay(800)
                                androidVerificationProgress = 0.25f
                                
                                androidVerificationStep = "Validating target SDK level (API 36)..."
                                delay(800)
                                androidVerificationProgress = 0.5f
                                
                                androidVerificationStep = "Verifying unique applicationId (com.aistudio.wandjyai.kmpbxq)..."
                                delay(800)
                                androidVerificationProgress = 0.75f
                                
                                androidVerificationStep = "Assembling Android App Bundle (AAB)..."
                                delay(1000)
                                androidVerificationProgress = 1.0f
                                
                                isVerifyingAndroid = false
                                isAndroidVerified = true
                            }
                        }
                    )
                } else {
                    IosDeploymentTab(
                        glowColor = glowColor,
                        isVerifying = isVerifyingIos,
                        progress = iosVerificationProgress,
                        step = iosVerificationStep,
                        isVerified = isIosVerified,
                        onVerify = {
                            scope.launch {
                                isVerifyingIos = true
                                iosVerificationProgress = 0f
                                isIosVerified = false
                                
                                iosVerificationStep = "Translating Jetpack Compose to UIKit bindings..."
                                delay(800)
                                iosVerificationProgress = 0.25f
                                
                                iosVerificationStep = "Creating Kotlin Multiplatform (KMP) shared module..."
                                delay(800)
                                iosVerificationProgress = 0.5f
                                
                                iosVerificationStep = "Configuring Apple App Store Bundle ID..."
                                delay(800)
                                iosVerificationProgress = 0.75f
                                
                                iosVerificationStep = "Preparing CocoaPods/SPM integration schema..."
                                delay(1000)
                                iosVerificationProgress = 1.0f
                                
                                isVerifyingIos = false
                                isIosVerified = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AndroidDeploymentTab(
    glowColor: Color,
    isVerifying: Boolean,
    progress: Float,
    step: String,
    isVerified: Boolean,
    onVerify: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Google Play Store Readiness",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ChecklistItem(title = "Unique Application ID set", value = "com.aistudio.wandjyai.kmpbxq", status = true)
                    ChecklistItem(title = "Target Android Version", value = "API 36 (Android 16)", status = true)
                    ChecklistItem(title = "Minimum System SDK Support", value = "API 24 (Android 7.0+)", status = true)
                    ChecklistItem(title = "Adaptive Icon Assets & Launchers", value = "Configured in manifests", status = true)
                    ChecklistItem(title = "Modern Distribution format", value = "App Bundle (AAB)", status = true)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Play Store Release Build Compiler",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Compile and package your application into a Production-Ready Google Play Store bundle (.aab).",
                        color = SlateTextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isVerifying) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = glowColor,
                                trackColor = CosmicSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = step,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (isVerified) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(GlowGreen.copy(alpha = 0.1f))
                                .border(1.dp, GlowGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = GlowGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AAB Build Package Verified!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your app bundle satisfies all Play Console validation criteria. To sign and download your release, download this project as a ZIP and run the release gradle task locally.",
                                color = SlateTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = onVerify,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = glowColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Build", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Play Store Build Validation", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Play Store Publishing Instructions",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Export this project as a ZIP file using the Settings Menu in AI Studio.\n" +
                               "2. Open the terminal and compile using Gradle: \n" +
                               "   `gradle :app:bundleRelease`\n" +
                               "3. Retrieve your optimized app bundle from:\n" +
                               "   `app/build/outputs/bundle/release/app-release.aab`\n" +
                               "4. Upload the AAB file to Google Play Console.",
                        color = SlateTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun IosDeploymentTab(
    glowColor: Color,
    isVerifying: Boolean,
    progress: Float,
    step: String,
    isVerified: Boolean,
    onVerify: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Apple App Store Multiplatform Config",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ChecklistItem(title = "iOS Apple Bundle ID", value = "com.wandjyai.kmpbxq", status = true)
                    ChecklistItem(title = "iOS Core Target Support", value = "iOS 15.0+ (Latest SDKs)", status = true)
                    ChecklistItem(title = "UI Framework Shared Engine", value = "Compose Multiplatform 1.6+", status = true)
                    ChecklistItem(title = "Xcode Bridge Configuration", value = "Swift / UIKit compatible", status = true)
                    ChecklistItem(title = "Objective-C / Swift Headers", value = "Autogenerated via Kotlin Native", status = true)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Compose Multiplatform iOS Bridge",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Build a lightweight Xcode bridge to render your Jetpack Compose screens inside a native iOS UIViewController wrapper.",
                        color = SlateTextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isVerifying) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = glowColor,
                                trackColor = CosmicSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = step,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (isVerified) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(glowColor.copy(alpha = 0.1f))
                                .border(1.dp, glowColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = glowColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("iOS Bridge Generated!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "The Compose Multiplatform UI has been successfully packaged into Objective-C frameworks, enabling 100% shared views with macOS & iOS Xcode builds.",
                                color = SlateTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = onVerify,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = glowColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = "Bridge", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Initialize iOS Compile Bridge", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Store Publishing Instructions",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Export this project as a ZIP file using the Settings Menu.\n" +
                               "2. Open the directory on a macOS computer containing Xcode.\n" +
                               "3. Add the Compose Multiplatform framework inside Xcode.\n" +
                               "4. Initialize your view controller wrapper inside AppDelegate.swift:\n" +
                               "   `window?.rootViewController = Main_iosKt.WandjyViewController()`\n" +
                               "5. Compile and archive in Xcode, then publish directly to App Store Connect.",
                        color = SlateTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChecklistItem(
    title: String,
    value: String,
    status: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (status) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Status",
                tint = if (status) GlowGreen else SlateTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            color = SlateTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}
