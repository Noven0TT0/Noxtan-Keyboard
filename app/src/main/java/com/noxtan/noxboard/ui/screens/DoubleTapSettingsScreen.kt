package com.noxtan.noxboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.ui.utils.topAndBottomNoise
import org.json.JSONObject
import com.noxtan.noxboard.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubleTapSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }

    val masterDoubleTapMap = remember {
        mapOf(
            "ဆ" to "ဈ", "က" to "ဿ", "သ" to "ဥ", "ေ" to "ဗ", "ျ" to "ှ",
            "ိ" to "ီ", "်" to "ွ", "့" to "ံ", "ြ" to "ဲ", "ု" to "ဒ",
            "ူ" to "ဓ", "ဖ" to "ဇ", "ထ" to "ဌ", "ခ" to "ဃ", "လ" to "ဠ",
            "ဘ" to "၊", "ည" to "ဉ", "ာ" to "ါ", "ယ" to "။", "ဏ" to "ဩ",
            "ရ" to "ဋ္ဌ", "ဂ" to "ဏ္ဍ", "ဝ" to "ဋ", "ဟ" to "ဍ"
        )
    }

    var statesJson by remember { mutableStateOf(JSONObject(prefs.doubleTapStates)) }

    val scrollState = rememberScrollState()

    androidx.activity.compose.BackHandler(enabled = true) { onBack() }

    Scaffold(
        topBar = {
            com.noxtan.noxboard.ui.components.NoxTopBar(
                title = androidx.compose.ui.res.stringResource(R.string.double_tap_shortcuts),
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
                .topAndBottomNoise()
                .verticalScroll(scrollState)
                .padding(
                    start = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    end = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 32.dp
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.double_tap_shortcuts_instruction),
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            masterDoubleTapMap.forEach { (key, value) ->
                val isEnabled = if (statesJson.has(key)) statesJson.getBoolean(key) else true

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(Color(0xFF222222), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(key, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(value, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            val newJson = JSONObject(statesJson.toString())
                            newJson.put(key, checked)
                            statesJson = newJson
                            prefs.doubleTapStates = newJson.toString()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}