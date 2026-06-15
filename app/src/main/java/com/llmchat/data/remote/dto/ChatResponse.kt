package com.llmchat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val id: String? = null,
    val choices: List<ChatChoice> = emptyList()
)

@Serializable
data class ChatChoice(
    val message: ChatMessageDto? = null,
    val delta: ChatDelta? = null,
    val index: Int = 0,
    val finish_reason: String? = null
)

@Serializable
data class ChatDelta(
    val role: String? = null,
    val content: String? = null
)
