package com.noxtan.noxboard.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.noxtan.noxboard.KeyboardMode
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.NoxKeyboardView
import com.noxtan.noxboard.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardCustomThemeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }
    val initialWallpaperUri = remember { prefs.customWallpaperUri }
    val initialWallpaperScale = remember { prefs.customWallpaperScale }
    val initialWallpaperOffsetX = remember { prefs.customWallpaperOffsetX }
    val initialWallpaperOffsetY = remember { prefs.customWallpaperOffsetY }

    var wallpaperUri by remember { mutableStateOf(prefs.customWallpaperUri) }
    var wallpaperScale by remember { mutableFloatStateOf(prefs.customWallpaperScale) }
    var wallpaperOffsetX by remember { mutableFloatStateOf(prefs.customWallpaperOffsetX) }
    var wallpaperOffsetY by remember { mutableFloatStateOf(prefs.customWallpaperOffsetY) }
    var isTextureEffectEnabled by remember { mutableStateOf(prefs.isTextureEffectEnabled) }
    var useCustomIconColors by remember { mutableStateOf(prefs.useCustomIconColors) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                if (wallpaperUri != null && wallpaperUri!!.contains("theme_wallpapers")) {
                    try {
                        val path = android.net.Uri.parse(wallpaperUri).path
                        if (path != null) java.io.File(path).delete()
                    } catch (e: Exception) {}
                }

                val compressedPath = com.noxtan.noxboard.utils.ImageCompressor.compressAndSaveImage(
                    context = context,
                    inputUri = uri,
                    fileName = "custom_wall_${System.currentTimeMillis()}.jpg"
                )

                if (compressedPath != null) {
                    wallpaperUri = compressedPath
                    prefs.customWallpaperUri = wallpaperUri
                } else {
                    android.widget.Toast.makeText(context, "Failed to load image", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    var individualKeyTextColorsMap by remember {
        mutableStateOf(
            try {
                val json = org.json.JSONObject(prefs.individualKeyTextColors)
                val map = mutableMapOf<String, String>()
                json.keys().forEach { map[it] = json.getString(it) }
                map
            } catch (e: Exception) { mutableMapOf<String, String>() }
        )
    }

    var individualKeyActiveColorsMap by remember { mutableStateOf(try { val j = org.json.JSONObject(prefs.individualKeyActiveColors); val m = mutableMapOf<String, String>(); j.keys().forEach { m[it] = j.getString(it) }; m } catch (e: Exception) { mutableMapOf<String, String>() }) }
    var individualKeyPopupBgColorsMap by remember { mutableStateOf(try { val j = org.json.JSONObject(prefs.individualKeyPopupBgColors); val m = mutableMapOf<String, String>(); j.keys().forEach { m[it] = j.getString(it) }; m } catch (e: Exception) { mutableMapOf<String, String>() }) }
    var individualKeyPopupTextColorsMap by remember { mutableStateOf(try { val j = org.json.JSONObject(prefs.individualKeyPopupTextColors); val m = mutableMapOf<String, String>(); j.keys().forEach { m[it] = j.getString(it) }; m } catch (e: Exception) { mutableMapOf<String, String>() }) }

    var individualKeyColorsMap by remember {
        mutableStateOf(
            try {
                val json = org.json.JSONObject(prefs.individualKeyColors)
                val map = mutableMapOf<String, String>()
                json.keys().forEach { map[it] = json.getString(it) }
                map
            } catch (e: Exception) { mutableMapOf<String, String>() }
        )
    }

    var bgColorHex by remember { mutableStateOf(prefs.customBackgroundColor) }
    var keyColorHex by remember { mutableStateOf(prefs.customKeyColor) }
    var textColorHex by remember { mutableStateOf(prefs.customTextColor) }
    var specialKeyColorHex by remember { mutableStateOf(prefs.customSpecialKeyColor) }
    var specialTextColorHex by remember { mutableStateOf(prefs.customSpecialTextColor) }
    var keyBorderColorHex by remember { mutableStateOf(prefs.customKeyBorderColor) }

    var activeKeyColorHex by remember { mutableStateOf(prefs.customActiveKeyColor) }
    var popupBgColorHex by remember { mutableStateOf(prefs.customPopupBgColor) }
    var popupTextColorHex by remember { mutableStateOf(prefs.customPopupTextColor) }
    var numpadBgColorHex by remember { mutableStateOf(prefs.customNumpadBgColor) }
    var numpadKeyColorHex by remember { mutableStateOf(prefs.customNumpadKeyColor) }
    var numpadTextColorHex by remember { mutableStateOf(prefs.customNumpadTextColor) }

    var isCustomizingSpecificKeys by remember { mutableStateOf(false) }
    var isCustomizingSpecificEffects by remember { mutableStateOf(false) }
    var previewKeyboardMode by remember { mutableStateOf(KeyboardMode.MYANMAR) }
    var selectedKeysToEdit by remember { mutableStateOf(setOf<String>()) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val availableTabs = remember(prefs.isNumberRowEnabled) {
        val list = mutableListOf(
            0 to R.string.tab_background,
            1 to R.string.tab_keys,
            2 to R.string.tab_text,
            3 to R.string.tab_highlights
        )
        if (!prefs.isNumberRowEnabled) {
            list.add(4 to R.string.tab_keypad)
        }
        list.add(5 to R.string.tab_effects)
        list
    }
    if (selectedTab >= availableTabs.size) {
        selectedTab = availableTabs.size - 1
    }
    var numpadSpecialKeyColorHex by remember { mutableStateOf(prefs.customNumpadSpecialKeyColor) }
    var numpadSpecialTextColorHex by remember { mutableStateOf(prefs.customNumpadSpecialTextColor) }
    val initialIndColorsMap = remember { individualKeyColorsMap.toMap() }
    val initialIndTextsMap = remember { individualKeyTextColorsMap.toMap() }
    val initialIndActiveMap = remember { individualKeyActiveColorsMap.toMap() }
    val initialIndPopBgMap = remember { individualKeyPopupBgColorsMap.toMap() }
    val initialIndPopTextMap = remember { individualKeyPopupTextColorsMap.toMap() }

    var showExitDialog by remember { mutableStateOf(false) }

    val handleBack = {
        val hasChanges = bgColorHex != prefs.customBackgroundColor ||
                keyColorHex != prefs.customKeyColor ||
                textColorHex != prefs.customTextColor ||
                specialKeyColorHex != prefs.customSpecialKeyColor ||
                specialTextColorHex != prefs.customSpecialTextColor ||
                activeKeyColorHex != prefs.customActiveKeyColor ||
                popupBgColorHex != prefs.customPopupBgColor ||
                popupTextColorHex != prefs.customPopupTextColor ||
                numpadBgColorHex != prefs.customNumpadBgColor ||
                numpadKeyColorHex != prefs.customNumpadKeyColor ||
                numpadTextColorHex != prefs.customNumpadTextColor ||
                numpadSpecialKeyColorHex != prefs.customNumpadSpecialKeyColor ||
                numpadSpecialTextColorHex != prefs.customNumpadSpecialTextColor ||
                wallpaperUri != initialWallpaperUri ||
                wallpaperScale != initialWallpaperScale ||
                wallpaperOffsetX != initialWallpaperOffsetX ||
                wallpaperOffsetY != initialWallpaperOffsetY ||
                useCustomIconColors != prefs.useCustomIconColors ||
                isTextureEffectEnabled != prefs.isTextureEffectEnabled ||
                individualKeyColorsMap != initialIndColorsMap ||
                individualKeyTextColorsMap != initialIndTextsMap ||
                individualKeyActiveColorsMap != initialIndActiveMap ||
                individualKeyPopupBgColorsMap != initialIndPopBgMap ||
                individualKeyPopupTextColorsMap != initialIndPopTextMap

        if (hasChanges) {
            showExitDialog = true
        } else {
            prefs.customWallpaperUri = initialWallpaperUri
            prefs.customWallpaperScale = initialWallpaperScale
            prefs.customWallpaperOffsetX = initialWallpaperOffsetX
            prefs.customWallpaperOffsetY = initialWallpaperOffsetY
            onBack()
        }
    }

    androidx.activity.compose.BackHandler(enabled = true) {
        handleBack()
    }

    LaunchedEffect(selectedTab) {
        val originalIndex = availableTabs.getOrNull(selectedTab)?.first ?: 0
        if (originalIndex != 1) isCustomizingSpecificKeys = false
        if (originalIndex != 5) isCustomizingSpecificEffects = false
        if (originalIndex != 1 && originalIndex != 5) {
            selectedKeysToEdit = emptySet()
            previewKeyboardMode = KeyboardMode.MYANMAR
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    prefs.customWallpaperUri = initialWallpaperUri
                    prefs.customWallpaperScale = initialWallpaperScale
                    prefs.customWallpaperOffsetX = initialWallpaperOffsetX
                    prefs.customWallpaperOffsetY = initialWallpaperOffsetY
                    handleBack()
                }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray, fontSize = 16.sp)
                }

                val isEditing = prefs.editingThemeId != null
                Text(if (isEditing) "Edit Theme" else stringResource(R.string.create_theme_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Button(
                    onClick = {
                        prefs.customBackgroundColor = bgColorHex
                        prefs.customKeyColor = keyColorHex
                        prefs.customTextColor = textColorHex
                        prefs.customSpecialKeyColor = specialKeyColorHex
                        prefs.customSpecialTextColor = specialTextColorHex
                        prefs.customKeyBorderColor = keyBorderColorHex
                        prefs.customActiveKeyColor = activeKeyColorHex
                        prefs.customPopupBgColor = popupBgColorHex
                        prefs.customPopupTextColor = popupTextColorHex
                        prefs.useCustomIconColors = useCustomIconColors
                        prefs.isTextureEffectEnabled = isTextureEffectEnabled
                        prefs.customNumpadSpecialKeyColor = numpadSpecialKeyColorHex
                        prefs.customNumpadSpecialTextColor = numpadSpecialTextColorHex
                        prefs.customNumpadBgColor = numpadBgColorHex
                        prefs.customNumpadKeyColor = numpadKeyColorHex
                        prefs.customNumpadTextColor = numpadTextColorHex

                        val jColors = org.json.JSONObject(); individualKeyColorsMap.forEach { (k, v) -> jColors.put(k, v) }; prefs.individualKeyColors = jColors.toString()
                        val jActive = org.json.JSONObject(); individualKeyActiveColorsMap.forEach { (k, v) -> jActive.put(k, v) }; prefs.individualKeyActiveColors = jActive.toString()
                        val jPopBg = org.json.JSONObject(); individualKeyPopupBgColorsMap.forEach { (k, v) -> jPopBg.put(k, v) }; prefs.individualKeyPopupBgColors = jPopBg.toString()
                        val jPopText = org.json.JSONObject(); individualKeyPopupTextColorsMap.forEach { (k, v) -> jPopText.put(k, v) }; prefs.individualKeyPopupTextColors = jPopText.toString()
                        val jText = org.json.JSONObject(); individualKeyTextColorsMap.forEach { (k, v) -> jText.put(k, v) }; prefs.individualKeyTextColors = jText.toString()
                        val themesArray = org.json.JSONArray(prefs.savedCustomThemes)
                        val editingId = prefs.editingThemeId
                        val themeId = editingId ?: ("theme_" + System.currentTimeMillis())

                        val newTheme = org.json.JSONObject()
                        newTheme.put("id", themeId)
                        newTheme.put("name", if (editingId != null) {
                            var oldName = "Custom Theme"
                            for (i in 0 until themesArray.length()) {
                                val item = themesArray.getJSONObject(i)
                                if (item.optString("id") == editingId) oldName = item.optString("name")
                            }
                            oldName
                        } else "Custom Theme ${themesArray.length() + 1}")

                        newTheme.put("numpadSpecialKey", numpadSpecialKeyColorHex)
                        newTheme.put("numpadSpecialText", numpadSpecialTextColorHex)
                        newTheme.put("bg", bgColorHex)
                        newTheme.put("key", keyColorHex)
                        newTheme.put("text", textColorHex)
                        newTheme.put("specialKey", specialKeyColorHex)
                        newTheme.put("specialText", specialTextColorHex)
                        newTheme.put("keyBorder", keyBorderColorHex)
                        newTheme.put("activeKey", activeKeyColorHex)
                        newTheme.put("popupBg", popupBgColorHex)
                        newTheme.put("popupText", popupTextColorHex)
                        newTheme.put("iconTint", useCustomIconColors)
                        newTheme.put("texture", isTextureEffectEnabled)

                        newTheme.put("numpadBg", numpadBgColorHex)
                        newTheme.put("numpadKey", numpadKeyColorHex)
                        newTheme.put("numpadText", numpadTextColorHex)

                        newTheme.put("wallUri", wallpaperUri ?: "")
                        newTheme.put("wallScale", wallpaperScale.toDouble())
                        newTheme.put("wallX", wallpaperOffsetX.toDouble())
                        newTheme.put("wallY", wallpaperOffsetY.toDouble())

                        newTheme.put("ind_colors", jColors.toString())
                        newTheme.put("ind_texts", jText.toString())
                        newTheme.put("ind_active", jActive.toString())
                        newTheme.put("ind_popBg", jPopBg.toString())
                        newTheme.put("ind_popText", jPopText.toString())

                        if (editingId != null) {
                            val newArray = org.json.JSONArray()
                            for (i in 0 until themesArray.length()) {
                                val item = themesArray.getJSONObject(i)
                                if (item.optString("id") == editingId) {
                                    newArray.put(newTheme)
                                } else {
                                    newArray.put(item)
                                }
                            }
                            prefs.savedCustomThemes = newArray.toString()
                            prefs.editingThemeId = null
                        } else {
                            themesArray.put(newTheme)
                            prefs.savedCustomThemes = themesArray.toString()
                        }

                        prefs.activeTheme = themeId
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(stringResource(R.string.save), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            com.noxtan.noxboard.ui.components.UnsavedChangesDialog(
                showDialog = showExitDialog,
                onDismiss = { showExitDialog = false },
                onConfirm = {
                    prefs.customWallpaperUri = initialWallpaperUri
                    prefs.customWallpaperScale = initialWallpaperScale
                    prefs.customWallpaperOffsetX = initialWallpaperOffsetX
                    prefs.customWallpaperOffsetY = initialWallpaperOffsetY

                    showExitDialog = false
                    onBack()
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
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        NoxKeyboardView(ctx).apply {
                            isEditMode = true
                            onEditKeyClick = { rIndex, kIndex ->
                                if (isCustomizingSpecificKeys || isCustomizingSpecificEffects) {
                                    val layout = when (previewKeyboardMode) {
                                        KeyboardMode.MYANMAR -> com.noxtan.noxboard.KeyboardLayout.getMyanmarLayout(context, prefs.isNumberRowEnabled)
                                        KeyboardMode.ENGLISH -> com.noxtan.noxboard.KeyboardLayout.getEnglishLayout(context, prefs.isNumberRowEnabled)
                                        else -> com.noxtan.noxboard.KeyboardLayout.getSymbolLayout(context, 1)
                                    }
                                    if (rIndex < layout.size && kIndex < layout[rIndex].size) {
                                        val key = layout[rIndex][kIndex]
                                        val isStandard = previewKeyboardMode == KeyboardMode.MYANMAR || previewKeyboardMode == KeyboardMode.ENGLISH
                                        val keyId = when {
                                            (isStandard && rIndex == 0) -> "Suggestion"
                                            key.code in -102..-100 -> "Suggestion"
                                            key.code == 32 -> "Space"
                                            key.code == 10 -> "Enter"
                                            key.code == -2 -> "Delete"
                                            key.code == -1 -> "Shift"
                                            key.code == -3 -> "Globe"
                                            key.code == -4 || key.normalText == "ABC" || key.normalText == "#12" -> "Symbol"
                                            key.code == -11 -> "Emoji"
                                            key.code == -12 -> "Vault"
                                            key.code == -13 -> "Clipboard"
                                            key.code == -14 -> "Settings"
                                            key.code == -8 -> "Numpad"
                                            else -> key.normalText
                                        }

                                        if (keyId.isNotEmpty()) {
                                            selectedKeysToEdit = if (selectedKeysToEdit.contains(keyId)) {
                                                selectedKeysToEdit - keyId
                                            } else {
                                                selectedKeysToEdit + keyId
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    update = { view ->
                        val triggerUri = wallpaperUri
                        val triggerScale = wallpaperScale
                        val triggerX = wallpaperOffsetX
                        val triggerY = wallpaperOffsetY

                        view.currentMode = previewKeyboardMode
                        view.applySettings(prefs)
                        view.useCustomIconColors = useCustomIconColors
                        view.theme.isTextureEffectEnabled = isTextureEffectEnabled

                        val safeParse = { hex: String, applyTheme: (Int, IntArray?) -> Unit ->
                            try {
                                if (hex.startsWith("GRADIENT:")) {
                                    val arr = hex.removePrefix("GRADIENT:").split(",").map { android.graphics.Color.parseColor(it) }.toIntArray()
                                    applyTheme(arr[0], arr)
                                } else {
                                    applyTheme(android.graphics.Color.parseColor(hex), null)
                                }
                            } catch (e: Exception) {}
                        }

                        safeParse(numpadBgColorHex) { c, g -> view.theme.numpadBackgroundColor = c; view.theme.numpadBgGradient = g }
                        safeParse(numpadKeyColorHex) { c, g -> view.theme.numpadKeyColor = c; view.theme.numpadKeyGradient = g }
                        safeParse(numpadTextColorHex) { c, g -> view.theme.numpadTextColor = c; view.theme.numpadTextGradient = g }
                        safeParse(numpadSpecialKeyColorHex) { c, g -> view.theme.numpadSpecialKeyColor = c; view.theme.numpadSpecialKeyGradient = g }
                        safeParse(numpadSpecialTextColorHex) { c, g -> view.theme.numpadSpecialTextColor = c; view.theme.numpadSpecialTextGradient = g }
                        val currentOriginalIndex = availableTabs.getOrNull(selectedTab)?.first ?: 0
                        view.isNumpadMode = (currentOriginalIndex == 4)
                        safeParse(bgColorHex) { c, g -> view.theme.backgroundColor = c; view.theme.backgroundGradient = g }
                        safeParse(keyColorHex) { c, g -> view.theme.keyColor = c; view.theme.keyGradient = g }
                        safeParse(specialKeyColorHex) { c, g -> view.theme.specialKeyColor = c; view.theme.specialKeyGradient = g }
                        safeParse(activeKeyColorHex) { c, g -> view.theme.activeKeyColor = c; view.theme.activeKeyGradient = g }
                        safeParse(popupBgColorHex) { c, g -> view.theme.popupBackgroundColor = c; view.theme.popupBgGradient = g }
                        safeParse(popupTextColorHex) { c, g -> view.theme.popupTextColor = c; view.theme.popupTextGradient = g }
                        safeParse(textColorHex) { c, g -> view.theme.textColor = c; view.theme.textGradient = g }
                        safeParse(specialTextColorHex) { c, g -> view.theme.specialTextColor = c; view.theme.specialTextGradient = g }

                        try {
                            view.theme.keyBorderColor = android.graphics.Color.parseColor(keyBorderColorHex)
                        } catch(e: Exception) {}

                        view.initPaints()

                        view.setIndividualColorsForPreview(
                            bgMap = individualKeyColorsMap,
                            textMap = individualKeyTextColorsMap,
                            activeMap = individualKeyActiveColorsMap,
                            popBgMap = individualKeyPopupBgColorsMap,
                            popTextMap = individualKeyPopupTextColorsMap
                        )

                        if ((isCustomizingSpecificKeys || isCustomizingSpecificEffects) && selectedKeysToEdit.isNotEmpty()) {
                            val layout = when (previewKeyboardMode) {
                                KeyboardMode.MYANMAR -> com.noxtan.noxboard.KeyboardLayout.getMyanmarLayout(context, prefs.isNumberRowEnabled)
                                KeyboardMode.ENGLISH -> com.noxtan.noxboard.KeyboardLayout.getEnglishLayout(context, prefs.isNumberRowEnabled)
                                else -> com.noxtan.noxboard.KeyboardLayout.getSymbolLayout(context, 1)
                            }
                            val foundPositions = mutableSetOf<Pair<Int, Int>>()
                            for (r in layout.indices) {
                                for (c in layout[r].indices) {
                                    val key = layout[r][c]
                                    val isStandard = previewKeyboardMode == KeyboardMode.MYANMAR || previewKeyboardMode == KeyboardMode.ENGLISH
                                    val keyId = when {
                                        (isStandard && r == 0) -> "Suggestion"
                                        key.code in -102..-100 -> "Suggestion"
                                        key.code == 32 -> "Space"
                                        key.code == 10 -> "Enter"
                                        key.code == -2 -> "Delete"
                                        key.code == -1 -> "Shift"
                                        key.code == -3 -> "Globe"
                                        key.code == -4 || key.normalText == "ABC" || key.normalText == "#12" -> "Symbol"
                                        key.code == -11 -> "Emoji"
                                        key.code == -12 -> "Vault"
                                        key.code == -13 -> "Clipboard"
                                        key.code == -14 -> "Settings"
                                        key.code == -8 -> "Numpad"
                                        else -> key.normalText
                                    }
                                    if (selectedKeysToEdit.contains(keyId)) {
                                        foundPositions.add(Pair(r, c))
                                    }
                                }
                            }
                            view.editSelectedKeyPositions = foundPositions
                        } else {
                            view.editSelectedKeyPositions = emptySet()
                        }

                        view.requestLayout()
                        view.invalidate()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    availableTabs.forEachIndexed { index, pair ->
                        val title = stringResource(pair.second)
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == index) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 4.dp)
                ) {
                    when (availableTabs.getOrNull(selectedTab)?.first) {
                        0 -> {
                            Column(
                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                ColorPickerPanel(
                                    title = stringResource(R.string.bg_color),
                                    currentColorHex = bgColorHex,
                                    onColorSelected = { bgColorHex = it },
                                    onReset = { bgColorHex = "#000000" }
                                )

                                HorizontalDivider(color = Color.DarkGray)

                                Text(stringResource(R.string.bg_wallpaper), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text(stringResource(R.string.pick_image), color = Color.Black, fontWeight = FontWeight.Bold)
                                    }

                                    if (wallpaperUri != null) {
                                        Button(
                                            onClick = {
                                                if (wallpaperUri!!.contains("theme_wallpapers")) {
                                                    try {
                                                        val path = android.net.Uri.parse(wallpaperUri).path
                                                        if (path != null) java.io.File(path).delete()
                                                    } catch (e: Exception) {}
                                                }
                                                wallpaperUri = null
                                                prefs.customWallpaperUri = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                        ) {
                                            Text(stringResource(R.string.remove), color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (wallpaperUri != null) {
                                    ColorSlider(stringResource(R.string.image_scale), (wallpaperScale - 0.5f) / 4.5f, MaterialTheme.colorScheme.primary) {
                                        wallpaperScale = 0.5f + (it * 4.5f)
                                        prefs.customWallpaperScale = wallpaperScale
                                    }
                                    ColorSlider(stringResource(R.string.offset_x), (wallpaperOffsetX + 500f) / 1000f, MaterialTheme.colorScheme.primary) {
                                        wallpaperOffsetX = -500f + (it * 1000f)
                                        prefs.customWallpaperOffsetX = wallpaperOffsetX
                                    }
                                    ColorSlider(stringResource(R.string.offset_y), (wallpaperOffsetY + 500f) / 1000f, MaterialTheme.colorScheme.primary) {
                                        wallpaperOffsetY = -500f + (it * 1000f)
                                        prefs.customWallpaperOffsetY = wallpaperOffsetY
                                    }
                                }

                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }

                        1 -> {
                            Column(
                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCustomizingSpecificKeys) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                                        .clickable {
                                            isCustomizingSpecificKeys = !isCustomizingSpecificKeys
                                            selectedKeysToEdit = emptySet()
                                            if (!isCustomizingSpecificKeys) previewKeyboardMode = KeyboardMode.MYANMAR
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(stringResource(R.string.customize_specific_keys), fontWeight = FontWeight.Bold, color = if (isCustomizingSpecificKeys) MaterialTheme.colorScheme.primary else Color.White)
                                        Text(stringResource(R.string.tap_keys_to_change_color), fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Switch(checked = isCustomizingSpecificKeys, onCheckedChange = {
                                        isCustomizingSpecificKeys = it
                                        selectedKeysToEdit = emptySet()
                                        if (!it) previewKeyboardMode = KeyboardMode.MYANMAR
                                    })
                                }

                                if (isCustomizingSpecificKeys) {
                                    val langTabs = listOf("Myanmar", "English", "Symbols")
                                    var selectedLangTab by remember { mutableIntStateOf(0) }

                                    LaunchedEffect(selectedLangTab) {
                                        previewKeyboardMode = when (selectedLangTab) {
                                            0 -> KeyboardMode.MYANMAR
                                            1 -> KeyboardMode.ENGLISH
                                            else -> KeyboardMode.SYMBOLS_1
                                        }
                                        selectedKeysToEdit = emptySet()
                                    }

                                    TabRow(
                                        selectedTabIndex = selectedLangTab,
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ) {
                                        langTabs.forEachIndexed { index, title ->
                                            Tab(
                                                selected = selectedLangTab == index,
                                                onClick = { selectedLangTab = index },
                                                text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                            )
                                        }
                                    }

                                    if (selectedKeysToEdit.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text(stringResource(R.string.editing_keys, selectedKeysToEdit.size), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                TextButton(onClick = {
                                                    val newBg = individualKeyColorsMap.toMutableMap()
                                                    val newText = individualKeyTextColorsMap.toMutableMap()
                                                    selectedKeysToEdit.forEach {
                                                        newBg.remove(it)
                                                        newText.remove(it)
                                                    }
                                                    individualKeyColorsMap = newBg
                                                    individualKeyTextColorsMap = newText
                                                }) { Text(stringResource(R.string.clear), color = Color.Red) }
                                            }

                                            val firstKey = selectedKeysToEdit.first()
                                            val currentBg = individualKeyColorsMap[firstKey] ?: keyColorHex
                                            val currentText = individualKeyTextColorsMap[firstKey] ?: textColorHex

                                            ColorPickerPanel(
                                                title = stringResource(R.string.background_text),
                                                currentColorHex = currentBg,
                                                onColorSelected = { hex ->
                                                    val newMap = individualKeyColorsMap.toMutableMap()
                                                    selectedKeysToEdit.forEach { newMap[it] = hex }
                                                    individualKeyColorsMap = newMap
                                                },
                                                onReset = {
                                                    val newMap = individualKeyColorsMap.toMutableMap()
                                                    selectedKeysToEdit.forEach { newMap.remove(it) }
                                                    individualKeyColorsMap = newMap
                                                }
                                            )
                                            ColorPickerPanel(
                                                title = stringResource(R.string.text_icon),
                                                currentColorHex = currentText,
                                                onColorSelected = { hex ->
                                                    val newMap = individualKeyTextColorsMap.toMutableMap()
                                                    selectedKeysToEdit.forEach { newMap[it] = hex }
                                                    individualKeyTextColorsMap = newMap
                                                },
                                                onReset = {
                                                    val newMap = individualKeyTextColorsMap.toMutableMap()
                                                    selectedKeysToEdit.forEach { newMap.remove(it) }
                                                    individualKeyTextColorsMap = newMap
                                                }
                                            )
                                        }
                                    } else {
                                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                            Text(stringResource(R.string.tap_any_key_preview), color = Color.Gray)
                                        }
                                    }
                                } else {
                                    ColorPickerPanel(
                                        title = stringResource(R.string.normal_keys_color),
                                        currentColorHex = keyColorHex,
                                        onColorSelected = { keyColorHex = it },
                                        onReset = { keyColorHex = "#1E1A2B" }
                                    )
                                }
                                Spacer(modifier = Modifier.height(60.dp))
                            }
                        }

                        2 -> {
                            Column(
                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                ColorPickerPanel(
                                    title = stringResource(R.string.text_icon_color),
                                    currentColorHex = textColorHex,
                                    onColorSelected = { textColorHex = it },
                                    onReset = { textColorHex = "#FFFFFF" }
                                )
                                Spacer(modifier = Modifier.height(60.dp))
                            }
                        }

                        3 -> {
                            Column(
                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                ColorPickerPanel(
                                    title = stringResource(R.string.highlights_bg_color),
                                    currentColorHex = specialKeyColorHex,
                                    onColorSelected = { specialKeyColorHex = it },
                                    onReset = { specialKeyColorHex = "#12101C" }
                                )
                                ColorPickerPanel(
                                    title = stringResource(R.string.highlights_text_color),
                                    currentColorHex = specialTextColorHex,
                                    onColorSelected = { specialTextColorHex = it },
                                    onReset = { specialTextColorHex = "#FFFFFF" }
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(stringResource(R.string.tint_emoji_vault), fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(stringResource(R.string.tint_emoji_vault_desc), fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Switch(
                                        checked = useCustomIconColors,
                                        onCheckedChange = { useCustomIconColors = it }
                                    )
                                }

                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }

                        4 -> {
                            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                                ColorPickerPanel(title = stringResource(R.string.keypad_bg), currentColorHex = numpadBgColorHex, onColorSelected = { numpadBgColorHex = it }, onReset = { numpadBgColorHex = "#000000" })
                                ColorPickerPanel(title = stringResource(R.string.keypad_normal_keys), currentColorHex = numpadKeyColorHex, onColorSelected = { numpadKeyColorHex = it }, onReset = { numpadKeyColorHex = "#1E1A2B" })
                                ColorPickerPanel(title = stringResource(R.string.keypad_text_color), currentColorHex = numpadTextColorHex, onColorSelected = { numpadTextColorHex = it }, onReset = { numpadTextColorHex = "#FFFFFF" })
                                ColorPickerPanel(title = stringResource(R.string.keypad_special_keys), currentColorHex = numpadSpecialKeyColorHex, onColorSelected = { numpadSpecialKeyColorHex = it }, onReset = { numpadSpecialKeyColorHex = "#12101C" })
                                ColorPickerPanel(title = stringResource(R.string.keypad_special_text), currentColorHex = numpadSpecialTextColorHex, onColorSelected = { numpadSpecialTextColorHex = it }, onReset = { numpadSpecialTextColorHex = "#FFFFFF" })

                                Spacer(modifier = Modifier.height(60.dp))
                            }
                        }
                        5 -> {
                            Column(
                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(stringResource(R.string.texture_effect), fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(stringResource(R.string.texture_effect_desc), fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Switch(
                                        checked = isTextureEffectEnabled,
                                        onCheckedChange = { isTextureEffectEnabled = it }
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCustomizingSpecificEffects) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                                        .clickable {
                                            isCustomizingSpecificEffects = !isCustomizingSpecificEffects
                                            selectedKeysToEdit = emptySet()
                                            if (!isCustomizingSpecificEffects) previewKeyboardMode = KeyboardMode.MYANMAR
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(stringResource(R.string.customize_specific_effects), fontWeight = FontWeight.Bold, color = if (isCustomizingSpecificEffects) MaterialTheme.colorScheme.primary else Color.White)
                                        Text(stringResource(R.string.tap_keys_to_change_effects), fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Switch(checked = isCustomizingSpecificEffects, onCheckedChange = {
                                        isCustomizingSpecificEffects = it
                                        selectedKeysToEdit = emptySet()
                                        if (!it) previewKeyboardMode = KeyboardMode.MYANMAR
                                    })
                                }

                                if (isCustomizingSpecificEffects) {
                                    val langTabs = listOf(stringResource(R.string.tab_myanmar), stringResource(R.string.tab_english), stringResource(R.string.tab_symbols))
                                    var selectedLangTab by remember { mutableIntStateOf(0) }

                                    LaunchedEffect(selectedLangTab) {
                                        previewKeyboardMode = when (selectedLangTab) {
                                            0 -> KeyboardMode.MYANMAR
                                            1 -> KeyboardMode.ENGLISH
                                            else -> KeyboardMode.SYMBOLS_1
                                        }
                                        selectedKeysToEdit = emptySet()
                                    }

                                    TabRow(
                                        selectedTabIndex = selectedLangTab,
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ) {
                                        langTabs.forEachIndexed { index, title ->
                                            Tab(
                                                selected = selectedLangTab == index,
                                                onClick = { selectedLangTab = index },
                                                text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                            )
                                        }
                                    }

                                    if (selectedKeysToEdit.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text(stringResource(R.string.editing_keys, selectedKeysToEdit.size), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                TextButton(onClick = {
                                                    val m1 = individualKeyActiveColorsMap.toMutableMap(); val m2 = individualKeyPopupBgColorsMap.toMutableMap(); val m3 = individualKeyPopupTextColorsMap.toMutableMap()
                                                    selectedKeysToEdit.forEach { m1.remove(it); m2.remove(it); m3.remove(it) }
                                                    individualKeyActiveColorsMap = m1; individualKeyPopupBgColorsMap = m2; individualKeyPopupTextColorsMap = m3
                                                }) { Text(stringResource(R.string.clear), color = Color.Red) }
                                            }

                                            val firstKey = selectedKeysToEdit.first()
                                            val curActive = individualKeyActiveColorsMap[firstKey] ?: activeKeyColorHex
                                            val curPopBg = individualKeyPopupBgColorsMap[firstKey] ?: popupBgColorHex
                                            val curPopText = individualKeyPopupTextColorsMap[firstKey] ?: popupTextColorHex

                                            ColorPickerPanel(
                                                title = stringResource(R.string.key_press_color),
                                                currentColorHex = curActive,
                                                onColorSelected = { hex -> val m = individualKeyActiveColorsMap.toMutableMap(); selectedKeysToEdit.forEach { m[it] = hex }; individualKeyActiveColorsMap = m },
                                                onReset = {
                                                    val m = individualKeyActiveColorsMap.toMutableMap()
                                                    selectedKeysToEdit.forEach { m.remove(it) }
                                                    individualKeyActiveColorsMap = m
                                                }
                                            )
                                            ColorPickerPanel(
                                                title = stringResource(R.string.popup_bg),
                                                currentColorHex = curPopBg,
                                                onColorSelected = { hex -> val m = individualKeyPopupBgColorsMap.toMutableMap(); selectedKeysToEdit.forEach { m[it] = hex }; individualKeyPopupBgColorsMap = m },
                                                onReset = {
                                                    val m = individualKeyPopupBgColorsMap.toMutableMap()
                                                    selectedKeysToEdit.forEach { m.remove(it) }
                                                    individualKeyPopupBgColorsMap = m
                                                }
                                            )
                                            ColorPickerPanel(
                                                title = stringResource(R.string.popup_text_color),
                                                currentColorHex = curPopText,
                                                onColorSelected = { hex -> val m = individualKeyPopupTextColorsMap.toMutableMap(); selectedKeysToEdit.forEach { m[it] = hex }; individualKeyPopupTextColorsMap = m },
                                                onReset = {
                                                    val m = individualKeyPopupTextColorsMap.toMutableMap()
                                                    selectedKeysToEdit.forEach { m.remove(it) }
                                                    individualKeyPopupTextColorsMap = m
                                                }
                                            )
                                        }
                                    } else {
                                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { Text(stringResource(R.string.tap_any_key_preview), color = Color.Gray) }
                                    }
                                } else {
                                    ColorPickerPanel(
                                        title = stringResource(R.string.global_key_press_color),
                                        currentColorHex = activeKeyColorHex,
                                        onColorSelected = { activeKeyColorHex = it },
                                        onReset = { activeKeyColorHex = "#7C4DFF" }
                                    )
                                    ColorPickerPanel(
                                        title = stringResource(R.string.global_popup_bg),
                                        currentColorHex = popupBgColorHex,
                                        onColorSelected = { popupBgColorHex = it },
                                        onReset = { popupBgColorHex = "#1F1F1F" }
                                    )
                                    ColorPickerPanel(
                                        title = stringResource(R.string.global_popup_text),
                                        currentColorHex = popupTextColorHex,
                                        onColorSelected = { popupTextColorHex = it },
                                        onReset = { popupTextColorHex = "#FFFFFF" }
                                    )
                                }
                                Spacer(modifier = Modifier.height(60.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerPanel(
    title: String,
    currentColorHex: String,
    onColorSelected: (String) -> Unit,
    onReset: (() -> Unit)? = null
) {
    var showCustomPicker by remember { mutableStateOf(false) }

    val presetColors = listOf(
        "#FFFFFF", "#F2F3F4", "#E0E0E0", "#9E9E9E", "#424242", "#2B2B2B", "#1E1A2B", "#121212", "#000000",
        "#FF5252", "#F44336", "#E91E63", "#FF4081", "#E040FB", "#9C27B0", "#7C4DFF", "#673AB7", "#311B92",
        "#3F51B5", "#536DFE", "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A",
        "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722", "#795548", "#607D8B",
        "#FFCDD2", "#F8BBD0", "#E1BEE7", "#D1C4E9", "#C5CAE9", "#BBDEFB", "#B2EBF2", "#B2DFDB", "#C8E6C9", "#FFF9C4", "#FFE0B2",
        "GRADIENT:#FF0000,#FFFF00,#00FF00,#00FFFF,#0000FF,#FF00FF",
        "GRADIENT:#00C6FF,#0072FF",
        "GRADIENT:#F5AF19,#F12711",
        "GRADIENT:#8A2387,#E94057,#F27121",
        "GRADIENT:#11998E,#38EF7D",
        "GRADIENT:#9D50BB,#6E48AA",
        "GRADIENT:#EB3349,#F45C43",
        "GRADIENT:#FF00CC,#333399",
        "GRADIENT:#232526,#414345",
        "GRADIENT:#BF953F,#FCF6BA,#B38728,#FBF5B7,#AA771C"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (onReset != null) {
                AnimatedResetButton(onClick = onReset)
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            item {
                val rainbowBrush = androidx.compose.ui.graphics.Brush.sweepGradient(
                    listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, rainbowBrush, CircleShape)
                        .clickable { showCustomPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎨", fontSize = 20.sp)
                }
            }

            items(presetColors) { hex ->
                val isGradient = hex.startsWith("GRADIENT:")
                val brush = if (isGradient) {
                    val colors = hex.removePrefix("GRADIENT:").split(",").map {
                        try { Color(android.graphics.Color.parseColor(it)) } catch(e: Exception) { Color.Black }
                    }
                    androidx.compose.ui.graphics.Brush.linearGradient(colors)
                } else {
                    val solid = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Transparent }
                    androidx.compose.ui.graphics.SolidColor(solid)
                }
                val isSelected = currentColorHex.equals(hex, ignoreCase = true)

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(brush)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        val checkColor = if (!isGradient) {
                            val solid = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Black }
                            if ((solid.red * 0.299 + solid.green * 0.587 + solid.blue * 0.114) > 0.5) Color.Black else Color.White
                        } else {
                            Color.White
                        }
                        Icon(Icons.Default.Check, contentDescription = null, tint = checkColor, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (showCustomPicker) {
        CustomColorPickerDialog(
            title = title,
            initialColorHex = currentColorHex,
            onColorSelected = {
                onColorSelected(it)
                showCustomPicker = false
            },
            onDismiss = { showCustomPicker = false }
        )
    }
}

@Composable
fun CustomColorPickerDialog(
    title: String,
    initialColorHex: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isInitialGradient = initialColorHex.startsWith("GRADIENT:")
    var isGradientMode by remember { mutableStateOf(isInitialGradient) }

    val c1 = remember {
        try {
            if (isInitialGradient) {
                Color(android.graphics.Color.parseColor(initialColorHex.removePrefix("GRADIENT:").split(",")[0]))
            } else {
                Color(android.graphics.Color.parseColor(initialColorHex))
            }
        } catch (e: Exception) { Color.Black }
    }

    val c2 = remember {
        try {
            if (isInitialGradient) {
                Color(android.graphics.Color.parseColor(initialColorHex.removePrefix("GRADIENT:").split(",").last()))
            } else {
                Color.White
            }
        } catch (e: Exception) { Color.White }
    }

    fun colorToHsv(color: Color): FloatArray {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(android.graphics.Color.argb(255, (color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt()), hsv)
        return hsv
    }

    val hsv1Init = remember { colorToHsv(c1) }
    val hsv2Init = remember { colorToHsv(c2) }

    var a1 by remember { mutableFloatStateOf(c1.alpha) }
    var h1 by remember { mutableFloatStateOf(hsv1Init[0] / 360f) }
    var s1 by remember { mutableFloatStateOf(hsv1Init[1]) }
    var v1 by remember { mutableFloatStateOf(hsv1Init[2]) }

    var a2 by remember { mutableFloatStateOf(c2.alpha) }
    var h2 by remember { mutableFloatStateOf(hsv2Init[0] / 360f) }
    var s2 by remember { mutableFloatStateOf(hsv2Init[1]) }
    var v2 by remember { mutableFloatStateOf(hsv2Init[2]) }

    var editingStartColor by remember { mutableStateOf(true) }

    val color1 = Color(android.graphics.Color.HSVToColor((a1 * 255).toInt(), floatArrayOf(h1 * 360f, s1, v1)))
    val color2 = Color(android.graphics.Color.HSVToColor((a2 * 255).toInt(), floatArrayOf(h2 * 360f, s2, v2)))

    val hex1 = String.format("#%02X%06X", (a1 * 255).toInt(), android.graphics.Color.HSVToColor(floatArrayOf(h1 * 360f, s1, v1)) and 0xFFFFFF)
    val hex2 = String.format("#%02X%06X", (a2 * 255).toInt(), android.graphics.Color.HSVToColor(floatArrayOf(h2 * 360f, s2, v2)) and 0xFFFFFF)

    val finalOutput = if (isGradientMode) "GRADIENT:$hex1,$hex2" else hex1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.custom_color_title, title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.gradient_mode), color = Color.White, fontSize = 14.sp)
                    Switch(checked = isGradientMode, onCheckedChange = { isGradientMode = it })
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isGradientMode) androidx.compose.ui.graphics.Brush.linearGradient(listOf(color1, color2))
                            else androidx.compose.ui.graphics.SolidColor(color1)
                        )
                        .border(2.dp, Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {}

                if (isGradientMode) {
                    TabRow(
                        selectedTabIndex = if (editingStartColor) 0 else 1,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(selected = editingStartColor, onClick = { editingStartColor = true }, text = { Text(stringResource(R.string.start_color), fontWeight = FontWeight.Bold) })
                        Tab(selected = !editingStartColor, onClick = { editingStartColor = false }, text = { Text(stringResource(R.string.end_color), fontWeight = FontWeight.Bold) })
                    }
                }

                if (!isGradientMode || editingStartColor) {
                    ColorSlider(stringResource(R.string.opacity_slider), a1, Color.LightGray) { a1 = it }
                    ColorSlider(stringResource(R.string.hue_slider), h1, color1) { h1 = it }
                    ColorSlider(stringResource(R.string.saturation_slider), s1, color1) { s1 = it }
                    ColorSlider(stringResource(R.string.brightness_slider), v1, Color.White) { v1 = it }
                } else {
                    ColorSlider(stringResource(R.string.opacity_slider), a2, Color.LightGray) { a2 = it }
                    ColorSlider(stringResource(R.string.hue_slider), h2, color2) { h2 = it }
                    ColorSlider(stringResource(R.string.saturation_slider), s2, color2) { s2 = it }
                    ColorSlider(stringResource(R.string.brightness_slider), v2, Color.White) { v2 = it }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(finalOutput) }) {
                Text(stringResource(R.string.done), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Color.Gray)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ColorSlider(label: String, value: Float, color: Color, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 13.sp)
            Text("${(value * 100).toInt()}%", color = Color.Gray, fontSize = 13.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color
            ),
            modifier = Modifier.height(36.dp)
        )
    }
}

@Composable
fun AnimatedResetButton(onClick: () -> Unit) {
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val animatedAngle by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 500),
        label = "ResetAnimation"
    )

    IconButton(
        onClick = {
            rotationAngle += 360f
            onClick()
        },
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_reset),
            contentDescription = "Reset",
            tint = Color.Gray,
            modifier = Modifier.rotate(animatedAngle)
        )
    }
}