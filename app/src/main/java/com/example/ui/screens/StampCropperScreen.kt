package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.ui.components.StampOverlayMask
import com.example.ui.theme.DarkPostBlue
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.VintageCream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StampCropperScreen(
    sourceBitmap: Bitmap,
    initialShape: String,
    onBack: () -> Unit,
    onCropped: (Bitmap, String) -> Unit, // passes cropped bitmap and shape to details screen
    modifier: Modifier = Modifier
) {
    var activeShape by remember { mutableStateOf(initialShape) }

    // Gesture States
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Track Viewport size
    var containerWidth by remember { mutableStateOf(1) }
    var containerHeight by remember { mutableStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VintageCream)
    ) {
        // --- TOP APP BAR ---
        TopAppBar(
            title = { Text("Cân Chỉnh Con Tem", fontWeight = FontWeight.Bold, color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkPostBlue)
        )

        // --- SHAPE CHANGER SELECTOR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val shapes = listOf(
                "RECTANGLE" to "Góc 3:4",
                "SQUARE" to "Vuông 1:1",
                "CIRCLE" to "Tròn"
            )

            shapes.forEach { (shapeKey, name) ->
                val isSelected = activeShape == shapeKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) DarkPostBlue else Color.Transparent)
                        .clickable {
                            activeShape = shapeKey
                            // Reset gestures on shape switch for clean state
                            scale = 1f
                            offset = Offset.Zero
                        }
                        .padding(vertical = 8.dp)
                        .testTag("crop_shape_$shapeKey"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Text(
            text = "Dùng 2 ngón tay thu phóng và di chuyển ảnh khớp khung",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // --- INTERACTIVE VIEWPORT CROP CONTAINER ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(VintageCream)
                .onGloballyPositioned { coordinates ->
                    containerWidth = coordinates.size.width
                    containerHeight = coordinates.size.height
                }
                .pointerInput(activeShape) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 6.0f)
                        offset += pan
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Raw Image showing beneath
            androidx.compose.foundation.Image(
                bitmap = sourceBitmap.asImageBitmap(),
                contentDescription = "Cân chỉnh ảnh",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )

            // Centered mask cut-out overlay
            StampOverlayMask(shape = activeShape)
        }

        // --- BOTTOM ACTIONS ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    val croppedBitmap = applyMatrixCrop(
                        source = sourceBitmap,
                        shape = activeShape,
                        scale = scale,
                        offset = offset,
                        cWidth = containerWidth,
                        cHeight = containerHeight
                    )
                    onCropped(croppedBitmap, activeShape)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_crop_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Xác nhận")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Xong - Nhập Thông Tin Tem", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

/**
 * Executes a precise matrix translation & scale crop on the physical bitmap in memory.
 * It matches what the user aligned visually inside the viewport constraint.
 */
private fun applyMatrixCrop(
    source: Bitmap,
    shape: String,
    scale: Float,
    offset: Offset,
    cWidth: Int,
    cHeight: Int
): Bitmap {
    val targetWidth = 800
    val targetHeight = if (shape == "RECTANGLE") 1066 else 800

    val cropped = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(cropped)
    val matrix = Matrix()

    // 1. Calculate base scale to match "Fit" in PreviewView
    val srcW = source.width.toFloat()
    val srcH = source.height.toFloat()

    val scaleFit = minOf(cWidth / srcW, cHeight / srcH)
    val displayedImageW = srcW * scaleFit
    val displayedImageH = srcH * scaleFit

    // 2. Map coordinates of displaying viewport to target canvas size
    val visualCutoutWidth = cWidth * 0.72f
    val visualCutoutHeight = if (shape == "RECTANGLE") visualCutoutWidth * 1.33f else visualCutoutWidth

    // Scale translation from Screen Pixels to Target Save pixels
    val ratio = targetWidth.toFloat() / visualCutoutWidth

    val screenCenterX = cWidth / 2f
    val screenCenterY = cHeight / 2f

    // Scale factor between direct source and target canvas
    val baseScaleX = targetWidth.toFloat() / displayedImageW
    val baseScaleY = targetHeight.toFloat() / displayedImageH
    
    // We want the default centered position mapped as identity, then apply scale & panning offsets
    matrix.postTranslate(-srcW / 2f, -srcH / 2f)
    matrix.postScale(scale, scale)
    matrix.postTranslate((srcW / 2f) + (offset.x / (scaleFit * scale)), (srcH / 2f) + (offset.y / (scaleFit * scale)))
    
    // Map fit-scale inside target
    val mapScale = (targetWidth.toFloat() / visualCutoutWidth) * scaleFit
    val displayTranslationX = ((cWidth - visualCutoutWidth) / 2f)
    val displayTranslationY = ((cHeight - visualCutoutHeight) / 2f)

    val finalMatrix = Matrix().apply {
        // Initial center
        postTranslate(-srcW / 2f, -srcH / 2f)
        // Apply pinch scale
        postScale(scale, scale)
        // Center shift
        postTranslate(srcW / 2f, srcH / 2f)
        // Back-scale to screen coords
        postScale(scaleFit, scaleFit)
        // Apply user drag offsets
        postTranslate(offset.x, offset.y)
        // Shift screen-center coordinates back to target cutout coordinates
        postTranslate(-displayTranslationX, -displayTranslationY)
        // Up-scale to save dimensions (800 x 800/1066)
        postScale(ratio, ratio)
    }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    canvas.drawBitmap(source, finalMatrix, paint)

    return cropped
}
