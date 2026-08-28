package com.noxtan.noxboard.ui.screens.tabs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxtan.noxboard.KeyboardMode
import com.noxtan.noxboard.NoxBoardPrefs
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LayoutTab() {
    val context = LocalContext.current
    val prefs = remember { NoxBoardPrefs(context) }

    var activeLayoutState by remember { mutableStateOf(prefs.activeLayoutMyanmar) }

    val layouts = listOf(
        Triple("Modern Layout", "စာရိုက်ရလွယ်ကူကျယ်ဝန်းသော ခေတ်မီလက်ကွက်ပုံစံ", "MODERN"),
        Triple("Classic Layout", "ရင်းနှီးပြီးသား ရိုးရာလက်ကွက်ပုံစံအဟောင်း", "CLASSIC"),
        Triple("Compact Layout", "ကျစ်လျစ်သိပ်သည်းသော စနစ်လက်ကွက်ပုံစံ", "COMPACT")
    )

    val initialPage = layouts.indexOfFirst { it.third == prefs.activeLayoutMyanmar }.takeIf { it >= 0 } ?: 0
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { layouts.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            pageSpacing = 32.dp
        ) { page ->
            val layoutData = layouts[page]
            val isActive = activeLayoutState == layoutData.third

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = layoutData.first,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = layoutData.second,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            com.noxtan.noxboard.NoxKeyboardView(ctx).apply {
                                isEditMode = true
                            }
                        },
                        update = { view ->
                            view.currentMode = KeyboardMode.MYANMAR
                            view.previewLayoutId = layoutData.third
                            view.applySettings(prefs)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        prefs.activeLayoutMyanmar = layoutData.third
                        prefs.activeLayoutEnglish = layoutData.third
                        activeLayoutState = layoutData.third
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                        contentColor = if (isActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp)
                ) {
                    Text(
                        text = if (isActive) "Active Layout" else "Apply Layout",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .height(8.dp)
                        .width(width)
                )
            }
        }
    }
}