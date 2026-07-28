package com.example.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.DepositStatus
import com.example.data.models.TransactionStatus
import com.example.data.models.TransactionType
import com.example.ui.theme.*
import com.example.ui.viewmodel.WinX7ViewModel
import com.example.ui.components.RefreshableLayout

@Composable
fun WalletScreen(
    viewModel: WinX7ViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val adminConfig by viewModel.adminConfig.collectAsState()
    val depositRequests by viewModel.depositRequests.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    var depositInput by remember { mutableStateOf("100") }
    var txnIdInput by remember { mutableStateOf("") }
    var withdrawInput by remember { mutableStateOf("100") }
    var upiIdInput by remember { mutableStateOf("") }
    var couponCodeInput by remember { mutableStateOf("") }

    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    var isRefreshing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        RefreshableLayout(isRefreshing = isRefreshing, onRefresh = {
            isRefreshing = true
            viewModel.refreshAllData()
        }) {
            LaunchedEffect(isRefreshing) {
                if (isRefreshing) {
                    kotlinx.coroutines.delay(800)
                    isRefreshing = false
                }
            }
            LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Main Balance Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("wallet_balance_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryPurple.copy(alpha = 0.8f), SecondaryPurple.copy(alpha = 0.8f))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("TOTAL WALLET BALANCE", color = TextGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("₹${currentUser.totalBalance.toInt()}", color = AccentGold, fontSize = 32.sp, fontWeight = FontWeight.Black)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AccentGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("24/7 INSTANT", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // 3 Balance Sub-cards: Deposit, Winning, Bonus
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Deposit", color = TextGrey, fontSize = 10.sp)
                                        Text("₹${currentUser.depositBalance.toInt()}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Winnings", color = TextGrey, fontSize = 10.sp)
                                        Text("₹${currentUser.winningBalance.toInt()}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Bonus", color = TextGrey, fontSize = 10.sp)
                                        Text("₹${currentUser.bonusBalance.toInt()}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Deposit & Withdraw Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { showDepositDialog = true },
                                    modifier = Modifier.weight(1f).height(46.dp).testTag("add_money_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ADD MONEY", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                Button(
                                    onClick = { showWithdrawDialog = true },
                                    modifier = Modifier.weight(1f).height(46.dp).testTag("withdraw_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("WITHDRAW", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Coupon Code / Promo Code Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("coupon_section")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = PrimaryPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Have a Promo / Coupon Code?", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = couponCodeInput,
                                onValueChange = { couponCodeInput = it },
                                placeholder = { Text("Enter Coupon (e.g. WELCOME100)", color = TextGrey, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f).testTag("coupon_input_field"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceDark,
                                    unfocusedContainerColor = SurfaceDark,
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (couponCodeInput.isNotBlank()) {
                                        viewModel.applyCoupon(couponCodeInput) { success, msg ->
                                            snackbarMsg = msg
                                            if (success) couponCodeInput = ""
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(50.dp).testTag("apply_coupon_button")
                            ) {
                                Text("APPLY", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Transactions History Header
            item {
                Text("Recent Transactions", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            items(transactions) { txn ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("transaction_item_${txn.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when (txn.type) {
                                            TransactionType.DEPOSIT -> SuccessGreen.copy(alpha = 0.2f)
                                            TransactionType.WITHDRAW -> PrimaryPurple.copy(alpha = 0.2f)
                                            TransactionType.JOIN_TOURNAMENT -> ErrorRed.copy(alpha = 0.2f)
                                            TransactionType.WINNING_REWARD -> AccentGold.copy(alpha = 0.2f)
                                            else -> SurfaceDark
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (txn.type) {
                                        TransactionType.DEPOSIT -> Icons.Default.ArrowDownward
                                        TransactionType.WITHDRAW -> Icons.Default.ArrowUpward
                                        TransactionType.JOIN_TOURNAMENT -> Icons.Default.SportsEsports
                                        TransactionType.WINNING_REWARD -> Icons.Default.EmojiEvents
                                        else -> Icons.Default.AccountBalanceWallet
                                    },
                                    contentDescription = null,
                                    tint = when (txn.type) {
                                        TransactionType.DEPOSIT -> SuccessGreen
                                        TransactionType.WITHDRAW -> PrimaryPurple
                                        TransactionType.JOIN_TOURNAMENT -> ErrorRed
                                        TransactionType.WINNING_REWARD -> AccentGold
                                        else -> TextWhite
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(txn.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${txn.timestamp} • ${txn.referenceId}", color = TextGrey, fontSize = 11.sp)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val isPlus = txn.type == TransactionType.DEPOSIT || txn.type == TransactionType.WINNING_REWARD || txn.type == TransactionType.BONUS_CREDIT || txn.type == TransactionType.REFUND
                            Text(
                                text = "${if (isPlus) "+" else "-"}₹${txn.amount.toInt()}",
                                color = if (isPlus) SuccessGreen else TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = txn.status.name,
                                color = when (txn.status) {
                                    TransactionStatus.SUCCESS -> SuccessGreen
                                    TransactionStatus.PENDING -> AccentGold
                                    else -> ErrorRed
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Deposit Dialog
        if (showDepositDialog) {
            AlertDialog(
                onDismissRequest = { showDepositDialog = false },
                containerColor = CardDark,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = AccentGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (adminConfig.depositMode == "GATEWAY") "Instant Gateway Deposit" else "Manual UPI Deposit",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (adminConfig.depositMode == "GATEWAY") {
                            Surface(
                                color = SurfaceDark,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Payment Gateway API Connected", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("${adminConfig.gatewayProvider} • Auto Credit", color = TextGrey, fontSize = 10.sp)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = depositInput,
                                onValueChange = { depositInput = it },
                                label = { Text("Deposit Amount (₹)", color = TextGrey) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("50", "100", "200", "500", "1000").forEach { quickAmt ->
                                    FilterChip(
                                        selected = depositInput == quickAmt,
                                        onClick = { depositInput = quickAmt },
                                        label = { Text("₹$quickAmt", fontSize = 11.sp) }
                                    )
                                }
                            }
                        } else {
                            // MANUAL UPI & QR CODE DEPOSIT
                            Text(adminConfig.manualQrInstruction, color = TextGrey, fontSize = 11.sp)

                            // Admin UPI ID Box with Copy Button
                            Surface(
                                color = SurfaceDark,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Official Admin UPI ID:", color = TextGrey, fontSize = 10.sp)
                                        Text(adminConfig.manualUpiId, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(adminConfig.manualUpiId))
                                            snackbarMsg = "UPI ID copied to clipboard!"
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy UPI ID", tint = TextWhite)
                                    }
                                }
                            }

                            // QR Code Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceDark, shape = RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        color = TextWhite,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(150.dp)
                                    ) {
                                        AsyncImage(
                                            model = adminConfig.manualQrUrl,
                                            contentDescription = "UPI Payment QR Code",
                                            modifier = Modifier.fillMaxSize().padding(6.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Scan QR Code using Paytm / PhonePe / GPay", color = TextGrey, fontSize = 10.sp)
                                }
                            }

                            // Amount Input
                            OutlinedTextField(
                                value = depositInput,
                                onValueChange = { depositInput = it },
                                label = { Text("Deposit Amount (₹)", color = TextGrey) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick Amount Chips
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("50", "100", "200", "500", "1000").forEach { quickAmt ->
                                    FilterChip(
                                        selected = depositInput == quickAmt,
                                        onClick = { depositInput = quickAmt },
                                        label = { Text("₹$quickAmt", fontSize = 11.sp) }
                                    )
                                }
                            }

                            // Transaction ID / UTR Input
                            OutlinedTextField(
                                value = txnIdInput,
                                onValueChange = { txnIdInput = it },
                                label = { Text("12-Digit UPI Ref / UTR / Txn ID *", color = AccentGold) },
                                placeholder = { Text("e.g. 420188921034", color = TextGrey) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = depositInput.toDoubleOrNull() ?: 100.0
                            val mode = if (adminConfig.depositMode == "GATEWAY") "GATEWAY" else "MANUAL_UPI"
                            viewModel.submitDepositRequest(amt, txnIdInput, upiIdInput, mode) { success, msg ->
                                snackbarMsg = msg
                                if (success) {
                                    showDepositDialog = false
                                    txnIdInput = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (adminConfig.depositMode == "GATEWAY") "PAY VIA GATEWAY API" else "SUBMIT DEPOSIT FOR VERIFICATION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDepositDialog = false }) {
                        Text("CANCEL", color = TextGrey)
                    }
                }
            )
        }

        // Withdraw Dialog
        if (showWithdrawDialog) {
            AlertDialog(
                onDismissRequest = { showWithdrawDialog = false },
                containerColor = CardDark,
                title = { Text("Request Instant Withdrawal", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Winnings Balance available: ₹${currentUser.winningBalance.toInt()}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = withdrawInput,
                            onValueChange = { withdrawInput = it },
                            label = { Text("Withdraw Amount (₹)", color = TextGrey) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = upiIdInput,
                            onValueChange = { upiIdInput = it },
                            label = { Text("Your UPI ID (e.g. 9876543210@paytm)", color = TextGrey) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = withdrawInput.toDoubleOrNull() ?: 0.0
                            viewModel.requestWithdraw(amt) { success, msg ->
                                snackbarMsg = msg
                                if (success) showWithdrawDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("SUBMIT WITHDRAWAL", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWithdrawDialog = false }) {
                        Text("CANCEL", color = TextGrey)
                    }
                }
            )
        }

        snackbarMsg?.let { msg ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                containerColor = SurfaceDark,
                contentColor = TextWhite
            ) {
                Text(msg)
            }
        }
    }
}
}
