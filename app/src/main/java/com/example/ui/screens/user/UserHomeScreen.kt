package com.example.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.Category
import com.example.data.models.Tournament
import com.example.ui.components.TournamentCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.WinX7ViewModel
import com.example.ui.components.RefreshableLayout

@Composable
fun UserHomeScreen(
    viewModel: WinX7ViewModel,
    onTournamentClick: (String) -> Unit,
    onNavigateToWallet: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val tournaments by viewModel.tournaments.collectAsState()
    val joinedIds by viewModel.joinedTournamentIds.collectAsState()
    val banners by viewModel.banners.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    var joinDialogTournament by remember { mutableStateOf<Tournament?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val filteredTournaments = tournaments.filter { tour ->
        !joinedIds.contains(tour.id) && (selectedCategoryId == null || tour.categoryId == selectedCategoryId)
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Category Tab Bar with Red / Purple Underline
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardDark)
                    .padding(top = 8.dp)
                    .testTag("category_horizontal_scroll"),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                item {
                    val isSelected = selectedCategoryId == null
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedCategoryId = null }
                    ) {
                        Text(
                            text = "ALL BR",
                            color = if (isSelected) AccentGold else TextGrey,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(2.5.dp)
                                .background(if (isSelected) AccentGold else Color.Transparent)
                        )
                    }
                }

                items(categories) { cat ->
                    val isSelected = selectedCategoryId == cat.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedCategoryId = cat.id }
                    ) {
                        Text(
                            text = cat.name.uppercase(),
                            color = if (isSelected) AccentGold else TextGrey,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(2.5.dp)
                                .background(if (isSelected) AccentGold else Color.Transparent)
                        )
                    }
                }
            }

            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

            // Tournaments List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
            ) {
                if (filteredTournaments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardDark)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = TextGrey,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No matches found in this category",
                                    color = TextGrey,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTournaments) { tour ->
                        val isJoined = joinedIds.contains(tour.id)
                        TournamentCard(
                            tournament = tour,
                            isJoined = isJoined,
                            onCardClick = { onTournamentClick(tour.id) },
                            onJoinClick = {
                                if (!isJoined) {
                                    joinDialogTournament = tour
                                }
                            }
                        )
                    }
                }
            }
        }

        // Join Confirmation Dialog
        joinDialogTournament?.let { tour ->
            AlertDialog(
                onDismissRequest = { joinDialogTournament = null },
                containerColor = CardDark,
                title = {
                    Text(
                        text = "Confirm Entry - ${tour.title}",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Map: ${tour.mapName} • Mode: ${tour.mode}",
                            color = TextGrey,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDark)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Entry Fee:", color = TextGrey, fontSize = 13.sp)
                            Text("₹${tour.entryFee.toInt()}", color = AccentGold, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDark)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Your Total Wallet:", color = TextGrey, fontSize = 13.sp)
                            Text("₹${currentUser.totalBalance.toInt()}", color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.horizontalGradient(listOf(MetallicGold, PremiumGold)))
                            .clickable {
                                val targetId = tour.id
                                joinDialogTournament = null
                                viewModel.joinTournament(targetId) { success, msg ->
                                    snackbarMessage = msg
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CONFIRM JOIN", color = BgDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { joinDialogTournament = null }) {
                        Text("CANCEL", color = TextGrey)
                    }
                }
            )
        }

        // Snackbar notification
        snackbarMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                containerColor = SurfaceDark,
                contentColor = TextWhite,
                action = {
                    TextButton(onClick = { snackbarMessage = null }) {
                        Text("OK", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                    }
                }
            ) {
                Text(msg)
            }
        }
    }
}
