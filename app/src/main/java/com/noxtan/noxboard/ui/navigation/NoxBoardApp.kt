package com.noxtan.noxboard.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import com.noxtan.noxboard.KeyboardSetupHelper
import com.noxtan.noxboard.ui.screens.KeyboardSettingsScreen
import com.noxtan.noxboard.ui.screens.KeyboardSetupScreen

enum class AppScreen { SETUP, SETTINGS, THEME_EDITOR, ABOUT, SIZE_POSITION, OPEN_SOURCE }

@Composable
fun NoxBoardApp() {
    val context = LocalContext.current

    var currentScreen by androidx.compose.runtime.saveable.rememberSaveable {
        mutableStateOf(
            if (KeyboardSetupHelper.isKeyboardEnabled(context) && KeyboardSetupHelper.isKeyboardSelected(context)) {
                AppScreen.SETTINGS
            } else {
                AppScreen.SETUP
            }
        )
    }

    var selectedBottomTab by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(0) }
    var activeAppearanceTab by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(0) }

    val designTabScrollState = rememberScrollState()
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
                        onNavigateToTheme = { currentScreen = AppScreen.THEME_EDITOR },
                        onNavigateToAbout = { currentScreen = AppScreen.ABOUT },
                        onNavigateToSizePosition = { currentScreen = AppScreen.SIZE_POSITION },
                        selectedBottomTab = selectedBottomTab,
                        onTabSelected = { selectedBottomTab = it },
                        activeAppearanceTab = activeAppearanceTab,
                        onAppearanceTabSelected = { activeAppearanceTab = it },
                        designTabScrollState = designTabScrollState
                    )
                }
                AppScreen.THEME_EDITOR -> {
                    com.noxtan.noxboard.ui.screens.ThemeEditorScreen(
                        onBack = { currentScreen = AppScreen.SETTINGS }
                    )
                }
                AppScreen.ABOUT -> {
                    com.noxtan.noxboard.ui.screens.AboutScreen(
                        onBack = { currentScreen = AppScreen.SETTINGS },
                        onNavigateToOpenSource = { currentScreen = AppScreen.OPEN_SOURCE }
                    )
                }
                AppScreen.OPEN_SOURCE -> {
                    com.noxtan.noxboard.ui.screens.OpenSourceLicensesScreen(
                        onBack = { currentScreen = AppScreen.ABOUT }
                    )
                }
                AppScreen.SIZE_POSITION -> {
                    com.noxtan.noxboard.ui.screens.KeyboardSizePositionScreen(
                        onBack = { currentScreen = AppScreen.SETTINGS }
                    )
                }
            }
        }
    }
}