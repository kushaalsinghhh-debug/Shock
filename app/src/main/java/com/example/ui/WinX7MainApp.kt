package com.example.ui

import android.os.Build
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.UserRole
import com.example.ui.components.WinX7TopBar
import com.example.ui.screens.admin.AdminLoginScreen
import com.example.ui.screens.admin.AdminPanelScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.splash.AppOpeningSplashScreen
import com.example.ui.screens.staff.StaffPanelScreen
import com.example.ui.screens.user.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WinX7ViewModel

sealed class UserNavTab(val route: String, val title: String, val icon: ImageVector) {
    object Home : UserNavTab("home", "Home", Icons.Default.Home)
    object MyMatches : UserNavTab("my_matches", "My Matches", Icons.Default.SportsEsports)
    object Wallet : UserNavTab("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
    object Leaderboard : UserNavTab("leaderboard", "Ranks", Icons.Default.EmojiEvents)
    object Profile : UserNavTab("profile", "Profile", Icons.Default.Person)
}

@Composable
fun WinX7MainApp(
    viewModel: WinX7ViewModel = viewModel()
) {
    val currentRole by viewModel.currentPortalRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isFirebaseAuthenticated by viewModel.isAuthenticated.collectAsState()

    var showSplashScreen by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf<UserNavTab>(UserNavTab.Home) }
    var activeDetailTournamentId by remember { mutableStateOf<String?>(null) }
    var isShowingNotifications by remember { mutableStateOf(false) }
    var isShowingSupport by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // If denied, we could show a dialog explaining why we need it. 
            // For now, we just silently ignore as per standard minimal handling.
        }
    )

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showSplashScreen) {
        WinX7Theme {
            AppOpeningSplashScreen(
                onSplashComplete = {
                    showSplashScreen = false
                }
            )
        }
        return
    }

    LaunchedEffect(isFirebaseAuthenticated) {
        if (isFirebaseAuthenticated) {
            isLoggedIn = true
        }
    }

    if (!isLoggedIn && !isFirebaseAuthenticated) {
        AuthScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                isLoggedIn = true
            }
        )
        return
    }

    WinX7Theme {
        // Back navigation handling
        val context = LocalContext.current
        val activity = context as? Activity
        var lastBackPress by remember { mutableStateOf(0L) }

        BackHandler {
            // Priority: Detail view > Notifications > Support > Non-home tab > Home double-back exit
            when {
                activeDetailTournamentId != null -> {
                    activeDetailTournamentId = null
                }
                isShowingNotifications -> {
                    isShowingNotifications = false
                }
                isShowingSupport -> {
                    isShowingSupport = false
                }
                currentTab != UserNavTab.Home -> {
                    currentTab = UserNavTab.Home
                }
                else -> {
                    val now = System.currentTimeMillis()
                    if (now - lastBackPress <= 2000L) {
                        activity?.finish()
                    } else {
                        lastBackPress = now
                        // Show a transient message; no Toast utilities imported, use Android Toast
                        android.widget.Toast.makeText(context, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        Scaffold(
            topBar = {
                if (currentRole == UserRole.USER && activeDetailTournamentId == null && !isShowingNotifications && !isShowingSupport) {
                    WinX7TopBar(
                        role = currentRole,
                        walletBalance = currentUser.totalBalance,
                        onPortalSwitchRequest = { currentTab = UserNavTab.Profile },
                        onNotificationClick = { isShowingNotifications = true },
                        onProfileClick = { currentTab = UserNavTab.Profile },
                        onWalletClick = { currentTab = UserNavTab.Wallet }
                    )
                }
            },
            bottomBar = {
                if (currentRole == UserRole.USER && activeDetailTournamentId == null && !isShowingNotifications && !isShowingSupport) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = PrimaryPurple, spotColor = Color.Black.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        NavigationBar(
                            containerColor = CardDark.copy(alpha = 0.9f),
                            contentColor = PrimaryPurple,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .testTag("main_bottom_navigation")
                                .background(Brush.linearGradient(listOf(Color.White.copy(alpha=0.05f), Color.Transparent)))
                        ) {
                            listOf(
                                UserNavTab.Home,
                                UserNavTab.MyMatches,
                                UserNavTab.Wallet,
                                UserNavTab.Leaderboard,
                                UserNavTab.Profile
                            ).forEach { tab ->
                                val isSelected = currentTab.route == tab.route
                                val scale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1f, animationSpec = tween(300))
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Box(
                                            modifier = Modifier.graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                            }
                                        ) {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = tab.title,
                                                modifier = if (isSelected) Modifier.drawBehind {
                                                    val y = size.height + 6.dp.toPx()
                                                    drawLine(
                                                        color = AccentGold,
                                                        start = Offset(-4.dp.toPx(), y),
                                                        end = Offset(size.width + 4.dp.toPx(), y),
                                                        strokeWidth = 3.dp.toPx()
                                                    )
                                                } else Modifier,
                                                tint = if (isSelected) PrimaryPurple else TextGrey
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 10.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryPurple,
                                        selectedTextColor = PrimaryPurple,
                                        indicatorColor = Color.Transparent,
                                        unselectedIconColor = TextGrey,
                                        unselectedTextColor = TextGrey
                                    )
                                )
                            }
                        }
                    }
                }
            },
            containerColor = BgDark
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (currentRole) {
                    UserRole.ADMIN -> {
                        // Admin Login is separate and strictly enforced for users with 'admin' role
                        if (currentUser.role == UserRole.ADMIN) {
                            AdminPanelScreen(
                                viewModel = viewModel,
                                onReturnToUserApp = {
                                    viewModel.switchPortalRole(UserRole.USER)
                                    isLoggedIn = false
                                }
                            )
                        } else {
                            AdminLoginScreen(
                                viewModel = viewModel,
                                onAdminLoginSuccess = {
                                    viewModel.switchPortalRole(UserRole.ADMIN)
                                },
                                onReturnToUserApp = {
                                    viewModel.switchPortalRole(UserRole.USER)
                                    isLoggedIn = false
                                }
                            )
                        }
                    }

                    UserRole.STAFF -> {
                        StaffPanelScreen(
                            viewModel = viewModel,
                            onReturnToUserApp = {
                                viewModel.switchPortalRole(UserRole.USER)
                                isLoggedIn = false
                            }
                        )
                    }

                    UserRole.USER -> {
                        when {
                            activeDetailTournamentId != null -> {
                                TournamentDetailsScreen(
                                    tournamentId = activeDetailTournamentId!!,
                                    viewModel = viewModel,
                                    onBackClick = { activeDetailTournamentId = null }
                                )
                            }

                            isShowingNotifications -> {
                                Column {
                                    IconButton(
                                        onClick = { isShowingNotifications = false },
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                                    }
                                    NotificationsScreen(viewModel = viewModel)
                                }
                            }

                            isShowingSupport -> {
                                Column {
                                    IconButton(
                                        onClick = { isShowingSupport = false },
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                                    }
                                    SupportScreen(viewModel = viewModel)
                                }
                            }

                            else -> {
                                when (currentTab) {
                                    UserNavTab.Home -> {
                                        UserHomeScreen(
                                            viewModel = viewModel,
                                            onTournamentClick = { tourId ->
                                                activeDetailTournamentId = tourId
                                            },
                                            onNavigateToWallet = { currentTab = UserNavTab.Wallet }
                                        )
                                    }

                                    UserNavTab.MyMatches -> {
                                        MyMatchesScreen(
                                            viewModel = viewModel,
                                            onMatchClick = { tourId ->
                                                activeDetailTournamentId = tourId
                                            }
                                        )
                                    }

                                    UserNavTab.Wallet -> {
                                        WalletScreen(viewModel = viewModel)
                                    }

                                    UserNavTab.Leaderboard -> {
                                        LeaderboardScreen(viewModel = viewModel)
                                    }

                                    UserNavTab.Profile -> {
                                        ProfileScreen(
                                            viewModel = viewModel,
                                            onLogout = {
                                                isLoggedIn = false
                                                currentTab = UserNavTab.Home
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
