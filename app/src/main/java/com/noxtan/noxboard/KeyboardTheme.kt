package com.noxtan.noxboard

import android.graphics.Color
import androidx.core.graphics.toColorInt

data class KeyboardTheme(
    var backgroundColor: Int = "#000000".toColorInt(),
    var backgroundGradient: IntArray? = null,
    var keyColor: Int = "#1E1A2B".toColorInt(),
    var keyGradient: IntArray? = null,
    var keyBorderColor: Int = Color.TRANSPARENT,
    var specialKeyColor: Int = "#12101C".toColorInt(),
    var specialKeyGradient: IntArray? = null,
    var specialTextColor: Int = Color.WHITE,
    var specialTextGradient: IntArray? = null,
    var activeKeyColor: Int = "#7C4DFF".toColorInt(),
    var activeKeyGradient: IntArray? = null,
    var textColor: Int = Color.WHITE,
    var textGradient: IntArray? = null,

    var isTextureEffectEnabled: Boolean = false,

    var popupBackgroundColor: Int = "#12101C".toColorInt(),
    var popupBgGradient: IntArray? = null,
    var popupKeyColor: Int = "#2D2D2D".toColorInt(),
    var popupSpecialKeyColor: Int = "#3A3A3C".toColorInt(),
    var popupActiveKeyColor: Int = "#4E4E50".toColorInt(),
    var popupTextColor: Int = Color.WHITE,
    var popupTextGradient: IntArray? = null,

    var keyboardHeightDp: Float = 235f,
    var bottomPaddingDp: Float = 54f,
    var keyPaddingDp: Float = 1f,
    var keyCornerRadiusDp: Float = 10f,
    var textSizeSp: Float = 16f,

    var numpadBackgroundColor: Int = "#000000".toColorInt(),
    var numpadBgGradient: IntArray? = null,
    var numpadKeyColor: Int = "#1E1A2B".toColorInt(),
    var numpadKeyGradient: IntArray? = null,
    var numpadTextColor: Int = Color.WHITE,
    var numpadTextGradient: IntArray? = null,
    var numpadSpecialKeyColor: Int = "#12101C".toColorInt(),
    var numpadSpecialKeyGradient: IntArray? = null,
    var numpadSpecialTextColor: Int = Color.WHITE,
    var numpadSpecialTextGradient: IntArray? = null,

    var keyboardWidthPercent: Float = 100f,
    var keyboardAlignment: String = "CENTER",
    var globalRowGapDp: Float = 0f,
    var individualRowGapsMap: Map<Int, Float> = emptyMap()
)