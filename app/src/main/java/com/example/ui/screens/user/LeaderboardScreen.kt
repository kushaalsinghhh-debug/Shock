package com.example.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.WinX7ViewModel
import com.example.ui.components.RefreshableLayout

@Composable
fun LeaderboardScreen(
    viewModel: WinX7ViewModel
) {
    val leaderboard by viewModel.leaderboard.collectAsState()

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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Table Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RANK & PLAYER",
                        color = TextGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "TOTAL WINNINGS",
                        color = TextGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Podium for Top 3
            if (leaderboard.isNotEmpty()) {
                item {
                    val top3 = leaderboard.take(3)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (top3.size >= 2) {
                            PodiumItem(entry = top3[1], color = Color(0xFFC0C0C0), height = 120.dp, label = "2nd")
                        }
                        if (top3.isNotEmpty()) {
                            PodiumItem(entry = top3[0], color = AccentGold, height = 150.dp, label = "1st", isFirst = true)
                        }
                        if (top3.size >= 3) {
                            PodiumItem(entry = top3[2], color = Color(0xFFCD7F32), height = 100.dp, label = "3rd")
                        }
                    }
                }
            }

            // Player List for rest
            val rest = leaderboard.drop(3)
            items(rest) { entry ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("leaderboard_item_${entry.rank}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Section: Rank + Profile Avatar + Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "#${entry.rank}",
                                color = TextGrey,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.width(36.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceDark)
                                    .border(1.dp, SurfaceBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (entry.avatarUrl.isNotBlank()) {
                                    coil.compose.AsyncImage(
                                        model = entry.avatarUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = "Player", tint = TextGrey, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = entry.playerName,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${entry.wins} Wins • ${entry.kills} Kills",
                                    color = TextGrey,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Right Section: Winning Amount
                        Text(
                            text = "₹${entry.totalEarnings.toInt()}",
                            color = AccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun PodiumItem(entry: com.example.data.models.LeaderboardEntry, color: Color, height: androidx.compose.ui.unit.Dp, label: String, isFirst: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.height(height + 100.dp)
    ) {
        if (isFirst) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Crown",
                tint = color,
                modifier = Modifier.size(32.dp).padding(bottom = 4.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .size(if (isFirst) 72.dp else 56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (entry.avatarUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = entry.avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = "Player", tint = color, modifier = Modifier.size(if (isFirst) 40.dp else 30.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = entry.playerName,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = if (isFirst) 14.sp else 12.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(
            text = "₹${entry.totalEarnings.toInt()}",
            color = AccentGold,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (isFirst) 13.sp else 11.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .width(if (isFirst) 80.dp else 60.dp)
                .height(height)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.3f))
                    ),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = label,
                color = BgDark,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)


