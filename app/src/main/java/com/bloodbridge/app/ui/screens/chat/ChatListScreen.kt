package com.bloodbridge.app.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodbridge.app.data.api.models.Chat
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.components.*
import com.bloodbridge.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    repository: BloodBridgeRepository,
    onNavigateToChat: (Int, String) -> Unit
) {
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadChats() {
        scope.launch {
            isLoading = true
            val result = repository.fetchChats()
            result.fold(
                onSuccess = { chats = it; isLoading = false },
                onFailure = { error = it.message; isLoading = false }
            )
        }
    }

    LaunchedEffect(Unit) { loadChats() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        when {
            isLoading -> LoadingIndicator(Modifier.padding(padding))
            error != null -> ErrorMessage(error, onRetry = { loadChats() }, modifier = Modifier.padding(padding))
            chats.isEmpty() -> ErrorMessage("No conversations yet", modifier = Modifier.padding(padding))
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(chats, key = { it.id }) { chat ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                onNavigateToChat(chat.id, chat.other_user_name ?: "Chat")
                            },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileImage(
                                    url = null,
                                    name = chat.other_user_name,
                                    size = 52
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        chat.other_user_name ?: "Unknown",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (!chat.last_message.isNullOrEmpty()) {
                                        Text(
                                            chat.last_message!!,
                                            color = Gray,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
