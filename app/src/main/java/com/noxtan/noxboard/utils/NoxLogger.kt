package com.noxtan.noxboard.utils

import android.content.Context
import android.os.Build
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NoxLogger {
    private var logFile: File? = null
    private const val MAX_FILE_SIZE = 1024 * 1024

    fun init(context: Context) {
        logFile = File(context.filesDir, "nox_system_logs.txt")

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logCrash(context, thread, throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = buildString {
            append("[$time] ERROR [$tag]: $message\n")
            throwable?.let {
                append(it.stackTraceToString())
                append("\n")
            }
            append("--------------------------------------------------\n")
        }
        writeToFile(logMessage)
    }

    private fun logCrash(context: Context, thread: Thread, throwable: Throwable) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val appVersion = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "Unknown"
        } catch (e: Exception) { "Unknown" }

        val crashReport = buildString {
            append("\n================ FATAL CRASH ================\n")
            append("Time: $time\n")
            append("App Version: $appVersion\n")
            append("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
            append("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
            append("Thread: ${thread.name}\n")
            append("Exception: ${throwable.message}\n\n")
            append(throwable.stackTraceToString())
            append("\n=============================================\n\n")
        }
        writeToFile(crashReport)
    }

    private fun writeToFile(message: String) {
        try {
            val file = logFile ?: return

            if (file.exists() && file.length() > MAX_FILE_SIZE) {
                file.delete()
            }

            val writer = FileWriter(file, true)
            writer.append(message)
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readLogs(): String {
        return try {
            val file = logFile ?: return "Log file not initialized."
            if (file.exists()) {
                val content = file.readText()
                if (content.isBlank()) "No logs found. System is running smoothly." else content
            } else {
                "No logs found. System is running smoothly."
            }
        } catch (e: Exception) {
            "Error reading logs: ${e.message}"
        }
    }

    fun clearLogs() {
        try {
            logFile?.let {
                if (it.exists()) {
                    it.writeText("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}