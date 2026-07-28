package com.example.ui.screens.staff

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Tournament
import com.example.data.models.TournamentStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.WinX7ViewModel

@Composable
fun StaffPanelScreen(
    viewModel: WinX7ViewModel,
    onReturnToUserApp: () -> Unit
) {
    val tournaments by viewModel.tournaments.collectAsState()
    val tickets by viewModel.tickets.collectAsState()

    var selectedStaffTab by remember { mutableIntStateOf(0) }
    val staffTabs = listOf("Match Moderation", "Support Tickets")

    var editingTournament by remember { mutableStateOf<Tournament?>(null) }
    var roomIdInput by remember { mutableStateOf("") }
    var roomPassInput by remember { mutableStateOf("") }

    var replyingTicketId by remember { mutableStateOf<String?>(null) }
    var replyTextInput by remember { mutableStateOf("") }

    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Staff Top Bar
            Surface(
                color = SurfaceDark,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentGold)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("STAFF PANEL", color = BgDark, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Moderator Desk", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Button(
                        onClick = onReturnToUserApp,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Log Out", fontSize = 11.sp)
                    }
                }
            }

            // Tabs Header
            TabRow(
                selectedTabIndex = selectedStaffTab,
                containerColor = BgDark,
                contentColor = AccentGold
            ) {
                staffTabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedStaffTab == idx,
                        onClick = { selectedStaffTab = idx },
                        text = {
                            Text(
                                title,
                                color = if (selectedStaffTab == idx) AccentGold else TextGrey,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                if (selectedStaffTab == 0) {
                    // Match Moderation Tab
                    item {
                        Text("Assigned Tournaments for Room ID & Result Entry", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    items(tournaments) { tour ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.fillMaxWidth().testTag("staff_tournament_card_${tour.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(tour.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(tour.status.name, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Map: ${tour.mapName} • Time: ${tour.matchTime} • Participants: ${tour.joinedSlots}/${tour.totalSlots}", color = TextGrey, fontSize = 11.sp)

                                if (tour.roomId.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("ROOM ID: ${tour.roomId} | PASS: ${tour.roomPassword}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            editingTournament = tour
                                            roomIdInput = tour.roomId
                                            roomPassInput = tour.roomPassword
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Text("Upload Room ID", color = BgDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.updateTournamentStatus(tour.id, TournamentStatus.COMPLETED)
                                            snackbarMsg = "Match marked completed!"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Text("Verify Results", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Support Tickets Tab
                    item {
                        Text("User Support Tickets Response Queue", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    items(tickets) { tkt ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.fillMaxWidth().testTag("staff_ticket_${tkt.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("From: ${tkt.userName}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(tkt.status, color = if (tkt.status == "RESOLVED") SuccessGreen else AccentGold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Subject: ${tkt.subject}", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Message: ${tkt.message}", color = TextGrey, fontSize = 12.sp)

                                if (tkt.reply.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Reply Sent: ${tkt.reply}", color = SuccessGreen, fontSize = 11.sp)
                                } else {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            replyingTicketId = tkt.id
                                            replyTextInput = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text("RESPOND TO TICKET", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Room ID Dialog
        editingTournament?.let { tour ->
            AlertDialog(
                onDismissRequest = { editingTournament = null },
                containerColor = CardDark,
                title = { Text("Upload Room ID & Pass - ${tour.title}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = roomIdInput,
                            onValueChange = { roomIdInput = it },
                            label = { Text("Room ID", color = TextGrey) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        OutlinedTextField(
                            value = roomPassInput,
                            onValueChange = { roomPassInput = it },
                            label = { Text("Room Password", color = TextGrey) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateRoomDetails(tour.id, roomIdInput, roomPassInput)
                            editingTournament = null
                            snackbarMsg = "Room ID & Pass published for players!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                    ) {
                        Text("PUBLISH NOW", color = BgDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingTournament = null }) {
                        Text("CANCEL", color = TextGrey)
                    }
                }
            )
        }

        // Ticket Reply Dialog
        replyingTicketId?.let { tktId ->
            AlertDialog(
                onDismissRequest = { replyingTicketId = null },
                containerColor = CardDark,
                title = { Text("Reply to Support Ticket", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = replyTextInput,
                        onValueChange = { replyTextInput = it },
                        placeholder = { Text("Enter response for user...", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.replyTicket(tktId, replyTextInput)
                            replyingTicketId = null
                            snackbarMsg = "Reply sent to user!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("SEND REPLY", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { replyingTicketId = null }) {
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
