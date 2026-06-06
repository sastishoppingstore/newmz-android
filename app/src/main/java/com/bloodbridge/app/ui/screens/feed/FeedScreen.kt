package com.bloodbridge.app.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bloodbridge.app.data.api.models.Post
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.components.*
import com.bloodbridge.app.ui.theme.*
import com.bloodbridge.app.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    repository: BloodBridgeRepository,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToComments: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToEmergency: () -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun loadPosts() {
        scope.launch {
            val result = repository.getFeed()
            result.fold(
                onSuccess = { posts = it; isLoading = false; isRefreshing = false },
                onFailure = { error = it.message; isLoading = false; isRefreshing = false }
            )
        }
    }

    LaunchedEffect(Unit) { loadPosts() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MZ Blood Bridge", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, "Search", tint = GrayDark)
                    }
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, "Notifications", tint = GrayDark)
                    }
                    IconButton(onClick = onNavigateToEmergency) {
                        Icon(Icons.Default.Warning, "Emergency", tint = BloodRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = BloodRed
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> LoadingIndicator()
                error != null -> ErrorMessage(error, onRetry = { isLoading = true; loadPosts() })
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                onClick = onNavigateToCreatePost,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = GrayLight)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ProfileImage(
                                        url = ImageUtils.getProfileUrl(repository.sessionManager.userPhoto),
                                        name = repository.sessionManager.userName,
                                        size = 40
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("What's on your mind?", color = Gray, fontSize = 15.sp)
                                }
                            }
                        }

                        items(posts, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                onLike = {
                                    scope.launch {
                                        repository.toggleReaction(post.id)
                                        loadPosts()
                                    }
                                },
                                onComment = { onNavigateToComments(post.id) },
                                onShare = {
                                    scope.launch { repository.sharePost(post.id) }
                                },
                                onProfileClick = { onNavigateToProfile(post.user_id) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileImage(
                    url = ImageUtils.getProfileUrl(post.profile_photo),
                    name = post.name,
                    size = 40,
                    modifier = Modifier.clickable { onProfileClick() }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (post.is_verified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, null, tint = BloodRed, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(ImageUtils.timeAgo(post.created_at), color = Gray, fontSize = 12.sp)
                }
                BloodGroupBadge(post.user_blood_group)
            }

            if (post.content.isNotEmpty()) {
                Text(
                    text = post.content,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 14.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!post.media_url.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageUtils.getPostMediaUrl(post.media_url),
                    contentDescription = "Post media",
                    modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(0.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (post.is_emergency == 1) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    color = BloodRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = BloodRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Emergency", color = BloodRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onLike() }) {
                    Icon(if (post.user_reaction != null) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null, tint = if (post.user_reaction != null) BloodRed else Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.reaction_count}", color = Gray, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onComment() }) {
                    Icon(Icons.Default.ChatBubbleOutline, null, tint = Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.comment_count}", color = Gray, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onShare() }) {
                    Icon(Icons.Default.Share, null, tint = Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.share_count}", color = Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.then(
        androidx.compose.foundation.clickable(onClick = onClick)
    )
} 
