package com.jcinc.ui.screens.intro

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jcinc.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import com.jcinc.data.getVerificationStatus
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(navController: NavController) {

    val context = LocalContext.current

    var isChecking by remember { mutableStateOf(true) }

    // 🔥 Auto-check KYC status on launch
    LaunchedEffect(Unit) {

        delay(600) // small splash delay (optional)

        val status = getVerificationStatus(context)

        when (status) {
            "verified" -> {
                navController.navigate("dashboard") {
                    popUpTo("intro") { inclusive = true }
                }
            }
            "pending",
            "failed" -> {
                navController.navigate("verificationResult") {
                    popUpTo("intro") { inclusive = true }
                }
            }
            else -> {
                // new user → stay on intro
            }
        }

        isChecking = false
    }

    // 🔄 Loading overlay
    if (isChecking) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    // ------------------------------------------------------------------------------------------
    // ORIGINAL INTRO UI BELOW
    // ------------------------------------------------------------------------------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 🔹 Background image
        Image(
            painter = painterResource(id = R.drawable.intro_bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 🔹 Foreground content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "JC Escrow Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to JC Escrow",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Buy and sell safely with verified identity and escrow-protected payments.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { navController.navigate("register") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A1B9A) // Purple
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Get Started",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Login",
                color = Color.Yellow,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { navController.navigate("login") }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "from Ogorry",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun IntroScreenPreview() {
    val navController = rememberNavController()
    IntroScreen(navController = navController)
}