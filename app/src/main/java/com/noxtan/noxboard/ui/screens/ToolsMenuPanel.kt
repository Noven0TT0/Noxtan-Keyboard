package com.noxtan.noxboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.painterResource
import com.noxtan.noxboard.R

@Composable
fun ToolsMenuPanel(
    bottomPaddingDp: Float,
    hasSelection: Boolean, // <--- ဒါလေး အသစ်ထည့်ပါမယ်
    onClose: () -> Unit,
    onActionClick: (String) -> Unit,
    onCutClick: () -> Unit,
    onCopyClick: () -> Unit,
    onPasteClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onLeftArrowClick: () -> Unit,
    onRightArrowClick: () -> Unit,
    onClipboardHistoryClick: () -> Unit,
    onVaultClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFloatingClick: () -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val onSurface = MaterialTheme.colorScheme.onSurface

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = androidx.compose.runtime.remember { com.noxtan.noxboard.NoxBoardPrefs(context) }
    val shortcutList = androidx.compose.runtime.remember { prefs.customShortcutRow.split(",").filter { it.isNotEmpty() }.take(6) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(bottom = bottomPaddingDp.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        data class ToolDef(val id: String, val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val click: () -> Unit)

        // Select မှတ်ထား/မထား ပေါ်မူတည်ပြီး Copy/Paste ကို Dynamic ပြောင်းပေးမယ်
        val copyPasteName = if (hasSelection) "Copy" else "Paste"
        val copyPasteIcon = if (hasSelection) Icons.Default.ContentCopy else Icons.Default.ContentPaste
        val copyPasteClick = if (hasSelection) onCopyClick else onPasteClick

        val allTools = listOf(
            ToolDef("CLIPBOARD", copyPasteName, copyPasteIcon, copyPasteClick),
            ToolDef("CUT", "Cut", Icons.Default.ContentCut, onCutClick),
            ToolDef("SELECT_ALL", "Select All", Icons.Default.SelectAll, onSelectAllClick),
            ToolDef("LEFT", "Left", Icons.Default.ArrowBack, onLeftArrowClick),
            ToolDef("RIGHT", "Right", Icons.Default.ArrowForward, onRightArrowClick),
            ToolDef("HISTORY", "History", Icons.Default.Assignment, onClipboardHistoryClick),
            ToolDef("VAULT", "Nox Vault", Icons.Default.Shield, onVaultClick),
            ToolDef("FLOATING", "Floating", Icons.Default.OpenWith, onFloatingClick) // <--- အသစ်ထည့်ရန်
        )

        // Suggestion bar (Top row) မှာ မပါတဲ့ ကောင်တွေကိုပဲ စစ်ထုတ်ယူမယ်
        val toolsToShow = allTools.filter { it.id !in shortcutList }

        // ၄ ခု တစ်တန်းစီ ခွဲပြီး ပြမယ်
        toolsToShow.chunked(4).forEach { rowTools ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowTools.forEach { tool ->
                    CompactToolItem(
                        title = tool.name,
                        icon = tool.icon,
                        bg = surfaceColor,
                        tint = onSurface,
                        onClick = tool.click,
                        modifier = Modifier.weight(1f) // <--- Auto နေရာယူအောင် weight သုံးလိုက်ပါပြီ
                    )
                }
                // တစ်တန်းမှာ ၄ ခု မပြည့်ရင် နေရာလွတ်တွေ အစားထိုးပေးမယ်
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
    icon: ImageVector,
    bg: Color,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = tint,
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
    onClose: () -> Unit,
    onItemClick: (String) -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val onSurface = MaterialTheme.colorScheme.onSurface

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
            Text("Clipboard History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onSurface)
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp).clip(CircleShape).background(surfaceColor)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = onSurface, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No copied items yet.", color = onSurface.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyList) { text ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(surfaceColor)
                            .clickable { onItemClick(text) }
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
        }
    }
}

@Composable
fun ShortcutActionItem(action: String, tint: Color, hasSelection: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        when (action) {
            "VAULT" -> Icon(painterResource(id = R.drawable.ic_pass), contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            "CLIPBOARD" -> { // <--- ဒါကို Dynamic Copy/Paste အဖြစ် ပြောင်းလိုက်ပါပြီ
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
            "LEFT" -> Text("◀", color = tint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            "RIGHT" -> Text("▶", color = tint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            "UP" -> Text("▲", color = tint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            "DOWN" -> Text("▼", color = tint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            "UNDO" -> Icon(Icons.Default.Undo, contentDescription = "Undo", tint = tint, modifier = Modifier.size(20.dp))
            "REDO" -> Icon(Icons.Default.Redo, contentDescription = "Redo", tint = tint, modifier = Modifier.size(20.dp))
            "COPY" -> Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = tint, modifier = Modifier.size(20.dp))
            "PASTE" -> Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = tint, modifier = Modifier.size(20.dp))
            "CUT" -> Icon(Icons.Default.ContentCut, contentDescription = "Cut", tint = tint, modifier = Modifier.size(20.dp))
            "SELECT_ALL" -> Text("All", color = tint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            "COPY_ALL" -> Text("All", color = tint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            else -> Text(action.take(3), color = tint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}