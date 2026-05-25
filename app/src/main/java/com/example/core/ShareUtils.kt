package com.example.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ShareUtils {
    fun shareStampImage(context: Context, file: File, label: String) {
        try {
            val authority = "com.aistudio.stampcollector.pqmnty.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Stampify Collection: $label")
                putExtra(Intent.EXTRA_TEXT, "Xem con tem tuyệt đẹp tôi tự tạo và sưu tầm bằng app Stampify: \"$label\"!")
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ con tem bưu chính"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
