package com.noxtan.noxboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.ui.components.CategoryPill
import com.noxtan.noxboard.ui.components.EffectPreviewCard
import com.noxtan.noxboard.ui.components.MockKeyboard
import com.noxtan.noxboard.ui.components.SwatchPreviewCard
import com.noxtan.noxboard.ui.models.CustomizerColorPreset
import androidx.core.graphics.ColorUtils
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeEditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }
    val scrollState = rememberScrollState()

    var activeSubTab by remember { mutableIntStateOf(0) }
    var showColorPicker by remember { mutableStateOf(false) }
    var initialColorForPicker by remember { mutableStateOf("#FFFFFF") }
    var selectedKeyGroup by remember { mutableStateOf("NORMAL") }
    var selectedTargetElement by remember { mutableStateOf("KEY_COLOR") }
    var selectedColorPresetId by remember { mutableStateOf("PRESET_1") }

    var currentBg by remember { mutableStateOf("#111111") }
    var currentKey by remember { mutableStateOf("#FFFDF0") }
    var currentText by remember { mutableStateOf("#111111") }
    var currentSpecKey by remember { mutableStateOf("#111111") }
    var currentSpecText by remember { mutableStateOf("#D4AF37") }
    var currentBorder by remember { mutableStateOf("#00000000") }
    var currentSpecBorder by remember { mutableStateOf("#00000000") }

    var currentPopupBg by remember { mutableStateOf(prefs.customPopupBgColor) }
    var currentPopupText by remember { mutableStateOf(prefs.customPopupTextColor) }
    var currentActiveKey by remember { mutableStateOf(prefs.customActiveKeyColor) }
    var isPreviewEnabled by remember { mutableStateOf(prefs.isKeyPreviewEnabled) }

    var currentSugText by remember {
        mutableStateOf(
            try {
                val json = org.json.JSONObject(prefs.individualKeyTextColors)
                if (json.has("Suggestion")) json.getString("Suggestion") else ""
            } catch (e: Exception) { "" }
        )
    }

    var undoStack by remember { mutableStateOf(listOf<ThemeSnapshot>()) }
    var redoStack by remember { mutableStateOf(listOf<ThemeSnapshot>()) }

    val currentSnapshot = ThemeSnapshot(currentBg, currentKey, currentText, currentSpecKey, currentSpecText, currentBorder, currentSpecBorder, currentPopupBg, currentPopupText, currentActiveKey, currentSugText)
    var initialSnapshot by remember { mutableStateOf(currentSnapshot) }

    var showSaveDialog by remember { mutableStateOf(false) }
    var themeNameInput by remember { mutableStateOf("") }
    var editingCustomThemeId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val editData = prefs.editingThemeId
        if (!editData.isNullOrEmpty() && editData.startsWith("{")) {
            try {
                val json = org.json.JSONObject(editData)

                val parsedId = json.optString("id", "")
                if (parsedId.startsWith("CUSTOM_")) {
                    editingCustomThemeId = parsedId
                    themeNameInput = json.optString("name", "")
                }

                currentBg = json.optString("bgHex", currentBg)
                currentKey = json.optString("keyHex", currentKey)
                currentText = json.optString("textHex", currentText)
                currentSpecKey = json.optString("specKeyHex", currentSpecKey)
                currentSpecText = json.optString("specTextHex", currentSpecText)
                currentBorder = json.optString("borderHex", "#00000000")
                currentSpecBorder = json.optString("specBorderHex", "#00000000")
                currentPopupBg = json.optString("popupBgHex", currentSpecKey)
                currentPopupText = json.optString("popupTextHex", currentSpecText)
                currentActiveKey = json.optString("activeKeyHex", currentSpecText)
                currentSugText = json.optString("sugTextHex", currentSpecText)

                initialSnapshot = ThemeSnapshot(currentBg, currentKey, currentText, currentSpecKey, currentSpecText, currentBorder, currentSpecBorder, currentPopupBg, currentPopupText, currentActiveKey, currentSugText)
                undoStack = emptyList()
                redoStack = emptyList()
                selectedColorPresetId = ""
            } catch(e: Exception) {}
            prefs.editingThemeId = ""
        }
    }

    val saveSnapshot = {
        undoStack = undoStack + currentSnapshot
        if (undoStack.size > 30) undoStack = undoStack.drop(1)
        redoStack = emptyList()
    }

    val applySnapshot = { snap: ThemeSnapshot ->
        currentBg = snap.bg
        currentKey = snap.key
        currentText = snap.text
        currentSpecKey = snap.specKey
        currentSpecText = snap.specText
        currentBorder = snap.border
        currentSpecBorder = snap.specBorder
        currentPopupBg = snap.popupBg
        currentPopupText = snap.popupText
        currentActiveKey = snap.activeKey
        currentSugText = snap.sugText
    }

    val colorPresets = listOf(
        CustomizerColorPreset("PRESET_1", "#111111", "#FFFDF0", "#111111", "#111111", "#D4AF37"),
        CustomizerColorPreset("PRESET_2", "#FCE4EC", "#FFFFFF", "#F06292", "#FCE4EC", "#F06292"),
        CustomizerColorPreset("PRESET_3", "#1976D2", "#42A5F5", "#FFD700", "#0D47A1", "#FFFFFF"),
        CustomizerColorPreset("PRESET_4", "#E1BEE7", "#F3E5F5", "#4A148C", "#CE93D8", "#FFFFFF"),
        CustomizerColorPreset("PRESET_5", "#B2EBF2", "#E0F7FA", "#006064", "#80DEEA", "#006064"),
        CustomizerColorPreset("PRESET_6", "#E8F5E9", "#FFFFFF", "#2E7D32", "#C8E6C9", "#2E7D32"),
        CustomizerColorPreset("PRESET_7", "#FFF3E0", "#FFFFFF", "#E65100", "#FFE0B2", "#E65100"),
        CustomizerColorPreset("PRESET_8", "#263238", "#37474F", "#FFFFFF", "#212121", "#80DEEA"),
        CustomizerColorPreset("PRESET_9", "#000000", "#1E1A2B", "#FFFFFF", "#12101C", "#877C4DFF"),
        CustomizerColorPreset("PRESET_10", "#EAEBEE", "#FFFFFF", "#000000", "#D2D4D9", "#000000"),
        CustomizerColorPreset("PRESET_11", "#3E2723", "#4E342E", "#FFF8E7", "#271511", "#FFF8E7"),
        CustomizerColorPreset("PRESET_12", "#11052C", "#3D087B", "#F43B86", "#11052C", "#FFE459"),
        CustomizerColorPreset("PRESET_13", "#F3E5F5", "#FFFFFF", "#6A1B9A", "#E1BEE7", "#4A148C"),
        CustomizerColorPreset("PRESET_14", "#121212", "#212121", "#FFFFFF", "#121212", "#FF1744"),
        CustomizerColorPreset("PRESET_15", "#0A192F", "#112240", "#E6F1FF", "#0A192F", "#64FFDA"),
        CustomizerColorPreset("PRESET_16", "#FFCC00", "#212121", "#FFD700", "#121212", "#00B0FF"),
        CustomizerColorPreset("PRESET_17", "#0D0221", "#261447", "#00FFCC", "#FF3864", "#FFFFFF"),
        CustomizerColorPreset("PRESET_18", "#3E2723", "#5D4037", "#D7CCC8", "#795548", "#FFC107"),
        CustomizerColorPreset("PRESET_19", "#212121", "#424242", "#FFFFFF", "#FF5722", "#FFEB3B"),
        CustomizerColorPreset("PRESET_20", "#120A2A", "#25144A", "#E0D4FF", "#7B2CBF", "#00FFD1"),
        CustomizerColorPreset("PRESET_21", "#E0F2F1", "#FFFFFF", "#004D40", "#B2DFDB", "#FF5252")
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { Text("Theme Creator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showSaveDialog = true }) {
                        Text("Save", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(android.graphics.Color.parseColor(currentBg))),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        com.noxtan.noxboard.NoxKeyboardView(ctx).apply {
                            applySettings(com.noxtan.noxboard.NoxBoardPrefs(ctx))
                            isEditMode = false
                            theme.bottomPaddingDp = 0f
                        }
                    },
                    update = { view ->
                        view.theme.bottomPaddingDp = 0f

                        view.theme.backgroundColor = android.graphics.Color.parseColor(currentBg)
                        view.theme.backgroundGradient = null
                        view.theme.keyColor = android.graphics.Color.parseColor(currentKey)
                        view.theme.keyGradient = null
                        view.theme.textColor = android.graphics.Color.parseColor(currentText)
                        view.theme.textGradient = null
                        view.theme.specialKeyColor = android.graphics.Color.parseColor(currentSpecKey)
                        view.theme.specialKeyGradient = null
                        view.theme.specialTextColor = android.graphics.Color.parseColor(currentSpecText)
                        view.theme.specialTextGradient = null

                        view.theme.keyBorderColor = android.graphics.Color.parseColor(currentBorder)
                        view.theme.specialKeyBorderColor = android.graphics.Color.parseColor(currentSpecBorder)

                        view.theme.popupBackgroundColor = android.graphics.Color.parseColor(currentPopupBg)
                        view.theme.popupTextColor = android.graphics.Color.parseColor(currentPopupText)
                        view.theme.popupKeyColor = android.graphics.Color.parseColor(currentKey)

                        view.theme.activeKeyColor = android.graphics.Color.parseColor(currentActiveKey)
                        view.theme.activeKeyGradient = null
                        view.theme.popupActiveKeyColor = android.graphics.Color.parseColor(currentActiveKey)

                        view.setIndividualColorsForPreview(
                            emptyMap(),
                            if (currentSugText.isNotEmpty()) mapOf("Suggestion" to currentSugText) else emptyMap(),
                            emptyMap(), emptyMap(), emptyMap()
                        )

                        view.initPaints()
                        view.requestLayout()
                        view.invalidate()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    ModernSegmentedTab("Colors", isSelected = activeSubTab == 0) { activeSubTab = 0 }
                    ModernSegmentedTab("Effects", isSelected = activeSubTab == 1) { activeSubTab = 1 }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (undoStack.isNotEmpty()) {
                                redoStack = redoStack + currentSnapshot
                                val prev = undoStack.last()
                                undoStack = undoStack.dropLast(1)
                                applySnapshot(prev)
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        enabled = undoStack.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo", tint = if (undoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.3f), modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = {
                            if (redoStack.isNotEmpty()) {
                                undoStack = undoStack + currentSnapshot
                                val next = redoStack.last()
                                redoStack = redoStack.dropLast(1)
                                applySnapshot(next)
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        enabled = redoStack.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Redo, contentDescription = "Redo", tint = if (redoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.3f), modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = {
                            if (currentSnapshot != initialSnapshot) {
                                saveSnapshot()
                                applySnapshot(initialSnapshot)
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        enabled = currentSnapshot != initialSnapshot
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = if (currentSnapshot != initialSnapshot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.3f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeSubTab) {
                    0 -> {
                        var selectedColorMode by remember { mutableIntStateOf(0) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryPill("Presets", isSelected = selectedColorMode == 0) { selectedColorMode = 0 }
                            CategoryPill("Detailed Colors", isSelected = selectedColorMode == 1) { selectedColorMode = 1 }
                        }

                        if (selectedColorMode == 0) {
                            val chunkedPresets = colorPresets.chunked(3)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                chunkedPresets.forEach { rowPresets ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowPresets.forEach { preset ->
                                            val isSelected = selectedColorPresetId == preset.id
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .border(
                                                        width = if (isSelected) 2.dp else 0.dp,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable {
                                                        if (isSelected) {
                                                            selectedColorPresetId = ""
                                                            if (undoStack.isNotEmpty()) {
                                                                redoStack = redoStack + currentSnapshot
                                                                val prev = undoStack.last()
                                                                undoStack = undoStack.dropLast(1)
                                                                applySnapshot(prev)
                                                            }
                                                        } else {
                                                            saveSnapshot()
                                                            selectedColorPresetId = preset.id
                                                            currentBg = preset.bgHex
                                                            currentKey = preset.keyHex
                                                            currentText = preset.textHex
                                                            currentSpecKey = preset.specKeyHex
                                                            currentSpecText = preset.specTextHex

                                                            currentBorder = "#00000000"
                                                            currentSpecBorder = "#00000000"
                                                        }
                                                    }
                                            ) {
                                                SwatchPreviewCard(preset = preset)
                                            }
                                        }
                                        if (rowPresets.size < 3) {
                                            repeat(3 - rowPresets.size) { Spacer(modifier = Modifier.weight(1f)) }
                                        }
                                    }
                                }
                            }
                        } else {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val groups = listOf("Letters" to "NORMAL", "Function" to "FUNCTION", "Background" to "BACKGROUND", "All" to "ALL")
                                groups.forEach { (label, value) ->
                                    val isSel = selectedKeyGroup == value
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                selectedKeyGroup = value
                                                if ((value == "BACKGROUND" || value == "ALL") && selectedTargetElement == "BORDER_COLOR") {
                                                    selectedTargetElement = "KEY_COLOR"
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val elements = when (selectedKeyGroup) {
                                    "BACKGROUND", "ALL" -> listOf("Key Color" to "KEY_COLOR", "Letter Color" to "TEXT_COLOR")
                                    "FUNCTION" -> listOf("Key Color" to "KEY_COLOR", "Letter Color" to "TEXT_COLOR", "Border" to "BORDER_COLOR", "Suggestion" to "SUG_TEXT_COLOR")
                                    else -> listOf("Key Color" to "KEY_COLOR", "Letter Color" to "TEXT_COLOR", "Border" to "BORDER_COLOR")
                                }
                                elements.forEach { (label, value) ->
                                    val isSel = selectedTargetElement == value
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .border(1.dp, if (isSel) Color.Transparent else MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                                            .clickable { selectedTargetElement = value }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            val solidColors = listOf(
                                "CUSTOM",
                                "#4A148C", "#7B1FA2", "#9C27B0", "#E1BEE7", "#F06292", "#E91E63",
                                "#880E4F", "#B71C1C", "#D32F2F", "#F44336", "#FF8A80", "#FFAB91",
                                "#FF7043", "#FF9800", "#FFB74D", "#FFE082", "#FFF9C4", "#FFEE58",
                                "#FFEB3B", "#CDDC39", "#4CAF50", "#81C784", "#A5D6A7", "#E8F5E9",
                                "#004D40", "#006064", "#00838F", "#00ACC1", "#4DD0E1", "#E0F7FA",
                                "#0D47A1", "#1976D2", "#2196F3", "#64B5F6", "#BBDEFB", "#ECEFF1"
                            )

                            val activeColorHex = if (selectedTargetElement == "BORDER_COLOR") {
                                when (selectedKeyGroup) {
                                    "NORMAL" -> currentBorder
                                    "FUNCTION" -> currentSpecBorder
                                    "ALL" -> currentBorder
                                    else -> currentBorder
                                }
                            } else if (selectedTargetElement == "SUG_TEXT_COLOR") {
                                currentSugText
                            } else {
                                when (selectedKeyGroup) {
                                    "NORMAL" -> if (selectedTargetElement == "KEY_COLOR") currentKey else currentText
                                    "FUNCTION" -> if (selectedTargetElement == "KEY_COLOR") currentSpecKey else currentSpecText
                                    "BACKGROUND" -> currentBg
                                    "ALL" -> if (selectedTargetElement == "KEY_COLOR") currentKey else currentText
                                    else -> "#FFFFFF"
                                }
                            }

                            val chunkedColors = solidColors.chunked(6)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                chunkedColors.forEach { rowColors ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowColors.forEach { hex ->
                                            if (hex == "CUSTOM") {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
                                                        .clip(CircleShape)
                                                        .background(
                                                            brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                                                                listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                                                            )
                                                        )
                                                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                                        .clickable {
                                                            initialColorForPicker = if (activeColorHex.isEmpty()) currentSpecText else activeColorHex
                                                            showColorPicker = true
                                                        }
                                                )
                                            } else {
                                                val colorVal = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Transparent }
                                                val isColorSelected = hex.equals(activeColorHex, ignoreCase = true)

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
                                                        .clip(CircleShape)
                                                        .background(colorVal)
                                                        .border(
                                                            width = if (isColorSelected) 3.dp else 1.dp,
                                                            color = if (isColorSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
                                                            shape = CircleShape
                                                        )
                                                        .clickable {
                                                            if (isColorSelected) {
                                                                if (undoStack.isNotEmpty()) {
                                                                    redoStack = redoStack + currentSnapshot
                                                                    val prev = undoStack.last()
                                                                    undoStack = undoStack.dropLast(1)
                                                                    applySnapshot(prev)
                                                                }
                                                            } else {
                                                                saveSnapshot()
                                                                if (selectedTargetElement == "BORDER_COLOR") {
                                                                    when (selectedKeyGroup) {
                                                                        "NORMAL" -> currentBorder = hex
                                                                        "FUNCTION" -> currentSpecBorder = hex
                                                                        "ALL" -> {
                                                                            currentBorder = hex
                                                                            currentSpecBorder = hex
                                                                        }
                                                                    }
                                                                } else if (selectedTargetElement == "SUG_TEXT_COLOR") {
                                                                    currentSugText = hex
                                                                } else {
                                                                    when (selectedKeyGroup) {
                                                                        "NORMAL" -> {
                                                                            if (selectedTargetElement == "KEY_COLOR") currentKey = hex
                                                                            else if (selectedTargetElement == "TEXT_COLOR") currentText = hex
                                                                        }
                                                                        "FUNCTION" -> {
                                                                            if (selectedTargetElement == "KEY_COLOR") currentSpecKey = hex
                                                                            else if (selectedTargetElement == "TEXT_COLOR") currentSpecText = hex
                                                                        }
                                                                        "BACKGROUND" -> {
                                                                            currentBg = hex
                                                                        }
                                                                        "ALL" -> {
                                                                            if (selectedTargetElement == "KEY_COLOR") {
                                                                                currentKey = hex; currentSpecKey = hex
                                                                            } else if (selectedTargetElement == "TEXT_COLOR") {
                                                                                currentText = hex; currentSpecText = hex
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isColorSelected) {
                                                        val colorInt = android.graphics.Color.parseColor(hex)
                                                        val darkness = 1 - (0.299 * android.graphics.Color.red(colorInt) + 0.587 * android.graphics.Color.green(colorInt) + 0.114 * android.graphics.Color.blue(colorInt)) / 255
                                                        val iconTint = if (darkness < 0.5) Color.Black else Color.White
                                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = iconTint, modifier = Modifier.size(20.dp))
                                                    }
                                                }
                                            }
                                        }

                                        if (rowColors.size < 6) {
                                            repeat(6 - rowColors.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        var selectedEffectTab by remember { mutableIntStateOf(0) }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryPill(
                                "Key Popup",
                                isSelected = selectedEffectTab == 0
                            ) { selectedEffectTab = 0 }

                            CategoryPill(
                                "Key Motion",
                                isSelected = selectedEffectTab == 1
                            ) { selectedEffectTab = 1 }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (selectedEffectTab == 0) {

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Pressed Key Color", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ColorPickerButton(
                                        label = "Pressed Key",
                                        colorHex = currentActiveKey,
                                        onClick = {
                                            selectedTargetElement = "ACTIVE_KEY"
                                            initialColorForPicker = currentActiveKey
                                            showColorPicker = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Key Popup Preview", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                            Text("Show a magnified key when pressed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(
                                            checked = isPreviewEnabled,
                                            onCheckedChange = {
                                                isPreviewEnabled = it
                                                prefs.isKeyPreviewEnabled = it
                                            }
                                        )
                                    }

                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = isPreviewEnabled,
                                        enter = androidx.compose.animation.expandVertically(expandFrom = Alignment.Top) + androidx.compose.animation.fadeIn(),
                                        exit = androidx.compose.animation.shrinkVertically(shrinkTowards = Alignment.Top) + androidx.compose.animation.fadeOut()
                                    ) {
                                        Column {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                ColorPickerButton(
                                                    label = "Popup BG",
                                                    colorHex = currentPopupBg,
                                                    onClick = {
                                                        selectedTargetElement = "POPUP_BG"
                                                        initialColorForPicker = currentPopupBg
                                                        showColorPicker = true
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                ColorPickerButton(
                                                    label = "Popup Text",
                                                    colorHex = currentPopupText,
                                                    onClick = {
                                                        selectedTargetElement = "POPUP_TEXT"
                                                        initialColorForPicker = currentPopupText
                                                        showColorPicker = true
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            var currentKeyMotion by remember { mutableStateOf(prefs.keyMotionStyle) }
                            val motions = listOf(
                                "NONE" to "Normal",
                                "SHRINK" to "Shrink",
                                "POP" to "Pop Up",
                                "TILT" to "Tilt",
                                "SINK" to "Sink",
                                "SQUISH" to "Squish",
                                "FLOAT" to "Float",
                                "SKEW" to "Skew"
                            )

                            val chunkedMotions = motions.chunked(3)
                            chunkedMotions.forEach { rowMotions ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    rowMotions.forEach { (id, name) ->
                                        val isSel = currentKeyMotion == id
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                                                .border(1.5.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    if (isSel) {
                                                        currentKeyMotion = "NONE"
                                                        prefs.keyMotionStyle = "NONE"
                                                    } else {
                                                        currentKeyMotion = id
                                                        prefs.keyMotionStyle = id
                                                    }
                                                }
                                                .padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(name, fontSize = 13.sp, fontWeight = if(isSel) FontWeight.Bold else FontWeight.Medium, color = if(isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                    if (rowMotions.size < 3) {
                                        repeat(3 - rowMotions.size) { Spacer(modifier = Modifier.weight(1f)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showColorPicker) {
        SimpleColorPickerDialog(
            initialHex = initialColorForPicker,
            showAlpha = selectedKeyGroup != "BACKGROUND" && selectedKeyGroup != "ALL",
            onDismiss = { showColorPicker = false },
            onColorSelected = { hex ->
                saveSnapshot()
                if (selectedTargetElement == "POPUP_BG") {
                    currentPopupBg = hex
                } else if (selectedTargetElement == "POPUP_TEXT") {
                    currentPopupText = hex
                } else if (selectedTargetElement == "ACTIVE_KEY") {
                    currentActiveKey = hex
                } else if (selectedTargetElement == "SUG_TEXT_COLOR") {
                    currentSugText = hex
                } else if (selectedTargetElement == "BORDER_COLOR") {
                    when (selectedKeyGroup) {
                        "NORMAL" -> currentBorder = hex
                        "FUNCTION" -> currentSpecBorder = hex
                        "ALL" -> {
                            currentBorder = hex
                            currentSpecBorder = hex
                        }
                    }
                } else {
                    when (selectedKeyGroup) {
                        "NORMAL" -> {
                            if (selectedTargetElement == "KEY_COLOR") currentKey = hex
                            else if (selectedTargetElement == "TEXT_COLOR") currentText = hex
                        }
                        "FUNCTION" -> {
                            if (selectedTargetElement == "KEY_COLOR") currentSpecKey = hex
                            else if (selectedTargetElement == "TEXT_COLOR") currentSpecText = hex
                        }
                        "BACKGROUND" -> {
                            currentBg = hex
                        }
                        "ALL" -> {
                            if (selectedTargetElement == "KEY_COLOR") {
                                currentKey = hex; currentSpecKey = hex
                            } else if (selectedTargetElement == "TEXT_COLOR") {
                                currentText = hex; currentSpecText = hex
                            }
                        }
                    }
                }
                showColorPicker = false
            }
        )
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Custom Theme", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                OutlinedTextField(
                    value = themeNameInput,
                    onValueChange = { themeNameInput = it },
                    label = { Text("Theme Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val finalName = if (themeNameInput.isBlank()) "My Custom Theme" else themeNameInput
                        val themeId = if (editingCustomThemeId.isNotEmpty()) editingCustomThemeId else "CUSTOM_${System.currentTimeMillis()}"

                        val jsonObj = org.json.JSONObject().apply {
                            put("id", themeId)
                            put("name", finalName)
                            put("bgHex", currentBg)
                            put("keyHex", currentKey)
                            put("textHex", currentText)
                            put("specKeyHex", currentSpecKey)
                            put("specTextHex", currentSpecText)
                            put("borderHex", currentBorder)
                            put("specBorderHex", currentSpecBorder)
                            put("popupBgHex", currentPopupBg)
                            put("popupTextHex", currentPopupText)
                            put("activeKeyHex", currentActiveKey)
                            put("sugTextHex", currentSugText)
                        }

                        val savedArr = try { org.json.JSONArray(prefs.savedCustomThemes) } catch(e: Exception) { org.json.JSONArray() }

                        var isUpdated = false
                        for (i in 0 until savedArr.length()) {
                            val obj = savedArr.getJSONObject(i)
                            if (obj.optString("id") == themeId) {
                                savedArr.put(i, jsonObj)
                                isUpdated = true
                                break
                            }
                        }
                        if (!isUpdated) {
                            savedArr.put(jsonObj)
                        }

                        prefs.savedCustomThemes = savedArr.toString()

                        prefs.activeTheme = themeId
                        prefs.customBackgroundColor = currentBg
                        prefs.customKeyColor = currentKey
                        prefs.customTextColor = currentText
                        prefs.customSpecialKeyColor = currentSpecKey
                        prefs.customSpecialTextColor = currentSpecText
                        prefs.customKeyBorderColor = currentBorder
                        prefs.customSpecialKeyBorderColor = currentSpecBorder
                        prefs.customPopupBgColor = currentPopupBg
                        prefs.customPopupTextColor = currentPopupText
                        prefs.customActiveKeyColor = currentActiveKey

                        prefs.customNumpadBgColor = currentBg
                        prefs.customNumpadKeyColor = currentKey
                        prefs.customNumpadTextColor = currentText
                        prefs.customNumpadSpecialKeyColor = currentSpecKey
                        prefs.customNumpadSpecialTextColor = currentSpecText

                        val jsonText = try { org.json.JSONObject(prefs.individualKeyTextColors) } catch(e: Exception) { org.json.JSONObject() }
                        if (currentSugText.isNotEmpty()) jsonText.put("Suggestion", currentSugText) else jsonText.remove("Suggestion")
                        prefs.individualKeyTextColors = jsonText.toString()

                        showSaveDialog = false
                        onBack()
                    }
                ) { Text("Save", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

data class ThemeSnapshot(
    val bg: String, val key: String, val text: String,
    val specKey: String, val specText: String,
    val border: String, val specBorder: String,
    val popupBg: String, val popupText: String, val activeKey: String,
    val sugText: String
)

@Composable
fun SimpleColorPickerDialog(
    initialHex: String,
    showAlpha: Boolean = true,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val initialColor = try { android.graphics.Color.parseColor(initialHex) } catch (e: Exception) { android.graphics.Color.WHITE }

    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(initialColor, hsl)

    var hue by remember { mutableFloatStateOf(hsl[0]) }
    var saturation by remember { mutableFloatStateOf(hsl[1]) }
    var lightness by remember { mutableFloatStateOf(hsl[2]) }
    var alpha by remember { mutableFloatStateOf(if (showAlpha) android.graphics.Color.alpha(initialColor) / 255f else 1f) }

    val pureHueColor = Color(ColorUtils.HSLToColor(floatArrayOf(hue, 1f, 0.5f)))
    val currentColorInt = ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness))
    val finalColorInt = android.graphics.Color.argb(
        (alpha * 255).toInt(),
        android.graphics.Color.red(currentColorInt),
        android.graphics.Color.green(currentColorInt),
        android.graphics.Color.blue(currentColorInt)
    )
    val currentColor = Color(finalColorInt)
    val hexString = String.format("#%08X", finalColorInt)

    val isLight = (android.graphics.Color.red(currentColorInt) * 0.299 +
            android.graphics.Color.green(currentColorInt) * 0.587 +
            android.graphics.Color.blue(currentColorInt) * 0.114) > 150
    val textColor = if (isLight) Color.Black else Color.White

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Custom Color", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(currentColor)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(hexString, color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                VisualSlider(
                    label = "Color (Hue)",
                    value = hue,
                    valueRange = 0f..360f,
                    onValueChange = { hue = it },
                    backgroundBrush = Brush.horizontalGradient(
                        listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                    )
                )

                VisualSlider(
                    label = "Brightness",
                    value = lightness,
                    valueRange = 0f..1f,
                    onValueChange = { lightness = it },
                    backgroundBrush = Brush.horizontalGradient(
                        listOf(Color.Black, pureHueColor, Color.White)
                    )
                )

                VisualSlider(
                    label = "Vibrancy",
                    value = saturation,
                    valueRange = 0f..1f,
                    onValueChange = { saturation = it },
                    backgroundBrush = Brush.horizontalGradient(
                        listOf(Color.Gray, pureHueColor)
                    )
                )

                if (showAlpha) {
                    VisualSlider(
                        label = "Transparency (Alpha)",
                        value = alpha,
                        valueRange = 0f..1f,
                        onValueChange = { alpha = it },
                        backgroundBrush = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(currentColorInt))
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(hexString) }) {
                Text("Apply", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun VisualSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    backgroundBrush: Brush
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(top = 4.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.LightGray)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(backgroundBrush))
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun ModernSegmentedTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ColorPickerButton(label: String, colorHex: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}