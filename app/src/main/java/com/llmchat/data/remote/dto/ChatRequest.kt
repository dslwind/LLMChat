package com.llmchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessageDto>,
    val stream: Boolean = true,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    val temperature: Double? = null
)

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: String
)
