package com.noxtan.noxboard.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.provider.VaultAidlClient
import com.noxtan.pass.aidl.VaultAccountInfo
import kotlinx.coroutines.launch

@Composable
fun CredentialVaultPanel(
    packageName: String,
    bottomPaddingDp: Float,
    accounts: List<VaultAccountInfo>,
    isLocked: Boolean,
    onUnlockClick: () -> Unit,
    onFillText: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val bgOled = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val accentPurple = MaterialTheme.colorScheme.primary
    val keyRadius = 13.dp


    var isVaultLockedState by remember(isLocked) { mutableStateOf(isLocked) }
    var currentAccountsList by remember(accounts) { mutableStateOf(accounts) }
    var activeAccount by remember(currentAccountsList) { mutableStateOf<VaultAccountInfo?>(currentAccountsList.firstOrNull()) }

    var isBrowseAllMode by remember { mutableStateOf(false) }
    var allAccountsList by remember { mutableStateOf<List<VaultAccountInfo>>(emptyList()) }

    val pinBuffer = remember { mutableStateListOf<Char>() }
    var isPinError by remember { mutableStateOf(false) }
    val shakeAnim = remember { Animatable(0f) }

    fun refreshAccounts() {
        scope.launch {
            when (val result = VaultAidlClient.fetchAccounts(context, packageName)) {
                is VaultAidlClient.VaultResult.Success -> {
                    currentAccountsList = result.accounts
                    activeAccount = result.accounts.firstOrNull()
                    isVaultLockedState = false
                }
                is VaultAidlClient.VaultResult.Locked -> {
                    isVaultLockedState = true
                }
                is VaultAidlClient.VaultResult.Error -> {
                    currentAccountsList = emptyList()
                    isVaultLockedState = false
                }
            }
        }
    }

    fun openBrowseAllAccounts() {
        if (isVaultLockedState) {
            return
        }
        isBrowseAllMode = true
        scope.launch {
            val results = VaultAidlClient.searchAllAccounts(context, "")
            allAccountsList = results.sortedBy { it.name.lowercase() }
        }
    }

    fun submitPin(pinString: String) {
        scope.launch {
            val success = VaultAidlClient.unlockWithPin(context, pinString)
            if (success) {
                pinBuffer.clear()
                isPinError = false
                isVaultLockedState = false
                refreshAccounts()
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isPinError = true
                pinBuffer.clear()
                shakeAnim.animateTo(15f, animationSpec = tween(50))
                shakeAnim.animateTo(-15f, animationSpec = tween(50))
                shakeAnim.animateTo(15f, animationSpec = tween(50))
                shakeAnim.animateTo(0f, animationSpec = tween(50))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgOled)
            .padding(horizontal = 6.dp)
            .padding(top = 4.dp, bottom = bottomPaddingDp.dp)
    ) {
        if (isVaultLockedState) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = if (isPinError) "Incorrect PIN. Try again." else "Enter 6-Digit PIN",
                        color = if (isPinError) MaterialTheme.colorScheme.error else accentPurple,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.graphicsLayer { translationX = shakeAnim.value }
                    ) {
                        for (i in 0 until 6) {
                            val isFilled = i < pinBuffer.size
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) accentPurple else surfaceColor)
                                    .border(1.dp, if (isFilled) accentPurple else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), CircleShape)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val keyRows = listOf(
                        listOf('1', '2', '3'),
                        listOf('4', '5', '6'),
                        listOf('7', '8', '9'),
                        listOf('C', '0', '⌫')
                    )

                    for (row in keyRows) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (key in row) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(surfaceColor)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            when (key) {
                                                'C' -> onClose()
                                                '⌫' -> {
                                                    if (pinBuffer.isNotEmpty()) pinBuffer.removeLast()
                                                    isPinError = false
                                                }
                                                else -> {
                                                    if (pinBuffer.size < 6) {
                                                        pinBuffer.add(key)
                                                        if (pinBuffer.size == 6) {
                                                            submitPin(pinBuffer.joinToString(""))
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (key) {
                                        'C' -> Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                        '⌫' -> Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                        else -> Text(text = key.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        else if (isBrowseAllMode) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(38.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isBrowseAllMode = false }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "All Vault Accounts (${allAccountsList.size})",
                            color = accentPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { isBrowseAllMode = false }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    items(allAccountsList) { acc ->
                        val isSelected = activeAccount?.id == acc.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) accentPurple.copy(alpha = 0.15f) else surfaceColor)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) accentPurple else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    activeAccount = acc
                                    isBrowseAllMode = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(if (isSelected) accentPurple else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = acc.name.take(1).uppercase(),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = acc.name,
                                    color = if (isSelected) accentPurple else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (acc.username.isNotEmpty()) {
                                    Text(
                                        text = acc.username,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = accentPurple, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(packageName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = { openBrowseAllAccounts() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "All Accounts", tint = accentPurple, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                VaultAidlClient.lockVault(context)
                                isVaultLockedState = true
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock Vault", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (currentAccountsList.isEmpty() && activeAccount == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("No matching account", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Browse vault to pick any credential", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { openBrowseAllAccounts() },
                            colors = ButtonDefaults.buttonColors(containerColor = surfaceColor),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentPurple.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = accentPurple, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Browse All", color = accentPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.CenterStart) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayList = if (currentAccountsList.isNotEmpty()) currentAccountsList else listOfNotNull(activeAccount)
                        items(displayList) { acc ->
                            val isSelected = activeAccount == acc
                            Row(
                                modifier = Modifier
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(if (isSelected) accentPurple.copy(alpha = 0.15f) else surfaceColor)
                                    .border(1.dp, if (isSelected) accentPurple else Color.Transparent, RoundedCornerShape(22.dp))
                                    .clickable { activeAccount = acc }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(24.dp).background(if (isSelected) accentPurple else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(acc.name.take(1).uppercase(), color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(verticalArrangement = Arrangement.Center) {
                                    Text(acc.name, color = if (isSelected) accentPurple else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(acc.username.ifEmpty { "No username" }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val currentAcc = activeAccount
                val hasAccount = currentAcc != null

                Row(
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NoxVaultKey(
                        icon = Icons.Default.Person,
                        label = "User",
                        enabled = hasAccount && currentAcc?.username?.isNotEmpty() == true,
                        onClick = { currentAcc?.username?.let { if (it.isNotEmpty()) onFillText(it) } },
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius
                    )
                    NoxVaultKey(
                        icon = Icons.Default.Key,
                        label = "Password",
                        enabled = hasAccount && currentAcc?.password?.isNotEmpty() == true,
                        onClick = { currentAcc?.password?.let { if (it.isNotEmpty()) onFillText(it) } },
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius,
                        borderColor = if (hasAccount) accentPurple else Color.Transparent,
                        iconTint = if (hasAccount) accentPurple else Color.Unspecified
                    )
                    NoxVaultKey(
                        icon = Icons.Default.Pin,
                        label = "TOTP",
                        enabled = hasAccount && currentAcc?.totp?.isNotEmpty() == true,
                        onClick = { currentAcc?.totp?.let { if (it.isNotEmpty()) onFillText(it) } },
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NoxVaultKey(
                        icon = Icons.Default.Link,
                        label = "Link",
                        enabled = hasAccount && currentAcc?.url?.isNotEmpty() == true,
                        onClick = { currentAcc?.url?.let { if (it.isNotEmpty()) onFillText(it) } },
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius
                    )
                    NoxVaultKey(
                        icon = Icons.AutoMirrored.Filled.Notes,
                        label = "Note",
                        enabled = hasAccount && currentAcc?.note?.isNotEmpty() == true,
                        onClick = { currentAcc?.note?.let { if (it.isNotEmpty()) onFillText(it) } },
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius
                    )
                    NoxVaultKey(
                        icon = Icons.Default.Keyboard,
                        label = "Keyboard",
                        enabled = true,
                        onClick = onClose,
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius,
                        iconTint = MaterialTheme.colorScheme.onSurface
                    )
                    NoxVaultKey(
                        icon = Icons.AutoMirrored.Filled.Backspace,
                        label = "Del",
                        enabled = true,
                        onClick = { onFillText("⌫") },
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    NoxVaultKey(
                        icon = Icons.AutoMirrored.Filled.KeyboardReturn,
                        label = "Enter",
                        enabled = true,
                        onClick = { onFillText("\n") },
                        surfaceColor = accentPurple,
                        cornerRadius = keyRadius,
                        iconTint = bgOled,
                        textColor = bgOled
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.NoxVaultKey(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    surfaceColor: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    borderColor: Color = Color.Transparent,
    iconTint: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified
) {
    val baseAlpha = if (enabled) 1f else 0.35f
    val resolvedIconTint = (if (iconTint == Color.Unspecified) MaterialTheme.colorScheme.onSurface else iconTint).copy(alpha = baseAlpha)
    val resolvedTextColor = (if (textColor == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else textColor).copy(alpha = baseAlpha)

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (enabled) surfaceColor else surfaceColor.copy(alpha = 0.4f))
            .border(1.dp, if (enabled) borderColor else Color.Transparent, RoundedCornerShape(cornerRadius))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = resolvedIconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = resolvedTextColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}