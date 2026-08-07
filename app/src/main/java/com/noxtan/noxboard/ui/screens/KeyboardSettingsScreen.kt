package com.noxtan.noxboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.R
import com.noxtan.noxboard.ui.utils.topAndBottomNoise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.noxtan.noxboard.data.NoxDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardSettingsScreen(
    onNavigateToSpacebarControl: () -> Unit,
    onNavigateToDoubleTapSettings: () -> Unit,
    onNavigateToResize: () -> Unit,
    onNavigateToSoundSettings: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToRearrangeKeys: () -> Unit,
    onNavigateToLanguageSelection: () -> Unit,
    onNavigateToAbout: () -> Unit,
    scrollState: androidx.compose.foundation.ScrollState = androidx.compose.foundation.rememberScrollState(),
    modifier: Modifier = Modifier
) {
    var showClearDictDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }

    val importAllLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonStr = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: ""

                val json = org.json.JSONObject(jsonStr)
                val sharedPrefs = context.getSharedPreferences("noxboard_prefs", android.content.Context.MODE_PRIVATE)
                val editor = sharedPrefs.edit()
                val keys = json.keys()

                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = json.get(key)
                    if (key == "clear_dict_trigger") {
                        editor.putLong(key, json.optLong(key))
                    } else {
                        when (value) {
                            is Boolean -> editor.putBoolean(key, value)
                            is Number -> editor.putFloat(key, value.toFloat())
                            is String -> editor.putString(key, value)
                        }
                    }
                }
                editor.apply()

                android.widget.Toast.makeText(context, "All settings & configurations imported successfully!", android.widget.Toast.LENGTH_SHORT).show()

                (context as? android.app.Activity)?.recreate()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Import failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val exportAll = {
        try {
            val sharedPrefs = context.getSharedPreferences("noxboard_prefs", android.content.Context.MODE_PRIVATE)
            val allPrefs = sharedPrefs.all
            val json = org.json.JSONObject()

            for ((key, value) in allPrefs) {
                json.put(key, value)
            }

            val file = java.io.File(context.cacheDir, "nox_all_backup_${System.currentTimeMillis()}.noxbackup")
            file.writeText(json.toString(), Charsets.UTF_8)

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
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Export All Settings via"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Export failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    var isSpaceCursorControlEnabled by remember { mutableStateOf(prefs.isSpaceCursorControlEnabled) }
    var spaceCursorSensitivity by remember { mutableStateOf(prefs.spaceCursorSensitivity) }

    var isVibrationEnabled by remember { mutableStateOf(prefs.isVibrationEnabled) }
    var isSoundEnabled by remember { mutableStateOf(prefs.isSoundEnabled) }
    var isNumberRowEnabled by remember { mutableStateOf(prefs.isNumberRowEnabled) }
    var isKeyPreviewEnabled by remember { mutableStateOf(prefs.isKeyPreviewEnabled) }
    var isSuggestionEnabled by remember { mutableStateOf(prefs.isSuggestionEnabled) }
    var isIncognitoModeEnabled by remember { mutableStateOf(prefs.isIncognitoModeEnabled) }
    var isAutoCorrectionEnabled by remember { mutableStateOf(prefs.isAutoCorrectionEnabled) }
    var isNextWordPredictionEnabled by remember { mutableStateOf(prefs.isNextWordPredictionEnabled) }
    var isAutoCapitalizationEnabled by remember { mutableStateOf(prefs.isAutoCapitalizationEnabled) }
    var isAutoSpaceEnabled by remember { mutableStateOf(prefs.isAutoSpaceEnabled) }
    var isBlockOffensiveWordsEnabled by remember { mutableStateOf(prefs.isBlockOffensiveWordsEnabled) }
    var isDoubleSpacePeriodEnabled by remember { mutableStateOf(prefs.isDoubleSpacePeriodEnabled) }

    var showMenu by remember { mutableStateOf(false) }
    var myanmarTypingStyle by remember { mutableStateOf(prefs.myanmarTypingStyle) }
    var showTypingStyleDialog by remember { mutableStateOf(false) }
    var isUnicodeStyle by remember { mutableStateOf(myanmarTypingStyle == "UNICODE") }

    Scaffold(
        topBar = {
            com.noxtan.noxboard.ui.components.NoxTopBar(
                title = stringResource(R.string.settings_title),
                scrollState = scrollState,
                onBackClick = null,
                actions = { iconBgColor ->
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.clip(androidx.compose.foundation.shape.CircleShape).background(iconBgColor)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.app_language), fontWeight = FontWeight.Bold) },
                            onClick = { showMenu = false; onNavigateToLanguageSelection() }
                        )

                        DropdownMenuItem(
                            text = { Text("Export All", fontWeight = FontWeight.Bold) },
                            onClick = {
                                showMenu = false
                                exportAll()
                            },
                            trailingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )

                        DropdownMenuItem(
                            text = { Text("Import All", fontWeight = FontWeight.Bold) },
                            onClick = {
                                showMenu = false
                                importAllLauncher.launch(arrayOf("*/*"))
                            },
                            trailingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.share_app), fontWeight = FontWeight.Bold) },
                            onClick = {
                                showMenu = false
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, context.getString(R.string.share_app_desc, context.packageName))
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, context.getString(R.string.share_app_via)))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("About Nox Board", fontWeight = FontWeight.Bold) },
                            onClick = { showMenu = false; onNavigateToAbout() }
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        var cachedTopPadding by remember { mutableStateOf(0.dp) }
        val currentTopPadding = innerPadding.calculateTopPadding()
        if (currentTopPadding > 0.dp) cachedTopPadding = currentTopPadding

        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 600.dp)
                .topAndBottomNoise()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                Color(0xFF7C4DFF)
                            )
                        )
                    )
                    .clickable { onNavigateToTheme() }
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_color_plate),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.keyboard_themes),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(text = stringResource(R.string.theme_desc), fontSize = 12.sp, color = Color.Black.copy(alpha = 0.7f))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 145.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onNavigateToResize() }
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.BottomStart)) {
                        Icon(
                            Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.adjust_size),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = stringResource(R.string.adjust_size_desc),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 145.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onNavigateToRearrangeKeys() }
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.BottomStart)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.rearrange_keys),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = stringResource(R.string.rearrange_keys_desc),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.controls_feedback),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Numbers, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.num_row), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(stringResource(R.string.num_row_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isNumberRowEnabled,
                        onCheckedChange = {
                            isNumberRowEnabled = it
                            prefs.isNumberRowEnabled = it
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.KeyboardCapslock, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.key_preview), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(stringResource(R.string.key_preview_desc_short), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isKeyPreviewEnabled,
                        onCheckedChange = {
                            isKeyPreviewEnabled = it
                            prefs.isKeyPreviewEnabled = it
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showTypingStyleDialog = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.typing_style), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = when (myanmarTypingStyle) {
                                    "UNICODE" -> "${stringResource(R.string.typing_style_unicode)} (ဖ + ြ + ေ)"
                                    "VISUAL_SMART" -> "${stringResource(R.string.typing_style_visual)} (ေ + ဖ + ြ)"
                                    else -> "${stringResource(R.string.typing_style_zawgyi)} (ေ + ြ + ဖ)"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.vibrate), fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    }
                    Switch(
                        checked = isVibrationEnabled,
                        onCheckedChange = {
                            isVibrationEnabled = it
                            prefs.isVibrationEnabled = it
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateToDoubleTapSettings() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.double_tap_shortcuts), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(stringResource(R.string.double_tap_shortcuts_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateToSpacebarControl() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.SpaceBar, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.spacebar_cursor), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(stringResource(R.string.spacebar_cursor_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = isSpaceCursorControlEnabled,
                            onCheckedChange = {
                                isSpaceCursorControlEnabled = it
                                prefs.isSpaceCursorControlEnabled = it
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateToSoundSettings() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.sound), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(stringResource(R.string.sound_desc_short), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = {
                                isSoundEnabled = it
                                prefs.isSoundEnabled = it
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.text_correction_suggestions),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.show_suggestion_strip), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text(stringResource(R.string.show_suggestion_strip_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isSuggestionEnabled, onCheckedChange = { isSuggestionEnabled = it; prefs.isSuggestionEnabled = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.incognito_mode), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text(stringResource(R.string.incognito_mode_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isIncognitoModeEnabled, onCheckedChange = { isIncognitoModeEnabled = it; prefs.isIncognitoModeEnabled = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.auto_correction), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isSuggestionEnabled) Color.White else Color.Gray)
                        Text(stringResource(R.string.auto_correction_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isAutoCorrectionEnabled, onCheckedChange = { isAutoCorrectionEnabled = it; prefs.isAutoCorrectionEnabled = it }, enabled = isSuggestionEnabled)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.auto_capitalization), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text(stringResource(R.string.auto_capitalization_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isAutoCapitalizationEnabled, onCheckedChange = { isAutoCapitalizationEnabled = it; prefs.isAutoCapitalizationEnabled = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.auto_space), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text(stringResource(R.string.auto_space_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isAutoSpaceEnabled, onCheckedChange = { isAutoSpaceEnabled = it; prefs.isAutoSpaceEnabled = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.double_space_period), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text(stringResource(R.string.double_space_period_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isDoubleSpacePeriodEnabled, onCheckedChange = { isDoubleSpacePeriodEnabled = it; prefs.isDoubleSpacePeriodEnabled = it })
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.block_offensive), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isSuggestionEnabled) Color.White else Color.Gray)
                        Text(stringResource(R.string.block_offensive_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isBlockOffensiveWordsEnabled, onCheckedChange = { isBlockOffensiveWordsEnabled = it; prefs.isBlockOffensiveWordsEnabled = it }, enabled = isSuggestionEnabled)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showClearDictDialog = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.Red.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.clear_dict), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Red)
                            Text(stringResource(R.string.clear_dict_desc), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 48.dp))
        }

        if (showClearDictDialog) {
            AlertDialog(
                onDismissRequest = { showClearDictDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.clear_dict_dialog_title),
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.clear_dict_dialog_msg),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val db = NoxDatabase.getDatabase(context)
                            db.suggestionDao().deleteAllSuggestions()

                            prefs.clearDictTrigger = System.currentTimeMillis()
                        }
                        showClearDictDialog = false
                        android.widget.Toast.makeText(context, context.getString(R.string.dict_cleared_toast), android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Text(stringResource(R.string.delete_btn), color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDictDialog = false }) {
                        Text(stringResource(R.string.cancel), color = Color.Gray)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (showTypingStyleDialog) {
            AlertDialog(
                onDismissRequest = { showTypingStyleDialog = false },
                title = {
                    Column {
                        Text(
                            text = "Typing Style",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.typing_style_prompt),
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val styles = listOf(
                            Triple("UNICODE", "ဖ + ြ + ေ", stringResource(R.string.typing_style_unicode)),
                            Triple("VISUAL_SMART", "ေ + ဖ + ြ", stringResource(R.string.typing_style_visual)),
                            Triple("ZAWGYI", "ေ + ြ + ဖ", stringResource(R.string.typing_style_zawgyi))
                        )

                        styles.forEach { (styleCode, sequence, desc) ->
                            val isSelected = myanmarTypingStyle == styleCode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable {
                                        myanmarTypingStyle = styleCode
                                        prefs.myanmarTypingStyle = styleCode
                                        showTypingStyleDialog = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = sequence,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = desc,
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTypingStyleDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}