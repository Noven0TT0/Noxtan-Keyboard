package com.noxtan.noxboard.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun NoxTopBar(
    title: String,
    scrollState: ScrollState,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.(iconBgColor: Color) -> Unit = {}
) {
    val isScrolled = scrollState.value > 50

    val iconBgColor by animateColorAsState(
        targetValue = if (isScrolled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f),
        animationSpec = tween(200),
        label = "IconBackgroundColor"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        var cachedStatusBarHeight by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0.dp) }
        val currentStatusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        if (currentStatusBarHeight > 0.dp) cachedStatusBarHeight = currentStatusBarHeight
        val statusBarHeight = if (cachedStatusBarHeight > 0.dp) cachedStatusBarHeight else currentStatusBarHeight

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarHeight)
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.clip(CircleShape).background(iconBgColor)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                actions(iconBgColor)
            }
        }
    }
}