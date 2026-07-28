package com.example.ui.screens.user

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.WinX7ViewModel
import com.example.ui.components.RefreshableLayout

@Composable
fun SupportScreen(
    viewModel: WinX7ViewModel
) {
    val adminConfig by viewModel.adminConfig.collectAsState()
    val context = LocalContext.current

    val faqs = listOf(
        "How do I receive Room ID & Password?" to "Room ID & Password appear automatically under 'My Matches' tab 15 minutes before match start time.",
        "How fast are wallet withdrawals?" to "Withdrawals are processed instantly 24/7 directly to your registered UPI ID.",
        "What if match gets cancelled?" to "100% full entry fee refund is automatically credited to your deposit wallet instantly.",
        "How do I contact customer support?" to "Click the Official Telegram Support button below to connect directly with our active match moderators."
    )

    var isRefreshing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        RefreshableLayout(isRefreshing = isRefreshing, onRefresh = {
            isRefreshing = true
            viewModel.refreshAllData()
        }) {
            LaunchedEffect(isRefreshing) {
                if (isRefreshing) {
                    kotlinx.coroutines.delay(600)
                    isRefreshing = false
                }
            }
            LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            item {
                Text("Help & Support Center", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            // Official Telegram Support Portal Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("telegram_support_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = PrimaryPurple.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Telegram Support",
                                tint = PrimaryPurple,
                                modifier = Modifier.padding(16.dp).size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Official Telegram Support Desk", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Get instant 24/7 assistance from our official WinX7 tournament moderators.",
                            color = TextGrey,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val url = adminConfig.telegramContact.ifBlank { "https://t.me/winx7_official" }
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                runCatching { context.startActivity(intent) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("telegram_support_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("JOIN TELEGRAM SUPPORT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Frequently Asked Questions Header
            item {
                Text("Frequently Asked Questions", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            items(faqs) { (q, a) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Q: $q", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(a, color = TextGrey, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
}
