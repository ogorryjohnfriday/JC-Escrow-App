package com.jcinc.ui.screens.verification

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.jcinc.data.TempKycStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NinBvnVerificationScreen(navController: NavHostController) {

    // 🔹 Select input mode
    var selectedMode by remember { mutableStateOf("NIN") }

    var nin by remember { mutableStateOf("") }
    var bvn by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KYC Verification", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔘 Mode selector
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedMode == "NIN",
                    onClick = { selectedMode = "NIN" },
                    label = { Text("Use NIN") }
                )

                FilterChip(
                    selected = selectedMode == "BVN",
                    onClick = { selectedMode = "BVN" },
                    label = { Text("Use BVN") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // First Name
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Surname
            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                label = { Text("Surname") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // NIN or BVN field
            if (selectedMode == "NIN") {
                OutlinedTextField(
                    value = nin,
                    onValueChange = { nin = it.take(11) },
                    label = { Text("NIN (11 digits)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = bvn,
                    onValueChange = { bvn = it.take(11) },
                    label = { Text("BVN (11 digits)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Continue Button
            Button(
                onClick = {

                    if (firstName.isBlank() || surname.isBlank()) {
                        message = "First Name and Surname are required."
                        return@Button
                    }

                    val id = if (selectedMode == "NIN") nin else bvn
                    if (id.length != 11) {
                        message = "Invalid $selectedMode — must be 11 digits."
                        return@Button
                    }

                    // Save for later (liveness step)
                    TempKycStore.firstName = firstName
                    TempKycStore.lastName = surname
                    TempKycStore.mode = selectedMode
                    TempKycStore.idNumber = id

                    // Go to camera
                    navController.navigate("faceVerificationConsent")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text("Continue to Face Capture")
            }

            // Error message
            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}