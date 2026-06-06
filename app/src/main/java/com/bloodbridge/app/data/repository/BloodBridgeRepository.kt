package com.bloodbridge.app.data.repository

import com.bloodbridge.app.data.api.ApiClient
import com.bloodbridge.app.data.api.models.*
import com.bloodbridge.app.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.File

class BloodBridgeRepository(
    val sessionManager: SessionManager
) {
    private val json = Json { ignoreUnknownKeys = true }

    init {
        ApiClient.sessionManager = sessionManager
    }

    suspend fun initSession() {
        withContext(Dispatchers.IO) {
            ApiClient.initSession()
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.login(email, password)
            result.map { jsonObj ->
                val user = json.decodeFromString<User>(jsonObj.getJSONObject("user").toString())
                sessionManager.saveUser(user)
                user
            }
        }
    }

    suspend fun register(
        name: String, email: String, password: String, confirmPassword: String,
        phone: String, bloodGroup: String, city: String, cnic: String,
        gender: String, dob: String, referralCode: String = ""
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.register(name, email, password, confirmPassword, phone,
                bloodGroup, city, cnic, gender, dob, referralCode)
            result.map { jsonObj ->
                val user = json.decodeFromString<User>(jsonObj.getJSONObject("user").toString())
                sessionManager.saveUser(user)
                user
            }
        }
    }

    suspend fun logout(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            ApiClient.logout()
        }
    }

    suspend fun getProfile(): Result<ProfileResponse> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.getProfile()
            result.map { jsonObj ->
                val user = if (jsonObj.has("user") && !jsonObj.isNull("user"))
                    json.decodeFromString(jsonObj.getJSONObject("user").toString()) else null
                val stats = if (jsonObj.has("stats") && !jsonObj.isNull("stats"))
                    json.decodeFromString(jsonObj.getJSONObject("stats").toString()) else null
                val postsArray = jsonObj.optJSONArray("posts")
                val posts = if (postsArray != null) {
                    (0 until postsArray.length()).map {
                        json.decodeFromString<Post>(postsArray.getJSONObject(it).toString())
                    }
                } else emptyList()

                ProfileResponse(success = true, user = user, stats = stats, posts = posts)
            }
        }
    }

    suspend fun getFeed(offset: Int = 0, limit: Int = 20, city: Int = 0, bloodGroup: Int = 0): Result<List<Post>> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.getFeed(offset, limit, city, bloodGroup)
            result.map { body ->
                val response = json.decodeFromString<FeedResponse>(body)
                response.posts
            }
        }
    }

    suspend fun createPost(
        content: String, type: String = "text",
        bloodGroup: String? = null, city: String? = null,
        hospitalName: String? = null, urgency: String = "low",
        isEmergency: Int = 0, mediaFile: File? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.createPost(content, type, bloodGroup, city, hospitalName, urgency, isEmergency, mediaFile)
            result.map { it.optBoolean("success") }
        }
    }

    suspend fun submitEmergencyRequest(
        patientName: String, bloodGroup: Int, city: String, hospital: String,
        units: Int, urgency: String, contact: String, attendantName: String,
        doctorNote: String = "", reportFile: File? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.submitEmergencyRequest(patientName, bloodGroup, city, hospital,
                units, urgency, contact, attendantName, doctorNote, reportFile)
            result.map { it.optBoolean("success") }
        }
    }

    suspend fun getEmergencyRequests(cityId: Int = 0): Result<List<EmergencyRequest>> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.getEmergencyRequests(cityId)
            result.map { body ->
                val response = json.decodeFromString<EmergencyListResponse>(body)
                response.requests
            }
        }
    }

    suspend fun fetchChats(): Result<List<Chat>> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.fetchChats()
            result.map { body ->
                val response = json.decodeFromString<ChatListResponse>(body)
                response.chats
            }
        }
    }

    suspend fun fetchMessages(chatId: Int): Result<List<Message>> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.fetchMessages(chatId)
            result.map { body ->
                val response = json.decodeFromString<ChatMessagesResponse>(body)
                response.messages
            }
        }
    }

    suspend fun sendMessage(chatId: Int, message: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            ApiClient.sendMessage(chatId, message).map { it.optBoolean("success") }
        }
    }

    suspend fun toggleReaction(postId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            ApiClient.toggleReaction(postId).map { it.optBoolean("success") }
        }
    }

    suspend fun getComments(postId: Int): Result<List<Comment>> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.getComments(postId)
            result.map { body ->
                val response = json.decodeFromString<CommentsResponse>(body)
                response.comments
            }
        }
    }

    suspend fun addComment(postId: Int, content: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            ApiClient.addComment(postId, content).map { it.optBoolean("success") }
        }
    }

    suspend fun getNotifications(): Result<List<Notification>> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.getNotifications()
            result.map { body ->
                val response = json.decodeFromString<NotificationsResponse>(body)
                response.notifications
            }
        }
    }

    suspend fun getUnreadCount(): Result<Int> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.getUnreadCount()
            result.map { body ->
                val response = json.decodeFromString<UnreadCountResponse>(body)
                response.count
            }
        }
    }

    suspend fun searchDonors(city: String? = null, bloodGroup: String? = null): Result<List<Donor>> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.searchDonors(city, bloodGroup)
            result.map { body ->
                val response = json.decodeFromString<DonorListResponse>(body)
                response.donors
            }
        }
    }

    suspend fun getMetadata(): Result<MetadataResponse> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.getMetadata()
            result.map { body ->
                json.decodeFromString<MetadataResponse>(body)
            }
        }
    }

    suspend fun sharePost(postId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            ApiClient.sharePost(postId).map { it.optBoolean("success") }
        }
    }

    suspend fun savePost(postId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            ApiClient.savePost(postId).map { it.optBoolean("success") }
        }
    }

    suspend fun search(query: String): Result<org.json.JSONObject> {
        return withContext(Dispatchers.IO) {
            ApiClient.search(query).map { org.json.JSONObject(it) }
        }
    }

    suspend fun requestDonor(
        donorId: Int, patientName: String, hospital: String,
        contactNumber: String, urgency: String = "medium"
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            ApiClient.requestDonor(donorId, patientName, hospital, contactNumber, urgency)
                .map { it.optBoolean("success") }
        }
    }
}
