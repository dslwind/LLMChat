package com.llmchat.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun AppMarkdownText(text: String, modifier: Modifier = Modifier) {
    if (text.isBlank()) {
        Text("…", modifier = modifier, style = MaterialTheme.typography.bodyLarge)
        return
    }
    Column(modifier = modifier) {
        val lines = text.lines()
        var inCode = false
        val codeLines = mutableListOf<String>()
        lines.forEach { line ->
            if (line.trim().startsWith("```")) {
                if (inCode) {
                    CodeBlock(codeLines.joinToString("\n"))
                    codeLines.clear()
                }
                inCode = !inCode
            } else if (inCode) {
                codeLines += line
            } else {
                MarkdownLine(line)
            }
        }
        if (codeLines.isNotEmpty()) CodeBlock(codeLines.joinToString("\n"))
    }
}

@Composable
private fun MarkdownLine(line: String) {
    val trimmed = line.trimStart()
    val style = when {
        trimmed.startsWith("# ") -> MaterialTheme.typography.headlineSmall
        trimmed.startsWith("## ") -> MaterialTheme.typography.titleLarge
        trimmed.startsWith("### ") -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.bodyLarge
    }
    val content = trimmed
        .removePrefix("### ")
        .removePrefix("## ")
        .removePrefix("# ")
        .let { if (it.startsWith("- ") || it.startsWith("* ")) "• ${it.drop(2)}" else it }
    LinkedRichText(content.ifBlank { " " }, style, Modifier.padding(vertical = 2.dp))
}

@Composable
private fun CodeBlock(code: String) {
    Text(
        text = code,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small).padding(10.dp),
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun LinkedRichText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val annotated = markdownAnnotatedString(text)
    ClickableText(text = annotated, modifier = modifier, style = style.copy(color = MaterialTheme.colorScheme.onSurface)) { offset ->
        annotated.getStringAnnotations("URL", offset, offset).firstOrNull()?.let { uriHandler.openUri(it.item) }
    }
}

private fun markdownAnnotatedString(text: String): AnnotatedString = buildAnnotatedString {
    var index = 0
    val tokenPattern = Regex("(\\*\\*.+?\\*\\*|_.+?_|`.+?`|\\[.+?]\\(.+?\\))")
    tokenPattern.findAll(text).forEach { match ->
        append(text.substring(index, match.range.first))
        val token = match.value
        when {
            token.startsWith("**") -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(token.removeSurrounding("**")) }
            token.startsWith("_") -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(token.removeSurrounding("_")) }
            token.startsWith("`") -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(token.removeSurrounding("`")) }
            token.startsWith("[") -> {
                val label = token.substringAfter("[").substringBefore("]")
                val url = token.substringAfter("](").substringBeforeLast(")")
                pushStringAnnotation("URL", url)
                withStyle(SpanStyle(color = androidx.compose.ui.graphics.Color(0xFF3F6FE5), textDecoration = TextDecoration.Underline)) { append(label) }
                pop()
            }
        }
        index = match.range.last + 1
    }
    append(text.substring(index))
}
