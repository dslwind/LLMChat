package com.llmchat.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmchat.data.local.entity.Provider
import com.llmchat.domain.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val json: Json
) : ViewModel() {

    val providers: StateFlow<List<Provider>> = providerRepository.observeProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteProvider(provider: Provider) {
        viewModelScope.launch { providerRepository.deleteProvider(provider) }
    }

    fun setDefault(provider: Provider) {
        viewModelScope.launch {
            providerRepository.saveProvider(provider.copy(isDefault = true))
        }
    }
}
