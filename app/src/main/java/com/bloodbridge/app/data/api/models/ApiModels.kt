package com.bloodbridge.app.data.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val error: String? = null,
    val message: String? = null,
    val data: T? = null
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val error: String? = null,
    val message: String? = null,
    val user: User? = null
)

@Serializable
data class RegisterResponse(
    val success: Boolean,
    val error: String? = null,
    val errors: List<String>? = null,
    val user: User? = null
)

@Serializable
data class User(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val phone: String? = null,
    val profile_photo: String? = null,
    val blood_group_id: Int? = null,
    val city_id: Int? = null,
    val gender: String? = null,
    val date_of_birth: String? = null,
    val is_verified: Boolean = false,
    val role: String = "user",
    val bio: String? = null,
    val cover_photo: String? = null,
    val total_donations: Int? = null,
    val last_donation_date: String? = null,
    val eligibility_status: String? = null,
    val emergency_contact_phone: String? = null,
    val donor_badge: String? = null,
)

@Serializable
data class ProfileResponse(
    val success: Boolean,
    val user: User? = null,
    val stats: ProfileStats? = null,
    val posts: List<Post>? = null
)

@Serializable
data class ProfileStats(
    val posts_count: Int = 0,
    val friends_count: Int = 0,
    val followers_count: Int = 0,
    val total_donations: Int = 0,
    val lives_saved: Int = 0
)

@Serializable
data class Post(
    val id: Int = 0,
    val user_id: Int = 0,
    val type: String = "text",
    val content: String = "",
    val media_url: String? = null,
    val blood_group_id: Int? = null,
    val city_id: Int? = null,
    val hospital_name: String? = null,
    val urgency: String? = null,
    val is_emergency: Int = 0,
    val visibility: String = "public",
    val created_at: String = "",
    val name: String = "",
    val profile_photo: String? = null,
    val user_blood_group: String? = null,
    val city: String? = null,
    val is_verified: Boolean = false,
    val reaction_count: Int = 0,
    val comment_count: Int = 0,
    val share_count: Int = 0,
    val user_reaction: String? = null,
    val is_saved: Boolean = false
)

@Serializable
data class FeedResponse(
    val success: Boolean,
    val posts: List<Post> = emptyList()
)

@Serializable
data class EmergencyRequest(
    val id: Int = 0,
    val requester_id: Int = 0,
    val patient_name: String = "",
    val blood_group_id: Int = 0,
    val city_id: Int = 0,
    val hospital_name: String = "",
    val units_needed: Int = 1,
    val urgency_level: String = "high",
    val contact_number: String = "",
    val attendant_name: String = "",
    val doctor_note: String? = null,
    val report_file: String? = null,
    val status: String = "pending",
    val created_at: String = "",
    val requester_name: String = "",
    val requester_phone: String? = null,
    val blood_group: String? = null,
    val city: String? = null
)

@Serializable
data class EmergencyListResponse(
    val success: Boolean,
    val requests: List<EmergencyRequest> = emptyList()
)

@Serializable
data class Message(
    val id: Int = 0,
    val chat_id: Int = 0,
    val sender_id: Int = 0,
    val content: String = "",
    val created_at: String = "",
    val name: String = "",
    val profile_photo: String? = null,
    val is_read: Int = 0
)

@Serializable
data class ChatMessagesResponse(
    val success: Boolean,
    val messages: List<Message> = emptyList()
)

@Serializable
data class Chat(
    val id: Int = 0,
    val created_at: String? = null,
    val updated_at: String? = null,
    val other_user_name: String? = null,
    val other_user_photo: String? = null,
    val last_message: String? = null
)

@Serializable
data class ChatListResponse(
    val success: Boolean,
    val chats: List<Chat> = emptyList()
)

@Serializable
data class Notification(
    val id: Int = 0,
    val user_id: Int = 0,
    val type: String = "",
    val title: String = "",
    val message: String = "",
    val data: String? = null,
    val is_read: Int = 0,
    val created_at: String = ""
)

@Serializable
data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<Notification> = emptyList()
)

@Serializable
data class UnreadCountResponse(
    val success: Boolean,
    val count: Int = 0
)

@Serializable
data class Comment(
    val id: Int = 0,
    val post_id: Int = 0,
    val user_id: Int = 0,
    val content: String = "",
    val parent_comment_id: Int? = null,
    val deleted_at: String? = null,
    val created_at: String = "",
    val name: String = "",
    val profile_photo: String? = null
)

@Serializable
data class CommentsResponse(
    val success: Boolean,
    val comments: List<Comment> = emptyList()
)

@Serializable
data class CommentAddResponse(
    val success: Boolean,
    val comment_id: Int? = null,
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class Donor(
    val id: Int = 0,
    val name: String = "",
    val email: String? = null,
    val phone: String? = null,
    val profile_photo: String? = null,
    val blood_group: String? = null,
    val city: String? = null,
    val total_donations: Int? = null,
    val last_donation_date: String? = null,
    val eligibility_status: String? = null,
    val is_verified: Boolean = false,
    val donor_badge: String? = null
)

@Serializable
data class DonorListResponse(
    val success: Boolean,
    val donors: List<Donor> = emptyList()
)

@Serializable
data class BloodGroup(
    val id: Int,
    val name: String
)

@Serializable
data class City(
    val id: Int,
    val name: String,
    val province: String? = null
)

@Serializable
data class MetadataResponse(
    val success: Boolean,
    val blood_groups: List<BloodGroup> = emptyList(),
    val cities: List<City> = emptyList()
)

@Serializable
data class SimpleResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null,
    val redirect: String? = null,
    val comment_id: Int? = null,
    val count: Int? = null,
    val filename: String? = null
)
