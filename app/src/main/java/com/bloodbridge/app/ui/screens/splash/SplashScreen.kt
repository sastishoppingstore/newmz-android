package com.bloodbridge.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.theme.BloodRed
import com.bloodbridge.app.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    repository: BloodBridgeRepository,
    onNavigate: (Boolean) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        repository.initSession()
        delay(1500)
        onNavigate(repository.sessionManager.isLoggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BloodRed),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .background(White.copy(alpha = 0.2f), shape = MaterialTheme.shapes.extraLarge),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MZ",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "MZ Blood Bridge",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Text(
                text = "Save Lives, Donate Blood",
                fontSize = 14.sp,
                color = White.copy(alpha = 0.8f),
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}
