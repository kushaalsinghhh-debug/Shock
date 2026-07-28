package com.example.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WinX7ViewModel

enum class AdminPage(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    USER_MANAGEMENT("User Management", Icons.Default.People),
    STAFF_MANAGEMENT("Staff & Roles", Icons.Default.Badge),
    TOURNAMENT_MGMT("Tournaments", Icons.Default.SportsEsports),
    CREATE_EDIT_TOURNAMENT("Create/Edit Match", Icons.Default.AddBox),
    DEPOSIT_REQUESTS("Deposit Requests", Icons.Default.AccountBalanceWallet),
    WITHDRAWAL_REQUESTS("Withdrawal Requests", Icons.Default.Payments),
    SYSTEM_MANAGEMENT("Category & Content", Icons.Default.FolderSpecial),
    NOTIFICATIONS("Push Notifications", Icons.Default.Notifications),
    SUPPORT_TICKETS("Support Tickets", Icons.Default.SupportAgent),
    REPORTS_ANALYTICS("Reports & Analytics", Icons.Default.Analytics),
    APP_SETTINGS("App Settings", Icons.Default.Settings)
}

@Composable
fun AdminPanelScreen(
    viewModel: WinX7ViewModel,
    onReturnToUserApp: () -> Unit
) {
    val tournaments by viewModel.tournaments.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val adminConfig by viewModel.adminConfig.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val staffUsers by viewModel.staffUsers.collectAsState()
    val tickets by viewModel.tickets.collectAsState()
    val depositRequests by viewModel.depositRequests.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activePage by remember { mutableStateOf(AdminPage.DASHBOARD) }

    // Dialog & Form states
    var showCreateTournamentDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Solo BR") }
    var newMap by remember { mutableStateOf("Bermuda") }
    var newMode by remember { mutableStateOf("Solo") }
    var newEntryFee by remember { mutableStateOf("30") }
    var newPrizePool by remember { mutableStateOf("1500") }
    var newKillReward by remember { mutableStateOf("15") }
    var newTotalSlots by remember { mutableStateOf("48") }
    var newRules by remember { mutableStateOf("1. No emulators.\n2. No hacking.\n3. Room ID 15m before match.") }
    var newThumbnailUrl by remember { mutableStateOf("") }

    var editingTournament by remember { mutableStateOf<Tournament?>(null) }
    var editRoomId by remember { mutableStateOf("") }
    var editRoomPass by remember { mutableStateOf("") }

    var editingTournamentDetails by remember { mutableStateOf<Tournament?>(null) }
    var editTitleInput by remember { mutableStateOf("") }
    var editEntryFeeInput by remember { mutableStateOf("") }
    var editPrizePoolInput by remember { mutableStateOf("") }
    var editMapInput by remember { mutableStateOf("") }
    var editThumbnailInput by remember { mutableStateOf("") }

    var showAddStaffDialog by remember { mutableStateOf(false) }
    var newStaffName by remember { mutableStateOf("") }
    var newStaffEmail by remember { mutableStateOf("") }
    var newStaffRoleTitle by remember { mutableStateOf("Match Moderator") }
    var selectedPermissions by remember { mutableStateOf(setOf("UPLOAD_ROOM", "UPDATE_RESULTS", "SUPPORT_TICKETS")) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    var userSearchQuery by remember { mutableStateOf("") }
    var selectedUserForDetails by remember { mutableStateOf<UserProfile?>(null) }
    var walletAdjustUser by remember { mutableStateOf<UserProfile?>(null) }
    var depositDeltaInput by remember { mutableStateOf("100") }
    var winningDeltaInput by remember { mutableStateOf("0") }
    var bonusDeltaInput by remember { mutableStateOf("0") }

    var rejectingRequestId by remember { mutableStateOf<String?>(null) }
    var rejectReasonInput by remember { mutableStateOf("Transaction ID / UTR verification failed") }

    var rejectingWithdrawalId by remember { mutableStateOf<String?>(null) }
    var rejectWithdrawalReasonInput by remember { mutableStateOf("UPI details mismatched") }

    var replyTicketId by remember { mutableStateOf<String?>(null) }
    var replyTextInput by remember { mutableStateOf("") }

    var pushTitleInput by remember { mutableStateOf("") }
    var pushMessageInput by remember { mutableStateOf("") }

    var upiIdConfigInput by remember(adminConfig.manualUpiId) { mutableStateOf(adminConfig.manualUpiId) }
    var qrUrlConfigInput by remember(adminConfig.manualQrUrl) { mutableStateOf(adminConfig.manualQrUrl) }
    var qrInstructionInput by remember(adminConfig.manualQrInstruction) { mutableStateOf(adminConfig.manualQrInstruction) }

    var gatewayProviderInput by remember(adminConfig.gatewayProvider) { mutableStateOf(adminConfig.gatewayProvider) }
    var merchantIdInput by remember(adminConfig.gatewayMerchantId) { mutableStateOf(adminConfig.gatewayMerchantId) }
    var apiKeyInput by remember(adminConfig.gatewayApiKey) { mutableStateOf(adminConfig.gatewayApiKey) }
    var secretKeyInput by remember(adminConfig.gatewaySecretKey) { mutableStateOf(adminConfig.gatewaySecretKey) }

    var telegramLinkInput by remember(adminConfig.telegramContact) { mutableStateOf(adminConfig.telegramContact) }
    var whatsappNumberInput by remember(adminConfig.whatsappContact) { mutableStateOf(adminConfig.whatsappContact) }

    var snackbarHostState = remember { SnackbarHostState() }
    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMsg = null
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(BgDark).testTag("admin_portal_container")) {
        val isWideScreen = maxWidth >= 720.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = BgDark
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (isWideScreen) {
                    // DESKTOP / TABLET RESPONSIVE SPLIT LAYOUT
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left Permanent Side Navigation Drawer
                        Surface(
                            color = CardDark,
                            tonalElevation = 6.dp,
                            modifier = Modifier
                                .width(260.dp)
                                .fillMaxHeight()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    // Admin Header
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(ErrorRed.copy(alpha = 0.2f))
                                                .border(1.dp, ErrorRed, RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Shield, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("WINX7 ADMIN", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                            Text("Control Center v2.4", color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Admin User Card
                                    Surface(
                                        color = SurfaceDark,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(currentUser.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                            Text(currentUser.email, color = TextGrey, fontSize = 10.sp, maxLines = 1)
                                            Text("Role: ${currentUser.role.name}", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }

                                    HorizontalDivider(color = SurfaceBorder, modifier = Modifier.padding(bottom = 12.dp))

                                    // Nav Items List
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        items(AdminPage.values()) { page ->
                                            val isSelected = activePage == page
                                            Surface(
                                                color = if (isSelected) ErrorRed.copy(alpha = 0.2f) else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp),
                                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ErrorRed) else null,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { activePage = page }
                                                    .testTag("admin_nav_${page.name.lowercase()}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = page.icon,
                                                        contentDescription = page.title,
                                                        tint = if (isSelected) ErrorRed else TextGrey,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        text = page.title,
                                                        color = if (isSelected) TextWhite else TextGrey,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 12.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Exit to User App Button
                                Button(
                                    onClick = onReturnToUserApp,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Log Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        VerticalDivider(color = SurfaceBorder, modifier = Modifier.fillMaxHeight())

                        // Right Main Content Pane
                        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            // Top Bar
                            Surface(
                                color = SurfaceDark,
                                tonalElevation = 4.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(activePage.icon, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(22.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(activePage.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Surface(
                                            color = SuccessGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("SYSTEM ONLINE", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }
                                }
                            }

                            // Content Page View
                            Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                                RenderAdminPageContent(
                                    page = activePage,
                                    viewModel = viewModel,
                                    tournaments = tournaments,
                                    allUsers = allUsers,
                                    adminConfig = adminConfig,
                                    categories = categories,
                                    staffUsers = staffUsers,
                                    tickets = tickets,
                                    depositRequests = depositRequests,
                                    transactions = transactions,
                                    notifications = notifications,
                                    userSearchQuery = userSearchQuery,
                                    onUserSearchQueryChange = { userSearchQuery = it },
                                    onOpenCreateTournament = { showCreateTournamentDialog = true },
                                    onOpenAddStaff = { showAddStaffDialog = true },
                                    onOpenAddCategory = { showAddCategoryDialog = true },
                                    onEditRoom = { tour ->
                                        editingTournament = tour
                                        editRoomId = tour.roomId
                                        editRoomPass = tour.roomPassword
                                    },
                                    onEditTournamentDetails = { tour ->
                                        editingTournamentDetails = tour
                                        editTitleInput = tour.title
                                        editEntryFeeInput = tour.entryFee.toInt().toString()
                                        editPrizePoolInput = tour.prizePool.toInt().toString()
                                        editMapInput = tour.mapName
                                        editThumbnailInput = tour.thumbnailUrl
                                    },
                                    onOpenWalletAdjust = { usr ->
                                        walletAdjustUser = usr
                                        depositDeltaInput = "100"
                                        winningDeltaInput = "0"
                                        bonusDeltaInput = "0"
                                    },
                                    onOpenRejectDeposit = { id ->
                                        rejectingRequestId = id
                                        rejectReasonInput = "Transaction ID / UTR verification failed"
                                    },
                                    onOpenRejectWithdrawal = { id ->
                                        rejectingWithdrawalId = id
                                        rejectWithdrawalReasonInput = "UPI details mismatched"
                                    },
                                    onOpenReplyTicket = { tkt ->
                                        replyTicketId = tkt.id
                                        replyTextInput = tkt.reply
                                    },
                                    onShowMessage = { snackbarMsg = it },
                                    pushTitleInput = pushTitleInput,
                                    onPushTitleChange = { pushTitleInput = it },
                                    pushMessageInput = pushMessageInput,
                                    onPushMessageChange = { pushMessageInput = it },
                                    upiIdConfigInput = upiIdConfigInput,
                                    onUpiIdConfigChange = { upiIdConfigInput = it },
                                    qrUrlConfigInput = qrUrlConfigInput,
                                    onQrUrlConfigChange = { qrUrlConfigInput = it },
                                    qrInstructionInput = qrInstructionInput,
                                    onQrInstructionChange = { qrInstructionInput = it },
                                    gatewayProviderInput = gatewayProviderInput,
                                    onGatewayProviderChange = { gatewayProviderInput = it },
                                    merchantIdInput = merchantIdInput,
                                    onMerchantIdChange = { merchantIdInput = it },
                                    apiKeyInput = apiKeyInput,
                                    onApiKeyChange = { apiKeyInput = it },
                                    secretKeyInput = secretKeyInput,
                                    onSecretKeyChange = { secretKeyInput = it },
                                    telegramLinkInput = telegramLinkInput,
                                    onTelegramLinkChange = { telegramLinkInput = it },
                                    whatsappNumberInput = whatsappNumberInput,
                                    onWhatsappNumberChange = { whatsappNumberInput = it }
                                )
                            }
                        }
                    }
                } else {
                    // MOBILE RESPONSIVE LAYOUT
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top Mobile Bar
                        Surface(
                            color = SurfaceDark,
                            tonalElevation = 6.dp,
                            modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ErrorRed)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ADMIN", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(activePage.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }

                                Button(
                                    onClick = onReturnToUserApp,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Log Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Mobile Horizontal Scrollable Page Tabs
                        ScrollableTabRow(
                            selectedTabIndex = activePage.ordinal,
                            containerColor = CardDark,
                            contentColor = ErrorRed,
                            edgePadding = 10.dp
                        ) {
                            AdminPage.values().forEach { page ->
                                Tab(
                                    selected = activePage == page,
                                    onClick = { activePage = page },
                                    text = {
                                        Text(
                                            text = page.title,
                                            fontWeight = if (activePage == page) FontWeight.Bold else FontWeight.Normal,
                                            color = if (activePage == page) ErrorRed else TextGrey,
                                            fontSize = 12.sp
                                        )
                                    }
                                )
                            }
                        }

                        // Mobile Content
                        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            RenderAdminPageContent(
                                page = activePage,
                                viewModel = viewModel,
                                tournaments = tournaments,
                                allUsers = allUsers,
                                adminConfig = adminConfig,
                                categories = categories,
                                staffUsers = staffUsers,
                                tickets = tickets,
                                depositRequests = depositRequests,
                                transactions = transactions,
                                notifications = notifications,
                                userSearchQuery = userSearchQuery,
                                onUserSearchQueryChange = { userSearchQuery = it },
                                onOpenCreateTournament = { showCreateTournamentDialog = true },
                                onOpenAddStaff = { showAddStaffDialog = true },
                                onOpenAddCategory = { showAddCategoryDialog = true },
                                onEditRoom = { tour ->
                                    editingTournament = tour
                                    editRoomId = tour.roomId
                                    editRoomPass = tour.roomPassword
                                },
                                onEditTournamentDetails = { tour ->
                                    editingTournamentDetails = tour
                                    editTitleInput = tour.title
                                    editEntryFeeInput = tour.entryFee.toInt().toString()
                                    editPrizePoolInput = tour.prizePool.toInt().toString()
                                    editMapInput = tour.mapName
                                    editThumbnailInput = tour.thumbnailUrl
                                },
                                onOpenWalletAdjust = { usr ->
                                    walletAdjustUser = usr
                                    depositDeltaInput = "100"
                                    winningDeltaInput = "0"
                                    bonusDeltaInput = "0"
                                },
                                onOpenRejectDeposit = { id ->
                                    rejectingRequestId = id
                                    rejectReasonInput = "Transaction ID / UTR verification failed"
                                },
                                onOpenRejectWithdrawal = { id ->
                                    rejectingWithdrawalId = id
                                    rejectWithdrawalReasonInput = "UPI details mismatched"
                                },
                                onOpenReplyTicket = { tkt ->
                                    replyTicketId = tkt.id
                                    replyTextInput = tkt.reply
                                },
                                onShowMessage = { snackbarMsg = it },
                                pushTitleInput = pushTitleInput,
                                onPushTitleChange = { pushTitleInput = it },
                                pushMessageInput = pushMessageInput,
                                onPushMessageChange = { pushMessageInput = it },
                                upiIdConfigInput = upiIdConfigInput,
                                onUpiIdConfigChange = { upiIdConfigInput = it },
                                qrUrlConfigInput = qrUrlConfigInput,
                                onQrUrlConfigChange = { qrUrlConfigInput = it },
                                qrInstructionInput = qrInstructionInput,
                                onQrInstructionChange = { qrInstructionInput = it },
                                gatewayProviderInput = gatewayProviderInput,
                                onGatewayProviderChange = { gatewayProviderInput = it },
                                merchantIdInput = merchantIdInput,
                                onMerchantIdChange = { merchantIdInput = it },
                                apiKeyInput = apiKeyInput,
                                onApiKeyChange = { apiKeyInput = it },
                                secretKeyInput = secretKeyInput,
                                onSecretKeyChange = { secretKeyInput = it },
                                telegramLinkInput = telegramLinkInput,
                                onTelegramLinkChange = { telegramLinkInput = it },
                                whatsappNumberInput = whatsappNumberInput,
                                onWhatsappNumberChange = { whatsappNumberInput = it }
                            )
                        }
                    }
                }
            }
        }

        // --- MODAL DIALOGS ---

        // 1. CREATE TOURNAMENT MODAL
        if (showCreateTournamentDialog) {
            AlertDialog(
                onDismissRequest = { showCreateTournamentDialog = false },
                title = { Text("Create New Tournament", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Tournament Title", color = TextGrey) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("dialog_tour_title"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newMap,
                                onValueChange = { newMap = it },
                                label = { Text("Map (e.g., Bermuda)", color = TextGrey) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )
                            OutlinedTextField(
                                value = newMode,
                                onValueChange = { newMode = it },
                                label = { Text("Mode (Solo/Duo/Squad)", color = TextGrey) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newEntryFee,
                                onValueChange = { newEntryFee = it },
                                label = { Text("Entry Fee (₹)", color = TextGrey) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )
                            OutlinedTextField(
                                value = newPrizePool,
                                onValueChange = { newPrizePool = it },
                                label = { Text("Prize Pool (₹)", color = TextGrey) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newKillReward,
                                onValueChange = { newKillReward = it },
                                label = { Text("Kill Reward (₹)", color = TextGrey) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )
                            OutlinedTextField(
                                value = newTotalSlots,
                                onValueChange = { newTotalSlots = it },
                                label = { Text("Total Slots", color = TextGrey) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )
                        }
                        OutlinedTextField(
                            value = newThumbnailUrl,
                            onValueChange = { newThumbnailUrl = it },
                            label = { Text("Thumbnail / Banner Image URL (Optional)", color = TextGrey) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTitle.isBlank()) return@Button
                            val tour = Tournament(
                                title = newTitle,
                                categoryId = "cat-custom",
                                categoryName = newMode,
                                mapName = newMap,
                                mode = newMode,
                                entryFee = newEntryFee.toDoubleOrNull() ?: 0.0,
                                prizePool = newPrizePool.toDoubleOrNull() ?: 0.0,
                                killReward = newKillReward.toDoubleOrNull() ?: 0.0,
                                totalSlots = newTotalSlots.toIntOrNull() ?: 48,
                                rules = newRules,
                                thumbnailUrl = newThumbnailUrl
                            )
                            viewModel.createTournament(tour)
                            showCreateTournamentDialog = false
                            snackbarMsg = "Tournament created successfully!"
                            newTitle = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("CREATE MATCH", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateTournamentDialog = false }) {
                        Text("Cancel", color = TextGrey)
                    }
                },
                containerColor = CardDark
            )
        }

        // 2. EDIT ROOM DETAILS MODAL
        editingTournament?.let { tour ->
            AlertDialog(
                onDismissRequest = { editingTournament = null },
                title = { Text("Room ID & Password for ${tour.title}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editRoomId,
                            onValueChange = { editRoomId = it },
                            label = { Text("Room ID", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_room_id"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        OutlinedTextField(
                            value = editRoomPass,
                            onValueChange = { editRoomPass = it },
                            label = { Text("Room Password", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_room_pass"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateRoomDetails(tour.id, editRoomId, editRoomPass)
                            editingTournament = null
                            snackbarMsg = "Room details updated and broadcasted to players!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text("UPDATE ROOM", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingTournament = null }) { Text("Cancel", color = TextGrey) }
                },
                containerColor = CardDark
            )
        }

        // 3. EDIT TOURNAMENT DETAILS MODAL
        editingTournamentDetails?.let { tour ->
            AlertDialog(
                onDismissRequest = { editingTournamentDetails = null },
                title = { Text("Edit Tournament #${tour.id.takeLast(4)}", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editTitleInput,
                            onValueChange = { editTitleInput = it },
                            label = { Text("Tournament Title", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editEntryFeeInput,
                                onValueChange = { editEntryFeeInput = it },
                                label = { Text("Entry Fee", color = TextGrey) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )
                            OutlinedTextField(
                                value = editPrizePoolInput,
                                onValueChange = { editPrizePoolInput = it },
                                label = { Text("Prize Pool", color = TextGrey) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )
                        }
                        OutlinedTextField(
                            value = editMapInput,
                            onValueChange = { editMapInput = it },
                            label = { Text("Map Name", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        OutlinedTextField(
                            value = editThumbnailInput,
                            onValueChange = { editThumbnailInput = it },
                            label = { Text("Image Banner URL", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val updated = tour.copy(
                                title = editTitleInput,
                                entryFee = editEntryFeeInput.toDoubleOrNull() ?: tour.entryFee,
                                prizePool = editPrizePoolInput.toDoubleOrNull() ?: tour.prizePool,
                                mapName = editMapInput,
                                thumbnailUrl = editThumbnailInput
                            )
                            viewModel.updateTournamentDetails(updated)
                            editingTournamentDetails = null
                            snackbarMsg = "Tournament details updated!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                    ) {
                        Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingTournamentDetails = null }) { Text("Cancel", color = TextGrey) }
                },
                containerColor = CardDark
            )
        }

        // 4. CREATE STAFF ROLE MODAL
        if (showAddStaffDialog) {
            AlertDialog(
                onDismissRequest = { showAddStaffDialog = false },
                title = { Text("Create Staff Member & Assign Role", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newStaffName,
                            onValueChange = { newStaffName = it },
                            label = { Text("Full Name", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_staff_name"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        OutlinedTextField(
                            value = newStaffEmail,
                            onValueChange = { newStaffEmail = it },
                            label = { Text("Staff Email", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_staff_email"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        OutlinedTextField(
                            value = newStaffRoleTitle,
                            onValueChange = { newStaffRoleTitle = it },
                            label = { Text("Role Title (e.g. Match Moderator)", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_staff_role"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )

                        Text("Assign Role Permissions:", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        listOf("UPLOAD_ROOM", "UPDATE_RESULTS", "VERIFY_DEPOSITS", "SUPPORT_TICKETS", "APP_CONFIG").forEach { perm ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPermissions = if (selectedPermissions.contains(perm)) {
                                            selectedPermissions - perm
                                        } else {
                                            selectedPermissions + perm
                                        }
                                    }
                            ) {
                                Checkbox(
                                    checked = selectedPermissions.contains(perm),
                                    onCheckedChange = { checked ->
                                        selectedPermissions = if (checked) selectedPermissions + perm else selectedPermissions - perm
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = ErrorRed)
                                )
                                Text(perm.replace("_", " "), color = TextWhite, fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newStaffName.isBlank() || newStaffEmail.isBlank()) return@Button
                            viewModel.addStaffUserWithPermissions(newStaffName, newStaffEmail, newStaffRoleTitle, selectedPermissions.toList())
                            showAddStaffDialog = false
                            snackbarMsg = "Staff member created successfully with permissions!"
                            newStaffName = ""
                            newStaffEmail = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("CREATE STAFF ROLE", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddStaffDialog = false }) { Text("Cancel", color = TextGrey) }
                },
                containerColor = CardDark
            )
        }

        // 5. ADJUST WALLET MODAL
        walletAdjustUser?.let { usr ->
            AlertDialog(
                onDismissRequest = { walletAdjustUser = null },
                title = { Text("Adjust Wallet Balances for ${usr.name}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current Deposit: ₹${usr.depositBalance.toInt()} | Winning: ₹${usr.winningBalance.toInt()} | Bonus: ₹${usr.bonusBalance.toInt()}", color = AccentGold, fontSize = 11.sp)

                        OutlinedTextField(
                            value = depositDeltaInput,
                            onValueChange = { depositDeltaInput = it },
                            label = { Text("Deposit Delta (+/- ₹)", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        OutlinedTextField(
                            value = winningDeltaInput,
                            onValueChange = { winningDeltaInput = it },
                            label = { Text("Winning Delta (+/- ₹)", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        OutlinedTextField(
                            value = bonusDeltaInput,
                            onValueChange = { bonusDeltaInput = it },
                            label = { Text("Bonus Delta (+/- ₹)", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val depD = depositDeltaInput.toDoubleOrNull() ?: 0.0
                            val winD = winningDeltaInput.toDoubleOrNull() ?: 0.0
                            val bonD = bonusDeltaInput.toDoubleOrNull() ?: 0.0
                            viewModel.adjustUserWallet(usr.id, depD, winD, bonD)
                            walletAdjustUser = null
                            snackbarMsg = "Wallet updated for ${usr.name}!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text("APPLY BALANCE CHANGE", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { walletAdjustUser = null }) { Text("Cancel", color = TextGrey) }
                },
                containerColor = CardDark
            )
        }

        // 6. REJECT DEPOSIT MODAL
        rejectingRequestId?.let { reqId ->
            AlertDialog(
                onDismissRequest = { rejectingRequestId = null },
                title = { Text("Reject Deposit Request", color = ErrorRed, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Please specify the reason for rejection:", color = TextGrey, fontSize = 12.sp)
                        OutlinedTextField(
                            value = rejectReasonInput,
                            onValueChange = { rejectReasonInput = it },
                            label = { Text("Rejection Reason", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.rejectDepositRequest(reqId, rejectReasonInput) { _, msg ->
                                snackbarMsg = msg
                            }
                            rejectingRequestId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("CONFIRM REJECTION", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { rejectingRequestId = null }) { Text("Cancel", color = TextGrey) }
                },
                containerColor = CardDark
            )
        }

        // 7. REJECT WITHDRAWAL MODAL
        rejectingWithdrawalId?.let { txnId ->
            AlertDialog(
                onDismissRequest = { rejectingWithdrawalId = null },
                title = { Text("Reject Withdrawal & Refund", color = ErrorRed, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Amount will be automatically refunded back to the user's winning wallet.", color = TextGrey, fontSize = 12.sp)
                        OutlinedTextField(
                            value = rejectWithdrawalReasonInput,
                            onValueChange = { rejectWithdrawalReasonInput = it },
                            label = { Text("Rejection Reason", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val (success, msg) = viewModel.rejectWithdrawal(txnId, rejectWithdrawalReasonInput)
                            snackbarMsg = msg
                            rejectingWithdrawalId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("REJECT & REFUND", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { rejectingWithdrawalId = null }) { Text("Cancel", color = TextGrey) }
                },
                containerColor = CardDark
            )
        }

        // 8. REPLY TICKET MODAL
        replyTicketId?.let { ticketId ->
            AlertDialog(
                onDismissRequest = { replyTicketId = null },
                title = { Text("Reply to Support Ticket", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = replyTextInput,
                            onValueChange = { replyTextInput = it },
                            label = { Text("Admin Reply Message", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (replyTextInput.isNotBlank()) {
                                viewModel.replyTicket(ticketId, replyTextInput)
                                replyTicketId = null
                                snackbarMsg = "Ticket reply sent and resolved!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text("SEND REPLY & RESOLVE", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { replyTicketId = null }) { Text("Cancel", color = TextGrey) }
                },
                containerColor = CardDark
            )
        }

        // 9. ADD CATEGORY MODAL
        if (showAddCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showAddCategoryDialog = false },
                title = { Text("Add New Tournament Category", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Category Name (e.g. 4v4 Unlimited)", color = TextGrey) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                viewModel.addCategory(newCategoryName)
                                showAddCategoryDialog = false
                                snackbarMsg = "Category '$newCategoryName' created!"
                                newCategoryName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("ADD CATEGORY", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCategoryDialog = false }) { Text("Cancel", color = TextGrey) }
                },
                containerColor = CardDark
            )
        }
    }
}

// MAIN PAGE CONTENT RENDERER
@Composable
fun RenderAdminPageContent(
    page: AdminPage,
    viewModel: WinX7ViewModel,
    tournaments: List<Tournament>,
    allUsers: List<UserProfile>,
    adminConfig: AdminConfig,
    categories: List<Category>,
    staffUsers: List<StaffUser>,
    tickets: List<SupportTicket>,
    depositRequests: List<DepositRequest>,
    transactions: List<WalletTransaction>,
    notifications: List<AppNotification>,
    userSearchQuery: String,
    onUserSearchQueryChange: (String) -> Unit,
    onOpenCreateTournament: () -> Unit,
    onOpenAddStaff: () -> Unit,
    onOpenAddCategory: () -> Unit,
    onEditRoom: (Tournament) -> Unit,
    onEditTournamentDetails: (Tournament) -> Unit,
    onOpenWalletAdjust: (UserProfile) -> Unit,
    onOpenRejectDeposit: (String) -> Unit,
    onOpenRejectWithdrawal: (String) -> Unit,
    onOpenReplyTicket: (SupportTicket) -> Unit,
    onShowMessage: (String) -> Unit,
    pushTitleInput: String,
    onPushTitleChange: (String) -> Unit,
    pushMessageInput: String,
    onPushMessageChange: (String) -> Unit,
    upiIdConfigInput: String,
    onUpiIdConfigChange: (String) -> Unit,
    qrUrlConfigInput: String,
    onQrUrlConfigChange: (String) -> Unit,
    qrInstructionInput: String,
    onQrInstructionChange: (String) -> Unit,
    gatewayProviderInput: String,
    onGatewayProviderChange: (String) -> Unit,
    merchantIdInput: String,
    onMerchantIdChange: (String) -> Unit,
    apiKeyInput: String,
    onApiKeyChange: (String) -> Unit,
    secretKeyInput: String,
    onSecretKeyChange: (String) -> Unit,
    telegramLinkInput: String,
    onTelegramLinkChange: (String) -> Unit,
    whatsappNumberInput: String,
    onWhatsappNumberChange: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        when (page) {
            AdminPage.DASHBOARD -> {
                item {
                    Text("Realtime Analytics Dashboard", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminMetricCard(title = "TOTAL USERS", value = "${allUsers.size}", icon = Icons.Default.People, color = PrimaryPurple, modifier = Modifier.weight(1f))
                        AdminMetricCard(title = "TOTAL MATCHES", value = "${tournaments.size}", icon = Icons.Default.SportsEsports, color = AccentGold, modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val pendingDeposits = depositRequests.count { it.status == DepositStatus.PENDING }
                        val pendingWithdrawals = transactions.count { it.type == TransactionType.WITHDRAW && it.status == TransactionStatus.PENDING }
                        AdminMetricCard(title = "PENDING DEPOSITS", value = "$pendingDeposits", icon = Icons.Default.AccountBalanceWallet, color = if (pendingDeposits > 0) AccentGold else TextGrey, modifier = Modifier.weight(1f))
                        AdminMetricCard(title = "PENDING PAYOUTS", value = "$pendingWithdrawals", icon = Icons.Default.Payments, color = if (pendingWithdrawals > 0) ErrorRed else TextGrey, modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Match Status Breakdown", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("UPCOMING", color = TextGrey, fontSize = 10.sp)
                                    Text("${tournaments.count { it.status == TournamentStatus.UPCOMING }}", color = PrimaryPurple, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("LIVE NOW", color = TextGrey, fontSize = 10.sp)
                                    Text("${tournaments.count { it.status == TournamentStatus.LIVE }}", color = SuccessGreen, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("COMPLETED", color = TextGrey, fontSize = 10.sp)
                                    Text("${tournaments.count { it.status == TournamentStatus.COMPLETED }}", color = AccentGold, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            }

            AdminPage.USER_MANAGEMENT -> {
                item {
                    Text("User Management & Wallets", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                item {
                    OutlinedTextField(
                        value = userSearchQuery,
                        onValueChange = onUserSearchQueryChange,
                        label = { Text("Search users by Name, Email or Phone", color = TextGrey) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGrey) },
                        modifier = Modifier.fillMaxWidth().testTag("admin_user_search"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                }

                val filteredUsers = allUsers.filter {
                    userSearchQuery.isBlank() ||
                            it.name.contains(userSearchQuery, ignoreCase = true) ||
                            it.email.contains(userSearchQuery, ignoreCase = true) ||
                            it.phone.contains(userSearchQuery, ignoreCase = true)
                }

                items(filteredUsers) { usr ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth().testTag("admin_user_card_${usr.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(usr.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${usr.email} • ${usr.phone}", color = TextGrey, fontSize = 11.sp)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (usr.isBanned || usr.isSuspended) ErrorRed.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (usr.isBanned) "BANNED" else if (usr.isSuspended) "SUSPENDED" else "ACTIVE",
                                        color = if (usr.isBanned || usr.isSuspended) ErrorRed else SuccessGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Deposit: ₹${usr.depositBalance.toInt()} | Winning: ₹${usr.winningBalance.toInt()} | Bonus: ₹${usr.bonusBalance.toInt()}", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onOpenWalletAdjust(usr) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Text("Adjust Wallet", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.banUser(usr.id, !usr.isBanned) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Text(if (usr.isBanned) "Unban User" else "Ban User", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            AdminPage.STAFF_MANAGEMENT -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Staff Management & Roles", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Button(
                            onClick = onOpenAddStaff,
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_staff_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CREATE STAFF ROLE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(staffUsers) { stf ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth().testTag("staff_card_${stf.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stf.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${stf.email} • Role: ${stf.roleTitle}", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Permissions: ${stf.permissions.joinToString(", ")}", color = TextGrey, fontSize = 10.sp)
                            }

                            IconButton(
                                onClick = {
                                    viewModel.deleteStaffUser(stf.id)
                                    onShowMessage("Staff member removed.")
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Staff", tint = ErrorRed)
                            }
                        }
                    }
                }
            }

            AdminPage.TOURNAMENT_MGMT -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tournament Management", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Button(
                            onClick = onOpenCreateTournament,
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("admin_create_tournament_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CREATE MATCH", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(tournaments) { tour ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth().testTag("admin_tournament_card_${tour.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tour.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Status: ${tour.status.name}", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Map: ${tour.mapName} • Entry: ₹${tour.entryFee.toInt()} • Prize: ₹${tour.prizePool.toInt()} • Slots: ${tour.joinedSlots}/${tour.totalSlots}", color = TextGrey, fontSize = 11.sp)

                            if (tour.roomId.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Room ID: ${tour.roomId} | Pass: ${tour.roomPassword}", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onEditRoom(tour) },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Room ID", fontSize = 10.sp)
                                }

                                Button(
                                    onClick = { onEditTournamentDetails(tour) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Edit Data", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val nextStatus = when (tour.status) {
                                            TournamentStatus.UPCOMING -> TournamentStatus.LIVE
                                            TournamentStatus.LIVE -> TournamentStatus.COMPLETED
                                            else -> TournamentStatus.UPCOMING
                                        }
                                        viewModel.updateTournamentStatus(tour.id, nextStatus)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Next Status", fontSize = 10.sp)
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.deleteTournament(tour.id)
                                        onShowMessage("Tournament deleted!")
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }

            AdminPage.CREATE_EDIT_TOURNAMENT -> {
                item {
                    Text("Quick Match Creation Hub", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Launch New Free Fire Match", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Click below to configure entry fee, kill rewards, maps, and slot rules.", color = TextGrey, fontSize = 12.sp)

                            Button(
                                onClick = onOpenCreateTournament,
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Icon(Icons.Default.AddBox, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("OPEN CREATE MATCH FORM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            AdminPage.DEPOSIT_REQUESTS -> {
                item {
                    Text("Wallet Deposit Verification Requests", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (depositRequests.isEmpty()) {
                    item {
                        Surface(color = CardDark, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Text("No deposit requests found.", color = TextGrey, modifier = Modifier.padding(16.dp), fontSize = 12.sp)
                        }
                    }
                } else {
                    items(depositRequests) { req ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.fillMaxWidth().testTag("deposit_request_${req.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    when (req.status) {
                                                        DepositStatus.PENDING -> AccentGold.copy(alpha = 0.2f)
                                                        DepositStatus.APPROVED -> SuccessGreen.copy(alpha = 0.2f)
                                                        DepositStatus.REJECTED -> ErrorRed.copy(alpha = 0.2f)
                                                    }
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                req.status.name,
                                                color = when (req.status) {
                                                    DepositStatus.PENDING -> AccentGold
                                                    DepositStatus.APPROVED -> SuccessGreen
                                                    DepositStatus.REJECTED -> ErrorRed
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("₹${req.amount.toInt()}", color = SuccessGreen, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    }
                                    Text(req.timestamp, color = TextGrey, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("User: ${req.userName} (${req.userPhone})", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Mode: ${req.paymentMode} • UTR / Txn ID: ${req.txnId}", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                if (req.status == DepositStatus.PENDING) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.approveDepositRequest(req.id) { _, msg ->
                                                    onShowMessage(msg)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Text("APPROVE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { onOpenRejectDeposit(req.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdminPage.WITHDRAWAL_REQUESTS -> {
                item {
                    Text("User Withdrawal Requests", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                val withdrawals = transactions.filter { it.type == TransactionType.WITHDRAW }

                if (withdrawals.isEmpty()) {
                    item {
                        Surface(color = CardDark, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Text("No withdrawal requests found.", color = TextGrey, modifier = Modifier.padding(16.dp), fontSize = 12.sp)
                        }
                    }
                } else {
                    items(withdrawals) { w ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Amount: ₹${w.amount.toInt()}", color = ErrorRed, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    Text(w.status.name, color = if (w.status == TransactionStatus.SUCCESS) SuccessGreen else AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("${w.title} (${w.timestamp})", color = TextWhite, fontSize = 12.sp)

                                if (w.status == TransactionStatus.PENDING) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                val (success, msg) = viewModel.approveWithdrawal(w.id)
                                                onShowMessage(msg)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Text("APPROVE PAYOUT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { onOpenRejectWithdrawal(w.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Text("REJECT & REFUND", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdminPage.SYSTEM_MANAGEMENT -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Category & Content Operations", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Button(
                            onClick = onOpenAddCategory,
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ADD CATEGORY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(categories) { cat ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(
                                onClick = {
                                    viewModel.deleteCategory(cat.id)
                                    onShowMessage("Category removed.")
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    }
                }
            }

            AdminPage.NOTIFICATIONS -> {
                item {
                    Text("Push Notification Broadcasting", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = pushTitleInput,
                                onValueChange = onPushTitleChange,
                                label = { Text("Notification Title", color = TextGrey) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("push_title_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )

                            OutlinedTextField(
                                value = pushMessageInput,
                                onValueChange = onPushMessageChange,
                                label = { Text("Notification Message Body", color = TextGrey) },
                                modifier = Modifier.fillMaxWidth().testTag("push_message_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )

                            Button(
                                onClick = {
                                    if (pushTitleInput.isNotBlank() && pushMessageInput.isNotBlank()) {
                                        viewModel.sendPushNotification(pushTitleInput, pushMessageInput)
                                        onShowMessage("Push Notification broadcasted to all users!")
                                        onPushTitleChange("")
                                        onPushMessageChange("")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("BROADCAST PUSH NOTIFICATION", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            AdminPage.SUPPORT_TICKETS -> {
                item {
                    Text("User Support Tickets", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (tickets.isEmpty()) {
                    item {
                        Surface(color = CardDark, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Text("No support tickets submitted.", color = TextGrey, modifier = Modifier.padding(16.dp), fontSize = 12.sp)
                        }
                    }
                } else {
                    items(tickets) { tkt ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tkt.subject, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(tkt.status, color = if (tkt.status == "RESOLVED") SuccessGreen else AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("User: ${tkt.userName}", color = TextGrey, fontSize = 11.sp)
                                Text("Message: ${tkt.message}", color = TextWhite, fontSize = 12.sp)

                                if (tkt.reply.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Reply: ${tkt.reply}", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onOpenReplyTicket(tkt) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().height(34.dp)
                                ) {
                                    Text("REPLY TICKET", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            AdminPage.REPORTS_ANALYTICS -> {
                item {
                    Text("Financial & Tournament Analytics", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Financial Performance Summary", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Est. Gross Revenue", color = TextGrey, fontSize = 10.sp)
                                    Text("₹64,200", color = SuccessGreen, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                }
                                Column {
                                    Text("Total Deposits", color = TextGrey, fontSize = 10.sp)
                                    Text("₹82,500", color = PrimaryPurple, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                }
                                Column {
                                    Text("Total Withdrawals", color = TextGrey, fontSize = 10.sp)
                                    Text("₹18,300", color = ErrorRed, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }

            AdminPage.APP_SETTINGS -> {
                item {
                    Text("Global System Settings & Mode Toggles", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("app_config_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            AdminToggleRow("Registration Active", adminConfig.isRegistrationOn) {
                                viewModel.updateConfig(adminConfig.copy(isRegistrationOn = it))
                            }
                            HorizontalDivider(color = SurfaceBorder)
                            AdminToggleRow("Tournaments Active", adminConfig.isTournamentOn) {
                                viewModel.updateConfig(adminConfig.copy(isTournamentOn = it))
                            }
                            HorizontalDivider(color = SurfaceBorder)
                            AdminToggleRow("Withdrawals Active", adminConfig.isWithdrawOn) {
                                viewModel.updateConfig(adminConfig.copy(isWithdrawOn = it))
                            }
                            HorizontalDivider(color = SurfaceBorder)
                            AdminToggleRow("Deposits Active", adminConfig.isDepositOn) {
                                viewModel.updateConfig(adminConfig.copy(isDepositOn = it))
                            }
                            HorizontalDivider(color = SurfaceBorder)
                            AdminToggleRow("Maintenance Mode", adminConfig.isMaintenanceMode) {
                                viewModel.updateConfig(adminConfig.copy(isMaintenanceMode = it))
                            }
                        }
                    }
                }

                item {
                    Text("Manual UPI & Payment Gateway Setup", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Active Deposit Mode", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = adminConfig.depositMode == "MANUAL",
                                    onClick = {
                                        viewModel.updateConfig(adminConfig.copy(depositMode = "MANUAL"))
                                        onShowMessage("Deposit mode set to Manual UPI QR!")
                                    },
                                    label = { Text("Manual UPI QR") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = adminConfig.depositMode == "GATEWAY",
                                    onClick = {
                                        viewModel.updateConfig(adminConfig.copy(depositMode = "GATEWAY"))
                                        onShowMessage("Deposit mode set to Payment Gateway API!")
                                    },
                                    label = { Text("Gateway API") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (adminConfig.depositMode == "MANUAL") {
                                OutlinedTextField(
                                    value = upiIdConfigInput,
                                    onValueChange = onUpiIdConfigChange,
                                    label = { Text("Admin Official UPI ID", color = TextGrey) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )

                                OutlinedTextField(
                                    value = qrUrlConfigInput,
                                    onValueChange = onQrUrlConfigChange,
                                    label = { Text("QR Code URL", color = TextGrey) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )

                                Button(
                                    onClick = {
                                        viewModel.updateConfig(adminConfig.copy(manualUpiId = upiIdConfigInput, manualQrUrl = qrUrlConfigInput))
                                        onShowMessage("Manual UPI settings saved!")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("SAVE MANUAL DEPOSIT CONFIG", fontWeight = FontWeight.Bold)
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
fun AdminMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = TextGrey, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(value, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun AdminToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextWhite,
                checkedTrackColor = ErrorRed,
                uncheckedThumbColor = TextGrey,
                uncheckedTrackColor = SurfaceDark
            )
        )
    }
}
