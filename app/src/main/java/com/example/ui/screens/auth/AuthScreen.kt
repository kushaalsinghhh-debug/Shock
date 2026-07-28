package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.WinX7ViewModel

@Composable
fun AuthScreen(
    viewModel: WinX7ViewModel,
    onLoginSuccess: (UserRole) -> Unit
) {
    val adminConfig by viewModel.adminConfig.collectAsState()

    var isRegisterMode by remember { mutableStateOf(false) }

    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .systemBarsPadding()
            .imePadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth().testTag("auth_card")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // WinX7 Logo Crest
                Image(
                    painter = painterResource(id = R.drawable.winx7_logo_1785039507914),
                    contentDescription = "WINX7 Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.5.dp, PrimaryPurple, RoundedCornerShape(18.dp))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("WINX7", color = TextWhite, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, letterSpacing = 1.sp)
                Text("PLAY • COMPETE • WIN", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)

                Spacer(modifier = Modifier.height(18.dp))

                // Mode Title
                Text(
                    text = if (isRegisterMode) "Create Free Fire Account" else "Sign In to WinX7",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isRegisterMode) {
                    if (!adminConfig.isRegistrationOn) {
                        Text(
                            "New user registrations are currently disabled by Admin.",
                            color = ErrorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name", color = TextGrey) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextGrey) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_name_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number", color = TextGrey) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextGrey) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_phone_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email Address", color = TextGrey) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextGrey) },
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password", color = TextGrey) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextGrey) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                var successMessage by remember { mutableStateOf<String?>(null) }

                if (!isRegisterMode) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            text = "Forgot Password?",
                            color = AccentGold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clickable {
                                    if (emailInput.isBlank()) {
                                        errorMessage = "Enter your email to reset password."
                                    } else {
                                        viewModel.resetPassword(emailInput.trim()) { success, msg ->
                                            if (success) {
                                                successMessage = msg
                                                errorMessage = null
                                            } else {
                                                errorMessage = msg
                                                successMessage = null
                                            }
                                        }
                                    }
                                }
                        )
                    }
                }

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(err, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                successMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(msg, color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(18.dp))

                var isLoading by remember { mutableStateOf(false) }

                Button(
                    onClick = {
                        errorMessage = null
                        successMessage = null

                        if (isRegisterMode && !adminConfig.isRegistrationOn) {
                            errorMessage = "Registrations are disabled by Admin."
                            return@Button
                        }

                        if (emailInput.isBlank()) {
                            errorMessage = "Email cannot be empty."
                            return@Button
                        }
                        if (!viewModel.isValidEmail(emailInput.trim())) {
                            errorMessage = "Please enter a valid email address."
                            return@Button
                        }
                        if (passwordInput.isBlank()) {
                            errorMessage = "Password cannot be empty."
                            return@Button
                        }
                        if (passwordInput.length < 6) {
                            errorMessage = "Password must be at least 6 characters."
                            return@Button
                        }
                        if (isRegisterMode && (nameInput.isBlank() || phoneInput.isBlank())) {
                            errorMessage = "Please fill Full Name and Phone Number for registration."
                            return@Button
                        }

                        isLoading = true

                        if (isRegisterMode) {
                            viewModel.register(nameInput.trim(), phoneInput.trim(), emailInput.trim(), passwordInput) { success, msg ->
                                isLoading = false
                                if (success) {
                                    errorMessage = null
                                    // Immediately signal login success
                                    onLoginSuccess(UserRole.USER)
                                } else {
                                    errorMessage = msg
                                }
                            }
                        } else {
                            viewModel.login(emailInput.trim(), passwordInput) { success, msg ->
                                isLoading = false
                                if (success) {
                                    errorMessage = null
                                    // Detect role from email if required, otherwise repository will populate
                                    val detectedRole = when {
                                        emailInput.lowercase().contains("admin") -> UserRole.ADMIN
                                        emailInput.lowercase().contains("staff") -> UserRole.STAFF
                                        else -> UserRole.USER
                                    }
                                    viewModel.switchPortalRole(detectedRole)
                                    onLoginSuccess(detectedRole)
                                } else {
                                    errorMessage = msg
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, disabledContainerColor = PrimaryPurple.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isRegisterMode) "REGISTER & PLAY" else "LOGIN NOW",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.clickable {
                        isRegisterMode = !isRegisterMode
                        errorMessage = null
                    }
                ) {
                    Text(
                        text = if (isRegisterMode) "Already registered? " else "Don't have an account? ",
                        color = TextGrey,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (isRegisterMode) "Sign In" else "Create Account",
                        color = AccentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
