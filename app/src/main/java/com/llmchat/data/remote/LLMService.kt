package com.llmchat.data.remote

import com.llmchat.data.remote.dto.ChatRequest
import com.llmchat.data.remote.dto.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LLMService {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse
}
