package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.DeenTokViewModel
import com.example.viewmodel.GiftAnimationEvent
import kotlinx.coroutines.delay

// --- 1. GIFT BOTTOM SHEET (SEND GIFTS TO CREATORS) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftBottomSheet(
    viewModel: DeenTokViewModel,
    targetUserId: String,
    targetUsername: String,
    videoId: Int = 0,
    onDismiss: () -> Unit,
    onOpenBuyCoins: () -> Unit
) {
    val userWallet by viewModel.userWallet.collectAsState()
    val allGifts by viewModel.allVirtualGifts.collectAsState()
    val coinBalance = userWallet?.coinBalance ?: 0

    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedGift by remember { mutableStateOf<VirtualGift?>(null) }
    var giftSendSuccessMsg by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filteredGifts = remember(allGifts, selectedCategory) {
        if (selectedCategory == "ALL") allGifts
        else allGifts.filter { it.category == selectedCategory }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF14141B),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header: Wallet Balance & Buy Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Send Gift to @$targetUsername",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Support Islamic Creators on DeenTok",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }

                // Balance Chip with + Buy button
                Surface(
                    color = Color(0xFF262632),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, GoldYellow.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { onOpenBuyCoins() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$coinBalance",
                            fontWeight = FontWeight.Bold,
                            color = GoldYellow,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Buy Coins",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf("ALL" to "All Gifts", "BASIC" to "Basic 🌹", "SPECIAL" to "Special 📜", "PREMIUM" to "Premium 🕌")
                categories.forEach { (catKey, label) ->
                    val isSelected = selectedCategory == catKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = catKey },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldYellow,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF262632),
                            labelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message banners
            giftSendSuccessMsg?.let { msg ->
                Surface(
                    color = EmeraldGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, EmeraldGreen),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = msg,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            errorMessage?.let { msg ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onOpenBuyCoins() }) {
                            Text("Recharge 🪙", color = GoldYellow, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Gifts Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredGifts) { gift ->
                    val isSelected = selectedGift?.id == gift.id
                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                selectedGift = gift
                                errorMessage = null
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) GoldYellow.copy(alpha = 0.25f) else Color(0xFF22222E),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) GoldYellow else Color.White.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(text = gift.iconSymbol, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = gift.name,
                                fontSize = 10.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪙", fontSize = 10.sp)
                                Text(
                                    text = "${gift.coinPrice}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldYellow
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button: Send Selected Gift
            Button(
                onClick = {
                    val gift = selectedGift
                    if (gift == null) {
                        errorMessage = "Please select a gift first!"
                        return@Button
                    }
                    if (coinBalance < gift.coinPrice) {
                        errorMessage = "Insufficient DT Coins! You need ${gift.coinPrice - coinBalance} more coins."
                        return@Button
                    }
                    val success = viewModel.sendVirtualGift(
                        gift = gift,
                        targetUserId = targetUserId,
                        targetUsername = targetUsername,
                        videoId = videoId
                    )
                    if (success) {
                        giftSendSuccessMsg = "Sent ${gift.name} ${gift.iconSymbol} to @$targetUsername!"
                        errorMessage = null
                    } else {
                        errorMessage = "Failed to send gift. Please check your coin balance."
                    }
                },
                enabled = selectedGift != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldYellow,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF333342)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_gift_button")
            ) {
                if (selectedGift != null) {
                    Text(
                        text = "Send ${selectedGift!!.name} (${selectedGift!!.coinPrice} Coins 🪙)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } else {
                    Text(
                        text = "Select a Gift",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// --- 2. FLOATING ANIMATED GIFT OVERLAY ---
@Composable
fun GiftAnimationOverlay(
    event: GiftAnimationEvent?,
    onDismiss: () -> Unit
) {
    if (event == null) return

    LaunchedEffect(event) {
        delay(3500) // Display gift float animation for 3.5s
        onDismiss()
    }

    var scale by remember { mutableStateOf(0.2f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "giftScale"
    )

    LaunchedEffect(Unit) {
        scale = 1.2f
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.85f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(GoldYellow, EmeraldGreen))),
            shadowElevation = 12.dp,
            modifier = Modifier
                .scale(animatedScale)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = event.giftSymbol,
                    fontSize = 72.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${event.senderUsername} sent a ${event.giftName}!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("🪙", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${event.coinPrice} DT Coins",
                        color = GoldYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// --- 3. BUY DT COIN DIALOG (ETHIOPIAN TELEBIRR & CBE PAYMENT FLOW) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyCoinsDialog(
    viewModel: DeenTokViewModel,
    onDismiss: () -> Unit
) {
    val coinPackages by viewModel.allCoinPackages.collectAsState()
    var selectedPackage by remember { mutableStateOf<CoinPackage?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf("Telebirr") } // "Telebirr" or "CBE"
    var accountOrPhone by remember { mutableStateOf("") }
    var txRefInput by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var showSuccessReceipt by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(coinPackages) {
        if (selectedPackage == null && coinPackages.isNotEmpty()) {
            selectedPackage = coinPackages.firstOrNull { it.badge == "Most Popular" } ?: coinPackages.first()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        containerColor = Color(0xFF181822),
        shape = RoundedCornerShape(20.dp),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (showSuccessReceipt) {
                    // Success View
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✨", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Purchase Successful!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val totalCoins = (selectedPackage?.coinAmount ?: 0) + (selectedPackage?.bonusAmount ?: 0)
                        Text(
                            text = "+$totalCoins DT Coins Added",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = GoldYellow,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Payment of ${selectedPackage?.priceEtb} ETB via $selectedPaymentMethod verified.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldYellow, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Purchase Form
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Buy DT Coins",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    Text(
                        text = "Instant local payment via Telebirr or CBE Bank (Ethiopia)",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step 1: Select Coin Package
                    Text(
                        text = "1. Select Coin Package",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldYellow
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())
                    ) {
                        coinPackages.filter { it.isActive }.forEach { pkg ->
                            val isSelected = selectedPackage?.id == pkg.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPackage = pkg },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) DarkGold.copy(alpha = 0.2f) else Color(0xFF242432),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) GoldYellow else Color.White.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🪙", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${pkg.coinAmount} Coins",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 15.sp
                                                )
                                                if (pkg.bonusAmount > 0) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = EmeraldGreen.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "+${pkg.bonusAmount} Free",
                                                            color = EmeraldGreen,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = pkg.title,
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${pkg.priceEtb.toInt()} ETB",
                                            fontWeight = FontWeight.Bold,
                                            color = GoldYellow,
                                            fontSize = 15.sp
                                        )
                                        if (pkg.badge.isNotBlank()) {
                                            Text(
                                                text = pkg.badge,
                                                fontSize = 9.sp,
                                                color = DarkGold,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step 2: Select Payment Method
                    Text(
                        text = "2. Select Payment Method",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldYellow
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Telebirr Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPaymentMethod = "Telebirr" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedPaymentMethod == "Telebirr") TelebirrBlue.copy(alpha = 0.25f) else Color(0xFF242432),
                            border = BorderStroke(
                                width = if (selectedPaymentMethod == "Telebirr") 2.dp else 1.dp,
                                color = if (selectedPaymentMethod == "Telebirr") TelebirrBlue else Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("📱", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Telebirr",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // CBE Bank Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPaymentMethod = "CBE" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedPaymentMethod == "CBE") CbePurple.copy(alpha = 0.25f) else Color(0xFF242432),
                            border = BorderStroke(
                                width = if (selectedPaymentMethod == "CBE") 2.dp else 1.dp,
                                color = if (selectedPaymentMethod == "CBE") CbePurple else Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🏦", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CBE Bank",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Phone / Account Number
                    OutlinedTextField(
                        value = accountOrPhone,
                        onValueChange = { accountOrPhone = it },
                        label = { Text(if (selectedPaymentMethod == "Telebirr") "Telebirr Phone Number (09...)" else "CBE Account Number", fontSize = 12.sp) },
                        placeholder = { Text(if (selectedPaymentMethod == "Telebirr") "0912345678" else "1000123456789", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldYellow,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = GoldYellow,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("payment_account_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Optional Tx Reference Number
                    OutlinedTextField(
                        value = txRefInput,
                        onValueChange = { txRefInput = it },
                        label = { Text("Transaction Ref / SMS Code (Optional)", fontSize = 12.sp) },
                        placeholder = { Text("e.g. TLB89234561", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldYellow,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = GoldYellow,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("payment_ref_input")
                    )

                    errorMessage?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Payment Button
                    Button(
                        onClick = {
                            val pkg = selectedPackage
                            if (pkg == null) {
                                errorMessage = "Please select a coin package."
                                return@Button
                            }
                            if (accountOrPhone.trim().length < 8) {
                                errorMessage = "Please enter a valid ${selectedPaymentMethod} account or phone number."
                                return@Button
                            }

                            isProcessing = true
                            viewModel.buyCoinsWithLocalPayment(
                                pkg = pkg,
                                paymentMethod = selectedPaymentMethod,
                                accountOrPhone = accountOrPhone.trim(),
                                txRef = txRefInput.trim()
                            )
                            isProcessing = false
                            showSuccessReceipt = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldYellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_buy_coins_button")
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                        } else {
                            val coins = (selectedPackage?.coinAmount ?: 0) + (selectedPackage?.bonusAmount ?: 0)
                            val price = selectedPackage?.priceEtb?.toInt() ?: 0
                            Text("Pay $price ETB for $coins Coins 🪙", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    )
}

// --- 4. BALANCE SCREEN (TIKTOK ECONOMY SYSTEM) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: DeenTokViewModel,
    onBack: () -> Unit
) {
    val userWallet by viewModel.userWallet.collectAsState()
    val creatorWallet by viewModel.creatorWallet.collectAsState()
    val coinTxHistory by viewModel.userCoinTransactions.collectAsState()
    val paymentTxHistory by viewModel.userPaymentTransactions.collectAsState()
    val allGifts by viewModel.allVirtualGifts.collectAsState()
    val giftHistory by viewModel.creatorGiftTransactions.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()

    var showBuyCoinsModal by remember { mutableStateOf(false) }
    var showWithdrawModal by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Coins, 1: Gifts, 2: Diamonds, 3: Rewards, 4: History, 5: Promotions

    val userCoins = userWallet?.coinBalance ?: 0
    val creatorDiamonds = creatorWallet?.rewardBalanceCoins ?: 0
    val myVideos = remember(allVideos) { allVideos.filter { it.userId == "current_user" } }

    if (showBuyCoinsModal) {
        BuyCoinsDialog(
            viewModel = viewModel,
            onDismiss = { showBuyCoinsModal = false }
        )
    }

    if (showWithdrawModal) {
        CreatorWithdrawDialog(
            viewModel = viewModel,
            onDismiss = { showWithdrawModal = false }
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF12121A))
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Balance",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = "TikTok Economy • Viewers Send Gifts, Creators Earn Diamonds",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        },
        containerColor = Color(0xFF0F0F17)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // TikTok Style Balance Overview Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF261F0A), Color(0xFF1B1607), Color(0xFF0A1813))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(1.5.dp, GoldYellow.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("BALANCE OVERVIEW", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // DT Coins
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🪙", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$userCoins",
                                        color = GoldYellow,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp
                                    )
                                }
                                Text("DT Coins (Viewer)", color = Color.Gray, fontSize = 11.sp)
                            }

                            Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color.White.copy(alpha = 0.15f)))

                            // DT Diamonds
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💎", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$creatorDiamonds",
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp
                                    )
                                }
                                Text("DT Diamonds (${(creatorDiamonds * 0.5).toInt()} ETB)", color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showBuyCoinsModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldYellow, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("balance_recharge_coins_btn")
                            ) {
                                Text("Recharge 🪙", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showWithdrawModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("balance_withdraw_btn")
                            ) {
                                Text("Withdraw 💎", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { selectedTab = 5 },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262638), contentColor = CyanAccent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("balance_promote_btn")
                            ) {
                                Text("Promote 🚀", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 6 Scrollable Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF13131D),
                contentColor = GoldYellow,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldYellow
                    )
                }
            ) {
                val tabs = listOf(
                    "🪙 DT Coins",
                    "🎁 Gifts",
                    "💎 DT Diamonds",
                    "🏆 Creator Rewards",
                    "📊 History",
                    "🚀 Promotions"
                )
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> CoinsTabContent(userCoins = userCoins, onRechargeClick = { showBuyCoinsModal = true }, viewModel = viewModel)
                    1 -> GiftsShopTabContent(allGifts = allGifts, userCoins = userCoins, onRechargeClick = { showBuyCoinsModal = true }, viewModel = viewModel)
                    2 -> DiamondsTabContent(creatorWallet = creatorWallet, giftHistory = giftHistory, onWithdrawClick = { showWithdrawModal = true })
                    3 -> CreatorRewardsTabContent(myVideos = myVideos, viewModel = viewModel)
                    4 -> TransactionHistoryTabContent(coinTxHistory = coinTxHistory, paymentTxHistory = paymentTxHistory)
                    5 -> PromotionsTabContent(myVideos = myVideos, userCoins = userCoins, onRechargeClick = { showBuyCoinsModal = true }, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BalanceScreen(viewModel: DeenTokViewModel, onBack: () -> Unit) {
    WalletScreen(viewModel, onBack)
}

// --- SUB-TAB 1: DT COINS & RECHARGE ---
@Composable
fun CoinsTabContent(userCoins: Int, onRechargeClick: () -> Unit, viewModel: DeenTokViewModel) {
    val coinPackages by viewModel.allCoinPackages.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = Color(0xFF1A1A26),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("DT COIN BALANCE", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$userCoins Coins", color = GoldYellow, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                    }
                    Button(
                        onClick = onRechargeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldYellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Recharge Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text("Use DT Coins to send virtual gifts to creators or boost your videos to reach more viewers.", color = Color.LightGray, fontSize = 11.sp)
            }
        }

        Text("Recharge Coin Packages", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        // Coin packages list
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            items(coinPackages) { pkg ->
                Surface(
                    color = Color(0xFF1D1D2B),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (pkg.badge == "Most Popular") GoldYellow else Color(0xFF2C2C3E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRechargeClick() }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (pkg.badge.isNotBlank()) {
                            Text(pkg.badge, color = GoldYellow, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${pkg.coinAmount}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            if (pkg.bonusAmount > 0) {
                                Text(" +${pkg.bonusAmount}", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                        Text("${pkg.priceEtb.toInt()} ETB", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Supported Payments Card
        Surface(
            color = Color(0xFF14141F),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF232332)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Accepted Local & Global Payment Methods", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text("Telebirr 📱", fontSize = 10.sp) })
                    AssistChip(onClick = {}, label = { Text("CBE Bank 🏦", fontSize = 10.sp) })
                    AssistChip(onClick = {}, label = { Text("Chapa 💳", fontSize = 10.sp) })
                    AssistChip(onClick = {}, label = { Text("Google Play 🛍️", fontSize = 10.sp) })
                }
            }
        }
    }
}

// --- SUB-TAB 2: VIRTUAL GIFTS & GIFT SHOP ---
@Composable
fun GiftsShopTabContent(allGifts: List<VirtualGift>, userCoins: Int, onRechargeClick: () -> Unit, viewModel: DeenTokViewModel) {
    var selectedGift by remember { mutableStateOf<VirtualGift?>(null) }
    var testTargetUsername by remember { mutableStateOf("ustaz_marcus") }
    var giftResultMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("ISLAMIC VIRTUAL GIFT SHOP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Gift prices listed in DT Coins (1 Coin = 1 Creator Diamond)", color = Color.Gray, fontSize = 11.sp)
            }
            Surface(
                color = Color(0xFF262635),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.clickable { onRechargeClick() }
            ) {
                Text("🪙 $userCoins Coins +", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
            }
        }

        giftResultMsg?.let { msg ->
            Text(msg, color = if (msg.contains("Success")) EmeraldGreen else MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            items(allGifts) { gift ->
                val isSelected = selectedGift?.id == gift.id
                Surface(
                    color = if (isSelected) Color(0xFF2B250D) else Color(0xFF1A1A26),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (isSelected) GoldYellow else Color(0xFF2C2C3E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGift = gift }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(gift.iconSymbol, fontSize = 32.sp)
                        Text(gift.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${gift.coinPrice} Coins", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Test sending gift
        selectedGift?.let { gift ->
            Surface(
                color = Color(0xFF1E1E2D),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GoldYellow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Send Gift Test (from Video / LIVE)", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(gift.iconSymbol, fontSize = 28.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(gift.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Price: ${gift.coinPrice} Coins (Gives +${gift.coinPrice} Diamonds to Creator)", color = Color.Gray, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                val success = viewModel.sendVirtualGift(
                                    gift = gift,
                                    targetUserId = "marcus123",
                                    targetUsername = testTargetUsername
                                )
                                giftResultMsg = if (success) "Successfully sent ${gift.name} to @$testTargetUsername!" else "Insufficient Coins! Please recharge."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldYellow, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Send Gift 🎁", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-TAB 3: DT DIAMONDS & CREATOR WITHDRAWAL ---
@Composable
fun DiamondsTabContent(creatorWallet: CreatorWallet?, giftHistory: List<GiftTransaction>, onWithdrawClick: () -> Unit) {
    val rewardCoins = creatorWallet?.rewardBalanceCoins ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = Color(0xFF0D241C),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("CREATOR GIFT EARNINGS (DT DIAMONDS)", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💎", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$rewardCoins Diamonds", color = EmeraldGreen, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                        Text("Estimated Value: ${(rewardCoins * 0.5).toInt()} ETB", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onWithdrawClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Withdraw System", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Gifts Received", color = Color.Gray, fontSize = 10.sp)
                        Text("${creatorWallet?.totalGiftsReceived ?: 0}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Lifetime Diamonds Earned", color = Color.Gray, fontSize = 10.sp)
                        Text("${creatorWallet?.totalCoinsEarned ?: 0} 💎", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Text("Earnings History (Gifts Received from Viewers)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        if (giftHistory.isEmpty()) {
            Text("No creator gift earnings recorded yet.", color = Color.Gray, fontSize = 12.sp)
        } else {
            giftHistory.forEach { giftTx ->
                Surface(
                    color = Color(0xFF1B1B28),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(giftTx.giftIcon, fontSize = 22.sp)
                            Column {
                                Text("${giftTx.giftName} from @${giftTx.senderUsername}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Gift reward credited to wallet", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                        Text("+${giftTx.coinPrice} 💎", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// --- SUB-TAB 4: CREATOR REWARDS ---
@Composable
fun CreatorRewardsTabContent(myVideos: List<Video>, viewModel: DeenTokViewModel) {
    var claimedRewardsMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("CREATOR REWARDS & CAMPAIGNS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        claimedRewardsMsg?.let { msg ->
            Surface(color = EmeraldGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(msg, color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
            }
        }

        // 1. Video Milestone Rewards Section
        Surface(color = Color(0xFF1B1B28), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("📹 Video View Milestone Rewards", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Earn bonus DT Diamonds when your Islamic short videos achieve view milestones.", color = Color.Gray, fontSize = 11.sp)

                if (myVideos.isEmpty()) {
                    Text("Upload your first video to participate in Video Rewards!", color = Color.LightGray, fontSize = 11.sp)
                } else {
                    myVideos.forEach { video ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(video.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                Text("${video.viewsCount} Views • Goal: 1,000 Views (+100 💎)", color = Color.Gray, fontSize = 10.sp)
                            }
                            Button(
                                onClick = {
                                    viewModel.claimCreatorVideoReward(video.id, video.title, 100)
                                    claimedRewardsMsg = "Claimed +100 DT Diamonds for '${video.title}'!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Claim 💎", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Campaign Rewards
        Surface(color = Color(0xFF1B1B28), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🌟 Active Creator Challenges & Campaigns", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                val campaigns = listOf(
                    "📖 #QuranReflection Challenge" to 300,
                    "🕋 #DeenReminders Heritage Campaign" to 500,
                    "🕌 #JumuahMubaraka Recitation Contest" to 250
                )

                campaigns.forEach { (title, reward) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Bonus: +$reward DT Diamonds for top entries", color = GoldYellow, fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.claimCreatorCampaignReward(title, reward)
                                claimedRewardsMsg = "Participated & claimed +$reward DT Diamonds for $title!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Join & Claim", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-TAB 5: TRANSACTION HISTORY ---
@Composable
fun TransactionHistoryTabContent(coinTxHistory: List<CoinTransaction>, paymentTxHistory: List<PaymentTransaction>) {
    var filterType by remember { mutableStateOf("ALL") } // ALL, PURCHASES, GIFTS_SENT, GIFTS_RECEIVED, REWARDS

    val filteredList = remember(coinTxHistory, filterType) {
        when (filterType) {
            "PURCHASES" -> coinTxHistory.filter { it.type == "PURCHASE" }
            "GIFTS_SENT" -> coinTxHistory.filter { it.type == "GIFT_SENT" }
            "GIFTS_RECEIVED" -> coinTxHistory.filter { it.type == "GIFT_RECEIVED" }
            "REWARDS" -> coinTxHistory.filter { it.type == "VIDEO_REWARD" || it.type == "CAMPAIGN_REWARD" || it.type == "REWARD_PAYOUT" }
            else -> coinTxHistory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("TRANSACTION HISTORY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("ALL" to "All", "PURCHASES" to "Purchases", "GIFTS_SENT" to "Gifts Sent", "GIFTS_RECEIVED" to "Gifts Recv", "REWARDS" to "Rewards").forEach { (typeKey, label) ->
                FilterChip(
                    selected = filterType == typeKey,
                    onClick = { filterType = typeKey },
                    label = { Text(label, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldYellow, selectedLabelColor = Black)
                )
            }
        }

        if (filteredList.isEmpty()) {
            Text("No transactions found for this category.", color = Color.Gray, fontSize = 12.sp)
        } else {
            filteredList.forEach { tx ->
                Surface(
                    color = Color(0xFF1B1B28),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val icon = when (tx.type) {
                                "PURCHASE" -> "💳"
                                "GIFT_SENT" -> "🎁"
                                "GIFT_RECEIVED" -> "✨"
                                "PROMOTION" -> "🚀"
                                else -> "🪙"
                            }
                            Text(icon, fontSize = 20.sp)
                            Column {
                                Text(tx.description, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(tx.type, color = Color.Gray, fontSize = 10.sp)
                            }
                        }

                        val isPositive = tx.amount > 0
                        Text(
                            text = if (isPositive) "+${tx.amount}" else "${tx.amount}",
                            color = if (isPositive) EmeraldGreen else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-TAB 6: PROMOTIONS (BOOST VIDEOS) ---
@Composable
fun PromotionsTabContent(
    myVideos: List<Video>,
    userCoins: Int,
    onRechargeClick: () -> Unit,
    viewModel: DeenTokViewModel
) {
    var selectedVideo by remember { mutableStateOf<Video?>(myVideos.firstOrNull()) }
    var selectedGoal by remember { mutableStateOf("More Views") }
    var selectedBudgetCoins by remember { mutableStateOf(200) } // 50, 200, 500
    var promoteMsg by remember { mutableStateOf<String?>(null) }

    val estimatedViews = remember(selectedBudgetCoins) {
        when (selectedBudgetCoins) {
            50 -> 1000
            200 -> 5000
            500 -> 15000
            else -> 35000
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🚀 PROMOTE VIDEOS WITH DT COINS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Boost your Islamic short videos on Deen Tok to gain more views, followers, and profile visits.", color = Color.Gray, fontSize = 11.sp)

        promoteMsg?.let { msg ->
            Surface(
                color = if (msg.contains("Successfully")) EmeraldGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(msg, color = if (msg.contains("Successfully")) EmeraldGreen else MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
            }
        }

        if (myVideos.isEmpty()) {
            Surface(color = Color(0xFF1B1B28), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text("No videos found to promote. Upload a video first to use Promotions!", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
            }
        } else {
            // Select Video
            Text("1. Select Video to Boost:", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(myVideos) { vid ->
                    val isSel = selectedVideo?.id == vid.id
                    Surface(
                        color = if (isSel) Color(0xFF2B250D) else Color(0xFF1A1A26),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isSel) GoldYellow else Color(0xFF2C2C3E)),
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { selectedVideo = vid }
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(vid.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                            Text("Current Views: ${vid.viewsCount}", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Select Goal
            Text("2. Choose Promotion Goal:", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("More Views", "More Followers", "More Profile Visits").forEach { goal ->
                    FilterChip(
                        selected = selectedGoal == goal,
                        onClick = { selectedGoal = goal },
                        label = { Text(goal, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldYellow, selectedLabelColor = Black)
                    )
                }
            }

            // Select Budget
            Text("3. Select Coin Budget:", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(50 to "+1k Views", 200 to "+5k Views", 500 to "+15k Views", 1000 to "+35k Views").forEach { (coins, viewsLabel) ->
                    val isSel = selectedBudgetCoins == coins
                    Surface(
                        color = if (isSel) GoldYellow else Color(0xFF1B1B28),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedBudgetCoins = coins }
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🪙 $coins", color = if (isSel) Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(viewsLabel, color = if (isSel) Black else EmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Overview & Confirm
            Surface(color = Color(0xFF1B1B28), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, GoldYellow), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Promote: '${selectedVideo?.title ?: ""}'", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Budget: $selectedBudgetCoins Coins", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text("Estimated Impressions: +$estimatedViews viewers in For You feed", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = {
                            val vid = selectedVideo
                            if (vid != null) {
                                if (userCoins < selectedBudgetCoins) {
                                    promoteMsg = "Insufficient DT Coins! Recharge coins to promote video."
                                    onRechargeClick()
                                } else {
                                    val success = viewModel.boostVideoWithCoins(
                                        videoId = vid.id,
                                        videoTitle = vid.title,
                                        targetViews = estimatedViews,
                                        coinsCost = selectedBudgetCoins
                                    )
                                    if (success) {
                                        promoteMsg = "Successfully boosted '${vid.title}' for +$estimatedViews views!"
                                    } else {
                                        promoteMsg = "Failed to boost video."
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldYellow, contentColor = Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_promote_video_btn")
                    ) {
                        Text("Promote Video Now (🪙 $selectedBudgetCoins Coins)", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// --- 5. CREATOR WITHDRAWAL DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorWithdrawDialog(
    viewModel: DeenTokViewModel,
    onDismiss: () -> Unit
) {
    val creatorWallet by viewModel.creatorWallet.collectAsState()
    val rewardCoins = creatorWallet?.rewardBalanceCoins ?: 0

    var withdrawAmountText by remember { mutableStateOf("$rewardCoins") }
    var selectedPayoutMethod by remember { mutableStateOf("Telebirr") }
    var accountDetailsInput by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        containerColor = Color(0xFF181822),
        shape = RoundedCornerShape(20.dp),
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (showSuccess) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Withdrawal Submitted!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Admin team is processing your request. Payout will be sent to your $selectedPayoutMethod account.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Withdraw Creator Rewards",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    Text(
                        text = "Available Balance: $rewardCoins Coins (${(rewardCoins * 0.5).toInt()} ETB)",
                        style = MaterialTheme.typography.bodySmall.copy(color = EmeraldGreen, fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount input
                    OutlinedTextField(
                        value = withdrawAmountText,
                        onValueChange = { withdrawAmountText = it },
                        label = { Text("Coins Amount to Withdraw", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = EmeraldGreen,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_amount_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Method Selector
                    Text("Select Payout Method:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedPayoutMethod == "Telebirr",
                            onClick = { selectedPayoutMethod = "Telebirr" },
                            label = { Text("Telebirr 📱") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TelebirrBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedPayoutMethod == "CBE Bank",
                            onClick = { selectedPayoutMethod = "CBE Bank" },
                            label = { Text("CBE Bank 🏦") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CbePurple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Account Details
                    OutlinedTextField(
                        value = accountDetailsInput,
                        onValueChange = { accountDetailsInput = it },
                        label = { Text(if (selectedPayoutMethod == "Telebirr") "Telebirr Phone Number" else "CBE Bank Account Number", fontSize = 12.sp) },
                        placeholder = { Text(if (selectedPayoutMethod == "Telebirr") "0912345678" else "1000123456789", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = EmeraldGreen,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_account_input")
                    )

                    errorMessage?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val amount = withdrawAmountText.toIntOrNull() ?: 0
                            if (amount <= 0 || amount > rewardCoins) {
                                errorMessage = "Invalid coin amount! You have $rewardCoins available."
                                return@Button
                            }
                            if (accountDetailsInput.trim().length < 8) {
                                errorMessage = "Please enter a valid $selectedPayoutMethod account or phone number."
                                return@Button
                            }

                            val success = viewModel.requestCreatorWithdrawal(
                                coinsAmount = amount,
                                payoutMethod = selectedPayoutMethod,
                                accountDetails = accountDetailsInput.trim()
                            )
                            if (success) {
                                showSuccess = true
                            } else {
                                errorMessage = "Failed to submit withdrawal request."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("confirm_withdraw_button")
                    ) {
                        val amount = withdrawAmountText.toIntOrNull() ?: 0
                        val estEtb = (amount * 0.5).toInt()
                        Text("Request Payout ($estEtb ETB)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}
