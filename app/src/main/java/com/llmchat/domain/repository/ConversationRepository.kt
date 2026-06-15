package com.llmchat.domain.repository

import com.llmchat.data.local.dao.ConversationDao
import com.llmchat.data.local.dao.MessageDao
import com.llmchat.data.local.entity.Conversation
import com.llmchat.data.local.entity.Message
import com.llmchat.data.remote.LLMService
import com.llmchat.data.remote.StreamingChatClient
import com.llmchat.data.remote.dto.ChatMessageDto
import com.llmchat.data.remote.dto.ChatRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val providerRepository: ProviderRepository,
    private val streamingChatClient: StreamingChatClient,
    private val llmService: LLMService
) {
    fun observeConversations(): Flow<List<Conversation>> = conversationDao.observeAll()
    fun observeConversation(id: Long): Flow<Conversation?> = conversationDao.observeById(id)
    fun observeMessages(conversationId: Long): Flow<List<Message>> = messageDao.observeForConversation(conversationId)

    suspend fun createConversation(providerId: String, modelId: String, firstMessage: String? = null): Long {
        val now = System.currentTimeMillis()
        val title = firstMessage?.take(40)?.ifBlank { null } ?: "New conversation"
        return conversationDao.insert(
            Conversation(
                title = title,
                providerId = providerId,
                modelId = modelId,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun deleteConversation(id: Long) = conversationDao.deleteById(id)

    suspend fun renameConversation(id: Long, title: String) {
        val conversation = conversationDao.getById(id) ?: return
        conversationDao.update(conversation.copy(title = title, updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateModel(conversationId: Long, providerId: String, modelId: String) {
        val conversation = conversationDao.getById(conversationId) ?: return
        conversationDao.update(conversation.copy(providerId = providerId, modelId = modelId, updatedAt = System.currentTimeMillis()))
    }

    suspend fun addMessage(message: Message): Long = messageDao.insert(message)

    suspend fun sendStreamingMessage(conversationId: Long, content: String): Flow<String> {
        val conversation = conversationDao.getById(conversationId) ?: error("Conversation not found")
        val provider = providerRepository.getProvider(conversation.providerId) ?: error("Provider not found")
        val now = System.currentTimeMillis()
        messageDao.insert(Message(conversationId = conversationId, role = "user", content = content, createdAt = now))
        val assistantId = messageDao.insert(
            Message(conversationId = conversationId, role = "assistant", content = "", isStreaming = true, createdAt = now + 1)
        )
        val messages = messageDao.getForConversation(conversationId)
            .filterNot { it.id == assistantId && it.content.isBlank() }
            .map { ChatMessageDto(role = it.role, content = it.content) }
        val request = ChatRequest(model = conversation.modelId, messages = messages, stream = true, temperature = 0.7)
        var accumulated = ""
        return kotlinx.coroutines.flow.flow {
            try {
                streamingChatClient.stream(provider, request).collect { token ->
                    accumulated += token
                    messageDao.updateContent(assistantId, accumulated, true)
                    emit(token)
                }
                messageDao.updateContent(assistantId, accumulated, false)
                conversationDao.update(conversation.copy(updatedAt = System.currentTimeMillis()))
            } catch (throwable: Throwable) {
                val errorText = accumulated.ifBlank { "Error: ${throwable.message ?: "generation failed"}" }
                messageDao.updateContent(assistantId, errorText, false)
                throw throwable
            }
        }
    }

    suspend fun sendNonStreamingMessage(conversationId: Long, content: String): String {
        val conversation = conversationDao.getById(conversationId) ?: error("Conversation not found")
        val provider = providerRepository.getProvider(conversation.providerId) ?: error("Provider not found")
        val messages = messageDao.getForConversation(conversationId).map { ChatMessageDto(it.role, it.content) } + ChatMessageDto("user", content)
        val response = llmService.chatCompletion(
            authorization = "Bearer ${provider.apiKey}",
            request = ChatRequest(model = conversation.modelId, messages = messages, stream = false, temperature = 0.7)
        )
        return response.choices.firstOrNull()?.message?.content.orEmpty()
    }
}
