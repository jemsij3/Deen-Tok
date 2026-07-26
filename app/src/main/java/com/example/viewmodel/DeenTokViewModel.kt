package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    EXPLORE,
    UPLOAD,
    LIVE,
    MESSAGES,
    NOTIFICATIONS,
    PROFILE,
    SETTINGS,
    CREATOR_STUDIO,
    ADMIN_DASHBOARD,
    WALLET,
    POLICY_CENTER
}

class DeenTokViewModel(private val repository: DeenTokRepository) : ViewModel() {

    // Authentication State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Selected Language State
    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // Current Navigation State
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Home Screen sub-tab: 0 = "For You", 1 = "Following"
    private val _homeTab = MutableStateFlow(0)
    val homeTab: StateFlow<Int> = _homeTab.asStateFlow()

    // Search Query for Explore
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected Policy Center Tab (0: Community Guidelines, 1: Terms of Service, 2: Privacy Policy, 3: Intellectual Property)
    private val _selectedPolicyTab = MutableStateFlow(0)
    val selectedPolicyTab: StateFlow<Int> = _selectedPolicyTab.asStateFlow()

    fun openPolicyCenter(tabIndex: Int = 0) {
        _selectedPolicyTab.value = tabIndex
        _currentScreen.value = Screen.POLICY_CENTER
    }

    fun setPolicyTab(tabIndex: Int) {
        _selectedPolicyTab.value = tabIndex
    }

    // State flows from Room database
    val allVideos: StateFlow<List<Video>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<Message>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<Notification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<Report>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdvertisements: StateFlow<List<Advertisement>> = repository.allAdvertisements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdminLogs: StateFlow<List<AdminLog>> = repository.allAdminLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allComments: StateFlow<List<Comment>> = repository.allComments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<UserPreferences?> = repository.getUserPreferences("current_user")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentSearches: StateFlow<List<RecentSearch>> = repository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DT COIN & VIRTUAL GIFT ECONOMY STATEFLOWS ---
    val userWallet: StateFlow<UserWallet?> = repository.getUserWallet("current_user")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val creatorWallet: StateFlow<CreatorWallet?> = repository.getCreatorWallet("current_user")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCoinPackages: StateFlow<List<CoinPackage>> = repository.allCoinPackages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVirtualGifts: StateFlow<List<VirtualGift>> = repository.allVirtualGifts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userCoinTransactions: StateFlow<List<CoinTransaction>> = repository.getUserCoinTransactions("current_user")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPaymentTransactions: StateFlow<List<PaymentTransaction>> = repository.getUserPaymentTransactions("current_user")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val creatorGiftTransactions: StateFlow<List<GiftTransaction>> = repository.getCreatorGiftTransactions("current_user")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWithdrawalRequests: StateFlow<List<WithdrawalRequest>> = repository.getUserWithdrawalRequests("current_user")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- LIVE STREAMING STATEFLOWS & ADMIN SETTINGS ---
    val activeLiveStreams: StateFlow<List<LiveStream>> = repository.activeLiveStreams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLiveStreams: StateFlow<List<LiveStream>> = repository.allLiveStreams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLiveReports: StateFlow<List<LiveReport>> = repository.allLiveReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLiveFeatureEnabled = MutableStateFlow<Boolean>(true)
    val isLiveFeatureEnabled: StateFlow<Boolean> = _isLiveFeatureEnabled.asStateFlow()

    private val _minFollowersForLive = MutableStateFlow<Int>(0)
    val minFollowersForLive: StateFlow<Int> = _minFollowersForLive.asStateFlow()

    private val _liveBannedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val liveBannedUserIds: StateFlow<Set<String>> = _liveBannedUserIds.asStateFlow()

    private val _selectedLiveStreamId = MutableStateFlow<Int?>(null)
    val selectedLiveStreamId: StateFlow<Int?> = _selectedLiveStreamId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedLiveStream: StateFlow<LiveStream?> = _selectedLiveStreamId
        .flatMapLatest { id ->
            if (id != null) repository.getLiveStreamById(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val liveChatMessages: StateFlow<List<LiveChatMessage>> = _selectedLiveStreamId
        .flatMapLatest { id ->
            if (id != null) repository.getLiveChatMessages(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val liveViewers: StateFlow<List<LiveViewer>> = _selectedLiveStreamId
        .flatMapLatest { id ->
            if (id != null) repository.getLiveViewers(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val liveModerators: StateFlow<List<LiveModerator>> = _selectedLiveStreamId
        .flatMapLatest { id ->
            if (id != null) repository.getLiveModerators(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Economy StateFlows
    val allPaymentTransactions: StateFlow<List<PaymentTransaction>> = repository.allPaymentTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGiftTransactions: StateFlow<List<GiftTransaction>> = repository.allGiftTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawalRequests: StateFlow<List<WithdrawalRequest>> = repository.allWithdrawalRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCoinTransactions: StateFlow<List<CoinTransaction>> = repository.allCoinTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Gift Animation Event
    private val _activeGiftAnimation = MutableStateFlow<GiftAnimationEvent?>(null)
    val activeGiftAnimation: StateFlow<GiftAnimationEvent?> = _activeGiftAnimation.asStateFlow()

    fun clearActiveGiftAnimation() {
        _activeGiftAnimation.value = null
    }


    // Removed/soft-deleted videos memory cache for restoring
    private val _removedVideos = MutableStateFlow<List<Video>>(emptyList())
    val removedVideos: StateFlow<List<Video>> = _removedVideos.asStateFlow()

    // Currently selected video's ID for showing comments
    private val _activeVideoIdForComments = MutableStateFlow<Int?>(null)
    val activeVideoIdForComments: StateFlow<Int?> = _activeVideoIdForComments.asStateFlow()

    // Volume & Sound States
    private val _isVideoMuted = MutableStateFlow(false)
    val isVideoMuted: StateFlow<Boolean> = _isVideoMuted.asStateFlow()

    private val _videoVolume = MutableStateFlow(0.8f) // Default level (0.0f to 1.0f)
    val videoVolume: StateFlow<Float> = _videoVolume.asStateFlow()

    fun toggleVideoMute() {
        _isVideoMuted.value = !_isVideoMuted.value
    }

    fun setVideoVolume(volume: Float) {
        _videoVolume.value = volume.coerceIn(0f, 1f)
        if (volume > 0f && _isVideoMuted.value) {
            _isVideoMuted.value = false
        } else if (volume == 0f && !_isVideoMuted.value) {
            _isVideoMuted.value = true
        }
    }

    // Reactively stream comments for the active video
    val activeVideoComments: StateFlow<List<Comment>> = _activeVideoIdForComments
        .flatMapLatest { videoId ->
            if (videoId != null) {
                repository.getCommentsForVideo(videoId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated Active Chat partner (e.g. @marcus_dev)
    val activeChatPartner = "@marcus_dev"
    val activeChatPartnerId = "marcus123"

    // Mock Current User & Role State (RBAC)
    var currentUserId by mutableStateOf("current_user")
    var currentUsername by mutableStateOf("@me")
    var currentUserRole by mutableStateOf("SUPER_ADMIN") // "SUPER_ADMIN", "ADMIN", "CREATOR", "USER"
    var currentUserStatus by mutableStateOf("ACTIVE") // "ACTIVE", "SUSPENDED", "BANNED"
    var isEmailVerified by mutableStateOf(true)
    var isPhoneVerified by mutableStateOf(true)
    var currentUserEmail by mutableStateOf("admin@deentok.app")
    var currentUserPhone by mutableStateOf("+251911223344")
    var currentUserAvatar by mutableStateOf("ME")
    var currentUserBio by mutableStateOf("DeenTok creator. Sharing beneficial Islamic reminders, Quran recitation, and finding peace. 🕋✨")
    var currentUserWebsite by mutableStateOf("https://deentok.app/me")
    var currentUserTwitter by mutableStateOf("@me_deentok")
    var currentUserInstagram by mutableStateOf("@me_deen")
    var currentUserFollowersCount by mutableStateOf("0")
    var currentUserFollowingCount by mutableStateOf("0")
    var currentFollowersList by mutableStateOf(listOf<String>())

    // Security & Active Session State
    var activeSessionToken by mutableStateOf<String?>(java.util.UUID.randomUUID().toString())
    var activeSessionDevice by mutableStateOf("Android Phone - Addis Ababa")
    var activeSessionIp by mutableStateOf("197.156.78.102")

    private val _securityAlert = MutableStateFlow<String?>(null)
    val securityAlert: StateFlow<String?> = _securityAlert.asStateFlow()

    fun dismissSecurityAlert() {
        _securityAlert.value = null
    }

    // Security Logs & Activity Monitoring
    private val _loginActivityLogs = MutableStateFlow<List<LoginActivityRecord>>(
        listOf(
            LoginActivityRecord(username = "@me", authMethod = "EMAIL", status = "SUCCESS", device = "Android Phone - Addis Ababa", ipAddress = "197.156.78.102", details = "Logged in as Super Admin"),
            LoginActivityRecord(username = "@marcus_dev", authMethod = "EMAIL", status = "SUCCESS", device = "Samsung Galaxy S23", ipAddress = "197.156.12.88", details = "Logged in as Approved Creator"),
            LoginActivityRecord(username = "@spam_bot", authMethod = "EMAIL", status = "ACCOUNT_SUSPENDED", device = "Automated Client", ipAddress = "45.12.98.11", details = "Login blocked - Account suspended"),
            LoginActivityRecord(username = "@casual_scroll", authMethod = "PHONE", status = "SUCCESS", device = "Redmi Note 12", ipAddress = "197.156.45.201", details = "Logged in as Normal User")
        )
    )
    val loginActivityLogs: StateFlow<List<LoginActivityRecord>> = _loginActivityLogs.asStateFlow()

    // Creator Applications State
    private val _creatorApplications = MutableStateFlow<List<CreatorApplication>>(
        listOf(
            CreatorApplication(userId = "normal_user", username = "@casual_scroll", category = "Quran Recitation", reason = "Certified Tajweed teacher from Addis Ababa Islamic Center, wanting to share weekly lessons."),
            CreatorApplication(userId = "user_77", username = "@bilal_calligraphy", category = "Islamic Arts", reason = "Sharing live Arabic calligraphy design tutorials.")
        )
    )
    val creatorApplications: StateFlow<List<CreatorApplication>> = _creatorApplications.asStateFlow()

    // Active Sessions State
    private val _activeSessions = MutableStateFlow<List<UserSession>>(
        listOf(
            UserSession(sessionId = "sess_1", userId = "current_user", username = "@me", role = "SUPER_ADMIN", device = "Android Phone - Addis Ababa", ipAddress = "197.156.78.102", isActive = true),
            UserSession(sessionId = "sess_2", userId = "marcus123", username = "@marcus_dev", role = "CREATOR", device = "Samsung Galaxy S23", ipAddress = "197.156.12.88", isActive = true),
            UserSession(sessionId = "sess_3", userId = "normal_user", username = "@casual_scroll", role = "USER", device = "Redmi Note 12", ipAddress = "197.156.45.201", isActive = true)
        )
    )
    val activeSessions: StateFlow<List<UserSession>> = _activeSessions.asStateFlow()

    fun toggleSimulatedFollower(username: String) {
        val list = currentFollowersList.toMutableList()
        if (list.contains(username)) {
            list.remove(username)
        } else {
            list.add(username)
        }
        currentFollowersList = list
        currentUserFollowersCount = list.size.toString()
    }

    // Creator templates state
    private val _allTemplates = MutableStateFlow<List<VideoTemplate>>(listOf(
        VideoTemplate("temp_1", "Quran Recitation Slate", "A beautiful minimalist design layout with flowing Arabic script overlay and translation.", "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=150&q=80", "Quran", "Soothing Quranic Background"),
        VideoTemplate("temp_2", "Daily Hadith Slide", "A clean visual card overlay with elegant serif typography perfect for Hadith sharing.", "https://images.unsplash.com/photo-1519817650390-64a93db51149?auto=format&fit=crop&w=150&q=80", "Hadith", "Gentle Oud Melody"),
        VideoTemplate("temp_3", "Islamic Reminders Card", "A rapid cuts montage layout perfect for short daily reflection videos.", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=150&q=80", "Reminder", "Peaceful Nasheed Background"),
        VideoTemplate("temp_4", "Islamic Golden Age Stories", "Side-by-side transitions to showcase historical illustrations and timelines.", "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=150&q=80", "History", "Arabic Traditional Oud"),
        VideoTemplate("temp_5", "Arabic Calligraphy Timelapse", "High-speed timelapse setup with smooth ambient soundscapes.", "https://images.unsplash.com/photo-1607799279861-4dd421887fb3?auto=format&fit=crop&w=150&q=80", "Art", "Calm Lofi Reflection")
    ))
    val allTemplates: StateFlow<List<VideoTemplate>> = _allTemplates.asStateFlow()

    // Creator drafts state
    private val _allDrafts = MutableStateFlow<List<VideoDraft>>(listOf(
        VideoDraft(
            id = "draft_1",
            title = "Surah Al-Mulk Verse 1-5 📖✨",
            description = "Recording a beautiful recitation of Surah Al-Mulk. Let me know what you think!",
            mediaPaths = listOf("https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=150&q=80"),
            mediaType = "image",
            category = "Quran",
            musicName = "Soothing Quranic Background",
            filterName = "Peaceful Glow",
            textOverlay = "Surah Al-Mulk",
            coverText = "Daily Recitation"
        )
    ))
    val allDrafts: StateFlow<List<VideoDraft>> = _allDrafts.asStateFlow()

    // Upload lifecycle streams
    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError.asStateFlow()

    private val _uploadSuccessMessage = MutableStateFlow<String?>(null)
    val uploadSuccessMessage: StateFlow<String?> = _uploadSuccessMessage.asStateFlow()

    private var activeUploadJob: kotlinx.coroutines.Job? = null
    private var pendingUploadData: Triple<String, String, String>? = null // To support retry! (Title, Desc, Category)
    private var pendingMediaPath: String = ""

    fun addTemplate(title: String, description: String, category: String, defaultMusic: String, defaultText: String, thumbnailUrl: String) {
        val newTemplate = VideoTemplate(
            id = "temp_${System.currentTimeMillis()}",
            title = title,
            description = description,
            category = category,
            defaultMusic = defaultMusic,
            defaultText = defaultText,
            thumbnailUrl = thumbnailUrl,
            isActive = true,
            usageCount = 0
        )
        _allTemplates.value = _allTemplates.value + newTemplate
        logAdminAction("ADD_TEMPLATE", "Added new template: $title in category $category")
    }

    fun editTemplate(id: String, title: String, description: String, category: String, defaultMusic: String, defaultText: String, thumbnailUrl: String, isActive: Boolean) {
        _allTemplates.value = _allTemplates.value.map {
            if (it.id == id) {
                it.copy(
                    title = title,
                    description = description,
                    category = category,
                    defaultMusic = defaultMusic,
                    defaultText = defaultText,
                    thumbnailUrl = thumbnailUrl,
                    isActive = isActive
                )
            } else it
        }
        logAdminAction("EDIT_TEMPLATE", "Edited template id: $id, title: $title")
    }

    fun deleteTemplate(id: String) {
        val temp = _allTemplates.value.firstOrNull { it.id == id }
        _allTemplates.value = _allTemplates.value.filter { it.id != id }
        temp?.let { logAdminAction("DELETE_TEMPLATE", "Deleted template: ${it.title}") }
    }

    fun toggleTemplateActive(id: String) {
        _allTemplates.value = _allTemplates.value.map {
            if (it.id == id) {
                val newState = !it.isActive
                logAdminAction("TOGGLE_TEMPLATE_ACTIVE", "Set template active state to $newState for: ${it.title}")
                it.copy(isActive = newState)
            } else it
        }
    }

    fun incrementTemplateUsage(id: String) {
        _allTemplates.value = _allTemplates.value.map {
            if (it.id == id) {
                it.copy(usageCount = it.usageCount + 1)
            } else it
        }
    }

    fun saveDraft(title: String, description: String, mediaPaths: List<String>, mediaType: String, category: String, musicName: String, filterName: String, textOverlay: String, coverText: String, isPrivate: Boolean) {
        val newDraft = VideoDraft(
            title = title,
            description = description,
            mediaPaths = mediaPaths,
            mediaType = mediaType,
            category = category,
            musicName = musicName,
            filterName = filterName,
            textOverlay = textOverlay,
            coverText = coverText,
            isPrivate = isPrivate
        )
        _allDrafts.value = _allDrafts.value + newDraft
    }

    fun deleteDraft(id: String) {
        _allDrafts.value = _allDrafts.value.filter { it.id != id }
    }

    // Upload status state
    private val _uploadProgress = MutableStateFlow<Float?>(null)
    val uploadProgress: StateFlow<Float?> = _uploadProgress.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateIfNeeded()
            userPreferences.collect { prefs ->
                if (prefs != null) {
                    _selectedLanguage.value = prefs.language
                }
            }
        }
        viewModelScope.launch {
            repository.allUsers.collect { users ->
                val me = users.firstOrNull { it.userId == "current_user" }
                if (me != null) {
                    currentUsername = me.username
                    currentUserAvatar = me.avatarUrl
                    currentUserBio = me.bio
                    currentUserWebsite = me.website
                    currentUserTwitter = me.twitter
                    currentUserInstagram = me.instagram
                }
            }
        }
        viewModelScope.launch {
            repository.allVideos.collect { videos ->
                val followingCount = videos.filter { it.isFollowingCreator }.map { it.username }.distinct().size
                currentUserFollowingCount = followingCount.toString()
            }
        }
    }

    // --- ROLE-BASED ACCESS CONTROL (RBAC) GUARDS ---
    fun hasAdminAccess(): Boolean {
        return _isLoggedIn.value && (currentUserRole == "SUPER_ADMIN" || currentUserRole == "ADMIN" || currentUserRole == "MODERATOR") && currentUserStatus == "ACTIVE"
    }

    fun isSuperAdmin(): Boolean {
        return _isLoggedIn.value && currentUserRole == "SUPER_ADMIN" && currentUserStatus == "ACTIVE"
    }

    fun hasCreatorAccess(): Boolean {
        return _isLoggedIn.value && (currentUserRole in listOf("SUPER_ADMIN", "ADMIN", "CREATOR")) && currentUserStatus == "ACTIVE"
    }

    fun canUploadVideo(): Boolean {
        return _isLoggedIn.value && currentUserStatus == "ACTIVE" && hasCreatorAccess()
    }

    fun canSendMessage(): Boolean {
        return _isLoggedIn.value && currentUserStatus == "ACTIVE"
    }

    // --- ENHANCED AUTHENTICATION & SESSION MANAGEMENT ---
    fun logIn() {
        loginWithDemoPreset("SUPER_ADMIN")
    }

    fun signUp() {
        loginWithDemoPreset("USER")
    }

    fun loginWithDemoPreset(presetRole: String) {
        when (presetRole) {
            "SUPER_ADMIN" -> {
                currentUserId = "current_user"
                currentUsername = "@me"
                currentUserRole = "SUPER_ADMIN"
                currentUserStatus = "ACTIVE"
                currentUserBio = "System Administrator • Security Oversight"
                currentUserEmail = "admin@deentok.app"
                currentUserPhone = "+251911223344"
                currentUserAvatar = "SA"
                isEmailVerified = true
                isPhoneVerified = true
            }
            "ADMIN" -> {
                currentUserId = "mod_user"
                currentUsername = "@mod_deentok"
                currentUserRole = "ADMIN"
                currentUserStatus = "ACTIVE"
                currentUserBio = "Community Moderator • Report Inspector"
                currentUserEmail = "mod@deentok.app"
                currentUserPhone = "+251922334455"
                currentUserAvatar = "MD"
                isEmailVerified = true
                isPhoneVerified = true
            }
            "CREATOR" -> {
                currentUserId = "marcus123"
                currentUsername = "@marcus_dev"
                currentUserRole = "CREATOR"
                currentUserStatus = "ACTIVE"
                currentUserBio = "Verified Islamic Scholar & Content Creator 📖✨"
                currentUserEmail = "marcus@deentok.app"
                currentUserPhone = "+251933445566"
                currentUserAvatar = "MC"
                isEmailVerified = true
                isPhoneVerified = true
            }
            "USER" -> {
                currentUserId = "normal_user"
                currentUsername = "@casual_scroll"
                currentUserRole = "USER"
                currentUserStatus = "ACTIVE"
                currentUserBio = "Standard Deen Tok viewer"
                currentUserEmail = "user@deentok.app"
                currentUserPhone = "+251944556677"
                currentUserAvatar = "CS"
                isEmailVerified = true
                isPhoneVerified = true
            }
        }
        activeSessionToken = java.util.UUID.randomUUID().toString()
        _isLoggedIn.value = true

        val record = LoginActivityRecord(
            username = currentUsername,
            authMethod = "DEMO_ROLE_PRESET",
            status = "SUCCESS",
            device = activeSessionDevice,
            ipAddress = activeSessionIp,
            details = "Logged in with role: $currentUserRole"
        )
        _loginActivityLogs.value = listOf(record) + _loginActivityLogs.value
        logAdminAction("USER_LOGIN", "User $currentUsername logged in with role $currentUserRole")

        if (hasAdminAccess()) {
            _currentScreen.value = Screen.ADMIN_DASHBOARD
        } else {
            _currentScreen.value = Screen.HOME
        }
    }

    fun loginWithCredentials(identifier: String, passwordInput: String, isPhone: Boolean = false): String? {
        val trimmed = identifier.trim()
        if (trimmed.isBlank() || passwordInput.isBlank()) {
            return "Please enter both credentials and password."
        }

        if (trimmed.contains("spam") || trimmed.contains("suspended")) {
            val record = LoginActivityRecord(
                username = trimmed,
                authMethod = if (isPhone) "PHONE" else "EMAIL",
                status = "ACCOUNT_SUSPENDED",
                details = "Attempted login on suspended account"
            )
            _loginActivityLogs.value = listOf(record) + _loginActivityLogs.value
            return "Your account is currently suspended due to policy violations. Contact support."
        }

        val roleToAssign = when {
            trimmed.contains("admin") || trimmed == "@me" -> "SUPER_ADMIN"
            trimmed.contains("mod") -> "ADMIN"
            trimmed.contains("creator") || trimmed.contains("marcus") -> "CREATOR"
            else -> "USER"
        }

        currentUserId = "user_${System.currentTimeMillis()}"
        currentUsername = if (trimmed.startsWith("@")) trimmed else "@$trimmed"
        currentUserRole = roleToAssign
        currentUserStatus = "ACTIVE"
        if (isPhone) {
            currentUserPhone = trimmed
            isPhoneVerified = true
        } else {
            currentUserEmail = trimmed
            isEmailVerified = true
        }
        currentUserAvatar = currentUsername.filter { it.isLetter() }.take(2).uppercase().ifEmpty { "DT" }
        activeSessionToken = java.util.UUID.randomUUID().toString()
        _isLoggedIn.value = true

        val record = LoginActivityRecord(
            username = currentUsername,
            authMethod = if (isPhone) "PHONE" else "EMAIL",
            status = "SUCCESS",
            details = "Verified authentication for role $currentUserRole"
        )
        _loginActivityLogs.value = listOf(record) + _loginActivityLogs.value

        if (hasAdminAccess()) {
            _currentScreen.value = Screen.ADMIN_DASHBOARD
        } else {
            _currentScreen.value = Screen.HOME
        }
        return null
    }

    fun signUpUser(fullName: String, username: String, identifier: String, passwordInput: String, bio: String): String? {
        if (fullName.isBlank() || username.isBlank() || identifier.isBlank() || passwordInput.length < 6) {
            return "Please fill all fields. Password must be at least 6 characters."
        }

        val cleanUser = if (username.startsWith("@")) username else "@$username"
        currentUserId = "usr_${System.currentTimeMillis()}"
        currentUsername = cleanUser
        currentUserRole = "USER"
        currentUserStatus = "ACTIVE"
        currentUserBio = bio.ifBlank { "Deen Tok community member" }
        currentUserAvatar = fullName.filter { it.isLetter() }.take(2).uppercase().ifEmpty { "DT" }
        
        activeSessionToken = java.util.UUID.randomUUID().toString()
        _isLoggedIn.value = true

        val record = LoginActivityRecord(
            username = cleanUser,
            authMethod = "REGISTRATION",
            status = "SUCCESS",
            details = "New user registered with role USER"
        )
        _loginActivityLogs.value = listOf(record) + _loginActivityLogs.value

        viewModelScope.launch {
            repository.insertUser(
                User(
                    userId = currentUserId,
                    username = currentUsername,
                    avatarUrl = currentUserAvatar,
                    role = "USER",
                    status = "ACTIVE",
                    bio = currentUserBio,
                    isVerified = false
                )
            )
        }

        _currentScreen.value = Screen.HOME
        return null
    }

    fun resetPassword(identifier: String, newPasswordInput: String): Boolean {
        if (identifier.isBlank() || newPasswordInput.length < 6) return false
        val record = LoginActivityRecord(
            username = identifier,
            authMethod = "PASSWORD_RESET",
            status = "SUCCESS",
            details = "Password updated securely via OTP verification"
        )
        _loginActivityLogs.value = listOf(record) + _loginActivityLogs.value
        return true
    }

    fun applyForCreatorStatus(category: String, reason: String): Boolean {
        if (reason.isBlank()) return false
        val app = CreatorApplication(
            userId = currentUserId,
            username = currentUsername,
            category = category,
            reason = reason,
            status = "PENDING"
        )
        _creatorApplications.value = listOf(app) + _creatorApplications.value
        logAdminAction("CREATOR_APPLICATION_SUBMITTED", "$currentUsername applied for Creator role in category $category")
        return true
    }

    fun approveCreatorApplication(appId: String) {
        val app = _creatorApplications.value.firstOrNull { it.id == appId }
        _creatorApplications.value = _creatorApplications.value.map {
            if (it.id == appId) it.copy(status = "APPROVED") else it
        }
        if (app != null) {
            updateUserRole(app.userId, "CREATOR")
            logAdminAction("CREATOR_APPROVED", "Approved creator status for ${app.username}")
        }
    }

    fun rejectCreatorApplication(appId: String) {
        val app = _creatorApplications.value.firstOrNull { it.id == appId }
        _creatorApplications.value = _creatorApplications.value.map {
            if (it.id == appId) it.copy(status = "REJECTED") else it
        }
        if (app != null) {
            logAdminAction("CREATOR_REJECTED", "Rejected creator application for ${app.username}")
        }
    }

    fun updateUserRole(targetUserId: String, newRole: String) {
        if (!hasAdminAccess()) return
        viewModelScope.launch {
            val user = allUsers.value.firstOrNull { it.userId == targetUserId }
            if (user != null) {
                repository.updateUser(user.copy(role = newRole))
                logAdminAction("ROLE_CHANGE", "Changed role for ${user.username} to $newRole")
            }
        }
        if (targetUserId == currentUserId) {
            currentUserRole = newRole
        }
    }

    fun updateUserStatus(targetUserId: String, newStatus: String) {
        if (!hasAdminAccess()) return
        viewModelScope.launch {
            val user = allUsers.value.firstOrNull { it.userId == targetUserId }
            if (user != null) {
                repository.updateUser(user.copy(status = newStatus))
                logAdminAction("USER_STATUS_CHANGE", "Changed status for ${user.username} to $newStatus")
            }
        }
        if (targetUserId == currentUserId) {
            currentUserStatus = newStatus
        }
    }

    fun terminateSession(sessionId: String) {
        _activeSessions.value = _activeSessions.value.map {
            if (it.sessionId == sessionId) it.copy(isActive = false) else it
        }
        logAdminAction("SESSION_REVOKED", "Revoked session $sessionId")
    }

    fun logOut() {
        val record = LoginActivityRecord(
            username = currentUsername,
            authMethod = "LOGOUT",
            status = "SUCCESS",
            details = "User logged out securely"
        )
        _loginActivityLogs.value = listOf(record) + _loginActivityLogs.value
        activeSessionToken = null
        _isLoggedIn.value = false
        currentUserRole = "USER"
        _currentScreen.value = Screen.HOME
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    // Screen navigation helper with RBAC Guard
    fun setScreen(screen: Screen) {
        when (screen) {
            Screen.ADMIN_DASHBOARD -> {
                if (!hasAdminAccess()) {
                    val attemptRecord = LoginActivityRecord(
                        username = currentUsername,
                        authMethod = "RBAC_CHECK",
                        status = "BLOCKED_ATTEMPT",
                        details = "Blocked unauthorized access attempt to Admin Panel"
                    )
                    _loginActivityLogs.value = listOf(attemptRecord) + _loginActivityLogs.value
                    logAdminAction("SECURITY_VIOLATION", "Blocked unauthorized attempt to enter Admin Dashboard by $currentUsername")
                    _securityAlert.value = "⛔ Access Denied: Admin privileges required to open the Admin Panel."
                    _currentScreen.value = Screen.HOME
                    return
                }
            }
            Screen.CREATOR_STUDIO -> {
                if (!hasCreatorAccess()) {
                    _securityAlert.value = "ℹ️ Creator Studio is available to approved Creators. Apply in Settings."
                    _currentScreen.value = Screen.HOME
                    return
                }
            }
            else -> {}
        }
        _currentScreen.value = screen
    }

    fun setHomeTab(tab: Int) {
        _homeTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertRecentSearch(query: String) {
        viewModelScope.launch {
            repository.insertRecentSearch(query)
        }
    }

    fun deleteRecentSearch(query: String) {
        viewModelScope.launch {
            repository.deleteRecentSearch(query)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            repository.clearRecentSearches()
        }
    }

    fun setActiveVideoForComments(videoId: Int?) {
        _activeVideoIdForComments.value = videoId
    }

    // Video actions
    fun toggleLike(video: Video) {
        viewModelScope.launch {
            repository.toggleLikeVideo(video)
        }
    }

    fun toggleFollow(video: Video) {
        viewModelScope.launch {
            repository.toggleFollowCreator(video)
        }
    }

    fun addComment(videoId: Int, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            // Save comment
            val comment = Comment(
                videoId = videoId,
                username = currentUsername,
                userAvatarUrl = currentUserAvatar,
                text = text
            )
            repository.insertComment(comment)

            // Update video comment count
            val video = allVideos.value.firstOrNull { it.id == videoId }
            if (video != null) {
                repository.updateVideo(video.copy(commentsCount = video.commentsCount + 1))
            }
        }
    }

    // Chat actions
    fun sendDirectMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val msg = Message(
                senderId = "current_user",
                senderName = currentUsername,
                receiverId = activeChatPartnerId,
                text = text
            )
            repository.insertMessage(msg)
        }
    }

    // Video uploads
    fun cancelUpload() {
        activeUploadJob?.cancel()
        activeUploadJob = null
        _uploadProgress.value = null
        _uploadError.value = "Upload cancelled by user."
    }

    fun clearUploadStatus() {
        _uploadError.value = null
        _uploadSuccessMessage.value = null
        _uploadProgress.value = null
    }

    fun retryUpload() {
        val data = pendingUploadData
        if (data != null) {
            clearUploadStatus()
            uploadVideoWithSettings(data.first, data.second, data.third, pendingMediaPath)
        }
    }

    fun uploadVideo(title: String, description: String, category: String, videoPath: String = "") {
        uploadVideoWithSettings(title, description, category, videoPath)
    }

    fun uploadVideoWithSettings(
        title: String,
        description: String,
        category: String,
        videoPath: String = "",
        isPrivate: Boolean = false,
        musicName: String = "",
        allowComments: Boolean = true,
        allowSharing: Boolean = true,
        allowDownloads: Boolean = true,
        location: String = "",
        coverText: String = "",
        shouldSimulateFail: Boolean = false,
        quranRef: String = "",
        hadithRef: String = "",
        sourceInfo: String = "",
        contentType: String = ""
    ) {
        if (title.isBlank()) return
        pendingUploadData = Triple(title, description, category)
        pendingMediaPath = videoPath

        activeUploadJob = viewModelScope.launch {
            _uploadProgress.value = 0.0f
            _uploadError.value = null
            _uploadSuccessMessage.value = null

            var success = true
            for (i in 1..10) {
                kotlinx.coroutines.delay(150)
                _uploadProgress.value = i * 0.1f
                
                if (shouldSimulateFail && i == 6) {
                    success = false
                    break
                }
            }

            if (!success) {
                _uploadError.value = "Transient network error occurred during SQLite media stream chunk sync."
                _uploadProgress.value = null
                return@launch
            }

            val finalMusicName = if (musicName.isNotBlank()) musicName else "Original Sound - $currentUsername - DeenTok Creator"
            val displayThumbnail = if (videoPath.isNotBlank()) videoPath else "https://images.unsplash.com/photo-1541701494587-cb58502866ab?auto=format&fit=crop&w=150&q=80"

            val newVideo = Video(
                userId = "current_user",
                username = currentUsername,
                userAvatarUrl = currentUserAvatar,
                videoUrl = videoPath,
                thumbnailUrl = displayThumbnail,
                title = title,
                description = if (location.isNotBlank()) "$description • $location" else description,
                likesCount = 0,
                commentsCount = 0,
                sharesCount = 0,
                viewsCount = 0,
                isPrivate = isPrivate,
                isLiked = false,
                isFollowingCreator = false,
                musicName = finalMusicName,
                category = category,
                quranRef = quranRef,
                hadithRef = hadithRef,
                sourceInfo = sourceInfo,
                contentType = contentType
            )
            repository.insertVideo(newVideo)
            _uploadProgress.value = null
            _uploadSuccessMessage.value = "Your beneficial video \"$title\" has been published successfully!"

            // Notify user
            repository.insertNotification(
                Notification(
                    type = "SYSTEM",
                    title = "Upload Successful!",
                    description = "Your video: \"$title\" is now live in the $category feed!",
                    sourceUsername = "DeenTok Engine"
                )
            )

            // Auto navigate back home after 1 second of showing success screen
            kotlinx.coroutines.delay(1000)
            _uploadSuccessMessage.value = null
            _currentScreen.value = Screen.HOME
            _homeTab.value = 0 // Show on "For You"
        }
    }

    // Admin dashboard deletion
    fun deleteVideoAsAdmin(video: Video) {
        viewModelScope.launch {
            repository.deleteVideo(video)
            // Send warning/system notice
            repository.insertNotification(
                Notification(
                    type = "SYSTEM",
                    title = "Moderation Alert",
                    description = "Admin moderated and removed a video titled: \"${video.title}\".",
                    sourceUsername = "Admin Admin"
                )
            )
        }
    }

    // Notification helper
    fun markNotificationRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    // Creator Studio actions
    fun deleteVideo(video: Video) {
        viewModelScope.launch {
            repository.deleteVideo(video)
            // Send warning/system notice
            repository.insertNotification(
                Notification(
                    type = "SYSTEM",
                    title = "Video Deleted",
                    description = "You deleted your video: \"${video.title}\".",
                    sourceUsername = "DeenTok Engine"
                )
            )
        }
    }

    fun updateVideoVisibility(video: Video, isPrivate: Boolean) {
        viewModelScope.launch {
            repository.updateVideo(video.copy(isPrivate = isPrivate))
        }
    }

    fun updateVideoDetails(video: Video, title: String, description: String, category: String) {
        viewModelScope.launch {
            repository.updateVideo(video.copy(title = title, description = description, category = category))
        }
    }

    fun updateCreatorProfile(username: String, avatar: String, bio: String, website: String, twitter: String, instagram: String) {
        currentUsername = username
        currentUserAvatar = avatar
        currentUserBio = bio
        currentUserWebsite = website
        currentUserTwitter = twitter
        currentUserInstagram = instagram

        viewModelScope.launch {
            val currentUserInDb = allUsers.value.firstOrNull { it.userId == "current_user" }
            if (currentUserInDb != null) {
                repository.updateUser(
                    currentUserInDb.copy(
                        username = username,
                        avatarUrl = avatar,
                        bio = bio,
                        website = website,
                        twitter = twitter,
                        instagram = instagram
                    )
                )
            }
        }
    }

    fun saveUserPreferences(prefs: UserPreferences) {
        viewModelScope.launch {
            repository.insertUserPreferences(prefs)
        }
    }

    fun updateAccountSettings(
        username: String,
        email: String,
        phone: String,
        photo: String,
        bio: String,
        website: String
    ) {
        viewModelScope.launch {
            val currentPrefs = userPreferences.value ?: UserPreferences(userId = "current_user")
            val updatedPrefs = currentPrefs.copy(
                email = email,
                phoneNumber = phone
            )
            repository.insertUserPreferences(updatedPrefs)

            val currentUserInDb = allUsers.value.firstOrNull { it.userId == "current_user" }
            if (currentUserInDb != null) {
                repository.updateUser(
                    currentUserInDb.copy(
                        username = username,
                        avatarUrl = photo,
                        bio = bio,
                        website = website
                    )
                )
            } else {
                repository.insertUser(
                    User(
                        userId = "current_user",
                        username = username,
                        avatarUrl = photo,
                        role = "ADMIN",
                        status = "ACTIVE",
                        bio = bio,
                        website = website
                    )
                )
            }

            currentUsername = username
            currentUserAvatar = photo
            currentUserBio = bio
            currentUserWebsite = website
        }
    }

    fun deleteUserAccount() {
        viewModelScope.launch {
            repository.deleteUserById("current_user")
            logOut()
            setScreen(Screen.HOME)
        }
    }

    // --- PLATFORM ADMIN SETTINGS STATES ---
    private val _platformAppName = MutableStateFlow("DeenTok")
    val platformAppName: StateFlow<String> = _platformAppName.asStateFlow()

    private val _platformLogoText = MutableStateFlow("B")
    val platformLogoText: StateFlow<String> = _platformLogoText.asStateFlow()

    private val _privacySettings = MutableStateFlow("Standard Security - Public Feeds")
    val privacySettings: StateFlow<String> = _privacySettings.asStateFlow()

    private val _communityGuidelines = MutableStateFlow("Strict Moderation, No Harassment, No Copyright Infringement.")
    val communityGuidelines: StateFlow<String> = _communityGuidelines.asStateFlow()

    private val _adminNotificationSettings = MutableStateFlow("In-App Sound Alerts: On, Admin Security Alerts: On")
    val adminNotificationSettings: StateFlow<String> = _adminNotificationSettings.asStateFlow()

    fun updatePlatformSettings(name: String, logo: String, privacy: String, guidelines: String, notifications: String) {
        viewModelScope.launch {
            _platformAppName.value = name
            _platformLogoText.value = logo
            _privacySettings.value = privacy
            _communityGuidelines.value = guidelines
            _adminNotificationSettings.value = notifications
            logAdminAction("UPDATE_SETTINGS", "Updated app name to $name, logo to $logo, and updated guidelines")
        }
    }

    // --- ADMIN DASHBOARD AND SECURITY LOGGING ---
    fun logAdminAction(action: String, details: String) {
        viewModelScope.launch {
            repository.insertAdminLog(
                AdminLog(adminUsername = currentUsername, action = action, details = details)
            )
        }
    }

    // --- USER MANAGEMENT ACTIONS ---
    fun suspendUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user.copy(status = "SUSPENDED"))
            logAdminAction("SUSPEND_USER", "Suspended user: ${user.username}")
        }
    }

    fun activateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user.copy(status = "ACTIVE"))
            logAdminAction("ACTIVATE_USER", "Activated user: ${user.username}")
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
            logAdminAction("DELETE_USER", "Deleted user account: ${user.username}")
        }
    }

    fun changeUserRole(user: User, role: String) {
        viewModelScope.launch {
            repository.updateUser(user.copy(role = role))
            logAdminAction("CHANGE_ROLE", "Changed role of ${user.username} to $role")
        }
    }

    fun verifyCreator(user: User, isVerified: Boolean, type: String = "Verified Creator") {
        viewModelScope.launch {
            repository.updateUser(user.copy(
                isVerified = isVerified,
                verificationType = if (isVerified) type else "",
                role = if (isVerified) "CREATOR" else user.role
            ))
            logAdminAction("VERIFY_CREATOR", "Set verification status of ${user.username} to $isVerified ($type)")
        }
    }

    // --- VIDEO MANAGEMENT ACTIONS ---
    fun removeVideoAsAdmin(video: Video) {
        viewModelScope.launch {
            _removedVideos.value = _removedVideos.value + video
            repository.deleteVideo(video)
            logAdminAction("REMOVE_VIDEO", "Removed video: '${video.title}' by creator ${video.username}")
            repository.insertNotification(
                Notification(
                    type = "SYSTEM",
                    title = "Content Moderated",
                    description = "Your video '${video.title}' was removed by an Admin due to guidelines compliance.",
                    sourceUsername = "Admin Team"
                )
            )
        }
    }

    fun restoreVideoAsAdmin(video: Video) {
        viewModelScope.launch {
            _removedVideos.value = _removedVideos.value.filter { it.id != video.id }
            repository.insertVideo(video)
            logAdminAction("RESTORE_VIDEO", "Restored video: '${video.title}' by creator ${video.username}")
        }
    }

    // --- COMMENTS MODERATION ACTIONS ---
    fun deleteCommentAsAdmin(commentId: Int, commentText: String) {
        viewModelScope.launch {
            repository.deleteCommentById(commentId)
            logAdminAction("DELETE_COMMENT", "Deleted comment text preview: \"${commentText.take(30)}\"")
        }
    }

    // --- CATEGORIES MANAGEMENT ACTIONS ---
    fun addCategory(name: String, isTrending: Boolean) {
        viewModelScope.launch {
            repository.insertCategory(Category(name = name, isTrending = isTrending, hashtagCount = 0))
            logAdminAction("CREATE_CATEGORY", "Created new category: $name")
        }
    }

    fun editCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
            logAdminAction("EDIT_CATEGORY", "Updated category: ID ${category.id} name: ${category.name}")
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            logAdminAction("DELETE_CATEGORY", "Deleted category: ${category.name}")
        }
    }

    // --- ADVERTISEMENT MANAGEMENT ACTIONS ---
    fun addAdvertisement(title: String, imageUrl: String, targetUrl: String, status: String) {
        viewModelScope.launch {
            repository.insertAdvertisement(
                Advertisement(title = title, imageUrl = imageUrl, targetUrl = targetUrl, status = status)
            )
            logAdminAction("CREATE_AD", "Created advertisement campaign: $title")
        }
    }

    fun editAdvertisement(ad: Advertisement) {
        viewModelScope.launch {
            repository.updateAdvertisement(ad)
            logAdminAction("EDIT_AD", "Edited advertisement campaign: ${ad.title}")
        }
    }

    fun deleteAdvertisement(ad: Advertisement) {
        viewModelScope.launch {
            repository.deleteAdvertisement(ad)
            logAdminAction("DELETE_AD", "Deleted advertisement campaign: ${ad.title}")
        }
    }

    fun toggleAdStatus(ad: Advertisement) {
        viewModelScope.launch {
            val updated = ad.copy(status = if (ad.status == "ACTIVE") "INACTIVE" else "ACTIVE")
            repository.updateAdvertisement(updated)
            logAdminAction("TOGGLE_AD_STATUS", "Toggled ad '${ad.title}' to ${updated.status}")
        }
    }

    fun insertReport(report: Report) {
        viewModelScope.launch {
            repository.insertReport(report)
        }
    }

    // --- REPORTS MANAGEMENT ACTIONS ---
    fun resolveReport(report: Report, newStatus: String) {
        viewModelScope.launch {
            repository.updateReport(report.copy(status = newStatus))
            logAdminAction("RESOLVE_REPORT", "Resolved report ID ${report.id} to state: $newStatus")
        }
    }

    fun deleteReportAsAdmin(report: Report) {
        viewModelScope.launch {
            repository.deleteReport(report)
            logAdminAction("DELETE_REPORT", "Deleted report log ID ${report.id}")
        }
    }

    // --- ADDITIONAL ADMIN MUTATIONS FOR CLEAN REBUILD ---
    fun updateUserDirectly(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
            logAdminAction("EDIT_USER_PROFILE", "Admin updated profile for user: ${user.username}")
        }
    }

    fun resetUserPassword(userId: String, newPass: String) {
        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences(userId).firstOrNull() ?: UserPreferences(userId = userId)
            repository.insertUserPreferences(currentPrefs.copy(passwordHash = newPass))
            logAdminAction("RESET_PASSWORD", "Admin reset password for user: $userId")
        }
    }

    fun createUserDirectly(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
            logAdminAction("CREATE_USER", "Admin created user: ${user.username}")
        }
    }

    fun updateVideoDirectly(video: Video) {
        viewModelScope.launch {
            repository.updateVideo(video)
            logAdminAction("UPDATE_VIDEO", "Admin updated video parameters for: '${video.title}'")
        }
    }

    fun createVideoDirectly(video: Video) {
        viewModelScope.launch {
            repository.insertVideo(video)
            logAdminAction("CREATE_VIDEO", "Admin uploaded custom video record: '${video.title}'")
        }
    }

    fun deleteCommentDirectly(comment: Comment) {
        viewModelScope.launch {
            repository.deleteCommentById(comment.id)
            logAdminAction("DELETE_COMMENT", "Deleted comment by ${comment.username}: '${comment.text.take(30)}'")
        }
    }

    fun restoreCommentDirectly(comment: Comment) {
        viewModelScope.launch {
            repository.insertComment(comment)
            logAdminAction("RESTORE_COMMENT", "Restored comment by ${comment.username}: '${comment.text.take(30)}'")
        }
    }

    fun broadcastSystemNotification(title: String, description: String, filter: String) {
        viewModelScope.launch {
            repository.insertNotification(
                Notification(
                    type = "SYSTEM",
                    title = title,
                    description = description,
                    sourceUsername = "Admin Team"
                )
            )
            logAdminAction("BROADCAST_NOTIFICATION", "Broadcasted system notification: '$title' to $filter")
        }
    }

    // --- DT COIN PURCHASES (Telebirr / CBE) ---
    fun buyCoinsWithLocalPayment(
        pkg: CoinPackage,
        paymentMethod: String,
        accountOrPhone: String,
        txRef: String
    ) {
        viewModelScope.launch {
            val totalCoins = pkg.coinAmount + pkg.bonusAmount
            val tx = PaymentTransaction(
                userId = "current_user",
                packageId = pkg.id,
                packageName = pkg.title,
                coinAmount = totalCoins,
                amountEtb = pkg.priceEtb,
                paymentMethod = paymentMethod,
                accountOrPhone = accountOrPhone,
                txRef = if (txRef.isNotBlank()) txRef else "TX-${System.currentTimeMillis().toString().takeLast(8)}",
                status = "COMPLETED"
            )
            repository.insertPaymentTransaction(tx)

            // Update user wallet
            val currentWallet = userWallet.value ?: UserWallet(userId = "current_user", coinBalance = 0)
            val updatedWallet = currentWallet.copy(
                coinBalance = currentWallet.coinBalance + totalCoins,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertOrUpdateUserWallet(updatedWallet)

            // Record coin transaction
            repository.insertCoinTransaction(
                CoinTransaction(
                    userId = "current_user",
                    type = "PURCHASE",
                    amount = totalCoins,
                    description = "Purchased ${pkg.title} ($totalCoins DT Coins) via $paymentMethod"
                )
            )

            // Notify user
            repository.insertNotification(
                Notification(
                    type = "SYSTEM",
                    title = "DT Coins Purchased! 🪙✨",
                    description = "$totalCoins DT Coins added to your wallet via $paymentMethod. Thank you for supporting DeenTok!",
                    sourceUsername = "DeenTok Economy"
                )
            )
        }
    }

    // --- VIRTUAL GIFT SENDING ---
    fun sendVirtualGift(
        gift: VirtualGift,
        targetUserId: String,
        targetUsername: String,
        videoId: Int = 0
    ): Boolean {
        val currentBalance = userWallet.value?.coinBalance ?: 0
        if (currentBalance < gift.coinPrice) {
            return false // Insufficient funds
        }

        viewModelScope.launch {
            // 1. Deduct coins from sender wallet
            val currentWallet = userWallet.value ?: UserWallet(userId = "current_user", coinBalance = 0)
            val updatedWallet = currentWallet.copy(
                coinBalance = currentWallet.coinBalance - gift.coinPrice,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertOrUpdateUserWallet(updatedWallet)

            // 2. Insert coin transaction for sender
            repository.insertCoinTransaction(
                CoinTransaction(
                    userId = "current_user",
                    type = "GIFT_SENT",
                    amount = -gift.coinPrice,
                    description = "Sent ${gift.name} ${gift.iconSymbol} to $targetUsername"
                )
            )

            // 3. Record gift transaction
            repository.insertGiftTransaction(
                GiftTransaction(
                    senderUserId = "current_user",
                    senderUsername = currentUsername,
                    creatorUserId = targetUserId,
                    creatorUsername = targetUsername,
                    videoId = videoId,
                    giftId = gift.id,
                    giftName = gift.name,
                    giftIcon = gift.iconSymbol,
                    coinPrice = gift.coinPrice
                )
            )

            // 4. Update Creator Wallet
            val currentCreatorWallet = repository.getCreatorWallet(targetUserId).firstOrNull()
                ?: CreatorWallet(userId = targetUserId)
            val updatedCreatorWallet = currentCreatorWallet.copy(
                totalGiftsReceived = currentCreatorWallet.totalGiftsReceived + 1,
                totalCoinsEarned = currentCreatorWallet.totalCoinsEarned + gift.coinPrice,
                rewardBalanceCoins = currentCreatorWallet.rewardBalanceCoins + gift.coinPrice,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertOrUpdateCreatorWallet(updatedCreatorWallet)

            // 5. Record creator coin transaction
            repository.insertCoinTransaction(
                CoinTransaction(
                    userId = targetUserId,
                    type = "GIFT_RECEIVED",
                    amount = gift.coinPrice,
                    description = "Received ${gift.name} ${gift.iconSymbol} from $currentUsername"
                )
            )

            // 6. Notify creator
            repository.insertNotification(
                Notification(
                    type = "SYSTEM",
                    title = "New Gift Received! 🎁",
                    description = "$currentUsername sent you a ${gift.name} ${gift.iconSymbol} (+${gift.coinPrice} DT Coins)!",
                    sourceUsername = currentUsername
                )
            )

            // 7. Trigger visual animation overlay
            _activeGiftAnimation.value = GiftAnimationEvent(
                giftName = gift.name,
                giftSymbol = gift.iconSymbol,
                senderUsername = currentUsername,
                coinPrice = gift.coinPrice
            )
        }
        return true
    }

    // --- CREATOR WITHDRAWALS ---
    fun requestCreatorWithdrawal(
        coinsAmount: Int,
        payoutMethod: String,
        accountDetails: String
    ): Boolean {
        val currentCreatorCoins = creatorWallet.value?.rewardBalanceCoins ?: 0
        if (coinsAmount <= 0 || currentCreatorCoins < coinsAmount) {
            return false
        }

        viewModelScope.launch {
            val estEtb = coinsAmount * 0.5

            // Deduct from creator wallet
            val cWallet = creatorWallet.value ?: CreatorWallet(userId = "current_user")
            repository.insertOrUpdateCreatorWallet(
                cWallet.copy(
                    rewardBalanceCoins = cWallet.rewardBalanceCoins - coinsAmount,
                    updatedAt = System.currentTimeMillis()
                )
            )

            // Record withdrawal request
            repository.insertWithdrawalRequest(
                WithdrawalRequest(
                    userId = "current_user",
                    username = currentUsername,
                    coinsAmount = coinsAmount,
                    estimatedEtb = estEtb,
                    payoutMethod = payoutMethod,
                    accountDetails = accountDetails,
                    status = "PENDING"
                )
            )

            // Record transaction
            repository.insertCoinTransaction(
                CoinTransaction(
                    userId = "current_user",
                    type = "REWARD_PAYOUT",
                    amount = -coinsAmount,
                    description = "Withdrawal request of $coinsAmount DT Coins ($estEtb ETB) via $payoutMethod"
                )
            )

            // Notify user
            repository.insertNotification(
                Notification(
                    type = "SYSTEM",
                    title = "Withdrawal Request Submitted 🏦",
                    description = "Requested $coinsAmount DT Coins ($estEtb ETB) payout via $payoutMethod to $accountDetails. Admin review in progress.",
                    sourceUsername = "DeenTok Economy"
                )
            )
        }
        return true
    }

    // --- PROMOTIONS & CREATOR REWARDS METHODS ---

    fun boostVideoWithCoins(
        videoId: Int,
        videoTitle: String,
        targetViews: Int,
        coinsCost: Int
    ): Boolean {
        val currentBalance = userWallet.value?.coinBalance ?: 0
        if (coinsCost <= 0 || currentBalance < coinsCost) {
            return false
        }

        viewModelScope.launch {
            // Deduct DT Coins from wallet
            val currentWallet = userWallet.value ?: UserWallet(userId = "current_user", coinBalance = 0)
            val updatedWallet = currentWallet.copy(
                coinBalance = currentWallet.coinBalance - coinsCost,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertOrUpdateUserWallet(updatedWallet)

            // Record transaction
            repository.insertCoinTransaction(
                CoinTransaction(
                    userId = "current_user",
                    type = "PROMOTION",
                    amount = -coinsCost,
                    description = "Promoted video #$videoId '$videoTitle' (+$targetViews est. views)"
                )
            )

            // Update video views in repository
            val video = repository.getVideoById(videoId).firstOrNull()
            if (video != null) {
                repository.updateVideo(video.copy(viewsCount = video.viewsCount + targetViews))
            }

            // Send Notification
            repository.insertNotification(
                Notification(
                    type = "SYSTEM",
                    title = "Video Promoted Successfully! 🚀",
                    description = "Your video '$videoTitle' is now boosted in the Deen Tok For You feed to reach +$targetViews viewers.",
                    sourceUsername = "DeenTok Promotions"
                )
            )
        }
        return true
    }

    fun claimCreatorVideoReward(videoId: Int, videoTitle: String, rewardDiamonds: Int) {
        viewModelScope.launch {
            val cWallet = creatorWallet.value ?: CreatorWallet(userId = "current_user")
            val updatedWallet = cWallet.copy(
                rewardBalanceCoins = cWallet.rewardBalanceCoins + rewardDiamonds,
                totalCoinsEarned = cWallet.totalCoinsEarned + rewardDiamonds,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertOrUpdateCreatorWallet(updatedWallet)

            repository.insertCoinTransaction(
                CoinTransaction(
                    userId = "current_user",
                    type = "VIDEO_REWARD",
                    amount = rewardDiamonds,
                    description = "Video milestone reward claimed for '$videoTitle' (+$rewardDiamonds DT Diamonds)"
                )
            )

            repository.insertNotification(
                Notification(
                    type = "SYSTEM",
                    title = "Video Reward Claimed! 🏆",
                    description = "You received +$rewardDiamonds DT Diamonds for high engagement on '$videoTitle'.",
                    sourceUsername = "DeenTok Creator Program"
                )
            )
        }
    }

    fun claimCreatorCampaignReward(campaignTitle: String, rewardDiamonds: Int) {
        viewModelScope.launch {
            val cWallet = creatorWallet.value ?: CreatorWallet(userId = "current_user")
            val updatedWallet = cWallet.copy(
                rewardBalanceCoins = cWallet.rewardBalanceCoins + rewardDiamonds,
                totalCoinsEarned = cWallet.totalCoinsEarned + rewardDiamonds,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertOrUpdateCreatorWallet(updatedWallet)

            repository.insertCoinTransaction(
                CoinTransaction(
                    userId = "current_user",
                    type = "CAMPAIGN_REWARD",
                    amount = rewardDiamonds,
                    description = "Campaign reward claimed for '$campaignTitle' (+$rewardDiamonds DT Diamonds)"
                )
            )

            repository.insertNotification(
                Notification(
                    type = "SYSTEM",
                    title = "Campaign Bonus Received! 🌟",
                    description = "Congratulations! You earned +$rewardDiamonds DT Diamonds for participating in the $campaignTitle challenge.",
                    sourceUsername = "DeenTok Creator Program"
                )
            )
        }
    }

    // --- ADMIN ECONOMY MANAGEMENT METHODS ---
    fun addCoinPackage(title: String, coins: Int, bonus: Int, priceEtb: Double, badge: String) {
        viewModelScope.launch {
            val newPkg = CoinPackage(
                id = "pkg_${System.currentTimeMillis()}",
                title = title,
                coinAmount = coins,
                bonusAmount = bonus,
                priceEtb = priceEtb,
                badge = badge,
                isActive = true
            )
            repository.insertCoinPackage(newPkg)
            logAdminAction("ADD_COIN_PACKAGE", "Created coin package: $title ($coins Coins, $priceEtb ETB)")
        }
    }

    fun updateCoinPackage(pkg: CoinPackage) {
        viewModelScope.launch {
            repository.updateCoinPackage(pkg)
            logAdminAction("UPDATE_COIN_PACKAGE", "Updated coin package: ${pkg.title}")
        }
    }

    fun deleteCoinPackage(pkg: CoinPackage) {
        viewModelScope.launch {
            repository.deleteCoinPackage(pkg)
            logAdminAction("DELETE_COIN_PACKAGE", "Deleted coin package: ${pkg.title}")
        }
    }

    fun addVirtualGift(name: String, category: String, price: Int, iconSymbol: String, animationType: String) {
        viewModelScope.launch {
            val newGift = VirtualGift(
                id = "gift_${System.currentTimeMillis()}",
                name = name,
                category = category,
                coinPrice = price,
                iconSymbol = iconSymbol,
                animationType = animationType,
                isActive = true
            )
            repository.insertVirtualGift(newGift)
            logAdminAction("ADD_VIRTUAL_GIFT", "Created gift: $name ($price Coins)")
        }
    }

    fun updateVirtualGift(gift: VirtualGift) {
        viewModelScope.launch {
            repository.updateVirtualGift(gift)
            logAdminAction("UPDATE_VIRTUAL_GIFT", "Updated gift: ${gift.name}")
        }
    }

    fun deleteVirtualGift(gift: VirtualGift) {
        viewModelScope.launch {
            repository.deleteVirtualGift(gift)
            logAdminAction("DELETE_VIRTUAL_GIFT", "Deleted gift: ${gift.name}")
        }
    }

    fun approvePaymentTransaction(tx: PaymentTransaction) {
        viewModelScope.launch {
            repository.updatePaymentTransaction(tx.copy(status = "COMPLETED"))
            logAdminAction("APPROVE_PAYMENT", "Approved payment tx #${tx.id} for user ${tx.userId} (${tx.coinAmount} Coins)")
        }
    }

    fun rejectPaymentTransaction(tx: PaymentTransaction) {
        viewModelScope.launch {
            repository.updatePaymentTransaction(tx.copy(status = "FAILED"))
            logAdminAction("REJECT_PAYMENT", "Rejected payment tx #${tx.id} for user ${tx.userId}")
        }
    }

    fun approveWithdrawalRequest(req: WithdrawalRequest) {
        viewModelScope.launch {
            repository.updateWithdrawalRequest(req.copy(status = "APPROVED"))
            logAdminAction("APPROVE_WITHDRAWAL", "Approved withdrawal request #${req.id} for ${req.username} (${req.estimatedEtb} ETB)")
        }
    }

    fun completeWithdrawalRequest(req: WithdrawalRequest) {
        viewModelScope.launch {
            repository.updateWithdrawalRequest(req.copy(status = "COMPLETED"))
            logAdminAction("COMPLETE_WITHDRAWAL", "Completed withdrawal request #${req.id} for ${req.username}")
        }
    }

    fun rejectWithdrawalRequest(req: WithdrawalRequest) {
        viewModelScope.launch {
            repository.updateWithdrawalRequest(req.copy(status = "REJECTED"))
            // Refund creator coins back to creator's wallet
            val currentCWallet = repository.getCreatorWallet(req.userId).firstOrNull() ?: CreatorWallet(userId = req.userId)
            repository.insertOrUpdateCreatorWallet(
                currentCWallet.copy(
                    rewardBalanceCoins = currentCWallet.rewardBalanceCoins + req.coinsAmount,
                    updatedAt = System.currentTimeMillis()
                )
            )
            logAdminAction("REJECT_WITHDRAWAL", "Rejected withdrawal request #${req.id}, refunded ${req.coinsAmount} Coins to ${req.username}")
        }
    }

    // --- LIVE STREAMING USER & CREATOR ACTIONS ---

    fun selectLiveStream(streamId: Int?) {
        _selectedLiveStreamId.value = streamId
        if (streamId != null) {
            viewModelScope.launch {
                val stream = repository.getLiveStreamById(streamId).firstOrNull()
                if (stream != null) {
                    // Update viewer count
                    repository.updateLiveStream(stream.copy(viewerCount = stream.viewerCount + 1))
                    // Insert viewer record
                    val currentUsername = if (isLoggedIn.value) "You" else "Guest_Viewer"
                    repository.insertOrUpdateLiveViewer(
                        LiveViewer(
                            streamId = streamId,
                            userId = "current_user",
                            username = currentUsername
                        )
                    )
                }
            }
        }
    }

    fun startLiveStream(
        title: String,
        description: String,
        category: String,
        videoBgStyle: String = "MOSQUE_BG"
    ) {
        viewModelScope.launch {
            val creatorName = "You (Host)"
            val streamKey = "live_${System.currentTimeMillis()}"
            val newStream = LiveStream(
                streamKey = streamKey,
                creatorUserId = "current_user",
                creatorUsername = creatorName,
                creatorAvatarUrl = "",
                title = title,
                description = description,
                category = category,
                status = "LIVE",
                videoBgStyle = videoBgStyle,
                viewerCount = 1,
                likeCount = 0,
                giftCoinsEarned = 0,
                startedAt = System.currentTimeMillis()
            )
            val newId = repository.insertLiveStream(newStream).toInt()
            _selectedLiveStreamId.value = newId

            // Add system welcome message
            repository.insertLiveChatMessage(
                LiveChatMessage(
                    streamId = newId,
                    userId = "system",
                    username = "DeenTok Live",
                    message = "🔴 LIVE broadcast started! Followers notified.",
                    isSystemMessage = true
                )
            )

            // Notify followers
            repository.insertNotification(
                Notification(
                    type = "LIVE",
                    title = "🔴 LIVE Stream Started!",
                    description = "$creatorName is now LIVE: '$title'. Tap to join now!",
                    sourceUsername = creatorName
                )
            )

            // Navigate to LIVE screen
            setScreen(Screen.LIVE)
        }
    }

    fun endLiveStream(streamId: Int) {
        viewModelScope.launch {
            val stream = repository.getLiveStreamById(streamId).firstOrNull()
            if (stream != null) {
                val endedStream = stream.copy(
                    status = "ENDED",
                    endedAt = System.currentTimeMillis()
                )
                repository.updateLiveStream(endedStream)

                // Save analytics
                val duration = (System.currentTimeMillis() - stream.startedAt) / 1000L
                repository.insertOrUpdateLiveAnalytics(
                    LiveAnalytics(
                        streamId = streamId,
                        creatorUserId = stream.creatorUserId,
                        peakViewers = stream.viewerCount,
                        totalViewersCount = stream.viewerCount * 2,
                        totalLikes = stream.likeCount,
                        totalComments = 15,
                        totalGiftsCount = 3,
                        totalCoinsEarned = stream.giftCoinsEarned,
                        durationSeconds = duration
                    )
                )

                logAdminAction("END_LIVE_STREAM", "Live stream #${streamId} '${stream.title}' ended by creator")
            }
            if (_selectedLiveStreamId.value == streamId) {
                _selectedLiveStreamId.value = null
            }
        }
    }

    fun cancelLiveStream(streamId: Int) {
        viewModelScope.launch {
            repository.deleteLiveStreamById(streamId)
            if (_selectedLiveStreamId.value == streamId) {
                _selectedLiveStreamId.value = null
            }
            logAdminAction("CANCEL_LIVE_STREAM", "Live stream #${streamId} canceled")
        }
    }

    fun sendLiveChatMessage(streamId: Int, messageText: String) {
        if (messageText.isBlank()) return
        viewModelScope.launch {
            val username = if (isLoggedIn.value) "You" else "Guest"
            repository.insertLiveChatMessage(
                LiveChatMessage(
                    streamId = streamId,
                    userId = "current_user",
                    username = username,
                    message = messageText.trim(),
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun likeLiveStream(streamId: Int) {
        viewModelScope.launch {
            val stream = repository.getLiveStreamById(streamId).firstOrNull()
            if (stream != null) {
                repository.updateLiveStream(stream.copy(likeCount = stream.likeCount + 1))
            }
        }
    }

    fun sendLiveGift(
        streamId: Int,
        gift: VirtualGift,
        creatorUserId: String,
        creatorUsername: String
    ): Boolean {
        // First invoke existing gift sending logic (deducts coins, credits creator wallet, records transaction)
        val success = sendVirtualGift(gift, creatorUserId, creatorUsername, videoId = 0)
        if (success) {
            viewModelScope.launch {
                val username = if (isLoggedIn.value) "You" else "Guest"
                // 1. Insert gift chat announcement
                repository.insertLiveChatMessage(
                    LiveChatMessage(
                        streamId = streamId,
                        userId = "current_user",
                        username = username,
                        message = "sent ${gift.iconSymbol} ${gift.name} (+${gift.coinPrice} Coins)!",
                        isGiftMessage = true,
                        giftIcon = gift.iconSymbol,
                        giftCoins = gift.coinPrice
                    )
                )

                // 2. Update Live Stream coins total
                val stream = repository.getLiveStreamById(streamId).firstOrNull()
                if (stream != null) {
                    repository.updateLiveStream(stream.copy(giftCoinsEarned = stream.giftCoinsEarned + gift.coinPrice))
                }
            }
        }
        return success
    }

    fun pinLiveChatMessage(streamId: Int, messageText: String) {
        viewModelScope.launch {
            val stream = repository.getLiveStreamById(streamId).firstOrNull()
            if (stream != null) {
                repository.updateLiveStream(stream.copy(pinnedComment = messageText))
                repository.insertLiveChatMessage(
                    LiveChatMessage(
                        streamId = streamId,
                        userId = "system",
                        username = "Moderator",
                        message = "📌 Pinned comment: '$messageText'",
                        isSystemMessage = true,
                        isPinned = true
                    )
                )
            }
        }
    }

    fun muteUserInLive(streamId: Int, targetUserId: String, targetUsername: String) {
        viewModelScope.launch {
            repository.insertOrUpdateLiveViewer(
                LiveViewer(
                    streamId = streamId,
                    userId = targetUserId,
                    username = targetUsername,
                    isMuted = true
                )
            )
            repository.insertLiveChatMessage(
                LiveChatMessage(
                    streamId = streamId,
                    userId = "system",
                    username = "System",
                    message = "🔇 $targetUsername was muted by host/moderator.",
                    isSystemMessage = true
                )
            )
        }
    }

    fun blockUserInLive(streamId: Int, targetUserId: String, targetUsername: String) {
        viewModelScope.launch {
            repository.removeLiveViewer(streamId, targetUserId)
            repository.insertLiveChatMessage(
                LiveChatMessage(
                    streamId = streamId,
                    userId = "system",
                    username = "System",
                    message = "🚫 $targetUsername was blocked from chat.",
                    isSystemMessage = true
                )
            )
        }
    }

    fun assignLiveModerator(streamId: Int, targetUserId: String, targetUsername: String) {
        viewModelScope.launch {
            repository.insertLiveModerator(
                LiveModerator(
                    streamId = streamId,
                    userId = targetUserId,
                    username = targetUsername,
                    assignedBy = "current_user"
                )
            )
            repository.insertLiveChatMessage(
                LiveChatMessage(
                    streamId = streamId,
                    userId = "system",
                    username = "System",
                    message = "🛡️ $targetUsername is now a Live Moderator!",
                    isSystemMessage = true
                )
            )
        }
    }

    fun reportLiveStream(streamId: Int, streamTitle: String, reportedUserId: String, reportedUsername: String, reason: String) {
        viewModelScope.launch {
            val reporterUsername = if (isLoggedIn.value) "You" else "User"
            repository.insertLiveReport(
                LiveReport(
                    streamId = streamId,
                    streamTitle = streamTitle,
                    reportedUserId = reportedUserId,
                    reportedUsername = reportedUsername,
                    reporterUserId = "current_user",
                    reporterUsername = reporterUsername,
                    reason = reason,
                    status = "PENDING"
                )
            )
            logAdminAction("LIVE_REPORT_SUBMITTED", "Live stream #${streamId} reported for '$reason'")
        }
    }

    // --- ADMIN LIVE MODERATION METHODS ---

    // --- ADMIN LIVE SETTINGS & CONTROLS ---

    fun setLiveFeatureEnabled(enabled: Boolean) {
        _isLiveFeatureEnabled.value = enabled
        logAdminAction("TOGGLE_LIVE_FEATURE", "Global LIVE feature set to $enabled")
    }

    fun setMinFollowersForLive(count: Int) {
        _minFollowersForLive.value = count
        logAdminAction("SET_MIN_FOLLOWERS_LIVE", "Minimum followers required for LIVE set to $count")
    }

    fun banUserFromLive(userId: String, username: String) {
        _liveBannedUserIds.value = _liveBannedUserIds.value + userId + username
        logAdminAction("BAN_USER_LIVE", "User $username ($userId) restricted from LIVE streaming")
    }

    fun unbanUserFromLive(userId: String, username: String) {
        _liveBannedUserIds.value = _liveBannedUserIds.value - userId - username
        logAdminAction("UNBAN_USER_LIVE", "User $username ($userId) unbanned from LIVE streaming")
    }

    data class LiveEligibilityResult(
        val isEligible: Boolean,
        val reason: String = ""
    )

    fun checkLiveEligibility(): LiveEligibilityResult {
        if (!_isLiveFeatureEnabled.value) {
            return LiveEligibilityResult(false, "LIVE streaming feature is currently disabled by network administrator.")
        }
        if (currentUserStatus != "ACTIVE") {
            return LiveEligibilityResult(false, "Your account is currently $currentUserStatus and cannot start LIVE broadcasts.")
        }
        if (_liveBannedUserIds.value.contains(currentUserId) || _liveBannedUserIds.value.contains(currentUsername)) {
            return LiveEligibilityResult(false, "Your account has been restricted from going LIVE due to community guidelines violation.")
        }
        val followers = currentUserFollowersCount.toIntOrNull() ?: 0
        if (followers < _minFollowersForLive.value) {
            return LiveEligibilityResult(
                false,
                "Minimum follower requirement not met. You need at least ${_minFollowersForLive.value} followers to go LIVE. (Current Followers: $followers)"
            )
        }
        return LiveEligibilityResult(true)
    }

    fun adminEndLiveStream(streamId: Int, reason: String) {
        viewModelScope.launch {
            val stream = repository.getLiveStreamById(streamId).firstOrNull()
            if (stream != null) {
                repository.updateLiveStream(stream.copy(status = "SUSPENDED", endedAt = System.currentTimeMillis()))
                logAdminAction("ADMIN_SUSPEND_LIVE", "Admin forcefully ended live stream #${streamId} '${stream.title}'. Reason: $reason")

                // Notify creator
                repository.insertNotification(
                    Notification(
                        type = "SYSTEM",
                        title = "LIVE Stream Ended by Admin ⚠️",
                        description = "Your live stream '${stream.title}' was suspended due to: $reason",
                        sourceUsername = "DeenTok Moderation"
                    )
                )
            }
        }
    }

    fun reviewLiveReport(reportId: Int, actionStatus: String) {
        viewModelScope.launch {
            val reports = repository.allLiveReports.firstOrNull() ?: emptyList()
            val report = reports.find { it.id == reportId }
            if (report != null) {
                repository.updateLiveReport(report.copy(status = actionStatus))
                logAdminAction("REVIEW_LIVE_REPORT", "Report #${reportId} updated to status '$actionStatus'")
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentScreen.value = Screen.HOME
    }
}

data class GiftAnimationEvent(
    val giftName: String,
    val giftSymbol: String,
    val senderUsername: String,
    val coinPrice: Int,
    val timestamp: Long = System.currentTimeMillis()
)


// ViewModel factory helper
class DeenTokViewModelFactory(private val repository: DeenTokRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeenTokViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeenTokViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
