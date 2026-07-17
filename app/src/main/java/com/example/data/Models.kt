package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class Video(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val username: String,
    val userAvatarUrl: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val title: String,
    val description: String,
    var likesCount: Int,
    var commentsCount: Int,
    var sharesCount: Int,
    var viewsCount: Int = 0,
    var isPrivate: Boolean = false,
    var isLiked: Boolean = false,
    var isFollowingCreator: Boolean = false,
    val musicName: String,
    val category: String,
    val quranRef: String = "",
    val hadithRef: String = "",
    val sourceInfo: String = "",
    val contentType: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoId: Int,
    val username: String,
    val userAvatarUrl: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,
    val senderName: String,
    val receiverId: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "LIKE", "COMMENT", "FOLLOW", "SYSTEM"
    val title: String,
    val description: String,
    val sourceUsername: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val username: String,
    val avatarUrl: String,
    val role: String, // "USER", "CREATOR", "ADMIN"
    val status: String, // "ACTIVE", "SUSPENDED"
    val bio: String = "",
    val website: String = "",
    val twitter: String = "",
    val instagram: String = "",
    val isVerified: Boolean = false,
    val verificationType: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isTrending: Boolean = false,
    val hashtagCount: Int = 0
)

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "VIDEO", "USER", "COMMENT", "SPAM", "ABUSE"
    val contentId: String, // e.g. videoId, commentId, userId
    val reportedItemTitle: String, // name or preview of the reported content
    val reason: String,
    val reportedBy: String,
    val status: String = "PENDING", // "PENDING", "RESOLVED_APPROVED", "RESOLVED_REMOVED", "CLOSED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "advertisements")
data class Advertisement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val imageUrl: String,
    val targetUrl: String,
    val status: String, // "ACTIVE", "INACTIVE"
    val views: Int = 0,
    val clicks: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_logs")
data class AdminLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val adminUsername: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey val userId: String,
    val email: String = "me@deentok.app",
    val phoneNumber: String = "+251912345678",
    val passwordHash: String = "defaultPassword123",
    val emailVerified: Boolean = true,
    val twoFactorEnabled: Boolean = false,
    
    // Privacy
    val isPrivateAccount: Boolean = false,
    val whoCanFollow: String = "Everyone", // "Everyone" or "Approved only"
    val whoCanComment: String = "Everyone", // "Everyone", "Followers only", "Nobody"
    val whoCanSendMessages: String = "Everyone", // "Everyone", "Followers only", "Nobody"
    val allowDownloads: Boolean = true,
    val showActivityStatus: Boolean = true,

    // Notifications
    val likeNotifications: Boolean = true,
    val commentNotifications: Boolean = true,
    val followerNotifications: Boolean = true,
    val messageNotifications: Boolean = true,
    val mentionNotifications: Boolean = true,
    val creatorUpdates: Boolean = true,
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,

    // Language
    val language: String = "English", // "English", "Afaan Oromoo", "Amharic"

    // Appearance
    val themeMode: String = "System Default", // "Light", "Dark", "System Default"

    // Content Preferences
    val videoInterests: String = "Tech, Music, Cooking", // comma separated
    val sensitiveContentFilter: Boolean = true,
    val autoplayVideos: String = "On", // "On", "Off"
    val dataSavingMode: Boolean = false,

    // Accessibility
    val textSize: String = "Medium", // "Small", "Medium", "Large"
    val captionsEnabled: Boolean = false,
    val reducedAnimations: Boolean = false,
    val highContrastMode: Boolean = false
)

@Entity(tableName = "recent_searches")
data class RecentSearch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class VideoTemplate(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val category: String, // "Trending", "Photo", "Business", "Education", "Celebration", "Story"
    val defaultMusic: String,
    val defaultText: String = "Replace this text with your slogan!",
    val isActive: Boolean = true,
    val usageCount: Int = 120
)

data class VideoDraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val mediaPaths: List<String> = emptyList(),
    val mediaType: String, // "image" or "video"
    val category: String = "Other",
    val musicName: String = "None",
    val filterName: String = "Normal",
    val textOverlay: String = "",
    val coverText: String = "",
    val isPrivate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)




