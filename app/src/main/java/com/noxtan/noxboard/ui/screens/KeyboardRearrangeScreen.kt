package com.noxtan.noxboard.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.Key
import com.noxtan.noxboard.KeyboardLayout
import com.noxtan.noxboard.NoxBoardPrefs
import com.noxtan.noxboard.ui.components.NoxTopBar
import com.noxtan.noxboard.ui.utils.topAndBottomNoise
import androidx.compose.ui.res.painterResource
import com.noxtan.noxboard.R
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.json.JSONObject

data class ActionIcon(val title: String, val code: Int, val prefKey: String, val icons: List<String>)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun KeyboardRearrangeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.tab_myanmar), stringResource(R.string.tab_english), stringResource(R.string.tab_symbols))

    var currentLayout by remember(selectedTabIndex) {
        mutableStateOf(
            when (selectedTabIndex) {
                0 -> KeyboardLayout.getMyanmarLayout(context, false).drop(1).map { it.toMutableList() }.toMutableList()
                1 -> KeyboardLayout.getEnglishLayout(context, false).drop(1).map { it.toMutableList() }.toMutableList()
                else -> KeyboardLayout.getSymbolLayout(context, 1).map { it.toMutableList() }.toMutableList()
            }
        )
    }

    var selectedKeyPos by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var editNormalText by remember { mutableStateOf("") }
    var activePresetId by remember(selectedTabIndex) {
        mutableStateOf(
            when (selectedTabIndex) {
                0 -> prefs.activeLayoutMyanmar
                1 -> prefs.activeLayoutEnglish
                else -> prefs.activeLayoutSymbols
            }
        )
    }

    var layoutPresetsList by remember(selectedTabIndex) {
        mutableStateOf(
            run {
                val jsonStr = when (selectedTabIndex) {
                    0 -> prefs.savedLayoutsMyanmar
                    1 -> prefs.savedLayoutsEnglish
                    else -> prefs.savedLayoutsSymbols
                }
                val arr = org.json.JSONArray(jsonStr)
                val list = mutableListOf<org.json.JSONObject>()
                for (i in 0 until arr.length()) {
                    list.add(arr.getJSONObject(i))
                }
                list
            }
        )
    }

    var baselineLayoutJson by remember(activePresetId, layoutPresetsList, selectedTabIndex) {
        mutableStateOf(
            if (activePresetId == "default") {
                val defaultLayout = when (selectedTabIndex) {
                    0 -> com.noxtan.noxboard.layouts.MyanmarLayout.getLayout(false).drop(1)
                    1 -> com.noxtan.noxboard.layouts.EnglishLayout.getLayout(false).drop(1)
                    else -> com.noxtan.noxboard.layouts.SymbolLayout.getLayout(1)
                }
                KeyboardLayout.layoutToJson(defaultLayout)
            } else {
                val preset = layoutPresetsList.find { it.optString("id") == activePresetId }
                preset?.optString("orig_layout", "") ?: ""
            }
        )
    }

    val initialActivePresetId by remember(selectedTabIndex) {
        mutableStateOf(
            when (selectedTabIndex) {
                0 -> prefs.activeLayoutMyanmar
                1 -> prefs.activeLayoutEnglish
                else -> prefs.activeLayoutSymbols
            }
        )
    }
    val initialSavedLayoutsJson by remember(selectedTabIndex) {
        mutableStateOf(
            when (selectedTabIndex) {
                0 -> prefs.savedLayoutsMyanmar
                1 -> prefs.savedLayoutsEnglish
                else -> prefs.savedLayoutsSymbols
            }
        )
    }

    var presetToDelete by remember { mutableStateOf<String?>(null) }
    var valueToEditType by remember { mutableStateOf<String?>(null) }
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(activePresetId, layoutPresetsList) {
        val targetIndex = if (activePresetId == "default") 2 else {
            val idx = layoutPresetsList.map { it.optString("id") }.indexOf(activePresetId)
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

    val selectPreset = { selectedId: String ->
        if (activePresetId != "default") {
            layoutPresetsList = layoutPresetsList.map { item ->
                if (item.optString("id") == activePresetId) {
                    item.put("layout", KeyboardLayout.layoutToJson(currentLayout))
                }
                item
            }.toMutableList()
        }

        activePresetId = selectedId
        when (selectedTabIndex) {
            0 -> prefs.activeLayoutMyanmar = selectedId
            1 -> prefs.activeLayoutEnglish = selectedId
            else -> prefs.activeLayoutSymbols = selectedId
        }

        val targetJson = if (selectedId == "default") {
            val defaultLayout = when (selectedTabIndex) {
                0 -> KeyboardLayout.getMyanmarLayout(context, false).drop(1)
                1 -> KeyboardLayout.getEnglishLayout(context, false).drop(1)
                else -> KeyboardLayout.getSymbolLayout(context, 1)
            }
            KeyboardLayout.layoutToJson(defaultLayout)
        } else {
            val preset = layoutPresetsList.find { it.optString("id") == selectedId }
            preset?.optString("layout", "") ?: ""
        }

        if (targetJson.isNotEmpty()) {
            val parsed = org.json.JSONArray(targetJson).let { rootArray ->
                val layout = mutableListOf<MutableList<Key>>()
                for (i in 0 until rootArray.length()) {
                    val rowArray = rootArray.getJSONArray(i)
                    val row = mutableListOf<Key>()
                    for (j in 0 until rowArray.length()) {
                        val keyObj = rowArray.getJSONObject(j)
                        row.add(Key(
                            normalText = keyObj.optString("normal", ""),
                            shiftText = keyObj.optString("shift", ""),
                            code = keyObj.optInt("code", 0),
                            widthWeight = keyObj.optDouble("weight", 1.0).toFloat(),
                            scaleX = keyObj.optDouble("scaleX", 1.0).toFloat(),
                            scaleY = keyObj.optDouble("scaleY", 1.0).toFloat(),
                            alignX = keyObj.optDouble("alignX", 0.5).toFloat(),
                            alignY = keyObj.optDouble("alignY", 0.5).toFloat()
                        ))
                    }
                    layout.add(row)
                }
                if (layout.isNotEmpty() && layout[0].isNotEmpty() && layout[0][0].code in -102..-100) {
                    layout.drop(1).toMutableList()
                } else {
                    layout
                }
            }
            currentLayout = parsed
        }
    }
    var editShiftText by remember { mutableStateOf("") }
    var editKeyWeight by remember { mutableFloatStateOf(1.0f) }
    var editScaleX by remember { mutableFloatStateOf(1.0f) }
    var editScaleY by remember { mutableFloatStateOf(1.0f) }
    var editAlignX by remember { mutableFloatStateOf(0.5f) }
    var editAlignY by remember { mutableFloatStateOf(0.5f) }
    val initialLayoutJson = remember(selectedTabIndex) { KeyboardLayout.layoutToJson(currentLayout) }
    val hasChanges = KeyboardLayout.layoutToJson(currentLayout) != initialLayoutJson ||
            activePresetId != initialActivePresetId ||
            (when (selectedTabIndex) {
                0 -> prefs.savedLayoutsMyanmar != initialSavedLayoutsJson
                1 -> prefs.savedLayoutsEnglish != initialSavedLayoutsJson
                else -> prefs.savedLayoutsSymbols != initialSavedLayoutsJson
            })

    val saveCurrentLayout = {
        val layoutToSave = if (selectedTabIndex == 0 || selectedTabIndex == 1) {
            listOf(mutableListOf(com.noxtan.noxboard.Key("", "", -100, 10.0f))) + currentLayout
        } else {
            currentLayout
        }
        val jsonStr = KeyboardLayout.layoutToJson(layoutToSave)

        if (activePresetId != "default") {
            val updatedList = layoutPresetsList.map { item ->
                if (item.optString("id") == activePresetId) {
                    item.put("layout", jsonStr)
                }
                item
            }
            val arr = org.json.JSONArray()
            updatedList.forEach { arr.put(it) }
            when (selectedTabIndex) {
                0 -> prefs.savedLayoutsMyanmar = arr.toString()
                1 -> prefs.savedLayoutsEnglish = arr.toString()
                else -> prefs.savedLayoutsSymbols = arr.toString()
            }
        } else {
            when (selectedTabIndex) {
                0 -> prefs.customLayoutMyanmar = jsonStr
                1 -> prefs.customLayoutEnglish = jsonStr
                else -> prefs.customLayoutSymbols = jsonStr
            }
        }

        when (selectedTabIndex) {
            0 -> prefs.activeLayoutMyanmar = activePresetId
            1 -> prefs.activeLayoutEnglish = activePresetId
            else -> prefs.activeLayoutSymbols = activePresetId
        }
        android.widget.Toast.makeText(context, context.getString(R.string.layout_saved_toast), android.widget.Toast.LENGTH_SHORT).show()
    }

    var pendingTabSwitchIndex by remember { mutableStateOf<Int?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    var iconUpdateTrigger by remember { mutableIntStateOf(0) }
    val importLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonStr = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: ""

                org.json.JSONArray(jsonStr)

                val newId = (layoutPresetsList.size + 1).toString()
                val newPreset = org.json.JSONObject().apply {
                    put("id", newId)
                    put("name", "Layout $newId")
                    put("layout", jsonStr)
                    put("orig_layout", jsonStr)
                }

                val newList = layoutPresetsList.toMutableList()
                newList.add(newPreset)
                layoutPresetsList = newList

                val arr = org.json.JSONArray()
                newList.forEach { arr.put(it) }

                when (selectedTabIndex) {
                    0 -> { prefs.savedLayoutsMyanmar = arr.toString(); prefs.activeLayoutMyanmar = newId }
                    1 -> { prefs.savedLayoutsEnglish = arr.toString(); prefs.activeLayoutEnglish = newId }
                    else -> { prefs.savedLayoutsSymbols = arr.toString(); prefs.activeLayoutSymbols = newId }
                }

                activePresetId = newId

                val parsed = org.json.JSONArray(jsonStr).let { rootArray ->
                    val layout = mutableListOf<MutableList<Key>>()
                    for (i in 0 until rootArray.length()) {
                        val rowArray = rootArray.getJSONArray(i)
                        val row = mutableListOf<Key>()
                        for (j in 0 until rowArray.length()) {
                            val keyObj = rowArray.getJSONObject(j)
                            row.add(Key(
                                normalText = keyObj.optString("normal", ""),
                                shiftText = keyObj.optString("shift", ""),
                                code = keyObj.optInt("code", 0),
                                widthWeight = keyObj.optDouble("weight", 1.0).toFloat(),
                                scaleX = keyObj.optDouble("scaleX", 1.0).toFloat(),
                                scaleY = keyObj.optDouble("scaleY", 1.0).toFloat(),
                                alignX = keyObj.optDouble("alignX", 0.5).toFloat(),
                                alignY = keyObj.optDouble("alignY", 0.5).toFloat()
                            ))
                        }
                        layout.add(row)
                    }
                    if (layout.isNotEmpty() && layout[0].isNotEmpty() && layout[0][0].code in -102..-100) {
                        layout.drop(1).toMutableList()
                    } else {
                        layout
                    }
                }
                currentLayout = parsed

                android.widget.Toast.makeText(context, "Layout imported to Slot $newId!", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Import failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val exportLayout = {
        try {
            val layoutToExport = if (selectedTabIndex == 0 || selectedTabIndex == 1) {
                listOf(mutableListOf(com.noxtan.noxboard.Key("", "", -100, 10.0f))) + currentLayout
            } else {
                currentLayout
            }
            val json = KeyboardLayout.layoutToJson(layoutToExport)

            val file = java.io.File(context.cacheDir, "nox_layout_config_${System.currentTimeMillis()}.noxlayout")
            file.writeText(json, Charsets.UTF_8)

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Keyboard Layout via"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Export failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    androidx.activity.compose.BackHandler(enabled = true) {
        if (hasChanges) showExitDialog = true else onBack()
    }

    var isSwapMode by remember { mutableStateOf(false) }
    var showCharPickerFor by remember { mutableStateOf<String?>(null) }

    val myanmarChars = remember { listOf("က","ခ","ဂ","ဃ","င","စ","ဆ","ဇ","ဈ","ည","ဋ","ဌ","ဍ","ဎ","ဏ","တ","ထ","ဒ","ဓ","န","ပ","ဖ","ဗ","ဘ","မ","ယ","ရ","လ","ဝ","သ","ဟ","ဠ","အ","ေ","ျ","ိ","်","့","ြ","ု","ူ","း","ာ","ါ","္") }
    val englishChars = remember { listOf("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z") }
    val numberChars = remember { listOf("၁","၂","၃","၄","၅","၆","၇","၈","၉","၀","1","2","3","4","5","6","7","8","9","0","!","@","#","$","%","^","&","*","(",")") }
    var swapKey1 by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var swapKey2 by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val isKeySelected = selectedKeyPos != null

    LaunchedEffect(selectedKeyPos) {
        selectedKeyPos?.let { (r, k) ->
            val key = currentLayout[r][k]
            editNormalText = key.normalText
            editShiftText = key.shiftText
            editKeyWeight = key.widthWeight
            editScaleX = key.scaleX
            editScaleY = key.scaleY
            editAlignX = key.alignX
            editAlignY = key.alignY
        } ?: run {
            editNormalText = ""
            editShiftText = ""
            editKeyWeight = 1.0f
            editScaleX = 1.0f; editScaleY = 1.0f
            editAlignX = 0.5f; editAlignY = 0.5f
        }
    }

    val updateSelectedKey: (Key) -> Unit = { newKey ->
        selectedKeyPos?.let { (r, k) ->
            val newLayout = currentLayout.map { it.toMutableList() }.toMutableList()
            newLayout[r][k] = newKey
            currentLayout = newLayout
        }
    }

    Scaffold(
        topBar = {
            NoxTopBar(
                title = stringResource(R.string.rearrange_keys_title),
                scrollState = scrollState,
                onBackClick = { if (hasChanges) showExitDialog = true else onBack() },
                actions = { iconBgColor ->
                    TextButton(onClick = {
                        if (baselineLayoutJson.isNotEmpty()) {
                            val parsed = org.json.JSONArray(baselineLayoutJson).let { rootArray ->
                                val layout = mutableListOf<MutableList<Key>>()
                                for (i in 0 until rootArray.length()) {
                                    val rowArray = rootArray.getJSONArray(i)
                                    val row = mutableListOf<Key>()
                                    for (j in 0 until rowArray.length()) {
                                        val keyObj = rowArray.getJSONObject(j)
                                        row.add(Key(
                                            normalText = keyObj.optString("normal", ""),
                                            shiftText = keyObj.optString("shift", ""),
                                            code = keyObj.optInt("code", 0),
                                            widthWeight = keyObj.optDouble("weight", 1.0).toFloat(),
                                            scaleX = keyObj.optDouble("scaleX", 1.0).toFloat(),
                                            scaleY = keyObj.optDouble("scaleY", 1.0).toFloat(),
                                            alignX = keyObj.optDouble("alignX", 0.5).toFloat(),
                                            alignY = keyObj.optDouble("alignY", 0.5).toFloat()
                                        ))
                                    }
                                    layout.add(row)
                                }
                                if (layout.isNotEmpty() && layout[0].isNotEmpty() && layout[0][0].code in -102..-100) {
                                    layout.drop(1).toMutableList()
                                } else {
                                    layout
                                }
                            }
                            currentLayout = parsed
                            selectedKeyPos = null
                        }
                    }) {
                        Text(stringResource(R.string.reset), color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            saveCurrentLayout()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(stringResource(R.string.save), fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 600.dp)
                .padding(top = innerPadding.calculateTopPadding())
                .imePadding()
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    state = lazyListState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        Button(
                            onClick = exportLayout,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Export", color = Color.White, fontSize = 13.sp)
                        }
                    }
                    item {
                        Button(
                            onClick = { importLauncher.launch(arrayOf("*/*")) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Import", color = Color.White, fontSize = 13.sp)
                        }
                    }
                    item {
                        val isDefaultSelected = activePresetId == "default"
                        Button(
                            onClick = { selectPreset("default") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDefaultSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = if (isDefaultSelected) Color.Black else Color.Red, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Default", color = if (isDefaultSelected) Color.Black else Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    items(layoutPresetsList.size) { index ->
                        val presetId = layoutPresetsList[index].optString("id")
                        val isSelected = activePresetId == presetId
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .combinedClickable(
                                    onClick = { selectPreset(presetId) },
                                    onLongClick = { presetToDelete = presetId }
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

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                if (selectedTabIndex != index) {
                                    if (hasChanges) {
                                        pendingTabSwitchIndex = index
                                    } else {
                                        selectedTabIndex = index
                                        selectedKeyPos = null
                                    }
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().animateContentSize()
                    ) {
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { ctx ->
                                com.noxtan.noxboard.NoxKeyboardView(ctx).apply {
                                    isEditMode = true

                                    onEditKeyClick = { rIndex, kIndex ->
                                        val currentPos = rIndex to kIndex
                                        if (isSwapMode) {
                                            if (swapKey1 == currentPos) swapKey1 = null
                                            else if (swapKey2 == currentPos) swapKey2 = null
                                            else if (swapKey1 == null) swapKey1 = currentPos
                                            else if (swapKey2 == null) swapKey2 = currentPos
                                        } else {
                                            selectedKeyPos = currentPos
                                        }
                                    }
                                }
                            },
                            update = { view ->
                                val trigger = iconUpdateTrigger
                                view.initIcons()

                                view.currentMode = when (selectedTabIndex) {
                                    0 -> com.noxtan.noxboard.KeyboardMode.MYANMAR
                                    1 -> com.noxtan.noxboard.KeyboardMode.ENGLISH
                                    else -> com.noxtan.noxboard.KeyboardMode.SYMBOLS_1
                                }
                                view.applySettings(prefs)

                                try {
                                    val keysField = view::class.java.getDeclaredField("keys")
                                    keysField.isAccessible = true
                                    keysField.set(view, currentLayout)

                                    if (selectedTabIndex == 0 || selectedTabIndex == 1) {
                                        val isTopRowRemovedField = view::class.java.getDeclaredField("isTopRowRemoved")
                                        isTopRowRemovedField.isAccessible = true
                                        isTopRowRemovedField.set(view, true)
                                    }

                                    val calcMethod = view::class.java.getDeclaredMethod("calculateKeyLayout", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                                    calcMethod.isAccessible = true
                                    calcMethod.invoke(view, view.width, view.height)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                view.editSelectedKeyPos = selectedKeyPos
                                view.editSwapKey1Pos = swapKey1
                                view.editSwapKey2Pos = swapKey2

                                view.requestLayout()
                                view.invalidate()
                                Log.d("NoxDebug", "Screen Update -> SelectedTab: $selectedTabIndex, CurrentLayoutSize: ${currentLayout.size}")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isSwapMode) {
                        Button(
                            onClick = {
                                isSwapMode = true
                                selectedKeyPos = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.enter_swap_mode), color = Color.White)
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.select_2_keys_to_swap),
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Row {
                            TextButton(onClick = {
                                isSwapMode = false; swapKey1 = null; swapKey2 = null
                            }) {
                                Text(stringResource(R.string.cancel), color = Color.Gray)
                            }
                            Button(
                                onClick = {
                                    if (swapKey1 != null && swapKey2 != null) {
                                        val newLayout =
                                            currentLayout.map { it.toMutableList() }.toMutableList()
                                        val temp = newLayout[swapKey1!!.first][swapKey1!!.second]
                                        newLayout[swapKey1!!.first][swapKey1!!.second] =
                                            newLayout[swapKey2!!.first][swapKey2!!.second]
                                        newLayout[swapKey2!!.first][swapKey2!!.second] = temp
                                        currentLayout = newLayout

                                        isSwapMode = false
                                        swapKey1 = null
                                        swapKey2 = null
                                    }
                                },
                                enabled = swapKey1 != null && swapKey2 != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50),
                                    disabledContainerColor = Color(0xFF1E3B20)
                                )
                            ) {
                                Text(
                                    stringResource(R.string.confirm_swap),
                                    color = if (swapKey1 != null && swapKey2 != null) Color.Black else Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isSwapMode) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isKeySelected) stringResource(R.string.edit_key_title, editNormalText) else stringResource(R.string.select_a_key_to_edit),
                                    color = if (isKeySelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Icon(Icons.Default.DragHandle, contentDescription = null, tint = if (isKeySelected) Color.White else Color.DarkGray)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = editNormalText,
                                    onValueChange = {
                                        editNormalText = it
                                        selectedKeyPos?.let { pos ->
                                            updateSelectedKey(currentLayout[pos.first][pos.second].copy(normalText = it, code = 0))
                                        }
                                    },
                                    label = { Text(stringResource(R.string.normal_text), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                    trailingIcon = {
                                        IconButton(onClick = { showCharPickerFor = "NORMAL" }, enabled = isKeySelected) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = if (isKeySelected) MaterialTheme.colorScheme.primary else Color.DarkGray)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = isKeySelected,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.DarkGray,
                                        unfocusedLabelColor = Color.Gray,
                                        disabledBorderColor = Color.DarkGray,
                                        disabledLabelColor = Color.Gray,
                                        disabledTextColor = Color.White
                                    ),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = editShiftText,
                                    onValueChange = {
                                        editShiftText = it
                                        selectedKeyPos?.let { pos ->
                                            updateSelectedKey(currentLayout[pos.first][pos.second].copy(shiftText = it, code = 0))
                                        }
                                    },
                                    label = { Text(stringResource(R.string.shift_text), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                    trailingIcon = {
                                        IconButton(onClick = { showCharPickerFor = "SHIFT" }, enabled = isKeySelected) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = if (isKeySelected) MaterialTheme.colorScheme.primary else Color.DarkGray)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = isKeySelected,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.DarkGray,
                                        unfocusedLabelColor = Color.Gray,
                                        disabledBorderColor = Color.DarkGray,
                                        disabledLabelColor = Color.Gray,
                                        disabledTextColor = Color.White
                                    ),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = if (isKeySelected) Modifier.clickable { valueToEditType = "WEIGHT" } else Modifier, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.key_space_weight, editKeyWeight), color = if (isKeySelected) Color.White else Color.DarkGray, fontSize = 14.sp)
                                if (isKeySelected) { Spacer(Modifier.width(6.dp)); Icon(painterResource(R.drawable.ic_edit), null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                            }
                            Slider(
                                value = editKeyWeight,
                                onValueChange = {
                                    editKeyWeight = it
                                    selectedKeyPos?.let { pos ->
                                        updateSelectedKey(currentLayout[pos.first][pos.second].copy(widthWeight = it))
                                    }
                                },
                                valueRange = 0.5f..5.0f,
                                enabled = isKeySelected,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    disabledThumbColor = Color.DarkGray,
                                    disabledActiveTrackColor = Color.DarkGray
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = if (isKeySelected) Modifier.clickable { valueToEditType = "SCALEX" } else Modifier, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.inner_width_scale_x, (editScaleX * 100).toInt()), color = if (isKeySelected) Color.White else Color.DarkGray, fontSize = 14.sp)
                                if (isKeySelected) { Spacer(Modifier.width(6.dp)); Icon(painterResource(R.drawable.ic_edit), null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                            }
                            Slider(
                                value = editScaleX,
                                onValueChange = {
                                    editScaleX = it
                                    selectedKeyPos?.let { pos ->
                                        updateSelectedKey(currentLayout[pos.first][pos.second].copy(scaleX = it))
                                    }
                                },
                                valueRange = 0.5f..1.0f,
                                enabled = isKeySelected,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF4CAF50),
                                    activeTrackColor = Color(0xFF4CAF50),
                                    disabledThumbColor = Color.DarkGray,
                                    disabledActiveTrackColor = Color.DarkGray
                                )
                            )

                            Row(modifier = if (isKeySelected) Modifier.clickable { valueToEditType = "SCALEY" } else Modifier, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.inner_height_scale_y, (editScaleY * 100).toInt()), color = if (isKeySelected) Color.White else Color.DarkGray, fontSize = 14.sp)
                                if (isKeySelected) { Spacer(Modifier.width(6.dp)); Icon(painterResource(R.drawable.ic_edit), null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                            }
                            Slider(
                                value = editScaleY,
                                onValueChange = {
                                    editScaleY = it
                                    selectedKeyPos?.let { pos ->
                                        updateSelectedKey(currentLayout[pos.first][pos.second].copy(scaleY = it))
                                    }
                                },
                                valueRange = 0.5f..1.0f,
                                enabled = isKeySelected,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF4CAF50),
                                    activeTrackColor = Color(0xFF4CAF50),
                                    disabledThumbColor = Color.DarkGray,
                                    disabledActiveTrackColor = Color.DarkGray
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val canAlignX = editScaleX < 1.0f
                            Row(modifier = if (isKeySelected && canAlignX) Modifier.clickable { valueToEditType = "ALIGNX" } else Modifier, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.align_x, (editAlignX * 100).toInt()), color = if (isKeySelected && canAlignX) Color.White else Color.DarkGray, fontSize = 14.sp)
                                if (isKeySelected && canAlignX) { Spacer(Modifier.width(6.dp)); Icon(painterResource(R.drawable.ic_edit), null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                            }
                            Slider(
                                value = editAlignX,
                                onValueChange = {
                                    editAlignX = it
                                    selectedKeyPos?.let { pos ->
                                        updateSelectedKey(currentLayout[pos.first][pos.second].copy(alignX = it))
                                    }
                                },
                                valueRange = 0f..1f,
                                enabled = isKeySelected && canAlignX,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF03A9F4),
                                    activeTrackColor = Color(0xFF03A9F4),
                                    disabledThumbColor = Color.DarkGray,
                                    disabledActiveTrackColor = Color.DarkGray
                                )
                            )

                            val canAlignY = editScaleY < 1.0f
                            Row(modifier = if (isKeySelected && canAlignY) Modifier.clickable { valueToEditType = "ALIGNY" } else Modifier, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.align_y, (editAlignY * 100).toInt()), color = if (isKeySelected && canAlignY) Color.White else Color.DarkGray, fontSize = 14.sp)
                                if (isKeySelected && canAlignY) { Spacer(Modifier.width(6.dp)); Icon(painterResource(R.drawable.ic_edit), null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                            }
                            Slider(
                                value = editAlignY,
                                onValueChange = {
                                    editAlignY = it
                                    selectedKeyPos?.let { pos ->
                                        updateSelectedKey(currentLayout[pos.first][pos.second].copy(alignY = it))
                                    }
                                },
                                valueRange = 0f..1f,
                                enabled = isKeySelected && canAlignY,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF03A9F4),
                                    activeTrackColor = Color(0xFF03A9F4),
                                    disabledThumbColor = Color.DarkGray,
                                    disabledActiveTrackColor = Color.DarkGray
                                )
                            )

                            Text(stringResource(R.string.move_key_position), color = if (isKeySelected) Color.White else Color.DarkGray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                IconButton(
                                    onClick = {
                                        selectedKeyPos?.let { pos ->
                                            val r = pos.first
                                            val k = pos.second
                                            if (k > 0) {
                                                val newLayout = currentLayout.map { it.toMutableList() }.toMutableList()
                                                val key = newLayout[r].removeAt(k)
                                                newLayout[r].add(k - 1, key)
                                                currentLayout = newLayout
                                                selectedKeyPos = r to (k - 1)
                                            }
                                        }
                                    },
                                    enabled = isKeySelected && selectedKeyPos?.second?.let { it > 0 } == true
                                ) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = if (isKeySelected) MaterialTheme.colorScheme.primary else Color.DarkGray) }

                                IconButton(
                                    onClick = {
                                        selectedKeyPos?.let { pos ->
                                            val r = pos.first
                                            val k = pos.second
                                            if (r > 0) {
                                                val newLayout = currentLayout.map { it.toMutableList() }.toMutableList()
                                                val key = newLayout[r].removeAt(k)
                                                val newK = minOf(k, newLayout[r - 1].size)
                                                newLayout[r - 1].add(newK, key)
                                                currentLayout = newLayout
                                                selectedKeyPos = (r - 1) to newK
                                            }
                                        }
                                    },
                                    enabled = isKeySelected && selectedKeyPos?.first?.let { it > 0 } == true
                                ) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = if (isKeySelected) MaterialTheme.colorScheme.primary else Color.DarkGray) }

                                IconButton(
                                    onClick = {
                                        selectedKeyPos?.let { pos ->
                                            val r = pos.first
                                            val k = pos.second
                                            if (r < currentLayout.size - 1) {
                                                val newLayout = currentLayout.map { it.toMutableList() }.toMutableList()
                                                val key = newLayout[r].removeAt(k)
                                                val newK = minOf(k, newLayout[r + 1].size)
                                                newLayout[r + 1].add(newK, key)
                                                currentLayout = newLayout
                                                selectedKeyPos = (r + 1) to newK
                                            }
                                        }
                                    },
                                    enabled = isKeySelected && selectedKeyPos?.first?.let { it < currentLayout.size - 1 } == true
                                ) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = if (isKeySelected) MaterialTheme.colorScheme.primary else Color.DarkGray) }

                                IconButton(
                                    onClick = {
                                        selectedKeyPos?.let { pos ->
                                            val r = pos.first
                                            val k = pos.second
                                            if (k < currentLayout[r].size - 1) {
                                                val newLayout = currentLayout.map { it.toMutableList() }.toMutableList()
                                                val key = newLayout[r].removeAt(k)
                                                newLayout[r].add(k + 1, key)
                                                currentLayout = newLayout
                                                selectedKeyPos = r to (k + 1)
                                            }
                                        }
                                    },
                                    enabled = isKeySelected && selectedKeyPos?.let { it.second < currentLayout[it.first].size - 1 } == true
                                ) { Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = if (isKeySelected) MaterialTheme.colorScheme.primary else Color.DarkGray) }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "စာကြောင်းတစ်ခုလုံး အထက်/အောက် ရွှေ့ရန် (Move Row)",
                                color = if (isKeySelected) Color.White else Color.DarkGray,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                IconButton(
                                    onClick = {
                                        selectedKeyPos?.let { pos ->
                                            val r = pos.first
                                            if (r > 0) {
                                                val newLayout = currentLayout.map { it.toMutableList() }.toMutableList()
                                                val temp = newLayout[r]
                                                newLayout[r] = newLayout[r - 1]
                                                newLayout[r - 1] = temp
                                                currentLayout = newLayout
                                                selectedKeyPos = (r - 1) to pos.second
                                            }
                                        }
                                    },
                                    enabled = isKeySelected && selectedKeyPos?.first?.let { it > 0 } == true
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowUpward,
                                        contentDescription = "Move Row Up",
                                        tint = if (isKeySelected && selectedKeyPos?.first?.let { it > 0 } == true) MaterialTheme.colorScheme.primary else Color.DarkGray
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        selectedKeyPos?.let { pos ->
                                            val r = pos.first
                                            if (r < currentLayout.size - 1) {
                                                val newLayout = currentLayout.map { it.toMutableList() }.toMutableList()
                                                val temp = newLayout[r]
                                                newLayout[r] = newLayout[r + 1]
                                                newLayout[r + 1] = temp
                                                currentLayout = newLayout
                                                selectedKeyPos = (r + 1) to pos.second
                                            }
                                        }
                                    },
                                    enabled = isKeySelected && selectedKeyPos?.first?.let { it < currentLayout.size - 1 } == true
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowDownward,
                                        contentDescription = "Move Row Down",
                                        tint = if (isKeySelected && selectedKeyPos?.first?.let { it < currentLayout.size - 1 } == true) MaterialTheme.colorScheme.primary else Color.DarkGray
                                    )
                                }
                            }

                            val canDeleteKey = isKeySelected && selectedKeyPos?.let { pos ->
                                currentLayout[pos.first].size > 1 || currentLayout.size > 3
                            } == true

                            val canDeleteRow = isKeySelected && currentLayout.size > 3

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        selectedKeyPos?.let { pos ->
                                            val r = pos.first
                                            val k = pos.second
                                            val newLayout = currentLayout.map { it.toMutableList() }.toMutableList()

                                            newLayout[r].removeAt(k)
                                            if (newLayout[r].isEmpty()) {
                                                newLayout.removeAt(r)
                                            }
                                            currentLayout = newLayout
                                            selectedKeyPos = null
                                        }
                                    },
                                    enabled = canDeleteKey,
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(25.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp),
                                    border = BorderStroke(1.dp, if (canDeleteKey) Color.DarkGray else Color.Transparent),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.Transparent,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = if (canDeleteKey) Color.Red else Color.DarkGray, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.del_key),
                                        color = if (canDeleteKey) Color.Red else Color.DarkGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        selectedKeyPos?.let { pos ->
                                            val r = pos.first
                                            val newLayout = currentLayout.map { it.toMutableList() }.toMutableList()

                                            newLayout.removeAt(r)

                                            currentLayout = newLayout
                                            selectedKeyPos = null
                                        }
                                    },
                                    enabled = canDeleteRow,
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(25.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp),
                                    border = BorderStroke(1.dp, if (canDeleteRow) Color.DarkGray else Color.Transparent),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.Transparent,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = if (canDeleteRow) Color.Red else Color.DarkGray, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.del_row),
                                        color = if (canDeleteRow) Color.Red else Color.DarkGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val newKey = com.noxtan.noxboard.Key(normalText = "", shiftText = "", code = 0, widthWeight = 1.0f)
                                val newLayout = currentLayout.map { it.toMutableList() }.toMutableList()

                                val targetRow = selectedKeyPos?.first ?: if (newLayout.size > 2) newLayout.size - 2 else newLayout.size - 1

                                newLayout[targetRow].add(newKey)
                                currentLayout = newLayout
                                selectedKeyPos = targetRow to (newLayout[targetRow].size - 1)
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.new_key), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Button(
                            onClick = {

                                val newKey = com.noxtan.noxboard.Key(normalText = "", shiftText = "", code = 0, widthWeight = 1.0f)
                                val newLayout = currentLayout.map { it.toMutableList() }.toMutableList()

                                val targetIndex = maxOf(0, newLayout.size - 1)
                                newLayout.add(targetIndex, mutableListOf(newKey))

                                currentLayout = newLayout
                                selectedKeyPos = targetIndex to 0
                            },
                            enabled = currentLayout.size < 6,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = if (currentLayout.size < 6) MaterialTheme.colorScheme.primary else Color.DarkGray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.new_row), color = if (currentLayout.size < 6) MaterialTheme.colorScheme.primary else Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        com.noxtan.noxboard.ui.components.UnsavedChangesDialog(
            showDialog = showExitDialog,
            onDismiss = { showExitDialog = false },
            onConfirm = {
                when (selectedTabIndex) {
                    0 -> {
                        prefs.activeLayoutMyanmar = initialActivePresetId
                        prefs.savedLayoutsMyanmar = initialSavedLayoutsJson
                    }
                    1 -> {
                        prefs.activeLayoutEnglish = initialActivePresetId
                        prefs.savedLayoutsEnglish = initialSavedLayoutsJson
                    }
                    else -> {
                        prefs.activeLayoutSymbols = initialActivePresetId
                        prefs.savedLayoutsSymbols = initialSavedLayoutsJson
                    }
                }
                showExitDialog = false
                onBack()
            }
        )

        if (pendingTabSwitchIndex != null) {
            AlertDialog(
                onDismissRequest = { pendingTabSwitchIndex = null },
                title = {
                    Text("Unsaved Changes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                },
                text = {
                    Text("လက်ရှိ Tab တွင် ပြင်ဆင်ထားမှုများကို သိမ်းဆည်းမည်လား?", color = Color.White, fontSize = 14.sp)
                },
                confirmButton = {
                    TextButton(onClick = {
                        saveCurrentLayout()
                        selectedTabIndex = pendingTabSwitchIndex!!
                        selectedKeyPos = null
                        pendingTabSwitchIndex = null
                    }) {
                        Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            selectedTabIndex = pendingTabSwitchIndex!!
                            selectedKeyPos = null
                            pendingTabSwitchIndex = null
                        }) {
                            Text("Discard", color = Color.Red)
                        }
                        TextButton(onClick = {
                            pendingTabSwitchIndex = null
                        }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (presetToDelete != null) {
            AlertDialog(
                onDismissRequest = { presetToDelete = null },
                title = {
                    Text(
                        text = "Delete Layout Slot",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Text(
                        text = "ဤ Layout Slot $presetToDelete လက်ကွက်ဆက်တင်ကို လုံးဝဖျက်ပစ်လိုပါသလား?",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val idToDelete = presetToDelete
                        presetToDelete = null
                        if (idToDelete != null) {
                            val filteredList = layoutPresetsList.filter { it.optString("id") != idToDelete }
                            layoutPresetsList = filteredList as MutableList<JSONObject>

                            val arr = org.json.JSONArray()
                            filteredList.forEach { arr.put(it) }

                            when (selectedTabIndex) {
                                0 -> prefs.savedLayoutsMyanmar = arr.toString()
                                1 -> prefs.savedLayoutsEnglish = arr.toString()
                                else -> prefs.savedLayoutsSymbols = arr.toString()
                            }

                            if (activePresetId == idToDelete) {
                                selectPreset("default")
                            }
                            android.widget.Toast.makeText(context, "Layout Slot $idToDelete deleted!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { presetToDelete = null }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        val editType = valueToEditType
        if (editType != null) {
            var textInput by remember {
                mutableStateOf(
                    when (editType) {
                        "WEIGHT" -> editKeyWeight.toString()
                        "SCALEX" -> (editScaleX * 100).toInt().toString()
                        "SCALEY" -> (editScaleY * 100).toInt().toString()
                        "ALIGNX" -> (editAlignX * 100).toInt().toString()
                        else -> (editAlignY * 100).toInt().toString()
                    }
                )
            }

            AlertDialog(
                onDismissRequest = { valueToEditType = null },
                title = {
                    Text(
                        text = when (editType) {
                            "WEIGHT" -> "Edit Key Weight"
                            "SCALEX" -> "Edit Inner Width (%)"
                            "SCALEY" -> "Edit Inner Height (%)"
                            "ALIGNX" -> "Edit Align X (%)"
                            else -> "Edit Align Y (%)"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Column {
                        Text(
                            text = when (editType) {
                                "WEIGHT" -> "ကန့်သတ်ချက်ဘောင်: 0.5 မှ 5.0 ကြား"
                                "SCALEX", "SCALEY" -> "ကန့်သတ်ချက်ဘောင်: 50% မှ 100% ကြား"
                                else -> "ကန့်သတ်ချက်ဘောင်: 0% မှ 100% ကြား"
                            },
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val inputVal = textInput.toFloatOrNull()
                        if (inputVal != null) {
                            selectedKeyPos?.let { pos ->
                                when (editType) {
                                    "WEIGHT" -> {
                                        val clamped = inputVal.coerceIn(0.5f, 5.0f)
                                        editKeyWeight = clamped
                                        updateSelectedKey(currentLayout[pos.first][pos.second].copy(widthWeight = clamped))
                                    }
                                    "SCALEX" -> {
                                        val clampedPercent = inputVal.coerceIn(50f, 100f)
                                        val clamped = clampedPercent / 100f
                                        editScaleX = clamped
                                        updateSelectedKey(currentLayout[pos.first][pos.second].copy(scaleX = clamped))
                                    }
                                    "SCALEY" -> {
                                        val clampedPercent = inputVal.coerceIn(50f, 100f)
                                        val clamped = clampedPercent / 100f
                                        editScaleY = clamped
                                        updateSelectedKey(currentLayout[pos.first][pos.second].copy(scaleY = clamped))
                                    }
                                    "ALIGNX" -> {
                                        val clampedPercent = inputVal.coerceIn(0f, 100f)
                                        val clamped = clampedPercent / 100f
                                        editAlignX = clamped
                                        updateSelectedKey(currentLayout[pos.first][pos.second].copy(alignX = clamped))
                                    }
                                    "ALIGNY" -> {
                                        val clampedPercent = inputVal.coerceIn(0f, 100f)
                                        val clamped = clampedPercent / 100f
                                        editAlignY = clamped
                                        updateSelectedKey(currentLayout[pos.first][pos.second].copy(alignY = clamped))
                                    }
                                }
                            }
                        }
                        valueToEditType = null
                    }) {
                        Text("OK", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { valueToEditType = null }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (showCharPickerFor != null) {
            val target = showCharPickerFor
            var selectedCategory by remember { mutableIntStateOf(0) }
            val categories = listOf(stringResource(R.string.char_myanmar), stringResource(R.string.char_english), stringResource(R.string.char_numbers), stringResource(R.string.char_action_keys))

            androidx.compose.ui.window.Dialog(onDismissRequest = { showCharPickerFor = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.select_char_action),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        ScrollableTabRow(
                            selectedTabIndex = selectedCategory,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            edgePadding = 8.dp
                        ) {
                            categories.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedCategory == index,
                                    onClick = { selectedCategory = index },
                                    text = { Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedCategory == 3) {
                            val actionIcons = listOf(
                                ActionIcon("Backspace", -2, "iconStyleBackspace", listOf("DEFAULT", "ic_backspace", "ic_backspace_cyber_slash", "ic_backspace_smooth_pill", "ic_backspace_solid_block", "ic_backspace_tech_wipe")),
                                ActionIcon("Symbols (?123)", -4, "iconStyleSymbols", listOf("DEFAULT", "ic_sym_classic_math", "ic_sym_pure_vector_line", "ic_sym_smooth_wave", "ic_sym_tech_bracket", "ic_sym_tech_pager")),
                                ActionIcon("Symbols Page", -5, "", listOf("DEFAULT", "ic_sym_classic_math", "ic_sym_pure_vector_line", "ic_sym_smooth_wave", "ic_sym_tech_bracket", "ic_sym_tech_pager")),
                                ActionIcon("Hide Keyboard", -16, "iconStyleHideKeyboard", listOf("DEFAULT", "ic_hide_keys_cyber_grid", "ic_hide_keys_smooth_hide", "ic_hide_keys_solid_drop", "ic_hide_keys_tech_minimize")),

                                ActionIcon("Emoji", -11, "iconStyleEmoji", listOf("DEFAULT", "ic_emoji_cheeky", "ic_emoji_bully", "ic_emoji_crying", "ic_emoji_punk")),
                                ActionIcon("Enter", 10, "iconStyleEnter", listOf("DEFAULT", "ic_enter_classic_return", "ic_enter_cyber_pipe", "ic_enter_smooth_swoop", "ic_enter_solid_return", "ic_enter_tech_slash")),
                                ActionIcon("Globe", -3, "iconStyleGlobe", listOf("DEFAULT", "ic_global_classic_globe", "ic_global_cyber_radar", "ic_global_smooth_orbit", "ic_global_solid_earth", "ic_global_tech_network")),
                                ActionIcon("Vault", -12, "iconStyleVault", listOf("DEFAULT", "ic_key_bio_card", "ic_key_classic", "ic_key_cyber", "ic_key_smart_lock", "ic_key_smooth_secure")),
                                ActionIcon("Numpad", -8, "iconStyleNumpad", listOf("DEFAULT", "ic_keypad_classic_dots", "ic_keypad_cyber_squaricle", "ic_keypad_smooth_pills", "ic_keypad_solid_grid", "ic_keypad_tech_matrix")),
                                ActionIcon("Clipboard", -13, "iconStyleClipboard", listOf("DEFAULT", "ic_copy_classic_outline", "ic_copy_cyber_sharp", "ic_copy_smooth_curve", "ic_copy_solid_block", "ic_copy_tech_nodes")),
                                ActionIcon("Settings", -14, "iconStyleSetting", listOf("DEFAULT", "ic_setting_classic_gear", "ic_setting_cyber_hex", "ic_setting_smooth_dial", "ic_setting_solid_gear", "ic_setting_tech_sliders")),
                                ActionIcon("Shift", -1, "iconStyleShift", listOf("DEFAULT", "ic_shift_classic_inactive", "ic_shift_cyber_inactive", "ic_shift_smooth_inactive", "ic_shift_solid_bold_inactive", "ic_shift_tech_inactive")),
                                ActionIcon("Spacebar", 32, "iconStyleSpacebar", listOf("DEFAULT", "ic_spacebar_classic_bar", "ic_spacebar_cyber_segments", "ic_spacebar_smooth_pill", "ic_spacebar_solid_base", "ic_spacebar_square_bracket", "ic_spacebar_tech_brackets", "ic_spacebar_text_space", "ic_spacebar_u_bracket"))
                            )

                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.height(220.dp).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(actionIcons.size) { i ->
                                    val action = actionIcons[i]
                                    Text(action.title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(action.icons.size) { j ->
                                            val iconName = action.icons[j]
                                            val resId = if (iconName == "DEFAULT") {
                                                when(action.code) {
                                                    -11 -> R.drawable.ic_emoji
                                                    10 -> R.drawable.ic_enter
                                                    -3 -> R.drawable.ic_earth
                                                    -12 -> R.drawable.ic_pass
                                                    -8 -> R.drawable.ic_numpad
                                                    -13 -> R.drawable.ic_copy
                                                    -14 -> R.drawable.ic_setting
                                                    -1 -> R.drawable.ic_shift_inactive
                                                    -2 -> R.drawable.ic_backspace_classic_erase
                                                    -4 -> R.drawable.ic_sym_classic_text
                                                    -5 -> R.drawable.ic_sym_classic_text
                                                    -16 -> R.drawable.ic_hide_keys_classic_down
                                                    else -> 0
                                                }
                                            } else {
                                                context.resources.getIdentifier(iconName, "drawable", context.packageName)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .clickable {
                                                        val text = when(action.code) {
                                                            32 -> "Space"
                                                            10 -> "Enter"
                                                            -1 -> "Shift"
                                                            -11 -> "Emoji"
                                                            -3 -> "Globe"
                                                            -12 -> "Vault"
                                                            -8 -> "Numpad"
                                                            -13 -> "Clipboard"
                                                            -14 -> "Settings"
                                                            -2 -> "⌫"
                                                            -4 -> "?123"
                                                            -5 -> "=\\<"
                                                            -16 -> "⌨"
                                                            else -> action.title
                                                        }

                                                        val shiftTextForAction = when(action.code) {
                                                            -5 -> "?123"
                                                            else -> text
                                                        }

                                                        editNormalText = text
                                                        editShiftText = shiftTextForAction

                                                        selectedKeyPos?.let { pos ->
                                                            updateSelectedKey(currentLayout[pos.first][pos.second].copy(
                                                                code = action.code,
                                                                normalText = text,
                                                                shiftText = shiftTextForAction,
                                                                customIcon = if (iconName == "DEFAULT") null else iconName
                                                            ))
                                                        }
                                                        showCharPickerFor = null
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (action.code == 32 && iconName == "DEFAULT") {
                                                    Text("Space", color = Color.White, fontSize = 10.sp)
                                                } else if (resId != 0) {
                                                    Icon(
                                                        painter = painterResource(id = resId),
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                } else {
                                                    val fallbackText = when(action.code) {
                                                        -5 -> "=\\<"
                                                        -4 -> "?123"
                                                        else -> action.title
                                                    }
                                                    Text(fallbackText, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            val charList = when (selectedCategory) {
                                0 -> myanmarChars
                                1 -> englishChars
                                else -> numberChars
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                modifier = Modifier.height(220.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(charList.size) { i ->
                                    val char = charList[i]
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                if (target == "NORMAL") {
                                                    editNormalText = char
                                                    selectedKeyPos?.let { pos ->
                                                        updateSelectedKey(currentLayout[pos.first][pos.second].copy(normalText = char, code = 0))
                                                    }
                                                } else if (target == "SHIFT") {
                                                    editShiftText = char
                                                    selectedKeyPos?.let { pos ->
                                                        updateSelectedKey(currentLayout[pos.first][pos.second].copy(shiftText = char, code = 0))
                                                    }
                                                }
                                                showCharPickerFor = null
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(char, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            TextButton(onClick = { showCharPickerFor = null }) {
                                Text(stringResource(R.string.close), color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

    }
}

private fun getKeyIconRes(code: Int): Int? {
    return when (code) {
        -11 -> R.drawable.ic_emoji
        -12 -> R.drawable.ic_pass
        -13 -> R.drawable.ic_copy
        -14 -> R.drawable.ic_setting
        -8  -> R.drawable.ic_numpad
        -3  -> R.drawable.ic_earth
        -1  -> R.drawable.ic_shift_inactive
        10  -> R.drawable.ic_enter
        else -> null
    }
}