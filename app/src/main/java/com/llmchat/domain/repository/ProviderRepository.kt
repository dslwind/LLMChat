package com.llmchat.domain.repository

import com.llmchat.data.local.dao.ProviderDao
import com.llmchat.data.local.entity.Provider
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderRepository @Inject constructor(
    private val providerDao: ProviderDao,
    private val json: Json
) {
    fun observeProviders(): Flow<List<Provider>> = providerDao.observeAll()
    fun observeProvider(id: String): Flow<Provider?> = providerDao.observeById(id)
    suspend fun getProvider(id: String): Provider? = providerDao.getById(id)
    suspend fun getDefaultProvider(): Provider? = providerDao.getDefault()

    suspend fun saveProvider(provider: Provider) {
        if (provider.isDefault) providerDao.clearDefault()
        providerDao.insert(provider)
    }

    suspend fun deleteProvider(provider: Provider) = providerDao.delete(provider)

    suspend fun ensureDefaultProvider() {
        if (providerDao.getDefault() == null) {
            providerDao.insert(
                Provider(
                    name = "OpenAI Compatible",
                    baseUrl = "https://api.openai.com",
                    apiKey = "",
                    models = json.encodeToString(listOf("gpt-4o-mini", "gpt-4o")),
                    isDefault = true
                )
            )
        }
    }
}
