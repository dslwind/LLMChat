package com.llmchat.di

import android.content.Context
import androidx.room.Room
import com.llmchat.data.local.AppDatabase
import com.llmchat.data.local.dao.ConversationDao
import com.llmchat.data.local.dao.MessageDao
import com.llmchat.data.local.dao.ProviderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "llmchat.db").build()

    @Provides fun provideConversationDao(database: AppDatabase): ConversationDao = database.conversationDao()
    @Provides fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()
    @Provides fun provideProviderDao(database: AppDatabase): ProviderDao = database.providerDao()
}
