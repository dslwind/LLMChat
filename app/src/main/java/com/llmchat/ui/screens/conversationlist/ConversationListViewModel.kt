package com.llmchat.ui.screens.conversationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmchat.data.local.entity.Conversation
import com.llmchat.domain.repository.ConversationRepository
import com.llmchat.domain.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val providerRepository: ProviderRepository,
    private val json: Json
) : ViewModel() {
    private val creating = MutableStateFlow(false)
    val uiState: StateFlow<ConversationListUiState> = combine(
        conversationRepository.observeConversations(),
        creating
    ) { conversations, isCreating -> ConversationListUiState(conversations, isCreating) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConversationListUiState())

    init {
        viewModelScope.launch { providerRepository.ensureDefaultProvider() }
    }

    fun createConversation(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            creating.value = true
            val provider = providerRepository.getDefaultProvider() ?: run {
                providerRepository.ensureDefaultProvider()
                providerRepository.getDefaultProvider()
            }
            if (provider != null) {
                val model = runCatching { json.decodeFromString<List<String>>(provider.models).firstOrNull() }.getOrNull() ?: "gpt-4o-mini"
                onCreated(conversationRepository.createConversation(provider.id, model))
            }
            creating.value = false
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch { conversationRepository.deleteConversation(id) }
    }

    fun renameConversation(id: Long, title: String) {
        viewModelScope.launch { conversationRepository.renameConversation(id, title) }
    }
}

data class ConversationListUiState(
    val conversations: List<Conversation> = emptyList(),
    val isCreating: Boolean = false
)
