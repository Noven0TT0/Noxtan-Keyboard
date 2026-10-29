package com.noxtan.noxboard.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.ui.components.CategoryPill
import com.noxtan.noxboard.ui.components.NoxTopBar
import com.noxtan.noxboard.ui.components.UnsavedChangesDialog

@Composable
fun KeyboardSizePositionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }
    val scrollState = rememberScrollState()

    val initialHeight = remember { prefs.keyboardHeight }
    val initialWidth = remember { prefs.keyboardWidth }
    val initialBottomPadding = remember { prefs.bottomPadding }
    val initialAlignment = remember { prefs.keyboardAlignment }
    val initialKeyPadding = remember { prefs.keyPadding }
    val initialCornerRadius = remember { prefs.keyCornerRadius }
    val initialFontSize = remember { prefs.keyFontSize }

    var height by remember { mutableFloatStateOf(initialHeight) }
    var width by remember { mutableFloatStateOf(initialWidth) }
    var bottomPadding by remember { mutableFloatStateOf(initialBottomPadding) }
    var alignment by remember { mutableStateOf(initialAlignment) }
    var keyPadding by remember { mutableFloatStateOf(initialKeyPadding) }
    var cornerRadius by remember { mutableFloatStateOf(initialCornerRadius) }
    var fontSize by remember { mutableFloatStateOf(initialFontSize) }

    var showExitDialog by remember { mutableStateOf(false) }

    val hasChanges = height != initialHeight || width != initialWidth ||
            bottomPadding != initialBottomPadding || alignment != initialAlignment ||
            keyPadding != initialKeyPadding || cornerRadius != initialCornerRadius ||
            fontSize != initialFontSize

    BackHandler(enabled = true) {
        if (hasChanges) showExitDialog = true else onBack()
    }

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Scaffold(
        topBar = {
            NoxTopBar(
                title = "Size & Position",
                scrollState = scrollState,
                onBackClick = { if (hasChanges) showExitDialog = true else onBack() },
                actions = {
                    TextButton(onClick = {
                        height = 235f; width = 100f; bottomPadding = 54f
                        alignment = "CENTER"; keyPadding = 3f; cornerRadius = 5f
                        fontSize = 16f
                    }) {
                        Text("Reset", fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            prefs.keyboardHeight = height
                            prefs.keyboardWidth = width
                            prefs.bottomPadding = bottomPadding
                            prefs.keyboardAlignment = alignment
                            prefs.keyPadding = keyPadding
                            prefs.keyCornerRadius = cornerRadius
                            prefs.keyFontSize = fontSize
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Keyboard Size", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                SliderControl("Height", height, 180f..350f) { height = it }
                SliderControl("Width (%)", width, 50f..100f) { width = it }
                SliderControl("Bottom Padding", bottomPadding, 0f..100f) { bottomPadding = it }

                AnimatedVisibility(visible = width < 100f) {
                    Column {
                        Text("Alignment", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryPill("Left", alignment == "LEFT") { alignment = "LEFT" }
                            CategoryPill("Center", alignment == "CENTER") { alignment = "CENTER" }
                            CategoryPill("Right", alignment == "RIGHT") { alignment = "RIGHT" }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Text("Key Appearance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                SliderControl("Key Spacing", keyPadding, 0f..10f) { keyPadding = it }
                SliderControl("Corner Radius", cornerRadius, 0f..20f) { cornerRadius = it }
                SliderControl("Font Size", fontSize, 10f..30f) { fontSize = it }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                AndroidView(
                    factory = { ctx ->
                        com.noxtan.noxboard.NoxKeyboardView(ctx).apply {
                            isEditMode = false
                            applySettings(prefs)
                        }
                    },
                    update = { view ->
                        view.theme.keyboardHeightDp = height
                        view.theme.keyboardWidthPercent = width
                        view.theme.bottomPaddingDp = bottomPadding
                        view.theme.keyboardAlignment = alignment
                        view.theme.keyPaddingDp = keyPadding
                        view.theme.keyCornerRadiusDp = cornerRadius
                        view.theme.textSizeSp = fontSize

                        view.cachedEffectiveTextSize = -1f

                        view.initPaints()

                        view.isEmojiMode = view.isEmojiMode
                        view.invalidate()
                    }
                )
            }
        }

        UnsavedChangesDialog(
            showDialog = showExitDialog,
            onDismiss = { showExitDialog = false },
            onConfirm = {
                showExitDialog = false
                onBack()
            }
        )
    }
}

@Composable
fun SliderControl(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("${value.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}