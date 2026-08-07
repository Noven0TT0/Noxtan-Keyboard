package com.noxtan.noxboard.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import com.noxtan.noxboard.KeyboardSetupHelper
import com.noxtan.noxboard.ui.screens.KeyboardResizeScreen
import com.noxtan.noxboard.ui.screens.KeyboardSettingsScreen
import com.noxtan.noxboard.ui.screens.KeyboardSetupScreen
import com.noxtan.noxboard.ui.screens.KeyboardSoundSettingsScreen
import com.noxtan.noxboard.ui.screens.KeyboardThemeScreen
import com.noxtan.noxboard.ui.screens.KeyboardCustomThemeScreen
import com.noxtan.noxboard.ui.screens.LanguageSelectionScreen
import com.noxtan.noxboard.ui.screens.SpacebarControlScreen
import com.noxtan.noxboard.ui.screens.KeyboardRearrangeScreen

enum class AppScreen { SETUP, SETTINGS, RESIZE, SOUND_SETTINGS, THEME_SELECTION, THEME_CUSTOM_CREATE, REARRANGE_KEYS, LANGUAGE_SELECTION, SPACEBAR_CONTROL, DOUBLE_TAP_SETTINGS, ABOUT, LOG_VIEWER, OPEN_SOURCE }

@Composable
fun NoxBoardApp() {
    val context = LocalContext.current

    var currentScreen by remember {
        mutableStateOf(
            if (KeyboardSetupHelper.isKeyboardEnabled(context) && KeyboardSetupHelper.isKeyboardSelected(context)) {
                AppScreen.SETTINGS
            } else {
                AppScreen.SETUP
            }
        )
    }

    val settingsScrollState = rememberScrollState()

    val windowInfo = LocalWindowInfo.current
    LaunchedEffect(windowInfo.isWindowFocused) {
        if (windowInfo.isWindowFocused) {
            val isSetupComplete = KeyboardSetupHelper.isKeyboardEnabled(context) &&
                    KeyboardSetupHelper.isKeyboardSelected(context)
            if (isSetupComplete && currentScreen == AppScreen.SETUP) {
                currentScreen = AppScreen.SETTINGS
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = currentScreen,
            animationSpec = tween(500),
            label = "AppFlowTransition"
        ) { screen ->
            when (screen) {
                AppScreen.SETUP -> {
                    KeyboardSetupScreen(onSetupComplete = { currentScreen = AppScreen.SETTINGS })
                }
                AppScreen.SETTINGS -> {
                    KeyboardSettingsScreen(
                        scrollState = settingsScrollState,
                        onNavigateToResize = { currentScreen = AppScreen.RESIZE },
                        onNavigateToSoundSettings = { currentScreen = AppScreen.SOUND_SETTINGS },
                        onNavigateToTheme = { currentScreen = AppScreen.THEME_SELECTION },
                        onNavigateToRearrangeKeys = { currentScreen = AppScreen.REARRANGE_KEYS },
                        onNavigateToLanguageSelection = { currentScreen = AppScreen.LANGUAGE_SELECTION },
                        onNavigateToSpacebarControl = { currentScreen = AppScreen.SPACEBAR_CONTROL },
                        onNavigateToDoubleTapSettings = { currentScreen = AppScreen.DOUBLE_TAP_SETTINGS },
                        onNavigateToAbout = { currentScreen = AppScreen.ABOUT }
                    )
                }

                AppScreen.DOUBLE_TAP_SETTINGS -> {
                    com.noxtan.noxboard.ui.screens.DoubleTapSettingsScreen(onBack = { currentScreen = AppScreen.SETTINGS })
                }
                AppScreen.SPACEBAR_CONTROL -> {
                    SpacebarControlScreen(onBack = { currentScreen = AppScreen.SETTINGS })
                }
                AppScreen.RESIZE -> {
                    KeyboardResizeScreen(onBack = { currentScreen = AppScreen.SETTINGS })
                }
                AppScreen.SOUND_SETTINGS -> {
                    KeyboardSoundSettingsScreen(onBack = { currentScreen = AppScreen.SETTINGS })
                }
                AppScreen.THEME_SELECTION -> {
                    KeyboardThemeScreen(
                        onBack = { currentScreen = AppScreen.SETTINGS },
                        onCreateCustomTheme = { currentScreen = AppScreen.THEME_CUSTOM_CREATE }
                    )
                }
                AppScreen.THEME_CUSTOM_CREATE -> {
                    KeyboardCustomThemeScreen(onBack = { currentScreen = AppScreen.THEME_SELECTION })
                }
                AppScreen.REARRANGE_KEYS -> {
                    KeyboardRearrangeScreen(onBack = { currentScreen = AppScreen.SETTINGS })
                }
                AppScreen.LANGUAGE_SELECTION -> {
                    LanguageSelectionScreen(onBack = { currentScreen = AppScreen.SETTINGS })
                }
                AppScreen.ABOUT -> {
                    com.noxtan.noxboard.ui.screens.AboutScreen(
                        onBack = { currentScreen = AppScreen.SETTINGS },
                        onNavigateToLogs = { currentScreen = AppScreen.LOG_VIEWER },
                        onNavigateToOpenSource = { currentScreen = AppScreen.OPEN_SOURCE }
                    )
                }
                AppScreen.OPEN_SOURCE -> {
                    com.noxtan.noxboard.ui.screens.OpenSourceScreen(
                        onBack = { currentScreen = AppScreen.ABOUT }
                    )
                }
                AppScreen.LOG_VIEWER -> {
                    com.noxtan.noxboard.ui.screens.LogViewerScreen(
                        onBack = { currentScreen = AppScreen.ABOUT }
                    )
                }
            }
        }
    }
}