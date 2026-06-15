package com.llmchat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val providerId: String,
    val modelId: String,
    val systemPrompt: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
