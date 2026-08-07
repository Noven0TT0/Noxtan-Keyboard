package com.noxtan.noxboard.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.topAndBottomNoise(
    fadeHeightDp: Dp = 80.dp,
    edgeDarkenAlpha: Float = 1.0f,
    fadeColor: Color = Color.Unspecified
): Modifier = composed {
    val resolvedFadeColor = if (fadeColor == Color.Unspecified) {
        androidx.compose.material3.MaterialTheme.colorScheme.background
    } else {
        fadeColor
    }

    this.drawWithCache {
        val fadeHeightPx = fadeHeightDp.toPx()
        val totalHeight = size.height

        val actualFadeHeightPx = if (totalHeight < fadeHeightPx * 2f) {
            totalHeight / 2f
        } else {
            fadeHeightPx
        }

        val fadeFraction = if (totalHeight > 0f) actualFadeHeightPx / totalHeight else 0.10f

        val stops = mutableListOf<Pair<Float, Color>>()
        val steps = 15

        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val easedAlpha = edgeDarkenAlpha * (1f - t)
            stops.add((t * fadeFraction) to resolvedFadeColor.copy(alpha = easedAlpha))
        }

        stops.add(0.5f to Color.Transparent)

        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val invT = 1f - t
            val easedAlpha = edgeDarkenAlpha * (1f - invT)
            stops.add((1f - fadeFraction + (t * fadeFraction)) to resolvedFadeColor.copy(alpha = easedAlpha))
        }

        val darkenBrush = Brush.verticalGradient(*stops.toTypedArray())

        onDrawWithContent {
            drawContent()
            drawRect(brush = darkenBrush)
        }
    }
}