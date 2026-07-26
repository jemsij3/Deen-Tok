package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.DeenTokViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AdminTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Home),
    LIVE_MODERATION("LIVE Streams", Icons.Default.Videocam),
    USERS("Users", Icons.Default.Person),
    CREATORS("Creators", Icons.Default.Face),
    VIDEOS("Videos", Icons.Default.PlayArrow),
    COMMENTS("Comments", Icons.Default.Email),
    REPORTS("Reports", Icons.Default.Warning),
    CATEGORIES("Categories", Icons.Default.List),
    HASHTAGS("Hashtags", Icons.Default.Star),
    ADVERTISEMENTS("Advertisements", Icons.Default.Share),
    TEMPLATES("Templates", Icons.Default.Layers),
    NOTIFICATIONS("Notifications", Icons.Default.Notifications),
    LANGUAGES("Languages", Icons.Default.Info),
    CONTENT_MODERATION("Content Moderation", Icons.Default.Lock),
    ANALYTICS("Analytics", Icons.Default.TrendingUp),
    SYSTEM_SETTINGS("System Settings", Icons.Default.Settings),
    ADMIN_ACCOUNTS("Admin Accounts", Icons.Default.Person),
    ACTIVITY_LOGS("Activity Logs", Icons.Default.Lock),
    BACKUP_RESTORE("Backup & Restore", Icons.Default.Refresh),
    HELP_CENTER("Help Center", Icons.Default.Info)
}

@Composable
fun AdminDashboardScreen(viewModel: DeenTokViewModel) {
    var isUnlocked by remember(viewModel.currentUserRole) { 
        mutableStateOf(viewModel.hasAdminAccess()) 
    }

    if (!isUnlocked) {
        AdminSecurityGate(
            onUnlock = { isUnlocked = true },
            viewModel = viewModel
        )
    } else {
        AdminDashboardMain(viewModel = viewModel)
    }
}

@Composable
fun AdminSecurityGate(
    onUnlock: () -> Unit,
    viewModel: DeenTokViewModel
) {
    var adminUser by remember { mutableStateOf(viewModel.currentUsername) }
    var adminPasscode by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var activeField by remember { mutableStateOf(1) } // Default to passcode (1) since user is @me by default
    val focusManager = LocalFocusManager.current

    val attemptUnlock = {
        if (viewModel.hasAdminAccess() || adminPasscode == "admin123" || adminPasscode == "DEENTOK-ADMIN-2026") {
            viewModel.logAdminAction("LOGIN_SUCCESS", "Admin gateway verified successfully for account: ${viewModel.currentUsername}")
            onUnlock()
        } else {
            viewModel.logAdminAction("LOGIN_FAILURE", "Unauthorized login attempt using account name: $adminUser")
            showError = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Gateway",
                        tint = CyanAccent,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "DEENTOK SECURITY TERMINAL",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Restricted Administrator Gate",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )

                Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

                // Input Field 1: Admin User
                OutlinedTextField(
                    value = adminUser,
                    onValueChange = { 
                        adminUser = it 
                        showError = false
                    },
                    label = { 
                        Text(
                            text = if (activeField == 0) "✦ Admin Account Name" else "Admin Account Name", 
                            color = if (activeField == 0) CyanAccent else TextGray,
                            fontWeight = if (activeField == 0) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = if (activeField == 0) CyanAccent.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) activeField = 0 }
                )

                // Input Field 2: Admin Passcode
                OutlinedTextField(
                    value = adminPasscode,
                    onValueChange = { 
                        adminPasscode = it 
                        showError = false
                    },
                    label = { 
                        Text(
                            text = if (activeField == 1) "✦ Security Access Key" else "Security Access Key", 
                            color = if (activeField == 1) CyanAccent else TextGray,
                            fontWeight = if (activeField == 1) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = if (activeField == 1) CyanAccent.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) activeField = 1 }
                )

                if (showError) {
                    Text(
                        text = "ACCESS DENIED: Invalid credentials.",
                        color = LikeRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Visual target active indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariantDark, RoundedCornerShape(6.dp))
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Text(
                        text = "Keyboard Target: ",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                    Text(
                        text = if (activeField == 0) "ADMIN ACCOUNT NAME" else "SECURITY ACCESS KEY",
                        fontSize = 11.sp,
                        color = if (activeField == 0) CyanAccent else Color.Yellow,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Preset Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            adminUser = "@me"
                            adminPasscode = "admin123"
                            activeField = 1
                            showError = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DEMO PASS", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Autofill admin123", color = TextGray, fontSize = 8.sp)
                        }
                    }

                    Button(
                        onClick = {
                            adminUser = "@admin"
                            adminPasscode = "DEENTOK-ADMIN-2026"
                            activeField = 1
                            showError = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color.Magenta.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MASTER PASS", color = Color.Magenta, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Autofill master key", color = TextGray, fontSize = 8.sp)
                        }
                    }
                }

                // VIRTUAL TERMINAL KEYBOARD
                var isShiftEnabled by remember { mutableStateOf(false) }

                val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                val row2 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
                val row3 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "-")
                val row4 = listOf("z", "x", "c", "v", "b", "n", "m", "@", ".", "_")

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row1.forEach { num ->
                            KeyButton(
                                text = num,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (activeField == 0) adminUser += num else adminPasscode += num
                                }
                            )
                        }
                    }

                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row2.forEach { char ->
                            val keyText = if (isShiftEnabled) char.uppercase() else char.lowercase()
                            KeyButton(
                                text = keyText,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (activeField == 0) adminUser += keyText else adminPasscode += keyText
                                }
                            )
                        }
                    }

                    // Row 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row3.forEach { char ->
                            val keyText = if (isShiftEnabled) char.uppercase() else char.lowercase()
                            KeyButton(
                                text = keyText,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (activeField == 0) adminUser += keyText else adminPasscode += keyText
                                }
                            )
                        }
                    }

                    // Row 4
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row4.forEach { char ->
                            val keyText = if (isShiftEnabled) char.uppercase() else char.lowercase()
                            KeyButton(
                                text = keyText,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (activeField == 0) adminUser += keyText else adminPasscode += keyText
                                }
                            )
                        }
                    }

                    // Row 5: Shift, Space, Backspace, Clear
                    var lastActionTime by remember { mutableStateOf(0L) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { 
                                val now = System.currentTimeMillis()
                                if (now - lastActionTime > 150L) {
                                    lastActionTime = now
                                    isShiftEnabled = !isShiftEnabled 
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isShiftEnabled) CyanAccent else SurfaceVariantDark
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(38.dp)
                        ) {
                            Text(
                                text = "SHIFT",
                                color = if (isShiftEnabled) Black else TextWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                val now = System.currentTimeMillis()
                                if (now - lastActionTime > 150L) {
                                    lastActionTime = now
                                    if (activeField == 0) adminUser += " " else adminPasscode += " "
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(2.5f)
                                .height(38.dp)
                        ) {
                            Text(text = "SPACE", color = TextWhite, fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                val now = System.currentTimeMillis()
                                if (now - lastActionTime > 150L) {
                                    lastActionTime = now
                                    if (activeField == 0) {
                                        if (adminUser.isNotEmpty()) adminUser = adminUser.dropLast(1)
                                    } else {
                                        if (adminPasscode.isNotEmpty()) adminPasscode = adminPasscode.dropLast(1)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LikeRed.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, LikeRed.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Backspace",
                                tint = LikeRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (activeField == 0) adminUser = "" else adminPasscode = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(38.dp)
                        ) {
                            Text(text = "CLR", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { attemptUnlock() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_gate_unlock")
                ) {
                    Text("AUTHORIZE TERMINAL ACCESS", color = Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun KeyButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var lastClickTime by remember { mutableStateOf(0L) }

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceVariantDark)
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .clickable {
                val now = System.currentTimeMillis()
                if (now - lastClickTime > 150L) {
                    lastClickTime = now
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TextWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AdminDashboardMain(viewModel: DeenTokViewModel) {
    var selectedTab by remember { mutableStateOf(AdminTab.DASHBOARD) }
    val trans = getTranslations(viewModel)
    
    // Database flows reactively collected
    val allVideos by viewModel.allVideos.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val allReports by viewModel.allReports.collectAsState()
    val allAdvertisements by viewModel.allAdvertisements.collectAsState()
    val allAdminLogs by viewModel.allAdminLogs.collectAsState()
    val allComments by viewModel.allComments.collectAsState()
    val removedVideos by viewModel.removedVideos.collectAsState()

    // Platform config collected
    val platformName by viewModel.platformAppName.collectAsState()
    val platformLogoText by viewModel.platformLogoText.collectAsState()
    val privacySettings by viewModel.privacySettings.collectAsState()
    val communityGuidelines by viewModel.communityGuidelines.collectAsState()
    val adminNotificationSettings by viewModel.adminNotificationSettings.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        val isMobile = maxWidth < 700.dp

        if (isMobile) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = SurfaceDark,
                        drawerContentColor = TextWhite,
                        modifier = Modifier.width(280.dp)
                    ) {
                        AdminSidebarContent(
                            selectedTab = selectedTab,
                            onTabSelected = { tab ->
                                selectedTab = tab
                                coroutineScope.launch { drawerState.close() }
                            },
                            platformLogoText = platformLogoText,
                            platformName = platformName,
                            trans = trans,
                            onBackToProfile = { viewModel.setScreen(Screen.PROFILE) }
                        )
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Black)
                ) {
                    // Mobile TopBar Header
                    Surface(
                        color = SurfaceDark,
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(
                                    onClick = { coroutineScope.launch { drawerState.open() } }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Open Admin Menu",
                                        tint = CyanAccent
                                    )
                                }

                                Column {
                                    Text(
                                        text = selectedTab.getLocalizedTitle(trans).uppercase(),
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$platformName Console",
                                        color = TextGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .background(Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(CyanAccent)
                                    )
                                    Text("LIVE", color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = { viewModel.setScreen(Screen.PROFILE) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Exit to Profile",
                                        tint = TextWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Content Pane (Mobile Padding)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        AdminTabRouter(
                            selectedTab = selectedTab,
                            viewModel = viewModel,
                            allVideos = allVideos,
                            allUsers = allUsers,
                            allComments = allComments,
                            allReports = allReports,
                            allAdvertisements = allAdvertisements,
                            removedVideos = removedVideos,
                            allCategories = allCategories,
                            allAdminLogs = allAdminLogs,
                            privacySettings = privacySettings,
                            communityGuidelines = communityGuidelines,
                            adminNotificationSettings = adminNotificationSettings
                        )
                    }
                }
            }
        } else {
            // Desktop / Tablet Layout
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .background(SurfaceDark)
                ) {
                    AdminSidebarContent(
                        selectedTab = selectedTab,
                        onTabSelected = { tab -> selectedTab = tab },
                        platformLogoText = platformLogoText,
                        platformName = platformName,
                        trans = trans,
                        onBackToProfile = { viewModel.setScreen(Screen.PROFILE) }
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.05f))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedTab.getLocalizedTitle(trans).uppercase(),
                                color = TextWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Real-time SQLite synchronized console",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(SurfaceDark, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent)
                            )
                            Text("LIVE FEED", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        AdminTabRouter(
                            selectedTab = selectedTab,
                            viewModel = viewModel,
                            allVideos = allVideos,
                            allUsers = allUsers,
                            allComments = allComments,
                            allReports = allReports,
                            allAdvertisements = allAdvertisements,
                            removedVideos = removedVideos,
                            allCategories = allCategories,
                            allAdminLogs = allAdminLogs,
                            privacySettings = privacySettings,
                            communityGuidelines = communityGuidelines,
                            adminNotificationSettings = adminNotificationSettings
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSidebarContent(
    selectedTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit,
    platformLogoText: String,
    platformName: String,
    trans: DeenTokTranslations,
    onBackToProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        // Header Branding
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 20.dp, start = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyanAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = platformLogoText,
                    color = Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Column {
                Text(
                    text = platformName,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "System Admin Panel",
                    color = CyanAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Scrollable List of Tabs
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AdminTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val localizedTitle = tab.getLocalizedTitle(trans)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyanAccent.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 11.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = localizedTitle,
                        tint = if (isSelected) CyanAccent else TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = localizedTitle,
                        color = if (isSelected) TextWhite else TextGray,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))

        // Back to App Feed Action
        Button(
            onClick = onBackToProfile,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return", tint = TextWhite, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("App Profile", color = TextWhite, fontSize = 12.sp)
        }
    }
}

@Composable
fun AdminTabRouter(
    selectedTab: AdminTab,
    viewModel: DeenTokViewModel,
    allVideos: List<Video>,
    allUsers: List<User>,
    allComments: List<Comment>,
    allReports: List<Report>,
    allAdvertisements: List<Advertisement>,
    removedVideos: List<Video>,
    allCategories: List<Category>,
    allAdminLogs: List<AdminLog>,
    privacySettings: String,
    communityGuidelines: String,
    adminNotificationSettings: String
) {
    when (selectedTab) {
        AdminTab.DASHBOARD -> DashboardTabContent(allVideos, allUsers, allComments, allReports, allAdvertisements)
        AdminTab.LIVE_MODERATION -> LiveModerationTabContent(viewModel)
        AdminTab.USERS -> UsersManagementTabContent(allUsers, allVideos, viewModel)
        AdminTab.CREATORS -> CreatorsManagementTabContent(allUsers, allVideos, viewModel)
        AdminTab.VIDEOS -> VideosManagementTabContent(allVideos, removedVideos, viewModel)
        AdminTab.COMMENTS -> CommentsManagementTabContent(allComments, viewModel)
        AdminTab.REPORTS -> ReportsManagementTabContent(allReports, viewModel)
        AdminTab.CATEGORIES -> CategoriesManagementTabContent(allCategories, viewModel)
        AdminTab.HASHTAGS -> HashtagsManagementTabContent(allVideos, viewModel)
        AdminTab.ADVERTISEMENTS -> AdvertisementsManagementTabContent(allAdvertisements, viewModel)
        AdminTab.TEMPLATES -> TemplatesManagementTabContent(viewModel)
        AdminTab.NOTIFICATIONS -> NotificationsManagementTabContent(allAdminLogs, viewModel)
        AdminTab.LANGUAGES -> LanguagesManagementTabContent(viewModel)
        AdminTab.CONTENT_MODERATION -> ContentModerationTabContent(allVideos, allComments, viewModel)
        AdminTab.ANALYTICS -> AnalyticsTabContent(allVideos, allUsers, allComments)
        AdminTab.SYSTEM_SETTINGS -> SystemSettingsManagementTabContent(viewModel)
        AdminTab.ADMIN_ACCOUNTS -> AdminAccountsTabContent(allUsers, viewModel)
        AdminTab.ACTIVITY_LOGS -> ActivityLogsTabContent(allAdminLogs)
        AdminTab.BACKUP_RESTORE -> BackupRestoreTabContent(viewModel)
        AdminTab.HELP_CENTER -> HelpCenterTabContent()
    }
}

// ==================== 1. DASHBOARD ====================
@Composable
fun DashboardTabContent(
    videos: List<Video>,
    users: List<User>,
    comments: List<Comment>,
    reports: List<Report>,
    ads: List<Advertisement>
) {
    val totalUsers = users.size
    val onlineUsers = remember { (totalUsers * 0.4).toInt().coerceAtLeast(1) }
    val totalCreators = users.count { it.role == "CREATOR" }
    val totalVideos = videos.size
    val videosUploadedToday = videos.count { it.createdAt > System.currentTimeMillis() - 86400000 }
    val totalViews = videos.sumOf { it.viewsCount }
    val totalLikes = videos.sumOf { it.likesCount }
    val totalComments = comments.size
    val totalShares = videos.sumOf { it.sharesCount }
    val reportsPending = reports.count { it.status == "PENDING" }
    val adsRunning = ads.count { it.status == "ACTIVE" }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMobile = maxWidth < 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(if (isMobile) 130.dp else 160.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = if (isMobile) 650.dp else 400.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { DashboardCard("Total Users", "$totalUsers", "Registered Users", CyanAccent) }
                item { DashboardCard("Online Users", "$onlineUsers", "Active Sessions", BlueAccent) }
                item { DashboardCard("Total Creators", "$totalCreators", "Verified Partners", TextWhite) }
                item { DashboardCard("Total Videos", "$totalVideos", "System Streams", CyanAccent) }
                item { DashboardCard("Videos Today", "$videosUploadedToday", "New uploads", BlueAccent) }
                item { DashboardCard("Total Views", "${totalViews}k", "Accumulated views", TextWhite) }
                item { DashboardCard("Total Likes", "$totalLikes", "Accumulated reactions", LikeRed) }
                item { DashboardCard("Total Comments", "$totalComments", "User comments", CyanAccent) }
                item { DashboardCard("Total Shares", "$totalShares", "Outbound sharing", BlueAccent) }
                item { DashboardCard("Pending Reports", "$reportsPending", "Action required", LikeRed) }
                item { DashboardCard("Running Ads", "$adsRunning", "Active campaigns", TextWhite) }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Custom Canvas Charts
            if (isMobile) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ChartCard("Platform User Growth", listOf(10f, 25f, 40f, 60f, 85f, 110f, 140f))
                    ChartCard("Daily Content Uploads", listOf(4f, 12f, 8f, 25f, 15f, 32f, 45f))
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ChartCard("Platform User Growth", listOf(10f, 25f, 40f, 60f, 85f, 110f, 140f))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ChartCard("Daily Content Uploads", listOf(4f, 12f, 8f, 25f, 15f, 32f, 45f))
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(label: String, value: String, subtitle: String, accentColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = accentColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = TextMuted, fontSize = 9.sp)
        }
    }
}

@Composable
fun ChartCard(title: String, points: List<Float>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(14.dp))
            
            Canvas(modifier = Modifier.fillMaxSize().weight(1f)) {
                val width = size.width
                val height = size.height
                val maxPoint = points.maxOrNull() ?: 1f
                
                val path = Path()
                points.forEachIndexed { i, pt ->
                    val x = i * (width / (points.size - 1))
                    val y = height - (pt / maxPoint) * height * 0.8f
                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                    drawCircle(CyanAccent, 4.dp.toPx(), Offset(x, y))
                }
                
                drawPath(
                    path = path,
                    color = CyanAccent,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

// ==================== 2. USERS ====================
@Composable
fun UsersManagementTabContent(users: List<User>, videos: List<Video>, viewModel: DeenTokViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("ALL") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var editUserTarget by remember { mutableStateOf<User?>(null) }
    var showResetPasswordUser by remember { mutableStateOf<User?>(null) }
    var deleteConfirmUser by remember { mutableStateOf<User?>(null) }

    val filteredUsers = users.filter {
        it.username.contains(searchQuery, ignoreCase = true) &&
        (selectedRoleFilter == "ALL" || it.role == selectedRoleFilter) &&
        (selectedStatusFilter == "ALL" || it.status == selectedStatusFilter)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by username...", color = TextGray, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.widthIn(min = 180.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Role Filters
            FilterDropdown("Role: $selectedRoleFilter", listOf("ALL", "USER", "CREATOR", "ADMIN")) {
                selectedRoleFilter = it
            }

            // Status Filters
            FilterDropdown("Status: $selectedStatusFilter", listOf("ALL", "ACTIVE", "SUSPENDED")) {
                selectedStatusFilter = it
            }
        }

        // Table
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.widthIn(min = 550.dp)) {
                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Black.copy(alpha = 0.3f)).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("User Info", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text("Role", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Status", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Actions", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                    items(filteredUsers) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User Column
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(user.username, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("ID: ${user.userId}", color = TextMuted, fontSize = 10.sp)
                            }
                            
                            // Role
                            Box(modifier = Modifier.weight(1f)) {
                                Text(
                                    user.role,
                                    color = if (user.role == "ADMIN") CyanAccent else if (user.role == "CREATOR") BlueAccent else TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Status
                            Box(modifier = Modifier.weight(1f)) {
                                Text(
                                    user.status,
                                    color = if (user.status == "ACTIVE") Color.Green else LikeRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Action buttons
                            Row(
                                modifier = Modifier.weight(2f),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { editUserTarget = user }) {
                                    Icon(Icons.Default.Edit, "Edit User", tint = CyanAccent, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { showResetPasswordUser = user }) {
                                    Icon(Icons.Default.Lock, "Reset Pass", tint = BlueAccent, modifier = Modifier.size(18.dp))
                                }
                                if (user.status == "ACTIVE") {
                                    IconButton(onClick = { viewModel.suspendUser(user) }) {
                                        Icon(Icons.Default.Block, "Suspend", tint = LikeRed, modifier = Modifier.size(18.dp))
                                    }
                                } else {
                                    IconButton(onClick = { viewModel.activateUser(user) }) {
                                        Icon(Icons.Default.Check, "Activate", tint = Color.Green, modifier = Modifier.size(18.dp))
                                    }
                                }
                                IconButton(onClick = { deleteConfirmUser = user }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = LikeRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Edit Profile Dialog
    editUserTarget?.let { user ->
        var editName by remember { mutableStateOf(user.username) }
        var editBio by remember { mutableStateOf(user.bio) }
        var editWebsite by remember { mutableStateOf(user.website) }
        var editRole by remember { mutableStateOf(user.role) }

        AlertDialog(
            onDismissRequest = { editUserTarget = null },
            title = { Text("Edit Profile: ${user.username}", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Username", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                    )
                    OutlinedTextField(
                        value = editWebsite,
                        onValueChange = { editWebsite = it },
                        label = { Text("Website", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                    )
                    Text("Role Selection", color = TextGray, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("USER", "CREATOR", "ADMIN").forEach { role ->
                            FilterChip(
                                selected = editRole == role,
                                onClick = { editRole = role },
                                label = { Text(role) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserDirectly(
                            user.copy(username = editName, bio = editBio, website = editWebsite, role = editRole)
                        )
                        editUserTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Save Changes", color = Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { editUserTarget = null }) {
                    Text("Cancel", color = TextWhite)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Reset Password Dialog
    showResetPasswordUser?.let { user ->
        var newPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showResetPasswordUser = null },
            title = { Text("Reset Password: ${user.username}", color = TextWhite) },
            text = {
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("New Security Password", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetUserPassword(user.userId, newPass)
                        showResetPasswordUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Confirm Reset", color = Black)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Confirm Delete Dialog
    deleteConfirmUser?.let { user ->
        AlertDialog(
            onDismissRequest = { deleteConfirmUser = null },
            title = { Text("Confirm Deletion", color = LikeRed, fontWeight = FontWeight.Bold) },
            text = { Text("Are you absolutely certain you want to purge user '${user.username}' from the Deen Tok local datastore?", color = TextWhite) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(user)
                        deleteConfirmUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LikeRed)
                ) {
                    Text("Purge Profile", color = TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmUser = null }) {
                    Text("Cancel", color = TextWhite)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

// Helper Dropdown
@Composable
fun FilterDropdown(label: String, items: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Text(label, color = TextWhite, fontSize = 12.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceDark)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = TextWhite) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ==================== 3. CREATORS ====================
@Composable
fun CreatorsManagementTabContent(users: List<User>, videos: List<Video>, viewModel: DeenTokViewModel) {
    val creators = users.filter { it.role == "CREATOR" || it.isVerified }
    var selectedCreatorForAnalytics by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Registered Creators", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState())) {
                    Column(modifier = Modifier.widthIn(min = 520.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(creators) { creator ->
                                val creatorVideos = videos.filter { it.userId == creator.userId }
                                val likes = creatorVideos.sumOf { it.likesCount }
                                val views = creatorVideos.sumOf { it.viewsCount }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(creator.username, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (creator.isVerified) {
                                                Icon(Icons.Default.CheckCircle, "Verified", tint = CyanAccent, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Text("Videos: ${creatorVideos.size}  | Likes: $likes | Views: $views", color = TextGray, fontSize = 11.sp)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { selectedCreatorForAnalytics = creator },
                                            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text("Analytics", color = TextWhite, fontSize = 10.sp)
                                        }

                                        if (creator.isVerified) {
                                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(creator.verificationType, color = CyanAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Button(
                                                    onClick = { viewModel.verifyCreator(creator, false) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = LikeRed),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(26.dp)
                                                ) {
                                                    Text("Revoke", color = TextWhite, fontSize = 10.sp)
                                                }
                                            }
                                        } else {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Button(
                                                    onClick = { viewModel.verifyCreator(creator, true, "Verified Scholar") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(26.dp)
                                                ) {
                                                    Text("Scholar 🎓", color = Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = { viewModel.verifyCreator(creator, true, "Islamic Organization") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(26.dp)
                                                ) {
                                                    Text("Org 🕌", color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = { viewModel.verifyCreator(creator, true, "Verified Creator") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(26.dp)
                                                ) {
                                                    Text("Creator ✅", color = Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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

    // Creator Analytics Dialog
    selectedCreatorForAnalytics?.let { creator ->
        val creatorVideos = videos.filter { it.userId == creator.userId }
        AlertDialog(
            onDismissRequest = { selectedCreatorForAnalytics = null },
            title = { Text("${creator.username}'s Analytics", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Bio: ${creator.bio}", color = TextGray, fontSize = 12.sp)
                    Text("Website: ${creator.website}", color = TextGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Uploaded Content Streams:", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyColumn(modifier = Modifier.height(150.dp)) {
                        items(creatorVideos) { vid ->
                            Text("- ${vid.title} (${vid.likesCount} Likes, ${vid.viewsCount} Views)", color = TextWhite, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCreatorForAnalytics = null }) {
                    Text("Close", color = CyanAccent)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

// ==================== 4. VIDEOS ====================
@Composable
fun VideosManagementTabContent(videos: List<Video>, removedVideos: List<Video>, viewModel: DeenTokViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var editVideoTarget by remember { mutableStateOf<Video?>(null) }
    var showPlayerVideo by remember { mutableStateOf<Video?>(null) }

    val filteredVideos = videos.filter {
        (it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)) &&
        (selectedCategoryFilter == "ALL" || it.category == selectedCategoryFilter)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search videos...", color = TextGray, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                modifier = Modifier.widthIn(min = 180.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            FilterDropdown("Category: $selectedCategoryFilter", listOf("ALL", "Tech", "Music", "Cooking", "Lifestyle", "Comedy")) {
                selectedCategoryFilter = it
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredVideos) { video ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(video.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Uploaded by: ${video.username} | Category: ${video.category}", color = TextGray, fontSize = 11.sp)
                                Text("Likes: ${video.likesCount} | Views: ${video.viewsCount}", color = TextMuted, fontSize = 10.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(onClick = { showPlayerVideo = video }) {
                                    Icon(Icons.Default.PlayArrow, "Watch", tint = CyanAccent)
                                }
                                IconButton(onClick = { editVideoTarget = video }) {
                                    Icon(Icons.Default.Edit, "Edit", tint = BlueAccent)
                                }
                                IconButton(onClick = { viewModel.removeVideoAsAdmin(video) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = LikeRed)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Removed Videos (Restore pane)
        if (removedVideos.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.height(150.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Purged / Moderated Streams (${removedVideos.size})", color = LikeRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn {
                        items(removedVideos) { vid ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(vid.title, color = TextWhite, fontSize = 12.sp)
                                Button(
                                    onClick = { viewModel.restoreVideoAsAdmin(vid) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Restore Stream", color = Black, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Video preview dialog
    showPlayerVideo?.let { video ->
        AlertDialog(
            onDismissRequest = { showPlayerVideo = null },
            title = { Text(video.title, color = TextWhite) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, "Playing", tint = CyanAccent, modifier = Modifier.size(48.dp))
                    }
                    Text("Description: ${video.description}", color = TextWhite, fontSize = 12.sp)
                    Text("Location URL: ${video.videoUrl}", color = TextGray, fontSize = 10.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlayerVideo = null }) {
                    Text("Close Preview", color = CyanAccent)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Edit video info
    editVideoTarget?.let { video ->
        var editTitle by remember { mutableStateOf(video.title) }
        var editDesc by remember { mutableStateOf(video.description) }
        var editCategory by remember { mutableStateOf(video.category) }

        AlertDialog(
            onDismissRequest = { editVideoTarget = null },
            title = { Text("Edit Stream Info", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                    )
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Description", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                    )
                    OutlinedTextField(
                        value = editCategory,
                        onValueChange = { editCategory = it },
                        label = { Text("Category", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateVideoDirectly(
                            video.copy(title = editTitle, description = editDesc, category = editCategory)
                        )
                        editVideoTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Save Video", color = Black)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

// ==================== 5. COMMENTS ====================
@Composable
fun CommentsManagementTabContent(comments: List<Comment>, viewModel: DeenTokViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredComments = comments.filter {
        it.text.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search comments by content or author...", color = TextGray) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredComments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(comment.username, color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(comment.text, color = TextWhite, fontSize = 13.sp)
                            }
                            
                            IconButton(onClick = { viewModel.deleteCommentDirectly(comment) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = LikeRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 6. REPORTS ====================
@Composable
fun ReportsManagementTabContent(reports: List<Report>, viewModel: DeenTokViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Safety Reports Pending", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState())) {
                    Column(modifier = Modifier.widthIn(min = 480.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(reports) { report ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Type: ${report.type} | ID: ${report.contentId}", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Reason: ${report.reason}", color = TextWhite, fontSize = 13.sp)
                                        Text("Reported By: ${report.reportedBy} | Status: ${report.status}", color = TextGray, fontSize = 11.sp)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { viewModel.resolveReport(report, "RESOLVED_REMOVED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = LikeRed),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Remove Content", color = TextWhite, fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.resolveReport(report, "CLOSED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Close", color = TextWhite, fontSize = 10.sp)
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

// ==================== 7. CATEGORIES ====================
@Composable
fun CategoriesManagementTabContent(categories: List<Category>, viewModel: DeenTokViewModel) {
    var newCatName by remember { mutableStateOf("") }
    var isTrending by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Create Category Pane
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Create Category", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        placeholder = { Text("Category Name...", color = TextGray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                        modifier = Modifier.widthIn(min = 160.dp),
                        singleLine = true
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isTrending, onCheckedChange = { isTrending = it })
                        Text("Trending", color = TextWhite, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (newCatName.isNotBlank()) {
                                viewModel.addCategory(newCatName, isTrending)
                                newCatName = ""
                                isTrending = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Text("Add", color = Black)
                    }
                }
            }
        }

        // Category Table
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("App Content Categories", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${category.name} ${if (category.isTrending) "🔥" else ""}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            IconButton(onClick = { viewModel.deleteCategory(category) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = LikeRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 8. HASHTAGS ====================
@Composable
fun HashtagsManagementTabContent(videos: List<Video>, viewModel: DeenTokViewModel) {
    val hashtags = remember(videos) {
        val list = mutableMapOf<String, Int>()
        videos.forEach { video ->
            val tags = video.description.split(" ").filter { it.startsWith("#") }
            tags.forEach { tag ->
                list[tag] = (list[tag] ?: 0) + 1
            }
        }
        list.toList().sortedByDescending { it.second }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Hashtags & Trend Metrics", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(hashtags) { (tag, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tag, color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("$count video streams", color = TextGray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==================== 9. ADVERTISEMENTS ====================
@Composable
fun AdvertisementsManagementTabContent(ads: List<Advertisement>, viewModel: DeenTokViewModel) {
    var adTitle by remember { mutableStateOf("") }
    var adImgUrl by remember { mutableStateOf("") }
    var adTargetUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Create Advertisement Campaign", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                
                OutlinedTextField(
                    value = adTitle,
                    onValueChange = { adTitle = it },
                    label = { Text("Campaign Name / Title", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = adImgUrl,
                    onValueChange = { adImgUrl = it },
                    label = { Text("Banner Image URL", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = adTargetUrl,
                    onValueChange = { adTargetUrl = it },
                    label = { Text("Destination Action URL", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (adTitle.isNotBlank() && adImgUrl.isNotBlank()) {
                            viewModel.addAdvertisement(adTitle, adImgUrl, adTargetUrl, "ACTIVE")
                            adTitle = ""
                            adImgUrl = ""
                            adTargetUrl = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Launch Campaign", color = Black)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Active Campaigns", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ads) { ad ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(ad.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Target: ${ad.targetUrl}", color = TextGray, fontSize = 11.sp)
                                Text("Status: ${ad.status}", color = if (ad.status == "ACTIVE") Color.Green else LikeRed, fontSize = 10.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.toggleAdStatus(ad) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Toggle Status", color = TextWhite, fontSize = 10.sp)
                                }
                                IconButton(onClick = { viewModel.deleteAdvertisement(ad) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = LikeRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 10. NOTIFICATIONS ====================
@Composable
fun NotificationsManagementTabContent(logs: List<AdminLog>, viewModel: DeenTokViewModel) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var targetGroup by remember { mutableStateOf("ALL") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Create System Broadcast", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Alert Heading", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Message Body", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Target Group:", color = TextWhite, fontSize = 12.sp)
                    listOf("ALL", "CREATORS", "USERS").forEach { grp ->
                        FilterChip(
                            selected = targetGroup == grp,
                            onClick = { targetGroup = grp },
                            label = { Text(grp) }
                        )
                    }
                }

                Button(
                    onClick = {
                        if (title.isNotBlank() && desc.isNotBlank()) {
                            viewModel.broadcastSystemNotification(title, desc, targetGroup)
                            title = ""
                            desc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Dispatch Notification", color = Black)
                }
            }
        }
    }
}

// ==================== 11. LANGUAGES ====================
@Composable
fun LanguagesManagementTabContent(viewModel: DeenTokViewModel) {
    val languages = listOf("English", "Afaan Oromoo", "Amharic")
    var currentLang by remember { mutableStateOf("English") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Localization & Translating Keys", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))

                languages.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentLang = lang }
                            .background(if (currentLang == lang) CyanAccent.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(lang, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (currentLang == lang) {
                            Icon(Icons.Default.Check, "Active", tint = CyanAccent)
                        }
                    }
                }
            }
        }
    }
}

// ==================== 12. CONTENT MODERATION ====================
@Composable
fun ContentModerationTabContent(videos: List<Video>, comments: List<Comment>, viewModel: DeenTokViewModel) {
    var blockedWord by remember { mutableStateOf("") }
    val blockedWords = remember { mutableStateListOf("spam", "scam", "abusive", "unreleased") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Banned Words Blacklist", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = blockedWord,
                        onValueChange = { blockedWord = it },
                        placeholder = { Text("Enter forbidden word...", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (blockedWord.isNotBlank()) {
                                blockedWords.add(blockedWord.lowercase())
                                viewModel.logAdminAction("BAN_WORD", "Added forbidden moderation word: $blockedWord")
                                blockedWord = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Text("Add Word", color = Black)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    blockedWords.forEach { word ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(word, color = TextWhite, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close, "Remove", tint = LikeRed,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            blockedWords.remove(word)
                                            viewModel.logAdminAction("UNBAN_WORD", "Removed forbidden moderation word: $word")
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 13. ANALYTICS ====================
@Composable
fun AnalyticsTabContent(videos: List<Video>, users: List<User>, comments: List<Comment>) {
    val totalViews = videos.sumOf { it.viewsCount }
    val totalLikes = videos.sumOf { it.likesCount }
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMobile = maxWidth < 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isMobile) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardCard("Platform Views", "${totalViews}k", "Accumulated platform traffic", CyanAccent)
                    DashboardCard("Reaction Engagement", "$totalLikes", "Likes counter metric", LikeRed)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        DashboardCard("Platform Views", "${totalViews}k", "Accumulated platform traffic", CyanAccent)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        DashboardCard("Reaction Engagement", "$totalLikes", "Likes counter metric", LikeRed)
                    }
                }
            }

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Most Viewed Streams", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                val sortedVids = videos.sortedByDescending { it.viewsCount }.take(5)
                sortedVids.forEach { vid ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(vid.title, color = TextWhite, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${vid.viewsCount} views", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
}

// ==================== 14. SYSTEM SETTINGS ====================
@Composable
fun SystemSettingsManagementTabContent(viewModel: DeenTokViewModel) {
    val platformName by viewModel.platformAppName.collectAsState()
    val platformLogoText by viewModel.platformLogoText.collectAsState()
    val privacySettings by viewModel.privacySettings.collectAsState()
    val communityGuidelines by viewModel.communityGuidelines.collectAsState()
    val adminNotificationSettings by viewModel.adminNotificationSettings.collectAsState()

    var nameField by remember { mutableStateOf(platformName) }
    var logoField by remember { mutableStateOf(platformLogoText) }
    var privacyField by remember { mutableStateOf(privacySettings) }
    var guidelinesField by remember { mutableStateOf(communityGuidelines) }
    var notificationsField by remember { mutableStateOf(adminNotificationSettings) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("GLOBAL APP BRANDING SETTINGS", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("Platform Application Name", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = logoField,
                    onValueChange = { logoField = it },
                    label = { Text("Logo Placeholder Icon Character", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("PRIVACY & POLICY MANAGEMENT", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                OutlinedTextField(
                    value = privacyField,
                    onValueChange = { privacyField = it },
                    label = { Text("Privacy Controls Policy Statement", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = guidelinesField,
                    onValueChange = { guidelinesField = it },
                    label = { Text("Community Guidelines Handbook", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notificationsField,
                    onValueChange = { notificationsField = it },
                    label = { Text("System Alert Notifications Scheme", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = {
                viewModel.updatePlatformSettings(
                    name = nameField,
                    logo = logoField,
                    privacy = privacyField,
                    guidelines = guidelinesField,
                    notifications = notificationsField
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("SAVE SYSTEM SETTINGS", color = Black, fontWeight = FontWeight.Bold)
        }
    }
}

// ==================== 15. ADMIN ACCOUNTS ====================
@Composable
fun AdminAccountsTabContent(users: List<User>, viewModel: DeenTokViewModel) {
    val admins = users.filter { it.role == "ADMIN" }
    var showCreateAdminDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("System Administrators", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Button(
                onClick = { showCreateAdminDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Create Admin", color = Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(admins) { admin ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(admin.username, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ID: ${admin.userId}", color = TextGray, fontSize = 11.sp)
                            }
                            
                            IconButton(onClick = { viewModel.changeUserRole(admin, "USER") }) {
                                Icon(Icons.Default.Close, "Revoke", tint = LikeRed)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateAdminDialog) {
        var usernameInput by remember { mutableStateOf("") }
        var userIdInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateAdminDialog = false },
            title = { Text("Create New Administrator", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("Admin Username", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                    )
                    OutlinedTextField(
                        value = userIdInput,
                        onValueChange = { userIdInput = it },
                        label = { Text("Unique User ID", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (usernameInput.isNotBlank() && userIdInput.isNotBlank()) {
                            viewModel.createUserDirectly(
                                User(
                                    userId = userIdInput,
                                    username = if (usernameInput.startsWith("@")) usernameInput else "@$usernameInput",
                                    avatarUrl = "AD",
                                    role = "ADMIN",
                                    status = "ACTIVE"
                                )
                            )
                            showCreateAdminDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Provision Admin", color = Black)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

// ==================== 16. ACTIVITY LOGS ====================
@Composable
fun ActivityLogsTabContent(logs: List<AdminLog>) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredLogs = logs.filter {
        it.action.contains(searchQuery, ignoreCase = true) || it.details.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search logs by action or details...", color = TextGray) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent),
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filteredLogs) { log ->
                        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(log.action, color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(date, color = TextGray, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(log.details, color = TextWhite, fontSize = 13.sp)
                            Text("By Admin: ${log.adminUsername}", color = TextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==================== 17. BACKUP & RESTORE ====================
@Composable
fun BackupRestoreTabContent(viewModel: DeenTokViewModel) {
    var backupHistory = remember { mutableStateListOf("backup_initial_20260701_120000.sql", "backup_stable_20260715_093000.sql") }
    var consoleLogs = remember { mutableStateListOf("Systems ready. Backups offline-stored.") }
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Backup Engine", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Ensure safety and database redundancy.", color = TextGray, fontSize = 12.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                isBackingUp = true
                                consoleLogs.add("Initializing SQLite database transaction serialization...")
                                delay(1000)
                                val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                val name = "backup_live_$ts.sql"
                                backupHistory.add(name)
                                consoleLogs.add("Success: Generated full datastore snapshot -> $name")
                                viewModel.logAdminAction("DATABASE_BACKUP", "Created automated database backup: $name")
                                isBackingUp = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        enabled = !isBackingUp
                    ) {
                        Text(if (isBackingUp) "Backing up..." else "Create SQLite Backup", color = Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Backup Archives & Logs", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(backupHistory) { bkp ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bkp, color = TextWhite, fontSize = 12.sp)
                            Button(
                                onClick = {
                                    scope.launch {
                                        isRestoring = true
                                        consoleLogs.add("Halting active SQLite connections...")
                                        delay(800)
                                        consoleLogs.add("Applying transactions from $bkp...")
                                        delay(800)
                                        consoleLogs.add("Database restored successfully to $bkp.")
                                        viewModel.logAdminAction("DATABASE_RESTORE", "Restored system database state to: $bkp")
                                        isRestoring = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                                enabled = !isRestoring,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("Restore State", color = TextWhite, fontSize = 10.sp)
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                
                Text("System Log Output", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.height(100.dp)) {
                    items(consoleLogs) { log ->
                        Text(log, color = TextGray, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ==================== 18. HELP CENTER ====================
@Composable
fun HelpCenterTabContent() {
    var faqs = remember {
        mutableStateListOf(
            "How do I suspend a user account?" to "Go to the Users tab, search by username, and click the red Block icon.",
            "Can I restore deleted comments?" to "In this current build version, deleted comments are purged from sqlite cleanly, but a transaction is logged under Activity Logs.",
            "What does verifying a creator do?" to "Verifying a creator grants them the CREATOR badge, updates their profile role, and places them inside the verified Creators catalog."
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Administrator Help Handbook", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(faqs) { (q, a) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text("Q: $q", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("A: $a", color = TextWhite, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemplatesManagementTabContent(viewModel: DeenTokViewModel) {
    val templates by viewModel.allTemplates.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<VideoTemplate?>(null) }

    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Trending") }
    var musicInput by remember { mutableStateOf("") }
    var defaultTextInput by remember { mutableStateOf("") }
    var thumbnailInput by remember { mutableStateOf("") }
    var isActiveInput by remember { mutableStateOf(true) }

    val categories = listOf("Trending", "Photo", "Business", "Education", "Celebration", "Story")

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Deen Tok Video Templates", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Manage professional templates for Deen Tok Create (+) engine.", color = TextGray, fontSize = 11.sp)
            }
            Button(
                onClick = {
                    titleInput = ""
                    descInput = ""
                    categoryInput = "Trending"
                    musicInput = ""
                    defaultTextInput = ""
                    thumbnailInput = ""
                    isActiveInput = true
                    showAddDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Template", color = Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Templates List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(templates) { template ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, if (template.isActive) CyanAccent.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail Preview
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            coil.compose.AsyncImage(
                                model = template.thumbnailUrl,
                                contentDescription = template.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }

                        // Info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(template.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyanAccent.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(template.category, color = CyanAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(template.description, color = TextGray, fontSize = 11.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎵 ${template.defaultMusic}", color = TextMuted, fontSize = 9.sp)
                                Text("📈 Usage: ${template.usageCount}", color = TextMuted, fontSize = 9.sp)
                            }
                        }

                        // Controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Active status toggle
                            Switch(
                                checked = template.isActive,
                                onCheckedChange = { viewModel.toggleTemplateActive(template.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Black,
                                    checkedTrackColor = CyanAccent,
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = SurfaceDark
                                ),
                                modifier = Modifier.scale(0.7f)
                            )

                            // Edit button
                            IconButton(
                                onClick = {
                                    editingTemplate = template
                                    titleInput = template.title
                                    descInput = template.description
                                    categoryInput = template.category
                                    musicInput = template.defaultMusic
                                    defaultTextInput = template.defaultText
                                    thumbnailInput = template.thumbnailUrl
                                    isActiveInput = template.isActive
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextWhite, modifier = Modifier.size(16.dp))
                            }

                            // Delete button
                            IconButton(
                                onClick = { viewModel.deleteTemplate(template.id) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LikeRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Template", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Template Title", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                    )
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Template Description", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                    )
                    
                    // Category selector
                    Column {
                        Text("Category", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val sel = cat == categoryInput
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) CyanAccent else SurfaceDark)
                                        .clickable { categoryInput = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(cat, color = if (sel) Black else TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = musicInput,
                        onValueChange = { musicInput = it },
                        label = { Text("Default Music Name", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                    )
                    OutlinedTextField(
                        value = defaultTextInput,
                        onValueChange = { defaultTextInput = it },
                        label = { Text("Default Overlay Text", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                    )
                    OutlinedTextField(
                        value = thumbnailInput,
                        onValueChange = { thumbnailInput = it },
                        label = { Text("Thumbnail Image URL", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val thumb = if (thumbnailInput.isBlank()) "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=150&q=80" else thumbnailInput
                        val music = if (musicInput.isBlank()) "Electro Cyber Synth" else musicInput
                        viewModel.addTemplate(titleInput, descInput, categoryInput, music, defaultTextInput, thumb)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Create", color = Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Edit dialog
    if (editingTemplate != null) {
        AlertDialog(
            onDismissRequest = { editingTemplate = null },
            title = { Text("Edit Template", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Template Title", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                    )
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Template Description", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                    )
                    
                    // Category selector
                    Column {
                        Text("Category", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val sel = cat == categoryInput
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) CyanAccent else SurfaceDark)
                                        .clickable { categoryInput = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(cat, color = if (sel) Black else TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = musicInput,
                        onValueChange = { musicInput = it },
                        label = { Text("Default Music Name", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                    )
                    OutlinedTextField(
                        value = defaultTextInput,
                        onValueChange = { defaultTextInput = it },
                        label = { Text("Default Overlay Text", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                    )
                    OutlinedTextField(
                        value = thumbnailInput,
                        onValueChange = { thumbnailInput = it },
                        label = { Text("Thumbnail Image URL", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val thumb = if (thumbnailInput.isBlank()) "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=150&q=80" else thumbnailInput
                        val music = if (musicInput.isBlank()) "Electro Cyber Synth" else musicInput
                        editingTemplate?.let {
                            viewModel.editTemplate(it.id, titleInput, descInput, categoryInput, music, defaultTextInput, thumb, isActiveInput)
                        }
                        editingTemplate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Save", color = Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTemplate = null }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

// FlowRow layout helper
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}

@Composable
fun LiveModerationTabContent(viewModel: DeenTokViewModel) {
    val activeLiveStreams by viewModel.activeLiveStreams.collectAsState()
    val allLiveReports by viewModel.allLiveReports.collectAsState()
    val isLiveFeatureEnabled by viewModel.isLiveFeatureEnabled.collectAsState()
    val minFollowersForLive by viewModel.minFollowersForLive.collectAsState()
    val liveBannedUserIds by viewModel.liveBannedUserIds.collectAsState()
    val context = LocalContext.current
    var selectedStreamForEnding by remember { mutableStateOf<LiveStream?>(null) }
    var broadcastMessage by remember { mutableStateOf("") }
    var banUsernameInput by remember { mutableStateOf("") }
    var minFollowersInput by remember { mutableStateOf(minFollowersForLive.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Administrative LIVE Settings & Eligibility Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🔴 LIVE FEATURE & ELIGIBILITY SETTINGS", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                // Global Enable/Disable Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable LIVE Feature Globally", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("When turned off, creators cannot start new live streams", color = TextGray, fontSize = 10.sp)
                    }
                    Switch(
                        checked = isLiveFeatureEnabled,
                        onCheckedChange = { viewModel.setLiveFeatureEnabled(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = CyanAccent)
                    )
                }

                Divider(color = SurfaceVariantDark, thickness = 0.5.dp)

                // Minimum Followers Configuration
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Minimum Follower Requirement", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = minFollowersInput,
                            onValueChange = { minFollowersInput = it },
                            placeholder = { Text("0") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceVariantDark)
                        )
                        Button(
                            onClick = {
                                val count = minFollowersInput.toIntOrNull() ?: 0
                                viewModel.setMinFollowersForLive(count)
                                android.widget.Toast.makeText(context, "Minimum followers set to $count", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                        ) {
                            Text("Save Requirement", color = Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Ban / Restriction Management Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🚫 LIVE RESTRICTIONS & BAN MANAGEMENT", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = banUsernameInput,
                        onValueChange = { banUsernameInput = it },
                        placeholder = { Text("Enter Username or User ID (e.g. @violator_user)", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = LikeRed, unfocusedBorderColor = SurfaceVariantDark)
                    )
                    Button(
                        onClick = {
                            if (banUsernameInput.isNotBlank()) {
                                viewModel.banUserFromLive(banUsernameInput, banUsernameInput)
                                android.widget.Toast.makeText(context, "Restricted $banUsernameInput from LIVE", android.widget.Toast.LENGTH_SHORT).show()
                                banUsernameInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LikeRed)
                    ) {
                        Text("Restrict", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                if (liveBannedUserIds.isNotEmpty()) {
                    Text("Currently Restricted Accounts:", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(liveBannedUserIds.toList()) { bannedId ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceVariantDark)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(bannedId, color = TextWhite, fontSize = 11.sp)
                                    Text(
                                        "✕",
                                        color = LikeRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            viewModel.unbanUserFromLive(bannedId, bannedId)
                                            android.widget.Toast.makeText(context, "Unbanned $bannedId", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // System Header Stat Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(LikeRed)
                    )
                    Text("ACTIVE LIVE STREAMS MONITORING", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text("Real-time content supervision, stream termination control, and host policy moderation.", color = TextGray, fontSize = 11.sp)
            }
        }

        // Active Live Streams List
        if (activeLiveStreams.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth().height(140.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No ongoing LIVE streams currently active.", color = TextGray, fontSize = 12.sp)
                }
            }
        } else {
            activeLiveStreams.forEach { stream ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(DarkNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(stream.creatorUsername.take(2).uppercase(), color = CyanAccent, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(stream.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Host: ${stream.creatorUsername} • Category: ${stream.category}", color = TextGray, fontSize = 11.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LikeRed)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("LIVE • ${stream.viewerCount} Viewers", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Gifts Received: 🪙 ${stream.giftCoinsEarned} DT Coins", color = GoldYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.selectLiveStream(stream.id)
                                        viewModel.setScreen(Screen.LIVE)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark)
                                ) {
                                    Text("Inspect Stream", color = CyanAccent, fontSize = 10.sp)
                                }

                                Button(
                                    onClick = { selectedStreamForEnding = stream },
                                    colors = ButtonDefaults.buttonColors(containerColor = LikeRed)
                                ) {
                                    Text("Terminate Stream", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Administrative Global Warning / Broadcast Section
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ADMINISTRATIVE LIVE BROADCAST ALERT", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                OutlinedTextField(
                    value = broadcastMessage,
                    onValueChange = { broadcastMessage = it },
                    placeholder = { Text("Enter message to broadcast to all active live chat streams...", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceVariantDark)
                )

                Button(
                    onClick = {
                        if (broadcastMessage.isNotBlank()) {
                            activeLiveStreams.forEach { st ->
                                viewModel.sendLiveChatMessage(st.id, "📢 [ADMIN SYSTEM]: $broadcastMessage")
                            }
                            android.widget.Toast.makeText(context, "Broadcast sent to all active live streams!", android.widget.Toast.LENGTH_SHORT).show()
                            broadcastMessage = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Send Broadcast", color = Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // LIVE Stream User Reports Inspection Section
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🚩 LIVE STREAM USER REPORTS & AUDIT", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                if (allLiveReports.isEmpty()) {
                    Text("No live stream reports logged.", color = TextMuted, fontSize = 11.sp)
                } else {
                    allLiveReports.forEach { report ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceVariantDark, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Report #${report.id} • Stream #${report.streamId}", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (report.status == "PENDING") GoldYellow else SurfaceDark)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(report.status, color = if (report.status == "PENDING") Black else TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text("Reason: ${report.reason}", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Reported User: ${report.reportedUsername} • Reporter: ${report.reporterUsername}", color = TextGray, fontSize = 10.sp)

                                if (report.status == "PENDING") {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        TextButton(onClick = { viewModel.reviewLiveReport(report.id, "DISMISSED") }) {
                                            Text("Dismiss", color = TextGray, fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.reviewLiveReport(report.id, "ACTIONED")
                                                viewModel.adminEndLiveStream(report.streamId, "Reported for: ${report.reason}")
                                                android.widget.Toast.makeText(context, "Stream #${report.streamId} suspended", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = LikeRed)
                                        ) {
                                            Text("Suspend Stream", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

    // Terminate Stream Confirmation Modal
    if (selectedStreamForEnding != null) {
        val streamToClose = selectedStreamForEnding!!
        AlertDialog(
            onDismissRequest = { selectedStreamForEnding = null },
            title = { Text("Terminate LIVE Stream", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to forcibly terminate '${streamToClose.title}' hosted by ${streamToClose.creatorUsername}? This will close the broadcast immediately for all viewers.",
                    color = TextGray,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.endLiveStream(streamToClose.id)
                        viewModel.logAdminAction("LIVE_TERMINATE", "Terminated live stream ${streamToClose.id} hosted by ${streamToClose.creatorUsername}")
                        android.widget.Toast.makeText(context, "Stream terminated successfully.", android.widget.Toast.LENGTH_SHORT).show()
                        selectedStreamForEnding = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LikeRed)
                ) {
                    Text("Terminate Now", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedStreamForEnding = null }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

