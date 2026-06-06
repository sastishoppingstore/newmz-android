package com.bloodbridge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bloodbridge.app.data.repository.BloodBridgeRepository
import com.bloodbridge.app.data.session.SessionManager
import com.bloodbridge.app.ui.navigation.NavRoutes
import com.bloodbridge.app.ui.screens.chat.ChatScreen
import com.bloodbridge.app.ui.screens.donors.DonorProfileScreen
import com.bloodbridge.app.ui.screens.emergency.EmergencyFormScreen
import com.bloodbridge.app.ui.screens.feed.CommentsScreen
import com.bloodbridge.app.ui.screens.feed.CreatePostScreen
import com.bloodbridge.app.ui.screens.login.LoginScreen
import com.bloodbridge.app.ui.screens.main.MainScreen
import com.bloodbridge.app.ui.screens.notifications.NotificationsScreen
import com.bloodbridge.app.ui.screens.profile.OtherProfileScreen
import com.bloodbridge.app.ui.screens.register.RegisterScreen
import com.bloodbridge.app.ui.screens.search.SearchScreen
import com.bloodbridge.app.ui.screens.settings.SettingsScreen
import com.bloodbridge.app.ui.screens.splash.SplashScreen
import com.bloodbridge.app.ui.theme.MZBloodBridgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(applicationContext)
        val repository = BloodBridgeRepository(sessionManager)

        setContent {
            MZBloodBridgeTheme {
                AppNavigation(repository)
            }
        }
    }
}

@Composable
fun AppNavigation(repository: BloodBridgeRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                repository = repository,
                onNavigate = { isLoggedIn ->
                    if (isLoggedIn) {
                        navController.navigate(NavRoutes.MAIN) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(NavRoutes.LOGIN) {
            LoginScreen(
                repository = repository,
                onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) },
                onLoginSuccess = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                repository = repository,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.MAIN) {
            MainScreen(
                repository = repository,
                onNavigateToCreatePost = { navController.navigate(NavRoutes.CREATE_POST) },
                onNavigateToComments = { postId -> navController.navigate(NavRoutes.comments(postId)) },
                onNavigateToProfile = { userId -> navController.navigate(NavRoutes.otherProfile(userId)) },
                onNavigateToEmergencyForm = { navController.navigate(NavRoutes.EMERGENCY_FORM) },
                onNavigateToChat = { chatId, chatName ->
                    navController.navigate(NavRoutes.chat(chatId, chatName))
                },
                onNavigateToDonorProfile = { donorId ->
                    navController.navigate(NavRoutes.donorProfile(donorId))
                },
                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
                onNavigateToNotifications = { navController.navigate(NavRoutes.NOTIFICATIONS) },
                onNavigateToSearch = { navController.navigate(NavRoutes.SEARCH_SCREEN) },
                onLogout = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.CREATE_POST) {
            CreatePostScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onPostCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.COMMENTS,
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: return@composable
            CommentsScreen(
                postId = postId,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.EMERGENCY_FORM) {
            EmergencyFormScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.CHAT,
            arguments = listOf(
                navArgument("chatId") { type = NavType.IntType },
                navArgument("chatName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getInt("chatId") ?: return@composable
            val chatName = backStackEntry.arguments?.getString("chatName") ?: "Chat"
            ChatScreen(
                chatId = chatId,
                chatName = chatName,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.OTHER_PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable
            OtherProfileScreen(
                userId = userId,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.DONOR_PROFILE,
            arguments = listOf(navArgument("donorId") { type = NavType.IntType })
        ) { backStackEntry ->
            val donorId = backStackEntry.arguments?.getInt("donorId") ?: return@composable
            DonorProfileScreen(
                donorId = donorId,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.SEARCH_SCREEN) {
            SearchScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
