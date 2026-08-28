package com.noxtan.noxboard.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.R
import com.noxtan.noxboard.ui.components.NoxTopBar
import com.noxtan.noxboard.ui.utils.topAndBottomNoise

@Composable
fun KeyboardLayoutScreen(
    onBack: () -> Unit,
    onNavigateToTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }

    var selectedStyle by remember { mutableStateOf("MODERN") }

    Scaffold(
        topBar = {
            NoxTopBar(
                title = "Keyboard Style",
                scrollState = scrollState,
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 600.dp)
                .padding(top = innerPadding.calculateTopPadding(), start = 16.dp, end = 16.dp)
                .topAndBottomNoise()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose your preferred keyboard layout style. You can customize the theme colors of your selected style.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            StyleOptionCard(
                title = "Modern Style",
                description = "Standard spaced layout, comfortable for fast typing.",
                iconRes = R.drawable.ic_nox_logo,
                isSelected = selectedStyle == "MODERN",
                onClick = { selectedStyle = "MODERN" },
                onEditClick = onNavigateToTheme
            )

            StyleOptionCard(
                title = "Classic Style",
                description = "Traditional placement with familiar secondary keys.",
                iconRes = R.drawable.ic_nox_logo,
                isSelected = selectedStyle == "CLASSIC",
                onClick = { selectedStyle = "CLASSIC" },
                onEditClick = onNavigateToTheme
            )

            StyleOptionCard(
                title = "System Style",
                description = "Compact layout similar to default device keyboards.",
                iconRes = R.drawable.ic_nox_logo,
                isSelected = selectedStyle == "SYSTEM",
                onClick = { selectedStyle = "SYSTEM" },
                onEditClick = onNavigateToTheme
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun StyleOptionCard(
    title: String,
    description: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .border(2.dp, borderColor, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(contentAlignment = Alignment.Center) {
                if (isSelected) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Theme",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), CircleShape)
                    )
                }
            }
        }
    }
}