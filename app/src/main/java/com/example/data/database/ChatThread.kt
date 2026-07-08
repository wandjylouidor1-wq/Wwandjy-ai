package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_threads")
data class ChatThread(
    @PrimaryKey val id: String,
    val title: String,
    val vibe: String,
    val timestamp: Long = System.currentTimeMillis()
)
