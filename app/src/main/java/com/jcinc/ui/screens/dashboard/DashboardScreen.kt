package com.jcinc.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.jcinc.data.TempKycStore
import com.jcinc.data.getVerificationStatus
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController) {

    val context = LocalContext.current

    var verificationStatus by remember { mutableStateOf<String?>(null) }
    var walletBalance by remember { mutableStateOf("₦0.00") }
    var loadingBalance by remember { mutableStateOf(false) }

    // Load verification status from DataStore
    LaunchedEffect(Unit) {
        verificationStatus = getVerificationStatus(context)
    }

    // Load balance (placeholder)
    LaunchedEffect(Unit) {
        loadingBalance = true
        delay(300)
        loadingBalance = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {

            // ------------------------------------
            // Greeting + Verification Status
            // ------------------------------------
            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User",
                    modifier = Modifier.size(58.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {

                    val displayName =
                        TempKycStore.firstName?.takeIf { it.isNotBlank() } ?: "User"

                    Text(
                        text = "Hello, $displayName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val statusText = when (verificationStatus) {
                        "verified" -> "Verified"
                        "pending" -> "Verification pending"
                        "failed" -> "Verification failed"
                        else -> "Not verified"
                    }

                    val statusColor = when (verificationStatus) {
                        "verified" -> Color(0xFF2ECC71)
                        "pending" -> Color(0xFFf1c40f)
                        "failed" -> Color.Red
                        else -> Color.Gray
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        if (verificationStatus != "verified") {
                            Text(
                                text = "Verify",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    navController.navigate("ninBvnVerification") {
                                        popUpTo("dashboard") { inclusive = false }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ------------------------------------
            // WALLET CARD — White + Blue mix
            // ------------------------------------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .shadow(6.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(Color.White, Color(0xFFBBDEFB)) // white + light blue
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Wallet Balance", fontWeight = FontWeight.Medium)

                            Row {
                                TextButton(onClick = { navController.navigate("fund_wallet") }) {
                                    Text("Fund")
                                }
                                TextButton(onClick = { navController.navigate("withdraw") }) {
                                    Text("Withdraw")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (loadingBalance) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp))
                        } else {
                            Text(
                                text = walletBalance,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ------------------------------------
            // ACTION CARDS — MIX COLOURS
            // ------------------------------------
            Row(modifier = Modifier.fillMaxWidth()) {

                GradientActionCard(
                    label = "Start Escrow",
                    icon = Icons.Default.Payments,
                    gradient = listOf(Color.White, Color(0xFF66BB6A)), // white + green
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    navController.navigate("initialize_transaction")
                }

                GradientActionCard(
                    label = "My Deals",
                    icon = Icons.AutoMirrored.Filled.List,
                    gradient = listOf(Color.White, Color(0xFF9575CD)), // white + purple
                    modifier = Modifier.weight(1f)
                ) {
                    navController.navigate("my_deals")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {

                GradientActionCard(
                    label = "Profile",
                    icon = Icons.Default.AccountCircle,
                    gradient = listOf(Color.White, Color(0xFF4FC3F7)), // white + sky blue
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    navController.navigate("profile")
                }

                GradientActionCard(
                    label = "Settings",
                    icon = Icons.Default.Lock,
                    gradient = listOf(Color.White, Color(0xFFCE93D8)), // white + soft purple
                    modifier = Modifier.weight(1f)
                ) {
                    navController.navigate("settings")
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Tip: Keep your verification completed to increase trust and limits.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
fun GradientActionCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(95.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(gradient)
                )
                .padding(14.dp)
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(icon, contentDescription = label, modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(label, fontWeight = FontWeight.Medium)
            }
        }
    }
}