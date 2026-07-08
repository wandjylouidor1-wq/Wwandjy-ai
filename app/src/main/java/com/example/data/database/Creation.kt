package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "creations")
data class Creation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "song", "photo", "video"
    val prompt: String,
    val title: String,
    val style: String,
    val lyrics: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
