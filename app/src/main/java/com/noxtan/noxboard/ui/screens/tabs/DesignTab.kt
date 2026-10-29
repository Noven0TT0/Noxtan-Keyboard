package com.noxtan.noxboard.ui.screens.tabs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.KeyboardMode
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.ui.components.MockKeyboard
import com.noxtan.noxboard.ui.components.ThemePreviewDialog
import com.noxtan.noxboard.ui.models.KeyboardThemePreset

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesignTab(
    onNavigateToTheme: () -> Unit,
    onNavigateToSizePosition: () -> Unit,
    activeAppearanceTab: Int = 0,
    onAppearanceTabSelected: (Int) -> Unit = {},
    scrollState: androidx.compose.foundation.ScrollState = androidx.compose.foundation.rememberScrollState()
) {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }
    var activeTheme by remember { mutableStateOf(prefs.activeTheme) }
    var selectedThemeForPreview by remember { mutableStateOf<KeyboardThemePreset?>(null) }

    val themes = listOf(
        KeyboardThemePreset("PRESET_PURE_BLACK", "pure black", "#000000", "#000000", "#FFFFFF", "#000000", "#A0A0A0"),
        KeyboardThemePreset("PRESET_PREMIUM_DARK", "premium dark", "#000000", "#1C1C1E", "#F0F0F0", "#121214", "#A1A1AA"),
        KeyboardThemePreset("PRESET_PURE_WHITE", "pure white", "#FFFFFF", "#FFFFFF", "#000000", "#FFFFFF", "#666666"),
        KeyboardThemePreset("PRESET_CREAM_WHITE", "cream white", "#FAF9F6", "#F2F0EA", "#1C1B1A", "#EAE8E0", "#6B6A65"),

        KeyboardThemePreset("PRESET_LUXURY", "luxury black and white", "#111111", "#FFFDF0", "#111111", "#111111", "#D4AF37"),
        KeyboardThemePreset("PRESET_STRAWBERRY", "strawberry milk", "#FCE4EC", "#FFFFFF", "#F06292", "#FCE4EC", "#F06292"),
        KeyboardThemePreset("PRESET_TARO", "taro milk", "#F3E5F5", "#FFFFFF", "#6A1B9A", "#E1BEE7", "#4A148C"),
        KeyboardThemePreset("PRESET_ABYSS", "abyss blue", "#0A192F", "#112240", "#E6F1FF", "#0A192F", "#64FFDA"),
        KeyboardThemePreset("PRESET_BUMBLEBEE", "bumblebee", "#FFCC00", "#212121", "#FFD700", "#121212", "#00B0FF", sugBgHex = "#121212", sugTextHex = "#FFD700"),
        KeyboardThemePreset("PRESET_CYBERPUNK", "cyberpunk 2077", "#0D0221", "#261447", "#00FFCC", "#FF3864", "#FFFFFF"),
        KeyboardThemePreset("PRESET_MOCHA", "mocha coffee", "#3E2723", "#5D4037", "#D7CCC8", "#795548", "#FFC107"),
        KeyboardThemePreset("PRESET_RETRO", "retro arcade", "#212121", "#424242", "#FFFFFF", "#FF5722", "#FFEB3B"),
        KeyboardThemePreset("PRESET_MIDNIGHT", "midnight purple", "#120A2A", "#25144A", "#E0D4FF", "#7B2CBF", "#00FFD1"),
        KeyboardThemePreset("PRESET_MINT", "mint breeze", "#E0F2F1", "#FFFFFF", "#004D40", "#B2DFDB", "#FF5252")
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (activeAppearanceTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onNavigateToTheme,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Custom Theme", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }

                val savedThemesStr = prefs.savedCustomThemes
                val customThemes = remember(savedThemesStr) {
                    val list = mutableListOf<KeyboardThemePreset>()
                    try {
                        val arr = org.json.JSONArray(savedThemesStr)
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            list.add(
                                KeyboardThemePreset(
                                    id = obj.optString("id"),
                                    name = obj.optString("name"),
                                    bgHex = obj.optString("bgHex"),
                                    keyHex = obj.optString("keyHex"),
                                    textHex = obj.optString("textHex"),
                                    specKeyHex = obj.optString("specKeyHex"),
                                    specTextHex = obj.optString("specTextHex"),
                                    borderHex = obj.optString("borderHex", "#00000000"),
                                    specBorderHex = obj.optString("specBorderHex", "#00000000"),
                                    popupBgHex = obj.optString("popupBgHex", obj.optString("specKeyHex")),
                                    popupTextHex = obj.optString("popupTextHex", obj.optString("specTextHex")),
                                    activeKeyHex = obj.optString("activeKeyHex", obj.optString("specTextHex")),
                                    sugBgHex = obj.optString("sugBgHex", obj.optString("bgHex")),
                                    sugTextHex = obj.optString("sugTextHex", obj.optString("specTextHex"))
                                )
                            )
                        }
                    } catch(e: Exception) {}
                    list.reversed()
                }

                if (customThemes.isNotEmpty()) {
                    Text("My Themes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(customThemes) { theme ->
                            val isSelected = activeTheme == theme.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(160.dp)
                                    .clickable {
                                        if (activeTheme == theme.id) {
                                            activeTheme = "DEFAULT"
                                            prefs.activeTheme = "DEFAULT"

                                            prefs.individualKeyTextColors = "{}"
                                            prefs.customKeyBorderColor = "#00000000"
                                            prefs.customSpecialKeyBorderColor = "#00000000"

                                        } else {
                                            selectedThemeForPreview = theme
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    val bgInt = try { android.graphics.Color.parseColor(theme.bgHex) } catch(e: Exception) { 0 }
                                    val darkness = 1 - (0.299 * android.graphics.Color.red(bgInt) + 0.587 * android.graphics.Color.green(bgInt) + 0.114 * android.graphics.Color.blue(bgInt)) / 255

                                    val calcSpecBorder = if (theme.bgHex.equals(theme.specKeyHex, ignoreCase = true) && !theme.id.contains("PURE")) {
                                        if (darkness < 0.5) Color(0x26000000) else Color(0x26FFFFFF)
                                    } else Color.Transparent

                                    val finalKeyBorder = if (theme.id.startsWith("CUSTOM_")) Color(android.graphics.Color.parseColor(theme.borderHex)) else Color.Transparent
                                    val finalSpecBorder = if (theme.id.startsWith("CUSTOM_")) Color(android.graphics.Color.parseColor(theme.specBorderHex)) else calcSpecBorder

                                    val finalSugColor = try { android.graphics.Color.parseColor(if (theme.sugTextHex.isNotEmpty()) theme.sugTextHex else theme.specTextHex) } catch(e: Exception) { android.graphics.Color.WHITE }
                                    val finalSugBgColor = try { Color(android.graphics.Color.parseColor(theme.sugBgHex)) } catch(e: Exception) { Color(android.graphics.Color.parseColor(theme.bgHex)) }

                                    MockKeyboard(
                                        bg = Color(android.graphics.Color.parseColor(theme.bgHex)),
                                        keyColor = Color(android.graphics.Color.parseColor(theme.keyHex)),
                                        textColor = Color(android.graphics.Color.parseColor(theme.textHex)),
                                        specKeyColor = Color(android.graphics.Color.parseColor(theme.specKeyHex)),
                                        specTextColor = Color(android.graphics.Color.parseColor(theme.specTextHex)),
                                        sugBgColor = finalSugBgColor,
                                        sugTextColor = Color(finalSugColor),
                                        keyBorderColor = finalKeyBorder,
                                        specKeyBorderColor = finalSpecBorder,
                                        scale = 0.55f
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier.padding(6.dp).size(20.dp).clip(CircleShape).background(Color.White).border(1.dp, Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = theme.name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Text("Presets", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)

                val chunkedThemes = themes.chunked(2)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    chunkedThemes.forEach { rowThemes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowThemes.forEach { theme ->
                                val isSelected = activeTheme == theme.id

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                        .clickable {
                                            if (activeTheme == theme.id) {
                                                activeTheme = "DEFAULT"
                                                prefs.activeTheme = "DEFAULT"

                                                prefs.individualKeyTextColors = "{}"
                                                prefs.customKeyBorderColor = "#00000000"
                                                prefs.customSpecialKeyBorderColor = "#00000000"

                                            } else {
                                                selectedThemeForPreview = theme
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.TopEnd) {
                                        val bgInt = try { android.graphics.Color.parseColor(theme.bgHex) } catch(e: Exception) { 0 }
                                        val darkness = 1 - (0.299 * android.graphics.Color.red(bgInt) + 0.587 * android.graphics.Color.green(bgInt) + 0.114 * android.graphics.Color.blue(bgInt)) / 255

                                        val calcSpecBorder = if (theme.bgHex.equals(theme.specKeyHex, ignoreCase = true) && !theme.id.contains("PURE")) {
                                            if (darkness < 0.5) Color(0x26000000) else Color(0x26FFFFFF)
                                        } else Color.Transparent

                                        val finalKeyBorder = if (theme.id.startsWith("CUSTOM_")) Color(android.graphics.Color.parseColor(theme.borderHex)) else Color.Transparent
                                        val finalSpecBorder = if (theme.id.startsWith("CUSTOM_")) Color(android.graphics.Color.parseColor(theme.specBorderHex)) else calcSpecBorder

                                        val finalSugColor = try { android.graphics.Color.parseColor(if (theme.sugTextHex.isNotEmpty()) theme.sugTextHex else theme.specTextHex) } catch(e: Exception) { android.graphics.Color.WHITE }
                                        val finalSugBgColor = try { Color(android.graphics.Color.parseColor(theme.sugBgHex)) } catch(e: Exception) { Color(android.graphics.Color.parseColor(theme.bgHex)) }

                                        MockKeyboard(
                                            bg = Color(android.graphics.Color.parseColor(theme.bgHex)),
                                            keyColor = Color(android.graphics.Color.parseColor(theme.keyHex)),
                                            textColor = Color(android.graphics.Color.parseColor(theme.textHex)),
                                            specKeyColor = Color(android.graphics.Color.parseColor(theme.specKeyHex)),
                                            specTextColor = Color(android.graphics.Color.parseColor(theme.specTextHex)),
                                            sugBgColor = finalSugBgColor,
                                            sugTextColor = Color(finalSugColor),
                                            keyBorderColor = finalKeyBorder,
                                            specKeyBorderColor = finalSpecBorder,
                                            scale = 0.55f
                                        )

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier.padding(6.dp).size(20.dp).clip(CircleShape).background(Color.White).border(1.dp, Color.White, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = theme.name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (rowThemes.size < 2) { repeat(2 - rowThemes.size) { Spacer(modifier = Modifier.weight(1f)) } }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                val layouts = listOf(
                    Triple("Modern Layout", "စာရိုက်ရလွယ်ကူကျယ်ဝန်းသော ခေတ်မီလက်ကွက်ပုံစံ", "MODERN"),
                    Triple("Classic Layout", "ရင်းနှီးပြီးသား ရိုးရာလက်ကွက်ပုံစံအဟောင်း", "CLASSIC")
                )

                var activeLayoutState by remember { mutableStateOf(prefs.activeLayoutMyanmar) }

                val initialPage = layouts.indexOfFirst { it.third == prefs.activeLayoutMyanmar }.takeIf { it >= 0 } ?: 0
                val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { layouts.size })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    pageSpacing = 16.dp
                ) { page ->
                    val layoutData = layouts[page]
                    val isActive = activeLayoutState == layoutData.third

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = layoutData.first,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = layoutData.second,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { ctx ->
                                    com.noxtan.noxboard.NoxKeyboardView(ctx).apply {
                                        isEditMode = true
                                    }
                                },
                                update = { view ->
                                    view.currentMode = KeyboardMode.MYANMAR
                                    view.previewLayoutId = layoutData.third
                                    view.applySettings(prefs)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                prefs.activeLayoutMyanmar = layoutData.third
                                prefs.activeLayoutEnglish = layoutData.third
                                activeLayoutState = layoutData.third
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                contentColor = if (isActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                        ) {
                            Text(
                                text = if (isActive) "Active Layout" else "Apply Layout",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = onNavigateToSizePosition,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Customize Size & Position", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .height(8.dp)
                                .width(width)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (activeAppearanceTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onAppearanceTabSelected(0) }
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Theme",
                        color = if (activeAppearanceTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (activeAppearanceTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onAppearanceTabSelected(1) }
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Layout",
                        color = if (activeAppearanceTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    if (selectedThemeForPreview != null) {
        ThemePreviewDialog(
            theme = selectedThemeForPreview!!,
            onDismiss = { selectedThemeForPreview = null },
            onApply = {
                val theme = selectedThemeForPreview!!
                activeTheme = theme.id
                prefs.activeTheme = theme.id

                prefs.customBackgroundColor = theme.bgHex
                prefs.customKeyColor = theme.keyHex
                prefs.customTextColor = theme.textHex
                prefs.customSpecialKeyColor = theme.specKeyHex
                prefs.customSpecialTextColor = theme.specTextHex

                prefs.customNumpadBgColor = theme.bgHex
                prefs.customNumpadKeyColor = theme.keyHex
                prefs.customNumpadTextColor = theme.textHex
                prefs.customNumpadSpecialKeyColor = theme.specKeyHex
                prefs.customNumpadSpecialTextColor = theme.specTextHex

                prefs.customPopupBgColor = theme.popupBgHex
                prefs.customPopupTextColor = theme.popupTextHex
                prefs.customActiveKeyColor = theme.activeKeyHex
                prefs.customSugBgColor = theme.sugBgHex

                if (theme.id.startsWith("CUSTOM_")) {
                    prefs.customKeyBorderColor = theme.borderHex
                    prefs.customSpecialKeyBorderColor = theme.specBorderHex
                } else {
                    val bgInt = try { android.graphics.Color.parseColor(theme.bgHex) } catch(e: Exception) { 0 }
                    val darkness = 1 - (0.299 * android.graphics.Color.red(bgInt) + 0.587 * android.graphics.Color.green(bgInt) + 0.114 * android.graphics.Color.blue(bgInt)) / 255

                    prefs.customKeyBorderColor = "#00000000"
                    if (theme.bgHex.equals(theme.specKeyHex, ignoreCase = true) && !theme.id.contains("PURE")) {
                        prefs.customSpecialKeyBorderColor = if (darkness < 0.5) "#26000000" else "#26FFFFFF"
                    } else {
                        prefs.customSpecialKeyBorderColor = "#00000000"
                    }
                }

                prefs.individualKeyColors = "{}"
                prefs.individualKeyActiveColors = "{}"
                prefs.individualKeyPopupBgColors = "{}"

                val jsonText = org.json.JSONObject()
                jsonText.put("Suggestion", theme.sugTextHex)
                prefs.individualKeyTextColors = jsonText.toString()

                selectedThemeForPreview = null
            },
            onEdit = {
                val theme = selectedThemeForPreview!!

                val finalBorderHex: String
                val finalSpecBorderHex: String

                if (theme.id.startsWith("CUSTOM_")) {
                    finalBorderHex = theme.borderHex
                    finalSpecBorderHex = theme.specBorderHex
                } else {
                    val bgInt = try { android.graphics.Color.parseColor(theme.bgHex) } catch(e: Exception) { 0 }
                    val darkness = 1 - (0.299 * android.graphics.Color.red(bgInt) + 0.587 * android.graphics.Color.green(bgInt) + 0.114 * android.graphics.Color.blue(bgInt)) / 255

                    finalBorderHex = "#00000000"
                    if (theme.bgHex.equals(theme.specKeyHex, ignoreCase = true) && !theme.id.contains("PURE")) {
                        finalSpecBorderHex = if (darkness < 0.5) "#26000000" else "#26FFFFFF"
                    } else {
                        finalSpecBorderHex = "#00000000"
                    }
                }

                val json = org.json.JSONObject().apply {
                    put("id", theme.id)
                    put("name", theme.name)
                    put("bgHex", theme.bgHex)
                    put("keyHex", theme.keyHex)
                    put("textHex", theme.textHex)
                    put("specKeyHex", theme.specKeyHex)
                    put("specTextHex", theme.specTextHex)
                    put("borderHex", finalBorderHex)
                    put("specBorderHex", finalSpecBorderHex)
                    put("popupBgHex", theme.popupBgHex)
                    put("popupTextHex", theme.popupTextHex)
                    put("activeKeyHex", theme.activeKeyHex)
                    put("sugBgHex", theme.sugBgHex)
                    put("sugTextHex", theme.sugTextHex)
                }
                prefs.editingThemeId = json.toString()
                selectedThemeForPreview = null
                onNavigateToTheme()
            }
        )
    }
}