package com.jcinc.ui.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * 🔆 Automatically adjusts screen brightness when active.
 * Set brightness to full during sensitive camera operations
 * (e.g. liveness verification), then restores afterward.
 *
 * Usage:
 *   BrightnessHandler(active = true)
 */
@Composable
fun BrightnessHandler(active: Boolean) {
    val context = LocalContext.current
    val activity = context.findActivity()
    var originalBrightness by remember { mutableFloatStateOf(-1f) } // system default

    // When active = true, brighten to max
    LaunchedEffect(active) {
        val window = activity?.window
        val params = window?.attributes
        if (params != null) {
            if (active) {
                // Store previous brightness only once
                if (originalBrightness == -1f) {
                    originalBrightness = params.screenBrightness
                }
                params.screenBrightness = 1f // max brightness
            } else {
                params.screenBrightness = originalBrightness
            }
            window.attributes = params
        }
    }

    // Ensure brightness is restored when screen disposes
    DisposableEffect(Unit) {
        onDispose {
            val window = activity?.window
            val params = window?.attributes
            if (params != null) {
                params.screenBrightness = originalBrightness
                window.attributes = params
            }
        }
    }
}

/**
 * 🔍 Helper to extract Activity from a Composable Context.
 */
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}