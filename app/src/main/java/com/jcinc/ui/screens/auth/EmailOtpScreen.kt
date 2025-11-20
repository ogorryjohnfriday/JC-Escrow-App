package com.jcinc.ui.screens.auth
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailOtpScreen(
    navController: NavController,
    email: String,
    viewModel: EmailOtpViewModel = viewModel()
) {
    val otpState by viewModel.otpState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var otp by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var canResend by remember { mutableStateOf(false) }
    var timer by remember { mutableIntStateOf(60) }

    // 🔁 Countdown logic for resend
    LaunchedEffect(Unit) {
        while (timer > 0) {
            delay(1000)
            timer--
        }
        canResend = true
    }

    // ✅ Handle OTP verification state only (NOT resend)
    LaunchedEffect(otpState) {
        if (otpState.message.isNotEmpty()) {
            isLoading = false
            message = otpState.message

            // ✅ Navigate ONLY when OTP verified successfully
            if (otpState.status && otpState.message.contains("OTP verified", ignoreCase = true)) {
                coroutineScope.launch {
                    message = "Email verified successfully 🎉"
                    delay(1200)
                    navController.navigate("login") {
                        popUpTo("intro") { inclusive = false }
                    }
                }
            }
        }
    }
// 🔥 Block back button and send user to Intro screen
    BackHandler {
        navController.navigate("intro") {
            popUpTo("intro") { inclusive = true }
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Verify Email") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Enter the 6-digit verification code sent to:",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(email, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                label = { Text("Enter OTP") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.height(25.dp))

            // ✅ Verify OTP button
            Button(
                onClick = {
                    if (otp.isEmpty()) {
                        message = "Please enter the OTP code."
                        return@Button
                    }
                    isLoading = true
                    message = ""
                    viewModel.verifyOtp(email, otp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Verify OTP")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Resend OTP button — does NOT navigate or verify automatically
            TextButton(
                enabled = canResend,
                onClick = {
                    if (canResend) {
                        isLoading = true
                        message = ""
                        viewModel.resendOtp(email)

                        // Restart timer after resend
                        timer = 60
                        canResend = false
                        coroutineScope.launch {
                            while (timer > 0) {
                                delay(1000)
                                timer--
                            }
                            canResend = true
                        }
                        isLoading = false
                        message = "A new OTP has been sent to your email."
                    }
                }
            ) {
                Text(
                    if (canResend) "Resend OTP"
                    else "Resend in ${timer}s"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ✅ Show any messages
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = if (message.contains("successfully", ignoreCase = true))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // ✅ Centered Success Toast Overlay
            if (message == "Account verified successfully 🎉") {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = androidx.compose.ui.graphics.Color(0xFFFFD700), // Gold
                        shape = MaterialTheme.shapes.medium,
                        shadowElevation = 6.dp,
                        tonalElevation = 3.dp
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}