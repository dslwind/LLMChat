package com.llmchat.di

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.llmchat.data.remote.LLMService
import com.llmchat.data.remote.SSEParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    fun provideSseParser(json: Json): SSEParser = SSEParser(json)

    @Provides
    @Singleton
    fun provideLlmService(okHttpClient: OkHttpClient, json: Json): LLMService = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(LLMService::class.java)
}
