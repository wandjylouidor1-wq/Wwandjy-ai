package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.ChatMessage
import com.example.data.database.ChatThread
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ChatRepository(database.chatDao())
    }

    // List of all conversations in history
    val allThreads: StateFlow<List<ChatThread>> = repository.allThreads
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current active thread ID
    private val _activeThreadId = MutableStateFlow<String?>(null)
    val activeThreadId: StateFlow<String?> = _activeThreadId.asStateFlow()

    // Current active vibe (character personality) for new chats
    private val _selectedVibe = MutableStateFlow(Vibe.CLASSIC)
    val selectedVibe: StateFlow<Vibe> = _selectedVibe.asStateFlow()

    // Observed messages for the active thread
    val activeMessages: StateFlow<List<ChatMessage>> = _activeThreadId
        .flatMapLatest { threadId ->
            if (threadId != null) {
                repository.getMessages(threadId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // List of starred messages across all chats
    val starredMessages: StateFlow<List<ChatMessage>> = repository.starredMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // List of all creations (songs, photos, videos)
    val allCreations: StateFlow<List<com.example.data.database.Creation>> = repository.allCreations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI loading / typing indicator
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // UI Error message (if any)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun selectVibe(vibe: Vibe) {
        _selectedVibe.value = vibe
    }

    suspend fun saveCreation(creation: com.example.data.database.Creation): Long {
        return repository.saveCreation(creation)
    }

    fun deleteCreation(creationId: Long) {
        viewModelScope.launch {
            repository.deleteCreation(creationId)
        }
    }

    suspend fun generateWithAI(prompt: String, systemInstruction: String): String {
        return repository.generateAIResponse(
            model = "gemini-3.5-flash",
            systemInstruction = systemInstruction,
            history = emptyList(),
            newPrompt = prompt
        )
    }

    fun setActiveThread(threadId: String?) {
        _activeThreadId.value = threadId
        // If switching thread, we automatically match the vibe of that thread if it exists
        if (threadId != null) {
            val thread = allThreads.value.find { it.id == threadId }
            if (thread != null) {
                val matchingVibe = Vibe.entries.find { it.vibeName == thread.vibe }
                if (matchingVibe != null) {
                    _selectedVibe.value = matchingVibe
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun deleteThread(threadId: String) {
        viewModelScope.launch {
            repository.deleteThread(threadId)
            if (_activeThreadId.value == threadId) {
                _activeThreadId.value = null
            }
        }
    }

    fun renameThread(threadId: String, newTitle: String) {
        viewModelScope.launch {
            repository.updateThreadTitle(threadId, newTitle)
        }
    }

    fun toggleStar(messageId: Long, isStarred: Boolean) {
        viewModelScope.launch {
            repository.toggleMessageStar(messageId, isStarred)
        }
    }

    fun sendMessage(userText: String, attachmentUri: String? = null, attachmentType: String? = null) {
        if (userText.isBlank() && attachmentUri == null) return

        viewModelScope.launch {
            _isGenerating.value = true
            _errorMessage.value = null

            // 1. Establish current thread
            var threadId = _activeThreadId.value
            val currentVibe = _selectedVibe.value

            if (threadId == null) {
                // Generate a thread ID and auto-title
                val newThreadId = UUID.randomUUID().toString()
                val displayTitle = if (userText.isNotBlank()) userText else if (attachmentType == "photo") "Shared Photo" else "Shared Video"
                val shortTitle = if (displayTitle.length > 25) displayTitle.take(22) + "..." else displayTitle
                val newThread = ChatThread(
                    id = newThreadId,
                    title = shortTitle,
                    vibe = currentVibe.vibeName
                )
                repository.createThread(newThread)
                _activeThreadId.value = newThreadId
                threadId = newThreadId
            }

            // 2. Save User Message with optional attachments
            val userMsg = ChatMessage(
                threadId = threadId,
                role = "user",
                text = if (userText.isNotBlank()) userText else if (attachmentType == "photo") "[Shared Photo]" else "[Shared Video]",
                attachmentUri = attachmentUri,
                attachmentType = attachmentType
            )
            repository.saveMessage(userMsg)

            // 3. Collect active messages history
            val history = activeMessages.value

            // 4. Trigger Gemini API request in background
            val aiResponse = repository.generateAIResponse(
                model = "gemini-3.5-flash",
                systemInstruction = currentVibe.systemInstruction,
                history = history,
                newPrompt = userMsg.text,
                attachmentUri = attachmentUri,
                attachmentType = attachmentType,
                context = getApplication()
            )

            // 5. Save Model Response
            val isError = aiResponse.startsWith("Error:")
            val modelMsg = ChatMessage(
                threadId = threadId,
                role = if (isError) "error" else "model",
                text = aiResponse
            )
            repository.saveMessage(modelMsg)

            if (isError) {
                _errorMessage.value = aiResponse
            }

            _isGenerating.value = false
        }
    }

    fun createNewChat() {
        _activeThreadId.value = null
    }

    // Enum representing Wandjy personalities
    enum class Vibe(
        val vibeName: String,
        val displayName: String,
        val subtitle: String,
        val systemInstruction: String,
        val emoji: String,
        val promptSuggestions: List<String>
    ) {
        CLASSIC(
            vibeName = "Classic",
            displayName = "Wandjy Classic",
            subtitle = "Sleek, polite, and exceptionally precise.",
            systemInstruction = "You are Wandjy, a classic AI assistant. You are exceptionally intelligent, clear, polite, and helpful. You explain complex topics simply and answer questions with precision. Keep answers structured and clean.",
            emoji = "🤖",
            promptSuggestions = listOf(
                "Explain Quantum Computing in simple terms",
                "Draft a professional email asking for a deadline extension",
                "Summarize the benefits of healthy sleep habits"
            )
        ),
        CREATIVE(
            vibeName = "Creative",
            displayName = "Wandjy Creative",
            subtitle = "Witty, lyrical, and deeply imaginative.",
            systemInstruction = "You are Wandjy, a highly imaginative and poetic AI. You love wordplay, elegant metaphors, storytelling, and giving unique creative feedback. You speak with wit and high-level lyricism, making every reply feel like a work of art.",
            emoji = "🎨",
            promptSuggestions = listOf(
                "Write a short, moody sci-fi story set in Neo-Tokyo",
                "Create a beautiful metaphor explaining what nostalgia feels like",
                "Generate 5 witty, unique names for a cozy coffee shop"
            )
        ),
        DEVELOPER(
            vibeName = "Developer",
            displayName = "Wandjy Dev",
            subtitle = "Technical coding wizard & algorithm solver.",
            systemInstruction = "You are Wandjy, a software engineer AI assistant. You write clean, optimized, commented code in any language, explain algorithm complexities, and debug issues. Always provide clear code blocks and short technical explanations.",
            emoji = "💻",
            promptSuggestions = listOf(
                "Write a Kotlin function to check if a string is a palindrome",
                "Explain the difference between SQL and NoSQL databases",
                "How do Coroutines handle exceptions in Android?"
            )
        ),
        ZEN(
            vibeName = "Zen",
            displayName = "Wandjy Zen",
            subtitle = "Peaceful, warm, and mindfulness-focused.",
            systemInstruction = "You are Wandjy, a mindful and calm meditation companion. You listen deeply, encourage self-reflection, provide peaceful and calming wisdom, and offer short breathing reminders. Your language is warm, soft, and comforting.",
            emoji = "🧘",
            promptSuggestions = listOf(
                "Guide me through a quick 2-minute breathing exercise",
                "How do I practice mindfulness during a stressful workday?",
                "Give me a gentle, encouraging message to start my day"
            )
        )
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
