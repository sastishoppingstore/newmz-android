package com.bloodbridge.app.data.api

import com.bloodbridge.app.BuildConfig
import com.bloodbridge.app.data.session.SessionManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object ApiClient {
    const val BASE_URL = "https://mzbloodbridge.pk/"
    private val JSON_MEDIA = "application/json".toMediaType()
    private val FORM_MEDIA = "application/x-www-form-urlencoded".toMediaType()

    private val cookieStore = ConcurrentHashMap<String, List<Cookie>>()
    lateinit var sessionManager: SessionManager

    val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore[url.host] = cookies
                }
                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieStore[url.host] ?: emptyList()
                }
            })
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                builder.addHeader("User-Agent", "MZBloodBridge-Android/1.0")
                sessionManager.csrfToken?.let {
                    builder.addHeader("X-CSRF-Token", it)
                }
                chain.proceed(builder.build())
            }
            .build()
    }

    private suspend fun getCsrfFromHtml(): String? {
        return try {
            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url(BASE_URL)
                    .get()
                    .build()
            ).execute()
            val body = response.body?.string() ?: return null
            val metaRegex = Regex("""<meta name="csrf-token" content="([a-f0-9]+)">""")
            metaRegex.find(body)?.groupValues?.getOrNull(1)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun initSession() {
        try {
            val token = getCsrfFromHtml()
            if (!token.isNullOrEmpty()) {
                sessionManager.csrfToken = token
            }
        } catch (_: Exception) {}
    }

    suspend fun login(email: String, password: String): Result<org.json.JSONObject> {
        return try {
            initSession()

            val formBody = StringBuilder()
            formBody.append("email=${java.net.URLEncoder.encode(email, "UTF-8")}")
            formBody.append("&password=${java.net.URLEncoder.encode(password, "UTF-8")}")

            val loginClient = client.newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build()

            val response = loginClient.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}login.php")
                    .post(formBody.toString().toRequestBody(FORM_MEDIA))
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .build()
            ).execute()

            val code = response.code
            val location = response.header("Location")

            if (code in 300..399 && location != null) {
                // Redirect = login success
                sessionManager.isLoggedIn = true
                sessionManager.userEmail = email

                // Fetch profile to get user name
                val profileResp = client.newCall(
                    okhttp3.Request.Builder()
                        .url("${BASE_URL}profile.php")
                        .get()
                        .build()
                ).execute()
                val profileBody = profileResp.body?.string() ?: ""
                val nameMatch = Regex("""<h[12][^>]*>\s*([^<]+)\s*</h[12]>""").find(profileBody, 500)
                val userName = nameMatch?.groupValues?.getOrNull(1)?.trim()

                val json = org.json.JSONObject().apply {
                    put("success", true)
                }
                val userJson = org.json.JSONObject().apply {
                    put("email", email)
                    userName?.let { put("name", it); sessionManager.userName = it }
                }
                json.put("user", userJson)
                Result.success(json)
            } else {
                val body = response.body?.string() ?: ""
                val errorMatch = Regex("""alert-error[^>]*>\s*([^<]+)""").find(body)
                val errorMsg = errorMatch?.groupValues?.getOrNull(1)?.trim() ?: "Invalid email or password"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String, email: String, password: String, confirmPassword: String,
        phone: String, bloodGroup: String, city: String, cnic: String,
        gender: String, dob: String, referralCode: String = ""
    ): Result<org.json.JSONObject> {
        return try {
            initSession()

            val params = mutableMapOf(
                "name" to name, "email" to email, "password" to password,
                "confirm_password" to confirmPassword, "phone" to phone,
                "blood_group" to bloodGroup, "city" to city, "cnic" to cnic,
                "gender" to gender, "dob" to dob
            )
            if (referralCode.isNotEmpty()) params["referral_code"] = referralCode

            val formBody = params.entries.joinToString("&") {
                "${java.net.URLEncoder.encode(it.key, "UTF-8")}=${java.net.URLEncoder.encode(it.value, "UTF-8")}"
            }

            val regClient = client.newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build()

            val response = regClient.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}register.php")
                    .post(formBody.toString().toRequestBody(FORM_MEDIA))
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .build()
            ).execute()

            val code = response.code
            val location = response.header("Location")

            if (code in 300..399 && location != null) {
                val json = org.json.JSONObject().apply { put("success", true) }
                sessionManager.isLoggedIn = true
                sessionManager.userName = name
                sessionManager.userEmail = email
                val userJson = org.json.JSONObject().apply { put("name", name); put("email", email) }
                json.put("user", userJson)
                Result.success(json)
            } else {
                val body = response.body?.string() ?: ""
                val errorMatch = Regex("""alert-error[^>]*>\s*([^<]+)""").find(body)
                val errorsMatch = Regex("""<li>([^<]+)</li>""").findAll(body)
                val errorList = errorsMatch.map { it.groupValues[1] }.toList()
                val errorMsg = if (errorList.isNotEmpty()) errorList.joinToString("\n")
                    else errorMatch?.groupValues?.getOrNull(1)?.trim()
                    ?: "Registration failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}register.php")
                    .post(formBody.toRequestBody(FORM_MEDIA))
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .build()
            ).execute()

            val body = response.body?.string() ?: return Result.failure(Exception("Empty response"))

            if (body.contains("alert-error") || body.contains("alert alert-error")) {
                val errorMatch = Regex("""alert-error[^>]*>([^<]+)""").find(body)
                val errorMsg = errorMatch?.groupValues?.getOrNull(1)?.trim() ?: "Registration failed"
                Result.failure(Exception(errorMsg))
            } else if (body.contains("Welcome") || body.contains("index.php")) {
                val json = org.json.JSONObject().apply { put("success", true) }
                sessionManager.isLoggedIn = true
                sessionManager.userName = name
                sessionManager.userEmail = email

                val userJson = org.json.JSONObject().apply {
                    put("name", name)
                    put("email", email)
                }
                json.put("user", userJson)
                Result.success(json)
            } else {
                Result.failure(Exception("Registration failed. Please try again."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Boolean> {
        sessionManager.clear()
        cookieStore.clear()
        return Result.success(true)
    }

    suspend fun getProfile(): Result<org.json.JSONObject> {
        return try {
            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}profile.php")
                    .get()
                    .build()
            ).execute()

            val body = response.body?.string() ?: return Result.failure(Exception("Empty response"))
            val json = org.json.JSONObject()

            val nameMatch = Regex("""<h[12][^>]*>([^<]+)</h[12]>""").find(body, 500)
            nameMatch?.let { json.put("name", it.groupValues[1].trim()) }

            val jsonResult = org.json.JSONObject().apply {
                put("success", true)
                put("user", json)
            }
            Result.success(jsonResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFeed(offset: Int = 0, limit: Int = 20, city: Int = 0, bloodGroup: Int = 0): Result<String> {
        return try {
            val urlBuilder = StringBuilder("${BASE_URL}api/feed.php?offset=$offset&limit=$limit")
            if (city > 0) urlBuilder.append("&city=$city")
            if (bloodGroup > 0) urlBuilder.append("&blood_group=$bloodGroup")

            val response = client.newCall(
                okhttp3.Request.Builder().url(urlBuilder.toString()).get().build()
            ).execute()
            Result.success(response.body?.string() ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(
        content: String, type: String = "text",
        bloodGroup: String? = null, city: String? = null,
        hospitalName: String? = null, urgency: String = "low",
        isEmergency: Int = 0, mediaFile: File? = null
    ): Result<org.json.JSONObject> {
        return try {
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("content", content)
                .addFormDataPart("type", type)
                .addFormDataPart("is_emergency", isEmergency.toString())
                .addFormDataPart("urgency", urgency)

            sessionManager.csrfToken?.let { builder.addFormDataPart("csrf_token", it) }
            bloodGroup?.let { builder.addFormDataPart("blood_group", it) }
            city?.let { builder.addFormDataPart("city", it) }
            hospitalName?.let { builder.addFormDataPart("hospital_name", it) }
            mediaFile?.let {
                builder.addFormDataPart("media", it.name, it.readBytes().toRequestBody("image/*".toMediaType()))
            }

            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}api/create_post.php")
                    .post(builder.build())
                    .build()
            ).execute()
            Result.success(org.json.JSONObject(response.body?.string() ?: "{}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitEmergencyRequest(
        patientName: String, bloodGroup: Int, city: String, hospital: String,
        units: Int, urgency: String, contact: String, attendantName: String,
        doctorNote: String = "", reportFile: File? = null
    ): Result<org.json.JSONObject> {
        return try {
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("patient_name", patientName)
                .addFormDataPart("blood_group", bloodGroup.toString())
                .addFormDataPart("city", city)
                .addFormDataPart("hospital", hospital)
                .addFormDataPart("units", units.toString())
                .addFormDataPart("urgency_level", urgency)
                .addFormDataPart("contact_number", contact)
                .addFormDataPart("attendant_name", attendantName)
                .addFormDataPart("doctor_note", doctorNote)
            sessionManager.csrfToken?.let { builder.addFormDataPart("csrf_token", it) }

            reportFile?.let {
                builder.addFormDataPart("report_file", it.name, it.readBytes().toRequestBody("application/pdf".toMediaType()))
            }

            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}api/emergency_request.php")
                    .post(builder.build())
                    .build()
            ).execute()
            Result.success(org.json.JSONObject(response.body?.string() ?: "{}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEmergencyRequests(cityId: Int = 0): Result<String> {
        return try {
            val url = if (cityId > 0) "${BASE_URL}api/emergency_request.php?city=$cityId" else "${BASE_URL}api/emergency_request.php"
            val response = client.newCall(okhttp3.Request.Builder().url(url).get().build()).execute()
            Result.success(response.body?.string() ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchChats(): Result<String> {
        return try {
            val response = client.newCall(
                okhttp3.Request.Builder().url("${BASE_URL}messages.php").get().build()
            ).execute()
            Result.success(response.body?.string() ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchMessages(chatId: Int): Result<String> {
        return try {
            val response = client.newCall(
                okhttp3.Request.Builder().url("${BASE_URL}api/chat_fetch.php?chat_id=$chatId").get().build()
            ).execute()
            Result.success(response.body?.string() ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(chatId: Int, message: String): Result<org.json.JSONObject> {
        return try {
            val jsonBody = org.json.JSONObject().apply {
                put("chat_id", chatId)
                put("message", message)
                sessionManager.csrfToken?.let { put("csrf_token", it) }
            }
            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}api/chat_send.php")
                    .post(jsonBody.toString().toRequestBody(JSON_MEDIA))
                    .build()
            ).execute()
            Result.success(org.json.JSONObject(response.body?.string() ?: "{}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleReaction(postId: Int, action: String = "toggle", reactionType: String = "like"): Result<org.json.JSONObject> {
        return try {
            val jsonBody = org.json.JSONObject().apply {
                put("post_id", postId)
                put("action", action)
                put("reaction_type", reactionType)
                sessionManager.csrfToken?.let { put("csrf_token", it) }
            }
            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}api/reaction.php")
                    .post(jsonBody.toString().toRequestBody(JSON_MEDIA))
                    .build()
            ).execute()
            Result.success(org.json.JSONObject(response.body?.string() ?: "{}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getComments(postId: Int): Result<String> {
        return try {
            val response = client.newCall(
                okhttp3.Request.Builder().url("${BASE_URL}api/comment.php?post_id=$postId").get().build()
            ).execute()
            Result.success(response.body?.string() ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: Int, content: String, parentCommentId: Int? = null): Result<org.json.JSONObject> {
        return try {
            val jsonBody = org.json.JSONObject().apply {
                put("post_id", postId)
                put("content", content)
                sessionManager.csrfToken?.let { put("csrf_token", it) }
                parentCommentId?.let { put("parent_comment_id", it) }
            }
            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}api/comment.php")
                    .post(jsonBody.toString().toRequestBody(JSON_MEDIA))
                    .build()
            ).execute()
            Result.success(org.json.JSONObject(response.body?.string() ?: "{}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotifications(): Result<String> {
        return try {
            val response = client.newCall(
                okhttp3.Request.Builder().url("${BASE_URL}api/notifications.php").get().build()
            ).execute()
            Result.success(response.body?.string() ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnreadCount(): Result<String> {
        return try {
            val response = client.newCall(
                okhttp3.Request.Builder().url("${BASE_URL}api/notifications_unread.php").get().build()
            ).execute()
            Result.success(response.body?.string() ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchDonors(city: String? = null, bloodGroup: String? = null): Result<String> {
        return try {
            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}api/search.php?type=donors&city=${city?.let { java.net.URLEncoder.encode(it, "UTF-8") } ?: ""}&blood_group=${bloodGroup ?: ""}")
                    .get()
                    .build()
            ).execute()
            Result.success(response.body?.string() ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMetadata(): Result<String> {
        return try {
            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}api/feed.php?limit=1")
                    .get()
                    .build()
            ).execute()
            Result.success(response.body?.string() ?: "{}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sharePost(postId: Int): Result<org.json.JSONObject> {
        return try {
            val jsonBody = org.json.JSONObject().apply {
                put("post_id", postId)
                sessionManager.csrfToken?.let { put("csrf_token", it) }
            }
            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}api/share.php")
                    .post(jsonBody.toString().toRequestBody(JSON_MEDIA))
                    .build()
            ).execute()
            Result.success(org.json.JSONObject(response.body?.string() ?: "{}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun savePost(postId: Int): Result<org.json.JSONObject> {
        return try {
            val jsonBody = org.json.JSONObject().apply {
                put("post_id", postId)
                sessionManager.csrfToken?.let { put("csrf_token", it) }
            }
            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}api/save_post.php")
                    .post(jsonBody.toString().toRequestBody(JSON_MEDIA))
                    .build()
            ).execute()
            Result.success(org.json.JSONObject(response.body?.string() ?: "{}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun search(query: String): Result<String> {
        return try {
            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}api/search.php?q=${java.net.URLEncoder.encode(query, "UTF-8")}")
                    .get()
                    .build()
            ).execute()
            Result.success(response.body?.string() ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestDonor(
        donorId: Int, patientName: String, hospital: String,
        contactNumber: String, urgency: String = "medium",
        requiredDate: String? = null, message: String? = null
    ): Result<org.json.JSONObject> {
        return try {
            val jsonBody = org.json.JSONObject().apply {
                put("donor_id", donorId)
                put("patient_name", patientName)
                put("hospital", hospital)
                put("contact_number", contactNumber)
                put("urgency", urgency)
                sessionManager.csrfToken?.let { put("csrf_token", it) }
                requiredDate?.let { put("required_date", it) }
                message?.let { put("message", it) }
            }
            val response = client.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}api/donor_request.php")
                    .post(jsonBody.toString().toRequestBody(JSON_MEDIA))
                    .build()
            ).execute()
            Result.success(org.json.JSONObject(response.body?.string() ?: "{}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
