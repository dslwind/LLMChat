package com.llmchat.ui.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChatScreen(
    conversationId: Long,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.content) {
        if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(uiState.messages.lastIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.conversation?.title ?: "Chat") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
                actions = {
                    ModelSelector(
                        providers = uiState.providers,
                        selectedProviderId = uiState.conversation?.providerId,
                        selectedModelId = uiState.conversation?.modelId,
                        modelsFor = viewModel::modelsFor,
                        onSelected = viewModel::selectModel
                    )
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            LazyColumn(Modifier.weight(1f), state = listState) {
                items(uiState.messages, key = { it.id }) { message -> MessageBubble(message) }
            }
            uiState.error?.let { Text(it, Modifier.padding(horizontal = 16.dp), color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            Row(Modifier.fillMaxWidth().padding(12.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message") },
                    minLines = 1,
                    maxLines = 5
                )
                if (uiState.isStreaming) {
                    Button(onClick = viewModel::stop, Modifier.padding(start = 8.dp)) { Text("Stop") }
                } else {
                    Button(
                        onClick = {
                            val text = input
                            input = ""
                            viewModel.send(text)
                        },
                        modifier = Modifier.padding(start = 8.dp),
                        enabled = input.isNotBlank()
                    ) { Text("Send") }
                }
            }
        }
    }
}
