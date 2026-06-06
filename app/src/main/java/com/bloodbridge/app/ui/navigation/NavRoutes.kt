package com.bloodbridge.app.ui.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val FEED = "feed"
    const val DONORS = "donors"
    const val EMERGENCY = "emergency"
    const val CHAT_LIST = "chat_list"
    const val CHAT = "chat/{chatId}/{chatName}"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val OTHER_PROFILE = "other_profile/{userId}"
    const val SETTINGS = "settings"
    const val CREATE_POST = "create_post"
    const val COMMENTS = "comments/{postId}"
    const val EMERGENCY_FORM = "emergency_form"
    const val SEARCH = "search"
    const val DONOR_PROFILE = "donor_profile/{donorId}"
    const val SEARCH_SCREEN = "search"

    fun chat(chatId: Int, chatName: String) = "chat/$chatId/$chatName"
    fun comments(postId: Int) = "comments/$postId"
    fun otherProfile(userId: Int) = "other_profile/$userId"
    fun donorProfile(donorId: Int) = "donor_profile/$donorId"
}
