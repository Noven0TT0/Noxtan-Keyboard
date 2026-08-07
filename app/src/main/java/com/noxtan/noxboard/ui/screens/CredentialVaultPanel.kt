package com.noxtan.noxboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.ui.theme.PrimaryPurple
import com.noxtan.noxboard.ui.theme.PureBlack
import com.noxtan.noxboard.ui.theme.SurfaceDark

data class VaultAccount(
    val name: String,
    val username: String,
    val password: String,
    val totp: String,
    val url: String,
    val note: String
)

@Composable
fun CredentialVaultPanel(
    packageName: String,
    bottomPaddingDp: Float,
    accounts: List<VaultAccount>,
    isLocked: Boolean,
    onUnlockClick: () -> Unit,
    onFillText: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeAccount by remember(accounts) { mutableStateOf<VaultAccount?>(accounts.firstOrNull()) }

    val bgOled = PureBlack
    val surfaceColor = SurfaceDark
    val accentPurple = PrimaryPurple
    val keyRadius = 13.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgOled)
            .padding(horizontal = 6.dp)
            .padding(
                top = 6.dp,
                bottom = bottomPaddingDp.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = accentPurple,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = packageName,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "Nox Vault",
                color = accentPurple,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLocked) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = accentPurple,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Vault is Locked", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onUnlockClick,
                        colors = ButtonDefaults.buttonColors(containerColor = accentPurple)
                    ) {
                        Text("Tap to Unlock", color = bgOled, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (accounts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No accounts found.", color = Color.Gray)
            }
        } else {

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(accounts) { acc ->
                    val isSelected = activeAccount == acc
                    Row(
                        modifier = Modifier
                            .height(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(if (isSelected) accentPurple.copy(alpha = 0.15f) else surfaceColor)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) accentPurple else Color.Transparent,
                                shape = RoundedCornerShape(23.dp)
                            )
                            .clickable { activeAccount = acc }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (isSelected) accentPurple else Color.DarkGray,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = acc.name.take(1).uppercase(),
                                color = if (isSelected) bgOled else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = acc.name,
                                color = if (isSelected) accentPurple else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = acc.username,
                                color = Color.Gray,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val currentAcc = activeAccount

                Row(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NoxVaultKey(
                        icon = Icons.Default.Person,
                        label = "User",
                        onClick = { currentAcc?.let { onFillText(it.username) } },
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius
                    )
                    NoxVaultKey(
                        icon = Icons.Default.Key,
                        label = "Password",
                        onClick = { currentAcc?.let { onFillText(it.password) } },
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius,
                        borderColor = accentPurple,
                        iconTint = accentPurple
                    )
                    NoxVaultKey(
                        icon = Icons.Default.Pin,
                        label = "TOTP",
                        onClick = { currentAcc?.let { onFillText(it.totp) } },
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NoxVaultKey(
                        icon = Icons.Default.Link,
                        label = "Link",
                        onClick = { currentAcc?.let { onFillText(it.url) } },
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius
                    )
                    NoxVaultKey(
                        icon = Icons.AutoMirrored.Filled.Notes,
                        label = "Note",
                        onClick = { currentAcc?.let { onFillText(it.note) } },
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius
                    )
                    NoxVaultKey(
                        icon = Icons.Default.Keyboard,
                        label = "Keyboard",
                        onClick = onClose,
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius,
                        iconTint = Color.White
                    )
                    NoxVaultKey(
                        icon = Icons.AutoMirrored.Filled.Backspace,
                        label = "Del",
                        onClick = { onFillText("⌫") },
                        surfaceColor = surfaceColor,
                        cornerRadius = keyRadius,
                        iconTint = Color.Gray
                    )
                    NoxVaultKey(
                        icon = Icons.AutoMirrored.Filled.KeyboardReturn,
                        label = "Enter",
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
    onClick: () -> Unit,
    surfaceColor: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    borderColor: Color = Color.Transparent,
    iconTint: Color = Color.White,
    textColor: Color = Color.Gray
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(cornerRadius))
            .background(surfaceColor)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}