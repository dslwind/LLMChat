package com.llmchat.ui.screens.conversationlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.llmchat.data.local.entity.Conversation
import java.text.DateFormat
import java.util.Date

@Composable
fun ConversationListScreen(
    onOpenConversation: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ConversationListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var renameTarget by remember { mutableStateOf<Conversation?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LLM Chat") },
                actions = { TextButton(onClick = onOpenSettings) { Text("Settings") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.createConversation(onOpenConversation) }) { Text("+") }
        }
    ) { padding ->
        if (uiState.conversations.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No conversations yet", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { viewModel.createConversation(onOpenConversation) }) { Text("Start chatting") }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(uiState.conversations, key = { it.id }) { conversation ->
                    ConversationRow(
                        conversation = conversation,
                        onOpen = { onOpenConversation(conversation.id) },
                        onDelete = { viewModel.deleteConversation(conversation.id) },
                        onRename = { renameTarget = conversation }
                    )
                }
            }
        }
    }

    renameTarget?.let { conversation ->
        RenameDialog(
            initialTitle = conversation.title,
            onDismiss = { renameTarget = null },
            onConfirm = { title ->
                viewModel.renameConversation(conversation.id, title)
                renameTarget = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationRow(conversation: Conversation, onOpen: () -> Unit, onDelete: () -> Unit, onRename: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it != SwipeToDismissBoxValue.Settled) onDelete()
        true
    })
    SwipeToDismissBox(state = dismissState, backgroundContent = {
        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.CenterEnd) { Text("Delete") }
    }) {
        Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).clickable(onClick = onOpen)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(conversation.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                    Text(DateFormat.getDateTimeInstance().format(Date(conversation.updatedAt)), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onRename) { Text("Rename") }
            }
        }
    }
}

@Composable
private fun RenameDialog(initialTitle: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename conversation") },
        text = { OutlinedTextField(value = title, onValueChange = { title = it }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onConfirm(title.trim().ifBlank { initialTitle }) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
