package com.noxtan.noxboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.ui.models.CustomizerColorPreset

@Composable
fun SwatchPreviewCard(
    preset: CustomizerColorPreset,
    modifier: Modifier = Modifier
) {
    val bg = Color(android.graphics.Color.parseColor(preset.bgHex))
    val keyColor = Color(android.graphics.Color.parseColor(preset.keyHex))
    val textColor = Color(android.graphics.Color.parseColor(preset.textHex))
    val specColor = Color(android.graphics.Color.parseColor(preset.specKeyHex))
    val specText = Color(android.graphics.Color.parseColor(preset.specTextHex))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SwatchMiniKey(bgColor = keyColor, textColor = textColor, label = "A", modifier = Modifier.weight(1f))
            SwatchMiniKey(bgColor = specColor, textColor = specText, label = "Fn", modifier = Modifier.weight(1.2f), isSpecial = true)
            SwatchMiniKey(bgColor = specColor, textColor = specText, label = "⏎", modifier = Modifier.weight(1.3f), isSpecial = true)
        }
    }
}

@Composable
fun RowScope.SwatchMiniKey(
    bgColor: Color,
    textColor: Color,
    label: String,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(4.dp), clip = false)
            .background(bgColor, RoundedCornerShape(4.dp))
            .border(
                width = 0.5.dp,
                color = if (isSpecial) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor.copy(alpha = 0.9f),
            fontSize = if (label == "⏎") 18.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
            )
        )
    }
}