package com.jcinc.ui.screens.auth

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var loginAttemptKey by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()


    // ---------------------------------------------------------
    // HANDLE LOGIN RESULT
    // ---------------------------------------------------------
    LaunchedEffect(loginAttemptKey, loginState) {

        if (loginState.message.isNotEmpty() || loginState.loginCompleted) {
            isLoading = false
        }

        if (loginState.message.isNotEmpty()) {
            message = loginState.message
        }

        if (!loginState.status && loginState.message.isNotEmpty()) {
            return@LaunchedEffect
        }

        if (loginState.loginCompleted && loginState.status) {

            if (!loginState.isVerified) {
                navController.navigate("ninBvnVerification") {
                    popUpTo("login") { inclusive = true }
                }
                return@LaunchedEffect
            }

            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
            }
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Login") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))
            Text("Welcome back!", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))


            // EMAIL FIELD
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )

            Spacer(modifier = Modifier.height(16.dp))


            // PASSWORD FIELD
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(25.dp))


            // LOGIN BUTTON
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        message = "Please enter both email and password."
                        return@Button
                    }

                    isLoading = true
                    loginAttemptKey++
                    viewModel.loginUser(context, email, password)
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
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            // ---------------------------------------------------------
            // SHOW BACKEND MESSAGE
            // ---------------------------------------------------------
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = if (loginState.status)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }


            // ---------------------------------------------------------
            // 🔥 SHOW "Verify OTP Now" LINK (ONLY FOR EMAIL NOT VERIFIED)
            // ---------------------------------------------------------
            if (message.contains("verify your email", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = {
                        if (email.isNotEmpty()) {
                            navController.navigate("email_otp/$email")
                        }
                    }
                ) {
                    Text(
                        "Verify OTP now",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }


            Spacer(modifier = Modifier.height(20.dp))


            Row {
                Text("Don't have an account?")
                TextButton(onClick = {
                    navController.navigate("register") {
                        popUpTo("login") { inclusive = true }
                    }
                }) {
                    Text("Sign up now")
                }
            }
        }
    }
}