package com.example.ui.screens.user

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.KYCStatus
import com.example.data.models.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.WinX7ViewModel
import com.example.ui.components.RefreshableLayout

@Composable
fun ProfileScreen(
    viewModel: WinX7ViewModel,
    onLogout: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(currentUser.name) }
    var editAvatarUrl by remember { mutableStateOf(currentUser.avatarUrl) }

    val presetAvatars = listOf(
        "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=150",
        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
        "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
        "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150"
    )

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
            // Profile Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("profile_header_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .clickable {
                                    editName = currentUser.name
                                    editAvatarUrl = currentUser.avatarUrl
                                    showEditProfileDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentUser.avatarUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = currentUser.avatarUrl,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(3.dp, PrimaryPurple, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(PrimaryPurple, DarkPurple)
                                            )
                                        )
                                        .border(3.dp, PrimaryPurple, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser.name.take(2).uppercase(),
                                        color = TextWhite,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp
                                    )
                                }
                            }

                            // Camera Edit Badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(AccentGold)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Edit Photo", tint = BgDark, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // In-Game Name (IGN)
                        Text(currentUser.name, color = TextWhite, fontWeight = FontWeight.Black, fontSize = 20.sp)

                        Spacer(modifier = Modifier.height(4.dp))

                        // Registered Contact Info (Email & Phone)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TextGrey, modifier = Modifier.size(13.dp))
                            Text(currentUser.email, color = TextGrey, fontSize = 12.sp)
                            Text("•", color = TextGrey, fontSize = 12.sp)
                            Icon(Icons.Default.Phone, contentDescription = null, tint = TextGrey, modifier = Modifier.size(13.dp))
                            Text(currentUser.phone, color = TextGrey, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // KYC Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when (currentUser.kycStatus) {
                                        KYCStatus.VERIFIED -> SuccessGreen.copy(alpha = 0.2f)
                                        KYCStatus.PENDING -> AccentGold.copy(alpha = 0.2f)
                                        else -> ErrorRed.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "KYC: ${currentUser.kycStatus.name}",
                                color = when (currentUser.kycStatus) {
                                    KYCStatus.VERIFIED -> SuccessGreen
                                    KYCStatus.PENDING -> AccentGold
                                    else -> ErrorRed
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // User Stats Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceDark)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Matches", color = TextGrey, fontSize = 10.sp)
                                Text("${currentUser.totalMatchesJoined}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Kills", color = TextGrey, fontSize = 10.sp)
                                Text("${currentUser.totalKills}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Wins", color = TextGrey, fontSize = 10.sp)
                                Text("${currentUser.totalWins}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = {
                                editName = currentUser.name
                                editAvatarUrl = currentUser.avatarUrl
                                showEditProfileDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPurple)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EDIT IGN & PHOTO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Referral Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("referral_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, tint = AccentGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REFER & EARN ₹25 BONUS", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Invite your Free Fire squad friends to WinX7! Both get ₹25 bonus on sign up.", color = TextGrey, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceDark)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("YOUR REFERRAL CODE", color = TextGrey, fontSize = 10.sp)
                                Text(currentUser.referralCode, color = TextWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(currentUser.referralCode))
                                    snackbarMsg = "Referral code copied!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Copy Code", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // App Settings & Actions
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingsRow(icon = Icons.Default.PrivacyTip, title = "Privacy Policy") {
                            snackbarMsg = "WinX7 Privacy Policy v1.0"
                        }
                        HorizontalDivider(color = SurfaceBorder)
                        SettingsRow(icon = Icons.Default.Description, title = "Terms & Conditions") {
                            snackbarMsg = "WinX7 Terms & Fair Play Rules"
                        }
                        HorizontalDivider(color = SurfaceBorder)
                        SettingsRow(icon = Icons.Default.Info, title = "About WinX7 App") {
                            snackbarMsg = "WinX7 Free Fire Tournament Platform v1.0.0"
                        }
                        HorizontalDivider(color = SurfaceBorder)
                        SettingsRow(icon = Icons.Default.Share, title = "Share App with Friends") {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Join WinX7 Free Fire Tournaments and win daily cash prizes! Download now using code: ${currentUser.referralCode}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share WinX7"))
                        }
                        HorizontalDivider(color = SurfaceBorder)
                        SettingsRow(icon = Icons.Default.Logout, title = "Log Out", isDestructive = true) {
                            showLogoutDialog = true
                        }
                    }
                }
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                containerColor = CardDark,
                title = { Text("Confirm Log Out", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to log out of your WinX7 account?", color = TextGrey) },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("LOG OUT", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("CANCEL", color = TextGrey)
                    }
                }
            )
        }

        // Edit Profile Dialog (IGN & Profile Photo only)
        if (showEditProfileDialog) {
            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                containerColor = CardDark,
                title = { Text("Edit Profile (IGN & Photo)", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Notice banner
                        Surface(
                            color = SurfaceDark,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Registered Email & Phone cannot be changed. You can only modify your In-Game Name (IGN) and Profile Photo.",
                                    color = TextGrey,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        // Editable IGN
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("In-Game Name (IGN)", color = TextGrey) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryPurple) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Choose Preset Avatar Media or Upload
                        Column {
                            Text("Select Avatar Media:", color = TextGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                presetAvatars.forEach { url ->
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = if (editAvatarUrl == url) 2.dp else 1.dp,
                                                color = if (editAvatarUrl == url) AccentGold else SurfaceBorder,
                                                shape = CircleShape
                                            )
                                            .clickable { editAvatarUrl = url }
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Avatar Media Option",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                                contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                            ) { uri ->
                                if (uri != null) {
                                    snackbarMsg = "Uploading profile picture..."
                                    viewModel.uploadProfileImage(uri) { success, msg ->
                                        snackbarMsg = msg
                                        if (success) {
                                            showEditProfileDialog = false
                                        }
                                    }
                                }
                            }
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = "Upload", tint = AccentGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload from Gallery", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = SurfaceBorder)

                        // Registered Details (Read-only)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("REGISTERED USER DETAILS", color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Email: ${currentUser.email}", color = TextGrey, fontSize = 12.sp)
                            Text("Phone: ${currentUser.phone}", color = TextGrey, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateProfile(name = editName.ifBlank { currentUser.name }, avatarUrl = editAvatarUrl)
                            showEditProfileDialog = false
                            snackbarMsg = "Profile updated successfully!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
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

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) ErrorRed else TextWhite,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = if (isDestructive) ErrorRed else TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextGrey)
    }
}

