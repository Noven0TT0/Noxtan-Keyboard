package com.noxtan.noxboard

import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.noxtan.noxboard.ui.navigation.NoxBoardApp
import com.noxtan.noxboard.ui.theme.NoxBoardTheme
import android.content.pm.ActivityInfo

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        com.noxtan.noxboard.utils.NoxLogger.init(this)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val isDarkMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES

        enableEdgeToEdge(
            statusBarStyle = if (isDarkMode) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = if (isDarkMode) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )

        setContent {
            NoxBoardTheme {
                NoxBoardApp()
            }
        }
    }
}