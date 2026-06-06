package com.bloodbridge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.bloodbridge.app.ui.theme.BloodRed
import com.bloodbridge.app.ui.theme.Gray

@Composable
fun ProfileImage(
    url: String?,
    name: String?,
    size: Int = 48,
    modifier: Modifier = Modifier
) {
    if (!url.isNullOrEmpty()) {
        AsyncImage(
            model = url,
            contentDescription = name ?: "Profile",
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(BloodRed.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = BloodRed,
                modifier = Modifier.size((size * 0.6).dp)
            )
        }
    }
}

@Composable
fun BloodGroupBadge(
    bloodGroup: String?,
    modifier: Modifier = Modifier
) {
    if (!bloodGroup.isNullOrEmpty()) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(4.dp),
            color = BloodRed.copy(alpha = 0.15f)
        ) {
            Text(
                text = bloodGroup,
                color = BloodRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = BloodRed)
    }
}

@Composable
fun ErrorMessage(
    message: String?,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    if (message != null) {
        Column(
            modifier = modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = message,
                color = Gray,
                style = MaterialTheme.typography.bodyLarge
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?) {
    if (!value.isNullOrEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray,
                modifier = Modifier.weight(0.4f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = BloodRed
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Gray
        )
    }
}
