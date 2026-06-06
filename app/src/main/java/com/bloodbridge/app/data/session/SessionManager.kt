package com.bloodbridge.app.data.session

import android.content.Context
import android.content.SharedPreferences
import com.bloodbridge.app.data.api.models.User

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("blood_bridge_prefs", Context.MODE_PRIVATE)

    var csrfToken: String?
        get() = prefs.getString(KEY_CSRF, null)
        set(value) = prefs.edit().putString(KEY_CSRF, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()

    var userId: Int
        get() = prefs.getInt(KEY_USER_ID, 0)
        set(value) = prefs.edit().putInt(KEY_USER_ID, value).apply()

    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var userPhone: String?
        get() = prefs.getString(KEY_USER_PHONE, null)
        set(value) = prefs.edit().putString(KEY_USER_PHONE, value).apply()

    var userPhoto: String?
        get() = prefs.getString(KEY_USER_PHOTO, null)
        set(value) = prefs.edit().putString(KEY_USER_PHOTO, value).apply()

    var userRole: String?
        get() = prefs.getString(KEY_USER_ROLE, null)
        set(value) = prefs.edit().putString(KEY_USER_ROLE, value).apply()

    var userBloodGroup: String?
        get() = prefs.getString(KEY_BLOOD_GROUP, null)
        set(value) = prefs.edit().putString(KEY_BLOOD_GROUP, value).apply()

    var userCity: String?
        get() = prefs.getString(KEY_CITY, null)
        set(value) = prefs.edit().putString(KEY_CITY, value).apply()

    fun saveUser(user: User) {
        userId = user.id
        userName = user.name
        userEmail = user.email
        userPhone = user.phone
        userPhoto = user.profile_photo
        userRole = user.role
        isLoggedIn = true
    }

    fun getUser(): User? {
        if (!isLoggedIn) return null
        return User(
            id = userId,
            name = userName ?: "",
            email = userEmail ?: "",
            phone = userPhone,
            profile_photo = userPhoto,
            role = userRole ?: "user"
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_CSRF = "csrf_token"
        private const val KEY_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_PHOTO = "user_photo"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_BLOOD_GROUP = "blood_group"
        private const val KEY_CITY = "city"
    }
}
