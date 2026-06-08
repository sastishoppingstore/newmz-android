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
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.Locale

object ApiClient {
    const val BASE_URL = "https://mzbloodbridge.pk/"
    private val JSON_MEDIA = "application/json".toMediaType()
    private val FORM_MEDIA = "application/x-www-form-urlencoded".toMediaType()

    private val cookieStore = ConcurrentHashMap<String, List<Cookie>>()
    lateinit var sessionManager: SessionManager

    private val bloodGroupIds = mapOf(
        "A+" to "1",
        "A-" to "2",
        "B+" to "3",
        "B-" to "4",
        "AB+" to "5",
        "AB-" to "6",
        "O+" to "7",
        "O-" to "8"
    )

    private val cityIds = mapOf(
        "abbottabad" to "52",
        "astore" to "85",
        "attock" to "47",
        "badin" to "17",
        "bahawalpur" to "29",
        "bannu" to "56",
        "batkhela" to "63",
        "bhakkar" to "44",
        "bhimber" to "78",
        "chakwal" to "48",
        "chaman" to "68",
        "charsadda" to "58",
        "chilas" to "83",
        "chiniot" to "41",
        "dadu" to "16",
        "dera ghazi khan" to "45",
        "dera ismail khan" to "57",
        "dhamke" to "86",
        "faisalabad" to "5",
        "ghizer" to "84",
        "ghotki" to "24",
        "gilgit" to "80",
        "gujranwala" to "9",
        "gujrat" to "27",
        "gwadar" to "65",
        "hafizabad" to "39",
        "haripur" to "62",
        "hub" to "69",
        "hunza" to "82",
        "hyderabad" to "10",
        "islamabad" to "3",
        "jacobabad" to "19",
        "jhang" to "35",
        "jhelum" to "49",
        "karachi" to "1",
        "kashmore" to "25",
        "kasur" to "37",
        "khairpur" to "23",
        "khanewal" to "36",
        "khushab" to "87",
        "khuzdar" to "67",
        "kohat" to "55",
        "kotli" to "77",
        "lahore" to "2",
        "larkana" to "12",
        "layyah" to "43",
        "loralai" to "72",
        "mandi bahauddin" to "38",
        "mansehra" to "61",
        "mardan" to "53",
        "mianwali" to "50",
        "mirpur" to "75",
        "mirpur khas" to "14",
        "multan" to "6",
        "muzaffarabad" to "74",
        "muzaffargarh" to "42",
        "narowal" to "40",
        "nawabshah" to "13",
        "nowshera" to "59",
        "nushki" to "73",
        "okara" to "33",
        "pakpattan" to "51",
        "palandri" to "79",
        "peshawar" to "7",
        "quetta" to "8",
        "rahim yar khan" to "31",
        "rajhanpur" to "46",
        "rawalakot" to "76",
        "rawalpindi" to "4",
        "sahiwal" to "32",
        "sargodha" to "30",
        "sheikhupura" to "28",
        "shikarpur" to "18",
        "sialkot" to "26",
        "sibi" to "71",
        "skardu" to "81",
        "sukkur" to "11",
        "swabi" to "60",
        "swat" to "54",
        "tando allahyar" to "20",
        "tando muhammad khan" to "21",
        "thatta" to "15",
        "timergara" to "64",
        "turbat" to "66",
        "umerkot" to "22",
        "vehari" to "34",
        "zhob" to "70"
    )

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

    private fun encodeForm(params: List<Pair<String, String>>): RequestBody {
        return params.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }.toRequestBody(FORM_MEDIA)
    }

    private fun lookupKey(value: String): String {
        return value.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")
    }

    private fun normalizeBloodGroup(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed in bloodGroupIds.values) return trimmed
        return bloodGroupIds[trimmed.uppercase(Locale.ROOT)]
    }

    private fun cityParams(city: String): List<Pair<String, String>> {
        val trimmed = city.trim()
        if (trimmed.isEmpty()) return emptyList()
        if (trimmed.all { it.isDigit() }) {
            return listOf("city" to trimmed, "city_new" to "")
        }

        val existingCityId = cityIds[lookupKey(trimmed)]
        return if (existingCityId != null) {
            listOf("city" to existingCityId, "city_new" to "")
        } else {
            listOf("city" to "", "city_new" to trimmed)
        }
    }

    private fun extractErrorMessage(body: String, fallback: String): String {
        val listErrors = Regex("""<li[^>]*>(.*?)</li>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(body)
            .map { it.groupValues[1].stripHtml() }
            .filter { it.isNotBlank() }
            .toList()
        if (listErrors.isNotEmpty()) return listErrors.joinToString("\n")

        val alertMatch = Regex(
            """<div[^>]*class=["'][^"']*alert-error[^"']*["'][^>]*>(.*?)</div>""",
            RegexOption.DOT_MATCHES_ALL
        ).find(body)
        val alertMessage = alertMatch?.groupValues?.getOrNull(1)?.stripHtml()
        return alertMessage?.takeIf { it.isNotBlank() } ?: fallback
    }

    private fun String.stripHtml(): String {
        return replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun isLoginPage(body: String): Boolean {
        return body.contains("name=\"password\"") &&
            body.contains("login", ignoreCase = true) &&
            body.contains("register.php", ignoreCase = true)
    }

    private fun extractProfileName(body: String): String? {
        val headingMatch = Regex("""<h[12][^>]*>\s*([^<]+)\s*</h[12]>""")
            .find(body, 500)
            ?.groupValues
            ?.getOrNull(1)
            ?.stripHtml()
        if (!headingMatch.isNullOrBlank() && !headingMatch.contains("MZ Blood Bridge", ignoreCase = true)) {
            return headingMatch
        }

        val labelMatch = Regex("""(?:Name|Full Name)\s*</[^>]+>\s*<[^>]+>\s*([^<]+)""", RegexOption.IGNORE_CASE)
            .find(body)
            ?.groupValues
            ?.getOrNull(1)
            ?.stripHtml()
        return labelMatch?.takeIf { it.isNotBlank() }
    }

    private fun userJson(email: String, name: String? = null, phone: String? = null): org.json.JSONObject {
        return org.json.JSONObject().apply {
            put("id", sessionManager.userId)
            put("email", email)
            put("name", name?.takeIf { it.isNotBlank() } ?: email.substringBefore("@"))
            phone?.takeIf { it.isNotBlank() }?.let { put("phone", it) }
            put("role", sessionManager.userRole ?: "user")
        }
    }

    suspend fun login(email: String, password: String): Result<org.json.JSONObject> {
        return try {
            initSession()

            val formBody = encodeForm(
                listOf(
                    "email" to email,
                    "password" to password,
                    "remember" to "on"
                )
            )

            val loginClient = client.newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build()

            val response = loginClient.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}login.php")
                    .post(formBody)
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .build()
            ).execute()

            val code = response.code
            val location = response.header("Location")

            if (code in 300..399 && location != null) {
                val profileResp = client.newCall(
                    okhttp3.Request.Builder()
                        .url("${BASE_URL}profile.php")
                        .get()
                        .build()
                ).execute()
                val profileBody = profileResp.body?.string() ?: ""
                if (profileResp.code in 300..399 || isLoginPage(profileBody)) {
                    return Result.failure(Exception("Login session could not be verified"))
                }

                val userName = extractProfileName(profileBody)
                sessionManager.userEmail = email
                userName?.let { sessionManager.userName = it }
                sessionManager.isLoggedIn = true

                val json = org.json.JSONObject().apply { put("success", true) }
                val userJson = userJson(email, userName)
                json.put("user", userJson)
                Result.success(json)
            } else {
                val body = response.body?.string() ?: ""
                Result.failure(Exception(extractErrorMessage(body, "Invalid email or password")))
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

            val serverBloodGroup = normalizeBloodGroup(bloodGroup)
                ?: return Result.failure(Exception("Please select a blood group"))
            val serverCityParams = cityParams(city)
            if (serverCityParams.isEmpty()) {
                return Result.failure(Exception("Please enter your city"))
            }

            val params = mutableListOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "password" to password,
                "confirm_password" to confirmPassword,
                "blood_group" to serverBloodGroup
            )
            params.addAll(serverCityParams)
            params.add("cnic" to cnic)
            params.add("gender" to gender)
            params.add("dob" to dob)
            if (referralCode.isNotEmpty()) params.add("referral_code" to referralCode)

            val regClient = client.newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build()

            val response = regClient.newCall(
                okhttp3.Request.Builder()
                    .url("${BASE_URL}register.php")
                    .post(encodeForm(params))
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
                val userJson = userJson(email, name, phone)
                json.put("user", userJson)
                Result.success(json)
            } else {
                val body = response.body?.string() ?: ""
                Result.failure(Exception(extractErrorMessage(body, "Registration failed")))
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
