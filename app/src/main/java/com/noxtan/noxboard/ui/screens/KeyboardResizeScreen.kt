package com.noxtan.noxboard.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.ui.components.KeyboardPreviewArea
import com.noxtan.noxboard.ui.components.UnsavedChangesDialog
import com.noxtan.noxboard.ui.screens.resize.ResizeSettingsCards
import com.noxtan.noxboard.ui.utils.topAndBottomNoise
import com.noxtan.noxboard.R
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun KeyboardResizeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }

    var initialWidth by remember { mutableStateOf(prefs.keyboardWidth) }
    var initialAlignment by remember { mutableStateOf(prefs.keyboardAlignment) }
    var initialHeight by remember { mutableStateOf(prefs.keyboardHeight) }
    var initialBottomPadding by remember { mutableStateOf(prefs.bottomPadding) }
    var initialCornerRadius by remember { mutableStateOf(prefs.keyCornerRadius) }
    var initialPadding by remember { mutableStateOf(prefs.keyPadding) }
    var initialFontSize by remember { mutableStateOf(prefs.keyFontSize) }
    var initialGlobalRowGap by remember { mutableStateOf(prefs.globalRowGap) }
    var initialIndividualRowGaps by remember { mutableStateOf(prefs.individualRowGaps) }

    var initialActivePresetId by remember { mutableStateOf(prefs.activeSizePreset) }
    var initialSavedSizePresets by remember { mutableStateOf(prefs.savedSizePresets) }

    var keyboardWidth by remember { mutableStateOf(prefs.keyboardWidth) }
    var keyboardAlignment by remember { mutableStateOf(prefs.keyboardAlignment) }
    var keyboardHeight by remember { mutableStateOf(prefs.keyboardHeight) }
    var bottomPadding by remember { mutableStateOf(prefs.bottomPadding) }
    var keyCornerRadius by remember { mutableStateOf(prefs.keyCornerRadius) }
    var keyPadding by remember { mutableStateOf(prefs.keyPadding) }
    var keyFontSize by remember { mutableStateOf(prefs.keyFontSize) }
    var globalRowGap by remember { mutableStateOf(prefs.globalRowGap) }
    var individualRowGaps by remember { mutableStateOf(prefs.individualRowGaps) }
    var activePresetId by remember { mutableStateOf(prefs.activeSizePreset) }
    var presetToDelete by remember { mutableStateOf<String?>(null) }
    var sizePresetsList by remember {
        mutableStateOf(
            org.json.JSONArray(prefs.savedSizePresets).let { arr ->
                val list = mutableListOf<org.json.JSONObject>()
                for (i in 0 until arr.length()) list.add(arr.getJSONObject(i))
                list
            }
        )
    }

    var baselineWidth by remember { mutableStateOf(100f) }
    var baselineAlignment by remember { mutableStateOf("CENTER") }
    var baselineHeight by remember { mutableStateOf(235f) }
    var baselineBottomPadding by remember { mutableStateOf(54f) }
    var baselineCornerRadius by remember { mutableStateOf(10f) }
    var baselinePadding by remember { mutableStateOf(1f) }
    var baselineFontSize by remember { mutableStateOf(16f) }
    var baselineGlobalRowGap by remember { mutableStateOf(0f) }
    var baselineIndividualRowGaps by remember { mutableStateOf("{}") }

    LaunchedEffect(activePresetId, sizePresetsList) {
        if (activePresetId == "default") {
            baselineWidth = 100f
            baselineAlignment = "CENTER"
            baselineHeight = 235f
            baselineBottomPadding = 54f
            baselineCornerRadius = 10f
            baselinePadding = 1f
            baselineFontSize = 16f
            baselineGlobalRowGap = 0f
            baselineIndividualRowGaps = "{}"
        } else {
            val preset = sizePresetsList.find { it.optString("id") == activePresetId }
            if (preset != null) {
                baselineWidth = preset.optDouble("orig_width", 100.0).toFloat()
                baselineAlignment = preset.optString("orig_alignment", "CENTER")
                baselineHeight = preset.optDouble("orig_height", 235.0).toFloat()
                baselineBottomPadding = preset.optDouble("orig_bottomPadding", 54.0).toFloat()
                baselineCornerRadius = preset.optDouble("orig_keyCornerRadius", 10.0).toFloat()
                baselinePadding = preset.optDouble("orig_keyPadding", 1.0).toFloat()
                baselineFontSize = preset.optDouble("orig_keyFontSize", 16.0).toFloat()
                baselineGlobalRowGap = preset.optDouble("orig_globalRowGap", 0.0).toFloat()
                baselineIndividualRowGaps = preset.optString("orig_individualRowGaps", "{}")
            }
        }
    }

    val selectPreset = { selectedId: String ->
        if (activePresetId != "default") {
            sizePresetsList = sizePresetsList.map { item ->
                if (item.optString("id") == activePresetId) {
                    item.put("width", keyboardWidth.toDouble())
                    item.put("alignment", keyboardAlignment)
                    item.put("height", keyboardHeight.toDouble())
                    item.put("bottomPadding", bottomPadding.toDouble())
                    item.put("keyCornerRadius", keyCornerRadius.toDouble())
                    item.put("keyPadding", keyPadding.toDouble())
                    item.put("keyFontSize", keyFontSize.toDouble())
                    item.put("globalRowGap", globalRowGap.toDouble())
                    item.put("individualRowGaps", individualRowGaps)
                }
                item
            }.toMutableList()
        }

        activePresetId = selectedId
        prefs.activeSizePreset = selectedId

        val newWidth = if (selectedId == "default") 100f else {
            val preset = sizePresetsList.find { it.optString("id") == selectedId }
            preset?.optDouble("width", 100.0)?.toFloat() ?: 100f
        }
        val newAlignment = if (selectedId == "default") "CENTER" else {
            val preset = sizePresetsList.find { it.optString("id") == selectedId }
            preset?.optString("alignment", "CENTER") ?: "CENTER"
        }
        val newHeight = if (selectedId == "default") 235f else {
            val preset = sizePresetsList.find { it.optString("id") == selectedId }
            preset?.optDouble("height", 235.0)?.toFloat() ?: 235f
        }
        val newBottomPadding = if (selectedId == "default") 54f else {
            val preset = sizePresetsList.find { it.optString("id") == selectedId }
            preset?.optDouble("bottomPadding", 54.0)?.toFloat() ?: 54f
        }
        val newCornerRadius = if (selectedId == "default") 10f else {
            val preset = sizePresetsList.find { it.optString("id") == selectedId }
            preset?.optDouble("keyCornerRadius", 10.0)?.toFloat() ?: 10f
        }
        val newPadding = if (selectedId == "default") 1f else {
            val preset = sizePresetsList.find { it.optString("id") == selectedId }
            preset?.optDouble("keyPadding", 1.0)?.toFloat() ?: 1f
        }
        val newFontSize = if (selectedId == "default") 16f else {
            val preset = sizePresetsList.find { it.optString("id") == selectedId }
            preset?.optDouble("keyFontSize", 16.0)?.toFloat() ?: 16f
        }
        val newGlobalRowGap = if (selectedId == "default") 0f else {
            val preset = sizePresetsList.find { it.optString("id") == selectedId }
            preset?.optDouble("globalRowGap", 0.0)?.toFloat() ?: 0f
        }
        val newIndividualRowGaps = if (selectedId == "default") "{}" else {
            val preset = sizePresetsList.find { it.optString("id") == selectedId }
            preset?.optString("individualRowGaps", "{}") ?: "{}"
        }

        keyboardWidth = newWidth
        keyboardAlignment = newAlignment
        keyboardHeight = newHeight
        bottomPadding = newBottomPadding
        keyCornerRadius = newCornerRadius
        keyPadding = newPadding
        keyFontSize = newFontSize
        globalRowGap = newGlobalRowGap
        individualRowGaps = newIndividualRowGaps

        prefs.keyboardWidth = newWidth
        prefs.keyboardAlignment = newAlignment
        prefs.keyboardHeight = newHeight
        prefs.bottomPadding = newBottomPadding
        prefs.keyCornerRadius = newCornerRadius
        prefs.keyPadding = newPadding
        prefs.keyFontSize = newFontSize
        prefs.globalRowGap = newGlobalRowGap
        prefs.individualRowGaps = newIndividualRowGaps
    }

    var activeSlider by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val importLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonStr = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: ""

                val json = org.json.JSONObject(jsonStr)

                val importedWidth = json.optDouble("width", 100.0).toFloat()
                val importedAlignment = json.optString("alignment", "CENTER")
                val importedHeight = json.optDouble("height", 235.0).toFloat()
                val importedBottomPadding = json.optDouble("bottomPadding", 54.0).toFloat()
                val importedCornerRadius = json.optDouble("keyCornerRadius", 10.0).toFloat()
                val importedPadding = json.optDouble("keyPadding", 1.0).toFloat()
                val importedFontSize = json.optDouble("keyFontSize", 16.0).toFloat()
                val importedGlobalGap = json.optDouble("globalRowGap", 0.0).toFloat()
                val importedIndGaps = json.optString("individualRowGaps", "{}")

                val newId = (sizePresetsList.size + 1).toString()
                val newPreset = org.json.JSONObject().apply {
                    put("id", newId)
                    put("name", "Config $newId")
                    put("width", importedWidth.toDouble())
                    put("alignment", importedAlignment)
                    put("height", importedHeight.toDouble())
                    put("bottomPadding", importedBottomPadding.toDouble())
                    put("keyCornerRadius", importedCornerRadius.toDouble())
                    put("keyPadding", importedPadding.toDouble())
                    put("keyFontSize", importedFontSize.toDouble())
                    put("globalRowGap", importedGlobalGap.toDouble())
                    put("individualRowGaps", importedIndGaps)
                    put("orig_width", importedWidth.toDouble())
                    put("orig_alignment", importedAlignment)
                    put("orig_height", importedHeight.toDouble())
                    put("orig_bottomPadding", importedBottomPadding.toDouble())
                    put("orig_keyCornerRadius", importedCornerRadius.toDouble())
                    put("orig_keyPadding", importedPadding.toDouble())
                    put("orig_keyFontSize", importedFontSize.toDouble())
                    put("orig_globalRowGap", importedGlobalGap.toDouble())
                    put("orig_individualRowGaps", importedIndGaps)
                }

                val newList = sizePresetsList.toMutableList()
                newList.add(newPreset)
                sizePresetsList = newList

                val arr = org.json.JSONArray()
                newList.forEach { arr.put(it) }
                prefs.savedSizePresets = arr.toString()

                activePresetId = newId
                prefs.activeSizePreset = newId

                keyboardWidth = importedWidth
                keyboardAlignment = importedAlignment
                keyboardHeight = importedHeight
                bottomPadding = importedBottomPadding
                keyCornerRadius = importedCornerRadius
                keyPadding = importedPadding
                keyFontSize = importedFontSize
                globalRowGap = importedGlobalGap
                individualRowGaps = importedIndGaps

                prefs.keyboardWidth = importedWidth
                prefs.keyboardAlignment = importedAlignment
                prefs.keyboardHeight = importedHeight
                prefs.bottomPadding = importedBottomPadding
                prefs.keyCornerRadius = importedCornerRadius
                prefs.keyPadding = importedPadding
                prefs.keyFontSize = importedFontSize
                prefs.globalRowGap = importedGlobalGap
                prefs.individualRowGaps = importedIndGaps

                android.widget.Toast.makeText(context, "Size settings imported to Slot $newId!", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Import failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val exportSizeConfig = {
        try {
            val json = org.json.JSONObject().apply {
                put("width", keyboardWidth)
                put("alignment", keyboardAlignment)
                put("height", keyboardHeight)
                put("bottomPadding", bottomPadding)
                put("keyCornerRadius", keyCornerRadius)
                put("keyPadding", keyPadding)
                put("keyFontSize", keyFontSize)
                put("globalRowGap", globalRowGap)
                put("individualRowGaps", individualRowGaps)
            }.toString()

            val file = java.io.File(context.cacheDir, "nox_size_config_${System.currentTimeMillis()}.noxsize")
            file.writeText(json, Charsets.UTF_8)

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Keyboard Size via"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Export failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isImeVisible = WindowInsets.isImeVisible

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val animatedWidth by animateDpAsState(
        targetValue = if (isImeVisible) (screenWidth - 32.dp) else 160.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
    )

    val animatedHeight by animateDpAsState(
        targetValue = if (isImeVisible) 56.dp else 48.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
    )

    val animatedCornerRadius by animateDpAsState(
        targetValue = if (isImeVisible) 12.dp else 24.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
    )

    val animatedBgColor by animateColorAsState(
        targetValue = if (isImeVisible) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 300)
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isImeVisible) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 300)
    )

    val isSlidingActive = activeSlider != null

    val hasChanges = keyboardWidth != initialWidth ||
            keyboardAlignment != initialAlignment ||
            keyboardHeight != initialHeight ||
            bottomPadding != initialBottomPadding ||
            keyCornerRadius != initialCornerRadius ||
            keyPadding != initialPadding ||
            keyFontSize != initialFontSize ||
            globalRowGap != initialGlobalRowGap ||
            individualRowGaps != initialIndividualRowGaps ||
            activePresetId != initialActivePresetId ||
            prefs.savedSizePresets != initialSavedSizePresets

    BackHandler(enabled = true) {
        if (hasChanges) {
            showExitDialog = true
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            com.noxtan.noxboard.ui.components.NoxTopBar(
                title = if (activeSlider == null) stringResource(R.string.adjust_size_title) else "",
                scrollState = scrollState,
                onBackClick = if (activeSlider == null) { { if (hasChanges) showExitDialog = true else onBack() } } else null,
                actions = { iconBgColor ->
                    if (activeSlider == null) {
                        TextButton(onClick = {
                            keyboardWidth = baselineWidth
                            keyboardAlignment = baselineAlignment
                            keyboardHeight = baselineHeight
                            bottomPadding = baselineBottomPadding
                            keyCornerRadius = baselineCornerRadius
                            keyPadding = baselinePadding
                            keyFontSize = baselineFontSize
                            globalRowGap = baselineGlobalRowGap
                            individualRowGaps = baselineIndividualRowGaps

                            prefs.keyboardWidth = baselineWidth
                            prefs.keyboardAlignment = baselineAlignment
                            prefs.keyboardHeight = baselineHeight
                            prefs.bottomPadding = baselineBottomPadding
                            prefs.keyCornerRadius = baselineCornerRadius
                            prefs.keyPadding = baselinePadding
                            prefs.keyFontSize = baselineFontSize
                            prefs.globalRowGap = baselineGlobalRowGap
                            prefs.individualRowGaps = baselineIndividualRowGaps
                        }) { Text(stringResource(R.string.reset), color = Color.Red, fontWeight = FontWeight.Bold) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (activePresetId != "default") {
                                    val updatedList = sizePresetsList.map { item ->
                                        if (item.optString("id") == activePresetId) {
                                            item.put("width", keyboardWidth.toDouble())
                                            item.put("alignment", keyboardAlignment)
                                            item.put("height", keyboardHeight.toDouble())
                                            item.put("bottomPadding", bottomPadding.toDouble())
                                            item.put("keyCornerRadius", keyCornerRadius.toDouble())
                                            item.put("keyPadding", keyPadding.toDouble())
                                            item.put("keyFontSize", keyFontSize.toDouble())
                                            item.put("globalRowGap", globalRowGap.toDouble())
                                            item.put("individualRowGaps", individualRowGaps)
                                        }
                                        item
                                    }
                                    val arr = org.json.JSONArray()
                                    updatedList.forEach { arr.put(it) }
                                    prefs.savedSizePresets = arr.toString()
                                }
                                prefs.activeSizePreset = activePresetId
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) { Text(stringResource(R.string.save), fontWeight = FontWeight.Bold, color = Color.Black) }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
        ) {

            ResizeSettingsCards(
                prefs = prefs,
                keyboardWidth = keyboardWidth,
                onKeyboardWidthChange = { keyboardWidth = it },
                keyboardAlignment = keyboardAlignment,
                onKeyboardAlignmentChange = { keyboardAlignment = it },
                keyboardHeight = keyboardHeight,
                onKeyboardHeightChange = { keyboardHeight = it },
                bottomPadding = bottomPadding,
                onBottomPaddingChange = { bottomPadding = it },
                keyCornerRadius = keyCornerRadius,
                onKeyCornerRadiusChange = { keyCornerRadius = it },
                keyPadding = keyPadding,
                onKeyPaddingChange = { keyPadding = it },
                keyFontSize = keyFontSize,
                onKeyFontSizeChange = { keyFontSize = it },
                globalRowGap = globalRowGap,
                onGlobalRowGapChange = { globalRowGap = it },
                individualRowGaps = individualRowGaps,
                onIndividualRowGapsChange = { individualRowGaps = it },
                onActiveSliderChange = { activeSlider = it },
                onExportClick = exportSizeConfig,
                onImportClick = { importLauncher.launch(arrayOf("*/*")) },
                onResetToDefaultClick = {
                    keyboardWidth = baselineWidth; keyboardAlignment = baselineAlignment; keyboardHeight = baselineHeight; bottomPadding = baselineBottomPadding; keyCornerRadius = baselineCornerRadius; keyPadding = baselinePadding; keyFontSize = baselineFontSize; globalRowGap = baselineGlobalRowGap; individualRowGaps = baselineIndividualRowGaps
                    prefs.keyboardWidth = baselineWidth; prefs.keyboardAlignment = baselineAlignment; prefs.keyboardHeight = baselineHeight; prefs.bottomPadding = baselineBottomPadding; prefs.keyCornerRadius = baselineCornerRadius; prefs.keyPadding = baselinePadding; prefs.keyFontSize = baselineFontSize; prefs.globalRowGap = baselineGlobalRowGap; prefs.individualRowGaps = baselineIndividualRowGaps
                },
                activePresetId = activePresetId,
                presetsList = sizePresetsList.map { it.optString("id") },
                onPresetSelect = { selectPreset(it) },
                onPresetDelete = { presetToDelete = it },
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .widthIn(max = 600.dp)
                    .topAndBottomNoise()
                    .verticalScroll(scrollState)
                    .padding(
                        top = innerPadding.calculateTopPadding() + 16.dp,
                        bottom = 100.dp + innerPadding.calculateBottomPadding()
                    )
            )

            if (activeSlider != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(enabled = false) {}
                ) {
                    if (!isImeVisible) {
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { ctx ->
                                com.noxtan.noxboard.NoxKeyboardView(ctx).apply {
                                    isEditMode = true
                                }
                            },
                            update = { view ->
                                prefs.isSliding = true
                                prefs.slideWidth = keyboardWidth
                                prefs.slideHeight = keyboardHeight
                                prefs.slideBottomPadding = bottomPadding
                                prefs.slideAlignment = keyboardAlignment

                                view.currentMode = com.noxtan.noxboard.KeyboardMode.MYANMAR
                                view.applySettings(prefs)
                                view.requestLayout()
                                view.invalidate()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }

            ActiveSliderOverlay(
                activeSlider = activeSlider,
                keyboardWidth = keyboardWidth,
                onKeyboardWidthChange = { keyboardWidth = it },
                keyboardHeight = keyboardHeight,
                onKeyboardHeightChange = { keyboardHeight = it },
                bottomPadding = bottomPadding,
                onBottomPaddingChange = { bottomPadding = it },
                onActiveSliderChange = { activeSlider = it },
                prefs = prefs
            )

            KeyboardPreviewArea(
                textState = textState,
                onTextChange = { textState = it },
                isImeVisible = isImeVisible,
                isSlidingActive = isSlidingActive,
                focusRequester = focusRequester,
                keyboardController = keyboardController,
                animatedWidth = animatedWidth,
                animatedHeight = animatedHeight,
                animatedCornerRadius = animatedCornerRadius,
                animatedBgColor = animatedBgColor,
                animatedBorderColor = animatedBorderColor,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isImeVisible) 0.dp else innerPadding.calculateBottomPadding())
            )

            UnsavedChangesDialog(
                showDialog = showExitDialog,
                onDismiss = { showExitDialog = false },
                onConfirm = {
                    prefs.keyboardWidth = initialWidth
                    prefs.keyboardAlignment = initialAlignment
                    prefs.keyboardHeight = initialHeight
                    prefs.bottomPadding = initialBottomPadding
                    prefs.keyCornerRadius = initialCornerRadius
                    prefs.keyPadding = initialPadding
                    prefs.keyFontSize = initialFontSize
                    prefs.globalRowGap = initialGlobalRowGap
                    prefs.individualRowGaps = initialIndividualRowGaps

                    prefs.activeSizePreset = initialActivePresetId
                    prefs.savedSizePresets = initialSavedSizePresets

                    showExitDialog = false
                    onBack()
                }
            )
            if (presetToDelete != null) {
                AlertDialog(
                    onDismissRequest = { presetToDelete = null },
                    title = {
                        Text(
                            text = "Delete Slot",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    text = {
                        Text(
                            text = "ဤ Slot $presetToDelete အရွယ်အစား ဆက်တင်ကို လုံးဝဖျက်ပစ်လိုပါသလား?",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val idToDelete = presetToDelete
                            presetToDelete = null
                            if (idToDelete != null) {
                                val filteredList = sizePresetsList.filter { it.optString("id") != idToDelete }
                                sizePresetsList = filteredList as MutableList<JSONObject>

                                val arr = org.json.JSONArray()
                                filteredList.forEach { arr.put(it) }
                                prefs.savedSizePresets = arr.toString()

                                if (activePresetId == idToDelete) {
                                    selectPreset("default")
                                }
                                android.widget.Toast.makeText(context, "Slot $idToDelete deleted successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { presetToDelete = null }) {
                            Text("Cancel", color = Color.White)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}