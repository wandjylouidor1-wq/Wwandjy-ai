package com.example.data.repository

import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.ChatDao
import com.example.data.database.ChatMessage
import com.example.data.database.ChatThread
import com.example.data.database.Creation
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {

    val allThreads: Flow<List<ChatThread>> = chatDao.getAllThreads()
    val starredMessages: Flow<List<ChatMessage>> = chatDao.getStarredMessages()
    val allCreations: Flow<List<Creation>> = chatDao.getAllCreations()

    fun getMessages(threadId: String): Flow<List<ChatMessage>> =
        chatDao.getMessagesForThread(threadId)

    suspend fun createThread(thread: ChatThread) =
        chatDao.insertThread(thread)

    suspend fun saveMessage(message: ChatMessage): Long =
        chatDao.insertMessage(message)

    suspend fun toggleMessageStar(messageId: Long, isStarred: Boolean) =
        chatDao.updateMessageStar(messageId, isStarred)

    suspend fun deleteThread(threadId: String) =
        chatDao.deleteThreadAndMessages(threadId)

    suspend fun updateThreadTitle(threadId: String, newTitle: String) =
        chatDao.updateThreadTitle(threadId, newTitle)

    suspend fun saveCreation(creation: Creation): Long =
        chatDao.insertCreation(creation)

    suspend fun deleteCreation(creationId: Long) =
        chatDao.deleteCreation(creationId)

    suspend fun generateAIResponse(
        model: String,
        systemInstruction: String,
        history: List<ChatMessage>,
        newPrompt: String,
        attachmentUri: String? = null,
        attachmentType: String? = null,
        context: android.content.Context? = null
    ): String {
        // Fetch API key injected by Secrets Gradle Plugin
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Error: Gemini API Key is missing. Please configure it in the Secrets panel in AI Studio."
        }

        // Map history and prompt into the format required by Gemini
        val contents = mutableListOf<Content>()
        // To avoid API errors, we only include valid user/model alternation in history
        history.filter { it.role == "user" || it.role == "model" }.forEach { msg ->
            contents.add(
                Content(
                    role = msg.role,
                    parts = listOf(Part(text = msg.text))
                )
            )
        }
        
        // Add current user prompt with optional attachment part
        val userParts = mutableListOf<Part>()
        userParts.add(Part(text = newPrompt))
        
        if (attachmentUri != null) {
            val mimeType = if (attachmentType == "video") "video/mp4" else "image/png"
            var base64Data: String? = null
            
            if (attachmentUri.startsWith("preset_")) {
                // Preset 1x1 transparent/green pixel PNG base64 representation
                base64Data = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
            } else if (context != null) {
                try {
                    val uri = android.net.Uri.parse(attachmentUri)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        base64Data = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    }
                } catch (e: Exception) {
                    // Fail gracefully and use 1x1 pixel fallback
                    base64Data = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
                }
            }
            
            if (base64Data != null) {
                userParts.add(Part(inlineData = com.example.data.api.Blob(mimeType = mimeType, data = base64Data)))
            }
        }

        contents.add(
            Content(
                role = "user",
                parts = userParts
            )
        )

        val request = GenerateContentRequest(
            contents = contents,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(model, apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Error: Empty or null response returned from the model."
        } catch (e: Exception) {
            "Error: ${e.localizedMessage ?: e.message ?: "Unknown connection error"}"
        }
    }
}
