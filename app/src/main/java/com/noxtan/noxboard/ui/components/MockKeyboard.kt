package com.noxtan.noxboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.North
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MockKeyboard(
    bg: Color,
    keyColor: Color,
    textColor: Color,
    specKeyColor: Color,
    specTextColor: Color,
    sugBgColor: Color = bg,
    sugTextColor: Color = specTextColor,
    keyBorderColor: Color = Color.Transparent,
    specKeyBorderColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
    scale: Float = 1f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape((16 * scale).dp))
            .background(bg)
            .padding((8 * scale).dp),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy((6 * scale).dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape((6 * scale).dp))
                    .background(sugBgColor)
                    .padding(vertical = (4 * scale).dp)
            ) {
                Text("Nox", color = sugTextColor, fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold)
                Text("Board", color = sugTextColor, fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold)
                Text("Theme", color = sugTextColor, fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy((4 * scale).dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Q","W","E","R","T","Y","U","I","O","P").forEach { text ->
                    MiniKey(keyColor, textColor, keyBorderColor, text = text, weight = 1f, scale = scale)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy((4 * scale).dp), modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(0.5f))
                listOf("A","S","D","F","G","H","J","K","L").forEach { text ->
                    MiniKey(keyColor, textColor, keyBorderColor, text = text, weight = 1f, scale = scale)
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy((4 * scale).dp), modifier = Modifier.fillMaxWidth()) {
                MiniKey(specKeyColor, specTextColor, specKeyBorderColor, weight = 1.5f, icon = Icons.Default.North, scale = scale)
                listOf("Z","X","C","V","B","N","M").forEach { text ->
                    MiniKey(keyColor, textColor, keyBorderColor, text = text, weight = 1f, scale = scale)
                }
                MiniKey(specKeyColor, specTextColor, specKeyBorderColor, weight = 1.5f, icon = Icons.Default.KeyboardBackspace, scale = scale)
            }
            Row(horizontalArrangement = Arrangement.spacedBy((4 * scale).dp), modifier = Modifier.fillMaxWidth()) {
                MiniKey(specKeyColor, specTextColor, specKeyBorderColor, weight = 1.5f, text = "?123", scale = scale)
                MiniKey(specKeyColor, specTextColor, specKeyBorderColor, weight = 1f, icon = Icons.Default.Language, scale = scale)
                MiniKey(keyColor, textColor, keyBorderColor, weight = 5f, text = "Space", scale = scale)
                MiniKey(specKeyColor, specTextColor, specKeyBorderColor, weight = 2.5f, text = "⏎", scale = scale)
            }
        }
    }
}

@Composable
fun RowScope.MiniKey(
    bgColor: Color,
    textColor: Color,
    borderColor: Color = Color.Transparent,
    weight: Float,
    text: String? = null,
    icon: ImageVector? = null,
    scale: Float = 1f
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .height((32 * scale).dp)
            .clip(RoundedCornerShape((5 * scale).dp))
            .background(bgColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border((1 * scale).dp, borderColor, RoundedCornerShape((5 * scale).dp))
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.8f),
                modifier = Modifier.size((14 * scale).dp)
            )
        } else if (text != null) {
            Text(
                text = text,
                color = textColor,
                fontSize = if (text == "Space" || text == "?123") (9 * scale).sp else if (text == "⏎") (14 * scale).sp else (11 * scale).sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}