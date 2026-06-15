package com.llmchat.data.remote

import com.llmchat.data.remote.dto.ChatResponse
import kotlinx.serialization.json.Json

class SSEParser(private val json: Json) {
    fun parseDelta(line: String): String? {
        val payload = line.removePrefix("data:").trim()
        if (payload.isBlank() || payload == "[DONE]") return null
        return runCatching {
            json.decodeFromString<ChatResponse>(payload).choices.firstOrNull()?.delta?.content
        }.getOrNull()
    }
}
