
package com.jcinc.ui.screens.verification
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.jcinc.data.BvnVerificationResponse
import com.jcinc.data.NinVerificationResponse
import com.jcinc.data.TempKycStore
import com.jcinc.data.getLoggedInEmail
import com.jcinc.data.setUserVerified
import com.jcinc.ui.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

@Composable
fun FinalKycVerificationScreen(navController: NavHostController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var message by remember { mutableStateOf("Finalizing verification…") }
    var showLoader by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {

            try {
                // 1️⃣ Load temporary values
                val mode = TempKycStore.mode?.lowercase()
                val idNumber = TempKycStore.idNumber
                val selfie = TempKycStore.selfieBase64
                val userFirst = TempKycStore.firstName
                val userLast = TempKycStore.lastName

                if (mode == null || idNumber == null || selfie == null) {
                    navigateError(navController, "Missing verification data")
                    return@launch
                }

                // 2️⃣ Email from DataStore
                val email = getLoggedInEmail(context)
                if (email.isNullOrBlank()) {
                    navigateError(navController, "Missing user email")
                    return@launch
                }

                // 3️⃣ First Step — Liveness Check
                message = "Checking liveness…"

                val livenessPayload = mapOf("image" to selfie)

                val liveResp = withContext(Dispatchers.IO) {
                    ApiClient.api.verifyLiveness(livenessPayload)
                }

                if (!liveResp.isSuccessful) {
                    navigateError(navController, "Liveness error ${liveResp.code()}")
                    return@launch
                }

                val liveBody = liveResp.body()
                val isLivenessOk =
                    liveBody?.data?.liveness == true ||
                            liveBody?.status?.lowercase() == "success"

                if (!isLivenessOk) {
                    navigateError(navController, liveBody?.message ?: "Liveness failed")
                    return@launch
                }

                // 4️⃣ NIN or BVN + selfie verification
                message = "Verifying ID with Dojah…"

                val finalFirst: String
                val finalLast: String
                val finalDob: String
                val finalNin: String
                val finalBvn: String

                if (mode == "nin") {
                    val resp: Response<NinVerificationResponse> = withContext(Dispatchers.IO) {
                        ApiClient.api.verifyNinWithLiveness(
                            mapOf(
                                "nin" to idNumber,
                                "selfie_image" to selfie
                            )
                        )
                    }

                    if (!resp.isSuccessful) {
                        navigateError(navController, "NIN verify error ${resp.code()}")
                        return@launch
                    }

                    val body = resp.body()
                    val data = body?.data
                    if (data == null) {
                        navigateError(navController, "Invalid NIN")
                        return@launch
                    }

                    finalFirst = data.first_name ?: ""
                    finalLast  = data.last_name ?: ""
                    finalDob   = data.date_of_birth ?: ""
                    finalNin   = data.nin ?: ""
                    finalBvn   = ""

                } else {
                    val resp: Response<BvnVerificationResponse> = withContext(Dispatchers.IO) {
                        ApiClient.api.verifyBvnWithLiveness(
                            mapOf(
                                "bvn" to idNumber,
                                "selfie_image" to selfie
                            )
                        )
                    }

                    if (!resp.isSuccessful) {
                        navigateError(navController, "BVN verify error ${resp.code()}")
                        return@launch
                    }

                    val body = resp.body()
                    val data = body?.data
                    if (data == null) {
                        navigateError(navController, "Invalid BVN")
                        return@launch
                    }

                    finalFirst = data.first_name ?: ""
                    finalLast  = data.last_name ?: ""
                    finalDob   = data.date_of_birth ?: ""
                    finalNin   = ""
                    finalBvn   = data.bvn ?: ""
                }

                // 5️⃣ FINAL KYC UPDATE — Backend validation
                message = "Saving verification…"

                val finalPayload = mapOf(
                    "email" to email,
                    "mode" to mode,
                    "first_name" to finalFirst,
                    "surname" to finalLast,
                    "dob" to finalDob,
                    "nin" to finalNin,
                    "bvn" to finalBvn,
                    "user_first_name" to (userFirst ?: ""),
                    "user_last_name" to (userLast ?: "")
                )

                val finalResp = withContext(Dispatchers.IO) {
                    ApiClient.api.finalKycUpdate(finalPayload)
                }

                if (!finalResp.isSuccessful) {
                    navigateError(navController, "Final update failed ${finalResp.code()}")
                    return@launch
                }

                val finalBody = finalResp.body()
                if (finalBody == null || finalBody.status != "success") {
                    navigateError(navController, finalBody?.message ?: "Final verification failed")
                    return@launch
                }

                // 6️⃣ SUCCESS
                setUserVerified(context, true)
                TempKycStore.clear()

                navController.navigate("dashboard") {
                    popUpTo(0) { inclusive = true }
                }

            } catch (e: Exception) {
                navigateError(navController, e.localizedMessage ?: "Unexpected error")
            }
        }

    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showLoader) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(20.dp))
        }
        Text(message, fontWeight = FontWeight.Bold)
    }
}

private fun navigateError(navController: NavHostController, msg: String) {
    val safe = msg.replace(" ", "+")
    navController.navigate("verificationResult?status=error&msg=$safe")
}