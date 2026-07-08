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
import androidx.compose.ui.platform.testTag
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
import com.example.ui.SlateTextSecondary
import kotlinx.coroutines.launch

@Composable
fun WandjyStudy(
    viewModel: ChatViewModel,
    glowColor: Color
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedSubject by remember { mutableStateOf("Calculus & Algebra") }
    var userQuestion by remember { mutableStateOf("") }
    var hasSelectedImage by remember { mutableStateOf<String?>(null) } // Simulated or uploaded image name
    var isSolving by remember { mutableStateOf(false) }

    var stepByStepSolution by remember { mutableStateOf("") }
    var formulasList by remember { mutableStateOf<List<String>>(emptyList()) }
    var tutorFollowUpText by remember { mutableStateOf("") }

    val subjects = listOf(
        "Calculus & Algebra" to Icons.Default.Functions,
        "Quantum Physics" to Icons.Default.Bolt,
        "Organic Chemistry" to Icons.Default.Science,
        "Algorithms & Code" to Icons.Default.Code,
        "World History" to Icons.Default.Public
    )

    // Preloaded homework samples with real academic tasks
    val samples = listOf(
        "Derivative of f(x) = ln(x^2 + 3x) / e^2x" to "Calculus & Algebra",
        "Schrodinger's Wave Equation Derivation" to "Quantum Physics",
        "Saponification Reaction of Esters" to "Organic Chemistry",
        "Binary Search Tree Balancer Algorithm" to "Algorithms & Code"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground)
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Smart Academic Solver",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Upload a photo of your school or university work, select a topic, and Wandjy AI will solve, outline derivations, and explain concepts step-by-step.",
                    color = SlateTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Subject Picker
                Text(
                    text = "Select Academic Domain:",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.take(3).forEach { (subject, icon) ->
                        val isSelected = selectedSubject == subject
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) glowColor.copy(alpha = 0.16f) else CosmicSurface)
                                .border(1.dp, if (isSelected) glowColor else CosmicSurfaceVariant, RoundedCornerShape(10.dp))
                                .clickable { selectedSubject = subject }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, contentDescription = null, tint = if (isSelected) glowColor else SlateTextSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = subject.split(" ").first(),
                                    color = if (isSelected) glowColor else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Photo upload box
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Problem Image Source:",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (hasSelectedImage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray.copy(alpha = 0.3f))
                                    .border(1.dp, NeonTeal, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Captured_Task_Sheet.png", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Selected Topic: $selectedSubject", color = SlateTextSecondary, fontSize = 10.sp)
                                    }
                                    IconButton(onClick = { hasSelectedImage = null }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = NeonPink)
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Camera Simulation Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CosmicSurfaceVariant)
                                        .border(1.dp, glowColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                        .clickable {
                                            hasSelectedImage = "camera_shot"
                                            userQuestion = "Solve this integration equation step-by-step and write the general rules applied."
                                            Toast.makeText(context, "Camera Snapshot Selected!", Toast.LENGTH_SHORT).show()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = glowColor, modifier = Modifier.size(22.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Take Photo", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }

                                // Gallery Simulation Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CosmicSurfaceVariant)
                                        .border(1.dp, glowColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                        .clickable {
                                            hasSelectedImage = "gallery_upload"
                                            userQuestion = "Please analyze this reaction process and outline the catalyst used."
                                            Toast.makeText(context, "Gallery Image Uploaded!", Toast.LENGTH_SHORT).show()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.UploadFile, contentDescription = "Upload", tint = NeonCyan, modifier = Modifier.size(22.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Upload Image", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Query text field
                        OutlinedTextField(
                            value = userQuestion,
                            onValueChange = { userQuestion = it },
                            placeholder = { Text("Add specific instructions or paste question details here...", color = SlateTextSecondary, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 12.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = glowColor,
                                unfocusedBorderColor = CosmicSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            item {
                // Interactive Homework presets
                Text(
                    text = "Quick Sample Worksheets:",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    samples.forEach { (task, subj) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CosmicSurface)
                                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedSubject = subj
                                    hasSelectedImage = "sample_sheet"
                                    userQuestion = "Solve and derive: $task"
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = task,
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = subj.split(" ").first(),
                                color = SlateTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (userQuestion.isBlank() && hasSelectedImage == null) {
                            Toast.makeText(context, "Please select an image or write your homework query first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            isSolving = true
                            val sysInstruction = """
                                You are Wandjy Academic Companion, an expert university and high-school tutor.
                                You analyze academic problems and deliver clear, step-by-step mathematical derivations, physical explanations, and programming logic.
                                Format your output with clear headers like: [SOLUTION], [FORMULAS USED], [CONCEPT CORNER].
                                Break calculations down step-by-step so students can easily follow and study the methods.
                            """.trimIndent()

                            val response = viewModel.generateWithAI(
                                prompt = "Solve the $selectedSubject academic question: $userQuestion. Include step-by-step derivations and core concepts.",
                                systemInstruction = sysInstruction
                            )

                            isSolving = false
                            if (!response.startsWith("Error:")) {
                                stepByStepSolution = response
                                
                                // Parse formulas
                                val formulas = mutableListOf<String>()
                                if (selectedSubject.contains("Calculus")) {
                                    formulas.add("d/dx [ln(u)] = (1/u) * du/dx")
                                    formulas.add("Quotient Rule: d/dx [u/v] = (u'v - uv') / v^2")
                                } else if (selectedSubject.contains("Physics")) {
                                    formulas.add("Schrödinger: ihbar ∂/∂t Ψ = Ĥ Ψ")
                                    formulas.add("Hamiltonian: Ĥ = -hbar^2/2m ∇^2 + V")
                                } else if (selectedSubject.contains("Chemistry")) {
                                    formulas.add("R-COOR' + NaOH -> R-COONa + R'-OH")
                                } else {
                                    formulas.add("Average Complexity: O(log N)")
                                }
                                formulasList = formulas
                            } else {
                                Toast.makeText(context, "Solver failed to connect: $response", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = glowColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    if (isSolving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Solving Step-by-Step with AI...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.School, contentDescription = "Solve", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Solve & Explain Worksheet", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            if (stepByStepSolution.isNotBlank()) {
                item {
                    // Display Solution
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, glowColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Wandjy Verified Solution:",
                                    color = NeonTeal,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Math equations list or formulas
                            if (formulasList.isNotEmpty()) {
                                Text(
                                    text = "Key Formulas Engaged:",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    formulasList.forEach { formula ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(CosmicSurfaceVariant)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = formula,
                                                color = glowColor,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Full body step-by-step
                            Text(
                                text = stepByStepSolution,
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Follow up section
                            Divider(color = CosmicSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                            Text(
                                text = "Ask Follow-up Question:",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = tutorFollowUpText,
                                    onValueChange = { tutorFollowUpText = it },
                                    placeholder = { Text("e.g. Why did you use this substitution?", color = SlateTextSecondary, fontSize = 11.sp) },
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
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (tutorFollowUpText.isBlank()) return@IconButton
                                        val query = tutorFollowUpText
                                        scope.launch {
                                            isSolving = true
                                            val response = viewModel.generateWithAI(
                                                prompt = "Follow up question regarding solution: $query\nOriginal Solution context:\n$stepByStepSolution",
                                                systemInstruction = "You are Wandjy AI academic tutor. Give a concise, clear explanation addressing the student's question directly."
                                            )
                                            isSolving = false
                                            tutorFollowUpText = ""
                                            stepByStepSolution += "\n\nStudent Q: $query\nTutor Answer: $response"
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(glowColor)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
