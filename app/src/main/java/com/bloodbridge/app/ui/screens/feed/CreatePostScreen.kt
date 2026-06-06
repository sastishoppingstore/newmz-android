package com.bloodbridge.app.ui.screens.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    repository: BloodBridgeRepository,
    onBack: () -> Unit,
    onPostCreated: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var isEmergency by remember { mutableStateOf(false) }
    var hospitalName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submitPost() {
        if (content.isBlank()) {
            errorMsg = "Please write something"
            return
        }
        isLoading = true
        errorMsg = null
        scope.launch {
            val result = repository.createPost(
                content = content.trim(),
                type = if (isEmergency) "emergency" else "text",
                isEmergency = if (isEmergency) 1 else 0,
                hospitalName = hospitalName.ifBlank { null }
            )
            isLoading = false
            result.fold(
                onSuccess = { onPostCreated() },
                onFailure = { errorMsg = it.message }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it; errorMsg = null },
                placeholder = { Text("What's on your mind?") },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed)
            )

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = isEmergency,
                    onCheckedChange = { isEmergency = it },
                    colors = CheckboxDefaults.colors(checkedColor = BloodRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("This is an emergency", fontWeight = FontWeight.Bold, color = if (isEmergency) BloodRed else GrayDark)
            }

            if (isEmergency) {
                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    label = { Text("Hospital Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.LocalHospital, null, tint = BloodRed) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloodRed)
                )
            }

            if (errorMsg != null) {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { submitPost() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Post", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
