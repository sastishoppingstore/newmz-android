package com.bloodbridge.app.ui.screens.notifications

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodbridge.app.data.api.models.Notification
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.components.*
import com.bloodbridge.app.ui.theme.*
import com.bloodbridge.app.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    repository: BloodBridgeRepository,
    onBack: () -> Unit
) {
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = repository.getNotifications()
        result.fold(
            onSuccess = { notifications = it; isLoading = false },
            onFailure = { error = it.message; isLoading = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        when {
            isLoading -> LoadingIndicator(Modifier.padding(padding))
            error != null -> ErrorMessage(error, modifier = Modifier.padding(padding))
            notifications.isEmpty() -> ErrorMessage("No notifications", modifier = Modifier.padding(padding))
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications, key = { it.id }) { notification ->
                        NotificationCard(notification)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification) {
    val icon = when (notification.type) {
        "comment" -> Icons.Default.ChatBubbleOutline
        "like", "reaction" -> Icons.Default.FavoriteBorder
        "donor_request" -> Icons.Default.Bloodtype
        "friend_request" -> Icons.Default.PersonAdd
        "referral_joined" -> Icons.Default.CardGiftcard
        "share" -> Icons.Default.Share
        "emergency" -> Icons.Default.Warning
        else -> Icons.Default.Notifications
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.is_read == 0) BloodRed.copy(alpha = 0.05f) else White
        ),
        elevation = CardDefaults.cardElevation(if (notification.is_read == 0) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon, null,
                tint = if (notification.is_read == 0) BloodRed else Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notification.title,
                    fontWeight = if (notification.is_read == 0) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
                Text(
                    notification.message,
                    color = Gray,
                    fontSize = 13.sp
                )
                Text(
                    ImageUtils.timeAgo(notification.created_at),
                    color = Gray.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
