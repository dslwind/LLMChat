package com.llmchat.data.remote

import com.llmchat.data.local.entity.Provider
import com.llmchat.data.remote.dto.ChatRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamingChatClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
    private val sseParser: SSEParser
) {
    fun stream(provider: Provider, request: ChatRequest): Flow<String> = flow {
        val endpoint = provider.baseUrl.trimEnd('/') + "/v1/chat/completions"
        val body = json.encodeToString(request.copy(stream = true))
            .toRequestBody("application/json".toMediaType())
        val httpRequest = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer ${provider.apiKey}")
            .header("Accept", "text/event-stream")
            .post(body)
            .build()
        okHttpClient.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}: ${response.message}")
            val source = response.body?.source() ?: return@use
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                val token = if (line.startsWith("data:")) sseParser.parseDelta(line) else null
                if (token != null) emit(token)
            }
        }
    }.flowOn(Dispatchers.IO)
}
