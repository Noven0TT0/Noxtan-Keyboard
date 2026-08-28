package com.noxtan.noxboard.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noxtan.noxboard.ui.components.NoxBottomBar
import com.noxtan.noxboard.ui.screens.tabs.DesignTab
import com.noxtan.noxboard.ui.screens.tabs.HomeTab
import com.noxtan.noxboard.ui.screens.tabs.LayoutTab
import com.noxtan.noxboard.ui.screens.tabs.SettingsTab
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.R
import com.noxtan.noxboard.ui.screens.tabs.LanguageTab

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KeyboardSettingsScreen(
    onNavigateToTheme: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSizePosition: () -> Unit,
    selectedBottomTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    activeAppearanceTab: Int = 0,
    onAppearanceTabSelected: (Int) -> Unit = {},
    designTabScrollState: androidx.compose.foundation.ScrollState = androidx.compose.foundation.rememberScrollState(),
    modifier: Modifier = Modifier
) {

    val isKeyboardVisible = WindowInsets.isImeVisible
    val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                TopAppBar(
                    title = {
                        if (selectedBottomTab == 0) {
                            Column {
                                Text(
                                    text = "Nox Board",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Version 1.0",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = when (selectedBottomTab) {
                                    1 -> "Appearance"
                                    2 -> "Languages"
                                    else -> "Settings"
                                },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    navigationIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_nox_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .padding(start = 16.dp, end = 12.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                    },
                    actions = {
                        if (selectedBottomTab == 0) {
                            IconButton(onClick = onNavigateToAbout) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "About App",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = if (isKeyboardVisible) 0.dp else (80.dp + navBarBottomInset))
            ) {
                when (selectedBottomTab) {
                    0 -> HomeTab()
                    1 -> DesignTab(
                        onNavigateToTheme = onNavigateToTheme,
                        onNavigateToSizePosition = onNavigateToSizePosition,
                        activeAppearanceTab = activeAppearanceTab,
                        onAppearanceTabSelected = onAppearanceTabSelected,
                        scrollState = designTabScrollState
                    )
                    2 -> LanguageTab()
                    3 -> SettingsTab()
                }
            }
        }

        if (!isKeyboardVisible) {
            NoxBottomBar(
                selectedTab = selectedBottomTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}