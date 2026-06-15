package com.llmchat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.llmchat.ui.screens.chat.ChatScreen
import com.llmchat.ui.screens.conversationlist.ConversationListScreen
import com.llmchat.ui.screens.settings.ProviderEditScreen
import com.llmchat.ui.screens.settings.SettingsScreen

object Routes {
    const val Conversations = "conversations"
    const val Settings = "settings"
    const val ProviderEdit = "providerEdit?providerId={providerId}"
    const val Chat = "chat/{conversationId}"
}

@Composable
fun LLMChatNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Conversations) {
        composable(Routes.Conversations) {
            ConversationListScreen(
                onOpenConversation = { navController.navigate("chat/$it") },
                onOpenSettings = { navController.navigate(Routes.Settings) }
            )
        }
        composable(
            route = Routes.Chat,
            arguments = listOf(navArgument("conversationId") { type = NavType.LongType })
        ) { entry ->
            ChatScreen(
                conversationId = entry.arguments?.getLong("conversationId") ?: 0L,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onEditProvider = { id -> navController.navigate("providerEdit?providerId=${id.orEmpty()}") }
            )
        }
        composable(
            route = Routes.ProviderEdit,
            arguments = listOf(navArgument("providerId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) {
            ProviderEditScreen(onBack = { navController.popBackStack() })
        }
    }
}
