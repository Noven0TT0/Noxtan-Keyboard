package com.noxtan.noxboard.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {
    fun compressAndSaveImage(context: Context, inputUri: Uri, fileName: String): String? {
        return try {
            val resolver = context.contentResolver

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(inputUri)?.use { BitmapFactory.decodeStream(it, null, options) }

            val reqWidth = 1080
            val reqHeight = 1920

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            var bitmap = resolver.openInputStream(inputUri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null

            val ratio = Math.min(reqWidth.toFloat() / bitmap.width, reqHeight.toFloat() / bitmap.height)
            if (ratio < 1f) {
                val scaledBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).toInt(),
                    (bitmap.height * ratio).toInt(),
                    true
                )
                if (scaledBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = scaledBitmap
                }
            }

            val wallpaperDir = File(context.filesDir, "theme_wallpapers")
            if (!wallpaperDir.exists()) wallpaperDir.mkdirs()

            val outputFile = File(wallpaperDir, fileName)
            FileOutputStream(outputFile).use { fos ->
                val compressFormat = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }

                bitmap.compress(compressFormat, 70, fos)
            }

            bitmap.recycle()

            Uri.fromFile(outputFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            com.noxtan.noxboard.utils.NoxLogger.logError("ImageCompressor", "Failed to compress/save image", e)
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}