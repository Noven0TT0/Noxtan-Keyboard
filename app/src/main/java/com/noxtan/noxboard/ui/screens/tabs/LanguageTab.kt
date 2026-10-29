package com.noxtan.noxboard.ui.screens.tabs

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs

@Composable
fun LanguageTab() {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }
    val scrollState = rememberScrollState()

    var isEnglishEnabled by remember { mutableStateOf(prefs.isEnglishEnabled) }
    var isMyanmarEnabled by remember { mutableStateOf(prefs.isMyanmarEnabled) }
    var isThaiEnabled by remember { mutableStateOf(prefs.isThaiEnabled) }

    var defaultLanguage by remember { mutableStateOf(prefs.defaultLanguage) }
    var myanmarTypingStyle by remember { mutableStateOf(prefs.myanmarTypingStyle) }

    var showDoubleTapDialog by remember { mutableStateOf(false) }

    val handleToggle: (String, Boolean) -> Unit = { lang, checked ->
        val willBeEn = if (lang == "ENGLISH") checked else isEnglishEnabled
        val willBeMy = if (lang == "MYANMAR") checked else isMyanmarEnabled
        val willBeTh = if (lang == "THAI") checked else isThaiEnabled

        if (willBeEn || willBeMy || willBeTh) {
            if (lang == "ENGLISH") { isEnglishEnabled = checked; prefs.isEnglishEnabled = checked }
            if (lang == "MYANMAR") { isMyanmarEnabled = checked; prefs.isMyanmarEnabled = checked }
            if (lang == "THAI") { isThaiEnabled = checked; prefs.isThaiEnabled = checked }

            val enabledList = mutableListOf<String>()
            if (isEnglishEnabled) enabledList.add("ENGLISH")
            if (isMyanmarEnabled) enabledList.add("MYANMAR")
            if (isThaiEnabled) enabledList.add("THAI")

            if (enabledList.size == 1) {
                defaultLanguage = enabledList.first()
                prefs.defaultLanguage = enabledList.first()
            } else if (!enabledList.contains(defaultLanguage)) {
                defaultLanguage = enabledList.first()
                prefs.defaultLanguage = enabledList.first()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            text = "Manage Languages",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Default",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LanguageItemRow(
                    languageName = "English (US)",
                    isEnabled = isEnglishEnabled,
                    isDefault = defaultLanguage == "ENGLISH",
                    onToggle = { handleToggle("ENGLISH", it) },
                    onSetDefault = { if (isEnglishEnabled) { defaultLanguage = "ENGLISH"; prefs.defaultLanguage = "ENGLISH" } }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                LanguageItemRow(
                    languageName = "Myanmar (ဗမာ)",
                    isEnabled = isMyanmarEnabled,
                    isDefault = defaultLanguage == "MYANMAR",
                    onToggle = { handleToggle("MYANMAR", it) },
                    onSetDefault = { if (isMyanmarEnabled) { defaultLanguage = "MYANMAR"; prefs.defaultLanguage = "MYANMAR" } }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                LanguageItemRow(
                    languageName = "Thai (ไทย)",
                    isEnabled = isThaiEnabled,
                    isDefault = defaultLanguage == "THAI",
                    onToggle = { handleToggle("THAI", it) },
                    onSetDefault = { if (isThaiEnabled) { defaultLanguage = "THAI"; prefs.defaultLanguage = "THAI" } }
                )
            }
        }

        AnimatedVisibility(visible = isMyanmarEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "Myanmar Typing Style",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "How do you type 'ဖြေ'?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Choose your preferred keystroke sequence",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TypingSequenceRow(
                            sequence = "ဖ + ြ + ေ",
                            label = "Standard Unicode",
                            isSelected = myanmarTypingStyle == "UNICODE",
                            onClick = { myanmarTypingStyle = "UNICODE"; prefs.myanmarTypingStyle = "UNICODE" }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        TypingSequenceRow(
                            sequence = "ေ + ဖ + ြ",
                            label = "Smart Unicode (Visual Typing)",
                            isSelected = myanmarTypingStyle == "VISUAL_SMART",
                            onClick = { myanmarTypingStyle = "VISUAL_SMART"; prefs.myanmarTypingStyle = "VISUAL_SMART" }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        TypingSequenceRow(
                            sequence = "ေ + ြ + ဖ",
                            label = "Zawgyi (Legacy Font)",
                            isSelected = myanmarTypingStyle == "ZAWGYI",
                            onClick = { myanmarTypingStyle = "ZAWGYI"; prefs.myanmarTypingStyle = "ZAWGYI" }
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showDoubleTapDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.TouchApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Double Tap Shortcuts", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showDoubleTapDialog) {
        DoubleTapSettingsDialog(prefs = prefs, onDismiss = { showDoubleTapDialog = false })
    }
}

@Composable
fun LanguageItemRow(
    languageName: String,
    isEnabled: Boolean,
    isDefault: Boolean,
    onToggle: (Boolean) -> Unit,
    onSetDefault: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f).clickable { onSetDefault() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        if (isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        CircleShape
                    )
                    .padding(4.dp)
            ) {
                if (isDefault) {
                    Box(
                        modifier = Modifier.fillMaxSize().clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = languageName,
                fontWeight = if (isDefault) FontWeight.Bold else FontWeight.Medium,
                fontSize = 15.sp,
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun TypingSequenceRow(
    sequence: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sequence,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(
                    2.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    CircleShape
                )
                .padding(4.dp)
        ) {
            if (isSelected) {
                Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            }
        }
    }
}

@Composable
fun DoubleTapSettingsDialog(prefs: NoxBoardPrefs, onDismiss: () -> Unit) {
    val masterDoubleTapMap = mapOf(
        "ဆ" to "ဈ", "က" to "ဿ", "သ" to "ဥ", "ေ" to "ဗ", "ျ" to "ှ",
        "ိ" to "ီ", "်" to "ွ", "့" to "ံ", "ြ" to "ဲ", "ု" to "ဒ",
        "ူ" to "ဓ", "ဖ" to "ဇ", "ထ" to "ဌ", "ခ" to "ဃ", "လ" to "ဠ",
        "ဘ" to "၊", "ည" to "ဉ", "ာ" to "ါ", "ယ" to "။", "ဏ" to "ဩ",
        "ရ" to "ဋ္ဌ", "ဂ" to "ဏ္ဍ", "ဝ" to "ဋ", "ဟ" to "ဍ"
    )
    val defaultOffKeys = setOf("ဆ", "ဖ", "ထ", "ခ", "ည", "ဏ", "ဂ", "ဝ", "ဟ", "သ", "ူ", "ရ")

    var currentStates by remember {
        val map = mutableMapOf<String, Boolean>()
        try {
            val json = org.json.JSONObject(prefs.doubleTapStates)
            masterDoubleTapMap.keys.forEach { key ->
                map[key] = if (json.has(key)) json.getBoolean(key) else !defaultOffKeys.contains(key)
            }
        } catch (e: Exception) {
            masterDoubleTapMap.keys.forEach { key -> map[key] = !defaultOffKeys.contains(key) }
        }
        mutableStateOf(map)
    }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Double Tap Shortcuts", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {
                Text("Choose which keys should change when double tapped.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))

                masterDoubleTapMap.forEach { (key, shiftChar) ->
                    val isEnabled = currentStates[key] ?: true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newVal = !isEnabled
                                val newMap = currentStates.toMutableMap()
                                newMap[key] = newVal
                                currentStates = newMap

                                val json = org.json.JSONObject()
                                newMap.forEach { (k, v) -> json.put(k, v) }
                                prefs.doubleTapStates = json.toString()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) { Text(key, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) }

                            Icon(androidx.compose.material.icons.Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp).size(16.dp))

                            Box(
                                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) { Text(shiftChar, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { newVal ->
                                val newMap = currentStates.toMutableMap()
                                newMap[key] = newVal
                                currentStates = newMap

                                val json = org.json.JSONObject()
                                newMap.forEach { (k, v) -> json.put(k, v) }
                                prefs.doubleTapStates = json.toString()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", fontWeight = FontWeight.Bold) }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}