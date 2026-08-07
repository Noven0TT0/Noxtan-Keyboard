package com.noxtan.noxboard.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ThemePacker {

    fun exportTheme(
        context: Context,
        themeJson: String,
        wallpaperUriString: String?,
        themeName: String
    ): File? {
        return try {
            val safeName = themeName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val exportFile = File(context.cacheDir, "${safeName}.noxtheme")

            val fos = FileOutputStream(exportFile)
            val zos = ZipOutputStream(fos)

            val jsonEntry = ZipEntry("config.json")
            zos.putNextEntry(jsonEntry)
            zos.write(themeJson.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            if (!wallpaperUriString.isNullOrEmpty()) {
                val uri = Uri.parse(wallpaperUriString)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val imageEntry = ZipEntry("background.jpg")
                    zos.putNextEntry(imageEntry)
                    inputStream.copyTo(zos)
                    zos.closeEntry()
                }
            }

            zos.close()
            fos.close()

            exportFile
        } catch (e: Exception) {
            e.printStackTrace()
            com.noxtan.noxboard.utils.NoxLogger.logError("ThemePacker", "Failed to export theme", e)
            null
        }
    }
    data class ImportedTheme(
        val themeJson: String,
        val wallpaperPath: String?
    )

    fun importTheme(context: Context, uri: android.net.Uri): ImportedTheme? {
        return try {
            var jsonContent: String? = null
            var wallpaperPath: String? = null

            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val zis = java.util.zip.ZipInputStream(inputStream)

            val MAX_JSON_SIZE = 1 * 1024 * 1024
            val MAX_IMAGE_SIZE = 10 * 1024 * 1024

            var entry = zis.nextEntry
            while (entry != null) {
                when (entry.name) {
                    "config.json" -> {
                        val buffer = java.io.ByteArrayOutputStream()
                        val data = ByteArray(1024)
                        var totalRead = 0
                        var count: Int
                        while (zis.read(data, 0, 1024).also { count = it } != -1) {
                            totalRead += count
                            if (totalRead > MAX_JSON_SIZE) throw SecurityException("JSON file is too large (Zip Bomb attack)")
                            buffer.write(data, 0, count)
                        }
                        jsonContent = buffer.toString(Charsets.UTF_8.name())
                    }
                    "background.jpg", "background.png", "background.webp" -> {
                        val wallpaperDir = java.io.File(context.filesDir, "theme_wallpapers")
                        if (!wallpaperDir.exists()) wallpaperDir.mkdirs()

                        val wallpaperFile = java.io.File(wallpaperDir, "wall_${System.currentTimeMillis()}.jpg")
                        java.io.FileOutputStream(wallpaperFile).use { fos ->
                            val data = ByteArray(1024)
                            var totalRead = 0
                            var count: Int
                            while (zis.read(data, 0, 1024).also { count = it } != -1) {
                                totalRead += count
                                if (totalRead > MAX_IMAGE_SIZE) throw SecurityException("Image file is too large (Zip Bomb attack)")
                                fos.write(data, 0, count)
                            }
                        }
                        wallpaperPath = android.net.Uri.fromFile(wallpaperFile).toString()
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
            zis.close()

            if (jsonContent != null) {
                ImportedTheme(jsonContent, wallpaperPath)
            } else {
                null
            }
        } catch (e: SecurityException) {
            android.util.Log.e("ThemePacker", "Security Blocked: ${e.message}")
            null
        } catch (e: Exception) {
            e.printStackTrace()
            com.noxtan.noxboard.utils.NoxLogger.logError("ThemePacker", "Failed to import theme", e)
            null
        }
    }

}