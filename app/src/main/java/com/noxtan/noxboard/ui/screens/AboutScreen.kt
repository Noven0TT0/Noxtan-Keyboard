package com.noxtan.noxboard.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.ui.components.NoxTopBar
import com.noxtan.noxboard.ui.utils.topAndBottomNoise
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.noxtan.noxboard.R

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToOpenSource: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showContactDialog by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = true) {
        onBack()
    }

    val appVersion = try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: "1.0.0"
    } catch (e: Exception) {
        "1.0.0"
    }

    Scaffold(
        topBar = {
            NoxTopBar(
                title = stringResource(R.string.about_app_info),
                scrollState = scrollState,
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        var cachedTopPadding by remember { mutableStateOf(0.dp) }
        val currentTopPadding = innerPadding.calculateTopPadding()
        if (currentTopPadding > 0.dp) cachedTopPadding = currentTopPadding

        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 600.dp)
                .topAndBottomNoise()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 32.dp))

            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_nox_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Nox Board", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.about_version, appVersion), color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Privacy Guarantee",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.about_privacy_title),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.about_privacy_desc),
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AboutSection(title = "SUPPORT & FEEDBACK") {
                AboutListItem(
                    icon = Icons.Default.HeadsetMic,
                    title = "Contact Us",
                    subtitle = "Report issues or suggest features",
                    onClick = { showContactDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surface, modifier = Modifier.padding(start = 64.dp))
                AboutListItem(
                    icon = Icons.Default.BugReport,
                    title = stringResource(R.string.about_system_logs),
                    subtitle = stringResource(R.string.about_system_logs_desc),
                    onClick = onNavigateToLogs
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AboutSection(title = "MORE FROM US") {
                AboutListItem(
                    iconRes = R.drawable.ic_noxtan_player,
                    title = "Nox Video Player",
                    subtitle = "A powerful and smooth media player",
                    onClick = {
                        val url = "https://t.me/your_channel_link"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AboutSection(title = "ABOUT") {
                AboutListItem(
                    icon = Icons.Default.IntegrationInstructions,
                    title = stringResource(R.string.about_open_source),
                    subtitle = stringResource(R.string.about_open_source_desc),
                    onClick = onNavigateToOpenSource
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surface, modifier = Modifier.padding(start = 64.dp))
                AboutListItem(
                    icon = Icons.Default.Code,
                    title = "Developer",
                    subtitle = "NoxTan (View GitHub)",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/noxtan")))
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/noxtan")))
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_github),
                        contentDescription = "GitHub",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(78.dp))
        }

        if (showContactDialog) {
            ContactSupportDialog(
                onDismiss = { showContactDialog = false },
                context = context
            )
        }
    }
}

@Composable
fun AboutSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun AboutListItem(
    icon: ImageVector? = null,
    iconRes: Int? = null,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            } else if (iconRes != null) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ContactSupportDialog(onDismiss: () -> Unit, context: android.content.Context) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Contact Support", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
        },
        text = {
            Column {
                Text("ဘယ်ကနေတဆင့် ဆက်သွယ်ချင်ပါသလဲ?", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ContactIconBtn(
                        name = "Telegram",
                        iconRes = R.drawable.ic_telegram,
                        onClick = {
                            onDismiss()
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=Noven0TT0"))
                            try { context.startActivity(intent) } catch (e: Exception) { android.widget.Toast.makeText(context, "Telegram is not installed", android.widget.Toast.LENGTH_SHORT).show() }
                        }
                    )

                    ContactIconBtn(
                        name = "Messenger",
                        iconRes = R.drawable.ic_messenger,
                        onClick = {
                            onDismiss()
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://m.me/your_fb_page"))
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        }
                    )

                    ContactIconBtn(
                        name = "Signal",
                        iconRes = R.drawable.ic_signal,
                        onClick = {
                            onDismiss()
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://signal.me/#p/+1234567890"))
                            try { context.startActivity(intent) } catch (e: Exception) { android.widget.Toast.makeText(context, "Signal is not installed", android.widget.Toast.LENGTH_SHORT).show() }
                        }
                    )

                    ContactIconBtn(
                        name = "Email",
                        iconRes = R.drawable.ic_email,
                        onClick = {
                            onDismiss()
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:noxtan.dev@proton.me"))
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = Color.Gray) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ContactIconBtn(name: String, iconRes: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = iconRes),
            contentDescription = name,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}