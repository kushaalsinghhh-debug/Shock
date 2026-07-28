package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.Tournament
import com.example.data.models.TournamentStatus
import com.example.data.models.UserRole
import com.example.ui.theme.*

@Composable
fun WinX7TopBar(
    role: UserRole = UserRole.USER,
    walletBalance: Double,
    onPortalSwitchRequest: () -> Unit = {},
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit
) {
    Surface(
        color = BgDark,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App Avatar & WINX7 Logo + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onProfileClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Ambient Purple Glow
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .blur(12.dp)
                                .background(PrimaryPurple.copy(alpha = 0.5f), CircleShape)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.winx7_logo_1785039507914),
                            contentDescription = "WINX7 Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.5.dp, PrimaryPurple, RoundedCornerShape(10.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "WINX7",
                            color = TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "PLAY • COMPETE • WIN",
                            color = AccentGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Right Actions: Wallet Chip, Notification Bell
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Wallet Chip (Glassmorphism + Gold Border)
                    Surface(
                        color = CardDark.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier
                            .testTag("wallet_balance_chip")
                            .clickable { onWalletClick() }
                            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = AccentGold, spotColor = AccentGold)
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Brush.linearGradient(listOf(Color.White.copy(alpha=0.05f), Color.Transparent)))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = AccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "₹${walletBalance.toInt()}",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Notification Bell
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CardDark.copy(alpha = 0.8f))
                            .border(1.dp, SurfaceBorder, CircleShape)
                            .testTag("notification_bell_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
        }
    }
}

@Composable
fun TournamentCard(
    tournament: Tournament,
    isJoined: Boolean,
    onCardClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    val progress = if (tournament.totalSlots > 0) {
        tournament.joinedSlots.toFloat() / tournament.totalSlots.toFloat()
    } else 0f

    val isFull = tournament.joinedSlots >= tournament.totalSlots
    val spotsLeft = tournament.totalSlots - tournament.joinedSlots

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 250),
        label = "button_scale"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(18.dp), // Requested 18-20dp
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = PrimaryPurple,
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .testTag("tournament_card_${tournament.id}")
            .clickable { onCardClick() }
    ) {
        Box(modifier = Modifier.background(Brush.linearGradient(listOf(Color.White.copy(alpha=0.03f), Color.Transparent)))) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Details & Progress
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    // Top Pills: Mode, Map, Slots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgePill(text = tournament.mode.uppercase())
                        BadgePill(text = tournament.mapName.uppercase())
                        BadgePill(text = "${tournament.totalSlots} SLOTS")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title
                    Text(
                        text = tournament.title.uppercase(),
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Prize Pool
                    Text(
                        text = "Prize Pool - ₹${tournament.prizePool.toInt()}",
                        color = AccentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Purple-to-Gold Progress Bar Line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SurfaceDark)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progress)
                                .fillMaxHeight()
                                .background(Brush.horizontalGradient(listOf(PrimaryPurple, AccentGold)))
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Spots Left
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = if (isFull) "0 spots left" else "$spotsLeft spots left",
                            color = AccentGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Right Column: Image, Date/Time, Join Button
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.width(125.dp) // slightly larger
                ) {
                    // Thumbnail Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(78.dp) // slightly larger
                            .clip(RoundedCornerShape(12.dp)) // increased radius
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.freefire_banner_1785039520004),
                            contentDescription = "Match Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Date and Time Text
                    Text(
                        text = "${tournament.matchDate} ${tournament.matchTime}",
                        color = TextGrey,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Join Button
                    val isButtonEnabled = !isJoined && !isFull && tournament.status == TournamentStatus.UPCOMING
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(18.dp))
                            .then(
                                if (isButtonEnabled) {
                                    Modifier.background(Brush.horizontalGradient(listOf(MetallicGold, PremiumGold)))
                                } else {
                                    Modifier.background(if (isJoined) SuccessGreen else SurfaceDark)
                                }
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                enabled = isButtonEnabled,
                                onClick = onJoinClick
                            )
                            .testTag("join_button_${tournament.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                isJoined -> "JOINED"
                                isFull -> "FULL"
                                tournament.isFree -> "FREE JOIN"
                                else -> "₹${tournament.entryFee.toInt()} JOIN"
                            },
                            color = if (isButtonEnabled) BgDark else TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RefreshableLayout(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = rememberPullRefreshState(isRefreshing, onRefresh)
    Box(modifier = modifier.pullRefresh(state)) {
        content()
        PullRefreshIndicator(refreshing = isRefreshing, state = state, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun BadgePill(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, SurfaceBorder, RoundedCornerShape(6.dp))
            .background(SurfaceDark, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = TextGrey,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
