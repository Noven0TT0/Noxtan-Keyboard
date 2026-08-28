package com.noxtan.noxboard.ui.screens.tabs

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.data.NoxDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Undo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab() {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var showClearDictDialog by remember { mutableStateOf(false) }

    var isSuggestionEnabled by remember { mutableStateOf(prefs.isSuggestionEnabled) }
    var isAutoCorrectionEnabled by remember { mutableStateOf(prefs.isAutoCorrectionEnabled) }
    var isNextWordPredictionEnabled by remember { mutableStateOf(prefs.isNextWordPredictionEnabled) }
    var isAutoCapitalizationEnabled by remember { mutableStateOf(prefs.isAutoCapitalizationEnabled) }
    var isDoubleSpacePeriodEnabled by remember { mutableStateOf(prefs.isDoubleSpacePeriodEnabled) }
    var isBlockOffensiveWordsEnabled by remember { mutableStateOf(prefs.isBlockOffensiveWordsEnabled) }
    var isIncognitoModeEnabled by remember { mutableStateOf(prefs.isIncognitoModeEnabled) }

    var isSpaceCursorControlEnabled by remember { mutableStateOf(prefs.isSpaceCursorControlEnabled) }
    var spaceDragDelay by remember { mutableFloatStateOf(prefs.spaceDragDelay) }
    var spaceCursorSensitivity by remember { mutableFloatStateOf(prefs.spaceCursorSensitivity) }

    var shortcutList by remember { mutableStateOf(prefs.customShortcutRow.split(",").filter { it.isNotEmpty() }) }

    var showActionPicker by remember { mutableStateOf(false) }
    var currentEditingGesture by remember { mutableStateOf("") }

    var isDualSwipeEnabled by remember { mutableStateOf(prefs.isDualSwipeEnabled) }
    var isTripleSwipeEnabled by remember { mutableStateOf(prefs.isTripleSwipeEnabled) }

    var dualUp by remember { mutableStateOf(prefs.dualSwipeUp) }
    var dualDown by remember { mutableStateOf(prefs.dualSwipeDown) }
    var dualLeft by remember { mutableStateOf(prefs.dualSwipeLeft) }
    var dualRight by remember { mutableStateOf(prefs.dualSwipeRight) }

    var tripleUp by remember { mutableStateOf(prefs.tripleSwipeUp) }
    var tripleDown by remember { mutableStateOf(prefs.tripleSwipeDown) }
    var tripleLeft by remember { mutableStateOf(prefs.tripleSwipeLeft) }
    var tripleRight by remember { mutableStateOf(prefs.tripleSwipeRight) }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Text Correction", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show Suggestions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Display word suggestions and predictions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isSuggestionEnabled, onCheckedChange = { isSuggestionEnabled = it; prefs.isSuggestionEnabled = it })
                    }

                    AnimatedVisibility(visible = !isSuggestionEnabled) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                            Text("Shortcut Row", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "Tools Menu",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                shortcutList.forEachIndexed { index, action ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable {
                                                currentEditingGesture = "SHORTCUT_$index"
                                                showActionPicker = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(getActionIcon(action), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                }

                                if (shortcutList.size < 8) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable {
                                                currentEditingGesture = "SHORTCUT_NEW"
                                                showActionPicker = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                                    }
                                }

                                val isClassicLayout = prefs.activeLayoutMyanmar == "CLASSIC" || prefs.activeLayoutEnglish == "CLASSIC"
                                val endIcon = if (isClassicLayout) Icons.Default.EmojiEmotions else Icons.Default.Dialpad
                                Icon(endIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Spellcheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Auto-Correction", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Automatically correct misspelled words", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = isAutoCorrectionEnabled, onCheckedChange = { isAutoCorrectionEnabled = it; prefs.isAutoCorrectionEnabled = it })
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Next-Word Prediction", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Show suggestions based on previous words", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isNextWordPredictionEnabled, onCheckedChange = { isNextWordPredictionEnabled = it; prefs.isNextWordPredictionEnabled = it })
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Capitalization", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Capitalize the first word of each sentence", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isAutoCapitalizationEnabled, onCheckedChange = { isAutoCapitalizationEnabled = it; prefs.isAutoCapitalizationEnabled = it })
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Double-Space Period", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Double tap spacebar to insert a period", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isDoubleSpacePeriodEnabled, onCheckedChange = { isDoubleSpacePeriodEnabled = it; prefs.isDoubleSpacePeriodEnabled = it })
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Block Offensive Words", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Do not suggest potentially offensive words", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isBlockOffensiveWordsEnabled, onCheckedChange = { isBlockOffensiveWordsEnabled = it; prefs.isBlockOffensiveWordsEnabled = it })
                    }
                }
            }

            Text("Privacy", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Incognito Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Stop keyboard from learning new words", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = isIncognitoModeEnabled, onCheckedChange = { isIncognitoModeEnabled = it; prefs.isIncognitoModeEnabled = it })
                    }
                }
            }

            Text("Pro Gestures", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Spacebar Cursor Control", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Swipe to move cursor (Double-tap & swipe to select)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isSpaceCursorControlEnabled, onCheckedChange = { isSpaceCursorControlEnabled = it; prefs.isSpaceCursorControlEnabled = it })
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSpaceCursorControlEnabled,
                        enter = androidx.compose.animation.expandVertically(expandFrom = Alignment.Top) + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically(shrinkTowards = Alignment.Top) + androidx.compose.animation.fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Activation Delay: ${spaceDragDelay.toInt()} ms", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Slider(
                                value = spaceDragDelay,
                                onValueChange = { spaceDragDelay = it; prefs.spaceDragDelay = it },
                                valueRange = 100f..500f,
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.height(32.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Movement Speed: ${spaceCursorSensitivity.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Slider(
                                value = spaceCursorSensitivity,
                                onValueChange = { spaceCursorSensitivity = it; prefs.spaceCursorSensitivity = it },
                                valueRange = 1f..10f,
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dual-Finger Swipe", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Swipe with two fingers to trigger actions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isDualSwipeEnabled, onCheckedChange = { isDualSwipeEnabled = it; prefs.isDualSwipeEnabled = it })
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isDualSwipeEnabled,
                        enter = androidx.compose.animation.expandVertically(expandFrom = Alignment.Top) + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically(shrinkTowards = Alignment.Top) + androidx.compose.animation.fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            GestureActionRow("Swipe Up", getActionName(dualUp)) { currentEditingGesture = "DUAL_UP"; showActionPicker = true }
                            GestureActionRow("Swipe Down", getActionName(dualDown)) { currentEditingGesture = "DUAL_DOWN"; showActionPicker = true }
                            GestureActionRow("Swipe Left", getActionName(dualLeft)) { currentEditingGesture = "DUAL_LEFT"; showActionPicker = true }
                            GestureActionRow("Swipe Right", getActionName(dualRight)) { currentEditingGesture = "DUAL_RIGHT"; showActionPicker = true }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Triple-Finger Swipe", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Swipe with three fingers for advanced tools", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isTripleSwipeEnabled, onCheckedChange = { isTripleSwipeEnabled = it; prefs.isTripleSwipeEnabled = it })
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isTripleSwipeEnabled,
                        enter = androidx.compose.animation.expandVertically(expandFrom = Alignment.Top) + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically(shrinkTowards = Alignment.Top) + androidx.compose.animation.fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            GestureActionRow("Swipe Up", getActionName(tripleUp)) { currentEditingGesture = "TRIPLE_UP"; showActionPicker = true }
                            GestureActionRow("Swipe Down", getActionName(tripleDown)) { currentEditingGesture = "TRIPLE_DOWN"; showActionPicker = true }
                            GestureActionRow("Swipe Left", getActionName(tripleLeft)) { currentEditingGesture = "TRIPLE_LEFT"; showActionPicker = true }
                            GestureActionRow("Swipe Right", getActionName(tripleRight)) { currentEditingGesture = "TRIPLE_RIGHT"; showActionPicker = true }
                        }
                    }
                }
            }

            Text("Manage Dictionary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showClearDictDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.Red.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Clear Learned Words", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Red)
                            Text("Delete all words learned by the keyboard", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = showActionPicker,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            androidx.activity.compose.BackHandler { showActionPicker = false }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showActionPicker = false }) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Action", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                }
                val isShortcutEdit = currentEditingGesture.startsWith("SHORTCUT_")
                val listToUse = if (isShortcutEdit) {
                    listOf(
                        "VAULT" to "Vault", "CLIPBOARD" to "Clipboard", "EMOJI" to "Emoji",
                        "LEFT" to "Arrow Left", "RIGHT" to "Arrow Right", "UP" to "Arrow Up", "DOWN" to "Arrow Down",
                        "CHANGE_LANGUAGE" to "Change Language", "TOOLS" to "Tools Menu",
                        "HIDE" to "Hide Keyboard", "UNDO" to "Undo", "REDO" to "Redo",
                        "COPY" to "Copy", "PASTE" to "Paste", "CUT" to "Cut", "SELECT_ALL" to "Select All", "COPY_ALL" to "Copy All"
                    ) + (if (currentEditingGesture != "SHORTCUT_NEW") listOf("REMOVE" to "Remove Item") else emptyList())
                } else {
                    listOf(
                        "NONE" to "None", "CHANGE_LANGUAGE" to "Change Language", "UNDO" to "Undo", "REDO" to "Redo", "COPY" to "Copy",
                        "PASTE" to "Paste", "CUT" to "Cut", "SELECT_ALL" to "Select All", "COPY_ALL" to "Copy All",
                        "CURSOR_LEFT_WORD" to "Move cursor left by word", "CURSOR_RIGHT_WORD" to "Move cursor right by word",
                        "CURSOR_START" to "Move to front", "CURSOR_END" to "Move to back",
                        "EMOJI" to "Open Emoji Mode", "CLIPBOARD" to "Open Clipboard", "VAULT" to "Open Nox Vault", "TOOLS" to "Open Tools Menu", "HIDE" to "Hide Keyboard"
                    )
                }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        listToUse.forEach { (actionId, actionName) ->
                            val isSelected = when {
                                isShortcutEdit -> false
                                currentEditingGesture == "DUAL_UP" -> dualUp == actionId
                                currentEditingGesture == "DUAL_DOWN" -> dualDown == actionId
                                currentEditingGesture == "DUAL_LEFT" -> dualLeft == actionId
                                currentEditingGesture == "DUAL_RIGHT" -> dualRight == actionId
                                currentEditingGesture == "TRIPLE_UP" -> tripleUp == actionId
                                currentEditingGesture == "TRIPLE_DOWN" -> tripleDown == actionId
                                currentEditingGesture == "TRIPLE_LEFT" -> tripleLeft == actionId
                                currentEditingGesture == "TRIPLE_RIGHT" -> tripleRight == actionId
                                else -> false
                            }
                            val textColor = if (actionId == "REMOVE") Color.Red else if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isShortcutEdit) {
                                            val newList = shortcutList.toMutableList()
                                            if (currentEditingGesture == "SHORTCUT_NEW") {
                                                if (actionId != "REMOVE") newList.add(actionId)
                                            } else {
                                                val idx = currentEditingGesture.removePrefix("SHORTCUT_").toInt()
                                                if (actionId == "REMOVE") newList.removeAt(idx) else newList[idx] = actionId
                                            }
                                            shortcutList = newList
                                            prefs.customShortcutRow = newList.joinToString(",")
                                        } else {
                                            when (currentEditingGesture) {
                                                "DUAL_UP" -> { dualUp = actionId; prefs.dualSwipeUp = actionId }
                                                "DUAL_DOWN" -> { dualDown = actionId; prefs.dualSwipeDown = actionId }
                                                "DUAL_LEFT" -> { dualLeft = actionId; prefs.dualSwipeLeft = actionId }
                                                "DUAL_RIGHT" -> { dualRight = actionId; prefs.dualSwipeRight = actionId }
                                                "TRIPLE_UP" -> { tripleUp = actionId; prefs.tripleSwipeUp = actionId }
                                                "TRIPLE_DOWN" -> { tripleDown = actionId; prefs.tripleSwipeDown = actionId }
                                                "TRIPLE_LEFT" -> { tripleLeft = actionId; prefs.tripleSwipeLeft = actionId }
                                                "TRIPLE_RIGHT" -> { tripleRight = actionId; prefs.tripleSwipeRight = actionId }
                                            }
                                        }
                                        showActionPicker = false
                                    }
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                    .padding(vertical = 16.dp, horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = actionName,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected || actionId == "REMOVE") FontWeight.Bold else FontWeight.Normal,
                                    color = textColor
                                )
                                if (isSelected) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        }
                        Spacer(modifier = Modifier.height(48.dp))
                    }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = showClearDictDialog,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { showClearDictDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {}
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.Red.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.Red, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Clear learned words?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This will delete all custom words you have typed. This action cannot be undone.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { showClearDictDialog = false },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                            }
                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val db = com.noxtan.noxboard.data.NoxDatabase.getDatabase(context)
                                        db.suggestionDao().deleteAllSuggestions()
                                        prefs.clearDictTrigger = System.currentTimeMillis()
                                        scope.launch(Dispatchers.Main) {
                                            android.widget.Toast.makeText(context, "Dictionary cleared successfully", android.widget.Toast.LENGTH_SHORT).show()
                                            showClearDictDialog = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getActionName(actionId: String): String {
    return when(actionId) {
        "UNDO" -> "Undo"; "REDO" -> "Redo"; "COPY" -> "Copy"
        "PASTE" -> "Paste"; "CUT" -> "Cut"; "SELECT_ALL" -> "Select All"; "COPY_ALL" -> "Copy All"
        "CHANGE_LANGUAGE" -> "Change Language"
        "CURSOR_LEFT_WORD" -> "Move left by word"; "CURSOR_RIGHT_WORD" -> "Move right by word"
        "CURSOR_START" -> "Move to front"; "CURSOR_END" -> "Move to back"
        "EMOJI" -> "Open Emoji Mode"; "CLIPBOARD" -> "Open Clipboard"
        "VAULT" -> "Open Nox Vault"; "TOOLS" -> "Open Tools Menu"
        "HIDE" -> "Hide Keyboard"
        else -> "None"
    }
}

fun getShortcutActionName(actionId: String): String {
    return when(actionId) {
        "VAULT" -> "Vault"; "CLIPBOARD" -> "Clipboard"; "EMOJI" -> "Emoji"
        "LEFT" -> "Arrow Left"; "RIGHT" -> "Arrow Right"; "UP" -> "Arrow Up"; "DOWN" -> "Arrow Down"
        "HIDE" -> "Hide Keyboard"
        "TOOLS" -> "Tools Menu"
        else -> getActionName(actionId)
    }
}

@Composable
fun GestureActionRow(direction: String, actionName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(direction, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(actionName, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun getActionIcon(action: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when(action) {
        "VAULT" -> androidx.compose.material.icons.Icons.Default.Shield
        "CLIPBOARD" -> androidx.compose.material.icons.Icons.Default.Assignment
        "EMOJI" -> androidx.compose.material.icons.Icons.Default.EmojiEmotions
        "LEFT" -> androidx.compose.material.icons.Icons.Default.ArrowBack
        "RIGHT" -> androidx.compose.material.icons.Icons.Default.ArrowForward
        "HIDE" -> androidx.compose.material.icons.Icons.Default.KeyboardHide
        "UNDO" -> androidx.compose.material.icons.Icons.Default.Undo
        "REDO" -> androidx.compose.material.icons.Icons.Default.Redo
        "COPY" -> androidx.compose.material.icons.Icons.Default.ContentCopy
        "PASTE" -> androidx.compose.material.icons.Icons.Default.ContentPaste
        "CUT" -> androidx.compose.material.icons.Icons.Default.ContentCut
        "SELECT_ALL" -> androidx.compose.material.icons.Icons.Default.SelectAll
        "COPY_ALL" -> androidx.compose.material.icons.Icons.Default.ContentCopy
        "CHANGE_LANGUAGE" -> androidx.compose.material.icons.Icons.Default.Language
        "TOOLS" -> androidx.compose.material.icons.Icons.Default.Settings
        else -> androidx.compose.material.icons.Icons.Default.TouchApp
    }
}