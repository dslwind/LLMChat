package com.llmchat.domain.model

import com.llmchat.data.local.entity.Conversation
import com.llmchat.data.local.entity.Message

data class ConversationWithMessages(
    val conversation: Conversation?,
    val messages: List<Message>
)
