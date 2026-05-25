package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin

object StampProcessor {

    /**
     * Creates a beautiful postage stamp from any source image.
     * Including custom borders, perforations, custom vintage texts, and retro postmarks.
     */
    fun createStamp(
        source: Bitmap,
        shape: String, // "RECTANGLE", "SQUARE", "CIRCLE"
        title: String = "",
        faceValue: String = "1000đ",
        country: String = "VIỆT NAM"
    ): Bitmap {
        // Step 1: Crop source to fit the desired aspect ratio/shape
        val cropped = cropSourceToShape(source, shape)

        // Step 2: Establish base dimension for the output stamp
        val targetWidth = 800
        val targetHeight = when (shape) {
            "SQUARE", "CIRCLE" -> 800
            else -> 1066 // 3:4 aspect ratio for stamps
        }

        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Paint setup
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        
        // Define margins and perforation hole config
        val holeRadius = 20f
        val paperInset = holeRadius // space for full perforations on edges

        // --- DRAW STAMP PAPER BACKING ---
        val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FCF7ED") // Authentic creamy paper color
            style = Paint.Style.FILL
        }

        if (shape == "CIRCLE") {
            val cx = targetWidth / 2f
            val cy = targetHeight / 2f
            val radius = (targetWidth / 2f) - paperInset
            canvas.drawCircle(cx, cy, radius, paperPaint)
        } else {
            val rect = RectF(
                paperInset,
                paperInset,
                targetWidth.toFloat() - paperInset,
                targetHeight.toFloat() - paperInset
            )
            val cornerRadius = 8f
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paperPaint)
        }

        // --- ENFORCE PERFORATIONS (JAGGED HOLES) WITH CLEAR XFERMODE ---
        val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            style = Paint.Style.FILL
        }

        if (shape == "CIRCLE") {
            val cx = targetWidth / 2f
            val cy = targetHeight / 2f
            val radius = (targetWidth / 2f) - paperInset
            val holeCount = 42
            for (i in 0 until holeCount) {
                val angle = (2 * Math.PI * i) / holeCount
                val hx = cx + radius * cos(angle).toFloat()
                val hy = cy + radius * sin(angle).toFloat()
                canvas.drawCircle(hx, hy, holeRadius, clearPaint)
            }
        } else {
            // Rectangle or Square borders
            // Draw along Top & Bottom
            val topY = paperInset
            val bottomY = targetHeight.toFloat() - paperInset
            val spacingX = 45f // constant distance between holes
            
            var x = paperInset + 10f
            while (x < targetWidth.toFloat() - paperInset) {
                canvas.drawCircle(x, topY, holeRadius, clearPaint)
                canvas.drawCircle(x, bottomY, holeRadius, clearPaint)
                x += spacingX
            }

            // Draw along Left & Right
            val leftX = paperInset
            val rightX = targetWidth.toFloat() - paperInset
            val spacingY = 45f
            
            var y = paperInset + 10f
            while (y < targetHeight.toFloat() - paperInset) {
                canvas.drawCircle(leftX, y, holeRadius, clearPaint)
                canvas.drawCircle(rightX, y, holeRadius, clearPaint)
                y += spacingY
            }
        }

        // --- DRAW IMAGE CONTAINER INSET ---
        // Leave a beautiful, solid margin around the image inside the stamp paper
        val outerMargin = 55f
        val imageRect = if (shape == "CIRCLE") {
            val cx = targetWidth / 2f
            val cy = targetHeight / 2f
            val radius = (targetWidth / 2f) - outerMargin
            
            // Draw cropped circular photo
            val photoBitmap = Bitmap.createScaledBitmap(cropped, (radius * 2).toInt(), (radius * 2).toInt(), true)
            val circleMask = createCircularBitmap(photoBitmap)
            canvas.drawBitmap(circleMask, cx - radius, cy - radius, paint)
            
            // Inner decorative ring
            val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2A3E52")
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawCircle(cx, cy, radius, ringPaint)
            
            RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        } else {
            val rect = RectF(
                outerMargin,
                outerMargin,
                targetWidth.toFloat() - outerMargin,
                targetHeight.toFloat() - if (shape == "SQUARE") outerMargin else outerMargin + 30f // extra space for text at bottom of portrait stamps
            )
            
            // Draw image inside rect
            val srcRect = Rect(0, 0, cropped.width, cropped.height)
            val destRect = Rect(rect.left.toInt(), rect.top.toInt(), rect.right.toInt(), rect.bottom.toInt())
            canvas.drawBitmap(cropped, srcRect, destRect, paint)

            // Dynamic border around the photo
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2A3E52")
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRect(rect, borderPaint)
            
            // Inner delicate light border line inside the image for style
            val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#40FFFFFF")
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            val innerRect = RectF(rect.left + 8, rect.top + 8, rect.right - 8, rect.bottom - 8)
            canvas.drawRect(innerRect, innerBorderPaint)
            
            rect
        }

        // --- ADD AUTHENTIC POSTAGE STAMP TYPOGRAPHY ---
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2B201A") // Vintage ink color
            textAlign = Paint.Align.CENTER
            textSize = 28f
            isFakeBoldText = true
        }

        val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A31D1D") // Red or deep tone for value
            textAlign = Paint.Align.LEFT
            textSize = 32f
            isFakeBoldText = true
        }

        if (shape == "CIRCLE") {
            // Circle overlay text (circular text path could be complex, simple centered top and bottom is extremely neat)
            textPaint.textSize = 24f
            textPaint.color = Color.parseColor("#FFFFFF")
            
            // Subtle shadowed text over photo background for readability
            textPaint.setShadowLayer(4f, 0f, 2f, Color.BLACK)
            canvas.drawText(country.uppercase(), targetWidth / 2f, outerMargin + 50f, textPaint)
            textPaint.setShadowLayer(0f, 0f, 0f, 0)
            
            facePaint.textSize = 34f
            facePaint.color = Color.parseColor("#FFD700") // Golden face value
            facePaint.textAlign = Paint.Align.CENTER
            facePaint.setShadowLayer(5f, 0f, 2f, Color.BLACK)
            canvas.drawText(faceValue, targetWidth / 2f, targetHeight - outerMargin - 30f, facePaint)
            facePaint.setShadowLayer(0f, 0f, 0f, 0)
        } else {
            // Rectangle or Square text layouts
            // Country at the top
            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2A3E52")
                textSize = 26f
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.3f
                isFakeBoldText = true
            }
            canvas.drawText(country.uppercase(), targetWidth / 2f, outerMargin - 15f, headerPaint)

            // Post office / classification text
            val classificationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2A3E52")
                textSize = 20f
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.2f
            }
            canvas.drawText("BƯU CHÍNH", targetWidth / 2f, imageRect.bottom + 42f, classificationPaint)

            // Face Value in the bottom corner (beautiful!)
            facePaint.color = Color.parseColor("#A31D1D")
            facePaint.textSize = 36f
            facePaint.textAlign = Paint.Align.LEFT
            canvas.drawText(faceValue, outerMargin + 10f, imageRect.bottom + 45f, facePaint)
            
            // Custom stamp title or date in opposite bottom corner
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2A3E52")
                textSize = 22f
                textAlign = Paint.Align.RIGHT
            }
            val displayTitle = if (title.length > 15) title.substring(0, 13) + ".." else title
            canvas.drawText(displayTitle.uppercase(), targetWidth - outerMargin - 10f, imageRect.bottom + 42f, titlePaint)
        }

        // --- APPLY RETRO POSTMARK OVERLAY ---
        // A stunning historic postmark stamp with circle and date lines!
        val markPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1C3A5E") // stamp ink
            style = Paint.Style.STROKE
            strokeWidth = 3f
            alpha = 85 // semi-transparent (out of 255)
        }
        val markFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1C3A5E")
            textSize = 18f
            letterSpacing = 0.15f
            alpha = 85
            textAlign = Paint.Align.CENTER
        }

        // Postmark center is slightly randomized or offset near bottom right
        val mcx = targetWidth * 0.72f
        val mcy = targetHeight * 0.45f
        
        // Inner and outer circular marks
        canvas.drawCircle(mcx, mcy, 110f, markPaint)
        canvas.drawCircle(mcx, mcy, 65f, markPaint)
        
        // Some vintage text inside postmark
        canvas.drawText("SÀI GÒN", mcx, mcy - 10f, markFillPaint)
        canvas.drawText("22-05-2026", mcx, mcy + 22f, markFillPaint)

        // Wave cancellation lines across stamp
        markPaint.strokeWidth = 1.5f
        canvas.drawLine(mcx - 220f, mcy - 40f, mcx + 220f, mcy - 50f, markPaint)
        canvas.drawLine(mcx - 230f, mcy, mcx + 220f, mcy - 10f, markPaint)
        canvas.drawLine(mcx - 220f, mcy + 40f, mcx + 210f, mcy + 30f, markPaint)

        return output
    }

    private fun cropSourceToShape(source: Bitmap, shape: String): Bitmap {
        val w = source.width
        val h = source.height

        val (targetW, targetH) = when (shape) {
            "SQUARE", "CIRCLE" -> {
                val size = minOf(w, h)
                Pair(size, size)
            }
            else -> { // "RECTANGLE" (3:4 aspect ratio, i.e. Portrait stamp)
                if (w * 4 > h * 3) {
                    val targetW = (h * 3) / 4
                    Pair(targetW, h)
                } else {
                    val targetH = (w * 4) / 3
                    Pair(w, targetH)
                }
            }
        }

        val startX = (w - targetW) / 2
        val startY = (h - targetH) / 2
        
        return Bitmap.createBitmap(source, startX, startY, targetW, targetH)
    }

    private fun createCircularBitmap(src: Bitmap): Bitmap {
        val size = minOf(src.width, src.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawARGB(0, 0, 0, 0)
        
        paint.color = Color.BLACK
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val rect = Rect(0, 0, size, size)
        canvas.drawBitmap(src, rect, rect, paint)
        
        return output
    }
}
