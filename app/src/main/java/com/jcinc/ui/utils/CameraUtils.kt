package com.jcinc.ui.utils

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream

fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onResult: (String?) -> Unit
) {
    Log.d("CameraUtils", "Attempting to take photo...")

    try {
        // 🟢 Save image directly to cache without resizing or compression
        val photoFile = File(context.cacheDir, "face_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    try {
                        val bytes = FileInputStream(photoFile).readBytes()

                        // 🟢 Encode full-quality image to Base64 (no compression)
                        val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)

                        Log.d("CameraUtils", "📸 Capture success — size=${bytes.size / 1024} KB")
                        onResult(base64Image)
                    } catch (e: Exception) {
                        Log.e("CameraUtils", "Base64 encoding failed: ${e.message}")
                        onResult(null)
                    } finally {
                        // Delete temp file to free space
                        photoFile.delete()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraUtils", "Capture failed: ${exception.message}", exception)
                    onResult(null)
                }
            }
        )
    } catch (e: Exception) {
        Log.e("CameraUtils", "Unexpected error: ${e.message}", e)
        onResult(null)
    }
}