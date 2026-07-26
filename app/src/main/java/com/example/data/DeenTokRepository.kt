package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class DeenTokRepository(private val deenTokDao: DeenTokDao) {

    val allVideos: Flow<List<Video>> = deenTokDao.getAllVideos()
    fun getVideoById(videoId: Int): Flow<Video?> = deenTokDao.getVideoById(videoId)
    val allMessages: Flow<List<Message>> = deenTokDao.getAllMessages()
    val allNotifications: Flow<List<Notification>> = deenTokDao.getAllNotifications()
    val allUsers: Flow<List<User>> = deenTokDao.getAllUsers()
    val allCategories: Flow<List<Category>> = deenTokDao.getAllCategories()
    val allReports: Flow<List<Report>> = deenTokDao.getAllReports()
    val allAdvertisements: Flow<List<Advertisement>> = deenTokDao.getAllAdvertisements()
    val allAdminLogs: Flow<List<AdminLog>> = deenTokDao.getAllAdminLogs()

    // --- DT COIN & GIFT ECONOMY FLOWS ---
    val allCoinPackages: Flow<List<CoinPackage>> = deenTokDao.getAllCoinPackages()
    val allVirtualGifts: Flow<List<VirtualGift>> = deenTokDao.getAllVirtualGifts()
    val allPaymentTransactions: Flow<List<PaymentTransaction>> = deenTokDao.getAllPaymentTransactions()
    val allGiftTransactions: Flow<List<GiftTransaction>> = deenTokDao.getAllGiftTransactions()
    val allWithdrawalRequests: Flow<List<WithdrawalRequest>> = deenTokDao.getAllWithdrawalRequests()
    val allCoinTransactions: Flow<List<CoinTransaction>> = deenTokDao.getAllCoinTransactions()

    fun getUserWallet(userId: String): Flow<UserWallet?> = deenTokDao.getUserWallet(userId)
    fun getUserPaymentTransactions(userId: String): Flow<List<PaymentTransaction>> = deenTokDao.getUserPaymentTransactions(userId)
    fun getUserCoinTransactions(userId: String): Flow<List<CoinTransaction>> = deenTokDao.getUserCoinTransactions(userId)
    fun getCreatorWallet(userId: String): Flow<CreatorWallet?> = deenTokDao.getCreatorWallet(userId)
    fun getCreatorGiftTransactions(creatorId: String): Flow<List<GiftTransaction>> = deenTokDao.getCreatorGiftTransactions(creatorId)
    fun getUserWithdrawalRequests(userId: String): Flow<List<WithdrawalRequest>> = deenTokDao.getUserWithdrawalRequests(userId)

    suspend fun insertOrUpdateUserWallet(wallet: UserWallet) = deenTokDao.insertOrUpdateUserWallet(wallet)

    suspend fun insertCoinPackage(pkg: CoinPackage) = deenTokDao.insertCoinPackage(pkg)
    suspend fun updateCoinPackage(pkg: CoinPackage) = deenTokDao.updateCoinPackage(pkg)
    suspend fun deleteCoinPackage(pkg: CoinPackage) = deenTokDao.deleteCoinPackage(pkg)

    suspend fun insertPaymentTransaction(tx: PaymentTransaction): Long = deenTokDao.insertPaymentTransaction(tx)
    suspend fun updatePaymentTransaction(tx: PaymentTransaction) = deenTokDao.updatePaymentTransaction(tx)

    suspend fun insertCoinTransaction(tx: CoinTransaction) = deenTokDao.insertCoinTransaction(tx)

    suspend fun insertVirtualGift(gift: VirtualGift) = deenTokDao.insertVirtualGift(gift)
    suspend fun updateVirtualGift(gift: VirtualGift) = deenTokDao.updateVirtualGift(gift)
    suspend fun deleteVirtualGift(gift: VirtualGift) = deenTokDao.deleteVirtualGift(gift)

    suspend fun insertGiftTransaction(tx: GiftTransaction): Long = deenTokDao.insertGiftTransaction(tx)

    suspend fun insertOrUpdateCreatorWallet(wallet: CreatorWallet) = deenTokDao.insertOrUpdateCreatorWallet(wallet)

    suspend fun insertWithdrawalRequest(req: WithdrawalRequest): Long = deenTokDao.insertWithdrawalRequest(req)
    suspend fun updateWithdrawalRequest(req: WithdrawalRequest) = deenTokDao.updateWithdrawalRequest(req)


    // --- LIVE STREAMING FLOWS & REPOSITORY METHODS ---
    val activeLiveStreams: Flow<List<LiveStream>> = deenTokDao.getActiveLiveStreams()
    val allLiveStreams: Flow<List<LiveStream>> = deenTokDao.getAllLiveStreams()
    val allLiveReports: Flow<List<LiveReport>> = deenTokDao.getAllLiveReports()

    fun getLiveStreamById(streamId: Int): Flow<LiveStream?> = deenTokDao.getLiveStreamById(streamId)
    suspend fun getActiveLiveStreamByCreator(userId: String): LiveStream? = deenTokDao.getActiveLiveStreamByCreator(userId)
    suspend fun insertLiveStream(stream: LiveStream): Long = deenTokDao.insertLiveStream(stream)
    suspend fun updateLiveStream(stream: LiveStream) = deenTokDao.updateLiveStream(stream)
    suspend fun deleteLiveStreamById(streamId: Int) = deenTokDao.deleteLiveStreamById(streamId)

    fun getLiveChatMessages(streamId: Int): Flow<List<LiveChatMessage>> = deenTokDao.getLiveChatMessages(streamId)
    suspend fun insertLiveChatMessage(msg: LiveChatMessage): Long = deenTokDao.insertLiveChatMessage(msg)
    suspend fun updateLiveChatMessage(msg: LiveChatMessage) = deenTokDao.updateLiveChatMessage(msg)
    suspend fun deleteLiveChatMessage(messageId: Int) = deenTokDao.deleteLiveChatMessage(messageId)

    fun getLiveViewers(streamId: Int): Flow<List<LiveViewer>> = deenTokDao.getLiveViewers(streamId)
    suspend fun insertOrUpdateLiveViewer(viewer: LiveViewer): Long = deenTokDao.insertOrUpdateLiveViewer(viewer)
    suspend fun removeLiveViewer(streamId: Int, userId: String) = deenTokDao.removeLiveViewer(streamId, userId)

    fun getLiveModerators(streamId: Int): Flow<List<LiveModerator>> = deenTokDao.getLiveModerators(streamId)
    suspend fun insertLiveModerator(mod: LiveModerator) = deenTokDao.insertLiveModerator(mod)
    suspend fun removeLiveModerator(streamId: Int, userId: String) = deenTokDao.removeLiveModerator(streamId, userId)

    suspend fun insertLiveReport(report: LiveReport): Long = deenTokDao.insertLiveReport(report)
    suspend fun updateLiveReport(report: LiveReport) = deenTokDao.updateLiveReport(report)

    fun getLiveAnalytics(streamId: Int): Flow<LiveAnalytics?> = deenTokDao.getLiveAnalytics(streamId)
    suspend fun insertOrUpdateLiveAnalytics(analytics: LiveAnalytics) = deenTokDao.insertOrUpdateLiveAnalytics(analytics)


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

        // --- PREPOPULATE DT COIN PACKAGES ---
        val packages = deenTokDao.getAllCoinPackages().first()
        if (packages.isEmpty()) {
            val defaultPackages = listOf(
                CoinPackage(id = "pkg_starter", title = "Starter Package", coinAmount = 100, bonusAmount = 0, priceEtb = 50.0, badge = "Starter"),
                CoinPackage(id = "pkg_popular", title = "Popular Package", coinAmount = 500, bonusAmount = 50, priceEtb = 250.0, badge = "Most Popular"),
                CoinPackage(id = "pkg_premium", title = "Premium Package", coinAmount = 1200, bonusAmount = 200, priceEtb = 500.0, badge = "Best Value"),
                CoinPackage(id = "pkg_vip", title = "VIP Package", coinAmount = 3000, bonusAmount = 600, priceEtb = 1000.0, badge = "VIP Gold")
            )
            for (p in defaultPackages) {
                deenTokDao.insertCoinPackage(p)
            }
        }

        // --- PREPOPULATE VIRTUAL GIFTS ---
        val existingGifts = deenTokDao.getAllVirtualGifts().first()
        val existingNames = existingGifts.map { it.name.trim().lowercase() }.toSet()

        val defaultGifts = listOf(
            VirtualGift(id = "gift_rose", name = "Rose", category = "BASIC", coinPrice = 5, iconSymbol = "🌹", animationType = "FLOAT_BURST"),
            VirtualGift(id = "gift_star", name = "Star", category = "BASIC", coinPrice = 10, iconSymbol = "⭐", animationType = "SPARKLE_BURST"),
            VirtualGift(id = "gift_heart", name = "Heart", category = "BASIC", coinPrice = 20, iconSymbol = "❤️", animationType = "HEART_FLY"),
            VirtualGift(id = "gift_crescent", name = "Crescent Moon", category = "BASIC", coinPrice = 35, iconSymbol = "🌙", animationType = "MOON_GLOW"),
            VirtualGift(id = "gift_scroll", name = "Quran Scroll", category = "SPECIAL", coinPrice = 100, iconSymbol = "📜", animationType = "GOLDEN_EXPAND"),
            VirtualGift(id = "gift_lantern", name = "Fanous Lantern", category = "SPECIAL", coinPrice = 250, iconSymbol = "🏮", animationType = "LANTERN_LIGHT"),
            VirtualGift(id = "gift_minaret", name = "Gold Minaret", category = "SPECIAL", coinPrice = 500, iconSymbol = "🕌", animationType = "MINARET_BEAM"),
            VirtualGift(id = "gift_kaaba", name = "Kaaba Model", category = "PREMIUM", coinPrice = 1000, iconSymbol = "🕋", animationType = "KAABA_LIGHT"),
            VirtualGift(id = "gift_crown", name = "Royal Crown", category = "PREMIUM", coinPrice = 2500, iconSymbol = "👑", animationType = "CROWN_ROYAL"),
            VirtualGift(id = "gift_light", name = "Light of Deen", category = "PREMIUM", coinPrice = 5000, iconSymbol = "✨", animationType = "LIGHT_BEAM"),

            // 54 DeenTok Virtual Gifts
            VirtualGift(id = "gift_jannah_pearl", name = "Jannah Pearl", category = "BASIC", coinPrice = 1, iconSymbol = "🌼", animationType = "PEARL_GLOW"),
            VirtualGift(id = "gift_rayyan_rose", name = "Rayyan Rose", category = "BASIC", coinPrice = 2, iconSymbol = "🌹", animationType = "ROSE_BLOOM"),
            VirtualGift(id = "gift_hilal", name = "Hilal", category = "BASIC", coinPrice = 3, iconSymbol = "🌙", animationType = "CRESCENT_SWIRL"),
            VirtualGift(id = "gift_najm", name = "Najm", category = "BASIC", coinPrice = 4, iconSymbol = "⭐", animationType = "STAR_SPARKLE"),
            VirtualGift(id = "gift_ihsan_leaf", name = "Ihsan Leaf", category = "BASIC", coinPrice = 5, iconSymbol = "🌿", animationType = "LEAF_DRIFT"),
            VirtualGift(id = "gift_aman_dove", name = "Aman Dove", category = "BASIC", coinPrice = 6, iconSymbol = "🕊️", animationType = "DOVE_FLIGHT"),
            VirtualGift(id = "gift_jannah_blossom", name = "Jannah Blossom", category = "BASIC", coinPrice = 7, iconSymbol = "🌸", animationType = "BLOSSOM_PETALS"),
            VirtualGift(id = "gift_rahmah", name = "Rahmah", category = "BASIC", coinPrice = 8, iconSymbol = "🌧️", animationType = "RAIN_SHOWER"),
            VirtualGift(id = "gift_noor", name = "Noor", category = "BASIC", coinPrice = 9, iconSymbol = "🌟", animationType = "NOOR_RADIANCE"),
            VirtualGift(id = "gift_sidrah", name = "Sidrah", category = "BASIC", coinPrice = 10, iconSymbol = "🌿", animationType = "TREE_BRANCH_GLOW"),
            VirtualGift(id = "gift_sidq_crystal", name = "Sidq Crystal", category = "BASIC", coinPrice = 15, iconSymbol = "💎", animationType = "CRYSTAL_SHINE"),
            VirtualGift(id = "gift_bashir_star", name = "Bashir Star", category = "BASIC", coinPrice = 20, iconSymbol = "⭐", animationType = "STAR_BURST"),
            VirtualGift(id = "gift_qamar_light", name = "Qamar Light", category = "BASIC", coinPrice = 25, iconSymbol = "🌙", animationType = "MOONBEAM_GLOW"),
            VirtualGift(id = "gift_naim_blossom", name = "Naim Blossom", category = "BASIC", coinPrice = 30, iconSymbol = "🌺", animationType = "FLOWER_POP"),
            VirtualGift(id = "gift_rayan_palm", name = "Rayan Palm", category = "BASIC", coinPrice = 40, iconSymbol = "🌴", animationType = "PALM_SWAY"),
            VirtualGift(id = "gift_dhikr_light", name = "Dhikr Light", category = "BASIC", coinPrice = 50, iconSymbol = "💫", animationType = "SPIRAL_LIGHT"),
            VirtualGift(id = "gift_husn_flower", name = "Husn Flower", category = "BASIC", coinPrice = 60, iconSymbol = "🌸", animationType = "PETAL_FLUTTER"),
            VirtualGift(id = "gift_amin_heart", name = "Amin Heart", category = "BASIC", coinPrice = 70, iconSymbol = "💚", animationType = "HEART_PULSE"),
            VirtualGift(id = "gift_tawakkul_branch", name = "Tawakkul Branch", category = "BASIC", coinPrice = 80, iconSymbol = "🌿", animationType = "GOLDEN_LEAVES"),
            VirtualGift(id = "gift_furqan_star", name = "Furqan Star", category = "BASIC", coinPrice = 90, iconSymbol = "⭐", animationType = "STAR_BEAM"),
            VirtualGift(id = "gift_shukr_sunrise", name = "Shukr Sunrise", category = "SPECIAL", coinPrice = 100, iconSymbol = "🌅", animationType = "SUNRISE_GLOW"),
            VirtualGift(id = "gift_adl_gem", name = "Adl Gem", category = "SPECIAL", coinPrice = 120, iconSymbol = "💎", animationType = "GEM_PRISM"),
            VirtualGift(id = "gift_ghayth_rain", name = "Ghayth Rain", category = "SPECIAL", coinPrice = 150, iconSymbol = "🌧️", animationType = "GOLDEN_RAIN"),
            VirtualGift(id = "gift_wudd_dove", name = "Wudd Dove", category = "SPECIAL", coinPrice = 180, iconSymbol = "🕊️", animationType = "DOVE_PAIR_FLIGHT"),
            VirtualGift(id = "gift_tayyib_blossom", name = "Tayyib Blossom", category = "SPECIAL", coinPrice = 200, iconSymbol = "🌺", animationType = "BLOSSOM_CASCADE"),
            VirtualGift(id = "gift_basirah_light", name = "Basirah Light", category = "SPECIAL", coinPrice = 250, iconSymbol = "🌟", animationType = "AURA_PULSE"),
            VirtualGift(id = "gift_yusr_moon", name = "Yusr Moon", category = "SPECIAL", coinPrice = 300, iconSymbol = "🌙", animationType = "FULL_MOON_SHINE"),
            VirtualGift(id = "gift_khayr_garden", name = "Khayr Garden", category = "SPECIAL", coinPrice = 400, iconSymbol = "🌿", animationType = "GARDEN_BLOOM"),
            VirtualGift(id = "gift_ihsan_crystal", name = "Ihsan Crystal", category = "SPECIAL", coinPrice = 500, iconSymbol = "💎", animationType = "DIAMOND_EXPLOSION"),
            VirtualGift(id = "gift_jood_flower", name = "Jood Flower", category = "SPECIAL", coinPrice = 600, iconSymbol = "🌸", animationType = "FLOWER_SHOWER"),
            VirtualGift(id = "gift_amal_star", name = "Amal Star", category = "SPECIAL", coinPrice = 700, iconSymbol = "⭐", animationType = "SHOOTING_STAR"),
            VirtualGift(id = "gift_ukhuwwah_dove", name = "Ukhuwwah Dove", category = "SPECIAL", coinPrice = 800, iconSymbol = "🕊️", animationType = "DOVE_AURORA"),
            VirtualGift(id = "gift_salam_garden", name = "Salam Garden", category = "SPECIAL", coinPrice = 900, iconSymbol = "🌴", animationType = "PALM_OASIS_GLOW"),
            VirtualGift(id = "gift_bashair_light", name = "Basha'ir Light", category = "PREMIUM", coinPrice = 1000, iconSymbol = "💫", animationType = "COSMIC_LIGHT"),
            VirtualGift(id = "gift_nur_blossom", name = "Nur Blossom", category = "PREMIUM", coinPrice = 1500, iconSymbol = "🌺", animationType = "GOLDEN_NUR_BLOOM"),
            VirtualGift(id = "gift_falah_sunrise", name = "Falah Sunrise", category = "PREMIUM", coinPrice = 2000, iconSymbol = "🌅", animationType = "HORIZON_BEAM"),
            VirtualGift(id = "gift_haya_leaf", name = "Haya Leaf", category = "PREMIUM", coinPrice = 2500, iconSymbol = "💚", animationType = "EMERALD_AURA"),
            VirtualGift(id = "gift_wafa_branch", name = "Wafa Branch", category = "PREMIUM", coinPrice = 3000, iconSymbol = "🌿", animationType = "GOLDEN_TREE_BURST"),
            VirtualGift(id = "gift_sadaqah_gem", name = "Sadaqah Gem", category = "PREMIUM", coinPrice = 4000, iconSymbol = "💎", animationType = "ROYAL_GEM_SHINE"),
            VirtualGift(id = "gift_iman_star", name = "Iman Star", category = "PREMIUM", coinPrice = 5000, iconSymbol = "⭐", animationType = "CONSTELLATION_BURST"),
            VirtualGift(id = "gift_qadr_moon", name = "Qadr Moon", category = "PREMIUM", coinPrice = 6000, iconSymbol = "🌙", animationType = "CELESTIAL_CRESCENT"),
            VirtualGift(id = "gift_rida_blossom", name = "Rida Blossom", category = "PREMIUM", coinPrice = 7000, iconSymbol = "🌸", animationType = "PARADISE_BLOOM"),
            VirtualGift(id = "gift_mahabbah_dove", name = "Mahabbah Dove", category = "PREMIUM", coinPrice = 8000, iconSymbol = "🕊️", animationType = "GOLDEN_DOVE_SWARM"),
            VirtualGift(id = "gift_bushra_light", name = "Bushra Light", category = "PREMIUM", coinPrice = 9000, iconSymbol = "🌟", animationType = "SUPERNOVA_GLOW"),
            VirtualGift(id = "gift_tayyib_palm", name = "Tayyib Palm", category = "PREMIUM", coinPrice = 10000, iconSymbol = "🌴", animationType = "GOLDEN_OASIS"),
            VirtualGift(id = "gift_tuba_tree", name = "Tuba Tree", category = "PREMIUM", coinPrice = 12000, iconSymbol = "🌴", animationType = "TUBA_TREE_JANNAH"),
            VirtualGift(id = "gift_hikmah_scroll", name = "Hikmah Scroll", category = "PREMIUM", coinPrice = 15000, iconSymbol = "📖", animationType = "DIVINE_SCROLL_UNFOLD"),
            VirtualGift(id = "gift_ikhlas_heart", name = "Ikhlas Heart", category = "PREMIUM", coinPrice = 20000, iconSymbol = "🤝", animationType = "GOLDEN_UNITY_PULSE"),
            VirtualGift(id = "gift_yaqeen_gem", name = "Yaqeen Gem", category = "PREMIUM", coinPrice = 25000, iconSymbol = "💎", animationType = "ULTIMATE_CRYSTAL_BEAM"),
            VirtualGift(id = "gift_barakah_bloom", name = "Barakah Bloom", category = "PREMIUM", coinPrice = 30000, iconSymbol = "🌸", animationType = "BARAKAH_PARADISE_SHOWER"),
            VirtualGift(id = "gift_sakinah", name = "Sakinah", category = "PREMIUM", coinPrice = 35000, iconSymbol = "🕊️", animationType = "SAKINAH_PEACE_AURA"),
            VirtualGift(id = "gift_taqwa_leaf", name = "Taqwa Leaf", category = "PREMIUM", coinPrice = 40000, iconSymbol = "💚", animationType = "TAQWA_EMERALD_SHIELD"),
            VirtualGift(id = "gift_salaam_dove", name = "Salaam Dove", category = "PREMIUM", coinPrice = 45000, iconSymbol = "🕊️", animationType = "SALAAM_ANGELIC_FLIGHT"),
            VirtualGift(id = "gift_fajr_light", name = "Fajr Light", category = "PREMIUM", coinPrice = 50000, iconSymbol = "🌅", animationType = "DIVINE_FAJR_SUNRISE")
        )

        for (g in defaultGifts) {
            if (!existingNames.contains(g.name.trim().lowercase())) {
                deenTokDao.insertVirtualGift(g)
            }
        }

        // --- PREPOPULATE USER WALLET & CREATOR WALLETS ---
        val wallet = deenTokDao.getUserWallet("current_user").first()
        if (wallet == null) {
            deenTokDao.insertOrUpdateUserWallet(UserWallet(userId = "current_user", coinBalance = 500))
            deenTokDao.insertCoinTransaction(
                CoinTransaction(
                    userId = "current_user",
                    type = "PURCHASE",
                    amount = 500,
                    description = "Welcome bonus DT Coins added to wallet"
                )
            )
        }

        val creatorWalletMe = deenTokDao.getCreatorWallet("current_user").first()
        if (creatorWalletMe == null) {
            deenTokDao.insertOrUpdateCreatorWallet(
                CreatorWallet(userId = "current_user", totalGiftsReceived = 6, totalCoinsEarned = 1200, rewardBalanceCoins = 1200)
            )
        }

        val creatorWalletMarcus = deenTokDao.getCreatorWallet("marcus123").first()
        if (creatorWalletMarcus == null) {
            deenTokDao.insertOrUpdateCreatorWallet(
                CreatorWallet(userId = "marcus123", totalGiftsReceived = 12, totalCoinsEarned = 2800, rewardBalanceCoins = 2800)
            )
        }

        // --- CLEAR DEMO LIVE STREAMS ---
        val liveStreams = deenTokDao.getActiveLiveStreams().first()
        liveStreams.forEach { stream ->
            if (stream.streamKey.startsWith("live_")) {
                deenTokDao.deleteLiveStreamById(stream.id)
            }
        }
    }

}
