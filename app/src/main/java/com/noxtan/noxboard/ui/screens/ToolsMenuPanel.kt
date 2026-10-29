package com.noxtan.noxboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.R
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset

@Composable
fun ToolsMenuPanel(
    bottomPaddingDp: Float,
    hasSelection: Boolean,
    isOneHandMode: Boolean,
    isFloatingMode: Boolean,
    onClose: () -> Unit,
    onActionClick: (String) -> Unit,
    onCutClick: () -> Unit,
    onCopyClick: () -> Unit,
    onPasteClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onLeftArrowClick: () -> Unit,
    onRightArrowClick: () -> Unit,
    onClipboardHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFloatingClick: () -> Unit,
    onResizeClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = androidx.compose.runtime.remember { com.noxtan.noxboard.NoxBoardPrefs(context) }

    val bgColor = try { Color(android.graphics.Color.parseColor(prefs.customBackgroundColor)) } catch(e:Exception){ Color.Black }
    val surfaceColor = try { Color(android.graphics.Color.parseColor(prefs.customSpecialKeyColor)) } catch(e:Exception){ Color.DarkGray }
    val onSurface = try { Color(android.graphics.Color.parseColor(prefs.customTextColor)) } catch(e:Exception){ Color.White }

    val shortcutList = prefs.customShortcutRow.split(",").filter { it.isNotEmpty() }.take(6)

    val isFloating = isFloatingMode
    val isOneHand = isOneHandMode

    data class ToolDef(
        val id: String,
        val name: String,
        val iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
        val iconRes: Int? = null,
        val isActive: Boolean = false,
        val click: () -> Unit
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(bottom = bottomPaddingDp.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        val copyPasteName = if (hasSelection) "Copy" else "Paste"
        val copyPasteIcon = if (hasSelection) Icons.Default.ContentCopy else Icons.Default.ContentPaste
        val copyPasteClick = if (hasSelection) onCopyClick else onPasteClick

        val allTools = listOf(
            ToolDef("CLIPBOARD", copyPasteName, iconVector = copyPasteIcon, click = copyPasteClick),
            ToolDef("CUT", "Cut", iconVector = Icons.Default.ContentCut, click = onCutClick),
            ToolDef("SELECT_ALL", "Select All", iconRes = R.drawable.ic_select_all, click = onSelectAllClick),
            ToolDef("LEFT", "Left", iconVector = Icons.Default.ArrowBack, click = onLeftArrowClick),
            ToolDef("RIGHT", "Right", iconVector = Icons.Default.ArrowForward, click = onRightArrowClick),
            ToolDef("HISTORY", "History", iconVector = Icons.Default.Assignment, click = onClipboardHistoryClick),
            ToolDef(
                id = "FLOATING",
                name = if (isFloating) "Dock" else "Floating",
                iconRes = R.drawable.ic_float,
                isActive = isFloating,
                click = onFloatingClick
            ),
            ToolDef("RESIZE", "Resize", iconVector = Icons.Default.OpenWith, click = onResizeClick),
            ToolDef(
                id = "ONE_HAND",
                name = if (isOneHand) "Expand" else "One Hand",
                iconVector = null,
                iconRes = R.drawable.ic_one_hand,
                isActive = isOneHand,
                click = { onActionClick("ONE_HAND") }
            )
        )

        val toolsToShow = allTools.filter { it.id !in shortcutList && !(isFloating && it.id == "ONE_HAND") }

        toolsToShow.chunked(4).forEach { rowTools ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowTools.forEach { tool ->
                    CompactToolItem(
                        title = tool.name,
                        iconVector = tool.iconVector,
                        iconRes = tool.iconRes,
                        isActive = tool.isActive,
                        bg = surfaceColor,
                        tint = onSurface,
                        onClick = tool.click,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowTools.size < 4) {
                    repeat(4 - rowTools.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CompactToolItem(
    title: String,
    iconVector: ImageVector? = null,
    iconRes: Int? = null,
    isActive: Boolean = false,
    bg: Color,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = androidx.compose.runtime.remember { com.noxtan.noxboard.NoxBoardPrefs(context) }
    val activeColor = try { Color(android.graphics.Color.parseColor(prefs.customActiveKeyColor)) } catch(e:Exception){ Color.Blue }
    val bgOled = try { Color(android.graphics.Color.parseColor(prefs.customBackgroundColor)) } catch(e:Exception){ Color.Black }

    val circleBg = if (isActive) activeColor else bg
    val iconTint = if (isActive) bgOled else tint
    val textColor = if (isActive) activeColor else tint

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(circleBg),
            contentAlignment = Alignment.Center
        ) {
            if (iconVector != null) {
                Icon(iconVector, contentDescription = title, tint = iconTint, modifier = Modifier.size(22.dp))
            } else if (iconRes != null) {
                val iconSize = if (iconRes == R.drawable.ic_one_hand) 28.dp else 22.dp
                Icon(painterResource(id = iconRes), contentDescription = title, tint = iconTint, modifier = Modifier.size(iconSize))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ClipboardHistoryPanel(
    bottomPaddingDp: Float,
    historyList: List<String>,
    pinnedList: List<String>,
    onClose: () -> Unit,
    onItemClick: (String) -> Unit,
    onPinClick: (String, Boolean) -> Unit,
    onDeleteClick: (String, Boolean) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = androidx.compose.runtime.remember { com.noxtan.noxboard.NoxBoardPrefs(context) }
    val bgColor = try { Color(android.graphics.Color.parseColor(prefs.customBackgroundColor)) } catch(e:Exception){ Color.Black }
    val surfaceColor = try { Color(android.graphics.Color.parseColor(prefs.customSpecialKeyColor)) } catch(e:Exception){ Color.DarkGray }
    val onSurface = try { Color(android.graphics.Color.parseColor(prefs.customTextColor)) } catch(e:Exception){ Color.White }

    var showPinnedView by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = bottomPaddingDp.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (showPinnedView) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(surfaceColor)
                            .clickable { showPinnedView = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = onSurface, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Pinned Items", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onSurface)
                }
            } else {
                Text("Clipboard", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onSurface)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!showPinnedView) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(surfaceColor)
                            .clickable { showPinnedView = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.PushPin, contentDescription = "Pinned", tint = onSurface, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(surfaceColor)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = onSurface, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val currentList = if (showPinnedView) pinnedList else historyList

        if (currentList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (showPinnedView) "No pinned items." else "No copied items yet.", color = onSurface.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentList, key = { it.hashCode() }) { text ->
                    val isAlreadyPinned = pinnedList.contains(text)
                    SwipeableClipboardItem(
                        text = text,
                        isInitiallyPinned = isAlreadyPinned,
                        surfaceColor = surfaceColor,
                        onSurface = onSurface,
                        onItemClick = { onItemClick(text) },
                        onPinClick = { isPinnedNow -> onPinClick(text, isPinnedNow) },
                        onDeleteClick = { onDeleteClick(text, showPinnedView) }
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeableClipboardItem(
    text: String,
    isInitiallyPinned: Boolean,
    surfaceColor: Color,
    onSurface: Color,
    onItemClick: () -> Unit,
    onPinClick: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    var offsetX by androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val maxSwipe = with(density) { -120.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = {
                    offsetX = 0f
                    onPinClick(!isInitiallyPinned)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.PushPin,
                    contentDescription = "Save/Pin",
                    tint = if (isInitiallyPinned) MaterialTheme.colorScheme.primary else onSurface.copy(alpha = 0.4f)
                )
            }
            IconButton(
                onClick = {
                    offsetX = 0f
                    onDeleteClick()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(surfaceColor)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            offsetX = if (offsetX < maxSwipe / 2) maxSwipe else 0f
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount).coerceIn(maxSwipe, 0f)
                    }
                }
                .clickable { onItemClick() }
                .padding(16.dp)
        ) {
            Text(
                text = text,
                color = onSurface,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ShortcutActionItem(action: String, tint: Color, hasSelection: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        when (action) {
            "CLIPBOARD" -> {
                if (hasSelection) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = tint, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = tint, modifier = Modifier.size(20.dp))
                }
            }
            "HISTORY" -> Icon(Icons.Default.Assignment, contentDescription = "History", tint = tint, modifier = Modifier.size(20.dp))
            "HIDE" -> Icon(painterResource(id = R.drawable.ic_hide_keys_classic_down), contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            "EMOJI" -> Icon(painterResource(id = R.drawable.ic_emoji), contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            "CHANGE_LANGUAGE" -> Icon(painterResource(id = R.drawable.ic_earth), contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            "TOOLS" -> Icon(painterResource(id = R.drawable.ic_tools_menu), contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            "ONE_HAND" -> Icon(painterResource(id = R.drawable.ic_one_hand), contentDescription = null, tint = tint, modifier = Modifier.size(86.dp))

            "LEFT" -> Text("◀", color = tint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            "RIGHT" -> Text("▶", color = tint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            "UP" -> Text("▲", color = tint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            "DOWN" -> Text("▼", color = tint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            "UNDO" -> Icon(Icons.Default.Undo, contentDescription = "Undo", tint = tint, modifier = Modifier.size(20.dp))
            "REDO" -> Icon(Icons.Default.Redo, contentDescription = "Redo", tint = tint, modifier = Modifier.size(20.dp))
            "COPY" -> Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = tint, modifier = Modifier.size(20.dp))
            "PASTE" -> Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = tint, modifier = Modifier.size(20.dp))
            "CUT" -> Icon(Icons.Default.ContentCut, contentDescription = "Cut", tint = tint, modifier = Modifier.size(20.dp))
            "SELECT_ALL" -> Icon(painterResource(id = R.drawable.ic_select_all), contentDescription = "Select All", tint = tint, modifier = Modifier.size(20.dp))
            "COPY_ALL" -> Icon(painterResource(id = R.drawable.ic_select_all), contentDescription = "Copy All", tint = tint, modifier = Modifier.size(20.dp))
            else -> Text(action.take(3), color = tint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}