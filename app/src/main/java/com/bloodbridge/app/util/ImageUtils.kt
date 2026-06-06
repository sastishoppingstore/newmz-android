package com.bloodbridge.app.util

import com.bloodbridge.app.data.api.ApiClient

object ImageUtils {
    fun getFullUrl(path: String?): String? {
        if (path.isNullOrEmpty()) return null
        if (path.startsWith("http")) return path
        return "${ApiClient.BASE_URL}uploads/$path"
    }

    fun getProfileUrl(filename: String?): String? {
        if (filename.isNullOrEmpty()) return null
        if (filename.startsWith("http")) return filename
        return "${ApiClient.BASE_URL}uploads/profiles/$filename"
    }

    fun getPostMediaUrl(filename: String?): String? {
        if (filename.isNullOrEmpty()) return null
        if (filename.startsWith("http")) return filename
        return "${ApiClient.BASE_URL}uploads/posts/$filename"
    }

    fun getReportUrl(filename: String?): String? {
        if (filename.isNullOrEmpty()) return null
        return "${ApiClient.BASE_URL}uploads/reports/$filename"
    }

    fun timeAgo(dateString: String): String {
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val date = format.parse(dateString) ?: return dateString
            val now = System.currentTimeMillis()
            val diff = now - date.time

            when {
                diff < 60_000 -> "Just now"
                diff < 3_600_000 -> "${diff / 60_000}m ago"
                diff < 86_400_000 -> "${diff / 3_600_000}h ago"
                diff < 604_800_000 -> "${diff / 86_400_000}d ago"
                else -> java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(date)
            }
        } catch (e: Exception) {
            dateString
        }
    }
}
