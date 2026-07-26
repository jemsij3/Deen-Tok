package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeenTokDao {
    // Videos
    @Query("SELECT * FROM videos WHERE id = :videoId")
    fun getVideoById(videoId: Int): Flow<Video?>

    @Query("SELECT * FROM videos ORDER BY id DESC")
    fun getAllVideos(): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE category = :category ORDER BY id DESC")
    fun getVideosByCategory(category: String): Flow<List<Video>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: Video): Long

    @Update
    suspend fun updateVideo(video: Video)

    @Delete
    suspend fun deleteVideo(video: Video)

    @Query("DELETE FROM videos WHERE id = :videoId")
    suspend fun deleteVideoById(videoId: Int)

    @Query("DELETE FROM videos")
    suspend fun clearAllVideos()

    // Comments
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY createdAt ASC")
    fun getCommentsForVideo(videoId: Int): Flow<List<Comment>>

    @Query("SELECT * FROM comments ORDER BY id DESC")
    fun getAllComments(): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: Int)

    // Messages
    @Query("SELECT * FROM messages ORDER BY createdAt ASC")
    fun getAllMessages(): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    // Users
    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUserById(userId: String)

    // Categories
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    // Reports
    @Query("SELECT * FROM reports ORDER BY id DESC")
    fun getAllReports(): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report)

    @Update
    suspend fun updateReport(report: Report)

    @Delete
    suspend fun deleteReport(report: Report)

    // Advertisements
    @Query("SELECT * FROM advertisements ORDER BY id DESC")
    fun getAllAdvertisements(): Flow<List<Advertisement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvertisement(advertisement: Advertisement)

    @Update
    suspend fun updateAdvertisement(advertisement: Advertisement)

    @Delete
    suspend fun deleteAdvertisement(advertisement: Advertisement)

    // Admin Logs
    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC")
    fun getAllAdminLogs(): Flow<List<AdminLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminLog(log: AdminLog)

    // User Preferences
    @Query("SELECT * FROM user_preferences WHERE userId = :userId LIMIT 1")
    fun getUserPreferences(userId: String): Flow<UserPreferences?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferences)

    // Recent Searches
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(search: RecentSearch)

    @Query("DELETE FROM recent_searches WHERE query = :query")
    suspend fun deleteRecentSearch(query: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches()

    // --- DT COIN & GIFT ECONOMY DAO METHODS ---

    // User Wallet
    @Query("SELECT * FROM user_wallets WHERE userId = :userId LIMIT 1")
    fun getUserWallet(userId: String): Flow<UserWallet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserWallet(wallet: UserWallet)

    // Coin Packages
    @Query("SELECT * FROM coin_packages ORDER BY priceEtb ASC")
    fun getAllCoinPackages(): Flow<List<CoinPackage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoinPackage(pkg: CoinPackage)

    @Update
    suspend fun updateCoinPackage(pkg: CoinPackage)

    @Delete
    suspend fun deleteCoinPackage(pkg: CoinPackage)

    // Payment Transactions
    @Query("SELECT * FROM payment_transactions ORDER BY createdAt DESC")
    fun getAllPaymentTransactions(): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM payment_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserPaymentTransactions(userId: String): Flow<List<PaymentTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentTransaction(tx: PaymentTransaction): Long

    @Update
    suspend fun updatePaymentTransaction(tx: PaymentTransaction)

    // Coin Transactions
    @Query("SELECT * FROM coin_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserCoinTransactions(userId: String): Flow<List<CoinTransaction>>

    @Query("SELECT * FROM coin_transactions ORDER BY createdAt DESC")
    fun getAllCoinTransactions(): Flow<List<CoinTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoinTransaction(tx: CoinTransaction)

    // Virtual Gifts
    @Query("SELECT * FROM virtual_gifts ORDER BY coinPrice ASC")
    fun getAllVirtualGifts(): Flow<List<VirtualGift>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVirtualGift(gift: VirtualGift)

    @Update
    suspend fun updateVirtualGift(gift: VirtualGift)

    @Delete
    suspend fun deleteVirtualGift(gift: VirtualGift)

    // Gift Transactions
    @Query("SELECT * FROM gift_transactions ORDER BY createdAt DESC")
    fun getAllGiftTransactions(): Flow<List<GiftTransaction>>

    @Query("SELECT * FROM gift_transactions WHERE creatorUserId = :creatorId ORDER BY createdAt DESC")
    fun getCreatorGiftTransactions(creatorId: String): Flow<List<GiftTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGiftTransaction(tx: GiftTransaction): Long

    // Creator Wallet
    @Query("SELECT * FROM creator_wallets WHERE userId = :userId LIMIT 1")
    fun getCreatorWallet(userId: String): Flow<CreatorWallet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCreatorWallet(wallet: CreatorWallet)

    // Withdrawal Requests
    @Query("SELECT * FROM withdrawal_requests ORDER BY createdAt DESC")
    fun getAllWithdrawalRequests(): Flow<List<WithdrawalRequest>>

    @Query("SELECT * FROM withdrawal_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserWithdrawalRequests(userId: String): Flow<List<WithdrawalRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawalRequest(req: WithdrawalRequest): Long

    @Update
    suspend fun updateWithdrawalRequest(req: WithdrawalRequest)

    // --- LIVE STREAMING SYSTEM DAOS ---

    // Live Streams
    @Query("SELECT * FROM live_streams WHERE status = 'LIVE' ORDER BY startedAt DESC")
    fun getActiveLiveStreams(): Flow<List<LiveStream>>

    @Query("SELECT * FROM live_streams ORDER BY startedAt DESC")
    fun getAllLiveStreams(): Flow<List<LiveStream>>

    @Query("SELECT * FROM live_streams WHERE id = :streamId LIMIT 1")
    fun getLiveStreamById(streamId: Int): Flow<LiveStream?>

    @Query("SELECT * FROM live_streams WHERE creatorUserId = :userId AND status = 'LIVE' LIMIT 1")
    suspend fun getActiveLiveStreamByCreator(userId: String): LiveStream?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveStream(stream: LiveStream): Long

    @Update
    suspend fun updateLiveStream(stream: LiveStream)

    @Query("DELETE FROM live_streams WHERE id = :streamId")
    suspend fun deleteLiveStreamById(streamId: Int)

    // Live Chat Messages
    @Query("SELECT * FROM live_chat_messages WHERE streamId = :streamId ORDER BY timestamp ASC")
    fun getLiveChatMessages(streamId: Int): Flow<List<LiveChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveChatMessage(msg: LiveChatMessage): Long

    @Update
    suspend fun updateLiveChatMessage(msg: LiveChatMessage)

    @Query("DELETE FROM live_chat_messages WHERE id = :messageId")
    suspend fun deleteLiveChatMessage(messageId: Int)

    // Live Viewers
    @Query("SELECT * FROM live_viewers WHERE streamId = :streamId")
    fun getLiveViewers(streamId: Int): Flow<List<LiveViewer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLiveViewer(viewer: LiveViewer): Long

    @Query("DELETE FROM live_viewers WHERE streamId = :streamId AND userId = :userId")
    suspend fun removeLiveViewer(streamId: Int, userId: String)

    // Live Moderators
    @Query("SELECT * FROM live_moderators WHERE streamId = :streamId")
    fun getLiveModerators(streamId: Int): Flow<List<LiveModerator>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveModerator(mod: LiveModerator)

    @Query("DELETE FROM live_moderators WHERE streamId = :streamId AND userId = :userId")
    suspend fun removeLiveModerator(streamId: Int, userId: String)

    // Live Reports
    @Query("SELECT * FROM live_reports ORDER BY timestamp DESC")
    fun getAllLiveReports(): Flow<List<LiveReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveReport(report: LiveReport): Long

    @Update
    suspend fun updateLiveReport(report: LiveReport)

    // Live Analytics
    @Query("SELECT * FROM live_analytics WHERE streamId = :streamId LIMIT 1")
    fun getLiveAnalytics(streamId: Int): Flow<LiveAnalytics?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLiveAnalytics(analytics: LiveAnalytics)
}


