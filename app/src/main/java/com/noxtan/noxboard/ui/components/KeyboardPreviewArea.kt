package com.noxtan.noxboard.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeyboardPreviewArea(
    textState: String,
    onTextChange: (String) -> Unit,
    isImeVisible: Boolean,
    isSlidingActive: Boolean,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    animatedWidth: Dp,
    animatedHeight: Dp,
    animatedCornerRadius: Dp,
    animatedBgColor: Color,
    animatedBorderColor: Color,
    modifier: Modifier = Modifier
) {
    val bottomPadding by animateDpAsState(
        targetValue = if (isSlidingActive) 0.dp else if (isImeVisible) 12.dp else 32.dp,
        label = "bottomPaddingAnimation"
    )

    BasicTextField(
        value = if (isImeVisible && !isSlidingActive) textState else "",
        onValueChange = { if (isImeVisible && !isSlidingActive) onTextChange(it) },
        modifier = modifier
            .padding(bottom = bottomPadding)
            .size(
                width = if (isSlidingActive) 1.dp else animatedWidth,
                height = if (isSlidingActive) 1.dp else animatedHeight
            )
            .alpha(if (isSlidingActive) 0f else 1f)
            .focusRequester(focusRequester),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontSize = 16.sp),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(animatedWidth)
                        .height(animatedHeight)
                        .border(
                            width = if (isImeVisible) 1.5.dp else 0.dp,
                            color = animatedBorderColor,
                            shape = RoundedCornerShape(animatedCornerRadius)
                        )
                        .background(
                            color = animatedBgColor,
                            shape = RoundedCornerShape(animatedCornerRadius)
                        )
                        .clickable(!isImeVisible) {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = if (isImeVisible) Alignment.CenterStart else Alignment.Center
                ) {
                    if (isImeVisible) {
                        if (textState.isEmpty()) {
                            Text("ဒီမှာ စာရိုက်စမ်းသပ်ပါ...", color = Color.Gray, fontSize = 15.sp)
                        }
                        innerTextField()
                    } else {
                        Text(
                            text = "Show Keyboard",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    )
}