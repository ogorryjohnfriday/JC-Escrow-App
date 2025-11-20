package com.jcinc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import com.jcinc.ui.screens.intro.IntroScreen
import com.jcinc.ui.screens.auth.LoginScreen
import com.jcinc.ui.screens.auth.RegisterScreen
import com.jcinc.ui.screens.auth.EmailOtpScreen

import com.jcinc.ui.screens.verification.NinBvnVerificationScreen
import com.jcinc.ui.screens.verification.LivenessCaptureScreen
import com.jcinc.ui.screens.verification.FinalKycVerificationScreen
import com.jcinc.ui.screens.verification.VerificationResultScreen
import com.jcinc.ui.screens.verification.FaceVerificationConsentScreen

import com.jcinc.ui.screens.dashboard.DashboardScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "intro"
    ) {

        // 🟦 INTRO + AUTH FLOW
        composable("intro") { IntroScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }

        // 🟦 EMAIL OTP
        composable(
            route = "email_otp/{email}",
            arguments = listOf(
                navArgument("email") { defaultValue = "" }
            )
        ) { entry ->
            val email = entry.arguments?.getString("email") ?: ""
            EmailOtpScreen(navController, email)
        }

        // 🟦 KYC STEP 1 — Choose NIN or BVN
        composable("ninBvnVerification") {
            NinBvnVerificationScreen(navController)
        }

        // 🟦 Optional consent screen
        composable("faceVerificationConsent") {
            FaceVerificationConsentScreen(navController)
        }

        // 🟦 KYC STEP 2 — Liveness Capture
        composable("livenessCapture") {
            LivenessCaptureScreen(navController)
        }

        // 🟦 KYC STEP 3 — Final verification
        composable("finalKycVerification") {
            FinalKycVerificationScreen(navController)
        }

        // 🟦 RESULT SCREEN — Supports encoded message + confidence
        composable(
            route = "verificationResult?status={status}&msg={msg}&confidence={confidence}",
            arguments = listOf(
                navArgument("status") { defaultValue = "failed" },
                navArgument("msg") { defaultValue = "" },
                navArgument("confidence") { defaultValue = "" }
            )
        ) { backStackEntry ->
            VerificationResultScreen(navController, backStackEntry)
        }

        // 🟦 MAIN USER DASHBOARD
        composable("dashboard") {
            DashboardScreen(navController)
        }
    }
}