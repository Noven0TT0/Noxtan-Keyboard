package com.noxtan.noxboard.ui.screens.tabs

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.KeyboardSetupHelper
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTab() {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }
    var textState by remember { mutableStateOf("") }

    var isEnabled by remember { mutableStateOf(KeyboardSetupHelper.isKeyboardEnabled(context)) }
    var isSelected by remember { mutableStateOf(KeyboardSetupHelper.isKeyboardSelected(context)) }

    val windowInfo = LocalWindowInfo.current
    LaunchedEffect(windowInfo.isWindowFocused) {
        if (windowInfo.isWindowFocused) {
            isEnabled = KeyboardSetupHelper.isKeyboardEnabled(context)
            isSelected = KeyboardSetupHelper.isKeyboardSelected(context)
        }
    }

    val isReady = isEnabled && isSelected

    var isSoundEnabled by remember { mutableStateOf(prefs.isSoundEnabled) }
    var soundVolume by remember { mutableFloatStateOf(prefs.soundVolume) }
    var selectedSoundPack by remember { mutableStateOf(prefs.selectedSoundPack) }
    var isVibrationEnabled by remember { mutableStateOf(prefs.isVibrationEnabled) }

    val soundPacks = listOf(
        "DEFAULT" to "Default Click",
        "IOS" to "iOS Classic",
        "MECH_CLICKY" to "Mech Clicky",
        "MECH_THOCKY" to "Mech Thocky",
        "MECH_LINEAR" to "Mech Linear",
        "MECH_TACTILE" to "Mech Tactile",
        "MECH_SILENT" to "Mech Silent",
        "BUBBLE" to "Water Bubble",
        "TYPEWRITER" to "Typewriter",
        "WOODEN" to "Wooden Block",
        "SOFT_THUD" to "Soft Thud",
        "SCI_FI" to "Sci-Fi"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isReady) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(!isReady) {
                    context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (isReady) "Keyboard is Active" else "Setup Required",
                        fontWeight = FontWeight.Bold,
                        color = if (isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = if (isReady) "You are ready to type." else "Tap here to enable Nox Board in settings.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            text = "Keyboard Feedback",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Keyboard Sound", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                    }
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = { isSoundEnabled = it; prefs.isSoundEnabled = it }
                    )
                }

                AnimatedVisibility(
                    visible = isSoundEnabled,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Volume: ${(soundVolume * 100).toInt()}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Slider(
                            value = soundVolume,
                            onValueChange = { soundVolume = it; prefs.soundVolume = it },
                            valueRange = 0f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(32.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Sound Style", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(soundPacks) { (packKey, packName) ->
                                val isSelected = selectedSoundPack == packKey
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            selectedSoundPack = packKey
                                            prefs.selectedSoundPack = packKey
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = packName,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Haptic Feedback (Vibration)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                    }
                    Switch(
                        checked = isVibrationEnabled,
                        onCheckedChange = { isVibrationEnabled = it; prefs.isVibrationEnabled = it }
                    )
                }
            }
        }

        Text(
            text = "Try it out",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )

        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            placeholder = { Text("Tap here to start typing...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}