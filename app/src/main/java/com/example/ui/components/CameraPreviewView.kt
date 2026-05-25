package com.example.ui.components

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture,
    onViewReady: (PreviewView) -> Unit = {},
    onCameraAvailabilityChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCameraAvailable by remember { mutableStateOf(true) }
    
    // Create PreviewView once
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Handle Camera Lifecycle binding sequentially and cleanly in LaunchedEffect
    LaunchedEffect(lifecycleOwner) {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val executor = ContextCompat.getMainExecutor(context)
            
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    
                    // Clear any previous bindings
                    cameraProvider.unbindAll()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    // Only bind if camera permission is granted and the lifecycle is active
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    if (hasPermission && lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.INITIALIZED)) {
                        if (cameraProvider.hasCamera(cameraSelector)) {
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                            onViewReady(previewView)
                            isCameraAvailable = true
                            onCameraAvailabilityChanged(true)
                        } else {
                            Log.e("CameraPreviewView", "Device/Emulator does not have a back camera.")
                            isCameraAvailable = false
                            onCameraAvailabilityChanged(false)
                        }
                    } else if (!hasPermission) {
                        Log.e("CameraPreviewView", "Camera permission not granted; skipping bindToLifecycle")
                    }
                } catch (e: Throwable) {
                    Log.e("CameraPreviewView", "Error binding CameraX lifecycle context", e)
                    isCameraAvailable = false
                    onCameraAvailabilityChanged(false)
                }
            }, executor)
        } catch (e: Throwable) {
            Log.e("CameraPreviewView", "Error instantiating ProcessCameraProvider", e)
            isCameraAvailable = false
            onCameraAvailabilityChanged(false)
        }
    }

    // Clean up all camera bindings when leaving composition or changing perspective
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                if (cameraProviderFuture.isDone) {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                }
            } catch (e: Throwable) {
                Log.e("CameraPreviewView", "Error unbinding camera on disposal", e)
            }
        }
    }

    if (isCameraAvailable) {
        AndroidView(
            factory = { previewView },
            modifier = modifier
        )
    } else {
        // Render a cute post-card visual fallback for emulators
        androidx.compose.foundation.layout.Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF1C1B1F)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = Color(0xFFD1E4FF),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Không tìm thấy Máy ảnh ô tô / Trình giả lập",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Vui lòng sử dụng tính năng \"Thư viện\" (nút album dưới góc trái) để chọn ảnh và tạo con tem nghệ thuật tuyệt đẹp của bạn!",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
