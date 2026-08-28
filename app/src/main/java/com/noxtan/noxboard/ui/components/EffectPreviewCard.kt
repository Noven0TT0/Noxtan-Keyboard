package com.noxtan.noxboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun EffectPreviewCard(
    effectType: String,
    effectColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF15151D)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(effectColor.copy(alpha = 0.45f), Color.Transparent)
                    )
                )
        )

        when (effectType) {
            "EFFECT_LASER" -> Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(effectColor.copy(alpha = 0.8f)))
            "EFFECT_SPARK" -> Box(modifier = Modifier.size(24.dp).border(0.8.dp, effectColor.copy(alpha = 0.7f), CircleShape))
            "EFFECT_PULSE" -> Box(modifier = Modifier.size(36.dp).border(1.dp, effectColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)))
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .shadow(2.dp, RoundedCornerShape(6.dp), clip = false)
                .background(Color(0xFF2E2E3A), RoundedCornerShape(6.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "A", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}