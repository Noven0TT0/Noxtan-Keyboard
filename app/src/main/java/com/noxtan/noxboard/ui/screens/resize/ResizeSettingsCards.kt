package com.noxtan.noxboard.ui.screens.resize

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.NoxBoardPrefs
import org.json.JSONObject
import androidx.compose.ui.res.stringResource
import com.noxtan.noxboard.R

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ResizeSettingsCards(
    prefs: NoxBoardPrefs,
    keyboardWidth: Float,
    onKeyboardWidthChange: (Float) -> Unit,
    keyboardAlignment: String,
    onKeyboardAlignmentChange: (String) -> Unit,
    keyboardHeight: Float,
    onKeyboardHeightChange: (Float) -> Unit,
    bottomPadding: Float,
    onBottomPaddingChange: (Float) -> Unit,
    keyCornerRadius: Float,
    onKeyCornerRadiusChange: (Float) -> Unit,
    keyPadding: Float,
    onKeyPaddingChange: (Float) -> Unit,
    keyFontSize: Float,
    onKeyFontSizeChange: (Float) -> Unit,
    globalRowGap: Float,
    onGlobalRowGapChange: (Float) -> Unit,
    individualRowGaps: String,
    onIndividualRowGapsChange: (String) -> Unit,
    onActiveSliderChange: (String?) -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onResetToDefaultClick: () -> Unit,
    activePresetId: String,
    presetsList: List<String>,
    onPresetSelect: (String) -> Unit,
    onPresetDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(activePresetId, presetsList) {
        val targetIndex = if (activePresetId == "default") {
            2
        } else {
            val idx = presetsList.indexOf(activePresetId)
            if (idx != -1) 3 + idx else -1
        }
        if (targetIndex != -1) {
            try {
                lazyListState.animateScrollToItem(targetIndex)
            } catch (e: Exception) {
                lazyListState.scrollToItem(targetIndex)
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        androidx.compose.foundation.lazy.LazyRow(
            state = lazyListState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Button(
                    onClick = onExportClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Export", color = Color.White, fontSize = 13.sp)
                }
            }
            item {
                Button(
                    onClick = onImportClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Import", color = Color.White, fontSize = 13.sp)
                }
            }
            item {
                val isDefaultSelected = activePresetId == "default"
                Button(
                    onClick = { onPresetSelect("default") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDefaultSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Restore,
                        contentDescription = null,
                        tint = if (isDefaultSelected) Color.Black else Color.Red,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Default", color = if (isDefaultSelected) Color.Black else Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            items(presetsList.size) { index ->
                val presetId = presetsList[index]
                val isSelected = activePresetId == presetId

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .combinedClickable(
                            onClick = { onPresetSelect(presetId) },
                            onLongClick = { onPresetDelete(presetId) }
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = presetId,
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Card(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.width_and_alignment), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${stringResource(R.string.width_label)}${keyboardWidth.toInt()}%", modifier = Modifier.width(95.dp), fontSize = 14.sp)
                    Slider(
                        value = keyboardWidth,
                        onValueChange = {
                            onKeyboardWidthChange(it)
                            onActiveSliderChange("WIDTH")
                            prefs.isSliding = true
                            prefs.slideWidth = it
                            prefs.slideHeight = keyboardHeight
                            prefs.slideBottomPadding = bottomPadding
                            prefs.slideAlignment = keyboardAlignment
                        },
                        onValueChangeFinished = {
                            onActiveSliderChange(null)
                            prefs.isSliding = false
                            prefs.keyboardWidth = keyboardWidth
                        },
                        valueRange = 70f..100f,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val alignments = listOf("LEFT", "CENTER", "RIGHT")
                    alignments.forEach { align ->
                        val isSelected = keyboardAlignment == align
                        Button(
                            onClick = {
                                onKeyboardAlignmentChange(align)
                                prefs.keyboardAlignment = align
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = when (align) {
                                    "LEFT" -> stringResource(R.string.align_left)
                                    "RIGHT" -> stringResource(R.string.align_right)
                                    else -> stringResource(R.string.align_center)
                                },
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.length_and_elevation), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${stringResource(R.string.height_label)}${keyboardHeight.toInt()}dp", modifier = Modifier.width(95.dp), fontSize = 14.sp)
                    Slider(
                        value = keyboardHeight,
                        onValueChange = {
                            onKeyboardHeightChange(it)
                            onActiveSliderChange("HEIGHT")
                            prefs.isSliding = true
                            prefs.slideHeight = it
                            prefs.slideWidth = keyboardWidth
                            prefs.slideBottomPadding = bottomPadding
                            prefs.slideAlignment = keyboardAlignment
                        },
                        onValueChangeFinished = {
                            onActiveSliderChange(null)
                            prefs.isSliding = false
                            prefs.keyboardHeight = keyboardHeight
                        },
                        valueRange = 180f..300f,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${stringResource(R.string.float_label)}${bottomPadding.toInt()}dp", modifier = Modifier.width(95.dp), fontSize = 14.sp)
                    Slider(
                        value = bottomPadding,
                        onValueChange = {
                            onBottomPaddingChange(it)
                            onActiveSliderChange("FLOAT")
                            prefs.isSliding = true
                            prefs.slideBottomPadding = it
                            prefs.slideWidth = keyboardWidth
                            prefs.slideHeight = keyboardHeight
                            prefs.slideAlignment = keyboardAlignment
                        },
                        onValueChangeFinished = {
                            onActiveSliderChange(null)
                            prefs.isSliding = false
                            prefs.bottomPadding = bottomPadding
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Card(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.key_styles_and_gaps), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${stringResource(R.string.corners_label)}${keyCornerRadius.toInt()}dp", modifier = Modifier.width(95.dp), fontSize = 14.sp)
                    Slider(
                        value = keyCornerRadius,
                        onValueChange = {
                            onKeyCornerRadiusChange(it)
                            prefs.keyCornerRadius = it
                        },
                        valueRange = 0f..20f,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${stringResource(R.string.key_gap_label)}${keyPadding.toInt()}dp", modifier = Modifier.width(95.dp), fontSize = 14.sp)
                    Slider(
                        value = keyPadding,
                        onValueChange = {
                            onKeyPaddingChange(it)
                            prefs.keyPadding = it
                        },
                        valueRange = 0f..10f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Card(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.font_size), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val newValue = (keyFontSize - 1f).coerceAtLeast(10f)
                        onKeyFontSizeChange(newValue)
                        prefs.keyFontSize = newValue
                    }) {
                        Text("A-", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = keyFontSize,
                        onValueChange = {
                            onKeyFontSizeChange(it)
                            prefs.keyFontSize = it
                        },
                        valueRange = 10f..30f,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        val newValue = (keyFontSize + 1f).coerceAtMost(34f)
                        onKeyFontSizeChange(newValue)
                        prefs.keyFontSize = newValue
                    }) {
                        Text("A+", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        var selectedRowIndex by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(-1) }
        val individualGapsMap = try {
            val json = org.json.JSONObject(individualRowGaps)
            val map = mutableMapOf<Int, Float>()
            json.keys().forEach { map[it.toInt()] = json.getDouble(it).toFloat() }
            map
        } catch(e: Exception) { mapOf<Int, Float>() }

        Card(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.row_gaps_spacing), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rowOptions = listOf(
                        stringResource(R.string.row_all) to -1,
                        stringResource(R.string.row_num) to 1,
                        stringResource(R.string.row_1) to 2,
                        stringResource(R.string.row_2) to 3,
                        stringResource(R.string.row_3) to 4,
                        stringResource(R.string.row_4) to 5
                    )

                    rowOptions.forEach { (label, index) ->
                        val isSelected = selectedRowIndex == index
                        Button(
                            onClick = { selectedRowIndex = index },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = label,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val currentVal = if (selectedRowIndex == -1) globalRowGap else (individualGapsMap[selectedRowIndex] ?: globalRowGap)
                    Text("${stringResource(R.string.gap_label)}${currentVal.toInt()}dp", modifier = Modifier.width(95.dp), fontSize = 14.sp)
                    Slider(
                        value = currentVal,
                        onValueChange = { newValue ->
                            if (selectedRowIndex == -1) {
                                onGlobalRowGapChange(newValue)
                                prefs.globalRowGap = newValue
                                onIndividualRowGapsChange("{}")
                                prefs.individualRowGaps = "{}"
                            } else {
                                val newMap = individualGapsMap.toMutableMap()
                                newMap[selectedRowIndex] = newValue

                                val json = org.json.JSONObject()
                                newMap.forEach { (k, v) -> json.put(k.toString(), v.toDouble()) }
                                val jsonStr = json.toString()
                                onIndividualRowGapsChange(jsonStr)
                                prefs.individualRowGaps = jsonStr
                            }
                        },
                        valueRange = if (selectedRowIndex == 1) -8f..12f else 0f..12f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}