package com.llmchat.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmchat.data.local.entity.Conversation
import com.llmchat.data.local.entity.Message
import com.llmchat.data.local.entity.Provider
import com.llmchat.domain.repository.ConversationRepository
import com.llmchat.domain.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val conversationRepository: ConversationRepository,
    private val providerRepository: ProviderRepository,
    private val json: Json
) : ViewModel() {
    private val conversationId: Long = checkNotNull(savedStateHandle["conversationId"])
    private val isStreaming = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private var streamJob: Job? = null

    val uiState: StateFlow<ChatUiState> = combine(
        conversationRepository.observeConversation(conversationId),
        conversationRepository.observeMessages(conversationId),
        providerRepository.observeProviders(),
        isStreaming,
        error
    ) { conversation, messages, providers, streaming, currentError ->
        ChatUiState(conversation, messages, providers, streaming, currentError)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    fun send(content: String) {
        if (content.isBlank() || isStreaming.value) return
        streamJob = viewModelScope.launch {
            error.value = null
            isStreaming.value = true
            try {
                conversationRepository.sendStreamingMessage(conversationId, content.trim()).collect { }
            } catch (throwable: Throwable) {
                error.value = throwable.message ?: "Generation failed"
            } finally {
                isStreaming.value = false
            }
        }
    }

    fun stop() {
        streamJob?.cancel()
        isStreaming.value = false
    }

    fun selectModel(providerId: String, modelId: String) {
        viewModelScope.launch { conversationRepository.updateModel(conversationId, providerId, modelId) }
    }

    fun modelsFor(provider: Provider): List<String> =
        runCatching { json.decodeFromString<List<String>>(provider.models) }.getOrDefault(emptyList())
}

data class ChatUiState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val providers: List<Provider> = emptyList(),
    val isStreaming: Boolean = false,
    val error: String? = null
)
