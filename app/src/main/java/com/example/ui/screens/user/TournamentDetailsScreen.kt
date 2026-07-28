package com.example.ui.screens.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.Tournament
import com.example.data.models.TournamentStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.WinX7ViewModel
import com.example.ui.components.RefreshableLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailsScreen(
    tournamentId: String,
    viewModel: WinX7ViewModel,
    onBackClick: () -> Unit
) {
    val tournaments by viewModel.tournaments.collectAsState()
    val joinedIds by viewModel.joinedTournamentIds.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val tournament = tournaments.find { it.id == tournamentId }
    val isJoined = joinedIds.contains(tournamentId)
    val clipboardManager = LocalClipboardManager.current

    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    if (tournament == null) {
        Box(modifier = Modifier.fillMaxSize().background(BgDark), contentAlignment = Alignment.Center) {
            Text("Tournament not found", color = TextWhite)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Details", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            var isRefreshing by remember { mutableStateOf(false) }
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Hero Banner Header
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth().height(170.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Free Fire Banner Image Background
                            Image(
                                painter = painterResource(id = R.drawable.freefire_banner_1785039520004),
                                contentDescription = "Tournament Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Gradient Scrim for contrast
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.3f),
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PrimaryPurple)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = tournament.categoryName.uppercase(),
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = tournament.title,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Map: ${tournament.mapName} • Mode: ${tournament.mode} • ${tournament.matchDate} @ ${tournament.matchTime}",
                                        color = TextWhite.copy(alpha = 0.85f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Room Credentials Card (Shown if joined & Room ID published!)
                if (isJoined && tournament.roomId.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkPurple.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                            modifier = Modifier.fillMaxWidth().testTag("room_credentials_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = AccentGold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("ROOM ID & PASSWORD", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SuccessGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("LIVE ROOM", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("ROOM ID", color = TextGrey, fontSize = 11.sp)
                                        Text(tournament.roomId, color = TextWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                    }
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(tournament.roomId))
                                            snackbarMsg = "Room ID copied to clipboard!"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy ID", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("PASSWORD", color = TextGrey, fontSize = 11.sp)
                                        Text(tournament.roomPassword, color = TextWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                    }
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(tournament.roomPassword))
                                            snackbarMsg = "Room password copied!"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Pass", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Stats Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Prize Pool", color = TextGrey, fontSize = 11.sp)
                                Text("₹${tournament.prizePool.toInt()}", color = AccentGold, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Kill Reward", color = TextGrey, fontSize = 11.sp)
                                Text("₹${tournament.killReward.toInt()}", color = SuccessGreen, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Entry Fee", color = TextGrey, fontSize = 11.sp)
                                Text("₹${tournament.entryFee.toInt()}", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                    }
                }

                // Rules & Description
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tournament Rules", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(tournament.rules, color = TextGrey, fontSize = 13.sp, lineHeight = 20.sp)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Description", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(tournament.description, color = TextGrey, fontSize = 13.sp)
                        }
                    }
                }

                // Participants List
                item {
                    Text("Joined Participants (${tournament.participants.size}/${tournament.totalSlots})", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                items(tournament.participants) { participantName ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = PrimaryPurple)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(participantName, color = TextWhite, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Bottom Fixed Join Button
            Surface(
                color = CardDark,
                shadowElevation = 8.dp,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            viewModel.joinTournament(tournament.id) { success, msg ->
                                snackbarMsg = msg
                            }
                        },
                        enabled = !isJoined && tournament.joinedSlots < tournament.totalSlots && tournament.status == TournamentStatus.UPCOMING,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text(
                            text = if (isJoined) "ALREADY JOINED (WAITING)" else "JOIN NOW (₹${tournament.entryFee.toInt()})",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            snackbarMsg?.let { msg ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 70.dp, start = 16.dp, end = 16.dp),
                    containerColor = SurfaceDark,
                    contentColor = TextWhite
                ) {
                    Text(msg)
                }
            }
        }
    }
}
}
