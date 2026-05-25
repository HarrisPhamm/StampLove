package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StampOverlayMask(
    shape: String, // "RECTANGLE", "SQUARE", "CIRCLE"
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val totalWidth = size.width
        val totalHeight = size.height

        // Beautiful elegant translucent frosted mask to "mờ" (fade out) the cropped-off area
        val maskColor = Color(0x99000000)

        // Calculate Central cut-out dimensions
        val cutoutWidth = totalWidth * 0.72f
        val cutoutHeight = when (shape) {
            "SQUARE", "CIRCLE" -> cutoutWidth
            else -> cutoutWidth * 1.33f // Rectangle (Portrait Stamp)
        }

        val left = (totalWidth - cutoutWidth) / 2f
        val top = (totalHeight - cutoutHeight) / 2f
        val right = left + cutoutWidth
        val bottom = top + cutoutHeight

        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas
            val checkpoint = nativeCanvas.saveLayer(0f, 0f, totalWidth, totalHeight, null)
            try {
                val paint = Paint().apply {
                    color = maskColor
                }
                // 1. Draw solid dark mask
                canvas.drawRect(0f, 0f, totalWidth, totalHeight, paint)

                // 2. Prepare clip paint for cutouts with PORTER_DUFF CLEAR
                val clearPaint = android.graphics.Paint().apply {
                    xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                    isAntiAlias = true
                }

                // Draw central viewport
                if (shape == "CIRCLE") {
                    val cx = totalWidth / 2f
                    val cy = totalHeight / 2f
                    val radius = cutoutWidth / 2f
                    nativeCanvas.drawCircle(cx, cy, radius, clearPaint)

                    // Draw tiny teeth (perforations) along the circular borders
                    val teethRadius = 13f
                    val holeCount = 36
                    for (i in 0 until holeCount) {
                        val angle = (2 * Math.PI * i) / holeCount
                        val tx = cx + radius * cos(angle).toFloat()
                        val ty = cy + radius * sin(angle).toFloat()
                        nativeCanvas.drawCircle(tx, ty, teethRadius, clearPaint)
                    }

                    // Restore and draw cute vintage inner border
                    val linePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#0061A4") // Vibrant royal post blue
                        alpha = 180
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 3f
                    }
                    nativeCanvas.drawCircle(cx, cy, radius - 6f, linePaint)
                } else {
                    // Rectangle or Square Stamp Cutout
                    nativeCanvas.drawRect(left, top, right, bottom, clearPaint)

                    val teethRadius = 14f
                    val step = 32f

                    // Top & Bottom teeth
                    var currX = left + 12f
                    while (currX < right) {
                        nativeCanvas.drawCircle(currX, top, teethRadius, clearPaint)
                        nativeCanvas.drawCircle(currX, bottom, teethRadius, clearPaint)
                        currX += step
                    }

                    // Left & Right teeth
                    var currY = top + 12f
                    while (currY < bottom) {
                        nativeCanvas.drawCircle(left, currY, teethRadius, clearPaint)
                        nativeCanvas.drawCircle(right, currY, teethRadius, clearPaint)
                        currY += step
                    }

                    // High-contrast deep blue inner frame for reference and aesthetic polish
                    val linePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#0061A4") // Vibrant royal post blue
                        alpha = 180
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 3f
                    }
                    nativeCanvas.drawRect(left + 8f, top + 8f, right - 8f, bottom - 8f, linePaint)
                }
            } finally {
                nativeCanvas.restoreToCount(checkpoint)
            }
        }
    }
}
