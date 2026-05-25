package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.CameraPreviewView
import com.example.ui.components.StampOverlayMask
import com.example.ui.theme.DarkPostBlue
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.VintageCream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun CamCreatorTab(
    onImageSelected: (Bitmap, String) -> Unit, // passes the bitmap and active shape to editing sub-screen
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var selectedShape by remember { mutableStateOf("RECTANGLE") } // RECTANGLE, SQUARE, CIRCLE

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val bitmap = loadBitmapFromUri(context, it)
                if (bitmap != null) {
                    onImageSelected(bitmap, selectedShape)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var isCameraReallyAvailable by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VintageCream)
    ) {
        // --- Shape selector ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val shapes = listOf(
                "RECTANGLE" to "Hình chữ nhật 3:4",
                "SQUARE" to "Hình vuông 1:1",
                "CIRCLE" to "Hình tròn"
            )

            shapes.forEach { (shapeKey, displayName) ->
                val isSelected = selectedShape == shapeKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) DarkPostBlue else Color.Transparent)
                        .clickable { selectedShape = shapeKey }
                        .padding(vertical = 10.dp)
                        .testTag("shape_btn_$shapeKey"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // --- Viewfinder / Cam container ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(VintageCream),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                // Live Camera Viewfinder
                CameraPreviewView(
                    modifier = Modifier.fillMaxSize(),
                    imageCapture = imageCapture,
                    onCameraAvailabilityChanged = { isCameraReallyAvailable = it }
                )

                // Fancy perforated cutout overlapping
                StampOverlayMask(shape = selectedShape)
            } else {
                // Permission Denied visual layout
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Ứng dụng cần quyền sử dụng máy ảnh để chụp ảnh con tem trực tuyến.",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp),
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaRed)
                    ) {
                        Text("Cấp Quyền Máy Ảnh")
                    }
                }
            }
        }

        // --- Trigger Shutter row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Import gallery button to upload image of a stamp from library
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F0FE)) // Soft post blue tint outer ring
                    .clickable { galleryLauncher.launch("image/*") }
                    .padding(4.dp) // Cute double-ring effect
                    .testTag("gallery_import_btn"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Thư viện",
                        tint = DarkPostBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Big gorgeous shutter button (Professional Polish design style: border-8 of light-blue with deep-blue center)
            val isShutterEnabled = hasCameraPermission && isCameraReallyAvailable
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(elevation = 12.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFD1E4FF)) // light container background as the elegant border
                    .clickable(enabled = isShutterEnabled) {
                        if (isShutterEnabled) {
                            capturePhoto(context, imageCapture) { bitmap ->
                                if (bitmap != null) {
                                    onImageSelected(bitmap, selectedShape)
                                }
                            }
                        }
                    }
                    .padding(8.dp) // creates the thick border impact
                    .testTag("shutter_trigger_btn"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(if (isShutterEnabled) DarkPostBlue else Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Chụp hình",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Simple visual spacing/placeholder box for absolute symmetric layout
            Box(modifier = Modifier.size(56.dp))
        }
    }
}

private fun capturePhoto(context: Context, imageCapture: ImageCapture, onComplete: (Bitmap?) -> Unit) {
    val tempFile = File(context.cacheDir, "temp_capture_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

    try {
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                        if (tempFile.exists()) tempFile.delete()
                        onComplete(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onComplete(null)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    if (tempFile.exists()) tempFile.delete()
                    ContextCompat.getMainExecutor(context).execute {
                        android.widget.Toast.makeText(context, "Lỗi chụp ảnh: ${exception.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    onComplete(null)
                }
            }
        )
    } catch (e: Exception) {
        e.printStackTrace()
        if (tempFile.exists()) tempFile.delete()
        android.widget.Toast.makeText(context, "Thiết bị không hỗ trợ camera hoặc chưa sẵn sàng.", android.widget.Toast.LENGTH_SHORT).show()
        onComplete(null)
    }
}

suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
