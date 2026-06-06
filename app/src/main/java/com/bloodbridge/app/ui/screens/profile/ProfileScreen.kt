package com.bloodbridge.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bloodbridge.app.data.api.models.Post
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.components.*
import com.bloodbridge.app.ui.screens.feed.PostCard
import com.bloodbridge.app.ui.theme.*
import com.bloodbridge.app.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: BloodBridgeRepository,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToComments: (Int) -> Unit,
    onNavigateToOtherProfile: (Int) -> Unit
) {
    var userName by remember { mutableStateOf(repository.sessionManager.userName ?: "") }
    var userEmail by remember { mutableStateOf(repository.sessionManager.userEmail ?: "") }
    var userPhoto by remember { mutableStateOf(repository.sessionManager.userPhoto) }
    var userBloodGroup by remember { mutableStateOf("") }
    var userCity by remember { mutableStateOf("") }
    var stats by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = repository.getProfile()
        result.fold(
            onSuccess = { profile ->
                profile.user?.let { u ->
                    userName = u.name
                    userEmail = u.email
                    userPhoto = u.profile_photo
                    userBloodGroup = u.blood_group_id?.toString() ?: ""
                    userCity = u.city_id?.toString() ?: ""
                }
                stats = mapOf(
                    "Posts" to "${profile.stats?.posts_count ?: 0}",
                    "Friends" to "${profile.stats?.friends_count ?: 0}",
                    "Donations" to "${profile.stats?.total_donations ?: 0}",
                    "Saved" to "${profile.stats?.lives_saved ?: 0}"
                )
                posts = profile.posts ?: emptyList()
                isLoading = false
            },
            onFailure = { isLoading = false }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    scope.launch { repository.logout(); onLogout() }
                }) { Text("Logout", color = BloodRed) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, null)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, null, tint = BloodRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingIndicator(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileImage(
                                url = ImageUtils.getProfileUrl(userPhoto),
                                name = userName,
                                size = 80
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(userName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(userEmail, color = Gray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                stats.forEach { (label, value) ->
                                    StatItem(value, label)
                                }
                            }
                        }
                    }
                }

                if (posts.isNotEmpty()) {
                    item {
                        Text("My Posts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    items(posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onLike = { scope.launch { repository.toggleReaction(post.id) } },
                            onComment = { onNavigateToComments(post.id) },
                            onShare = { scope.launch { repository.sharePost(post.id) } },
                            onProfileClick = { onNavigateToOtherProfile(post.user_id) }
                        )
                    }
                }
            }
        }
    }
}
