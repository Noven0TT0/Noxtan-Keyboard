package com.noxtan.noxboard.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.ui.utils.topAndBottomNoise
import com.noxtan.noxboard.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun KeyboardSoundSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }

    var isSoundEnabled by remember { mutableStateOf(prefs.isSoundEnabled) }
    var soundVolume by remember { mutableStateOf(prefs.soundVolume) }
    var selectedSoundPack by remember { mutableStateOf(prefs.selectedSoundPack) }
    val scrollState = rememberScrollState()

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

    BackHandler(enabled = true) {
        onBack()
    }

    Scaffold(
        topBar = {
            com.noxtan.noxboard.ui.components.NoxTopBar(
                title = androidx.compose.ui.res.stringResource(R.string.sound_settings_title),
                scrollState = scrollState,
                onBackClick = onBack
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
            var textState by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .widthIn(max = 600.dp)
                    .topAndBottomNoise()
                    .verticalScroll(scrollState)
                    .padding(
                        start = 16.dp,
                        top = innerPadding.calculateTopPadding() + 16.dp,
                        end = 16.dp,
                        bottom = 100.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(androidx.compose.ui.res.stringResource(R.string.keypress_sound_effects), fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = {
                                isSoundEnabled = it
                                prefs.isSoundEnabled = it
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isSoundEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(androidx.compose.ui.res.stringResource(R.string.sound_volume_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${(soundVolume * 100).toInt()}%", modifier = Modifier.width(60.dp), fontSize = 14.sp)
                                    Slider(
                                        value = soundVolume,
                                        onValueChange = {
                                            soundVolume = it
                                            prefs.soundVolume = it
                                        },
                                        valueRange = 0f..1.0f,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(androidx.compose.ui.res.stringResource(R.string.sound_packs_theme), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))

                                val soundPacks = listOf(
                                    "DEFAULT" to androidx.compose.ui.res.stringResource(R.string.sound_default_click),
                                    "SCI_FI" to androidx.compose.ui.res.stringResource(R.string.sound_scifi),
                                    "MECH_CLICKY" to androidx.compose.ui.res.stringResource(R.string.sound_mech_clicky),
                                    "MECH_THOCKY" to androidx.compose.ui.res.stringResource(R.string.sound_mech_thocky),
                                    "MECH_LINEAR" to androidx.compose.ui.res.stringResource(R.string.sound_mech_linear),
                                    "MECH_TACTILE" to androidx.compose.ui.res.stringResource(R.string.sound_mech_tactile),
                                    "MECH_SILENT" to androidx.compose.ui.res.stringResource(R.string.sound_mech_silent),
                                    "TYPEWRITER" to androidx.compose.ui.res.stringResource(R.string.sound_typewriter),
                                    "WOODEN" to androidx.compose.ui.res.stringResource(R.string.sound_wooden),
                                    "BUBBLE" to androidx.compose.ui.res.stringResource(R.string.sound_bubble),
                                    "IOS" to androidx.compose.ui.res.stringResource(R.string.sound_ios),
                                    "SOFT_THUD" to androidx.compose.ui.res.stringResource(R.string.sound_soft_thud)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    soundPacks.forEach { (packKey, packName) ->
                                        val isSelected = selectedSoundPack == packKey
                                        Button(
                                            onClick = {
                                                selectedSoundPack = packKey
                                                prefs.selectedSoundPack = packKey
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = packName,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.Black else Color.White,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            com.noxtan.noxboard.ui.components.KeyboardPreviewArea(
                textState = textState,
                onTextChange = { textState = it },
                isImeVisible = isImeVisible,
                isSlidingActive = false,
                focusRequester = focusRequester,
                keyboardController = keyboardController,
                animatedWidth = animatedWidth,
                animatedHeight = animatedHeight,
                animatedCornerRadius = animatedCornerRadius,
                animatedBgColor = animatedBgColor,
                animatedBorderColor = animatedBorderColor,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}