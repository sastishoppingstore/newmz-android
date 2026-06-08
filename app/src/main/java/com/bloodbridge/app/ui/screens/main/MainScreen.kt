package com.bloodbridge.app.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.ui.screens.donors.DonorsScreen
import com.bloodbridge.app.ui.screens.emergency.EmergencyScreen
import com.bloodbridge.app.ui.screens.feed.FeedScreen
import com.bloodbridge.app.ui.screens.profile.ProfileScreen
import com.bloodbridge.app.ui.theme.BloodRed
import com.bloodbridge.app.ui.theme.Gray
import com.bloodbridge.app.ui.theme.White

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun MainScreen(
    repository: BloodBridgeRepository,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToComments: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToEmergencyForm: () -> Unit,
    onNavigateToChat: (Int, String) -> Unit,
    onNavigateToDonorProfile: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onRequireLogin: () -> Unit,
    onLogout: () -> Unit
) {
    val items = listOf(
        BottomNavItem("Feed", Icons.Default.Home, "feed"),
        BottomNavItem("Donors", Icons.Default.People, "donors"),
        BottomNavItem("Emergency", Icons.Default.Warning, "emergency"),
        BottomNavItem("Chat", Icons.Default.Chat, "chat"),
        BottomNavItem("Profile", Icons.Default.Person, "profile")
    )

    var selectedIndex by remember { mutableIntStateOf(0) }
    val isLoggedIn = repository.sessionManager.isLoggedIn

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = White) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            if (!isLoggedIn && item.route in setOf("chat", "profile")) {
                                onRequireLogin()
                            } else {
                                selectedIndex = index
                            }
                        },
                        icon = {
                            Icon(
                                if (selectedIndex == index) item.icon else item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BloodRed,
                            selectedTextColor = BloodRed,
                            unselectedIconColor = Gray,
                            unselectedTextColor = Gray,
                            indicatorColor = BloodRed.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedIndex) {
                0 -> FeedScreen(
                    repository = repository,
                    onNavigateToCreatePost = onNavigateToCreatePost,
                    onNavigateToComments = onNavigateToComments,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToEmergency = onNavigateToEmergencyForm,
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToNotifications = onNavigateToNotifications
                )
                1 -> DonorsScreen(
                    repository = repository,
                    onNavigateToDonorProfile = onNavigateToDonorProfile
                )
                2 -> EmergencyScreen(
                    repository = repository,
                    onNavigateToForm = onNavigateToEmergencyForm
                )
                3 -> com.bloodbridge.app.ui.screens.chat.ChatListScreen(
                    repository = repository,
                    onNavigateToChat = onNavigateToChat
                )
                4 -> ProfileScreen(
                    repository = repository,
                    onLogout = onLogout,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToComments = onNavigateToComments,
                    onNavigateToOtherProfile = onNavigateToProfile
                )
            }
        }
    }
}
