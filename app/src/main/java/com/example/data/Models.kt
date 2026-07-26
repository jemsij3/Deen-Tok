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

// --- DT COIN & GIFT ECONOMY SYSTEM ENTITIES ---

@Entity(tableName = "user_wallets")
data class UserWallet(
    @PrimaryKey val userId: String,
    val coinBalance: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "coin_packages")
data class CoinPackage(
    @PrimaryKey val id: String,
    val title: String,
    val coinAmount: Int,
    val bonusAmount: Int = 0,
    val priceEtb: Double,
    val isActive: Boolean = true,
    val badge: String = "" // "Most Popular", "Best Value", "VIP"
)

@Entity(tableName = "payment_transactions")
data class PaymentTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val packageId: String,
    val packageName: String,
    val coinAmount: Int,
    val amountEtb: Double,
    val paymentMethod: String, // "Telebirr", "CBE"
    val accountOrPhone: String,
    val txRef: String,
    val status: String = "COMPLETED", // "COMPLETED", "PENDING", "FAILED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "coin_transactions")
data class CoinTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val type: String, // "PURCHASE", "GIFT_SENT", "GIFT_RECEIVED", "REWARD_PAYOUT"
    val amount: Int, // Positive for addition, negative for spent
    val description: String,
    val relatedId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "virtual_gifts")
data class VirtualGift(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // "BASIC", "SPECIAL", "PREMIUM"
    val coinPrice: Int,
    val iconSymbol: String, // e.g. "🌹", "⭐", "📜", "🕌", "🕋", "👑", "✨"
    val animationType: String = "FLOAT_BURST",
    val isActive: Boolean = true
)

@Entity(tableName = "gift_transactions")
data class GiftTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderUserId: String,
    val senderUsername: String,
    val creatorUserId: String,
    val creatorUsername: String,
    val videoId: Int = 0,
    val giftId: String,
    val giftName: String,
    val giftIcon: String,
    val coinPrice: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "creator_wallets")
data class CreatorWallet(
    @PrimaryKey val userId: String,
    val totalGiftsReceived: Int = 0,
    val totalCoinsEarned: Int = 0,
    val rewardBalanceCoins: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawal_requests")
data class WithdrawalRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val username: String,
    val coinsAmount: Int,
    val estimatedEtb: Double,
    val payoutMethod: String, // "Telebirr", "CBE Bank"
    val accountDetails: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED", "COMPLETED"
    val createdAt: Long = System.currentTimeMillis()
)

// --- LIVE STREAMING SYSTEM ENTITIES ---

@Entity(tableName = "live_streams")
data class LiveStream(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val streamKey: String,
    val creatorUserId: String,
    val creatorUsername: String,
    val creatorAvatarUrl: String = "",
    val title: String,
    val description: String = "",
    val category: String = "Islamic Lecture", // "Quran Recitation", "Islamic Lecture", "Q&A & Fatwa", "Nasheed & Arts", "Daily Reminders"
    val status: String = "LIVE", // "LIVE", "ENDED", "SUSPENDED"
    val thumbnailUrl: String = "",
    val videoBgStyle: String = "MOSQUE_BG", // Visual aesthetic preset for live stream preview
    val viewerCount: Int = 1,
    val likeCount: Int = 0,
    val giftCoinsEarned: Int = 0,
    val pinnedComment: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null
)

@Entity(tableName = "live_chat_messages")
data class LiveChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val streamId: Int,
    val userId: String,
    val username: String,
    val avatarUrl: String = "",
    val message: String,
    val isSystemMessage: Boolean = false,
    val isGiftMessage: Boolean = false,
    val giftIcon: String = "",
    val giftCoins: Int = 0,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_viewers")
data class LiveViewer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val streamId: Int,
    val userId: String,
    val username: String,
    val joinedAt: Long = System.currentTimeMillis(),
    val isMuted: Boolean = false,
    val isBlocked: Boolean = false,
    val isModerator: Boolean = false
)

@Entity(tableName = "live_moderators")
data class LiveModerator(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val streamId: Int,
    val userId: String,
    val username: String,
    val assignedBy: String,
    val assignedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_reports")
data class LiveReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val streamId: Int,
    val streamTitle: String,
    val reportedUserId: String,
    val reportedUsername: String,
    val reporterUserId: String,
    val reporterUsername: String,
    val reason: String,
    val status: String = "PENDING", // "PENDING", "REVIEWED", "DISMISSED", "ACTION_TAKEN"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_analytics")
data class LiveAnalytics(
    @PrimaryKey val streamId: Int,
    val creatorUserId: String,
    val peakViewers: Int = 0,
    val totalViewersCount: Int = 0,
    val totalLikes: Int = 0,
    val totalComments: Int = 0,
    val totalGiftsCount: Int = 0,
    val totalCoinsEarned: Int = 0,
    val durationSeconds: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

data class LoginActivityRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val username: String,
    val authMethod: String, // "EMAIL", "PHONE", "GOOGLE", "DEMO_PRESET"
    val status: String, // "SUCCESS", "FAILED_PASSWORD", "ACCOUNT_SUSPENDED", "BLOCKED_ATTEMPT"
    val device: String = "Android Phone - Addis Ababa",
    val ipAddress: String = "197.156.78.102",
    val details: String = ""
)

data class CreatorApplication(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,
    val username: String,
    val category: String,
    val reason: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val appliedAt: Long = System.currentTimeMillis()
)

data class UserSession(
    val sessionId: String = java.util.UUID.randomUUID().toString(),
    val userId: String,
    val username: String,
    val role: String,
    val device: String = "Android Phone",
    val ipAddress: String = "197.156.78.102",
    val loginTime: Long = System.currentTimeMillis(),
    var isActive: Boolean = true
)





