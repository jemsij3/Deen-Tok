package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class DeenTokRepository(private val deenTokDao: DeenTokDao) {

    val allVideos: Flow<List<Video>> = deenTokDao.getAllVideos()
    val allMessages: Flow<List<Message>> = deenTokDao.getAllMessages()
    val allNotifications: Flow<List<Notification>> = deenTokDao.getAllNotifications()
    val allUsers: Flow<List<User>> = deenTokDao.getAllUsers()
    val allCategories: Flow<List<Category>> = deenTokDao.getAllCategories()
    val allReports: Flow<List<Report>> = deenTokDao.getAllReports()
    val allAdvertisements: Flow<List<Advertisement>> = deenTokDao.getAllAdvertisements()
    val allAdminLogs: Flow<List<AdminLog>> = deenTokDao.getAllAdminLogs()

    suspend fun insertUser(user: User) {
        deenTokDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        deenTokDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        deenTokDao.deleteUser(user)
    }

    suspend fun deleteUserById(userId: String) {
        deenTokDao.deleteUserById(userId)
    }

    suspend fun insertCategory(category: Category) {
        deenTokDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        deenTokDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        deenTokDao.deleteCategory(category)
    }

    suspend fun insertReport(report: Report) {
        deenTokDao.insertReport(report)
    }

    suspend fun updateReport(report: Report) {
        deenTokDao.updateReport(report)
    }

    suspend fun deleteReport(report: Report) {
        deenTokDao.deleteReport(report)
    }

    suspend fun insertAdvertisement(ad: Advertisement) {
        deenTokDao.insertAdvertisement(ad)
    }

    suspend fun updateAdvertisement(ad: Advertisement) {
        deenTokDao.updateAdvertisement(ad)
    }

    suspend fun deleteAdvertisement(ad: Advertisement) {
        deenTokDao.deleteAdvertisement(ad)
    }

    suspend fun insertAdminLog(log: AdminLog) {
        deenTokDao.insertAdminLog(log)
    }


    fun getVideosByCategory(category: String): Flow<List<Video>> {
        return deenTokDao.getVideosByCategory(category)
    }

    fun getCommentsForVideo(videoId: Int): Flow<List<Comment>> {
        return deenTokDao.getCommentsForVideo(videoId)
    }

    val allComments: Flow<List<Comment>> = deenTokDao.getAllComments()

    suspend fun deleteCommentById(commentId: Int) {
        deenTokDao.deleteCommentById(commentId)
    }

    suspend fun insertVideo(video: Video): Long {
        return deenTokDao.insertVideo(video)
    }

    suspend fun updateVideo(video: Video) {
        deenTokDao.updateVideo(video)
    }

    suspend fun deleteVideo(video: Video) {
        deenTokDao.deleteVideo(video)
    }

    suspend fun deleteVideoById(videoId: Int) {
        deenTokDao.deleteVideoById(videoId)
    }

    suspend fun clearAllVideos() {
        deenTokDao.clearAllVideos()
    }

    suspend fun insertComment(comment: Comment) {
        deenTokDao.insertComment(comment)
    }

    suspend fun insertMessage(message: Message) {
        deenTokDao.insertMessage(message)
    }

    suspend fun insertNotification(notification: Notification) {
        deenTokDao.insertNotification(notification)
    }

    suspend fun markNotificationAsRead(id: Int) {
        deenTokDao.markNotificationAsRead(id)
    }

    suspend fun toggleLikeVideo(video: Video) {
        val updatedVideo = video.copy(
            isLiked = !video.isLiked,
            likesCount = if (video.isLiked) video.likesCount - 1 else video.likesCount + 1
        )
        deenTokDao.updateVideo(updatedVideo)

        // Create a notification if liked
        if (updatedVideo.isLiked) {
            val notif = Notification(
                type = "LIKE",
                title = "New Like!",
                description = "liked your video: \"${video.title}\"",
                sourceUsername = "you" // In a real app this is the current user
            )
            deenTokDao.insertNotification(notif)
        }
    }

    suspend fun toggleFollowCreator(video: Video) {
        val updatedVideo = video.copy(
            isFollowingCreator = !video.isFollowingCreator
        )
        // Update all videos by this creator to sync following state
        // In our simple offline demo, we'll retrieve all videos and update them or do it dynamically in our state.
        // Let's just update this video record first.
        deenTokDao.updateVideo(updatedVideo)

        // Create notification
        if (updatedVideo.isFollowingCreator) {
            deenTokDao.insertNotification(
                Notification(
                    type = "FOLLOW",
                    title = "New Follower!",
                    description = "started following you.",
                    sourceUsername = video.username
                )
            )
        }
    }

    fun getUserPreferences(userId: String): Flow<UserPreferences?> {
        return deenTokDao.getUserPreferences(userId)
    }

    suspend fun insertUserPreferences(preferences: UserPreferences) {
        deenTokDao.insertUserPreferences(preferences)
    }

    val recentSearches: Flow<List<RecentSearch>> = deenTokDao.getRecentSearches()

    suspend fun insertRecentSearch(query: String) {
        if (query.isNotBlank()) {
            deenTokDao.deleteRecentSearch(query)
            deenTokDao.insertRecentSearch(RecentSearch(query = query))
        }
    }

    suspend fun deleteRecentSearch(query: String) {
        deenTokDao.deleteRecentSearch(query)
    }

    suspend fun clearRecentSearches() {
        deenTokDao.clearRecentSearches()
    }

    suspend fun prepopulateIfNeeded() {
        // Clear all videos initially to make the surface perfectly clear for real, perfect video posting!
        deenTokDao.clearAllVideos()

        // Seed default Islamic videos
        val defaultVideos = listOf(
            Video(
                id = 1,
                userId = "creator_1",
                username = "@quran_reminders",
                userAvatarUrl = "QR",
                videoUrl = "",
                thumbnailUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=300&q=80",
                title = "Beautiful Surah Ar-Rahman Recitation 📖✨",
                description = "Let the beautiful words of Surah Ar-Rahman soothe your soul. Verily, in the remembrance of Allah do hearts find rest. #quran #peace #deentok",
                likesCount = 2450,
                commentsCount = 3,
                sharesCount = 189,
                viewsCount = 5400,
                isPrivate = false,
                isLiked = false,
                isFollowingCreator = false,
                musicName = "Soothing Quran Recitation - Sheikh Mishary",
                category = "Quran",
                quranRef = "Surah Ar-Rahman (55:1-13)",
                contentType = "Quran"
            ),
            Video(
                id = 2,
                userId = "creator_2",
                username = "@sunnah_gems",
                userAvatarUrl = "SG",
                videoUrl = "",
                thumbnailUrl = "https://images.unsplash.com/photo-1519817650390-64a93db51149?auto=format&fit=crop&w=300&q=80",
                title = "Hadith on Kindness & Mercy ❤️",
                description = "Prophet Muhammad (ﷺ) said: 'Allah will not be merciful to those who are not merciful to people.' Let's spread love and compassion. #hadith #sunnah #mercy",
                likesCount = 1820,
                commentsCount = 0,
                sharesCount = 98,
                viewsCount = 3100,
                isPrivate = false,
                isLiked = false,
                isFollowingCreator = true,
                musicName = "Soft Ambient Nasheed Instrumental",
                category = "Hadith",
                hadithRef = "Sahih al-Bukhari 7376",
                contentType = "Hadith"
            ),
            Video(
                id = 3,
                userId = "creator_3",
                username = "@muslim_reminders",
                userAvatarUrl = "MR",
                videoUrl = "",
                thumbnailUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=300&q=80",
                title = "The Power of Sincere Dua 🤲🏽",
                description = "Allah says: 'Call upon Me; I will respond to you.' Never give up on your prayers. Your breakthrough is coming. #dua #prayer #trust",
                likesCount = 3110,
                commentsCount = 0,
                sharesCount = 275,
                viewsCount = 6800,
                isPrivate = false,
                isLiked = false,
                isFollowingCreator = false,
                musicName = "Beautiful Dua Melodic Background",
                category = "Dua",
                quranRef = "Surah Ghafir (40:60)",
                contentType = "Dua"
            ),
            Video(
                id = 4,
                userId = "creator_4",
                username = "@deen_history",
                userAvatarUrl = "DH",
                videoUrl = "",
                thumbnailUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=300&q=80",
                title = "Islamic Golden Age: House of Wisdom 🏛️",
                description = "A short journey through the history of Baghdad's House of Wisdom, where science, mathematics, and translation flourished. #history #knowledge #scholars",
                likesCount = 1250,
                commentsCount = 0,
                sharesCount = 42,
                viewsCount = 2400,
                isPrivate = false,
                isLiked = false,
                isFollowingCreator = false,
                musicName = "Classical Oud Background",
                category = "Islamic History",
                sourceInfo = "Al-Khalili, Science and Islam",
                contentType = "Story"
            )
        )
        // For real posts, do not insert any default demo videos
        // for (video in defaultVideos) {
        //     deenTokDao.insertVideo(video)
        // }

        // Seed default comments for the first video
        val defaultComments = listOf(
            Comment(videoId = 1, username = "@marcus_dev", userAvatarUrl = "MD", text = "SubhanAllah, extremely beautiful recitation! Peace of mind."),
            Comment(videoId = 1, username = "@alice_codes", userAvatarUrl = "AC", text = "This is indeed what my heart needed to hear today. Thank you for sharing!"),
            Comment(videoId = 1, username = "@casual_scroll", userAvatarUrl = "CS", text = "Verily, in the remembrance of Allah do hearts find rest.")
        )
        // No default videos exist, so we do not seed comments for them initially
        // for (comment in defaultComments) {
        //     deenTokDao.insertComment(comment)
        // }

        // Prepopulate notifications if empty
        val currentNotifications = deenTokDao.getAllNotifications().first()
        if (currentNotifications.isEmpty()) {
            val defaultNotifications = listOf(
                Notification(
                    type = "SYSTEM",
                    title = "Welcome to DeenTok! 🕋✨",
                    description = "Welcome to your new Islamic short-video and learning community. Start sharing beneficial reminders, Quran, Hadith, and connecting with the global Muslim community!",
                    sourceUsername = "DeenTok Team"
                ),
                Notification(
                    type = "FOLLOW",
                    title = "New Follower!",
                    description = "started following your profile. Welcome them aboard!",
                    sourceUsername = "@marcus_dev"
                ),
                Notification(
                    type = "COMMENT",
                    title = "New Comment!",
                    description = "commented on your video: \"Beautiful reminder! Let me download the source!\"",
                    sourceUsername = "@alice_codes"
                ),
                Notification(
                    type = "LIKE",
                    title = "New Like!",
                    description = "liked your video. Keep creating amazing things!",
                    sourceUsername = "@modular_synth"
                )
            )

            for (notif in defaultNotifications) {
                deenTokDao.insertNotification(notif)
            }
        }

        // Prepopulate messages if empty
        val currentMessages = deenTokDao.getAllMessages().first()
        if (currentMessages.isEmpty()) {
            val defaultMessages = listOf(
                Message(
                    senderId = "marcus123",
                    senderName = "@marcus_dev",
                    receiverId = "current_user",
                    text = "As-salamu alaykum! Welcome to DeenTok! Let me know if you like the clean, peaceful layout."
                ),
                Message(
                    senderId = "current_user",
                    senderName = "@me",
                    receiverId = "marcus123",
                    text = "Wa alaykumu s-salam! I love it! The scrolling and modular structure is superb."
                ),
                Message(
                    senderId = "marcus123",
                    senderName = "@marcus_dev",
                    receiverId = "current_user",
                    text = "Alhamdulillah! We're planning to migrate to a real server soon. For now, the Room DB acts as a full-fidelity offline cache. Let's keep building! 🚀"
                )
            )

            for (msg in defaultMessages) {
                deenTokDao.insertMessage(msg)
            }
        }

        // Prepopulate Admin Dashboard tables if empty
        val currentUsers = deenTokDao.getAllUsers().first()
        if (currentUsers.isEmpty()) {
            val defaultUsers = listOf(
                User(userId = "current_user", username = "@me", avatarUrl = "ME", role = "ADMIN", status = "ACTIVE", bio = "System Administrator", isVerified = true, verificationType = "Verified Scholar"),
                User(userId = "marcus123", username = "@marcus_dev", avatarUrl = "MD", role = "CREATOR", status = "ACTIVE", bio = "Verified Scholar & Teacher", isVerified = true, website = "https://deentok.app/marcus", verificationType = "Verified Scholar"),
                User(userId = "alice99", username = "@alice_codes", avatarUrl = "AC", role = "CREATOR", status = "ACTIVE", bio = "Islamic Art & Calligraphy Studio", isVerified = true, verificationType = "Islamic Organization"),
                User(userId = "spammer_boy", username = "@spam_bot", avatarUrl = "SB", role = "USER", status = "SUSPENDED", bio = "Click link for free tokens!"),
                User(userId = "normal_user", username = "@casual_scroll", avatarUrl = "CS", role = "USER", status = "ACTIVE", bio = "Just standard browsing")
            )
            deenTokDao.insertUsers(defaultUsers)
        }

        val currentCategories = deenTokDao.getAllCategories().first()
        if (currentCategories.isEmpty()) {
            val defaultCategories = listOf(
                Category(name = "Quran", isTrending = true, hashtagCount = 5200),
                Category(name = "Hadith", isTrending = true, hashtagCount = 4300),
                Category(name = "Tafsir", isTrending = true, hashtagCount = 2100),
                Category(name = "Seerah", isTrending = false, hashtagCount = 1800),
                Category(name = "Islamic Knowledge", isTrending = true, hashtagCount = 3400),
                Category(name = "Dawah", isTrending = true, hashtagCount = 2900),
                Category(name = "Islamic History", isTrending = false, hashtagCount = 1500),
                Category(name = "Islamic Lifestyle", isTrending = true, hashtagCount = 3100),
                Category(name = "Dua", isTrending = true, hashtagCount = 4800),
                Category(name = "Ramadan", isTrending = true, hashtagCount = 6700),
                Category(name = "Hajj & Umrah", isTrending = false, hashtagCount = 1200),
                Category(name = "Muslim Community", isTrending = true, hashtagCount = 2500)
            )
            for (cat in defaultCategories) {
                deenTokDao.insertCategory(cat)
            }
        }

        val currentReports = deenTokDao.getAllReports().first()
        if (currentReports.isEmpty()) {
            val defaultReports = listOf(
                Report(type = "SPAM", contentId = "spammer_boy", reportedItemTitle = "@spam_bot Profile", reason = "Spamming commercial links in Islamic groups", reportedBy = "@alice_codes", status = "PENDING"),
                Report(type = "VIDEO", contentId = "3", reportedItemTitle = "Sincere Dua Video", reason = "Self-promotion instead of informative description", reportedBy = "@casual_scroll", status = "PENDING"),
                Report(type = "COMMENT", contentId = "1", reportedItemTitle = "Comment: SubhanAllah", reason = "Duplicate spam behavior", reportedBy = "@marcus_dev", status = "CLOSED")
            )
            for (rep in defaultReports) {
                deenTokDao.insertReport(rep)
            }
        }

        val currentAds = deenTokDao.getAllAdvertisements().first()
        if (currentAds.isEmpty()) {
            val defaultAds = listOf(
                Advertisement(title = "Learn Arabic with Qur'an Academy", imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=300&q=80", targetUrl = "https://deentok.app/arabic", status = "ACTIVE", views = 1540, clicks = 245),
                Advertisement(title = "DeenTok Hajj Guide Booklets", imageUrl = "https://images.unsplash.com/photo-1519817650390-64a93db51149?auto=format&fit=crop&w=300&q=80", targetUrl = "https://deentok.app/hajj", status = "INACTIVE", views = 900, clicks = 48)
            )
            for (ad in defaultAds) {
                deenTokDao.insertAdvertisement(ad)
            }
        }

        val currentLogs = deenTokDao.getAllAdminLogs().first()
        if (currentLogs.isEmpty()) {
            val defaultLogs = listOf(
                AdminLog(adminUsername = "@me", action = "LOGIN", details = "Admin logged into DeenTok terminal"),
                AdminLog(adminUsername = "@me", action = "SUSPEND_USER", details = "Suspended @spam_bot for malicious behavior"),
                AdminLog(adminUsername = "@me", action = "UPDATE_SETTINGS", details = "Updated system categories to Islamic studies")
            )
            for (log in defaultLogs) {
                deenTokDao.insertAdminLog(log)
            }
        }

        val prefs = deenTokDao.getUserPreferences("current_user").first()
        if (prefs == null) {
            deenTokDao.insertUserPreferences(
                UserPreferences(
                    userId = "current_user",
                    email = "me@deentok.app",
                    phoneNumber = "+251912345678",
                    passwordHash = "admin123"
                )
            )
        }
    }
}
