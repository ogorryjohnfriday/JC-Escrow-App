package com.jcinc.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jcinc.data.api.RetrofitInstance
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()


    // Password visibility toggle
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }


    // User input fields
    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // UI state
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Account") }
            )
        }
    ) { paddingValues ->
// -------------------- COLUMN START --------------------
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)  // ✅ enables scroll when keyboard appears
                .imePadding()                 // ✅ adjusts layout for soft keyboard
                .statusBarsPadding()          // ✅ avoids top overlap with status bar
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))


            Spacer(modifier = Modifier.height(20.dp))

            // ✅ First name
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            // ✅ Surname
            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                label = { Text("Surname") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            // ✅ Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // ✅ Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // ✅ Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))

            // ✅ Confirm Password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
            )

            Spacer(modifier = Modifier.height(25.dp))

            // ✅ Register Button
            Button(
                onClick = {
                    if (
                        firstName.isBlank() ||
                        surname.isBlank() ||
                        email.isBlank() ||
                        phone.isBlank() ||
                        password.isBlank() ||
                        confirmPassword.isBlank()
                    ) {
                        message = "Please fill all fields"
                        return@Button
                    }

// ✅ Check if passwords match
                    if (password != confirmPassword) {
                        message = "Passwords do not match"
                        return@Button
                    }

                    isLoading = true
                    message = ""

                    coroutineScope.launch {
                        try {
                            // Step 1: Register the user
                            val registerResponse = RetrofitInstance.api.registerUser(
                                data = mapOf(
                                    "first_name" to firstName,
                                    "surname" to surname,
                                    "email" to email,
                                    "phone" to phone,
                                    "password" to password,
                                    "confirm_password" to confirmPassword
                                )
                            )

                            val body = registerResponse.body()

                            if (registerResponse.isSuccessful && body?.status == true) {
                                // Step 2: Trigger send_otp.php
                                try {
                                    RetrofitInstance.api.sendOtp(mapOf("email" to email))
                                } catch (_: Exception) { /* ignore silent OTP error */
                                }

                                // Step 3: Navigate to Email OTP screen
                                navController.navigate("email_otp/$email")
                                message = "Account created. Check your email for OTP."
                            } else {
                                message = body?.message ?: "Registration failed"
                            }
                        } catch (e: Exception) {
                            message = "Network error: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
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
                    Text("Register")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ “Already have an account?” link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account? ", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }) {
                    Text("Login now", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Display message (error/success)
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = if (message.contains("Check your email", ignoreCase = true))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}