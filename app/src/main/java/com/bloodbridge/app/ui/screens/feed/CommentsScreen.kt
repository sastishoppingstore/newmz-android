package com.bloodbridge.app.ui.screens.feed

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
import com.bloodbridge.app.data.api.models.Comment
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.components.*
import com.bloodbridge.app.ui.theme.*
import com.bloodbridge.app.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    postId: Int,
    repository: BloodBridgeRepository,
    onBack: () -> Unit
) {
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var commentText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadComments() {
        scope.launch {
            isLoading = true
            val result = repository.getComments(postId)
            result.fold(
                onSuccess = { comments = it; isLoading = false },
                onFailure = { error = it.message; isLoading = false }
            )
        }
    }

    LaunchedEffect(Unit) { loadComments() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comments", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = White) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Write a comment...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                scope.launch {
                                    repository.addComment(postId, commentText.trim())
                                    commentText = ""
                                    loadComments()
                                }
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = BloodRed)
                    ) { Icon(Icons.Default.Send, null, tint = White) }
                }
            }
        }
    ) { padding ->
        when {
            isLoading -> LoadingIndicator(Modifier.padding(padding))
            error != null -> ErrorMessage(error, modifier = Modifier.padding(padding))
            comments.isEmpty() -> ErrorMessage("No comments yet", modifier = Modifier.padding(padding))
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GrayLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                ProfileImage(
                                    url = ImageUtils.getProfileUrl(comment.profile_photo),
                                    name = comment.name,
                                    size = 36
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(comment.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(comment.content, fontSize = 14.sp)
                                    Text(
                                        ImageUtils.timeAgo(comment.created_at),
                                        color = Gray, fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
