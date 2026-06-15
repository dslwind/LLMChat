package com.llmchat.ui.screens.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmchat.data.local.entity.Provider
import com.llmchat.domain.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ProviderEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val providerRepository: ProviderRepository,
    private val json: Json
) : ViewModel() {

    private val providerId: String? = savedStateHandle["providerId"]

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _baseUrl = MutableStateFlow("")
    val baseUrl: StateFlow<String> = _baseUrl

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey

    private val _models = MutableStateFlow("")
    val models: StateFlow<String> = _models

    private val _isDefault = MutableStateFlow(false)
    val isDefault: StateFlow<Boolean> = _isDefault

    private val _isEdit = MutableStateFlow(false)
    val isEdit: StateFlow<Boolean> = _isEdit

    init {
        if (providerId != null) {
            viewModelScope.launch {
                providerRepository.getProvider(providerId)?.let { provider ->
                    _name.value = provider.name
                    _baseUrl.value = provider.baseUrl
                    _apiKey.value = provider.apiKey
                    _models.value = runCatching {
                        json.decodeFromString<List<String>>(provider.models).joinToString(", ")
                    }.getOrDefault(provider.models)
                    _isDefault.value = provider.isDefault
                    _isEdit.value = true
                }
            }
        }
    }

    fun updateName(value: String) { _name.value = value }
    fun updateBaseUrl(value: String) { _baseUrl.value = value }
    fun updateApiKey(value: String) { _apiKey.value = value }
    fun updateModels(value: String) { _models.value = value }
    fun updateIsDefault(value: Boolean) { _isDefault.value = value }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            val modelsList = _models.value.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val provider = Provider(
                id = providerId ?: java.util.UUID.randomUUID().toString(),
                name = _name.value.ifBlank { "Unnamed" },
                baseUrl = _baseUrl.value.trimEnd('/'),
                apiKey = _apiKey.value,
                models = json.encodeToString(modelsList),
                isDefault = _isDefault.value
            )
            providerRepository.saveProvider(provider)
            onDone()
        }
    }
}
