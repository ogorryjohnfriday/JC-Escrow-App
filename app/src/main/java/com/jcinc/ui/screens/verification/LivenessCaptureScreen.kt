package com.jcinc.ui.screens.verification
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import android.Manifest
import android.content.Context
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.util.Base64
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.jcinc.ui.utils.BrightnessHandler
import androidx.camera.core.ImageProxy
import com.jcinc.data.TempKycStore
import androidx.camera.camera2.interop.ExperimentalCamera2Interop

@Composable
fun LivenessCaptureScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember {
        ImageCapture.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(1280, 720),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()
            )
            .setJpegQuality(95)   // sharper selfie
            .build()
    }
    var messageColor by remember { mutableStateOf(Color.Red) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var faceDetected by remember { mutableStateOf(false) }
    var captureMessage by remember { mutableStateOf("Kindly move your face into the frame") }
    var isCaptured by remember { mutableStateOf(false) }
    var stableFaceStart by remember { mutableStateOf<Long?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Request camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Keep screen bright during capture
    BrightnessHandler(active = hasCameraPermission && !isCaptured)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission) {

            // Brand text
            Text(
                text = "JCINCNIXDORF",
                color = Color(0xFF1976D2),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp)
            )

            // Camera preview
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(330.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            ) {
                CameraPreviewCircular(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    imageCapture = imageCapture,
                    onFaceDetected = { detected ->
                        coroutineScope.launch {
                            faceDetected = detected


                            if (detected && !isCaptured) {
                                if (stableFaceStart == null) {
                                    stableFaceStart = System.currentTimeMillis()
                                }

                                val stableDuration = System.currentTimeMillis() - (stableFaceStart ?: 0)

                                if (stableDuration >= 3000) {
                                    // ✅ Face held steady for 3 seconds
                                    isCaptured = true
                                    captureMessage = "Capturing..."
                                    messageColor = Color.Green

                                    coroutineScope.launch {

                                        takePhoto(context, imageCapture) { base64Image ->

                                            if (base64Image == null) {
                                                captureMessage = "Capture failed."
                                                messageColor = Color.Red
                                                isCaptured = false
                                                return@takePhoto
                                            }

                                            // Run suspend functions in a NEW coroutine
                                            coroutineScope.launch {
                                                try {
                                                    captureMessage = "Analyzing liveness..."
                                                    messageColor = Color.Black

                                                    // 1️⃣ SAFE: Now we are inside coroutine → withContext works
                                                    // Store selfie instantly
                                                    TempKycStore.selfieBase64 = base64Image

// Ensure required info exists
                                                    if (TempKycStore.mode == null || TempKycStore.idNumber == null) {
                                                        navController.navigate("verificationResult")
                                                        return@launch
                                                    }

// Immediately navigate – NO WAITING
                                                    navController.navigate("finalKycVerification")
                                                } catch (e: Exception) {
                                                    captureMessage = "Network error: ${e.message}"
                                                    messageColor = Color.Red
                                                    navController.navigate("verificationResult")
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    val secondsLeft = 3 - (stableDuration / 1000)
                                    captureMessage = "Hold steady... ${secondsLeft.coerceAtLeast(1)}s"
                                    messageColor = Color(0xFF1976D2)
                                }
                            } else {
                                stableFaceStart = null
                                if (!isCaptured) {
                                    captureMessage = "Kindly move your face into the frame"
                                    messageColor = Color.Red
                                }
                            }
                        }
                    }
                )

                FaceOverlayCircle(faceDetected = faceDetected)
            }

// Status message
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp)
            ) {
                Text(
                    text = captureMessage,
                    color = messageColor,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Text("Camera permission required", color = Color.Red)
        }
    }
}

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun CameraPreviewCircular(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    imageCapture: ImageCapture,
    onFaceDetected: (Boolean) -> Unit // ✅ updated to match new processImageProxy signature
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(Color.DarkGray)
        )
    } else {
        // ✅ Circular camera container
        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FIT_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }

                    // --- Face Detector configuration ---
                    val options = FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build()

                    val detector = FaceDetection.getClient(options)

                    // --- Resolution selector for camera ---
                    val resolutionSelector = ResolutionSelector.Builder()
                        .setAspectRatioStrategy(
                            AspectRatioStrategy(
                                AspectRatio.RATIO_4_3,
                                AspectRatioStrategy.FALLBACK_RULE_AUTO
                            )
                        )
                        .setResolutionStrategy(
                            ResolutionStrategy(
                                Size(1280, 720),   // FULL HD
                                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                            )
                        )
                        .build()
                    val analyzer = ImageAnalysis.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(
                                ContextCompat.getMainExecutor(ctx)
                            ) { imageProxy ->
                                processImageProxy(detector, imageProxy) { aligned ->
                                    onFaceDetected(aligned)
                                }
                            }
                        }
                    imageCapture.flashMode = ImageCapture.FLASH_MODE_AUTO

                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        val cameraProvider = providerFuture.get()

                        // --- HIGH-QUALITY PREVIEW BUILDER ---

                        val previewBuilder = Preview.Builder()
                            .setResolutionSelector(resolutionSelector)

// ⭐ Enable Camera2 autofocus mode
                        Camera2Interop.Extender(previewBuilder)
                            .setCaptureRequestOption(
                                CaptureRequest.CONTROL_AF_MODE,
                                CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )

                        val preview = previewBuilder
                            .build()
                            .also { it.surfaceProvider = previewView.surfaceProvider }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_FRONT_CAMERA,
                                preview,
                                imageCapture,
                                analyzer
                            )
                        } catch (e: Exception) {
                            Log.e("CameraX", "Binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape) // ✅ Ensures perfect round shape
            )
        }
    }
}
@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    detector: com.google.mlkit.vision.face.FaceDetector,
    imageProxy: ImageProxy,
    onFaceDetected: (Boolean) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces.first()

                    // --- Extract bounding box ---
                    val box = face.boundingBox
                    val imgWidth = image.width.toFloat()
                    val imgHeight = image.height.toFloat()

                    val faceCenterX = box.centerX() / imgWidth
                    val faceCenterY = box.centerY() / imgHeight
                    val faceRadiusX = (box.width() / imgWidth) / 2
                    val faceRadiusY = (box.height() / imgHeight) / 2

                    // --- Define circular detection zone ---
                    val frameCenterX = 0.5f
                    val frameCenterY = 0.65f
                    val frameRadius = 0.40f

                    // --- Distance from target center ---
                    val dx = faceCenterX - frameCenterX
                    val dy = faceCenterY - frameCenterY
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                    // --- Face must be centered + correct size ---
                    val aligned =
                        distance + (faceRadiusX.coerceAtLeast(faceRadiusY)) < frameRadius &&
                                faceRadiusY > 0.22f &&
                                faceRadiusY < 0.50f

                    onFaceDetected(aligned)
                } else {
                    onFaceDetected(false)
                }
            }
            .addOnFailureListener {
                onFaceDetected(false)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
@Composable
fun FaceOverlayCircle(faceDetected: Boolean) {
    Canvas(modifier = Modifier.size(280.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2 - 4.dp.toPx()

        // 🔵 Dynamic outer circle color
        val borderColor = if (faceDetected) Color(0xFF00FF00) else Color(0xFFFF3B30)

        // Outer border
        drawCircle(
            color = borderColor,
            radius = radius,
            center = center,
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )

        // 🟢 Dotted inner oval (face guide)
        val dotCount = 80
        val dotRadius = 3.dp.toPx()

        // Adjust these for oval shape
        val horizontalScale = 0.55f   // width (lower = thinner)
        val verticalScale = 0.75f     // height (higher = longer)
        val offsetY = -radius * 0.08f // move upward slightly

        for (i in 0 until dotCount) {
            val angle = (i * (360f / dotCount)) * (Math.PI / 180f)
            val x = center.x + radius * horizontalScale * kotlin.math.cos(angle).toFloat()
            val y = center.y + radius * verticalScale * kotlin.math.sin(angle).toFloat() + offsetY
            drawCircle(
                color = Color.Green.copy(alpha = 0.8f),
                radius = dotRadius,
                center = Offset(x, y)
            )
        }
    }
}

fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (String?) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(context)

    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {

        override fun onCaptureSuccess(imageProxy: ImageProxy) {
            try {
                // ⭐ Read RAW JPEG bytes directly (NO compression)
                val buffer = imageProxy.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                // ⭐ Direct Base64 of raw high-resolution JPEG
                val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)

                onImageCaptured(base64Image)
            } catch (e: Exception) {
                Log.e("CameraX", "Error processing image", e)
                onImageCaptured(null)
            } finally {
                imageProxy.close()
            }
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraX", "Capture failed", exception)
            onImageCaptured(null)
        }
    })
}

// 🔹 Extension function for formatted decimal output (used in logs)
private fun Float.format(digits: Int): String = "%.${digits}f".format(this)