package com.noxtan.noxboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.KeyboardMode
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.R
import com.noxtan.noxboard.ui.components.NoxTopBar
import com.noxtan.noxboard.ui.utils.topAndBottomNoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardLayoutSelectionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.tab_myanmar), stringResource(R.string.tab_english), stringResource(R.string.tab_symbols))

    var myanmarStyle by remember { mutableStateOf(if (prefs.activeLayoutMyanmar.isNotEmpty()) prefs.activeLayoutMyanmar else "MODERN") }
    var englishStyle by remember { mutableStateOf(if (prefs.activeLayoutEnglish.isNotEmpty()) prefs.activeLayoutEnglish else "MODERN") }
    var symbolStyle by remember { mutableStateOf(if (prefs.activeLayoutSymbols.isNotEmpty()) prefs.activeLayoutSymbols else "MODERN") }

    val currentSelectedStyle = when (selectedTabIndex) {
        0 -> myanmarStyle
        1 -> englishStyle
        else -> symbolStyle
    }

    val onStyleSelected: (String) -> Unit = { style ->
        when (selectedTabIndex) {
            0 -> myanmarStyle = style
            1 -> englishStyle = style
            else -> symbolStyle = style
        }
    }

    Scaffold(
        topBar = {
            NoxTopBar(
                title = "Keyboard Layout",
                scrollState = scrollState,
                onBackClick = onBack,
                actions = {
                    Button(
                        onClick = {
                            prefs.activeLayoutMyanmar = myanmarStyle
                            prefs.activeLayoutEnglish = englishStyle
                            prefs.activeLayoutSymbols = symbolStyle
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(stringResource(R.string.save), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 600.dp)
                .padding(top = innerPadding.calculateTopPadding())
                .topAndBottomNoise()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        com.noxtan.noxboard.NoxKeyboardView(ctx).apply {
                            isEditMode = true
                        }
                    },
                    update = { view ->
                        view.currentMode = when (selectedTabIndex) {
                            0 -> KeyboardMode.MYANMAR
                            1 -> KeyboardMode.ENGLISH
                            else -> KeyboardMode.SYMBOLS_1
                        }
                        view.previewLayoutId = currentSelectedStyle
                        view.applySettings(prefs)
                    },
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Layout Style",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )

                LayoutOptionCard(
                    title = "Modern Style",
                    description = "Standard spaced layout, comfortable for most users.",
                    isSelected = currentSelectedStyle == "MODERN",
                    onClick = { onStyleSelected("MODERN") }
                )

                LayoutOptionCard(
                    title = "Classic Style",
                    description = "Traditional style layout with specific key placements.",
                    isSelected = currentSelectedStyle == "CLASSIC",
                    onClick = { onStyleSelected("CLASSIC") }
                )

                LayoutOptionCard(
                    title = "System Style",
                    description = "Compact layout similar to default device keyboards.",
                    isSelected = currentSelectedStyle == "SYSTEM",
                    onClick = { onStyleSelected("SYSTEM") }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun LayoutOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .border(
                        2.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        androidx.compose.foundation.shape.CircleShape
                    )
                    .padding(4.dp)
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}