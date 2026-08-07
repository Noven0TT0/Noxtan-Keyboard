package com.noxtan.noxboard.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs
import androidx.compose.ui.res.stringResource
import com.noxtan.noxboard.R

@Composable
fun ActiveSliderOverlay(
    activeSlider: String?,
    keyboardWidth: Float,
    onKeyboardWidthChange: (Float) -> Unit,
    keyboardHeight: Float,
    onKeyboardHeightChange: (Float) -> Unit,
    bottomPadding: Float,
    onBottomPaddingChange: (Float) -> Unit,
    onActiveSliderChange: (String?) -> Unit,
    prefs: NoxBoardPrefs
) {
    AnimatedVisibility(
        visible = activeSlider != null,
        enter = fadeIn(animationSpec = tween(150)) + slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(targetOffsetY = { -40 }, animationSpec = tween(150))
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (activeSlider) {
                        "WIDTH" -> stringResource(R.string.adjust_width_overlay)
                        "HEIGHT" -> stringResource(R.string.adjust_height_overlay)
                        "FLOAT" -> stringResource(R.string.adjust_float_overlay)
                        else -> ""
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (activeSlider) {
                        "WIDTH" -> "${keyboardWidth.toInt()}%"
                        "HEIGHT" -> "${keyboardHeight.toInt()}dp"
                        "FLOAT" -> "${bottomPadding.toInt()}dp"
                        else -> ""
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = when (activeSlider) {
                        "WIDTH" -> keyboardWidth
                        "HEIGHT" -> keyboardHeight
                        "FLOAT" -> bottomPadding
                        else -> 0f
                    },
                    onValueChange = { newValue ->
                        when (activeSlider) {
                            "WIDTH" -> {
                                onKeyboardWidthChange(newValue)
                                prefs.slideWidth = newValue
                            }
                            "HEIGHT" -> {
                                onKeyboardHeightChange(newValue)
                                prefs.slideHeight = newValue
                            }
                            "FLOAT" -> {
                                onBottomPaddingChange(newValue)
                                prefs.slideBottomPadding = newValue
                            }
                        }
                    },
                    onValueChangeFinished = {
                        prefs.isSliding = false
                        when (activeSlider) {
                            "WIDTH" -> prefs.keyboardWidth = keyboardWidth
                            "HEIGHT" -> prefs.keyboardHeight = keyboardHeight
                            "FLOAT" -> prefs.bottomPadding = bottomPadding
                        }
                        onActiveSliderChange(null)
                    },
                    valueRange = when (activeSlider) {
                        "WIDTH" -> 70f..100f
                        "HEIGHT" -> 180f..300f
                        "FLOAT" -> 0f..100f
                        else -> 0f..100f
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}