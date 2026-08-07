package com.noxtan.noxboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.activity.compose.BackHandler
import com.noxtan.noxboard.ui.utils.topAndBottomNoise
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.noxtan.noxboard.ui.components.KeyboardPreviewArea
import com.noxtan.noxboard.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SpacebarControlScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }

    var isEnabled by remember { mutableStateOf(prefs.isSpaceCursorControlEnabled) }
    var sensitivity by remember { mutableStateOf(prefs.spaceCursorSensitivity) }
    var dragDelay by remember { mutableStateOf(prefs.spaceDragDelay) }
    val scrollState = rememberScrollState()

    BackHandler(enabled = true) {
        onBack()
    }

    Scaffold(
        topBar = {
            com.noxtan.noxboard.ui.components.NoxTopBar(
                title = androidx.compose.ui.res.stringResource(R.string.spacebar_control_title),
                scrollState = scrollState,
                onBackClick = onBack,
                actions = {
                    TextButton(onClick = {
                        isEnabled = true
                        sensitivity = 2.5f
                        dragDelay = 300f
                        prefs.isSpaceCursorControlEnabled = true
                        prefs.spaceCursorSensitivity = 2.5f
                        prefs.spaceDragDelay = 300f
                    }) {
                        Text(androidx.compose.ui.res.stringResource(R.string.reset_btn), color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        val previewHint = androidx.compose.ui.res.stringResource(R.string.spacebar_preview_text)
        var textState by remember { mutableStateOf(previewHint) }
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

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
        ) {
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
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(androidx.compose.ui.res.stringResource(R.string.spacebar_cursor_control_title), fontWeight = FontWeight.Bold, color = Color.White)
                            Text(androidx.compose.ui.res.stringResource(R.string.spacebar_cursor_control_desc), fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it; prefs.isSpaceCursorControlEnabled = it }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(androidx.compose.ui.res.stringResource(R.string.movement_speed), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(androidx.compose.ui.res.stringResource(R.string.speed_slow), modifier = Modifier.width(40.dp), fontSize = 12.sp, color = Color.Gray)
                                    Slider(
                                        value = sensitivity,
                                        onValueChange = { sensitivity = it; prefs.spaceCursorSensitivity = it },
                                        valueRange = 1f..10f,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(androidx.compose.ui.res.stringResource(R.string.speed_fast), modifier = Modifier.width(40.dp), fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(androidx.compose.ui.res.stringResource(R.string.press_delay_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(androidx.compose.ui.res.stringResource(R.string.press_delay_desc), fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${dragDelay.toInt()} ms", modifier = Modifier.width(55.dp), fontSize = 14.sp, color = Color.White)
                                    Slider(
                                        value = dragDelay,
                                        onValueChange = { dragDelay = it; prefs.spaceDragDelay = it },
                                        valueRange = 0f..500f,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            KeyboardPreviewArea(
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isImeVisible) 0.dp else innerPadding.calculateBottomPadding())
            )
        }
    }
}