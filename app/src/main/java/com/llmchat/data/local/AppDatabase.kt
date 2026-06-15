package com.llmchat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.llmchat.data.local.dao.ConversationDao
import com.llmchat.data.local.dao.MessageDao
import com.llmchat.data.local.dao.ProviderDao
import com.llmchat.data.local.entity.Conversation
import com.llmchat.data.local.entity.Message
import com.llmchat.data.local.entity.Provider

@Database(
    entities = [Conversation::class, Message::class, Provider::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun providerDao(): ProviderDao
}
