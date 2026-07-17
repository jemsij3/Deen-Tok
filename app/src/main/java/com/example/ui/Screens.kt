package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.data.VideoTemplate
import com.example.data.VideoDraft
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.input.key.*
import com.example.ui.theme.*
import com.example.viewmodel.DeenTokViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// MAIN SCREEN ROUTER
@Composable
fun MainAppScreen(viewModel: DeenTokViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    val unreadNotifications by viewModel.allNotifications.collectAsState()
    
    val unreadCount = unreadNotifications.count { !it.isRead }

    if (!isLoggedIn) {
        WelcomeScreen(viewModel = viewModel)
    } else {
        Scaffold(
            bottomBar = {
                if (currentScreen == Screen.HOME || currentScreen == Screen.EXPLORE || 
                    currentScreen == Screen.UPLOAD || currentScreen == Screen.MESSAGES || 
                    currentScreen == Screen.PROFILE) {
                    DeenTokBottomNavigation(
                        currentScreen = currentScreen,
                        unreadCount = unreadCount,
                        viewModel = viewModel,
                        onNavigate = { screen -> viewModel.setScreen(screen) }
                    )
                }
            },
            containerColor = Black,
            contentWindowInsets = WindowInsets.navigationBars
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (shouldShowNavBar(currentScreen)) innerPadding.calculateBottomPadding() else 0.dp)
            ) {
                when (currentScreen) {
                    Screen.HOME -> FeedScreen(viewModel)
                    Screen.EXPLORE -> ExploreScreen(viewModel)
                    Screen.UPLOAD -> UploadScreen(viewModel)
                    Screen.LIVE -> LiveScreen(viewModel)
                    Screen.MESSAGES -> MessagesScreen(viewModel)
                    Screen.NOTIFICATIONS -> NotificationsSection(viewModel)
                    Screen.PROFILE -> ProfileScreen(viewModel)
                    Screen.SETTINGS -> SettingsScreen(viewModel)
                    Screen.CREATOR_STUDIO -> CreatorStudioScreen(viewModel)
                    Screen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel)
                }
            }
        }
    }
}

private fun shouldShowNavBar(screen: Screen): Boolean {
    return screen == Screen.HOME || screen == Screen.EXPLORE || 
           screen == Screen.UPLOAD || screen == Screen.MESSAGES || 
           screen == Screen.PROFILE
}

// 1. HOME SCREEN (FOR YOU / FOLLOWING FEEDS)
@Composable
fun FeedScreen(viewModel: DeenTokViewModel) {
    val allVideos by viewModel.allVideos.collectAsState()
    val homeTab by viewModel.homeTab.collectAsState()
    val activeVideoIdForComments by viewModel.activeVideoIdForComments.collectAsState()
    
    val filteredVideos = remember(allVideos, homeTab) {
        if (homeTab == 1) {
            // "Following" feed: simulate showing videos where user is followed
            allVideos.filter { it.isFollowingCreator || it.userId == "current_user" }
        } else {
            // "Deen Feed" feed: show all
            allVideos
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (filteredVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "No Videos",
                        tint = TextGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (homeTab == 1) "Not following anyone yet!" else "No videos available",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (homeTab == 1) "Follow creators in 'Deen Feed' or 'Explore' to see their videos here." else "Upload your own short video to get started!",
                        color = TextGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    if (homeTab == 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.setHomeTab(0) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Explore Deen Feed", color = Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Swipable video list (simulating vertical paging)
            var currentVideoIndex by remember(filteredVideos) { mutableStateOf(0) }
            val currentVideo = filteredVideos.getOrNull(currentVideoIndex)

            if (currentVideo != null) {
                VideoPlayerCard(
                    video = currentVideo,
                    viewModel = viewModel,
                    onLike = { viewModel.toggleLike(currentVideo) },
                    onFollow = { viewModel.toggleFollow(currentVideo) },
                    onCommentClick = { viewModel.setActiveVideoForComments(currentVideo.id) },
                    onSwipeUp = {
                        if (currentVideoIndex < filteredVideos.size - 1) {
                            currentVideoIndex++
                        } else {
                            currentVideoIndex = 0 // loop
                        }
                    },
                    onSwipeDown = {
                        if (currentVideoIndex > 0) {
                            currentVideoIndex--
                        } else {
                            currentVideoIndex = filteredVideos.size - 1
                        }
                    }
                )
            }
        }

        // Top Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Following",
                color = if (homeTab == 1) TextWhite else TextGray,
                fontWeight = if (homeTab == 1) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier
                    .testTag("following_tab")
                    .clickable { viewModel.setHomeTab(1) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(TextMuted)
            )
            Text(
                text = "Deen Feed",
                color = if (homeTab == 0) TextWhite else TextGray,
                fontWeight = if (homeTab == 0) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier
                    .testTag("for_you_tab")
                    .clickable { viewModel.setHomeTab(0) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Comments Bottom Sheet overlay
        if (activeVideoIdForComments != null) {
            CommentsOverlaySheet(
                viewModel = viewModel,
                videoId = activeVideoIdForComments!!,
                onDismiss = { viewModel.setActiveVideoForComments(null) }
            )
        }
    }
}

// 2. VIDEO PLAYER SIMULATION CARD
@Composable
fun VideoPlayerCard(
    video: Video,
    viewModel: DeenTokViewModel,
    onLike: () -> Unit,
    onFollow: () -> Unit,
    onCommentClick: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit
) {
    val isVideoMuted by viewModel.isVideoMuted.collectAsState()
    val videoVolume by viewModel.videoVolume.collectAsState()
    var isPlaying by remember(video.id) { mutableStateOf(true) }
    var showPlayOverlay by remember(video.id) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Real-time ticking seek bar progress
    var videoProgress by remember(video.id) { mutableStateOf(0.0f) }
    LaunchedEffect(video.id, isPlaying) {
        if (isPlaying) {
            while (true) {
                delay(100)
                videoProgress += 0.005f
                if (videoProgress >= 1.0f) {
                    videoProgress = 0.0f
                }
            }
        }
    }

    // Double tap like animation trigger
    var doubleTapHearts by remember { mutableStateOf<List<Offset>>(emptyList()) }

    // Vinyl spinning rotation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl"
    )

    // Gesture detection for Swipe UP, Swipe DOWN, Single Tap, Double Tap
    var dragStartY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .pointerInput(video.id) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        doubleTapHearts = doubleTapHearts + offset
                        onLike()
                    },
                    onTap = {
                        isPlaying = !isPlaying
                        coroutineScope.launch {
                            showPlayOverlay = true
                            delay(500)
                            showPlayOverlay = false
                        }
                    }
                )
            }
            .pointerInput(video.id) {
                // Detect swipes
                detectDragGestures(
                    onDragStart = { offset -> dragStartY = offset.y },
                    onDragEnd = {
                        // Action on drag end
                    },
                    onDragCancel = {},
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount.y < -30f) {
                            onSwipeUp()
                        } else if (dragAmount.y > 30f) {
                            onSwipeDown()
                        }
                    }
                )
            }
    ) {
        // Futuristic Video Visual (Rotating Radial Gradients mimicking full screen motion)
        val videoGradientAngle by animateFloatAsState(
            targetValue = if (isPlaying) 360f else 0f,
            animationSpec = tween(15000, easing = LinearEasing),
            label = "video_gradient"
        )
        
        val videoBrush = remember(video.id, isPlaying) {
            val colors = when (video.category) {
                "Tech" -> listOf(Color(0xFF0F172A), Color(0xFF020617), Color(0xFF1E293B))
                "Vlog" -> listOf(Color(0xFF180828), Color(0xFF08020F), Color(0xFF2E1065))
                "Music" -> listOf(Color(0xFF05111B), Color(0xFF010408), Color(0xFF0B2135))
                "Art" -> listOf(Color(0xFF1A1010), Color(0xFF050101), Color(0xFF2C1A1A))
                else -> listOf(Color(0xFF1C1917), Color(0xFF0C0A09), Color(0xFF292524))
            }
            Brush.linearGradient(
                colors = colors,
                start = Offset(0f, 0f),
                end = Offset(1000f, 1500f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(videoBrush)
        ) {
            if (video.videoUrl.isNotBlank()) {
                if (isVideoFile(video.videoUrl)) {
                    VideoPlayer(
                        videoPath = video.videoUrl,
                        isMuted = isVideoMuted,
                        volume = videoVolume,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    coil.compose.AsyncImage(
                        model = video.videoUrl,
                        contentDescription = "Real Media Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            } else {
                // Visual decorative overlay mimicking abstract moving stars/visualizers
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .shadow(
                                elevation = 30.dp,
                                shape = CircleShape,
                                clip = false,
                                ambientColor = CyanAccent,
                                spotColor = BlueAccent
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        CyanAccent.copy(alpha = 0.15f),
                                        BlueAccent.copy(alpha = 0.2f),
                                        CyanAccent.copy(alpha = 0.15f)
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = if (video.category == "Tech") Icons.Default.Code 
                                          else if (video.category == "Music") Icons.Default.GraphicEq 
                                          else if (video.category == "Cooking") Icons.Default.RestaurantMenu
                                          else Icons.Default.Brush,
                            contentDescription = "Visualizer",
                            tint = CyanAccent.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.Center)
                                .rotate(if (isPlaying) rotation else 0f)
                        )
                    }
                }
            }
        }

        // Tap Play/Pause Indicator Overlay
        AnimatedVisibility(
            visible = showPlayOverlay,
            enter = fadeIn(animationSpec = tween(100)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPlaying) "Playing" else "Paused",
                    tint = TextWhite,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Right Sidebar Actions
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 110.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Icon
            Box(modifier = Modifier.padding(bottom = 10.dp)) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, TextWhite, CircleShape)
                        .background(SurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = video.userAvatarUrl,
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                
                // Follow badge (+)
                if (!video.isFollowingCreator) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 10.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(CyanAccent)
                            .clickable { onFollow() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Follow Creator",
                            tint = Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Like Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onLike() },
                    modifier = Modifier
                        .testTag("like_button")
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = if (video.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like Video",
                        tint = if (video.isLiked) LikeRed else TextWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = formatCount(video.likesCount),
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comment Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onCommentClick() },
                    modifier = Modifier
                        .testTag("comment_button")
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Open Comments",
                        tint = TextWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = formatCount(video.commentsCount),
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Share Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val context = LocalContext.current
                IconButton(
                    onClick = {
                        // Simulation of share trigger
                        android.widget.Toast.makeText(context, "Link Copied: Deen Tok Live Stream!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Video",
                        tint = TextWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = formatCount(video.sharesCount),
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Volume / Sound Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { viewModel.toggleVideoMute() },
                    modifier = Modifier
                        .testTag("volume_button")
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = if (isVideoMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Volume Controls",
                        tint = if (isVideoMuted) LikeRed else CyanAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark.copy(alpha = 0.6f))
                            .clickable { viewModel.setVideoVolume(videoVolume - 0.2f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = if (isVideoMuted) "Mute" else String.format("%.0f%%", videoVolume * 100f),
                        color = TextWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark.copy(alpha = 0.6f))
                            .clickable { viewModel.setVideoVolume(videoVolume + 0.2f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Spinning Music Disk
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .border(2.dp, SurfaceVariantDark, CircleShape)
                    .rotate(if (isPlaying) rotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(CyanAccent, BlueAccent)
                            )
                        )
                )
            }
        }

        // Bottom Left Info Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(bottom = 110.dp, start = 16.dp)
        ) {
            // Islamic Reference Card
            if (video.quranRef.isNotBlank() || video.hadithRef.isNotBlank() || video.sourceInfo.isNotBlank() || video.contentType.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F2E1E).copy(alpha = 0.9f)) // Peaceful Islamic Forest Green
                        .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(8.dp)) // Premium Gold border
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Islamic Reference",
                                tint = Color(0xFFD4AF37), // Serene Gold
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (video.contentType.isNotBlank()) "REF: ${video.contentType.uppercase()}" else "ISLAMIC REFERENCE",
                                color = Color(0xFFD4AF37),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        if (video.quranRef.isNotBlank()) {
                            Text(
                                text = "📖 Quran: ${video.quranRef}",
                                color = TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        if (video.hadithRef.isNotBlank()) {
                            Text(
                                text = "💬 Hadith: ${video.hadithRef}",
                                color = TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        if (video.sourceInfo.isNotBlank()) {
                            Text(
                                text = "💡 Source: ${video.sourceInfo}",
                                color = TextWhite.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }

            // Username + follow trigger
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = video.username,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (video.isFollowingCreator) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TextMuted.copy(alpha = 0.3f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Following", color = TextGray, fontSize = 10.sp)
                    }
                } else {
                    Text(
                        text = "• Follow",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onFollow() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            
            // Description
            Text(
                text = video.description,
                color = TextWhite.copy(alpha = 0.9f),
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Music Note Marquee Ticket
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = TextWhite,
                    modifier = Modifier.size(14.dp)
                )
                
                // Simple auto-scrolling representation
                Text(
                    text = video.musicName,
                    color = TextWhite.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Bottom Seek Bar Tick Progress Line
        LinearProgressIndicator(
            progress = { videoProgress },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp),
            color = CyanAccent,
            trackColor = TextMuted.copy(alpha = 0.3f)
        )

        // Render double-tap floating hearts
        doubleTapHearts.forEach { heartOffset ->
            var scale by remember { mutableStateOf(0f) }
            var alpha by remember { mutableStateOf(1f) }
            
            LaunchedEffect(key1 = heartOffset) {
                animate(
                    initialValue = 0f,
                    targetValue = 1.3f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) { value, _ -> scale = value }
                
                animate(
                    initialValue = 1f,
                    targetValue = 0f,
                    animationSpec = tween(300)
                ) { value, _ -> alpha = value }
                
                doubleTapHearts = doubleTapHearts.filter { it != heartOffset }
            }

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Like Heart POP",
                tint = LikeRed,
                modifier = Modifier
                    .offset { IntOffset(heartOffset.x.roundToInt() - 50, heartOffset.y.roundToInt() - 50) }
                    .size(100.dp)
                    .scale(scale)
                    .rotate((heartOffset.x % 30) - 15)
            )
        }
    }
}

// 3. EXPLORE SCREEN
// Active overlay screens
sealed interface ExploreOverlay {
    object None : ExploreOverlay
    data class VideoPreview(val video: Video) : ExploreOverlay
    data class CreatorProfile(val user: User) : ExploreOverlay
    data class HashtagView(val hashtag: String) : ExploreOverlay
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(viewModel: DeenTokViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    var activeTab by remember { mutableStateOf("Discover") } // Discover, Trending, Creators, Community
    var activeFilter by remember { mutableStateOf("Latest") } // Latest, Trending, Most Viewed, Most Liked
    var activeCategory by remember { mutableStateOf("All") }
    var activeLocation by remember { mutableStateOf("Global") } // Global, Ethiopia, Regional
    var searchFocused by remember { mutableStateOf(false) }

    var currentOverlay by remember { mutableStateOf<ExploreOverlay>(ExploreOverlay.None) }

    // Helper: extract hashtags from a video description
    fun extractHashtags(text: String): List<String> {
        return text.split(" ", "\n").filter { it.startsWith("#") && it.length > 1 }
    }

    // Engagement score formula
    fun getEngagementScore(video: Video): Int {
        return (video.likesCount * 2) + video.viewsCount + (video.commentsCount * 5) + (video.sharesCount * 10)
    }

    // Filter and Sort video collection dynamically
    val filteredVideos = remember(allVideos, searchQuery, activeCategory, activeFilter, activeLocation) {
        allVideos.filter { video ->
            val matchesCategory = (activeCategory == "All") || (video.category.equals(activeCategory, ignoreCase = true))
            
            // Search matches video title, description, creator, or tags
            val matchesQuery = searchQuery.isBlank() || 
                               video.title.contains(searchQuery, ignoreCase = true) || 
                               video.description.contains(searchQuery, ignoreCase = true) || 
                               video.username.contains(searchQuery, ignoreCase = true) ||
                               video.category.contains(searchQuery, ignoreCase = true)
            
            // Location simulation filter
            val matchesLocation = when (activeLocation) {
                "Ethiopia" -> video.username in listOf("@neon_stroll", "@chef_yuki", "@me") || video.description.contains("ethiopia", ignoreCase = true)
                "Regional" -> video.username in listOf("@me", "@marcus_dev")
                else -> true // Global
            }

            matchesCategory && matchesQuery && matchesLocation
        }.sortedWith { a, b ->
            when (activeFilter) {
                "Trending" -> getEngagementScore(b).compareTo(getEngagementScore(a))
                "Most Viewed" -> b.viewsCount.compareTo(a.viewsCount)
                "Most Liked" -> b.likesCount.compareTo(a.likesCount)
                else -> b.id.compareTo(a.id) // Latest
            }
        }
    }

    // Dynamic search suggestions
    val suggestions = remember(searchQuery, allVideos, allUsers, allCategories) {
        if (searchQuery.isBlank()) emptyList() else {
            val list = mutableListOf<String>()
            
            // Matching tags
            allVideos.flatMap { extractHashtags(it.description) }
                .filter { it.contains(searchQuery, ignoreCase = true) }
                .distinct()
                .take(3)
                .forEach { list.add(it) }

            // Matching creators
            allUsers.filter { it.username.contains(searchQuery, ignoreCase = true) }
                .map { it.username }
                .take(3)
                .forEach { list.add(it) }

            // Matching categories
            allCategories.filter { it.name.contains(searchQuery, ignoreCase = true) }
                .map { it.name }
                .take(3)
                .forEach { list.add(it) }

            list.distinct()
        }
    }

    // Top trending hashtags list based on database videos
    val trendingHashtags = remember(allVideos) {
        allVideos.flatMap { extractHashtags(it.description) }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(10)
    }

    // popular creators list
    val popularCreators = remember(allUsers) {
        allUsers.filter { it.role == "CREATOR" || it.role == "ADMIN" }
            .take(10)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            // Header with search & location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXPLORE",
                    color = TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.testTag("explore_title")
                )
                
                // Location toggle dropdown/chip
                Box {
                    var showLocMenu by remember { mutableStateOf(false) }
                    AssistChip(
                        onClick = { showLocMenu = true },
                        label = { Text("📍 $activeLocation", color = CyanAccent, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = SurfaceDark,
                            labelColor = CyanAccent
                        ),
                        border = BorderStroke(0.5.dp, SurfaceVariantDark)
                    )
                    
                    DropdownMenu(
                        expanded = showLocMenu,
                        onDismissRequest = { showLocMenu = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        listOf("Global", "Ethiopia", "Regional").forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc, color = TextWhite) },
                                onClick = {
                                    activeLocation = loc
                                    showLocMenu = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    viewModel.setSearchQuery(it)
                    searchFocused = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { searchFocused = it.isFocused }
                    .testTag("explore_search_bar"),
                placeholder = { Text("Search creators, sounds, or topics...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            viewModel.setSearchQuery("") 
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextGray)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            viewModel.insertRecentSearch(searchQuery)
                        }
                        searchFocused = false
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = SurfaceVariantDark,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Main Tab Row (Discover, Trending, Creators, Community)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf("Discover", "Trending", "Creators", "Community").forEach { tab ->
                    val isSel = activeTab == tab
                    Box(
                        modifier = Modifier
                            .clickable { activeTab = tab }
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = tab,
                            color = if (isSel) CyanAccent else TextGray,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp
                        )
                        if (isSel) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(CyanAccent)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Body rendering based on selected Tab
            if (activeTab == "Creators") {
                // Creators Discovery Page
                Text("ISLAMIC SCHOLARS & CREATORS", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(popularCreators) { creator ->
                        CreatorCard(creator = creator, onProfileClick = {
                            currentOverlay = ExploreOverlay.CreatorProfile(creator)
                        }, onFollowToggle = {
                            // Find a video from this creator to toggle follow state
                            allVideos.firstOrNull { it.username == creator.username }?.let { vid ->
                                viewModel.toggleFollow(vid)
                            }
                        })
                    }
                }
            } else {
                // Grid Filters (Latest, Trending, Most Viewed, Most Liked) & Horizontal Category Bar
                Column {
                    // Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Latest", "Trending", "Most Viewed", "Most Liked").forEach { filter ->
                            val selected = activeFilter == filter
                            FilterChip(
                                selected = selected,
                                onClick = { activeFilter = filter },
                                label = { Text(filter, color = if (selected) Black else TextWhite, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent,
                                    containerColor = SurfaceDark
                                ),
                                border = BorderStroke(0.5.dp, if (selected) CyanAccent else SurfaceVariantDark)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Categories LazyRow
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val selected = activeCategory == "All"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (selected) CyanAccent else SurfaceDark)
                                    .clickable { activeCategory = "All" }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "All Categories",
                                    color = if (selected) Black else TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        items(allCategories) { category ->
                            val selected = category.name.equals(activeCategory, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (selected) CyanAccent else SurfaceDark)
                                    .clickable { activeCategory = category.name }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = category.name,
                                        color = if (selected) Black else TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    if (category.isTrending) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = "Trending",
                                            tint = if (selected) Black else CyanAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Main video grid
                if (filteredVideos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No videos match filters in this community region.",
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Section header for Popular creators if on "Discover" tab
                        if (activeTab == "Discover" && searchQuery.isBlank() && activeCategory == "All") {
                            item(span = { GridItemSpan(2) }) {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("VERIFIED SCHOLARS & CREATORS", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        TextButton(onClick = { activeTab = "Creators" }) {
                                            Text("See All", color = CyanAccent, fontSize = 11.sp)
                                        }
                                    }
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(popularCreators) { creator ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .clickable { currentOverlay = ExploreOverlay.CreatorProfile(creator) }
                                                    .width(72.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(CircleShape)
                                                        .border(1.5.dp, CyanAccent, CircleShape)
                                                        .background(getGradientForUser(creator.username)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = creator.avatarUrl,
                                                        color = TextWhite,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Text(
                                                        text = creator.username,
                                                        color = TextWhite,
                                                        fontSize = 10.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    if (creator.isVerified) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(10.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    when (creator.verificationType) {
                                                                        "Verified Scholar" -> Color(0xFFD4AF37)
                                                                        "Islamic Organization" -> Color(0xFF2E7D32)
                                                                        else -> CyanAccent
                                                                    }
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Verified",
                                                                tint = if (creator.verificationType == "Verified Scholar" || creator.verificationType == "Islamic Organization") TextWhite else Black,
                                                                modifier = Modifier.size(6.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text("DISCOVER VIDEOS", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }

                        items(filteredVideos) { video ->
                            CustomExploreVideoCard(
                                video = video,
                                onVideoClick = {
                                    currentOverlay = ExploreOverlay.VideoPreview(video)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Search Suggestion and Search History Overlay panel
        if (searchFocused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black.copy(alpha = 0.95f))
                    .statusBarsPadding()
                    .padding(top = 70.dp, start = 16.dp, end = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SEARCH OPTIONS", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        TextButton(onClick = { searchFocused = false }) {
                            Text("Close Panel", color = CyanAccent, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Suggestions section
                    if (searchQuery.isNotBlank() && suggestions.isNotEmpty()) {
                        Text("SUGGESTIONS", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        suggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setSearchQuery(suggestion)
                                        viewModel.insertRecentSearch(suggestion)
                                        searchFocused = false
                                    }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (suggestion.startsWith("#")) Icons.Default.Tag else Icons.Default.TrendingUp,
                                    contentDescription = "Suggestion",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(suggestion, color = TextWhite, fontSize = 14.sp)
                            }
                            HorizontalDivider(color = SurfaceVariantDark.copy(alpha = 0.5f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Recent Searches Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("RECENT SEARCHES", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        if (recentSearches.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearRecentSearches() }) {
                                Text("Clear All", color = LikeRed, fontSize = 11.sp)
                            }
                        }
                    }

                    if (recentSearches.isEmpty()) {
                        Text(
                            text = "No recent searches found. Start exploring Deen Tok!",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(recentSearches) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.setSearchQuery(item.query)
                                                searchFocused = false
                                            },
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = "History", tint = TextGray, modifier = Modifier.size(16.dp))
                                        Text(item.query, color = TextWhite, fontSize = 14.sp)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteRecentSearch(item.query) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = TextGray, modifier = Modifier.size(16.dp))
                                    }
                                }
                                HorizontalDivider(color = SurfaceVariantDark.copy(alpha = 0.2f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    // Hot Topics & Trending Hashtags
                    Text("TRENDING TOPICS", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        trendingHashtags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDark)
                                    .clickable {
                                        viewModel.setSearchQuery(tag)
                                        viewModel.insertRecentSearch(tag)
                                        searchFocused = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(tag, color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Overlay Navigation management
        when (val overlay = currentOverlay) {
            is ExploreOverlay.VideoPreview -> {
                VideoPreviewOverlay(
                    video = overlay.video,
                    viewModel = viewModel,
                    allUsers = allUsers,
                    onClose = { currentOverlay = ExploreOverlay.None },
                    onHashtagClick = { tag ->
                        currentOverlay = ExploreOverlay.HashtagView(tag)
                    },
                    onCreatorClick = { creatorUser ->
                        currentOverlay = ExploreOverlay.CreatorProfile(creatorUser)
                    }
                )
            }
            is ExploreOverlay.CreatorProfile -> {
                CreatorProfileOverlay(
                    user = overlay.user,
                    viewModel = viewModel,
                    allVideos = allVideos,
                    onClose = { currentOverlay = ExploreOverlay.None },
                    onVideoClick = { video ->
                        currentOverlay = ExploreOverlay.VideoPreview(video)
                    }
                )
            }
            is ExploreOverlay.HashtagView -> {
                HashtagOverlay(
                    hashtag = overlay.hashtag,
                    allVideos = allVideos,
                    onClose = { currentOverlay = ExploreOverlay.None },
                    onVideoClick = { video ->
                        currentOverlay = ExploreOverlay.VideoPreview(video)
                    }
                )
            }
            else -> { /* No Overlay */ }
        }
    }
}

// Helper color generator based on creator's name
fun getGradientForUser(username: String): Brush {
    val hash = username.hashCode()
    val colors = listOf(
        listOf(Color(0xFF0D9488), Color(0xFF111827)), // Cyan Teal
        listOf(Color(0xFF7C3AED), Color(0xFF111827)), // Violet
        listOf(Color(0xFFDB2777), Color(0xFF111827)), // Pink
        listOf(Color(0xFFEA580C), Color(0xFF111827)), // Orange
        listOf(Color(0xFF2563EB), Color(0xFF111827))  // Blue
    )
    val index = Math.abs(hash) % colors.size
    return Brush.verticalGradient(colors[index])
}

@Composable
fun VerificationBadge(isVerified: Boolean, verificationType: String, modifier: Modifier = Modifier) {
    if (!isVerified) return
    val badgeColor = when (verificationType) {
        "Verified Scholar" -> Color(0xFFD4AF37) // Gold
        "Islamic Organization" -> Color(0xFF2E7D32) // Forest Green (Vibrant, clear)
        else -> CyanAccent // Cyan
    }
    val label = when (verificationType) {
        "Verified Scholar" -> "SCHOLAR"
        "Islamic Organization" -> "ORG"
        else -> ""
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Verified",
                tint = if (badgeColor == CyanAccent) Black else TextWhite,
                modifier = Modifier.size(9.dp)
            )
        }
        if (label.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .border(0.5.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = label,
                    color = badgeColor,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// 3.1 CREATOR CARD FOR CREATOR TAB
@Composable
fun CreatorCard(creator: User, onProfileClick: () -> Unit, onFollowToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(0.5.dp, SurfaceVariantDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(getGradientForUser(creator.username)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = creator.avatarUrl,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = creator.username,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                VerificationBadge(isVerified = creator.isVerified, verificationType = creator.verificationType)
            }
            Text(
                text = if (creator.bio.isNotBlank()) creator.bio else "Deen Tok Content Creator",
                color = TextGray,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(34.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onProfileClick() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanAccent
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                Text("Profile", color = Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 3.2 CUSTOM VIDEO CARD WITH GLOWING CYBERPUNK PALETTE
@Composable
fun CustomExploreVideoCard(video: Video, onVideoClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clip(RoundedCornerShape(14.dp))
            .border(0.5.dp, SurfaceVariantDark, RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable { onVideoClick() }
    ) {
        // Procedural ambient glowing thumbnail
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getGradientForUser(video.username))
        ) {
            // Draw subtle vector decoration inside the card to look like real loaded frame
            Canvas(modifier = Modifier.fillMaxSize()) {
                val wavePath = Path().apply {
                    val y = size.height * 0.65f
                    moveTo(0f, y)
                    quadraticTo(size.width * 0.25f, y - 40f, size.width * 0.5f, y)
                    quadraticTo(size.width * 0.75f, y + 40f, size.width, y)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(wavePath, color = Black.copy(alpha = 0.5f))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top elements: Category badge and likes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanPrimary.copy(alpha = 0.35f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = video.category,
                            color = CyanAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Black.copy(alpha = 0.4f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = LikeRed,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = formatCount(video.likesCount),
                            color = TextWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bottom details: Title, user avatar + handle, views count
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = video.title,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = video.userAvatarUrl,
                                    color = Black,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = video.username,
                                color = TextWhite.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveRedEye,
                                contentDescription = "Views",
                                tint = TextGray,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = formatCount(video.viewsCount),
                                color = TextGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3.3 HASHTAG OVERLAY SCREEN
@Composable
fun HashtagOverlay(
    hashtag: String,
    allVideos: List<Video>,
    onClose: () -> Unit,
    onVideoClick: (Video) -> Unit
) {
    val hashVideos = remember(hashtag, allVideos) {
        allVideos.filter { it.title.contains(hashtag, ignoreCase = true) || it.description.contains(hashtag, ignoreCase = true) }
    }

    val viewsSum = remember(hashVideos) {
        hashVideos.sumOf { it.viewsCount }
    }

    var isFollowing by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                }
                Text("Hashtag Campaign", color = TextGray, fontSize = 14.sp)
                Box(modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Hashtag Hero banner card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(0.5.dp, SurfaceVariantDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = hashtag,
                            color = CyanAccent,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${formatCount(hashVideos.size)} Posts • ${formatCount(viewsSum)} Views",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { isFollowing = !isFollowing },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) SurfaceDark else CyanAccent
                        ),
                        border = if (isFollowing) BorderStroke(1.dp, CyanAccent) else null
                    ) {
                        Text(
                            text = if (isFollowing) "Following" else "Follow Topic",
                            color = if (isFollowing) CyanAccent else Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("RELATED CHALLENGE VIDEOS", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (hashVideos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Be the first to post a video with $hashtag on Deen Tok!", color = TextGray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(hashVideos) { video ->
                        CustomExploreVideoCard(video = video, onVideoClick = { onVideoClick(video) })
                    }
                }
            }
        }
    }
}

// 3.4 CREATOR PROFILE DETAIL OVERLAY SCREEN
@Composable
fun CreatorProfileOverlay(
    user: User,
    viewModel: DeenTokViewModel,
    allVideos: List<Video>,
    onClose: () -> Unit,
    onVideoClick: (Video) -> Unit
) {
    val creatorVideos = remember(user, allVideos) {
        allVideos.filter { it.username == user.username }
    }

    val isFollowed = remember(creatorVideos) {
        creatorVideos.any { it.isFollowingCreator }
    }

    val likesSum = remember(creatorVideos) {
        creatorVideos.sumOf { it.likesCount }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                }
                Text("Creator Hub", color = TextGray, fontSize = 14.sp)
                Box(modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Bio / Profile Panel
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, CyanAccent, CircleShape)
                        .background(getGradientForUser(user.username)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.avatarUrl,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = user.username,
                        color = TextWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    VerificationBadge(isVerified = user.isVerified, verificationType = user.verificationType)
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (user.bio.isNotBlank()) user.bio else "Passionate short-form video story creator on Deen Tok.",
                    color = TextGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Stats: Followers, Following, Likes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "435", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Following", color = TextGray, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (user.username == "@marcus_dev") "842K" else "12.4K", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Followers", color = TextGray, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = formatCount(likesSum), color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Likes", color = TextGray, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (creatorVideos.isNotEmpty()) {
                                viewModel.toggleFollow(creatorVideos.first())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowed) SurfaceDark else CyanAccent
                        ),
                        modifier = Modifier.weight(1f),
                        border = if (isFollowed) BorderStroke(1.dp, CyanAccent) else null
                    ) {
                        Text(
                            text = if (isFollowed) "Following" else "Follow Creator",
                            color = if (isFollowed) CyanAccent else Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Text("CREATOR VIDEOS", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (creatorVideos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No videos uploaded by this creator yet.", color = TextGray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(creatorVideos) { video ->
                        CustomExploreVideoCard(video = video, onVideoClick = { onVideoClick(video) })
                    }
                }
            }
        }
    }
}

// 3.5 IMMERSIVE TIKTOK-STYLE VIDEO PREVIEW OVERLAY
@Composable
fun VideoPreviewOverlay(
    video: Video,
    viewModel: DeenTokViewModel,
    allUsers: List<User>,
    onClose: () -> Unit,
    onHashtagClick: (String) -> Unit,
    onCreatorClick: (User) -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var showComments by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    
    val comments by viewModel.activeVideoComments.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val isVideoMuted by viewModel.isVideoMuted.collectAsState()
    val videoVolume by viewModel.videoVolume.collectAsState()

    val uploaderUser = remember(video, allUsers) {
        allUsers.firstOrNull { it.username == video.username } ?: User(
            userId = video.userId,
            username = video.username,
            avatarUrl = video.userAvatarUrl,
            role = "CREATOR",
            status = "ACTIVE"
        )
    }

    // Load comments initially
    LaunchedEffect(video.id) {
        viewModel.setActiveVideoForComments(video.id)
    }

    // Spin animation for vinyl disk
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        // Simulated video background (with custom vibrant vertical gradient)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getGradientForUser(video.username))
                .clickable { isPlaying = !isPlaying },
            contentAlignment = Alignment.Center
        ) {
            if (video.videoUrl.isNotBlank()) {
                if (isVideoFile(video.videoUrl)) {
                    VideoPlayer(
                        videoPath = video.videoUrl,
                        isMuted = isVideoMuted,
                        volume = videoVolume,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    coil.compose.AsyncImage(
                        model = video.videoUrl,
                        contentDescription = "Real Media Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            } else {
                // Rotating soundwaves visualization when playing
                if (isPlaying) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = CyanAccent.copy(alpha = 0.05f),
                            radius = size.minDimension * 0.35f
                        )
                    }
                }
            }

            // Big play/pause state indicator overlay
            AnimatedVisibility(
                visible = !isPlaying,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Paused",
                        tint = TextWhite,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Close back button top left
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Black.copy(alpha = 0.4f))
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close Preview", tint = TextWhite)
        }

        // Location context tag at the top center if set
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(8.dp))
                .background(Black.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "⚡ DEENTOK IMPRESS PLAY",
                color = CyanAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        // BOTTOM CAPTION & DETAILS OVERLAY PANEL
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Creator details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onCreatorClick(uploaderUser) }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyanAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = video.userAvatarUrl, color = Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = video.username, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        VerificationBadge(isVerified = uploaderUser.isVerified, verificationType = uploaderUser.verificationType)
                    }
                    Text(text = "Original Audio Patch", color = TextGray, fontSize = 11.sp)
                }
            }

            // Title
            Text(text = video.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            // Description with hashtags rendering
            ExpandableDescriptionWithHashtags(
                description = video.description,
                onHashtagClick = { onHashtagClick(it) }
            )

            // Simulated Track Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("0:12", color = TextGray, fontSize = 9.sp)
                LinearProgressIndicator(
                    progress = { 0.4f },
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp),
                    color = CyanAccent,
                    trackColor = SurfaceVariantDark.copy(alpha = 0.5f)
                )
                Text("0:30", color = TextGray, fontSize = 9.sp)
            }
        }

        // RIGHT-SIDE INTERACTIVE TOOLBAR COLUMN (Like, Comment, Share, Save, Music)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 24.dp)
                .width(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile image with uploader follow button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onCreatorClick(uploaderUser) }
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.dp, CyanAccent, CircleShape)
                        .background(getGradientForUser(video.username)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = video.userAvatarUrl, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Mini Follow button overlay
                val following = video.isFollowingCreator
                IconButton(
                    onClick = { viewModel.toggleFollow(video) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 8.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (following) SurfaceVariantDark else CyanAccent)
                ) {
                    Icon(
                        imageVector = if (following) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = "Follow",
                        tint = if (following) TextWhite else Black,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Like action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { viewModel.toggleLike(video) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = if (video.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (video.isLiked) LikeRed else TextWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = formatCount(video.likesCount),
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comment action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { showComments = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comments",
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = formatCount(video.commentsCount),
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bookmark/Save Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        isBookmarked = !isBookmarked
                        val msg = if (isBookmarked) "Saved to bookmarks!" else "Removed from bookmarks."
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) CyanAccent else TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Save",
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Share Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        android.widget.Toast.makeText(context, "Link copied! Share freedom anywhere.", android.widget.Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Share",
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Volume / Sound Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { viewModel.toggleVideoMute() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = if (isVideoMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Volume Controls",
                        tint = if (isVideoMuted) LikeRed else CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Black.copy(alpha = 0.4f))
                            .clickable { viewModel.setVideoVolume(videoVolume - 0.2f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = if (isVideoMuted) "Mute" else String.format("%.0f%%", videoVolume * 100f),
                        color = TextWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Black.copy(alpha = 0.4f))
                            .clickable { viewModel.setVideoVolume(videoVolume + 0.2f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Rotating music vinyl disk
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .rotate(rotationAngle)
                    .background(Color.DarkGray)
                    .border(2.dp, CyanAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Black)
                )
            }
        }

        // REAL-TIME COMMENTS BOTTOM SHEET SLIDE IN OVERLAY
        AnimatedVisibility(
            visible = showComments,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                border = BorderStroke(1.dp, SurfaceVariantDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Title and Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${comments.size} COMMENTS",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        IconButton(onClick = { showComments = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                        }
                    }
                    HorizontalDivider(color = SurfaceVariantDark)

                    // Comments lazy column list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        if (comments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Be the first to share your mind!", color = TextGray)
                                }
                            }
                        } else {
                            items(comments) { comment ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(CyanAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = comment.userAvatarUrl,
                                            color = Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = comment.username, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = comment.text, color = TextWhite.copy(alpha = 0.9f), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Input Form
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Add comment on this sound...", color = TextMuted, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = SurfaceVariantDark
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    viewModel.addComment(video.id, commentText)
                                    commentText = ""
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CyanAccent)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// 3.6 EXPANDABLE DESCRIPTION WITH HASHTAG PARSING
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandableDescriptionWithHashtags(
    description: String,
    onHashtagClick: (String) -> Unit
) {
    val words = remember(description) { description.split(" ") }
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        words.forEach { word ->
            if (word.startsWith("#") && word.length > 1) {
                Text(
                    text = word,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onHashtagClick(word) }
                )
            } else {
                Text(
                    text = word,
                    color = TextWhite,
                    fontSize = 12.sp
                )
            }
        }
    }
}

fun saveUriToInternalStorage(context: android.content.Context, uri: android.net.Uri, isVideo: Boolean): String? {
    return try {
        val extension = if (isVideo) "mp4" else "jpg"
        val fileName = "deentok_${System.currentTimeMillis()}.$extension"
        val outputFile = java.io.File(context.filesDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            java.io.FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        outputFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun isVideoFile(path: String): Boolean {
    val lower = path.lowercase()
    return lower.endsWith(".mp4") || lower.endsWith(".3gp") || lower.endsWith(".mkv") || lower.endsWith(".webm") || lower.contains("video")
}

fun isImageFile(path: String): Boolean {
    val lower = path.lowercase()
    return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") || lower.contains("image")
}

@Composable
fun VideoPlayer(
    videoPath: String,
    isMuted: Boolean = false,
    volume: Float = 0.8f,
    modifier: Modifier = Modifier
) {
    var mpRef by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    
    LaunchedEffect(isMuted, volume, mpRef) {
        mpRef?.let { mp ->
            try {
                val finalVol = if (isMuted) 0f else volume
                mp.setVolume(finalVol, finalVol)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            android.widget.VideoView(ctx).apply {
                try {
                    tag = videoPath
                    setVideoPath(videoPath)
                    setOnPreparedListener { mp ->
                        mpRef = mp
                        mp.isLooping = true
                        val finalVol = if (isMuted) 0f else volume
                        mp.setVolume(finalVol, finalVol)
                        start()
                    }
                    setOnErrorListener { _, _, _ ->
                        true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        },
        update = { videoView ->
            val loadedPath = videoView.tag as? String
            if (loadedPath != videoPath) {
                videoView.tag = videoPath
                try {
                    videoView.setVideoPath(videoPath)
                    videoView.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        },
        modifier = modifier
    )
}

// 4. UPLOAD STUDIO SCREEN (PERSISTED STORAGE WITH REAL MEDIA CAPTURE)
@Composable
fun UploadScreen(viewModel: DeenTokViewModel) {
    var currentCreatorTab by remember { mutableStateOf("Upload") } // "Camera", "Upload", "Templates", "Drafts"
    var isEditingWorkspace by remember { mutableStateOf(false) }

    // Upload & Publishing settings states
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Quran") }
    var contentType by remember { mutableStateOf("Reminder") }
    var quranRef by remember { mutableStateOf("") }
    var hadithRef by remember { mutableStateOf("") }
    var sourceInfo by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var mentionsInput by remember { mutableStateOf("") }
    var isPrivateAccount by remember { mutableStateOf(false) }
    var allowComments by remember { mutableStateOf(true) }
    var allowSharing by remember { mutableStateOf(true) }
    var allowDownloads by remember { mutableStateOf(true) }

    // Media states
    var selectedMediaPaths by remember { mutableStateOf<List<String>>(emptyList()) }
    var mediaType by remember { mutableStateOf("video") } // "video" or "image"

    // Sound & Music states
    var selectedMusicName by remember { mutableStateOf("Original Sound") }
    var musicVolume by remember { mutableStateOf(0.5f) }
    var originalVolume by remember { mutableStateOf(0.8f) }
    var isMuted by remember { mutableStateOf(false) }
    var favoriteSounds by remember { mutableStateOf(setOf("Electro Cyber Synth", "Modern Trap Hype")) }
    var searchQuerySound by remember { mutableStateOf("") }
    var soundTab by remember { mutableStateOf(0) } // 0 = Trending, 1 = Favorites

    // Video Editing Tool states
    var trimStart by remember { mutableStateOf(0.0f) }
    var trimEnd by remember { mutableStateOf(15.0f) }
    var maxClipLength by remember { mutableStateOf(15.0f) }
    var rotationAngle by remember { mutableStateOf(0f) }
    var cropRatio by remember { mutableStateOf("9:16") }
    var activeFilter by remember { mutableStateOf("Normal") }
    var brightnessVal by remember { mutableStateOf(0.0f) }
    var contrastVal by remember { mutableStateOf(0.0f) }
    var saturationVal by remember { mutableStateOf(0.0f) }
    var isBlurEnabled by remember { mutableStateOf(false) }

    // Creative Overlays
    var textOverlayInput by remember { mutableStateOf("") }
    var textStyleName by remember { mutableStateOf("Classic") } // "Classic", "Neon", "Sleek", "Typewriter"
    var addedStickers by remember { mutableStateOf<List<String>>(emptyList()) }
    var showStickerPicker by remember { mutableStateOf(false) }
    
    // Freehand Drawing Canvas state
    var isDrawingMode by remember { mutableStateOf(false) }
    var brushColor by remember { mutableStateOf(CyanAccent) }
    var brushSize by remember { mutableStateOf(8f) }
    val drawingPaths = remember { mutableStateListOf<Pair<androidx.compose.ui.graphics.Path, Color>>() }
    var currentPath by remember { mutableStateOf<androidx.compose.ui.graphics.Path?>(null) }

    // Cover Thumbnail settings
    var coverFrameIndex by remember { mutableStateOf(0) }
    var coverOverlayText by remember { mutableStateOf("") }

    // Camera states
    var isRecording by remember { mutableStateOf(false) }
    var cameraFacingFront by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var selectedTimerSecs by remember { mutableStateOf(0) } // 0 = Off, 3, 5, 10
    var timerCountdown by remember { mutableStateOf(0) }
    var maxRecordingDuration by remember { mutableStateOf(15f) } // 15s, 30s, 60s, 180s
    var cameraClips by remember { mutableStateOf<List<Float>>(emptyList()) } // Accumulated clip durations
    var activeClipDuration by remember { mutableStateOf(0f) }

    // Templates Screen state
    var selectedTemplateCategory by remember { mutableStateOf("Trending") }
    var previewingTemplate by remember { mutableStateOf<VideoTemplate?>(null) }

    // Core states from View Model
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()
    val uploadSuccessMessage by viewModel.uploadSuccessMessage.collectAsState()
    val templates by viewModel.allTemplates.collectAsState()
    val drafts by viewModel.allDrafts.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    // Preset High-Fidelity assets for creative testing
    val sampleGalleryMedia = listOf(
        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=300&q=80" to "video",
        "https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&w=300&q=80" to "video",
        "https://images.unsplash.com/photo-1541701494587-cb58502866ab?auto=format&fit=crop&w=300&q=80" to "image",
        "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?auto=format&fit=crop&w=300&q=80" to "image",
        "https://images.unsplash.com/photo-1607799279861-4dd421887fb3?auto=format&fit=crop&w=300&q=80" to "video"
    )

    val availableSounds = listOf(
        "Electro Cyber Synth" to "Trending",
        "Acoustic Sunset Mood" to "Trending",
        "Modern Trap Hype" to "Trending",
        "Nostalgia Chill Lofi" to "Relaxing",
        "Retro Synthwave" to "Chill",
        "Vocal Deep Techno" to "Club"
    ).filter { it.first.contains(searchQuerySound, ignoreCase = true) }

    // System pickers
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri) ?: ""
            val isVid = mime.contains("video") || uri.toString().contains("video")
            val saved = saveUriToInternalStorage(context, uri, isVid)
            if (saved != null) {
                selectedMediaPaths = selectedMediaPaths + saved
                mediaType = if (isVid) "video" else "image"
                isEditingWorkspace = true
            }
        }
    }

    var simulatedFailToggle by remember { mutableStateOf(false) }

    // Launcher for camera photo capture
    val cameraPhotoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                val savedPath = saveUriToInternalStorage(context, uri, false)
                if (savedPath != null) {
                    selectedMediaPaths = selectedMediaPaths + savedPath
                    mediaType = "image"
                    isEditingWorkspace = true
                }
            } else {
                val bitmap = result.data?.extras?.get("data") as? android.graphics.Bitmap
                if (bitmap != null) {
                    try {
                        val file = java.io.File(context.filesDir, "deentok_photo_${System.currentTimeMillis()}.jpg")
                        java.io.FileOutputStream(file).use { out ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, out)
                        }
                        selectedMediaPaths = selectedMediaPaths + file.absolutePath
                        mediaType = "image"
                        isEditingWorkspace = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // Launcher for camera video capture
    val cameraVideoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                val savedPath = saveUriToInternalStorage(context, uri, true)
                if (savedPath != null) {
                    selectedMediaPaths = selectedMediaPaths + savedPath
                    mediaType = "video"
                    isEditingWorkspace = true
                }
            }
        }
    }

    // Launcher for requesting dangerous permissions at runtime
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[android.Manifest.permission.RECORD_AUDIO] ?: false
        if (!cameraGranted) {
            android.widget.Toast.makeText(context, "Camera permission is required to capture photos and videos.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Helper to check permissions and launch cameras
    fun checkAndLaunchCamera(isVideo: Boolean) {
        val hasCamera = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasAudio = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (hasCamera && (!isVideo || hasAudio)) {
            if (isVideo) {
                cameraVideoLauncher.launch(android.content.Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE))
            } else {
                cameraPhotoLauncher.launch(android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE))
            }
        } else {
            val permissionsToRequest = mutableListOf<String>()
            if (!hasCamera) permissionsToRequest.add(android.Manifest.permission.CAMERA)
            if (isVideo && !hasAudio) permissionsToRequest.add(android.Manifest.permission.RECORD_AUDIO)
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Timer Countdown animation
    if (timerCountdown > 0) {
        LaunchedEffect(timerCountdown) {
            kotlinx.coroutines.delay(1000)
            timerCountdown -= 1
            if (timerCountdown == 0) {
                isRecording = true
            }
        }
    }

    // Recording duration incremental loop
    if (isRecording) {
        LaunchedEffect(Unit) {
            while (isRecording) {
                kotlinx.coroutines.delay(100)
                activeClipDuration += 0.1f
                val totalDuration = cameraClips.sum() + activeClipDuration
                if (totalDuration >= maxRecordingDuration) {
                    cameraClips = cameraClips + activeClipDuration
                    activeClipDuration = 0f
                    isRecording = false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        if (uploadProgress != null || uploadError != null || uploadSuccessMessage != null) {
            // HIGH FIDELITY UPLOAD MODAL CARD
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "DEENTOK SYNC ENGINE",
                            color = CyanAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            letterSpacing = 1.5.sp
                        )

                        if (uploadProgress != null) {
                            CircularProgressIndicator(
                                progress = { uploadProgress ?: 0f },
                                color = CyanAccent,
                                modifier = Modifier.size(72.dp),
                                strokeWidth = 6.dp
                            )
                            Text(
                                "Uploading voice segments: ${(uploadProgress!! * 100).toInt()}%",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                "Streaming direct-to-node transaction with Room SQLite indexes...",
                                color = TextGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { viewModel.cancelUpload() },
                                colors = ButtonDefaults.buttonColors(containerColor = LikeRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("cancel_upload_btn")
                            ) {
                                Text("Cancel Upload", color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (uploadError != null) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = LikeRed,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                "Upload Failed",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                uploadError ?: "",
                                color = TextGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.clearUploadStatus() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Dismiss", color = TextWhite)
                                }
                                Button(
                                    onClick = { viewModel.retryUpload() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                    modifier = Modifier.weight(1f).testTag("retry_upload_btn")
                                ) {
                                    Text("Retry Upload", color = Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (uploadSuccessMessage != null) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = CyanAccent,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                "Upload Successful!",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                uploadSuccessMessage ?: "",
                                color = TextGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else if (isEditingWorkspace) {
            // ACTIVE EDITING STUDIO WORKSPACE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isEditingWorkspace = false }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                    Text(
                        "EDIT CREATION",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                    Button(
                        onClick = {
                            viewModel.saveDraft(
                                title = title.ifBlank { "Untitled Draft" },
                                description = description,
                                mediaPaths = selectedMediaPaths,
                                mediaType = mediaType,
                                category = category,
                                musicName = selectedMusicName,
                                filterName = activeFilter,
                                textOverlay = textOverlayInput,
                                coverText = coverOverlayText,
                                isPrivate = isPrivateAccount
                            )
                            isEditingWorkspace = false
                            android.widget.Toast.makeText(context, "Draft Saved!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, CyanAccent)
                    ) {
                        Text("Save Draft", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                // Interactive Visual Preview (Includes overlays, text styles, drawings, filters)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    val previewModel = selectedMediaPaths.getOrNull(0) ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=300&q=80"
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotationAngle)
                    ) {
                        coil.compose.AsyncImage(
                            model = previewModel,
                            contentDescription = "Media Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = if (cropRatio == "9:16") androidx.compose.ui.layout.ContentScale.Crop else androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }

                    // Apply Overlay filter visually
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                when (activeFilter) {
                                    "Mono" -> Color.Black.copy(alpha = 0.4f)
                                    "Neon" -> CyanAccent.copy(alpha = 0.2f)
                                    "Warm" -> Color.Red.copy(alpha = 0.15f)
                                    "Cool" -> Color.Blue.copy(alpha = 0.15f)
                                    "Cyberpunk" -> LikeRed.copy(alpha = 0.15f)
                                    else -> Color.Transparent
                                }
                            )
                    )

                    // Overlay Freehand Drawings
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(isDrawingMode) {
                                if (isDrawingMode) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val p = androidx.compose.ui.graphics.Path().apply {
                                                moveTo(offset.x, offset.y)
                                            }
                                            currentPath = p
                                            drawingPaths.add(p to brushColor)
                                        },
                                        onDragEnd = {
                                            currentPath = null
                                        },
                                        onDragCancel = {},
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            currentPath?.lineTo(change.position.x, change.position.y)
                                            val last = drawingPaths.lastOrNull()
                                            if (last != null) {
                                                drawingPaths[drawingPaths.lastIndex] = last
                                            }
                                        }
                                    )
                                }
                            }
                    ) {
                        drawingPaths.forEach { (path, color) ->
                            drawPath(
                                path = path,
                                color = color,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = brushSize)
                            )
                        }
                    }

                    // Text Overlays
                    if (textOverlayInput.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(
                                    if (textStyleName == "Neon") CyanAccent.copy(alpha = 0.15f) else Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = textOverlayInput,
                                color = if (textStyleName == "Neon") CyanAccent else TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (textStyleName == "Typewriter") 14.sp else 18.sp,
                                style = TextStyle(
                                    letterSpacing = if (textStyleName == "Sleek") 3.sp else 0.5.sp
                                )
                            )
                        }
                    }

                    // Stickers
                    addedStickers.forEachIndexed { idx, emoji ->
                        Box(
                            modifier = Modifier
                                .align(if (idx % 2 == 0) Alignment.TopStart else Alignment.BottomEnd)
                                .padding(24.dp)
                        ) {
                            Text(emoji, fontSize = 32.sp)
                        }
                    }

                    // Cover Text preview
                    if (coverOverlayText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(CyanAccent.copy(alpha = 0.8f))
                                .padding(4.dp)
                        ) {
                            Text(
                                text = coverOverlayText.uppercase(),
                                color = Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // BASIC EDITING PANEL
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Basic Editing tools", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Trim controls
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Trim Start: ${trimStart.toInt()}s", color = TextGray, fontSize = 10.sp)
                                Slider(
                                    value = trimStart,
                                    onValueChange = { trimStart = it.coerceIn(0f, trimEnd - 1f) },
                                    valueRange = 0f..maxClipLength,
                                    colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Trim End: ${trimEnd.toInt()}s", color = TextGray, fontSize = 10.sp)
                                Slider(
                                    value = trimEnd,
                                    onValueChange = { trimEnd = it.coerceIn(trimStart + 1f, maxClipLength) },
                                    valueRange = 0f..maxClipLength,
                                    colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    rotationAngle = (rotationAngle + 90f) % 360f
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Rotate", modifier = Modifier.size(14.dp), tint = TextWhite)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rotate 90°", color = TextWhite, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    cropRatio = if (cropRatio == "9:16") "1:1" else "9:16"
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.Crop, contentDescription = "Crop", modifier = Modifier.size(14.dp), tint = TextWhite)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aspect ($cropRatio)", color = TextWhite, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    // Simulated Split clip action
                                    android.widget.Toast.makeText(context, "Clip split successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.ContentCut, contentDescription = "Split", modifier = Modifier.size(14.dp), tint = TextWhite)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Split Clip", color = TextWhite, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // VISUAL EDITING & FILTERS PANEL
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Filters & Lighting", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        
                        // Filter list row
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Normal", "Mono", "Neon", "Warm", "Cool", "Cyberpunk").forEach { filt ->
                                val sel = filt == activeFilter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) CyanAccent else SurfaceDark)
                                        .clickable { activeFilter = filt }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(filt, color = if (sel) Black else TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Brightness, Contrast, Saturation sliders
                        Column {
                            Text("Brightness: ${(brightnessVal * 100).toInt()}%", color = TextGray, fontSize = 10.sp)
                            Slider(
                                value = brightnessVal,
                                onValueChange = { brightnessVal = it },
                                valueRange = -0.5f..0.5f,
                                colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                            )
                        }
                        Column {
                            Text("Contrast: ${(contrastVal * 100).toInt()}%", color = TextGray, fontSize = 10.sp)
                            Slider(
                                value = contrastVal,
                                onValueChange = { contrastVal = it },
                                valueRange = -0.5f..0.5f,
                                colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                            )
                        }
                    }
                }

                // CREATIVE EDITING (TEXT, DRAWING, STICKERS)
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Creative Overlays", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        OutlinedTextField(
                            value = textOverlayInput,
                            onValueChange = { textOverlayInput = it },
                            label = { Text("Video Overlay Text", color = TextGray) },
                            textStyle = TextStyle(color = TextWhite),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceVariantDark)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Classic", "Neon", "Sleek", "Typewriter").forEach { style ->
                                val sel = textStyleName == style
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) CyanAccent else Black)
                                        .clickable { textStyleName = style }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(style, color = if (sel) Black else TextWhite, fontSize = 10.sp)
                                }
                            }
                        }

                        // Stickers list picker
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Quick Stickers:", color = TextGray, fontSize = 11.sp)
                            listOf("🔥", "🎉", "🚀", "👑", "✨", "🎧", "😂").forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Black)
                                        .clickable { addedStickers = addedStickers + emoji }
                                        .padding(6.dp)
                                ) {
                                    Text(emoji, fontSize = 16.sp)
                                }
                            }
                        }

                        // Drawing mode toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isDrawingMode,
                                    onCheckedChange = { isDrawingMode = it },
                                    colors = CheckboxDefaults.colors(checkedColor = CyanAccent, checkmarkColor = Black)
                                )
                                Text("Enable Freehand Neon Drawing", color = TextWhite, fontSize = 11.sp)
                            }
                            if (isDrawingMode) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(CyanAccent, LikeRed, Color.Yellow, Color.Green).forEach { col ->
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(col)
                                                .border(
                                                    if (brushColor == col) 2.dp else 0.dp,
                                                    TextWhite,
                                                    CircleShape
                                                )
                                                .clickable { brushColor = col }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // MUSIC AND SOUND CARD
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Music and Audio Settings", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.MusicNote, contentDescription = "Music", tint = CyanAccent)
                            Text("Track Selected: $selectedMusicName", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Quick Music Selector search / trending sound library
                        OutlinedTextField(
                            value = searchQuerySound,
                            onValueChange = { searchQuerySound = it },
                            placeholder = { Text("Search Sound Library...", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceVariantDark)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableSounds.take(3).forEach { (sound, cat) ->
                                val isFav = favoriteSounds.contains(sound)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Black)
                                        .clickable { selectedMusicName = sound }
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(sound, color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Icon(
                                                imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "Fav",
                                                tint = Color.Yellow,
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clickable {
                                                        favoriteSounds = if (isFav) favoriteSounds - sound else favoriteSounds + sound
                                                    }
                                            )
                                        }
                                        Text(cat, color = TextGray, fontSize = 8.sp)
                                    }
                                }
                            }
                        }

                        // Volumes sliders
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Music Vol: ${(musicVolume * 100).toInt()}%", color = TextGray, fontSize = 9.sp)
                                Slider(
                                    value = musicVolume,
                                    onValueChange = { musicVolume = it },
                                    colors = SliderDefaults.colors(thumbColor = CyanAccent)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Original Vol: ${(originalVolume * 100).toInt()}%", color = TextGray, fontSize = 9.sp)
                                Slider(
                                    value = originalVolume,
                                    onValueChange = { originalVolume = it },
                                    colors = SliderDefaults.colors(thumbColor = CyanAccent)
                                )
                            }
                        }
                    }
                }

                // POST SETTINGS & COVER THUMBNAIL
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Post Settings & Cover", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Cover frames selector
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Choose Thumbnail Cover Text:", color = TextGray, fontSize = 10.sp)
                                OutlinedTextField(
                                    value = coverOverlayText,
                                    onValueChange = { coverOverlayText = it },
                                    placeholder = { Text("Intro text overlay...", color = TextMuted) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent)
                                )
                            }
                            Column(modifier = Modifier.width(80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Frame Pick", color = TextGray, fontSize = 8.sp)
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Black)
                                        .clickable { coverFrameIndex = (coverFrameIndex + 1) % 4 }
                                ) {
                                    Text("Frame $coverFrameIndex", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }

                        // Title, Captions, Hashtags, Location
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Video Title", color = TextGray) },
                            modifier = Modifier.fillMaxWidth().testTag("upload_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Caption & #Hashtags", color = TextGray) },
                            modifier = Modifier.fillMaxWidth().testTag("upload_desc_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("#deen", "#quran", "#hadith", "#ramadan", "#islamicreminders", "#deentok").forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Black)
                                        .clickable {
                                            if (!description.contains(tag)) {
                                                description = "$description $tag".trim()
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(tag, color = CyanAccent, fontSize = 10.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = locationInput,
                            onValueChange = { locationInput = it },
                            label = { Text("Add Location (Optional)", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                        )

                        // Category
                        Text("Category", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Quran", "Hadith", "Tafsir", "Seerah", "Islamic Knowledge", "Dawah", "Islamic History", "Islamic Lifestyle", "Dua", "Ramadan", "Hajj & Umrah", "Muslim Community").forEach { cat ->
                                val sel = category == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) CyanAccent else Black)
                                        .clickable { category = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(cat, color = if (sel) Black else TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ISLAMIC METADATA AND CONTENT TYPE CARD
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Islamic Metadata & Content Classification", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        
                        // Content Type Selector
                        Text("Content Type", color = TextGray, fontSize = 11.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Quran", "Hadith", "Tafsir", "Reminder", "Dawah", "Islamic Knowledge", "Story", "Other").forEach { type ->
                                val sel = contentType == type
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) CyanAccent else Black)
                                        .clickable { contentType = type }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(type, color = if (sel) Black else TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Quran Verse Ref
                        OutlinedTextField(
                            value = quranRef,
                            onValueChange = { quranRef = it },
                            label = { Text("Quran Verse Reference (e.g. 55:1-13) (Optional)", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                        )

                        // Hadith Ref
                        OutlinedTextField(
                            value = hadithRef,
                            onValueChange = { hadithRef = it },
                            label = { Text("Hadith Reference (e.g. Bukhari 7376) (Optional)", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                        )

                        // Source/Reference info
                        OutlinedTextField(
                            value = sourceInfo,
                            onValueChange = { sourceInfo = it },
                            label = { Text("Source / Study Reference Information (Optional)", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                        )
                    }
                }

                // PRIVACY SETTINGS CARD
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Privacy and Permissions", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Visibility Status:", color = TextWhite, fontSize = 11.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Everyone", "Followers only", "Only me").forEach { vis ->
                                    val sel = (vis == "Everyone" && !isPrivateAccount) || (vis == "Only me" && isPrivateAccount)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (sel) CyanAccent else Black)
                                            .clickable { isPrivateAccount = (vis == "Only me") }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(vis, color = if (sel) Black else TextWhite, fontSize = 9.sp)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allow Comments", color = TextGray, fontSize = 11.sp)
                            Switch(checked = allowComments, onCheckedChange = { allowComments = it }, colors = SwitchDefaults.colors(checkedTrackColor = CyanAccent))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allow Sharing & Duet", color = TextGray, fontSize = 11.sp)
                            Switch(checked = allowSharing, onCheckedChange = { allowSharing = it }, colors = SwitchDefaults.colors(checkedTrackColor = CyanAccent))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allow Direct Downloads", color = TextGray, fontSize = 11.sp)
                            Switch(checked = allowDownloads, onCheckedChange = { allowDownloads = it }, colors = SwitchDefaults.colors(checkedTrackColor = CyanAccent))
                        }
                    }
                }

                // SIMULATE ERROR SWITCH FOR TESTING
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Simulate Network Error (To test retry flow)", color = TextGray, fontSize = 11.sp)
                    Switch(checked = simulatedFailToggle, onCheckedChange = { simulatedFailToggle = it }, colors = SwitchDefaults.colors(checkedTrackColor = CyanAccent))
                }

                // Final Publish Button
                Button(
                    onClick = {
                        viewModel.uploadVideoWithSettings(
                            title = title,
                            description = description,
                            category = category,
                            videoPath = selectedMediaPaths.getOrNull(0) ?: "",
                            isPrivate = isPrivateAccount,
                            musicName = selectedMusicName,
                            allowComments = allowComments,
                            allowSharing = allowSharing,
                            allowDownloads = allowDownloads,
                            location = locationInput,
                            coverText = coverOverlayText,
                            shouldSimulateFail = simulatedFailToggle,
                            quranRef = quranRef,
                            hadithRef = hadithRef,
                            sourceInfo = sourceInfo,
                            contentType = contentType
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("publish_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(12.dp),
                    enabled = title.isNotBlank()
                ) {
                    Text("Publish Video Stream 🚀", color = Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        } else {
            // MAIN SELECTOR / CREATOR MODE MENU
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Header Title
                Text(
                    text = "DEENTOK CREATION HUB",
                    color = TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // The 4 Core Tab Selectors
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Camera", "Upload", "Templates", "Drafts").forEach { tab ->
                        val sel = currentCreatorTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (sel) CyanAccent else Color.Transparent)
                                .clickable { currentCreatorTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                tab,
                                color = if (sel) Black else TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Future Placeholders
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(SurfaceDark, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📡 LIVE (Coming Soon)", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(SurfaceDark, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎬 Stories (Coming Soon)", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // VIEWPORT RENDER ACCORDING TO ACTIVE TAB
                Box(modifier = Modifier.weight(1f)) {
                    when (currentCreatorTab) {
                        "Camera" -> {
                            // 2. CAMERA CREATION INTERFACE
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Camera Viewport HUD
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(SurfaceDark)
                                        .border(1.5.dp, CyanAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Live camera silhouette/video feed simulation
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(
                                            imageVector = if (cameraFacingFront) Icons.Default.Face else Icons.Default.PhotoCamera,
                                            contentDescription = "Lens",
                                            tint = CyanAccent,
                                            modifier = Modifier.size(56.dp)
                                        )
                                        
                                        if (timerCountdown > 0) {
                                            Text(
                                                "Timer Countdown: $timerCountdown",
                                                color = Color.Yellow,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        } else if (isRecording) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(LikeRed)
                                                )
                                                Text(
                                                    "Recording: ${activeClipDuration.toInt()}s / ${maxRecordingDuration.toInt()}s",
                                                    color = TextWhite,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else {
                                            Text(
                                                "Simulated ${if (cameraFacingFront) "Front" else "Back"} Viewport Feed",
                                                color = TextWhite,
                                                fontSize = 12.sp
                                            )
                                        }

                                        if (isFlashOn) {
                                            Text("⚡ Flash Active (Torch mode)", color = Color.Yellow, fontSize = 10.sp)
                                        }
                                    }

                                    // HUD Controls overlays
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Back/Front Switcher
                                        IconButton(
                                            onClick = { cameraFacingFront = !cameraFacingFront },
                                            modifier = Modifier.background(Black.copy(alpha = 0.6f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Switch lens", tint = CyanAccent)
                                        }

                                        // Flash trigger
                                        IconButton(
                                            onClick = { isFlashOn = !isFlashOn },
                                            modifier = Modifier.background(Black.copy(alpha = 0.6f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                                contentDescription = "Flash toggle",
                                                tint = if (isFlashOn) Color.Yellow else TextWhite
                                            )
                                        }

                                        // Timer switch
                                        IconButton(
                                            onClick = {
                                                selectedTimerSecs = when (selectedTimerSecs) {
                                                    0 -> 3
                                                    3 -> 5
                                                    5 -> 10
                                                    else -> 0
                                                }
                                            },
                                            modifier = Modifier.background(Black.copy(alpha = 0.6f), CircleShape)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Timer, contentDescription = "Timer", tint = CyanAccent)
                                                if (selectedTimerSecs > 0) {
                                                    Text(
                                                        "${selectedTimerSecs}s",
                                                        color = Black,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier
                                                            .background(CyanAccent, CircleShape)
                                                            .padding(2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Video Length selectors
                                Column {
                                    Text("Video Duration Limit:", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(15f to "15s", 30f to "30s", 60f to "60s", 180f to "3m").forEach { (duration, label) ->
                                            val sel = maxRecordingDuration == duration
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (sel) CyanAccent else SurfaceDark)
                                                    .clickable { maxRecordingDuration = duration }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(label, color = if (sel) Black else TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // Recording progress indicator (Multi-clip support)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Recording Progress HUD Segment Bar:", color = TextGray, fontSize = 10.sp)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SurfaceDark)
                                    ) {
                                        val totalClipsTime = cameraClips.sum() + activeClipDuration
                                        val pct = (totalClipsTime / maxRecordingDuration).coerceIn(0f, 1f)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(pct)
                                                .background(CyanAccent)
                                        )
                                    }
                                }

                                // Active capture actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Start / Pause Recording
                                    Button(
                                        onClick = {
                                            if (selectedTimerSecs > 0 && !isRecording) {
                                                timerCountdown = selectedTimerSecs
                                            } else {
                                                if (isRecording) {
                                                    cameraClips = cameraClips + activeClipDuration
                                                    activeClipDuration = 0f
                                                }
                                                isRecording = !isRecording
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) LikeRed else CyanAccent),
                                        modifier = Modifier.weight(1.5f)
                                    ) {
                                        Icon(
                                            imageVector = if (isRecording) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Capture",
                                            tint = if (isRecording) TextWhite else Black
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            if (isRecording) "Pause Clips" else "Record Clip",
                                            color = if (isRecording) TextWhite else Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Delete Last Clip
                                    Button(
                                        onClick = {
                                            if (cameraClips.isNotEmpty()) {
                                                cameraClips = cameraClips.dropLast(1)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                        border = BorderStroke(1.dp, LikeRed),
                                        enabled = cameraClips.isNotEmpty(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Delete Last", color = LikeRed, fontSize = 11.sp)
                                    }

                                    // Proceed to Editor Checkmark
                                    IconButton(
                                        onClick = {
                                            if (cameraClips.isNotEmpty()) {
                                                selectedMediaPaths = listOf("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=300&q=80")
                                                mediaType = "video"
                                                isEditingWorkspace = true
                                            } else {
                                                android.widget.Toast.makeText(context, "Please record at least one segment first!", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .background(CyanAccent, CircleShape)
                                            .size(42.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Done", tint = Black)
                                    }
                                }

                                // Real camera capture trigger
                                Button(
                                    onClick = { checkAndLaunchCamera(isVideo = true) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                    border = BorderStroke(1.dp, CyanAccent)
                                ) {
                                    Icon(Icons.Default.Videocam, contentDescription = "Hardware", tint = CyanAccent)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Launch Real Device Camera Capture", color = TextWhite)
                                }
                            }
                        }

                        "Upload" -> {
                            // 3. UPLOAD SYSTEM FROM GALLERY
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    border = BorderStroke(1.dp, CyanAccent),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Icon(Icons.Default.FolderOpen, contentDescription = "Files", tint = CyanAccent, modifier = Modifier.size(48.dp))
                                        Text("Select Native Files", color = TextWhite, fontWeight = FontWeight.Bold)
                                        Text("Import multiple videos or pictures directly from your local device storage gallery.", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                                        
                                        Button(
                                            onClick = { galleryLauncher.launch("*/*") },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                                        ) {
                                            Text("Choose Native Files", color = Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Multiple select simulation preset clips
                                Text("Select High-Fidelity Creator Presets:", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    sampleGalleryMedia.forEachIndexed { index, (url, mType) ->
                                        val isSelected = selectedMediaPaths.contains(url)
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .border(
                                                    if (isSelected) 2.dp else 0.dp,
                                                    CyanAccent,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    selectedMediaPaths = if (isSelected) {
                                                        selectedMediaPaths - url
                                                    } else {
                                                        selectedMediaPaths + url
                                                    }
                                                    mediaType = mType
                                                },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Box {
                                                coil.compose.AsyncImage(
                                                    model = url,
                                                    contentDescription = "Sample $index",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(4.dp)
                                                            .size(16.dp)
                                                            .clip(CircleShape)
                                                            .background(CyanAccent),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("${selectedMediaPaths.indexOf(url) + 1}", color = Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Rearrange & Preview selected clips list
                                if (selectedMediaPaths.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Rearrange Selected Clips & Preview order:", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        selectedMediaPaths.forEachIndexed { index, path ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(SurfaceDark, RoundedCornerShape(8.dp))
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(4.dp)).background(Black)) {
                                                        coil.compose.AsyncImage(model = path, contentDescription = "Clip Preview", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                                                    }
                                                    Text("Clip #${index + 1} (${mediaType.uppercase()})", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    // Move Up / Rearrange
                                                    if (index > 0) {
                                                        IconButton(
                                                            onClick = {
                                                                val mutable = selectedMediaPaths.toMutableList()
                                                                val temp = mutable[index]
                                                                mutable[index] = mutable[index - 1]
                                                                mutable[index - 1] = temp
                                                                selectedMediaPaths = mutable
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.ArrowDropUp, contentDescription = "Up", tint = CyanAccent)
                                                        }
                                                    }

                                                    // Remove selected
                                                    IconButton(
                                                        onClick = { selectedMediaPaths = selectedMediaPaths - path },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = LikeRed)
                                                    }
                                                }
                                            }
                                        }

                                        // Go to Editor workspace
                                        Button(
                                            onClick = { isEditingWorkspace = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                            modifier = Modifier.fillMaxWidth().height(46.dp)
                                        ) {
                                            Text("Enter Creator Editing Workspace ✨", color = Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        "Templates" -> {
                            // 6. TEMPLATE CHOOSER SCREEN
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Category filter
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Trending", "Photo", "Business", "Education", "Celebration", "Story").forEach { cat ->
                                        val sel = selectedTemplateCategory == cat
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (sel) CyanAccent else SurfaceDark)
                                                .clickable { selectedTemplateCategory = cat }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(cat, color = if (sel) Black else TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Templates matching selected category
                                val filteredTemplates = templates.filter { it.category == selectedTemplateCategory && it.isActive }
                                if (filteredTemplates.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(150.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No active templates in this category.", color = TextGray, fontSize = 12.sp)
                                    }
                                } else {
                                    filteredTemplates.forEach { template ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                            border = BorderStroke(1.dp, SurfaceVariantDark),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { previewingTemplate = template }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Black)
                                                ) {
                                                    coil.compose.AsyncImage(
                                                        model = template.thumbnailUrl,
                                                        contentDescription = "Thumb",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(template.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(template.description, color = TextGray, fontSize = 11.sp, maxLines = 1)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("🎵 ${template.defaultMusic}", color = CyanAccent, fontSize = 9.sp)
                                                }

                                                Icon(Icons.Default.PlayCircle, contentDescription = "Preview", tint = CyanAccent, modifier = Modifier.size(24.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "Drafts" -> {
                            // 10. DRAFTS SELECTOR
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (drafts.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(150.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No saved drafts found. Record or Import to save drafts!", color = TextGray, fontSize = 12.sp)
                                    }
                                } else {
                                    drafts.forEach { dft ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                            border = BorderStroke(1.dp, SurfaceVariantDark),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            title = dft.title
                                                            description = dft.description
                                                            category = dft.category
                                                            selectedMediaPaths = dft.mediaPaths
                                                            mediaType = dft.mediaType
                                                            selectedMusicName = dft.musicName
                                                            activeFilter = dft.filterName
                                                            textOverlayInput = dft.textOverlay
                                                            coverOverlayText = dft.coverText
                                                            isPrivateAccount = dft.isPrivate
                                                            isEditingWorkspace = true
                                                        }
                                                ) {
                                                    Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(6.dp)).background(Black)) {
                                                        coil.compose.AsyncImage(
                                                            model = dft.mediaPaths.getOrNull(0) ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=150&q=80",
                                                            contentDescription = "Draft thumbnail",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                        )
                                                    }
                                                    Column {
                                                        Text(dft.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        Text("🎵 ${dft.musicName} • Filter: ${dft.filterName}", color = TextGray, fontSize = 10.sp)
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { viewModel.deleteDraft(dft.id) }
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LikeRed, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Template Live Preview Dialog Sheet
    val currentPreviewTemplate = previewingTemplate
    if (currentPreviewTemplate != null) {
        AlertDialog(
            onDismissRequest = { previewingTemplate = null },
            title = { Text(currentPreviewTemplate.title, color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        coil.compose.AsyncImage(
                            model = currentPreviewTemplate.thumbnailUrl,
                            contentDescription = currentPreviewTemplate.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    Text(currentPreviewTemplate.description, color = TextWhite, fontSize = 12.sp)
                    Text("🎵 Default Track: ${currentPreviewTemplate.defaultMusic}", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Apply template values to current editing workspace
                        title = "${currentPreviewTemplate.title} Style 🎥⚡"
                        description = "Recreating the viral ${currentPreviewTemplate.title}! #templates"
                        category = currentPreviewTemplate.category
                        selectedMusicName = currentPreviewTemplate.defaultMusic
                        textOverlayInput = currentPreviewTemplate.defaultText
                        selectedMediaPaths = listOf(currentPreviewTemplate.thumbnailUrl)
                        mediaType = "video"
                        isEditingWorkspace = true
                        previewingTemplate = null
                        
                        viewModel.incrementTemplateUsage(currentPreviewTemplate.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Use This Template", color = Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { previewingTemplate = null }) {
                    Text("Close", color = TextGray)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

// 5. LIVE STREAM SCREEN (FUTURE PLACEHOLDER)
@Composable
fun LiveScreen(viewModel: DeenTokViewModel) {
    var signupState by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = LikeRed,
                    spotColor = LikeRed
                )
                .clip(CircleShape)
                .background(LikeRed.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Stream,
                contentDescription = "Live Streams",
                tint = LikeRed,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "DEENTOK LIVE IS IN FLIGHT",
            color = TextWhite,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Our high-speed peer-to-peer decentralized video broadcasting system is currently under heavy testing.",
            color = TextGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (signupState) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyanPrimary.copy(alpha = 0.15f))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = CyanAccent)
                    Text("Awesome! You are on the priority beta list.", color = TextWhite, fontSize = 12.sp)
                }
            }
        } else {
            Button(
                onClick = { signupState = true },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, CyanAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reserve My Creator Access Key", color = CyanAccent, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Back to home",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { viewModel.setScreen(Screen.HOME) }
                .padding(8.dp)
        )
    }
}

// 6. MESSAGES & NOTIFICATIONS SCREEN (SPLIT TAB/HOUSED CONVENIENTLY)
@Composable
fun MessagesScreen(viewModel: DeenTokViewModel) {
    val messages by viewModel.allMessages.collectAsState()
    var currentSubTab by remember { mutableStateOf(0) } // 0 = Chats, 1 = Notifications
    
    val notifications by viewModel.allNotifications.collectAsState()
    val unreadNotifCount = notifications.count { !it.isRead }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
            .padding(top = 16.dp)
    ) {
        // Toggle header sub-tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "Inbox",
                    color = if (currentSubTab == 0) TextWhite else TextGray,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    modifier = Modifier.clickable { currentSubTab = 0 }
                )
                
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Alerts",
                        color = if (currentSubTab == 1) TextWhite else TextGray,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        modifier = Modifier.clickable { currentSubTab = 1 }
                    )
                    if (unreadNotifCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 10.dp, y = (-4).dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LikeRed)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        if (currentSubTab == 0) {
            // Live Chat with @marcus_dev
            var chatInput by remember { mutableStateOf("") }
            
            Column(modifier = Modifier.weight(1f)) {
                // Chats History (zinc rounded bubble, clean contrast)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DECENTRALIZED ENCRYPTED CHANNEL",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    items(messages) { msg ->
                        val isMe = msg.senderId == "current_user"
                        ChatBubble(msg = msg, isMe = isMe)
                    }
                }

                // Rich messaging Text Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(12.dp)
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        placeholder = { Text("Send message to @marcus_dev...", color = TextMuted, fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Black,
                            unfocusedContainerColor = Black,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = SurfaceVariantDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    
                    IconButton(
                        onClick = {
                            viewModel.sendDirectMessage(chatInput)
                            chatInput = ""
                        },
                        modifier = Modifier
                            .testTag("send_msg_button")
                            .clip(CircleShape)
                            .background(CyanAccent)
                            .size(44.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Black)
                    }
                }
            }
        } else {
            // Notifications List section
            NotificationsSection(viewModel = viewModel)
        }
    }
}

@Composable
fun ChatBubble(msg: Message, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Text("MD", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 2.dp,
                        bottomEnd = if (isMe) 2.dp else 16.dp
                    )
                )
                .background(
                    if (isMe) Brush.linearGradient(listOf(CyanAccent, BlueAccent))
                    else Brush.linearGradient(listOf(SurfaceDark, SurfaceVariantDark))
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 260.dp)
        ) {
            Text(
                text = msg.text,
                color = if (isMe) Black else TextWhite,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// 7. NOTIFICATIONS LIST COMPONENT
@Composable
fun NotificationsSection(viewModel: DeenTokViewModel) {
    val notifications by viewModel.allNotifications.collectAsState()
    
    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No alerts yet!", color = TextGray)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notif ->
                NotificationRow(notif = notif) {
                    viewModel.markNotificationRead(notif.id)
                }
            }
        }
    }
}

@Composable
fun NotificationRow(notif: Notification, onClick: () -> Unit) {
    val icon = when (notif.type) {
        "LIKE" -> Icons.Default.Favorite
        "COMMENT" -> Icons.Default.ChatBubble
        "FOLLOW" -> Icons.Default.PersonAdd
        else -> Icons.Default.Info
    }
    val iconColor = when (notif.type) {
        "LIKE" -> LikeRed
        "COMMENT" -> CyanAccent
        "FOLLOW" -> BlueAccent
        else -> TextGray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (notif.isRead) SurfaceDark.copy(alpha = 0.5f) else SurfaceDark)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = notif.type, tint = iconColor, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notif.title,
                    color = if (notif.isRead) TextGray else TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (!notif.isRead) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CyanAccent)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${notif.sourceUsername} ${notif.description}",
                color = TextGray,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 8. PROFILE SCREEN
@Composable
fun ProfileScreen(viewModel: DeenTokViewModel) {
    val allVideos by viewModel.allVideos.collectAsState()
    
    // User profile statistics calculations
    val myVideos = remember(allVideos) { allVideos.filter { it.userId == "current_user" } }
    val likedVideos = remember(allVideos) { allVideos.filter { it.isLiked } }
    val totalLikes = remember(myVideos) { myVideos.sumOf { it.likesCount } }
    
    var activeProfileTab by remember { mutableStateOf(0) } // 0 = My Videos, 1 = Liked, 2 = Saved

    var showFollowingDialog by remember { mutableStateOf(false) }
    var showFollowersDialog by remember { mutableStateOf(false) }
    var showLikesDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showProfilePicDialog by remember { mutableStateOf(false) }

    if (showProfilePicDialog) {
        ProfilePicChangeDialog(
            currentAvatar = viewModel.currentUserAvatar,
            onDismiss = { showProfilePicDialog = false },
            onAvatarSelected = { newAvatar ->
                viewModel.updateCreatorProfile(
                    username = viewModel.currentUsername,
                    avatar = newAvatar,
                    bio = viewModel.currentUserBio,
                    website = viewModel.currentUserWebsite,
                    twitter = viewModel.currentUserTwitter,
                    instagram = viewModel.currentUserInstagram
                )
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(rememberScrollState())
    ) {
        // High fidelity visual cover header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(DarkGrey, SurfaceDark, DarkAccent)
                    )
                )
        ) {
            IconButton(
                onClick = { viewModel.setScreen(Screen.SETTINGS) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextWhite)
            }
        }

        // Centered Avatar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-50).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .align(Alignment.Center)
                        .shadow(10.dp, CircleShape)
                        .clip(CircleShape)
                        .border(3.dp, Black, CircleShape)
                        .background(SurfaceDark)
                        .clickable { showProfilePicDialog = true }
                        .testTag("profile_avatar_clickable"),
                    contentAlignment = Alignment.Center
                ) {
                    val avatar = viewModel.currentUserAvatar
                    val isImage = avatar.startsWith("http") || 
                                  avatar.startsWith("content:") || 
                                  avatar.startsWith("file:") || 
                                  avatar.startsWith("android.resource:")
                    if (isImage) {
                        coil.compose.AsyncImage(
                            model = avatar,
                            contentDescription = "Profile Picture",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = avatar.take(2).uppercase(),
                            color = CyanAccent,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Black.copy(alpha = 0.15f))
                    )
                }

                // The requested "+" button to insert/change profile picture!
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CyanAccent)
                        .border(2.dp, Black, CircleShape)
                        .clickable { showProfilePicDialog = true }
                        .testTag("profile_add_picture_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Profile Picture",
                        tint = Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Username
            Text(
                text = viewModel.currentUsername,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // Bio
            Text(
                text = viewModel.currentUserBio,
                color = TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // User stats numbers
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileStatColumn(count = viewModel.currentUserFollowingCount, label = "Following") {
                    showFollowingDialog = true
                }
                ProfileStatColumn(count = viewModel.currentUserFollowersCount, label = "Followers") {
                    showFollowersDialog = true
                }
                ProfileStatColumn(count = formatCount(totalLikes), label = "Likes") {
                    showLikesDialog = true
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Shortcut Badges for Special Roles (Admins & Creator Insights!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.setScreen(Screen.CREATOR_STUDIO) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = "Stats", tint = CyanAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Creator Studio", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.setScreen(Screen.ADMIN_DASHBOARD) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = "Admin", tint = LikeRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Admin Dashboard", color = LikeRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(0.5.dp, SurfaceVariantDark)),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileTabItem(icon = Icons.Default.GridView, label = "My Voices", selected = activeProfileTab == 0) {
                    activeProfileTab = 0
                }
                ProfileTabItem(icon = Icons.Default.Favorite, label = "Likes", selected = activeProfileTab == 1) {
                    activeProfileTab = 1
                }
                ProfileTabItem(icon = Icons.Default.BookmarkBorder, label = "Saved", selected = activeProfileTab == 2) {
                    activeProfileTab = 2
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Profile grid contents
            val displayedVideos = when (activeProfileTab) {
                0 -> myVideos
                1 -> likedVideos
                else -> emptyList()
            }

            if (displayedVideos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No videos in this section yet.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayedVideos.forEach { video ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.7f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDark)
                                .border(0.5.dp, SurfaceVariantDark, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setScreen(Screen.HOME) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = video.title,
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (displayedVideos.size < 3) {
                        repeat(3 - displayedVideos.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // --- INTERACTIVE DIALOGS ---

    // 1. FOLLOWING DIALOG
    if (showFollowingDialog) {
        val followedCreators = remember(allVideos) {
            allVideos.filter { it.isFollowingCreator }.map { it.username to it.userAvatarUrl }.distinctBy { it.first }
        }
        
        AlertDialog(
            onDismissRequest = { showFollowingDialog = false },
            title = {
                Text("Following", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (followedCreators.isEmpty()) {
                        Text("You aren't following any creators yet.", color = TextGray, fontSize = 13.sp)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(followedCreators) { (username, avatar) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Black, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(CyanAccent.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(avatar, color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Text(username, color = TextWhite, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    }
                                    
                                    Button(
                                        onClick = {
                                            val firstVideo = allVideos.firstOrNull { it.username == username }
                                            if (firstVideo != null) {
                                                viewModel.toggleFollow(firstVideo)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                        border = BorderStroke(1.dp, CyanAccent),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text("Unfollow", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFollowingDialog = false }) {
                    Text("Close", color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceDark,
            modifier = Modifier.testTag("following_dialog")
        )
    }

    // 2. FOLLOWERS DIALOG
    if (showFollowersDialog) {
        AlertDialog(
            onDismissRequest = { showFollowersDialog = false },
            title = {
                Text("Followers Simulator", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Test follower notifications and growth! Toggle community members to follow or unfollow your account.", color = TextGray, fontSize = 12.sp)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val possibleFollowers = listOf(
                        "@marcus_dev" to "MD",
                        "@alice_codes" to "AC",
                        "@casual_scroll" to "CS",
                        "@unboxing_pro" to "UP",
                        "@synth_lover" to "SL"
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(possibleFollowers) { (username, avatar) ->
                            val isFollowingUs = viewModel.currentFollowersList.contains(username)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Black, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(BlueAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(avatar, color = BlueAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Text(username, color = TextWhite, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                }
                                
                                Button(
                                    onClick = { viewModel.toggleSimulatedFollower(username) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowingUs) SurfaceDark else CyanAccent
                                    ),
                                    border = if (isFollowingUs) BorderStroke(1.dp, CyanAccent) else null,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = if (isFollowingUs) "Following You" else "Follow",
                                        color = if (isFollowingUs) CyanAccent else Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFollowersDialog = false }) {
                    Text("Done", color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceDark,
            modifier = Modifier.testTag("followers_dialog")
        )
    }

    // 3. LIKES DIALOG
    if (showLikesDialog) {
        AlertDialog(
            onDismissRequest = { showLikesDialog = false },
            title = {
                Text("Likes Analysis", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Breakdown of likes received across your uploaded voices:", color = TextGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (myVideos.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = LikeRed.copy(alpha = 0.4f), modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No uploaded voices yet.", color = TextGray, fontSize = 13.sp)
                            Text("Upload a voice to start receiving likes!", color = TextMuted, fontSize = 11.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(myVideos) { video ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Black, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(video.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(video.category, color = TextGray, fontSize = 11.sp)
                                    }
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Favorite, contentDescription = "Likes", tint = LikeRed, modifier = Modifier.size(16.dp))
                                        Text("${video.likesCount}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLikesDialog = false }) {
                    Text("Close", color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceDark,
            modifier = Modifier.testTag("likes_dialog")
        )
    }

    // 4. EDIT PROFILE DIALOG
    if (showEditProfileDialog) {
        var editUsername by remember { mutableStateOf(viewModel.currentUsername) }
        var editAvatar by remember { mutableStateOf(viewModel.currentUserAvatar) }
        var editBio by remember { mutableStateOf(viewModel.currentUserBio) }
        var editWebsite by remember { mutableStateOf(viewModel.currentUserWebsite) }
        var editTwitter by remember { mutableStateOf(viewModel.currentUserTwitter) }
        var editInstagram by remember { mutableStateOf(viewModel.currentUserInstagram) }
        
        var showDialogAvatarPicker by remember { mutableStateOf(false) }

        if (showDialogAvatarPicker) {
            ProfilePicChangeDialog(
                currentAvatar = editAvatar,
                onDismiss = { showDialogAvatarPicker = false },
                onAvatarSelected = { newAvatar ->
                    editAvatar = newAvatar
                }
            )
        }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text("Edit System Admin Profile", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editAvatar,
                        onValueChange = { editAvatar = it },
                        label = { Text("Avatar Initials or Image URL", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedLabelColor = CyanAccent
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showDialogAvatarPicker = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Pick Photo", tint = CyanAccent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("edit_avatar_input")
                    )

                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedLabelColor = CyanAccent
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_username_input")
                    )

                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedLabelColor = CyanAccent
                        ),
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("edit_bio_input")
                    )

                    OutlinedTextField(
                        value = editWebsite,
                        onValueChange = { editWebsite = it },
                        label = { Text("Website", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedLabelColor = CyanAccent
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_website_input")
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text("Cancel", color = TextGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.updateCreatorProfile(
                                username = editUsername,
                                avatar = editAvatar,
                                bio = editBio,
                                website = editWebsite,
                                twitter = editTwitter,
                                instagram = editInstagram
                            )
                            showEditProfileDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Text("Save", color = Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            containerColor = SurfaceDark,
            modifier = Modifier.testTag("edit_profile_dialog")
        )
    }
}

@Composable
fun ProfileStatColumn(count: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("profile_stat_${label.lowercase()}")
    ) {
        Text(text = count, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = TextGray, fontSize = 11.sp)
    }
}

@Composable
fun ProfileTabItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) CyanAccent else TextGray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (selected) CyanAccent else TextGray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProfilePicChangeDialog(
    currentAvatar: String,
    onDismiss: () -> Unit,
    onAvatarSelected: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var urlInput by remember { mutableStateOf("") }
    var initialsInput by remember { mutableStateOf(if (currentAvatar.length <= 2) currentAvatar else "ME") }
    
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = java.io.File(context.filesDir, "profile_avatar.jpg")
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                val localPathUri = android.net.Uri.fromFile(file).toString()
                onAvatarSelected(localPathUri)
                onDismiss()
            } catch (e: Exception) {
                e.printStackTrace()
                onAvatarSelected(uri.toString())
                onDismiss()
            }
        }
    }

    val presetAvatars = listOf(
        "Aesthetic Synth" to "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=150&q=80",
        "Cyber Hacker" to "https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&w=150&q=80",
        "Abstract Art" to "https://images.unsplash.com/photo-1541701494587-cb58502866ab?auto=format&fit=crop&w=150&q=80",
        "Neon Sphere" to "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?auto=format&fit=crop&w=150&q=80",
        "Matrix Code" to "https://images.unsplash.com/photo-1607799279861-4dd421887fb3?auto=format&fit=crop&w=150&q=80"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Update Profile Picture", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Select a source to insert or change your profile picture stream:",
                    color = TextGray,
                    fontSize = 12.sp
                )

                // Option 1: Native Gallery picker
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_gallery_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Gallery", tint = Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Photo from Gallery", color = Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Option 2: Pre-defined Premium Presets
                Text("Preset High-Fidelity Avatars:", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAvatars.forEach { (name, url) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onAvatarSelected(url)
                                    onDismiss()
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, CyanAccent, CircleShape)
                            ) {
                                coil.compose.AsyncImage(
                                    model = url,
                                    contentDescription = name,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                name.split(" ").last(),
                                color = TextGray,
                                fontSize = 8.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Option 3: Web Image URL Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Or Paste Image Web URL:", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = { Text("https://example.com/pic.jpg", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_url_input"),
                        textStyle = TextStyle(color = TextWhite, fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        singleLine = true
                    )
                    if (urlInput.isNotBlank()) {
                        Button(
                            onClick = {
                                onAvatarSelected(urlInput.trim())
                                onDismiss()
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Apply URL", color = Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Option 4: Use Initials
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Or Use Profile Initials (2 Letters):", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = initialsInput,
                            onValueChange = { if (it.length <= 2) initialsInput = it.uppercase() },
                            modifier = Modifier.width(80.dp).testTag("profile_initials_input"),
                            textStyle = TextStyle(color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (initialsInput.isNotBlank()) {
                                    onAvatarSelected(initialsInput)
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Apply Initials", color = Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextGray)
            }
        },
        containerColor = SurfaceDark
    )
}

// 9. SETTINGS SCREEN

enum class SettingsSection {
    MAIN, ACCOUNT, PRIVACY, SECURITY, NOTIFICATIONS, LANGUAGE, APPEARANCE, CONTENT, ACCESSIBILITY, SUPPORT, ABOUT
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(viewModel: DeenTokViewModel) {
    var currentSection by remember { mutableStateOf(SettingsSection.MAIN) }
    val preferences by viewModel.userPreferences.collectAsState()
    val currentPrefs = preferences ?: UserPreferences(userId = "current_user")
    val context = LocalContext.current
    val trans = getTranslations(viewModel)

    // Account settings draft states
    var draftUsername by remember { mutableStateOf("") }
    var draftEmail by remember { mutableStateOf("") }
    var draftPhone by remember { mutableStateOf("") }
    var draftAvatarUrl by remember { mutableStateOf("") }
    var draftBio by remember { mutableStateOf("") }
    var draftWebsite by remember { mutableStateOf("") }

    // Security states
    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var showTwoFactorDialog by remember { mutableStateOf(false) }

    // Support - Report a problem state
    var reportCategory by remember { mutableStateOf("SPAM") }
    var reportTitle by remember { mutableStateOf("") }
    var reportDesc by remember { mutableStateOf("") }

    // Accordion states for Support Center questions
    var expandedQuestionIndex by remember { mutableStateOf<Int?>(null) }

    // Destructive Account Delete State
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // Sync account draft states
    LaunchedEffect(currentSection, preferences, viewModel.currentUsername) {
        if (currentSection == SettingsSection.ACCOUNT) {
            draftUsername = viewModel.currentUsername
            draftEmail = currentPrefs.email
            draftPhone = currentPrefs.phoneNumber
            draftAvatarUrl = viewModel.currentUserAvatar
            draftBio = viewModel.currentUserBio
            draftWebsite = viewModel.currentUserWebsite
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        // TOP APP BAR WITH DYNAMIC TITLE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (currentSection == SettingsSection.MAIN) {
                        viewModel.setScreen(Screen.PROFILE)
                    } else {
                        currentSection = SettingsSection.MAIN
                    }
                },
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            
            Text(
                text = when (currentSection) {
                    SettingsSection.MAIN -> trans.profileSettings.uppercase()
                    SettingsSection.ACCOUNT -> trans.settingsAccount.uppercase()
                    SettingsSection.PRIVACY -> trans.settingsPrivacy.uppercase()
                    SettingsSection.SECURITY -> "SECURITY & SIGN-IN"
                    SettingsSection.NOTIFICATIONS -> trans.settingsNotifications.uppercase()
                    SettingsSection.LANGUAGE -> trans.settingsLanguage.uppercase()
                    SettingsSection.APPEARANCE -> "APPEARANCE STYLE"
                    SettingsSection.CONTENT -> trans.settingsContent.uppercase()
                    SettingsSection.ACCESSIBILITY -> "ACCESSIBILITY FEATURES"
                    SettingsSection.SUPPORT -> trans.settingsHelp.uppercase()
                    SettingsSection.ABOUT -> trans.settingsAbout.uppercase()
                },
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider(color = SurfaceDark)

        // SCREEN INNER CONTENT
        Box(modifier = Modifier.weight(1f)) {
            when (currentSection) {
                SettingsSection.MAIN -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        item {
                            Text(
                                text = trans.settingsTitle.uppercase(),
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        item {
                            SettingsMenuRow(
                                title = trans.settingsAccount,
                                desc = "View & change bio, username, social links",
                                icon = Icons.Default.Person,
                                testTag = "settings_section_account",
                                onClick = { currentSection = SettingsSection.ACCOUNT }
                            )
                            SettingsMenuRow(
                                title = trans.settingsPrivacy,
                                desc = "Private account, who can follow, comment, download",
                                icon = Icons.Default.Visibility,
                                testTag = "settings_section_privacy",
                                onClick = { currentSection = SettingsSection.PRIVACY }
                            )
                            SettingsMenuRow(
                                title = "Security & Sign-in",
                                desc = "Change password, email verification, 2FA status",
                                icon = Icons.Default.Lock,
                                testTag = "settings_section_security",
                                onClick = { currentSection = SettingsSection.SECURITY }
                            )
                            SettingsMenuRow(
                                title = trans.settingsNotifications,
                                desc = "Likes, comments, follower and creator push updates",
                                icon = Icons.Default.Notifications,
                                testTag = "settings_section_notifications",
                                onClick = { currentSection = SettingsSection.NOTIFICATIONS }
                            )
                            SettingsMenuRow(
                                title = trans.settingsLanguage,
                                desc = "Set app translation to English, Oromo, Amharic",
                                icon = Icons.Default.Language,
                                testTag = "settings_section_language",
                                onClick = { currentSection = SettingsSection.LANGUAGE }
                            )
                            SettingsMenuRow(
                                title = "Appearance Style",
                                desc = "Dark, Light, System default configurations",
                                icon = Icons.Default.Palette,
                                testTag = "settings_section_appearance",
                                onClick = { currentSection = SettingsSection.APPEARANCE }
                            )
                            SettingsMenuRow(
                                title = trans.settingsContent,
                                desc = "Video interest filters, sensitive content, autoplay",
                                icon = Icons.Default.Tune,
                                testTag = "settings_section_content",
                                onClick = { currentSection = SettingsSection.CONTENT }
                            )
                            SettingsMenuRow(
                                title = "Accessibility Features",
                                desc = "Text size adjustments, reduced motion, captions",
                                icon = Icons.Default.Accessibility,
                                testTag = "settings_section_accessibility",
                                onClick = { currentSection = SettingsSection.ACCESSIBILITY }
                            )
                            SettingsMenuRow(
                                title = trans.settingsHelp,
                                desc = "Help Center FAQs, contact support, report problems",
                                icon = Icons.Default.Help,
                                testTag = "settings_section_support",
                                onClick = { currentSection = SettingsSection.SUPPORT }
                            )
                            SettingsMenuRow(
                                title = trans.settingsAbout,
                                desc = "App version, license information, follow social links",
                                icon = Icons.Default.Info,
                                testTag = "settings_section_about",
                                onClick = { currentSection = SettingsSection.ABOUT }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    viewModel.logOut()
                                    viewModel.setScreen(Screen.HOME)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("settings_logout_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = "Exit", tint = LikeRed)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(trans.settingsLogOut.uppercase(), color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                SettingsSection.ACCOUNT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "EDIT PROFILE DATABASE VALUES",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        SettingsTextField(
                            value = draftUsername,
                            onValueChange = { draftUsername = it },
                            label = "Username ID (e.g. @me)",
                            testTag = "settings_username_input"
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsTextField(
                            value = draftEmail,
                            onValueChange = { draftEmail = it },
                            label = "Primary Registered Email",
                            testTag = "settings_email_input"
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsTextField(
                            value = draftPhone,
                            onValueChange = { draftPhone = it },
                            label = "Phone Number (Ethiopia)",
                            testTag = "settings_phone_input"
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsTextField(
                            value = draftAvatarUrl,
                            onValueChange = { draftAvatarUrl = it },
                            label = "Profile Photo Code (e.g. ME, MD, CS)",
                            testTag = "settings_avatar_input"
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsTextField(
                            value = draftBio,
                            onValueChange = { draftBio = it },
                            label = "Creator Bio Text Description",
                            testTag = "settings_bio_input"
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsTextField(
                            value = draftWebsite,
                            onValueChange = { draftWebsite = it },
                            label = "Primary Web Link URL",
                            testTag = "settings_website_input"
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.updateAccountSettings(
                                    username = draftUsername,
                                    email = draftEmail,
                                    phone = draftPhone,
                                    photo = draftAvatarUrl,
                                    bio = draftBio,
                                    website = draftWebsite
                                )
                                android.widget.Toast.makeText(context, "Account preferences saved directly to Room DB!", android.widget.Toast.LENGTH_SHORT).show()
                                currentSection = SettingsSection.MAIN
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_save_account_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("SAVE ACCOUNT PREFERENCES", color = Black, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = SurfaceDark)
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "DANGER ZONE",
                            color = LikeRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Deleting your account is permanent. It will instantly remove all your credentials, voice clips, and metadata transactions from the Local Room Database.",
                            color = TextGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Button(
                            onClick = { showDeleteAccountDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_delete_account_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = LikeRed)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PERMANENTLY DELETE ACCOUNT", color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                SettingsSection.PRIVACY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "PRIVACY ENGINE SETTINGS",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        SettingsToggleRow(
                            title = "Private Account Mode",
                            desc = "Only approved followers can view your feed and voice metrics.",
                            checked = currentPrefs.isPrivateAccount,
                            testTag = "settings_privacy_private_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(isPrivateAccount = it))
                            }
                        )

                        SettingsToggleRow(
                            title = "Allow Downloads",
                            desc = "Permit offline voice clipping exports from your catalog feed.",
                            checked = currentPrefs.allowDownloads,
                            testTag = "settings_privacy_downloads_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(allowDownloads = it))
                            }
                        )

                        SettingsToggleRow(
                            title = "Show Activity Status",
                            desc = "Let followers know when you are actively auditing live audio.",
                            checked = currentPrefs.showActivityStatus,
                            testTag = "settings_privacy_activity_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(showActivityStatus = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = SurfaceDark)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "INTERACTION SETTINGS",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Selector for who can follow
                        SettingsDropdownSelector(
                            title = "Who Can Follow Me",
                            desc = "Limit follow authorization to maintain clean audiences.",
                            options = listOf("Everyone", "Approved only"),
                            selected = currentPrefs.whoCanFollow,
                            onSelect = {
                                viewModel.saveUserPreferences(currentPrefs.copy(whoCanFollow = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Selector for who can comment
                        SettingsDropdownSelector(
                            title = "Who Can Comment",
                            desc = "Restrict audio discussions to authorized users.",
                            options = listOf("Everyone", "Followers only", "Nobody"),
                            selected = currentPrefs.whoCanComment,
                            onSelect = {
                                viewModel.saveUserPreferences(currentPrefs.copy(whoCanComment = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Selector for who can send messages
                        SettingsDropdownSelector(
                            title = "Who Can Message Me",
                            desc = "Direct Message limits for secure messaging feed.",
                            options = listOf("Everyone", "Followers only", "Nobody"),
                            selected = currentPrefs.whoCanSendMessages,
                            onSelect = {
                                viewModel.saveUserPreferences(currentPrefs.copy(whoCanSendMessages = it))
                            }
                        )
                    }
                }

                SettingsSection.SECURITY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "SECURITY CREDENTIALS",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "Verification Status",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (currentPrefs.emailVerified) Icons.Default.Verified else Icons.Default.Info,
                                contentDescription = "Verified Badge",
                                tint = if (currentPrefs.emailVerified) CyanAccent else LikeRed
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentPrefs.emailVerified) "Email Verified & Active" else "Pending Verification",
                                color = if (currentPrefs.emailVerified) CyanAccent else LikeRed,
                                fontSize = 12.sp
                            )
                        }
                        
                        if (!currentPrefs.emailVerified) {
                            Button(
                                onClick = {
                                    viewModel.saveUserPreferences(currentPrefs.copy(emailVerified = true))
                                    android.widget.Toast.makeText(context, "Verification code dispatched to ${currentPrefs.email}!", android.widget.Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                            ) {
                                Text("REQUEST VERIFICATION CODE", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = SurfaceDark)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "CHANGE PASSWORD SECURELY",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        SettingsTextField(
                            value = currentPasswordInput,
                            onValueChange = { currentPasswordInput = it },
                            label = "Current Account Password",
                            testTag = "security_current_pw_input"
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        SettingsTextField(
                            value = newPasswordInput,
                            onValueChange = { newPasswordInput = it },
                            label = "New Password (min 8 chars)",
                            testTag = "security_new_pw_input"
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        SettingsTextField(
                            value = confirmPasswordInput,
                            onValueChange = { confirmPasswordInput = it },
                            label = "Confirm New Password",
                            testTag = "security_confirm_pw_input"
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    if (newPasswordInput.length < 8) {
                                        android.widget.Toast.makeText(context, "Password must be at least 8 characters!", android.widget.Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (newPasswordInput != confirmPasswordInput) {
                                        android.widget.Toast.makeText(context, "Passwords do not match!", android.widget.Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.saveUserPreferences(currentPrefs.copy(passwordHash = newPasswordInput))
                                    android.widget.Toast.makeText(context, "Password updated successfully in DB!", android.widget.Toast.LENGTH_SHORT).show()
                                    currentPasswordInput = ""
                                    newPasswordInput = ""
                                    confirmPasswordInput = ""
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("security_update_pw_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                            ) {
                                Text("UPDATE PASSWORD", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = {
                                    android.widget.Toast.makeText(context, "Recovery code dispatched to ${currentPrefs.email}!", android.widget.Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                            ) {
                                Text("FORGOT PASSWORD", color = TextGray, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = SurfaceDark)
                        Spacer(modifier = Modifier.height(16.dp))

                        SettingsToggleRow(
                            title = "Two-Factor Authentication (2FA)",
                            desc = "Secure login using email TOTP verification challenge.",
                            checked = currentPrefs.twoFactorEnabled,
                            testTag = "security_2fa_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(twoFactorEnabled = it))
                                if (it) {
                                    showTwoFactorDialog = true
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ACTIVE AUDIT SESSIONS",
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Current Active Device", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Android Emulator API 34 • Addis Ababa, Ethiopia", color = TextWhite, fontSize = 12.sp)
                                Text("Active now • Secure session authenticated", color = TextGray, fontSize = 11.sp)
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Linked Web Instance", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Chrome OS • Nairobi, Kenya", color = TextWhite, fontSize = 12.sp)
                                Text("Linked 2 hours ago", color = TextGray, fontSize = 11.sp)
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        android.widget.Toast.makeText(context, "Flushed all active remote sessions!", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark)
                                ) {
                                    Text("TERMINATE ALL OTHER SESSIONS", color = LikeRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                SettingsSection.NOTIFICATIONS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "NOTIFICATIONS DELIVERY CHANNELS",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        SettingsToggleRow(
                            title = "Push Notifications Active",
                            desc = "Activate device system notifications banner alerts.",
                            checked = currentPrefs.pushNotifications,
                            testTag = "settings_notif_push_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(pushNotifications = it))
                            }
                        )

                        SettingsToggleRow(
                            title = "Email Notifications Active",
                            desc = "Get transaction digest reports straight to your inbox.",
                            checked = currentPrefs.emailNotifications,
                            testTag = "settings_notif_email_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(emailNotifications = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = SurfaceDark)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "ACTIVITY PREFERENCES",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        SettingsToggleRow(
                            title = "Like Interactions",
                            desc = "Notify me when someone likes my voice clips.",
                            checked = currentPrefs.likeNotifications,
                            testTag = "settings_notif_like_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(likeNotifications = it))
                            }
                        )

                        SettingsToggleRow(
                            title = "Comment Threads",
                            desc = "Notify me when someone comments on my audio posts.",
                            checked = currentPrefs.commentNotifications,
                            testTag = "settings_notif_comment_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(commentNotifications = it))
                            }
                        )

                        SettingsToggleRow(
                            title = "New Followers Alert",
                            desc = "Notify me when a creator or user starts following me.",
                            checked = currentPrefs.followerNotifications,
                            testTag = "settings_notif_follow_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(followerNotifications = it))
                            }
                        )

                        SettingsToggleRow(
                            title = "Direct Messaging Updates",
                            desc = "Instantly trigger notifications for new private chat threads.",
                            checked = currentPrefs.messageNotifications,
                            testTag = "settings_notif_msg_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(messageNotifications = it))
                            }
                        )

                        SettingsToggleRow(
                            title = "Audience Mention Alerts",
                            desc = "Notify me when my handle is tagged in description bios.",
                            checked = currentPrefs.mentionNotifications,
                            testTag = "settings_notif_mention_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(mentionNotifications = it))
                            }
                        )

                        SettingsToggleRow(
                            title = "Platform Creator News",
                            desc = "Opt-in to updates about live audio channels and system changes.",
                            checked = currentPrefs.creatorUpdates,
                            testTag = "settings_notif_creator_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(creatorUpdates = it))
                            }
                        )
                    }
                }

                SettingsSection.LANGUAGE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "APP TRANSLATION ENGINE",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "Select your preferred communication language. This will localized the main feeds, system guidelines, and menus.",
                            color = TextGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        val languages = listOf("English", "Afaan Oromoo", "Amharic")
                        languages.forEach { lang ->
                            val isSelected = currentPrefs.language == lang
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        viewModel.saveUserPreferences(currentPrefs.copy(language = lang))
                                        viewModel.setLanguage(lang)
                                        android.widget.Toast.makeText(context, "App language changed to: $lang", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) SurfaceVariantDark else SurfaceDark
                                ),
                                border = if (isSelected) BorderStroke(1.dp, CyanAccent) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = lang,
                                            color = if (isSelected) CyanAccent else TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = when (lang) {
                                                "English" -> "Standard International Layout"
                                                "Afaan Oromoo" -> "Hawaasa Oromoo Itiyoophiyaa"
                                                "Amharic" -> "የኢትዮጵያ ፌዴራላዊ የሥራ ቋንቋ"
                                                else -> ""
                                            },
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = CyanAccent)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("ACTIVE LOCALIZATION PREVIEW", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (currentPrefs.language) {
                                        "English" -> "Welcome to Deen Tok. Share reminders and learn about Islam."
                                        "Afaan Oromoo" -> "Baga nagaan gara Deen Tok dhuftan. Yaadachiisa deenii fi barnoota argadhaa."
                                        "Amharic" -> "ወደ ዴንቶክ እንኳን ደህና መጡ። መልካም ትምህርቶችንና ማስታወሻዎችን ያጋሩ።"
                                        else -> ""
                                    },
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }

                SettingsSection.APPEARANCE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "VISUAL IDENTITY STYLE",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        val themes = listOf("Light", "Dark", "System Default")
                        themes.forEach { theme ->
                            val isSelected = currentPrefs.themeMode == theme
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        viewModel.saveUserPreferences(currentPrefs.copy(themeMode = theme))
                                        android.widget.Toast.makeText(context, "Theme set to: $theme Mode!", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) SurfaceVariantDark else SurfaceDark
                                ),
                                border = if (isSelected) BorderStroke(1.dp, CyanAccent) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = theme + " Mode",
                                            color = if (isSelected) CyanAccent else TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = when (theme) {
                                                "Light" -> "Clean high contrast light workspace"
                                                "Dark" -> "Elegant dark canvas built for eye safety"
                                                "System Default" -> "Match with device OS settings dynamically"
                                                else -> ""
                                            },
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = CyanAccent)
                                    }
                                }
                            }
                        }
                    }
                }

                SettingsSection.CONTENT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "CONTENT PREFERENCES FILTER",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "Manage Interests & Feed Recommendations",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Select tags to shape the recommendation engine algorithm.",
                            color = TextGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val allInterests = listOf("Tech", "Music", "Cooking", "Art", "Vlog", "Comedy")
                        val currentInterests = remember(currentPrefs.videoInterests) {
                            currentPrefs.videoInterests.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            maxItemsInEachRow = 3
                        ) {
                            allInterests.forEach { interest ->
                                val isSelected = currentInterests.contains(interest)
                                Card(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clickable {
                                            val newInterests = if (isSelected) {
                                                currentInterests.filter { it != interest }
                                            } else {
                                                currentInterests + interest
                                            }
                                            viewModel.saveUserPreferences(
                                                currentPrefs.copy(videoInterests = newInterests.joinToString(", "))
                                            )
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) CyanPrimary else SurfaceDark
                                    )
                                ) {
                                    Text(
                                        text = interest,
                                        color = if (isSelected) Black else TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = SurfaceDark)
                        Spacer(modifier = Modifier.height(16.dp))

                        SettingsToggleRow(
                            title = "Sensitive Content Filter",
                            desc = "Exclude clips flagged by administrative auditors for graphic audio, noise outbursts, or strong warnings.",
                            checked = currentPrefs.sensitiveContentFilter,
                            testTag = "settings_content_sensitive_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(sensitiveContentFilter = it))
                            }
                        )

                        SettingsToggleRow(
                            title = "Data Saver Mode",
                            desc = "Lowers video bitrate and scales down background cache threads to save cellular data packets.",
                            checked = currentPrefs.dataSavingMode,
                            testTag = "settings_content_data_saver_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(dataSavingMode = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsDropdownSelector(
                            title = "Autoplay Videos & Clis",
                            desc = "Trigger audio playbacks automatically as you scroll.",
                            options = listOf("On", "Off"),
                            selected = currentPrefs.autoplayVideos,
                            onSelect = {
                                viewModel.saveUserPreferences(currentPrefs.copy(autoplayVideos = it))
                            }
                        )
                    }
                }

                SettingsSection.ACCESSIBILITY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "ACCESSIBILITY & COMFORT",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        SettingsDropdownSelector(
                            title = "System Text Size",
                            desc = "Scale labels, headings, and video description texts.",
                            options = listOf("Small", "Medium", "Large"),
                            selected = currentPrefs.textSize,
                            onSelect = {
                                viewModel.saveUserPreferences(currentPrefs.copy(textSize = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SettingsToggleRow(
                            title = "Generate Audio Captions",
                            desc = "Auto-transcribe live dialogue overlays when sound clips are playing.",
                            checked = currentPrefs.captionsEnabled,
                            testTag = "settings_accessibility_captions_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(captionsEnabled = it))
                            }
                        )

                        SettingsToggleRow(
                            title = "Reduced Animations",
                            desc = "Freeze background physics simulations and decorative fade transitions.",
                            checked = currentPrefs.reducedAnimations,
                            testTag = "settings_accessibility_reduced_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(reducedAnimations = it))
                            }
                        )

                        SettingsToggleRow(
                            title = "High Contrast Mode",
                            desc = "Forces standard visual elements to maximum contrast. (Future Ready)",
                            checked = currentPrefs.highContrastMode,
                            testTag = "settings_accessibility_contrast_switch",
                            onCheckedChange = {
                                viewModel.saveUserPreferences(currentPrefs.copy(highContrastMode = it))
                            }
                        )
                    }
                }

                SettingsSection.SUPPORT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "HELP CENTER & FREQUENTLY ASKED QUESTIONS",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val faqs = listOf(
                            "How do I post a live voice clip?" to "To post a voice clip, navigate to the UPLOAD screen from the sidebar, tap on Record/Upload, select your audio details/category, and commit to the database feed.",
                            "Is my conversation history secure?" to "Yes, Deen Tok utilizes localized client-side SQLite/Room database encryption strategies. Direct messages are transactionally separated.",
                            "What is the decentralization roadmap?" to "We are migrating to a secure node architecture. Users will hold secure keys to audit logs and verify content categories safely."
                        )

                        faqs.forEachIndexed { idx, pair ->
                            val isExpanded = expandedQuestionIndex == idx
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { expandedQuestionIndex = if (isExpanded) null else idx },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = pair.first,
                                            color = if (isExpanded) CyanAccent else TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Toggle Accordion",
                                            tint = TextGray
                                        )
                                    }
                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = pair.second, color = TextGray, fontSize = 12.sp, lineHeight = 18.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = SurfaceDark)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "REPORT A TRANSACTION/PROBLEM",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        SettingsDropdownSelector(
                            title = "Problem Category",
                            desc = "Tag category of reported transaction.",
                            options = listOf("VIDEO", "USER", "COMMENT", "SPAM", "ABUSE"),
                            selected = reportCategory,
                            onSelect = { reportCategory = it }
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        SettingsTextField(
                            value = reportTitle,
                            onValueChange = { reportTitle = it },
                            label = "Report Title or Item ID (e.g. @spam_bot)",
                            testTag = "support_report_title"
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        SettingsTextField(
                            value = reportDesc,
                            onValueChange = { reportDesc = it },
                            label = "Detailed Problem Description",
                            testTag = "support_report_desc"
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (reportTitle.isBlank() || reportDesc.isBlank()) {
                                    android.widget.Toast.makeText(context, "Please fill in all report fields!", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.insertReport(
                                    Report(
                                        type = reportCategory,
                                        contentId = "user_reported",
                                        reportedItemTitle = reportTitle,
                                        reason = reportDesc,
                                        reportedBy = viewModel.currentUsername
                                    )
                                )
                                android.widget.Toast.makeText(context, "Report submitted! Handled by Deen Tok Admin team.", android.widget.Toast.LENGTH_LONG).show()
                                reportTitle = ""
                                reportDesc = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("support_submit_report_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("SUBMIT COMPLAINT REPORT", color = Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = SurfaceDark)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "DIRECT CHANNELS",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text("Hotline support: +251 116 DEENTOK (Addis Ababa)", color = TextGray, fontSize = 12.sp)
                        Text("Official support email: support@deentok.app", color = TextGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("COMMUNITY & LEGAL", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        
                        listOf("Community Guidelines", "Privacy Policy", "Terms of Service").forEach { item ->
                            Text(
                                text = "• View $item",
                                color = CyanAccent,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        android.widget.Toast.makeText(context, "Displaying $item: Adhere to positive interaction standards.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }
                    }
                }

                SettingsSection.ABOUT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // DEEN TOK BRAND LOGO
                        DeenTokLogo(sizeDp = 80.dp)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("DEEN TOK VIDEO PLATFORM", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Ethiopia Stable Core Release", color = TextGray, fontSize = 11.sp, letterSpacing = 1.sp)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Software Version", color = TextGray, fontSize = 12.sp)
                                    Text("v1.0.0 Stable Build", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Data Store Engine", color = TextGray, fontSize = 12.sp)
                                    Text("SQLite / Room Cache", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Licensing Structure", color = TextGray, fontSize = 12.sp)
                                    Text("Proprietary Secure", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Copyright", color = TextGray, fontSize = 11.sp)
                                    Text("© 2026 Deen Tok Tech", color = TextGray, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("FOLLOW THE MOVEMENT", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val socials = listOf("Telegram" to "tg", "Twitter" to "tw", "Instagram" to "ig")
                            socials.forEach { social ->
                                Button(
                                    onClick = {
                                        android.widget.Toast.makeText(context, "Navigating to Deen Tok ${social.first} Channel!", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                                ) {
                                    Text(social.first, color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Two factor dialog modal simulation
    if (showTwoFactorDialog) {
        AlertDialog(
            onDismissRequest = { showTwoFactorDialog = false },
            title = { Text("Activate Two-Factor Login", color = TextWhite) },
            text = { Text("To enable full 2FA challenge screens, confirm confirmation dispatch to registered email address: ${currentPrefs.email}.", color = TextGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTwoFactorDialog = false
                        android.widget.Toast.makeText(context, "Two-Factor authentication enrolled successfully!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("CONFIRM DISPATCH", color = CyanAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTwoFactorDialog = false
                    viewModel.saveUserPreferences(currentPrefs.copy(twoFactorEnabled = false))
                }) {
                    Text("CANCEL", color = TextGray)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Delete account dialog confirmation modal
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("CRITICAL WARNING: Delete Account", color = LikeRed, fontWeight = FontWeight.Bold) },
            text = { Text("Are you absolutely certain you want to destroy your Deen Tok digital profile? This operation cannot be undone and will wipe all local SQLite data streams.", color = TextWhite) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.deleteUserAccount()
                        android.widget.Toast.makeText(context, "Your Deen Tok profile and SQLite records were completely purged.", android.widget.Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LikeRed)
                ) {
                    Text("YES, WIPE EVERYTHING", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("ABORT DELETION", color = TextGray)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun SettingsMenuRow(
    title: String,
    desc: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = CyanAccent, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = desc, color = TextGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Navigate", tint = TextGray, modifier = Modifier.size(18.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextGray, fontSize = 12.sp) },
        singleLine = true,
        textStyle = TextStyle(color = TextWhite, fontSize = 14.sp),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = SurfaceDark,
            cursorColor = CyanAccent,
            focusedContainerColor = SurfaceDark,
            unfocusedContainerColor = SurfaceDark
        )
    )
}

@Composable
fun SettingsToggleRow(
    title: String,
    desc: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = desc, color = TextGray, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Black,
                checkedTrackColor = CyanAccent,
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = SurfaceDark,
                uncheckedBorderColor = SurfaceDark
            )
        )
    }
}

@Composable
fun SettingsDropdownSelector(
    title: String,
    desc: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = desc, color = TextGray, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 2.dp))
            }
            
            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selected, color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = CyanAccent, modifier = Modifier.size(16.dp))
                    }
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    options.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt, color = TextWhite, fontSize = 13.sp) },
                            onClick = {
                                onSelect(opt)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}


// 10. CREATOR STUDIO & CONTENT MANAGEMENT ENGINE
@Composable
fun CreatorStudioScreen(viewModel: DeenTokViewModel) {
    val allVideos by viewModel.allVideos.collectAsState()
    val allNotifications by viewModel.allNotifications.collectAsState()

    // 1. Compute dynamic database metrics for the logged-in user (userId = "current_user")
    val myVideos = remember(allVideos) { allVideos.filter { it.userId == "current_user" } }
    val totalUploaded = myVideos.size
    val totalViews = remember(myVideos) { myVideos.sumOf { it.viewsCount } }
    val totalLikes = remember(myVideos) { myVideos.sumOf { it.likesCount } }
    val totalComments = remember(myVideos) { myVideos.sumOf { it.commentsCount } }
    val totalShares = remember(myVideos) { myVideos.sumOf { it.sharesCount } }
    val engagementRate = remember(totalViews, totalLikes, totalComments, totalShares) {
        if (totalViews > 0) {
            ((totalLikes + totalComments + totalShares).toFloat() / totalViews) * 100f
        } else {
            0f
        }
    }

    // Performance winners
    val bestPerformingVideo = remember(myVideos) { myVideos.maxByOrNull { it.viewsCount } }
    val mostLikedVideo = remember(myVideos) { myVideos.maxByOrNull { it.likesCount } }
    val mostCommentedVideo = remember(myVideos) { myVideos.maxByOrNull { it.commentsCount } }
    val mostSharedVideo = remember(myVideos) { myVideos.maxByOrNull { it.sharesCount } }

    // Recent creator activities (likes/comments notifications)
    val recentActivities = remember(allNotifications) {
        allNotifications.filter { it.type == "LIKE" || it.type == "COMMENT" || it.type == "FOLLOW" }
    }

    // Active sub-tab state (0 = Dashboard, 1 = Content Library, 2 = Performance, 3 = Profile Settings, 4 = Monetization & Future Tools)
    var activeStudioTab by remember { mutableStateOf(0) }

    // Modals & Dialogs States
    var videoToEdit by remember { mutableStateOf<Video?>(null) }
    var videoToDelete by remember { mutableStateOf<Video?>(null) }

    // Success snackbar feedback state
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Launch snackbar auto-dismiss
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            delay(3000)
            snackbarMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.setScreen(Screen.PROFILE) },
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceDark, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DEENTOK CREATOR STUDIO",
                    color = TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Manage your voice & grow your community",
                    color = TextGray,
                    fontSize = 11.sp
                )
            }
            // Realtime DB indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyanAccent.copy(alpha = 0.1f))
                    .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(CyanAccent, CircleShape)
                    )
                    Text("CONNECTED", color = CyanAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Custom Sub-tabs ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StudioTabItem(
                label = "Dashboard",
                icon = Icons.Default.BarChart,
                selected = activeStudioTab == 0
            ) { activeStudioTab = 0 }

            StudioTabItem(
                label = "Content (${myVideos.size})",
                icon = Icons.Default.VideoLibrary,
                selected = activeStudioTab == 1
            ) { activeStudioTab = 1 }

            StudioTabItem(
                label = "Performance",
                icon = Icons.Default.TrendingUp,
                selected = activeStudioTab == 2
            ) { activeStudioTab = 2 }

            StudioTabItem(
                label = "Studio Profile",
                icon = Icons.Default.Person,
                selected = activeStudioTab == 3
            ) { activeStudioTab = 3 }

            StudioTabItem(
                label = "Monetize",
                icon = Icons.Default.Shield,
                selected = activeStudioTab == 4
            ) { activeStudioTab = 4 }
        }

        // --- Snackbar Alert ---
        AnimatedVisibility(
            visible = snackbarMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyanPrimary)
                    .padding(14.dp)
            ) {
                Text(
                    text = snackbarMessage ?: "",
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Main Tab Content ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (activeStudioTab) {
                0 -> StudioDashboardView(
                    totalViews = totalViews,
                    totalLikes = totalLikes,
                    totalComments = totalComments,
                    totalUploaded = totalUploaded,
                    engagementRate = engagementRate,
                    recentActivities = recentActivities,
                    viewModel = viewModel
                )
                1 -> StudioContentManagerView(
                    myVideos = myVideos,
                    onEditVideo = { videoToEdit = it },
                    onDeleteVideo = { videoToDelete = it },
                    onTogglePrivacy = { video, isPrivate ->
                        viewModel.updateVideoVisibility(video, isPrivate)
                        snackbarMessage = "Updated visibility of '${video.title}' to ${if (isPrivate) "Private" else "Public"}"
                    }
                )
                2 -> StudioPerformanceWinsView(
                    bestPerformingVideo = bestPerformingVideo,
                    mostLikedVideo = mostLikedVideo,
                    mostCommentedVideo = mostCommentedVideo,
                    mostSharedVideo = mostSharedVideo
                )
                3 -> StudioProfileManagementView(
                    viewModel = viewModel,
                    onSaveSuccess = {
                        snackbarMessage = "Studio Profile updated successfully!"
                    }
                )
                4 -> StudioMonetizationView(
                    totalViews = totalViews,
                    totalFollowers = 842, // Consistent followers count
                    viewModel = viewModel
                )
            }
        }
    }

    // --- EDIT VIDEO MODAL DIALOG ---
    if (videoToEdit != null) {
        val editingVideo = videoToEdit!!
        var editTitle by remember(editingVideo) { mutableStateOf(editingVideo.title) }
        var editDesc by remember(editingVideo) { mutableStateOf(editingVideo.description) }
        var editCategory by remember(editingVideo) { mutableStateOf(editingVideo.category) }
        var editIsPrivate by remember(editingVideo) { mutableStateOf(editingVideo.isPrivate) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { videoToEdit = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkAccent)
                    .border(1.dp, SurfaceVariantDark, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Edit Video Details",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    // Title Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Title / Caption", color = TextGray, fontSize = 11.sp)
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            modifier = Modifier.fillMaxWidth().testTag("edit_video_title"),
                            textStyle = TextStyle(color = TextWhite, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = SurfaceVariantDark,
                                focusedContainerColor = Black,
                                unfocusedContainerColor = Black
                            ),
                            singleLine = true
                        )
                    }

                    // Description Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Description / Hashtags", color = TextGray, fontSize = 11.sp)
                        OutlinedTextField(
                            value = editDesc,
                            onValueChange = { editDesc = it },
                            modifier = Modifier.fillMaxWidth().height(80.dp).testTag("edit_video_desc"),
                            textStyle = TextStyle(color = TextWhite, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = SurfaceVariantDark,
                                focusedContainerColor = Black,
                                unfocusedContainerColor = Black
                            )
                        )
                    }

                    // Category Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Category", color = TextGray, fontSize = 11.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        ) {
                            listOf("Tech", "Music", "Vlog", "Cooking", "Art").forEach { cat ->
                                val isSelected = editCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) CyanAccent else SurfaceDark)
                                        .border(1.dp, if (isSelected) CyanAccent else SurfaceVariantDark, RoundedCornerShape(8.dp))
                                        .clickable { editCategory = cat }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) Black else TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Visibility Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Private Video", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Only you can view this content", color = TextGray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = editIsPrivate,
                            onCheckedChange = { editIsPrivate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyanAccent,
                                checkedTrackColor = CyanAccent.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { videoToEdit = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                        ) {
                            Text("Cancel", color = TextWhite)
                        }
                        Button(
                            onClick = {
                                viewModel.updateVideoDetails(editingVideo, editTitle, editDesc, editCategory)
                                viewModel.updateVideoVisibility(editingVideo, editIsPrivate)
                                snackbarMessage = "Video and privacy settings updated successfully!"
                                videoToEdit = null
                            },
                            modifier = Modifier.weight(1f).testTag("save_video_edit"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                        ) {
                            Text("Save", color = Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- DELETE VIDEO CONFIRMATION DIALOG ---
    if (videoToDelete != null) {
        val deletingVideo = videoToDelete!!
        androidx.compose.ui.window.Dialog(onDismissRequest = { videoToDelete = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkAccent)
                    .border(1.dp, SurfaceVariantDark, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(LikeRed.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Alert", tint = LikeRed, modifier = Modifier.size(28.dp))
                    }
                    Text(
                        text = "Delete Video permanently?",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Are you sure you want to delete \"${deletingVideo.title}\"? This action cannot be undone and will erase all comments and analytics.",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { videoToDelete = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                        ) {
                            Text("Cancel", color = TextWhite)
                        }
                        Button(
                            onClick = {
                                viewModel.deleteVideo(deletingVideo)
                                snackbarMessage = "Successfully deleted video."
                                videoToDelete = null
                            },
                            modifier = Modifier.weight(1f).testTag("confirm_delete_video"),
                            colors = ButtonDefaults.buttonColors(containerColor = LikeRed)
                        ) {
                            Text("Delete", color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudioTabItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) CyanAccent else SurfaceDark)
            .border(1.dp, if (selected) CyanAccent else SurfaceVariantDark, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Black else TextWhite,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = if (selected) Black else TextWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StudioDashboardView(
    totalViews: Int,
    totalLikes: Int,
    totalComments: Int,
    totalUploaded: Int,
    engagementRate: Float,
    recentActivities: List<Notification>,
    viewModel: DeenTokViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- High-fidelity Analytics Cards Grid ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StudioMetricCard(
                label = "Total Video Views",
                value = formatCount(totalViews),
                trend = "+14.8%",
                isTrendPositive = true,
                icon = Icons.Default.Explore,
                modifier = Modifier.weight(1f)
            )
            StudioMetricCard(
                label = "Total Likes",
                value = formatCount(totalLikes),
                trend = "+11.2%",
                isTrendPositive = true,
                icon = Icons.Default.Favorite,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StudioMetricCard(
                label = "Comments Received",
                value = formatCount(totalComments),
                trend = "+8.5%",
                isTrendPositive = true,
                icon = Icons.Default.ChatBubble,
                modifier = Modifier.weight(1f)
            )
            StudioMetricCard(
                label = "Engagement Rate",
                value = String.format("%.1f%%", engagementRate),
                trend = "+5.1%",
                isTrendPositive = true,
                icon = Icons.Default.TrendingUp,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StudioMetricCard(
                label = "Videos Uploaded",
                value = totalUploaded.toString(),
                trend = "Live DB",
                isTrendPositive = true,
                icon = Icons.Default.VideoLibrary,
                modifier = Modifier.weight(1f)
            )
            StudioMetricCard(
                label = "Followers / Following",
                value = "${viewModel.currentUserFollowersCount} / ${viewModel.currentUserFollowingCount}",
                trend = "Community",
                isTrendPositive = true,
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f)
            )
        }

        // --- Visual 7-Day Matrix ---
        Text(
            text = "7-DAY GROWTH ANALYSIS",
            color = CyanAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val chartWidth = size.width
                val chartHeight = size.height

                // Draw background horizontal grid lines
                val gridYCount = 5
                for (i in 0 until gridYCount) {
                    val y = chartHeight * i / (gridYCount - 1)
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )
                }

                // Analytics plot points for views (Cyan)
                val viewPoints = listOf(
                    Offset(0f, chartHeight * 0.85f),
                    Offset(chartWidth * 0.16f, chartHeight * 0.72f),
                    Offset(chartWidth * 0.33f, chartHeight * 0.45f),
                    Offset(chartWidth * 0.5f, chartHeight * 0.60f),
                    Offset(chartWidth * 0.66f, chartHeight * 0.25f),
                    Offset(chartWidth * 0.83f, chartHeight * 0.38f),
                    Offset(chartWidth, chartHeight * 0.15f)
                )

                // Brush gradient fill under views path
                val viewsPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, chartHeight)
                    for (point in viewPoints) {
                        lineTo(point.x, point.y)
                    }
                    lineTo(chartWidth, chartHeight)
                    close()
                }
                drawPath(
                    path = viewsPath,
                    brush = Brush.verticalGradient(
                        listOf(CyanAccent.copy(alpha = 0.25f), Color.Transparent)
                    )
                )

                // Draw line connection for views
                for (i in 0 until viewPoints.size - 1) {
                    drawLine(
                        color = CyanAccent,
                        start = viewPoints[i],
                        end = viewPoints[i + 1],
                        strokeWidth = 2.5.dp.toPx()
                    )
                }

                // Draw point markers
                for (point in viewPoints) {
                    drawCircle(color = Black, radius = 4.5.dp.toPx(), center = point)
                    drawCircle(color = CyanAccent, radius = 2.5.dp.toPx(), center = point)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Jul 10", color = TextMuted, fontSize = 9.sp)
            Text("Jul 13", color = TextMuted, fontSize = 9.sp)
            Text("Jul 16 (Today)", color = TextMuted, fontSize = 9.sp)
        }

        // --- Audience Activity ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceDark)
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyanAccent.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = "Activity", tint = CyanAccent, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Peak Audience Engagement", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Your audience is most active between 7:00 PM and 10:00 PM UTC. Best time to post!", color = TextGray, fontSize = 11.sp, lineHeight = 15.sp)
                }
            }
        }

        // --- Recent Content Activity (Dynamic Activity Feed) ---
        Text(
            text = "RECENT CHANNEL ACTIVITY",
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        if (recentActivities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(BorderStroke(0.5.dp, SurfaceVariantDark), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No recent notifications or activities yet.", color = TextMuted, fontSize = 11.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentActivities.take(5).forEach { act ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val (actColor, actIcon) = when (act.type) {
                            "LIKE" -> Pair(LikeRed, Icons.Default.Favorite)
                            "COMMENT" -> Pair(CyanAccent, Icons.Default.ChatBubble)
                            else -> Pair(TextWhite, Icons.Default.Person)
                        }
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(actColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(actIcon, contentDescription = null, tint = actColor, modifier = Modifier.size(14.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${act.sourceUsername} ${act.description}",
                                color = TextWhite,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Recent • ${act.title}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StudioMetricCard(
    label: String,
    value: String,
    trend: String,
    isTrendPositive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = CyanAccent.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
            }
            Text(text = value, color = TextWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(if (isTrendPositive) CyanAccent else LikeRed, CircleShape)
                )
                Text(
                    text = trend,
                    color = if (isTrendPositive) CyanAccent else LikeRed,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StudioContentManagerView(
    myVideos: List<Video>,
    onEditVideo: (Video) -> Unit,
    onDeleteVideo: (Video) -> Unit,
    onTogglePrivacy: (Video, Boolean) -> Unit
) {
    if (myVideos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = "No Videos",
                    tint = TextMuted,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No Videos Uploaded Yet",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Upload your voice and start tracking real-time database views, likes, comments, and analytics here.",
                    color = TextGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(myVideos) { video ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(BorderStroke(0.5.dp, SurfaceVariantDark), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(SurfaceVariantDark, DarkAccent)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = video.category,
                                        color = TextWhite,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = video.title,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = video.description,
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Posted: Jul 16, 2026",
                                    color = TextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Explore, contentDescription = "Views", tint = TextMuted, modifier = Modifier.size(12.dp))
                                    Text("${formatCount(video.viewsCount)} views", color = TextWhite, fontSize = 11.sp)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Likes", tint = LikeRed, modifier = Modifier.size(11.dp))
                                    Text("${formatCount(video.likesCount)} likes", color = TextWhite, fontSize = 11.sp)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.ChatBubble, contentDescription = "Comments", tint = CyanAccent, modifier = Modifier.size(11.dp))
                                    Text("${formatCount(video.commentsCount)} comments", color = TextWhite, fontSize = 11.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (video.isPrivate) SurfaceVariantDark else CyanAccent.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (video.isPrivate) "PRIVATE" else "PUBLIC",
                                    color = if (video.isPrivate) TextGray else CyanAccent,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Visibility:",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceVariantDark)
                                        .clickable { onTogglePrivacy(video, !video.isPrivate) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(if (video.isPrivate) TextMuted else CyanAccent, CircleShape)
                                        )
                                        Text(
                                            text = if (video.isPrivate) "Make Public" else "Make Private",
                                            color = TextWhite,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { onEditVideo(video) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(SurfaceVariantDark, CircleShape)
                                        .testTag("edit_video_btn_${video.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Edit Details",
                                        tint = TextWhite,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteVideo(video) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(LikeRed.copy(alpha = 0.15f), CircleShape)
                                        .testTag("delete_video_btn_${video.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Video",
                                        tint = LikeRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudioPerformanceWinsView(
    bestPerformingVideo: Video?,
    mostLikedVideo: Video?,
    mostCommentedVideo: Video?,
    mostSharedVideo: Video?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CREATOR PERFORMANCE ACHIEVEMENT AWARDS",
            color = CyanAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        PerformanceAwardCard(
            title = "Best Performing Voice",
            subtitle = "Highest view count in database",
            video = bestPerformingVideo,
            metricLabel = "Views",
            metricValue = bestPerformingVideo?.viewsCount?.let { formatCount(it) } ?: "0",
            iconColor = CyanAccent,
            badgeSymbol = "🏆"
        )

        PerformanceAwardCard(
            title = "Most Loved Voice",
            subtitle = "Highest total likes in database",
            video = mostLikedVideo,
            metricLabel = "Likes",
            metricValue = mostLikedVideo?.likesCount?.let { formatCount(it) } ?: "0",
            iconColor = LikeRed,
            badgeSymbol = "💖"
        )

        PerformanceAwardCard(
            title = "Most Discussed Voice",
            subtitle = "Highest comment count in database",
            video = mostCommentedVideo,
            metricLabel = "Comments",
            metricValue = mostCommentedVideo?.commentsCount?.let { formatCount(it) } ?: "0",
            iconColor = Color.Yellow,
            badgeSymbol = "💬"
        )

        PerformanceAwardCard(
            title = "Most Shared Voice",
            subtitle = "Highest shares count in database",
            video = mostSharedVideo,
            metricLabel = "Shares",
            metricValue = mostSharedVideo?.sharesCount?.let { formatCount(it) } ?: "0",
            iconColor = BlueAccent,
            badgeSymbol = "🚀"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PerformanceAwardCard(
    title: String,
    subtitle: String,
    video: Video?,
    metricLabel: String,
    metricValue: String,
    iconColor: Color,
    badgeSymbol: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(BorderStroke(1.dp, SurfaceVariantDark), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(badgeSymbol, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = subtitle, color = TextGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (video != null) {
                    Text(
                        text = "\"${video.title}\"",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "No videos uploaded yet",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Black)
                    .border(BorderStroke(0.5.dp, SurfaceVariantDark), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = metricValue, color = iconColor, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    Text(text = metricLabel, color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StudioProfileManagementView(
    viewModel: DeenTokViewModel,
    onSaveSuccess: () -> Unit
) {
    var editUsername by remember { mutableStateOf(viewModel.currentUsername) }
    var editAvatar by remember { mutableStateOf(viewModel.currentUserAvatar) }
    var editBio by remember { mutableStateOf(viewModel.currentUserBio) }
    var editWebsite by remember { mutableStateOf(viewModel.currentUserWebsite) }
    var editTwitter by remember { mutableStateOf(viewModel.currentUserTwitter) }
    var editInstagram by remember { mutableStateOf(viewModel.currentUserInstagram) }

    var showProfilePicDialog by remember { mutableStateOf(false) }

    if (showProfilePicDialog) {
        ProfilePicChangeDialog(
            currentAvatar = editAvatar,
            onDismiss = { showProfilePicDialog = false },
            onAvatarSelected = { newAvatar ->
                editAvatar = newAvatar
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "CREATOR DIGITAL PROFILE SETTINGS",
            color = CyanAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .border(2.dp, CyanAccent, CircleShape)
                        .clickable { showProfilePicDialog = true }
                        .testTag("profile_management_avatar_preview"),
                    contentAlignment = Alignment.Center
                ) {
                    val isImage = editAvatar.startsWith("http") || 
                                  editAvatar.startsWith("content:") || 
                                  editAvatar.startsWith("file:") || 
                                  editAvatar.startsWith("android.resource:")
                    if (isImage) {
                        coil.compose.AsyncImage(
                            model = editAvatar,
                            contentDescription = "Profile Picture Preview",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = editAvatar.take(2).uppercase(),
                            color = CyanAccent,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Small + button overlay
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(CyanAccent)
                        .border(1.5.dp, Black, CircleShape)
                        .clickable { showProfilePicDialog = true }
                        .testTag("profile_management_add_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Change avatar",
                        tint = Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Text("Profile Avatar Initials or Web URL", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = editAvatar,
                    onValueChange = { editAvatar = it },
                    modifier = Modifier.fillMaxWidth().testTag("profile_edit_avatar_input"),
                    textStyle = TextStyle(color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = SurfaceVariantDark,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    ),
                    singleLine = true
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Username", color = TextGray, fontSize = 11.sp)
            OutlinedTextField(
                value = editUsername,
                onValueChange = { editUsername = it },
                modifier = Modifier.fillMaxWidth().testTag("profile_edit_username_input"),
                textStyle = TextStyle(color = TextWhite, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = SurfaceVariantDark,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                singleLine = true
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Biography", color = TextGray, fontSize = 11.sp)
            OutlinedTextField(
                value = editBio,
                onValueChange = { editBio = it },
                modifier = Modifier.fillMaxWidth().height(80.dp).testTag("profile_edit_bio_input"),
                textStyle = TextStyle(color = TextWhite, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = SurfaceVariantDark,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                )
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Website / Linktree", color = TextGray, fontSize = 11.sp)
            OutlinedTextField(
                value = editWebsite,
                onValueChange = { editWebsite = it },
                modifier = Modifier.fillMaxWidth().testTag("profile_edit_website_input"),
                textStyle = TextStyle(color = TextWhite, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = SurfaceVariantDark,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                singleLine = true
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Twitter / X Social handle", color = TextGray, fontSize = 11.sp)
            OutlinedTextField(
                value = editTwitter,
                onValueChange = { editTwitter = it },
                modifier = Modifier.fillMaxWidth().testTag("profile_edit_twitter_input"),
                textStyle = TextStyle(color = TextWhite, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = SurfaceVariantDark,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                singleLine = true
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Instagram Social handle", color = TextGray, fontSize = 11.sp)
            OutlinedTextField(
                value = editInstagram,
                onValueChange = { editInstagram = it },
                modifier = Modifier.fillMaxWidth().testTag("profile_edit_instagram_input"),
                textStyle = TextStyle(color = TextWhite, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = SurfaceVariantDark,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                viewModel.updateCreatorProfile(
                    username = editUsername,
                    avatar = editAvatar,
                    bio = editBio,
                    website = editWebsite,
                    twitter = editTwitter,
                    instagram = editInstagram
                )
                onSaveSuccess()
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_profile_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Save Digital Identity", color = Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StudioMonetizationView(
    totalViews: Int,
    totalFollowers: Int,
    viewModel: DeenTokViewModel
) {
    val allVideos by viewModel.allVideos.collectAsState()
    val myVideos = remember(allVideos) { allVideos.filter { it.userId == "current_user" } }
    val totalUploaded = myVideos.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CREATOR TOOLS LIBRARY",
            color = CyanAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceDark)
                    .border(BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
                    .clickable { viewModel.setScreen(Screen.UPLOAD) }
                    .padding(14.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(CyanAccent.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Upload", tint = CyanAccent, modifier = Modifier.size(22.dp))
                    }
                    Text("Upload New Voice", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Publish to live DB", color = TextGray, fontSize = 9.sp, textAlign = TextAlign.Center)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceDark)
                    .border(BorderStroke(0.5.dp, SurfaceVariantDark), RoundedCornerShape(10.dp))
                    .clickable { }
                    .padding(14.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SurfaceVariantDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Drafts", tint = TextWhite, modifier = Modifier.size(18.dp))
                    }
                    Text("Manage Drafts (0)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Offline voice cache", color = TextGray, fontSize = 9.sp, textAlign = TextAlign.Center)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceDark)
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SurfaceVariantDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = "Sounds", tint = TextWhite, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Global Sound Library", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Browse over 150+ royalty-free beats and ambient soundscapes to overlay on your voices.", color = TextGray, fontSize = 11.sp)
                }
            }
        }

        Text(
            text = "DEENTOK PARTNER MONETIZATION ENGINE",
            color = CyanAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        val starsProg = (totalFollowers.toFloat() / 1000f).coerceIn(0f, 1f)
        MonetizationTierCard(
            title = "Deen Tok Voice Stars Creator Fund",
            description = "Receive virtual Stars tips from viewers that can be converted directly into real currency cash payouts.",
            progressLabel = "Followers required: $totalFollowers / 1,000",
            progress = starsProg,
            unlocked = totalFollowers >= 1000
        )

        val adProg = (totalViews.toFloat() / 100000f).coerceIn(0f, 1f)
        MonetizationTierCard(
            title = "Ad-Revenue Share Revenue Program",
            description = "Receive 55% of video ad placement revenue embedded inside your voice feeds dynamically.",
            progressLabel = "Views required: ${formatCount(totalViews)} / 100K",
            progress = adProg,
            unlocked = totalViews >= 100000
        )

        val uploadCount = totalUploaded
        MonetizationTierCard(
            title = "Paid Monthly Premium Audio Passes",
            description = "Offer premium content unlocked only by monthly paying channel sub-tier members.",
            progressLabel = "Unlocked at: 10 Uploads (${uploadCount}/10)",
            progress = (uploadCount.toFloat() / 10f).coerceIn(0f, 1f),
            unlocked = uploadCount >= 10
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MonetizationTierCard(
    title: String,
    description: String,
    progressLabel: String,
    progress: Float,
    unlocked: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(
                BorderStroke(
                    1.dp,
                    if (unlocked) CyanAccent.copy(alpha = 0.5f) else SurfaceVariantDark
                ),
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (unlocked) CyanAccent.copy(alpha = 0.15f) else SurfaceVariantDark)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (unlocked) "QUALIFIED" else "LOCKED",
                        color = if (unlocked) CyanAccent else TextMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(text = description, color = TextGray, fontSize = 10.sp, lineHeight = 14.sp)
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = progressLabel, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = String.format("%.0f%%", progress * 100f), color = if (unlocked) CyanAccent else TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Black)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(if (unlocked) CyanAccent else TextMuted)
                    )
                }
            }
        }
    }
}

// REDESIGNED ADMIN DASHBOARD REBUILT IN AdminDashboard.kt
// --- OTHER SHARED COMPONENT OVERLAYS ---

// Bottom Navigation pill items
@Composable
fun DeenTokBottomNavigation(
    currentScreen: Screen,
    unreadCount: Int,
    viewModel: DeenTokViewModel,
    onNavigate: (Screen) -> Unit
) {
    val trans = getTranslations(viewModel)
    val items = listOf(
        NavigationItem(trans.navHome, Screen.HOME, Icons.Default.Home, Icons.Outlined.Home),
        NavigationItem(trans.navExplore, Screen.EXPLORE, Icons.Default.Explore, Icons.Outlined.Explore),
        NavigationItem(trans.navAdd, Screen.UPLOAD, Icons.Default.Add, Icons.Default.Add),
        NavigationItem(trans.navMessages, Screen.MESSAGES, Icons.Default.Mail, Icons.Outlined.Mail),
        NavigationItem(trans.navProfile, Screen.PROFILE, Icons.Default.Person, Icons.Outlined.Person)
    )

    Surface(
        color = Black,
        tonalElevation = 8.dp,
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentScreen == item.screen
                
                if (item.screen == Screen.UPLOAD) {
                    // Custom Add Button highlighted with shadows
                    Box(
                        modifier = Modifier
                            .testTag("upload_nav_button")
                            .width(52.dp)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TextWhite)
                            .clickable { onNavigate(item.screen) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Upload",
                            tint = Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    // Standard icons with badge notifications
                    Column(
                        modifier = Modifier
                            .testTag(item.screen.name.lowercase() + "_nav_tab")
                            .clickable { onNavigate(item.screen) }
                            .padding(vertical = 4.dp, horizontal = 12.dp)
                            .widthIn(min = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                                contentDescription = item.label,
                                tint = if (selected) TextWhite else TextWhite.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                            // Render unread notifications alert badge on inbox tab
                            if (item.screen == Screen.MESSAGES && unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 8.dp, y = (-4).dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(LikeRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unreadCount.toString(),
                                        color = TextWhite,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = item.label,
                            color = if (selected) TextWhite else TextWhite.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val screen: Screen,
    val activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector
)

// Comments bottom sheet panel
@Composable
fun CommentsOverlaySheet(
    viewModel: DeenTokViewModel,
    videoId: Int,
    onDismiss: () -> Unit
) {
    val comments by viewModel.activeVideoComments.collectAsState()
    var inputComment by remember { mutableStateOf("") }

    // Animating overlay transition
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black.copy(alpha = 0.6f))
            .clickable { onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .clickable(enabled = false) { /* no-op stops click propagations */ },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = DarkGrey,
            border = BorderStroke(0.5.dp, SurfaceVariantDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Sheet Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comments (${comments.size})",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close comments", tint = TextWhite)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable comments list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No comments yet. Start the conversation!", color = TextGray, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            CommentRowItem(comment = comment)
                        }
                    }
                }

                // Add comment input section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputComment,
                        onValueChange = { inputComment = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field"),
                        placeholder = { Text("Write your thoughts down...", color = TextMuted, fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Black,
                            unfocusedContainerColor = Black,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = SurfaceVariantDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    IconButton(
                        onClick = {
                            viewModel.addComment(videoId, inputComment)
                            inputComment = ""
                        },
                        modifier = Modifier
                            .testTag("submit_comment_button")
                            .clip(CircleShape)
                            .background(CyanAccent)
                            .size(44.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post Comment", tint = Black)
                    }
                }
            }
        }
    }
}

@Composable
fun CommentRowItem(comment: Comment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Text(comment.userAvatarUrl, color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = comment.username, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = comment.text, color = TextGray, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

// Utilities count formatting helper (e.g. 24800 -> 24.8K)
fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
        count >= 1000 -> String.format("%.1fK", count / 1000.0)
        else -> count.toString()
    }
}

// ================= WELCOME SCREEN & LOCALIZATION COMPONENTS =================

enum class WelcomeState {
    HERO, LOGIN, SIGNUP
}


@Composable
fun DeenTokLogo(modifier: Modifier = Modifier, sizeDp: androidx.compose.ui.unit.Dp = 96.dp) {
    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(Color(0xFF0F2E1E)) // Elegant peaceful Islamic Forest Green background
            .border(2.dp, Color(0xFFD4AF37), androidx.compose.foundation.shape.CircleShape), // Gold border
        contentAlignment = Alignment.Center
    ) {
        // Draw Gold Crescent Moon
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)
            
            // Draw a Gold crescent moon wrapping on the left
            val moonPath = Path().apply {
                val radius = w * 0.35f
                addArc(
                    oval = androidx.compose.ui.geometry.Rect(center = Offset(w * 0.44f, h * 0.5f), radius = radius),
                    startAngleDegrees = -110f,
                    sweepAngleDegrees = 220f
                )
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(center = Offset(w * 0.54f, h * 0.5f), radius = radius * 0.95f),
                    startAngleDegrees = 110f,
                    sweepAngleDegrees = -220f,
                    forceMoveTo = false
                )
                close()
            }
            drawPath(
                path = moonPath,
                color = Color(0xFFD4AF37) // Gold
            )
        }
        
        // Custom logo layout for "DT" text inside the moon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = (sizeDp.value * 0.12f).dp) // offset slightly to the right to balance the moon
        ) {
            // "D" in Gold
            Text(
                text = "D",
                color = Color(0xFFD4AF37),
                fontSize = (sizeDp.value * 0.28f).sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
            )
            
            Spacer(modifier = Modifier.width((sizeDp.value * 0.04f).dp))
            
            // "T" with custom play button integrated
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size((sizeDp.value * 0.38f).dp)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val wT = size.width
                    val hT = size.height
                    
                    // Top horizontal bar of T (White)
                    val barHeight = hT * 0.22f
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(0f, hT * 0.15f),
                        size = androidx.compose.ui.geometry.Size(wT, barHeight)
                    )
                    
                    // Vertical stem of T (White)
                    val stemWidth = wT * 0.24f
                    drawRect(
                        color = Color.White,
                        topLeft = Offset((wT - stemWidth) / 2f, hT * 0.15f + barHeight),
                        size = androidx.compose.ui.geometry.Size(stemWidth, hT * 0.7f)
                    )
                    
                    // Hidden play triangle symbol inside the "T" (Gold)
                    val playPath = Path().apply {
                        val triSize = wT * 0.25f
                        val triCenterX = wT / 2f + wT * 0.03f
                        val triCenterY = hT * 0.55f
                        
                        moveTo(triCenterX - triSize / 2f, triCenterY - triSize / 2f)
                        lineTo(triCenterX + triSize * 0.6f, triCenterY)
                        lineTo(triCenterX - triSize / 2f, triCenterY + triSize / 2f)
                        close()
                    }
                    drawPath(
                        path = playPath,
                        color = Color(0xFFD4AF37) // Gold
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(viewModel: DeenTokViewModel) {
    val selectedLanguageStr by viewModel.selectedLanguage.collectAsState()
    val activeLang = remember(selectedLanguageStr) {
        Language.values().firstOrNull { it.displayName == selectedLanguageStr } ?: Language.ENGLISH
    }
    val translations = translationsMap[activeLang] ?: translationsMap[Language.ENGLISH]!!

    var showLoginForm by remember { mutableStateOf(false) }
    var showSignUpForm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .drawBehind {
                val w = this.size.width
                val h = this.size.height
                drawCircle(
                    color = CyanAccent.copy(alpha = 0.08f),
                    radius = 450f,
                    center = Offset(w * 0.2f, h * 0.2f)
                )
                drawCircle(
                    color = BlueAccent.copy(alpha = 0.06f),
                    radius = 600f,
                    center = Offset(w * 0.8f, h * 0.8f)
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Language selector at top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by remember { mutableStateOf(false) }
                
                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, TextGray.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("language_selector")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Select Language",
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${activeLang.flag} ${activeLang.displayName}",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceVariantDark, RoundedCornerShape(12.dp))
                    ) {
                        Language.values().forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = lang.flag, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = lang.displayName,
                                            color = if (lang == activeLang) CyanAccent else TextWhite,
                                            fontWeight = if (lang == activeLang) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setLanguage(lang.displayName)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Dynamic forms or Hero Branding
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = when {
                        showLoginForm -> WelcomeState.LOGIN
                        showSignUpForm -> WelcomeState.SIGNUP
                        else -> WelcomeState.HERO
                    },
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300)))
                            .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f, animationSpec = tween(200)))
                    },
                    label = "welcome_content"
                ) { state ->
                    when (state) {
                        WelcomeState.LOGIN -> {
                            WelcomeLoginFormCard(
                                viewModel = viewModel,
                                translations = translations,
                                onDismiss = { showLoginForm = false }
                            )
                        }
                        WelcomeState.SIGNUP -> {
                            WelcomeSignUpFormCard(
                                viewModel = viewModel,
                                translations = translations,
                                onDismiss = { showSignUpForm = false }
                            )
                        }
                        WelcomeState.HERO -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                // Premium Deen Tok circular logo
                                DeenTokLogo(
                                    modifier = Modifier.shadow(16.dp, CircleShape),
                                    sizeDp = 108.dp
                                )
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                Text(
                                    text = "Deen Tok",
                                    color = TextWhite,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 4.sp,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = translations.slogan,
                                    color = Color(0xFFD4AF37), // Elegant Gold
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = translations.description,
                                    color = TextGray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Buttons (when no forms are open)
            if (!showLoginForm && !showSignUpForm) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { showLoginForm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Black),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("welcome_login_button")
                    ) {
                        Text(
                            text = translations.loginButton,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { showSignUpForm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        border = BorderStroke(1.5.dp, CyanAccent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("welcome_signup_button")
                    ) {
                        Text(
                            text = translations.signUpButton,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "v1.1.0 Stable • Decentralized Voice Core",
                        color = TextMuted,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeLoginFormCard(
    viewModel: DeenTokViewModel,
    translations: DeenTokTranslations,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Text(
            text = translations.welcomeBackTitle,
            color = TextWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = translations.description,
            color = TextGray,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        var usernameInput by remember { mutableStateOf("@me") }
        var passwordInput by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf<String?>(null) }
        
        OutlinedTextField(
            value = usernameInput,
            onValueChange = {
                usernameInput = it
                if (it.isNotBlank()) errorText = null
            },
            label = { Text(translations.usernameLabel) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                focusedLabelColor = CyanAccent,
                unfocusedLabelColor = TextGray,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = Black,
                unfocusedContainerColor = Black
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("username_input")
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text(translations.passwordLabel) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                focusedLabelColor = CyanAccent,
                unfocusedLabelColor = TextGray,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = Black,
                unfocusedContainerColor = Black
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("password_input")
        )
        
        if (errorText != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorText!!, color = LikeRed, fontSize = 12.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray),
                border = BorderStroke(1.dp, TextGray.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("cancel_login_button")
            ) {
                Text(translations.cancelText)
            }
            
            Button(
                onClick = {
                    if (usernameInput.isBlank()) {
                        errorText = translations.validationEmptyUsername
                    } else {
                        viewModel.currentUsername = if (usernameInput.startsWith("@")) usernameInput else "@$usernameInput"
                        viewModel.currentUserAvatar = usernameInput.filter { it.isLetter() }.take(2).uppercase().ifEmpty { "ME" }
                        viewModel.logIn()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("confirm_login_button")
            ) {
                Text(translations.submitText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WelcomeSignUpFormCard(
    viewModel: DeenTokViewModel,
    translations: DeenTokTranslations,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Text(
            text = translations.createAccountTitle,
            color = TextWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = translations.description,
            color = TextGray,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        var fullNameInput by remember { mutableStateOf("") }
        var usernameInput by remember { mutableStateOf("") }
        var emailInput by remember { mutableStateOf("") }
        var bioInput by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf<String?>(null) }
        
        OutlinedTextField(
            value = fullNameInput,
            onValueChange = {
                fullNameInput = it
                if (it.isNotBlank()) errorText = null
            },
            label = { Text(translations.fullNameLabel) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                focusedLabelColor = CyanAccent,
                unfocusedLabelColor = TextGray,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = Black,
                unfocusedContainerColor = Black
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fullname_input")
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = usernameInput,
            onValueChange = {
                usernameInput = it
                if (it.isNotBlank()) errorText = null
            },
            label = { Text(translations.usernameLabel) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                focusedLabelColor = CyanAccent,
                unfocusedLabelColor = TextGray,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = Black,
                unfocusedContainerColor = Black
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_username_input")
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text(translations.emailLabel) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                focusedLabelColor = CyanAccent,
                unfocusedLabelColor = TextGray,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = Black,
                unfocusedContainerColor = Black
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_email_input")
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = bioInput,
            onValueChange = { bioInput = it },
            label = { Text(translations.bioLabel) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                focusedLabelColor = CyanAccent,
                unfocusedLabelColor = TextGray,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = Black,
                unfocusedContainerColor = Black
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_bio_input")
        )
        
        if (errorText != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorText!!, color = LikeRed, fontSize = 12.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray),
                border = BorderStroke(1.dp, TextGray.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("cancel_signup_button")
            ) {
                Text(translations.cancelText)
            }
            
            Button(
                onClick = {
                    if (fullNameInput.isBlank()) {
                        errorText = translations.validationEmptyName
                    } else if (usernameInput.isBlank()) {
                        errorText = translations.validationEmptyUsername
                    } else {
                        viewModel.currentUsername = if (usernameInput.startsWith("@")) usernameInput else "@$usernameInput"
                        viewModel.currentUserAvatar = fullNameInput.filter { it.isLetter() }.take(2).uppercase().ifEmpty { "ME" }
                        if (bioInput.isNotBlank()) {
                            viewModel.currentUserBio = bioInput
                        }
                        viewModel.signUp()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("confirm_signup_button")
            ) {
                Text(translations.signUpButton, fontWeight = FontWeight.Bold)
            }
        }
    }
}
