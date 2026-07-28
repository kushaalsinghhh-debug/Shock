package com.example.ui.screens.user

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Tournament
import com.example.data.models.TournamentStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.WinX7ViewModel
import com.example.ui.components.RefreshableLayout

@Composable
fun MyMatchesScreen(
    viewModel: WinX7ViewModel,
    onMatchClick: (String) -> Unit
) {
    val tournaments by viewModel.tournaments.collectAsState()
    val joinedIds by viewModel.joinedTournamentIds.collectAsState()
    val adminConfig by viewModel.adminConfig.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Live", "Completed")

    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    val myTournaments = tournaments.filter { joinedIds.contains(it.id) }

    val filteredMatches = myTournaments.filter { tour ->
        when (selectedTabIndex) {
            0 -> tour.status == TournamentStatus.UPCOMING
            1 -> tour.status == TournamentStatus.LIVE
            else -> tour.status == TournamentStatus.COMPLETED
        }
    }

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
            Column(modifier = Modifier.fillMaxSize()) {
            // Tab Header
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = BgDark,
                contentColor = PrimaryPurple,
                divider = { HorizontalDivider(color = SurfaceBorder) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) PrimaryPurple else TextGrey,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            if (filteredMatches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = TextGrey,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No ${tabs[selectedTabIndex]} matches found",
                            color = TextGrey,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Join tournaments from Home screen to see them here!",
                            color = TextGrey,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredMatches) { tour ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.fillMaxWidth().testTag("my_match_card_${tour.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PrimaryPurple.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(tour.categoryName, color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    Text("${tour.matchDate} @ ${tour.matchTime}", color = TextGrey, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(tour.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                                Spacer(modifier = Modifier.height(12.dp))

                                // Room Credentials Box (if available)
                                if (tour.roomId.isNotEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = DarkPurple.copy(alpha = 0.25f)),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("ROOM ID: ${tour.roomId}", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                                    Text("PASS: ${tour.roomPassword}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                }
                                                Row {
                                                    IconButton(
                                                        onClick = {
                                                            clipboardManager.setText(AnnotatedString("${tour.roomId} / ${tour.roomPassword}"))
                                                            snackbarMsg = "Room ID & Pass copied!"
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AccentGold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                } else {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.LockClock, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Room ID & Password will appear 15m before match start", color = TextGrey, fontSize = 11.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // Match Winner / Result Note
                                if (tour.winnerNote.isNotEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("MATCH RESULT & WINNERS", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(tour.winnerNote, color = TextWhite, fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // Quick Actions: Support Link & View Details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val url = adminConfig.telegramContact.ifBlank { "https://t.me/winx7_official" }
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            runCatching { context.startActivity(intent) }
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGold)
                                    ) {
                                        Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Telegram Support", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { onMatchClick(tour.id) },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                                    ) {
                                        Text("Match Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
