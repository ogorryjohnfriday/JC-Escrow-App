package com.jcinc.ui.screens.verification

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.compose.material3.CenterAlignedTopAppBar
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationResultScreen(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry
) {
    // 👉 Extract raw arguments
    val statusRaw = backStackEntry.arguments?.getString("status") ?: "failed"
    val msgRaw = backStackEntry.arguments?.getString("msg") ?: "Verification failed."
    val confidenceRaw = backStackEntry.arguments?.getString("confidence") ?: ""

    // 👉 Decode URL-encoded message (fixes "+" and %20 issues)
    val msg = URLDecoder.decode(msgRaw, StandardCharsets.UTF_8.toString())
    val status = URLDecoder.decode(statusRaw, StandardCharsets.UTF_8.toString())
    val confidence = URLDecoder.decode(confidenceRaw, StandardCharsets.UTF_8.toString())

    val isSuccess = status == "success"

    // ❌ Disable back button
    BackHandler { /* no back */ }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Verification Result", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔥 Title
            Text(
                text = if (isSuccess) "Verification Successful!" else "Verification Failed",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = if (isSuccess)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(15.dp))

            // 🔥 Display decoded message
            Text(
                text = msg,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )

            // 🔥 Display confidence if included
            if (confidence.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Confidence: $confidence%",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(45.dp))

            // 🔥 Buttons
            if (isSuccess) {

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    onClick = {
                        navController.navigate("dashboard") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Continue")
                }

            } else {

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    onClick = {
                        navController.navigate("ninBvnVerification") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Try Again")
                }
            }
        }
    }
}