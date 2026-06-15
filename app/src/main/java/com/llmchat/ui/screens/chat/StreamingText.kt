package com.llmchat.ui.screens.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun StreamingText(text: String, modifier: Modifier = Modifier) {
    AppMarkdownText(text = if (text.isBlank()) "▌" else "$text▌", modifier = modifier)
}
