package com.llmchat.ui.screens.chat

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.llmchat.data.local.entity.Provider

@Composable
fun ModelSelector(
    providers: List<Provider>,
    selectedProviderId: String?,
    selectedModelId: String?,
    modelsFor: (Provider) -> List<String>,
    onSelected: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedProvider = providers.firstOrNull { it.id == selectedProviderId }
    val label = listOfNotNull(selectedProvider?.name, selectedModelId).joinToString(" / ").ifBlank { "Model" }
    TextButton(onClick = { expanded = true }) { Text(label) }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        providers.forEach { provider ->
            modelsFor(provider).ifEmpty { listOf("gpt-4o-mini") }.forEach { model ->
                DropdownMenuItem(
                    text = { Text("${provider.name} / $model") },
                    onClick = {
                        expanded = false
                        onSelected(provider.id, model)
                    }
                )
            }
        }
    }
}
