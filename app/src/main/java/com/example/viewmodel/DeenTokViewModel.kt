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
    ADMIN_DASHBOARD
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

    // Mock Current User
    var currentUsername by mutableStateOf("@me")
    var currentUserAvatar by mutableStateOf("ME")
    var currentUserBio by mutableStateOf("DeenTok creator. Sharing beneficial Islamic reminders, Quran recitation, and finding peace. 🕋✨")
    var currentUserWebsite by mutableStateOf("https://deentok.app/me")
    var currentUserTwitter by mutableStateOf("@me_deentok")
    var currentUserInstagram by mutableStateOf("@me_deen")
    var currentUserFollowersCount by mutableStateOf("0")
    var currentUserFollowingCount by mutableStateOf("0")
    var currentFollowersList by mutableStateOf(listOf<String>())

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

    // Authentication helpers
    fun logIn() {
        _isLoggedIn.value = true
    }

    fun signUp() {
        _isLoggedIn.value = true
    }

    fun logOut() {
        _isLoggedIn.value = false
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    // Screen navigation helper
    fun setScreen(screen: Screen) {
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
}

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
