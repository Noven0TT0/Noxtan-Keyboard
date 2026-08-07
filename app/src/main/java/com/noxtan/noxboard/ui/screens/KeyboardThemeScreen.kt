package com.noxtan.noxboard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.drawWithCache
import com.noxtan.noxboard.utils.ThemePacker
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import com.noxtan.noxboard.R

fun String.toColorSafe(fallback: Color = Color.Black): Color {
    return try { Color(android.graphics.Color.parseColor(this)) } catch (e: Exception) { fallback }
}

fun String.toBrushSafe(fallbackColor: Color = Color.Black): androidx.compose.ui.graphics.Brush {
    return if (this.startsWith("GRADIENT:")) {
        try {
            val colors = this.removePrefix("GRADIENT:").split(",").map {
                Color(android.graphics.Color.parseColor(it))
            }
            androidx.compose.ui.graphics.Brush.linearGradient(colors)
        } catch (e: Exception) {
            androidx.compose.ui.graphics.SolidColor(fallbackColor)
        }
    } else {
        val solid = try { Color(android.graphics.Color.parseColor(this)) } catch (e: Exception) { fallbackColor }
        androidx.compose.ui.graphics.SolidColor(solid)
    }
}

fun getContrastColor(hex: String, fallback: Color = Color.White): Color {
    if (hex.startsWith("GRADIENT:")) return Color.White
    val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { return fallback }
    return if ((c.red * 0.299 + c.green * 0.587 + c.blue * 0.114) > 0.5f) Color.Black else Color.White
}

@Composable
fun KeyboardThemeScreen(
    onBack: () -> Unit,
    onCreateCustomTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.activity.compose.BackHandler(enabled = true) {
        onBack()
    }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }

    var cachedTopPadding by remember { mutableStateOf(0.dp) }
    val currentTopPadding = androidx.compose.foundation.layout.WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    if (currentTopPadding > 0.dp) {
        cachedTopPadding = currentTopPadding
    }

    var activeTheme by remember { mutableStateOf(prefs.activeTheme) }

    var customThemes by remember {
        mutableStateOf(
            org.json.JSONArray(prefs.savedCustomThemes).let { arr ->
                val list = mutableListOf<org.json.JSONObject>()
                for (i in 0 until arr.length()) list.add(arr.getJSONObject(i))
                list
            }
        )
    }

    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

    val targetIndex = remember(activeTheme, customThemes) {
        if (activeTheme == "DEFAULT") {
            1
        } else {
            val foundIndex = customThemes.indexOfFirst { it.optString("id") == activeTheme }
            if (foundIndex != -1) {
                foundIndex + 2
            } else {
                0
            }
        }
    }

    LaunchedEffect(targetIndex) {
        if (targetIndex > 0) {
            try {
                lazyListState.animateScrollToItem(targetIndex)
            } catch (e: Exception) {
                lazyListState.scrollToItem(targetIndex)
            }
        }
    }

    var themeToDelete by remember { mutableStateOf<String?>(null) }
    var themeToExport by remember { mutableStateOf<org.json.JSONObject?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val imported = ThemePacker.importTheme(context, uri)
            if (imported != null) {
                val importedJson = org.json.JSONObject(imported.themeJson)
                val newId = "theme_" + System.currentTimeMillis()
                importedJson.put("id", newId)

                val rawName = importedJson.optString("name", "Custom Theme")
                val baseName = rawName.replace(" (Imported)", "").trim()

                val existingThemes = org.json.JSONArray(prefs.savedCustomThemes)
                val existingNames = mutableSetOf<String>()
                for (i in 0 until existingThemes.length()) {
                    existingNames.add(existingThemes.getJSONObject(i).optString("name"))
                }

                var finalName = baseName
                while (existingNames.contains(finalName)) {
                    val regex = Regex("^(.*?)\\s*(\\d+)\$")
                    val matchResult = regex.matchEntire(finalName)
                    finalName = if (matchResult != null) {
                        val base = matchResult.groups[1]?.value ?: ""
                        val numStr = matchResult.groups[2]?.value ?: "1"
                        val nextNum = numStr.toInt() + 1
                        "${base.trimEnd()} $nextNum"
                    } else {
                        "$finalName 2"
                    }
                }

                importedJson.put("name", finalName)

                val themesArray = org.json.JSONArray(prefs.savedCustomThemes)
                themesArray.put(importedJson)
                prefs.savedCustomThemes = themesArray.toString()

                val list = mutableListOf<org.json.JSONObject>()
                for (i in 0 until themesArray.length()) list.add(themesArray.getJSONObject(i))
                customThemes = list

                android.widget.Toast.makeText(context, context.getString(R.string.import_success), android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(context, context.getString(R.string.import_failed), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = cachedTopPadding)
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.my_themes),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    importLauncher.launch(arrayOf("*/*"))
                }) {
                    Icon(Icons.Default.Download, contentDescription = "Import Theme", tint = Color.White)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 600.dp)
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "My Themes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                val currentThemesList = customThemes

                LazyRow(
                    state = lazyListState,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        val stroke = Stroke(
                            width = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        )
                        val primaryColor = MaterialTheme.colorScheme.primary

                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    prefs.editingThemeId = null
                                    onCreateCustomTheme()
                                }
                                .drawBehind {
                                    drawRoundRect(
                                        color = primaryColor.copy(alpha = 0.6f),
                                        style = stroke,
                                        cornerRadius = CornerRadius(12.dp.toPx())
                                    )
                                }
                                .background(primaryColor.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, contentDescription = "Create Theme", tint = primaryColor, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(stringResource(R.string.create_new_theme), color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    item {
                        ThemePreviewCard(
                            title = stringResource(R.string.default_theme),
                            isSelected = activeTheme == "DEFAULT",
                            bgHex = "#000000",
                            keyHex = "#1E1A2B",
                            textHex = "#FFFFFF",
                            specialKeyHex = "#12101C",
                            specialTextHex = "#FFFFFF",
                            useCustomIconColors = false,
                            wallpaperUri = null,
                            onClick = {
                                prefs.customBackgroundColor = "#000000"
                                prefs.customKeyColor = "#1E1A2B"
                                prefs.customTextColor = "#FFFFFF"
                                prefs.customSpecialKeyColor = "#12101C"
                                prefs.customSpecialTextColor = "#FFFFFF"
                                prefs.customKeyBorderColor = "#00000000"
                                prefs.customActiveKeyColor = "#7C4DFF"
                                prefs.customPopupBgColor = "#1F1F1F"
                                prefs.customPopupTextColor = "#FFFFFF"
                                prefs.useCustomIconColors = false
                                prefs.customWallpaperUri = null
                                prefs.individualKeyColors = "{}"
                                prefs.individualKeyTextColors = "{}"
                                prefs.individualKeyActiveColors = "{}"
                                prefs.individualKeyPopupBgColors = "{}"
                                prefs.individualKeyPopupTextColors = "{}"

                                prefs.activeTheme = "DEFAULT"
                                activeTheme = "DEFAULT"
                            }
                        )
                    }

                    items(currentThemesList.size) { index ->
                        val t = currentThemesList[index]
                        val tId = t.optString("id")
                        val wUri = t.optString("wallUri", "")

                        val loadThemeToPrefs = {
                            prefs.customNumpadSpecialKeyColor = t.optString("numpadSpecialKey", "#12101C")
                            prefs.customNumpadSpecialTextColor = t.optString("numpadSpecialText", "#FFFFFF")
                            prefs.customBackgroundColor = t.optString("bg", "#000000")
                            prefs.customKeyColor = t.optString("key", "#1E1A2B")
                            prefs.customTextColor = t.optString("text", "#FFFFFF")
                            prefs.customSpecialKeyColor = t.optString("specialKey", "#12101C")
                            prefs.customSpecialTextColor = t.optString("specialText", "#FFFFFF")
                            prefs.customKeyBorderColor = t.optString("keyBorder", "#00000000")
                            prefs.customActiveKeyColor = t.optString("activeKey", "#7C4DFF")
                            prefs.customPopupBgColor = t.optString("popupBg", "#12101C")
                            prefs.customPopupTextColor = t.optString("popupText", "#FFFFFF")
                            prefs.customNumpadBgColor = t.optString("numpadBg", "#000000")
                            prefs.customNumpadKeyColor = t.optString("numpadKey", "#1E1A2B")
                            prefs.customNumpadTextColor = t.optString("numpadText", "#FFFFFF")
                            prefs.useCustomIconColors = t.optBoolean("iconTint", false)
                            prefs.isTextureEffectEnabled = t.optBoolean("texture", false)
                            prefs.isTextureEffectEnabled = t.optBoolean("texture", false)
                            val wUri = t.optString("wallUri", "")
                            prefs.customWallpaperUri = if (wUri.isEmpty()) null else wUri
                            prefs.customWallpaperScale = t.optDouble("wallScale", 1.0).toFloat()
                            prefs.customWallpaperOffsetX = t.optDouble("wallX", 0.0).toFloat()
                            prefs.customWallpaperOffsetY = t.optDouble("wallY", 0.0).toFloat()
                            prefs.individualKeyColors = t.optString("ind_colors", "{}")
                            prefs.individualKeyTextColors = t.optString("ind_texts", "{}")
                            prefs.individualKeyActiveColors = t.optString("ind_active", "{}")
                            prefs.individualKeyPopupBgColors = t.optString("ind_popBg", "{}")
                            prefs.individualKeyPopupTextColors = t.optString("ind_popText", "{}")
                        }

                        ThemePreviewCard(
                            title = t.optString("name"),
                            isSelected = activeTheme == tId,
                            bgHex = t.optString("bg", "#000000"),
                            keyHex = t.optString("key", "#1E1A2B"),
                            textHex = t.optString("text", "#FFFFFF"),
                            specialKeyHex = t.optString("specialKey", "#12101C"),
                            specialTextHex = t.optString("specialText", "#FFFFFF"),
                            individualColorsJson = t.optString("ind_colors", "{}"),
                            individualTextsJson = t.optString("ind_texts", "{}"),
                            useCustomIconColors = t.optBoolean("iconTint", false),
                            wallpaperUri = if (wUri.isEmpty()) null else wUri,
                            onClick = {
                                loadThemeToPrefs()
                                prefs.activeTheme = tId
                                activeTheme = tId
                            },
                            onEdit = {
                                loadThemeToPrefs()
                                prefs.activeTheme = tId
                                activeTheme = tId
                                prefs.editingThemeId = tId
                                onCreateCustomTheme()
                            },
                            onDelete = {
                                themeToDelete = tId
                            },
                            onShare = {
                                val safeName = t.optString("name", "NoxTheme").replace(Regex("[^a-zA-Z0-9_-]"), "_")

                                val file = com.noxtan.noxboard.utils.ThemePacker.exportTheme(
                                    context = context,
                                    themeJson = t.toString(),
                                    wallpaperUriString = t.optString("wallUri", ""),
                                    themeName = safeName
                                )

                                if (file != null) {
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

                                    context.startActivity(android.content.Intent.createChooser(shareIntent, context.getString(R.string.share_theme_via)))
                                } else {
                                    android.widget.Toast.makeText(context, context.getString(R.string.share_theme_failed), android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {

                data class PresetInfo(
                    val name: String,
                    val assetFileName: String?,
                    val previewBg: String,
                    val previewKey: String,
                    val previewSpecial: String,
                    val activeKeyColor: String = "#7C4DFF",
                    val textHex: String? = null,
                    val specialTextHex: String? = null,
                    val previewWallpaper: String? = null
                )

                val categorizedPresets = listOf(
                    "Flat & Borderless" to listOf(
                        PresetInfo("Flat Light", null, "#FFFFFF", "#FFFFFF", "#FFFFFF", "#4A90E2"),
                        PresetInfo("Flat Dark", null, "#1A1A1A", "#1A1A1A", "#1A1A1A", "#7C4DFF"),
                        PresetInfo("Flat AMOLED", null, "#000000", "#000000", "#000000", "#03DAC6"),
                        PresetInfo("Flat Ocean", null, "#0D47A1", "#0D47A1", "#0D47A1", "#64B5F6"),
                        PresetInfo("Flat Crimson", null, "#690000", "#690000", "#690000", "#FF5252")
                    ),
                    "Uniform Monotone" to listOf(
                        PresetInfo("Uniform Gray", null, "#E0E0E0", "#F5F5F5", "#F5F5F5", "#757575"),
                        PresetInfo("Uniform Charcoal", null, "#121212", "#212121", "#212121", "#BDBDBD"),
                        PresetInfo("Uniform Navy", null, "#0A192F", "#112240", "#112240", "#4EA8DE"),
                        PresetInfo("Uniform Brown", null, "#3E2723", "#4E342E", "#4E342E", "#D7CCC8")
                    ),
                    "Gradient Vibes" to listOf(
                        PresetInfo("Cosmic Purple", null, "GRADIENT:#9D50BB,#6E48AA", "#1A1A1A", "#2D2D2D", "#00E5FF"),
                        PresetInfo("Sunset Orange", null, "GRADIENT:#F5AF19,#F12711", "#1A1A1A", "#2D2D2D", "#FFD700"),
                        PresetInfo("Ocean Wave", null, "GRADIENT:#00C6FF,#0072FF", "#0A192F", "#112240", "#00FFFF"),
                        PresetInfo("Minty Flow", null, "GRADIENT:#11998E,#38EF7D", "#061A14", "#0D3326", "#FFFFFF"),
                        PresetInfo("Cherry Skies", null, "GRADIENT:#EB3349,#F45C43", "#1A0505", "#330A0A", "#FFD700")
                    ),
                    "High Contrast" to listOf(
                        PresetInfo("Bumblebee", null, "#FFD600", "#111111", "#000000", activeKeyColor = "#FFD600", textHex = "#FFD600", specialTextHex = "#FFD600"),
                        PresetInfo("Hacker Terminal", null, "#000000", "#000000", "#002200", activeKeyColor = "#00FF00", textHex = "#00FF00", specialTextHex = "#00FF00"),
                        PresetInfo("Crimson Black", null, "#000000", "#D50000", "#111111", activeKeyColor = "#FF5252")
                    ),
                    "Sweet & Candy" to listOf(
                        PresetInfo("Cotton Candy", null, "#FFCDD2", "#F8BBD0", "#E1BEE7", "#FF4081"),
                        PresetInfo("Macaron Yellow", null, "#FFF9C4", "#FFE0B2", "#FFCC80", "#FF9800"),
                        PresetInfo("Peach Pink", null, "#FCE4EC", "#F8BBD0", "#F48FB1", "#E91E63")
                    ),
                    "Minimal & Classic" to listOf(
                        PresetInfo("Light Clean", null, "#F2F3F4", "#FFFFFF", "#E0E0E0", "#4A90E2"),
                        PresetInfo("OLED Midnight", null, "#000000", "#121212", "#2A2A2A", "#7C4DFF"),
                        PresetInfo("Pixel Blue", null, "#ECEFF1", "#FFFFFF", "#CFD8DC", "#1A73E8"),
                        PresetInfo("iOS Light", null, "#D1D5DB", "#FFFFFF", "#BFC5CE", "#007AFF"),
                        PresetInfo("iOS Dark", null, "#383838", "#5A5A5E", "#4A4A4C", "#0A84FF")
                    ),
                    "Nature & Earth" to listOf(
                        PresetInfo("Deep Ocean", null, "#154360", "#2980B9", "#1A5276", "#5DADE2"),
                        PresetInfo("Emerald Forest", null, "#061A14", "#0D3326", "#2EC4B6", "#FF9F1C"),
                        PresetInfo("Slate Rock", null, "#263238", "#37474F", "#455A64", "#81D4FA"),
                        PresetInfo("Coffee Brown", null, "#3E2723", "#4E342E", "#5D4037", "#D7CCC8"),
                        PresetInfo("Mint Fresh", null, "#12201D", "#1D3530", "#57CC99", "#80ED99")
                    ),
                    "Cyber & Neon" to listOf(
                        PresetInfo("Neon Pink", null, "#FF4081", "#F50057", "#C51162", "#00E5FF"),
                        PresetInfo("Cyberpunk Neon", null, "#0D0814", "#261533", "#00E5FF", "#FF007F"),
                        PresetInfo("Synthwave", null, "#2B0B3F", "#57186A", "#FF007F", "#00FFFF"),
                        PresetInfo("Matrix Green", null, "#051405", "#0A290A", "#00FF41", "#FFFFFF")
                    ),
                    "Premium & Luxury" to listOf(
                        PresetInfo("My Dark Theme", null, "#1A1A1A", "#333333", "#7C4DFF", "#B388FF"),
                        PresetInfo("Gold Luxe", null, "#141414", "#2B2B2B", "#FFD700", "#FFFFFF"),
                        PresetInfo("Purple Haze", null, "#160C28", "#2E1A47", "#BB86FC", "#03DAC6"),
                        PresetInfo("Dracula Dark", null, "#282A36", "#44475A", "#BD93F9", "#FF79C6"),
                        PresetInfo("Sakura Dark", null, "#1E1822", "#352A3C", "#FF85A1", "#FFAFCC"),
                        PresetInfo("Crimson Velvet", null, "#1A0505", "#330A0A", "#D4AF37", "#FFDF00"),
                        PresetInfo("Midnight Navy", null, "#0A1128", "#14213D", "#FCA311", "#E5E5E5")
                    )
                )

                val lightThemes = setOf(
                    "Light Clean", "Pixel Blue", "iOS Light", "Flat Light", "Uniform Gray",
                    "Bumblebee", "Cotton Candy", "Macaron Yellow", "Peach Pink"
                )

                categorizedPresets.forEach { (categoryName, presets) ->
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = categoryName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(presets.size) { index ->
                                val preset = presets[index]
                                val presetId = "PRESET_${preset.name}"
                                val isLightTheme = lightThemes.contains(preset.name)

                                val textColor = preset.textHex ?: if (isLightTheme) "#000000" else "#FFFFFF"
                                val specialTextColor = preset.specialTextHex ?: if (isLightTheme) "#000000" else "#FFFFFF"

                                ThemePreviewCard(
                                    title = preset.name,
                                    isSelected = activeTheme == presetId,
                                    bgHex = preset.previewBg,
                                    keyHex = preset.previewKey,
                                    textHex = textColor,
                                    specialKeyHex = preset.previewSpecial,
                                    specialTextHex = specialTextColor,
                                    individualColorsJson = "{}",
                                    individualTextsJson = "{}",
                                    useCustomIconColors = false,
                                    wallpaperUri = null,
                                    wallpaperAssetPath = preset.assetFileName?.let { "presets/$it" },
                                    onClick = {
                                        if (preset.assetFileName != null) {
                                            try {
                                                val tempFile = java.io.File(context.cacheDir, preset.assetFileName)
                                                context.assets.open("presets/${preset.assetFileName}").use { input ->
                                                    java.io.FileOutputStream(tempFile).use { output -> input.copyTo(output) }
                                                }

                                                val uri = android.net.Uri.fromFile(tempFile)
                                                val imported = com.noxtan.noxboard.utils.ThemePacker.importTheme(context, uri)

                                                if (imported != null) {
                                                    val t = org.json.JSONObject(imported.themeJson)
                                                    prefs.customBackgroundColor = t.optString("bg", "#000000")
                                                    prefs.customKeyColor = t.optString("key", "#1E1A2B")
                                                    prefs.customTextColor = t.optString("text", "#FFFFFF")
                                                    prefs.customSpecialKeyColor = t.optString("specialKey", "#12101C")
                                                    prefs.customSpecialTextColor = t.optString("specialText", "#FFFFFF")
                                                    prefs.customKeyBorderColor = t.optString("keyBorder", "#00000000")
                                                    prefs.customActiveKeyColor = t.optString("activeKey", "#7C4DFF")
                                                    prefs.customPopupBgColor = t.optString("popupBg", "#12101C")
                                                    prefs.customPopupTextColor = t.optString("popupText", "#FFFFFF")
                                                    prefs.useCustomIconColors = t.optBoolean("iconTint", false)
                                                    prefs.customNumpadBgColor = if (isLightTheme) "#F2F3F4" else "#1F1F1F"
                                                    prefs.customNumpadKeyColor = if (isLightTheme) "#FFFFFF" else "#2D2D2D"
                                                    prefs.customNumpadTextColor = if (isLightTheme) "#000000" else "#FFFFFF"
                                                    prefs.customNumpadSpecialKeyColor = if (isLightTheme) "#E0E0E0" else "#3A3A3C"
                                                    prefs.customNumpadSpecialTextColor = if (isLightTheme) "#000000" else "#FFFFFF"

                                                    if (imported.wallpaperPath != null) {
                                                        prefs.customWallpaperUri = imported.wallpaperPath
                                                        prefs.customWallpaperScale = t.optDouble("wallScale", 1.0).toFloat()
                                                        prefs.customWallpaperOffsetX = t.optDouble("wallX", 0.0).toFloat()
                                                        prefs.customWallpaperOffsetY = t.optDouble("wallY", 0.0).toFloat()
                                                    } else {
                                                        prefs.customWallpaperUri = null
                                                    }

                                                    prefs.individualKeyColors = t.optString("ind_colors", "{}")
                                                    prefs.individualKeyTextColors = t.optString("ind_texts", "{}")
                                                    prefs.individualKeyActiveColors = t.optString("ind_active", "{}")
                                                    prefs.individualKeyPopupBgColors = t.optString("ind_popBg", "{}")
                                                    prefs.individualKeyPopupTextColors = t.optString("ind_popText", "{}")
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                com.noxtan.noxboard.utils.NoxLogger.logError("ThemeLoader", "Failed to load preset theme", e)
                                                android.widget.Toast.makeText(context, context.getString(R.string.load_preset_failed), android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            prefs.customBackgroundColor = preset.previewBg
                                            prefs.customKeyColor = preset.previewKey
                                            prefs.customSpecialKeyColor = preset.previewSpecial
                                            prefs.customTextColor = textColor
                                            prefs.customSpecialTextColor = specialTextColor
                                            prefs.customActiveKeyColor = preset.activeKeyColor
                                            prefs.customPopupBgColor = preset.previewSpecial
                                            prefs.customPopupTextColor = textColor

                                            if (isLightTheme) {
                                                prefs.customNumpadBgColor = "#F2F3F4"
                                                prefs.customNumpadKeyColor = "#FFFFFF"
                                                prefs.customNumpadTextColor = "#000000"
                                                prefs.customNumpadSpecialKeyColor = "#E0E0E0"
                                                prefs.customNumpadSpecialTextColor = "#000000"
                                            } else {
                                                prefs.customNumpadBgColor = "#1F1F1F"
                                                prefs.customNumpadKeyColor = "#2D2D2D"
                                                prefs.customNumpadTextColor = "#FFFFFF"
                                                prefs.customNumpadSpecialKeyColor = "#3A3A3C"
                                                prefs.customNumpadSpecialTextColor = "#FFFFFF"
                                            }

                                            prefs.isTextureEffectEnabled = false
                                            prefs.customWallpaperUri = null
                                            prefs.individualKeyColors = "{}"
                                            prefs.individualKeyTextColors = "{}"
                                            prefs.individualKeyActiveColors = "{}"
                                            prefs.individualKeyPopupBgColors = "{}"
                                            prefs.individualKeyPopupTextColors = "{}"
                                        }

                                        prefs.activeTheme = presetId
                                        activeTheme = presetId
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
        if (themeToDelete != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { themeToDelete = null },
                title = {
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.delete_theme_title),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.delete_theme_msg),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            val idToDelete = themeToDelete
                            themeToDelete = null
                            if (idToDelete != null) {
                                val arr = org.json.JSONArray(prefs.savedCustomThemes)
                                val newArr = org.json.JSONArray()
                                for (i in 0 until arr.length()) {
                                    val item = arr.getJSONObject(i)
                                    if (item.optString("id") != idToDelete) {
                                        newArr.put(item)
                                    } else {
                                        val wallUri = item.optString("wallUri", "")
                                        if (wallUri.contains("theme_wallpapers")) {
                                            try {
                                                val path = android.net.Uri.parse(wallUri).path
                                                if (path != null) java.io.File(path).delete()
                                            } catch (e: Exception) {}
                                        }
                                    }
                                }
                                prefs.savedCustomThemes = newArr.toString()

                                if (activeTheme == idToDelete) {
                                    prefs.activeTheme = "DEFAULT"
                                    activeTheme = "DEFAULT"
                                    prefs.customBackgroundColor = "#000000"
                                    prefs.customKeyColor = "#1E1A2B"
                                    prefs.customTextColor = "#FFFFFF"
                                    prefs.customSpecialKeyColor = "#12101C"
                                    prefs.customSpecialTextColor = "#FFFFFF"
                                    prefs.customKeyBorderColor = "#00000000"
                                    prefs.customActiveKeyColor = "#7C4DFF"
                                    prefs.customPopupBgColor = "#1F1F1F"
                                    prefs.customPopupTextColor = "#FFFFFF"
                                    prefs.useCustomIconColors = false
                                    prefs.isTextureEffectEnabled = false
                                    prefs.customWallpaperUri = null
                                    prefs.individualKeyColors = "{}"
                                    prefs.individualKeyTextColors = "{}"
                                    prefs.individualKeyActiveColors = "{}"
                                    prefs.individualKeyPopupBgColors = "{}"
                                    prefs.individualKeyPopupTextColors = "{}"
                                }

                                val updatedList = mutableListOf<org.json.JSONObject>()
                                for (i in 0 until newArr.length()) updatedList.add(newArr.getJSONObject(i))
                                customThemes = updatedList
                            }
                        }
                    ) {
                        androidx.compose.material3.Text(stringResource(R.string.delete_btn), color = Color.Red, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { themeToDelete = null }) {
                        androidx.compose.material3.Text(stringResource(R.string.cancel), color = Color.White)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun ThemePreviewCard(
    title: String,
    isSelected: Boolean,
    bgHex: String,
    keyHex: String,
    textHex: String,
    specialKeyHex: String,
    specialTextHex: String,
    individualColorsJson: String = "{}",
    individualTextsJson: String = "{}",
    useCustomIconColors: Boolean = false,
    wallpaperUri: String? = null,
    wallpaperAssetPath: String? = null,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
    var localBgHex by remember(bgHex) { mutableStateOf(bgHex) }
    var localKeyHex by remember(keyHex) { mutableStateOf(keyHex) }
    var localTextHex by remember(textHex) { mutableStateOf(textHex) }
    var localSpecialKeyHex by remember(specialKeyHex) { mutableStateOf(specialKeyHex) }
    var localSpecialTextHex by remember(specialTextHex) { mutableStateOf(specialTextHex) }
    var localIndColors by remember(individualColorsJson) { mutableStateOf(individualColorsJson) }
    var localIndTexts by remember(individualTextsJson) { mutableStateOf(individualTextsJson) }
    var localIconTint by remember(useCustomIconColors) { mutableStateOf(useCustomIconColors) }

    val individualColorsMap = remember(localIndColors) {
        try { val json = org.json.JSONObject(localIndColors); val map = mutableMapOf<String, String>(); json.keys().forEach { map[it] = json.getString(it) }; map } catch (e: Exception) { emptyMap() }
    }

    val individualTextsMap = remember(localIndTexts) {
        try { val json = org.json.JSONObject(localIndTexts); val map = mutableMapOf<String, String>(); json.keys().forEach { map[it] = json.getString(it) }; map } catch (e: Exception) { emptyMap() }
    }

    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }
    var bitmap by remember(wallpaperUri, wallpaperAssetPath) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var boardCoords by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }

    LaunchedEffect(wallpaperUri, wallpaperAssetPath) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (wallpaperAssetPath != null) {
                    val inputStream = context.assets.open(wallpaperAssetPath)
                    val zis = java.util.zip.ZipInputStream(inputStream)
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (entry.name == "config.json") {
                            val buffer = java.io.ByteArrayOutputStream()
                            val data = ByteArray(1024)
                            var count: Int
                            while (zis.read(data, 0, 1024).also { count = it } != -1) {
                                buffer.write(data, 0, count)
                            }
                            val jsonStr = buffer.toString(Charsets.UTF_8.name())
                            try {
                                val json = org.json.JSONObject(jsonStr)
                                localBgHex = json.optString("bg", localBgHex)
                                localKeyHex = json.optString("key", localKeyHex)
                                localTextHex = json.optString("text", localTextHex)
                                localSpecialKeyHex = json.optString("specialKey", localSpecialKeyHex)
                                localSpecialTextHex = json.optString("specialText", localSpecialTextHex)
                                localIndColors = json.optString("ind_colors", localIndColors)
                                localIndTexts = json.optString("ind_texts", localIndTexts)
                                localIconTint = json.optBoolean("iconTint", localIconTint)
                            } catch(e: Exception) {}
                        } else if (entry.name.startsWith("background.")) {
                            val bmp = android.graphics.BitmapFactory.decodeStream(zis)
                            if (bmp != null) bitmap = bmp.asImageBitmap()
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                    zis.close()
                } else if (!wallpaperUri.isNullOrEmpty()) {
                    val uri = android.net.Uri.parse(wallpaperUri)
                    val stream = context.contentResolver.openInputStream(uri)
                    val bmp = android.graphics.BitmapFactory.decodeStream(stream)
                    stream?.close()
                    if (bmp != null) bitmap = bmp.asImageBitmap()
                }
            } catch (e: Exception) {}
        }
    }

    val bgBrush = remember(localBgHex) {
        if (localBgHex.startsWith("GRADIENT:")) {
            try {
                val colors = localBgHex.removePrefix("GRADIENT:").split(",").map { Color(android.graphics.Color.parseColor(it)) }
                androidx.compose.ui.graphics.Brush.linearGradient(colors)
            } catch (e: Exception) { androidx.compose.ui.graphics.SolidColor(Color.Black) }
        } else {
            androidx.compose.ui.graphics.SolidColor(try { Color(android.graphics.Color.parseColor(localBgHex)) } catch (e: Exception) { Color.Black })
        }
    }

    @Composable
    fun RowScope.MiniKey(weight: Float, defaultBgHex: String, defaultTextHex: String, text: String = "", iconRes: Int? = null, keyId: String) {
        val finalBgHex = individualColorsMap[keyId] ?: defaultBgHex
        val finalTextHex = individualTextsMap[keyId] ?: defaultTextHex

        val isOriginalIcon = !localIconTint && (keyId == "Emoji" || keyId == "Vault")

        var keyCoords by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }

        val bgBrush = remember(finalBgHex, keyCoords, boardCoords) {
            if (finalBgHex.startsWith("GRADIENT:")) {
                try {
                    val colors = finalBgHex.removePrefix("GRADIENT:").split(",").map { Color(android.graphics.Color.parseColor(it)) }
                    if (keyCoords != null && boardCoords != null) {
                        try {
                            val keyInBoard = boardCoords!!.localPositionOf(keyCoords!!, androidx.compose.ui.geometry.Offset.Zero)
                            val boardSize = boardCoords!!.size
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = colors,
                                start = androidx.compose.ui.geometry.Offset(-keyInBoard.x, -keyInBoard.y),
                                end = androidx.compose.ui.geometry.Offset(boardSize.width.toFloat() - keyInBoard.x, boardSize.height.toFloat() - keyInBoard.y)
                            )
                        } catch (e: Exception) { androidx.compose.ui.graphics.SolidColor(colors.first()) }
                    } else {
                        androidx.compose.ui.graphics.SolidColor(colors.first())
                    }
                } catch (e: Exception) { androidx.compose.ui.graphics.SolidColor(Color.Black) }
            } else {
                val solid = try { Color(android.graphics.Color.parseColor(finalBgHex)) } catch (e: Exception) { Color.Black }
                androidx.compose.ui.graphics.SolidColor(solid)
            }
        }

        val textBrush = remember(finalTextHex, keyCoords, boardCoords) {
            if (finalTextHex.startsWith("GRADIENT:")) {
                try {
                    val colors = finalTextHex.removePrefix("GRADIENT:").split(",").map { Color(android.graphics.Color.parseColor(it)) }.reversed()
                    if (keyCoords != null && boardCoords != null) {
                        try {
                            val keyInBoard = boardCoords!!.localPositionOf(keyCoords!!, androidx.compose.ui.geometry.Offset.Zero)
                            val boardSize = boardCoords!!.size
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = colors,
                                start = androidx.compose.ui.geometry.Offset(-keyInBoard.x, -keyInBoard.y),
                                end = androidx.compose.ui.geometry.Offset(boardSize.width.toFloat() - keyInBoard.x, boardSize.height.toFloat() - keyInBoard.y)
                            )
                        } catch (e: Exception) { androidx.compose.ui.graphics.SolidColor(colors.first()) }
                    } else {
                        androidx.compose.ui.graphics.SolidColor(colors.first())
                    }
                } catch (e: Exception) { androidx.compose.ui.graphics.SolidColor(Color.White) }
            } else {
                val solid = try { Color(android.graphics.Color.parseColor(finalTextHex)) } catch (e: Exception) { Color.White }
                androidx.compose.ui.graphics.SolidColor(solid)
            }
        }

        Box(
            modifier = Modifier
                .weight(weight)
                .fillMaxHeight()
                .onGloballyPositioned { keyCoords = it }
                .background(bgBrush, RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (iconRes != null) {
                if (isOriginalIcon) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(9.dp)
                    )
                } else {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = iconRes),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(9.dp)
                            .graphicsLayer(alpha = 0.99f)
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(brush = textBrush, blendMode = androidx.compose.ui.graphics.BlendMode.SrcAtop)
                                }
                            }
                    )
                }
            } else if (text.isNotEmpty()) {
                Text(
                    text = text,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = textBrush,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Medium,
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                        lineHeight = 7.sp
                    )
                )
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.width(160.dp).height(110.dp).clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(if (isSelected) 3.dp else 1.dp, borderColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { boardCoords = it }
                    .background(bgBrush)
            ) {
                if (bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap!!,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                val overlayColor = if (bitmap != null) Color.Black.copy(alpha = 0.2f) else Color.Transparent

                val layout = remember {
                    val raw = com.noxtan.noxboard.KeyboardLayout.getMyanmarLayout(context, false)
                    if (raw.isNotEmpty() && raw[0].isNotEmpty() && raw[0][0].code in -102..-100) raw.drop(1) else raw
                }

                val resolveIcon: (String, Int) -> Int? = { prefName, defaultRes ->
                    if (prefName == "DEFAULT") {
                        if (defaultRes != 0) defaultRes else null
                    } else {
                        val id = context.resources.getIdentifier(prefName, "drawable", context.packageName)
                        if (id != 0) id else if (defaultRes != 0) defaultRes else null
                    }
                }

                Column(modifier = Modifier.fillMaxSize().background(overlayColor).padding(6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    layout.forEach { row ->
                        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            row.forEach { key ->
                                val keyId = when {
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

                                val cIconRes = if (!key.customIcon.isNullOrEmpty()) {
                                    context.resources.getIdentifier(key.customIcon, "drawable", context.packageName)
                                } else 0

                                val iconRes = if (cIconRes != 0) cIconRes else when (key.code) {
                                    -11 -> resolveIcon(prefs.iconStyleEmoji, com.noxtan.noxboard.R.drawable.ic_emoji)
                                    -12 -> resolveIcon(prefs.iconStyleVault, com.noxtan.noxboard.R.drawable.ic_pass)
                                    -13 -> resolveIcon(prefs.iconStyleClipboard, com.noxtan.noxboard.R.drawable.ic_copy)
                                    -14 -> resolveIcon(prefs.iconStyleSetting, com.noxtan.noxboard.R.drawable.ic_setting)
                                    -8  -> resolveIcon(prefs.iconStyleNumpad, com.noxtan.noxboard.R.drawable.ic_numpad)
                                    -3  -> resolveIcon(prefs.iconStyleGlobe, com.noxtan.noxboard.R.drawable.ic_earth)
                                    -1  -> resolveIcon(prefs.iconStyleShift, com.noxtan.noxboard.R.drawable.ic_shift_inactive)
                                    10  -> resolveIcon(prefs.iconStyleEnter, com.noxtan.noxboard.R.drawable.ic_enter)
                                    -2  -> resolveIcon(prefs.iconStyleBackspace, com.noxtan.noxboard.R.drawable.ic_backspace_classic_erase)
                                    -4  -> resolveIcon(prefs.iconStyleSymbols, com.noxtan.noxboard.R.drawable.ic_sym_classic_text)
                                    -5  -> resolveIcon(prefs.iconStyleSymbols, com.noxtan.noxboard.R.drawable.ic_sym_classic_text)
                                    -16 -> resolveIcon(prefs.iconStyleHideKeyboard, com.noxtan.noxboard.R.drawable.ic_hide_keys_classic_down)
                                    32  -> resolveIcon(prefs.iconStyleSpacebar, 0)
                                    else -> null
                                }

                                val textToDraw = if (iconRes != null) "" else when (key.code) {
                                    32 -> ""
                                    -2 -> "⌫"
                                    -4 -> "?123"
                                    else -> key.normalText
                                }

                                val isSpecial = key.code != 0 && key.code != 32
                                val defaultBg = if (isSpecial) localSpecialKeyHex else localKeyHex
                                val defaultText = if (isSpecial) localSpecialTextHex else localTextHex

                                MiniKey(
                                    weight = key.widthWeight,
                                    defaultBgHex = defaultBg,
                                    defaultTextHex = defaultText,
                                    text = textToDraw,
                                    iconRes = iconRes,
                                    keyId = keyId
                                )
                            }
                        }
                    }
                }

                if (onEdit != null || onDelete != null || onShare != null) {
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (onShare != null) {
                            Box(modifier = Modifier.size(24.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape).clickable { onShare() }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                        if (onEdit != null) {
                            Box(modifier = Modifier.size(24.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape).clickable { onEdit() }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                        if (onDelete != null) {
                            Box(modifier = Modifier.size(24.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape).clickable { onDelete() }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }
        Text(text = title, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}