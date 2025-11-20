package com.jcinc.ui.screens.verification

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jcinc.R

@Composable
fun FaceVerificationConsentScreen(navController: NavController) {
    // --- Avatar bounce animation ---
    val infiniteTransition = rememberInfiniteTransition(label = "avatarAnim")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ), label = "avatarBounce"
    )

    // --- Button pulse animation ---
    val buttonPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ), label = "buttonPulse"
    )

    // --- UI layout ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // 🧠 Title
        Text(
            text = "Facial Feature Verification",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black,
            modifier = Modifier.padding(top = 16.dp)
        )

        // 🪶 Subtitle
        Text(
            text = "To ensure that the current operation is performed by the account holder, your identity needs to be verified via facial features.",
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // 🌟 Animated Avatar
        Box(
            modifier = Modifier
                .scale(bounce)
                .shadow(8.dp, CircleShape)
                .background(Color(0xFFE8F5E9), CircleShape)
                .padding(30.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.face_avatar), // 🧠 your custom avatar image
                contentDescription = "Face Verification Avatar",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(100.dp)
            )
        }

        // ✅ Info box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                .padding(16.dp)
                .padding(top = 8.dp)
        ) {
            Text(
                text = "Face verification is required to confirm your identity and keep your account safe.\nThe captured facial image is only used for verification purposes.",
                textAlign = TextAlign.Center,
                color = Color(0xFF2E7D32),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 🌈 Animated Verify Button
        Button(
            onClick = { navController.navigate("livenessCapture") },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .scale(buttonPulse)
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE91E63),
                contentColor = Color.White
            ),
            shape = CircleShape // 🪝 pill-style button
        ) {
            Text(
                text = "Verify",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(50.dp))
    }
}