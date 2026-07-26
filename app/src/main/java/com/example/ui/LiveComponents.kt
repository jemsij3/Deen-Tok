package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LiveChatMessage
import com.example.data.LiveStream
import com.example.data.LiveViewer
import com.example.data.LiveModerator
import com.example.ui.theme.*
import com.example.viewmodel.DeenTokViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamScreen(
    viewModel: DeenTokViewModel,
    onBack: (() -> Unit)? = null
) {
    val activeStreams by viewModel.activeLiveStreams.collectAsState()
    val selectedStream by viewModel.selectedLiveStream.collectAsState()
    val chatMessages by viewModel.liveChatMessages.collectAsState()
    val liveViewers by viewModel.liveViewers.collectAsState()
    val liveModerators by viewModel.liveModerators.collectAsState()

    var showGoLiveDialog by remember { mutableStateOf(false) }
    var showGiftSheet by remember { mutableStateOf(false) }
    var showBuyCoinsModal by remember { mutableStateOf(false) }
    var showModControls by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showEndConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBlack)
    ) {
        if (selectedStream != null && selectedStream?.status == "LIVE") {
            val stream = selectedStream!!

            // Background Animated Video Player Simulation
            LiveStreamPlayerCanvas(stream = stream)

            // Top Header Bar Overlay
            LiveHeaderOverlay(
                stream = stream,
                viewModel = viewModel,
                onOpenGoLive = { showGoLiveDialog = true },
                onOpenModControls = { showModControls = true },
                onOpenReport = { showReportDialog = true },
                onCloseStream = {
                    val isHost = stream.creatorUserId == "current_user" || stream.creatorUsername.contains("You")
                    if (isHost) {
                        showEndConfirmDialog = true
                    } else {
                        viewModel.selectLiveStream(null)
                    }
                }
            )

            // Floating Hearts Animation overlay
            var heartClicks by remember { mutableStateOf(0) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        heartClicks++
                        viewModel.likeLiveStream(stream.id)
                    }
            )

            // Heart Particles Layer
            LiveHeartParticles(clicks = heartClicks)

            // Bottom Section: Chat overlay & Input bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Pinned Comment Banner
                if (stream.pinnedComment.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xCC1E293B),
                        border = BorderStroke(1.dp, GoldYellow.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📌", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stream.pinnedComment,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Live Chat Messages
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    LiveChatList(messages = chatMessages)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat Input & Action Buttons Bar
                LiveBottomActionBar(
                    streamId = stream.id,
                    viewModel = viewModel,
                    onOpenGifts = { showGiftSheet = true },
                    onLikeStream = {
                        heartClicks++
                        viewModel.likeLiveStream(stream.id)
                    }
                )
            }
        } else {
            // Browse Live Streams Grid if no stream selected
            LiveExploreTab(
                viewModel = viewModel,
                onOpenGoLive = { showGoLiveDialog = true }
            )
        }

        // Modals & Dialogs
        if (showGoLiveDialog) {
            GoLiveDialog(
                viewModel = viewModel,
                onDismiss = { showGoLiveDialog = false }
            )
        }

        if (showGiftSheet && selectedStream != null) {
            val stream = selectedStream!!
            GiftBottomSheet(
                viewModel = viewModel,
                targetUserId = stream.creatorUserId,
                targetUsername = stream.creatorUsername,
                videoId = 0,
                onDismiss = { showGiftSheet = false },
                onOpenBuyCoins = {
                    showGiftSheet = false
                    showBuyCoinsModal = true
                }
            )
        }

        if (showBuyCoinsModal) {
            BuyCoinsDialog(
                viewModel = viewModel,
                onDismiss = { showBuyCoinsModal = false }
            )
        }

        if (showModControls && selectedStream != null) {
            LiveModerationBottomSheet(
                stream = selectedStream!!,
                viewers = liveViewers,
                moderators = liveModerators,
                viewModel = viewModel,
                onDismiss = { showModControls = false }
            )
        }

        if (showReportDialog && selectedStream != null) {
            LiveReportDialog(
                stream = selectedStream!!,
                viewModel = viewModel,
                onDismiss = { showReportDialog = false }
            )
        }

        if (showEndConfirmDialog && selectedStream != null) {
            val stream = selectedStream!!
            AlertDialog(
                onDismissRequest = { showEndConfirmDialog = false },
                title = { Text("Cancel / End LIVE Stream", fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text("Are you sure you want to cancel and end this LIVE broadcast? All viewers will be disconnected and the live stream will close.", color = Color.LightGray, fontSize = 13.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.endLiveStream(stream.id)
                            viewModel.selectLiveStream(null)
                            showEndConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                        modifier = Modifier.testTag("confirm_cancel_live_button")
                    ) {
                        Text("Cancel Live Stream", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEndConfirmDialog = false },
                        modifier = Modifier.testTag("dismiss_cancel_live_button")
                    ) {
                        Text("Keep Live", color = Color.Gray)
                    }
                },
                containerColor = DarkNavy
            )
        }
    }
}

@Composable
fun LiveExploreTab(
    viewModel: DeenTokViewModel,
    onOpenGoLive: () -> Unit
) {
    val activeStreams by viewModel.activeLiveStreams.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Quran Recitation", "Islamic Lecture", "Q&A & Fatwa", "Nasheed & Arts")

    val filteredStreams = remember(activeStreams, selectedCategory) {
        if (selectedCategory == "All") activeStreams
        else activeStreams.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(top = 16.dp)
    ) {
        // Top Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(LiveRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE STREAMS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Broadcasts, Recitations & Lectures in real time",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onOpenGoLive,
                colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("go_live_button")
            ) {
                Icon(Icons.Default.Videocam, contentDescription = "Go LIVE", tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Go LIVE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldGreen,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color.LightGray
                    ),
                    border = BorderStroke(1.dp, if (isSelected) EmeraldGreen else Color.Gray.copy(alpha = 0.3f))
                )
            }
        }

        if (filteredStreams.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📡", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Active LIVE Streams in '$selectedCategory'",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Be the first to start a live broadcast for the community!",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenGoLive,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text("Start Broadcast Now")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredStreams, key = { it.id }) { stream ->
                    LiveStreamCard(
                        stream = stream,
                        onJoin = { viewModel.selectLiveStream(stream.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LiveStreamCard(
    stream: LiveStream,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onJoin() }
            .testTag("live_stream_card_${stream.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF004D40))
                        )
                    )
            ) {
                // Background visual style
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🕌", fontSize = 52.sp)
                        Text("DEEN TOK LIVE BROADCAST", color = GoldYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    }
                }

                // Top Pills: LIVE badge & Viewer Count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LiveRed
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("LIVE", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xAA000000)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${stream.viewerCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Bottom Category Chip
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC00897B)
                ) {
                    Text(
                        text = stream.category,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Stream Info Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stream.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "By ${stream.creatorUsername}  •  🪙 ${stream.giftCoinsEarned} Coins",
                        color = GoldYellow,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onJoin,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("JOIN LIVE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun LiveStreamPlayerCanvas(stream: LiveStream) {
    // Dynamic background visual simulation with glowing Islamic geometry
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF064E3B),
                        Color(0xFF022C22)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(pulseScale)
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = EmeraldGreen.copy(alpha = 0.2f),
                border = BorderStroke(2.dp, GoldYellow)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = when (stream.videoBgStyle) {
                            "LECTURE_BG" -> "🕌"
                            "NASHEED_BG" -> "🎵"
                            else -> "📖"
                        },
                        fontSize = 60.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stream.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "🔴 LIVE BROADCAST STREAMING",
                color = GoldYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun LiveHeaderOverlay(
    stream: LiveStream,
    viewModel: DeenTokViewModel,
    onOpenGoLive: () -> Unit,
    onOpenModControls: () -> Unit,
    onOpenReport: () -> Unit,
    onCloseStream: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Creator Badge & Follow
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xAA000000)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stream.creatorUsername.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stream.creatorUsername,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "🪙 ${stream.giftCoinsEarned} Coins",
                        color = GoldYellow,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))

                var isFollowing by remember { mutableStateOf(false) }
                Surface(
                    modifier = Modifier
                        .clickable { isFollowing = !isFollowing }
                        .testTag("live_follow_creator_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFollowing) Color.Gray else EmeraldGreen
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "+ Follow",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Action Buttons: Viewer Pill, Mod Menu, Close
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isHost = stream.creatorUserId == "current_user" || stream.creatorUsername.contains("You")

            if (isHost) {
                Button(
                    onClick = onCloseStream,
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("cancel_live_stream_button")
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Cancel Live Stream", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel Live Stream", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Button(
                    onClick = onCloseStream,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xAA000000)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("cancel_live_stream_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel Stream", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel Live", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LiveRed
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${stream.viewerCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onOpenModControls,
                modifier = Modifier
                    .background(Color(0xAA000000), CircleShape)
                    .size(36.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = "Moderation", tint = GoldYellow, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Prominent Clickable Top-Right ( × ) Button
            IconButton(
                onClick = onCloseStream,
                modifier = Modifier
                    .background(Color(0xCC000000), CircleShape)
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .size(38.dp)
                    .testTag("close_live_stream_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Live Stream",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun LiveChatList(messages: List<LiveChatMessage>) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(messages) { msg ->
            LiveChatMessageItem(msg = msg)
        }
    }
}

@Composable
fun LiveChatMessageItem(msg: LiveChatMessage) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when {
            msg.isGiftMessage -> Color(0xDD3A2E00)
            msg.isSystemMessage -> Color(0xAA0F172A)
            else -> Color(0x88000000)
        },
        border = if (msg.isGiftMessage) BorderStroke(1.dp, GoldYellow) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (msg.isGiftMessage) {
                Text(msg.giftIcon, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = msg.username,
                        color = GoldYellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = msg.message,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (msg.isSystemMessage) {
                Text("📢", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = msg.message,
                    color = GoldYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "${msg.username}: ",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = msg.message,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun LiveBottomActionBar(
    streamId: Int,
    viewModel: DeenTokViewModel,
    onOpenGifts: () -> Unit,
    onLikeStream: () -> Unit
) {
    var chatText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = chatText,
            onValueChange = { chatText = it },
            placeholder = { Text("Add a live comment...", color = Color.Gray, fontSize = 13.sp) },
            modifier = Modifier
                .weight(1f)
                .testTag("live_chat_input"),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xAA000000),
                unfocusedContainerColor = Color(0xAA000000),
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            trailingIcon = {
                if (chatText.isNotBlank()) {
                    IconButton(
                        onClick = {
                            viewModel.sendLiveChatMessage(streamId, chatText)
                            chatText = ""
                        },
                        modifier = Modifier.testTag("send_live_chat_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = EmeraldGreen)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    viewModel.sendLiveChatMessage(streamId, chatText)
                    chatText = ""
                }
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Gift Button
        IconButton(
            onClick = onOpenGifts,
            modifier = Modifier
                .background(GoldYellow, CircleShape)
                .size(44.dp)
                .testTag("live_gift_button")
        ) {
            Text("🎁", fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Like Button
        IconButton(
            onClick = onLikeStream,
            modifier = Modifier
                .background(LiveRed, CircleShape)
                .size(44.dp)
                .testTag("live_like_button")
        ) {
            Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.White)
        }
    }
}

@Composable
fun LiveHeartParticles(clicks: Int) {
    if (clicks <= 0) return

    val infiniteTransition = rememberInfiniteTransition(label = "hearts")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "heartUp"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp, end = 24.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Text(
            text = "💖",
            fontSize = 32.sp,
            modifier = Modifier.offset(y = offsetY.dp)
        )
    }
}

@Composable
fun GoLiveDialog(
    viewModel: DeenTokViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Quran Recitation") }
    val categories = listOf("Quran Recitation", "Islamic Lecture", "Q&A & Fatwa", "Nasheed & Arts", "Daily Reminders")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔴", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start LIVE Broadcast", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("LIVE Stream Title") },
                    placeholder = { Text("e.g. Evening Quran Reflection & Tafsir") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("go_live_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Text("Category:", fontWeight = FontWeight.Bold, color = GoldYellow, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = cat == selectedCategory,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.startLiveStream(
                            title = title,
                            description = description,
                            category = selectedCategory
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                modifier = Modifier.testTag("confirm_go_live_button")
            ) {
                Text("Start LIVE Stream", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333348), contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("cancel_go_live_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancel Live Stream", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkNavy
    )
}

@Composable
fun LiveModerationBottomSheet(
    stream: LiveStream,
    viewers: List<LiveViewer>,
    moderators: List<LiveModerator>,
    viewModel: DeenTokViewModel,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = DarkNavy
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = GoldYellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LIVE Moderation & Viewers", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Pin Comment
            var pinInput by remember { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    placeholder = { Text("Pin a comment...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (pinInput.isNotBlank()) {
                            viewModel.pinLiveChatMessage(stream.id, pinInput)
                            pinInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Pin")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Active Viewers (${viewers.size})", fontWeight = FontWeight.Bold, color = GoldYellow, fontSize = 13.sp)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewers) { viewer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(viewer.username, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        Row {
                            IconButton(
                                onClick = { viewModel.muteUserInLive(stream.id, viewer.userId, viewer.username) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.VolumeOff, contentDescription = "Mute", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { viewModel.blockUserInLive(stream.id, viewer.userId, viewer.username) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = "Block", tint = LiveRed, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { viewModel.assignLiveModerator(stream.id, viewer.userId, viewer.username) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = "Add Mod", tint = GoldYellow, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.endLiveStream(stream.id)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("END LIVE STREAM NOW", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LiveReportDialog(
    stream: LiveStream,
    viewModel: DeenTokViewModel,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    val reasons = listOf("Inappropriate Content", "Harassment / Spam", "Misleading Information", "Copyright Issue")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report LIVE Stream", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select reason for reporting '${stream.title}':", fontSize = 12.sp, color = Color.LightGray)
                reasons.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = r }
                            .background(if (reason == r) EmeraldGreen.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (reason == r), onClick = { reason = r })
                        Text(r, color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) {
                        viewModel.reportLiveStream(
                            streamId = stream.id,
                            streamTitle = stream.title,
                            reportedUserId = stream.creatorUserId,
                            reportedUsername = stream.creatorUsername,
                            reason = reason
                        )
                        onDismiss()
                    }
                },
                enabled = reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = Color.Gray) }
        },
        containerColor = DarkNavy
    )
}
