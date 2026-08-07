package com.noxtan.noxboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withClip
import androidx.core.net.toUri
import org.json.JSONObject
import kotlin.math.abs

private var iconEmoji: Drawable? = null
private var iconCopy: Drawable? = null
private var iconPaste: Drawable? = null
private var iconEarth: Drawable? = null
private var iconEnter: Drawable? = null
private var iconNext: Drawable? = null
private var iconNumpad: Drawable? = null
private var iconPass: Drawable? = null
private var iconSearch: Drawable? = null
private var iconSend: Drawable? = null
private var iconSetting: Drawable? = null
private var iconShiftActive: Drawable? = null
private var iconShiftCapslock: Drawable? = null
private var iconShiftInactive: Drawable? = null
private var iconSpacebar: Drawable? = null
private var iconBackspace: Drawable? = null
private var iconSymbols: Drawable? = null
private var iconHideKeyboard: Drawable? = null

enum class KeyboardMode { MYANMAR, ENGLISH, SYMBOLS_1, SYMBOLS_2, NUMBER }

class NoxKeyboardView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var onSuggestionBoundsChanged: ((RectF?) -> Unit)? = null

    var hasSelection = false

    var theme = KeyboardTheme()
    var currentMode = KeyboardMode.MYANMAR
    var useCustomIconColors = false

    private val customIconCache = mutableMapOf<String, Drawable?>()
    private fun getCustomIcon(name: String?): Drawable? {
        if (name.isNullOrEmpty() || name == "DEFAULT") return null
        if (customIconCache.containsKey(name)) return customIconCache[name]
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        val drawable = if (resId != 0) androidx.core.content.ContextCompat.getDrawable(context, resId) else null
        customIconCache[name] = drawable
        return drawable
    }
    var isEditMode = false
    var editSelectedKeyPos: Pair<Int, Int>? = null
    var editSelectedKeyPositions: Set<Pair<Int, Int>> = emptySet()
    var editSwapKey1Pos: Pair<Int, Int>? = null
    var editSwapKey2Pos: Pair<Int, Int>? = null
    var onEditKeyClick: ((Int, Int) -> Unit)? = null

    private var lastShiftTapTime = 0L
    private val SHIFT_DOUBLE_TAP_TIMEOUT = 300L
    private var isCapsLock = false

    private var keys = listOf<List<Key>>()
    private var numpadKeys = listOf<Key>()

    private var isShifted = false
    var isInlineNumberRowVisible = true
    var userForceShowNumberRow = false
    var isNumpadMode = false
    private var activeKey: Key? = null
    private var isTopRowRemoved = false

    private val popupRect = RectF()
    private var enterKeyLabel: String = "⏎"

    private var isSpaceDown = false
    private var isSpaceCursorControlEnabled = true
    private var spaceDragDelayMs = 100L
    private var spaceDownTimeForDrag = 0L
    private var isSpaceDragging = false
    private var isSpaceCursorActivated = false
    private var isSpaceCursorCanceled = false
    private var spaceDragStartX = 0f
    private var spaceDragStartY = 0f
    private var spaceDragThreshold = 0f
    private var lastSpaceDownTime = 0L
    private var isSpaceSelecting = false
    private var isSpaceDoubleTapped = false
    private val SPACE_DOUBLE_TAP_TIMEOUT = 300L

    private val repeatHandler = Handler(Looper.getMainLooper())
    private var repeatRunnable: Runnable? = null

    private val longPressHandler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null
    private var isAccentMode = false
    private val accentKeys = mutableListOf<Key>()
    private var activeAccentKey: Key? = null
    private val accentPopupRect = RectF()

    private val tempRect = RectF()
    private val dimColor = "#AA000000".toColorInt()
    private val accentDimColor = "#44000000".toColorInt()

    private lateinit var keyPaint: Paint
    private lateinit var keyBorderPaint: Paint
    private lateinit var specialKeyPaint: Paint
    private lateinit var activeKeyPaint: Paint
    private lateinit var textPaint: Paint
    private lateinit var popupBgPaint: Paint
    private lateinit var popupKeyPaint: Paint
    private lateinit var popupSpecialKeyPaint: Paint
    private lateinit var popupActiveKeyPaint: Paint
    private lateinit var popupTextPaint: Paint
    private lateinit var numpadBgPaint: Paint
    private lateinit var numpadKeyPaint: Paint
    private lateinit var numpadTextPaint: Paint
    private lateinit var numpadSpecialKeyPaint: Paint
    private lateinit var numpadSpecialTextPaint: Paint
    private lateinit var accentTextPaint: Paint
    private lateinit var bgPaint: Paint
    private lateinit var borderPaint: Paint
    private var noisePaint: Paint? = null

    data class ParsedColor(val color: Int, val gradient: IntArray?)

    private lateinit var individualKeyPaint: Paint
    private var individualKeyColorsMap = mutableMapOf<String, ParsedColor>()
    private var individualKeyTextColorsMap = mutableMapOf<String, ParsedColor>()
    private var individualKeyActiveColorsMap = mutableMapOf<String, ParsedColor>()
    private var individualKeyPopupBgColorsMap = mutableMapOf<String, ParsedColor>()
    private var individualKeyPopupTextColorsMap = mutableMapOf<String, ParsedColor>()

    private var previewPopup: PopupWindow? = null
    private var previewOverlay: PreviewOverlayView? = null

    private var wallpaperBitmap: Bitmap? = null
    private var lastWallpaperUriString: String? = null

    var isEmojiMode = false
        set(value) {
            field = value
            requestLayout()
            calculateKeyLayout(width, height)
        }

    private val prefs = NoxBoardPrefs(context)

    init {
        updateLayoutData()
        numpadKeys = KeyboardLayout.getNumpadLayout()
        initPaints()
        initIcons()
        initPreviewPopup()
        spaceDragThreshold = 4.5f * resources.displayMetrics.density
        loadWallpaper(prefs.customWallpaperUri)
    }

    private fun initPreviewPopup() {
        previewOverlay = PreviewOverlayView(context)
        previewPopup = PopupWindow(
            previewOverlay,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            isTouchable = false
            animationStyle = 0
            isClippingEnabled = false
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    private fun triggerHapticFeedback(feedbackConstant: Int) {
        if (prefs.isVibrationEnabled) {
            performHapticFeedback(feedbackConstant)
        }
    }

    private fun loadWallpaper(uriString: String?) {
        if (uriString.isNullOrEmpty()) {
            wallpaperBitmap = null
            lastWallpaperUriString = null
            invalidate()
            return
        }
        if (uriString == lastWallpaperUriString) return

        Thread {
            try {
                val uri = uriString.toUri()
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    post {
                        wallpaperBitmap = bitmap
                        lastWallpaperUriString = uriString
                        invalidate()
                    }
                }
            } catch (e: Exception) {
                post {
                    wallpaperBitmap = null
                    lastWallpaperUriString = null
                    invalidate()
                }
            }
        }.start()
    }

    private val previewHandler = Handler(Looper.getMainLooper())
    private var previewDismissRunnable: Runnable? = null

    fun getEffectiveKeyboardHeightDp(): Float {
        val config = resources.configuration
        val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val isTabletOrFoldable = config.smallestScreenWidthDp >= 600

        val baseHeight = if (isEmojiMode) {
            if (isTabletOrFoldable && !isLandscape) 300f else 250f
        } else {
            if (isTabletOrFoldable && !isLandscape) theme.keyboardHeightDp * 1.2f else theme.keyboardHeightDp
        }

        return if (isLandscape && !isTabletOrFoldable) 200f else baseHeight
    }

    fun getEffectiveTextSizeSp(): Float {
        val config = resources.configuration
        val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val isTabletOrFoldable = config.smallestScreenWidthDp >= 600
        val scaledDensity = resources.displayMetrics.scaledDensity
        var sizeSp = theme.textSizeSp
        if (isTabletOrFoldable) sizeSp *= 1.15f
        if (isLandscape && !isTabletOrFoldable) sizeSp *= 0.85f

        return sizeSp * scaledDensity
    }

    fun getEffectiveBottomPaddingDp(): Float {
        val config = resources.configuration
        val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val isTabletOrFoldable = config.smallestScreenWidthDp >= 600

        if (isLandscape && !isTabletOrFoldable) {
            val resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
            val isGestureNav = if (resourceId > 0) resources.getInteger(resourceId) == 2 else false
            return if (isGestureNav) 16f else 4f
        }
        return if (isEmojiMode) 54f else theme.bottomPaddingDp
    }

    fun applySettings(prefs: NoxBoardPrefs) {
        val density = resources.displayMetrics.density
        useCustomIconColors = prefs.useCustomIconColors

        isSpaceCursorControlEnabled = prefs.isSpaceCursorControlEnabled
        spaceDragDelayMs = prefs.spaceDragDelay.toLong()
        spaceDragThreshold = (11f - prefs.spaceCursorSensitivity) * density

        theme = KeyboardTheme()

        val safeParse = { hex: String, applyTheme: (Int, IntArray?) -> Unit ->
            try {
                if (hex.startsWith("GRADIENT:")) {
                    val arr = hex.removePrefix("GRADIENT:").split(",").map { android.graphics.Color.parseColor(it) }.toIntArray()
                    applyTheme(arr[0], arr)
                } else {
                    applyTheme(android.graphics.Color.parseColor(hex), null)
                }
            } catch (e: Exception) {}
        }

        safeParse(prefs.customNumpadBgColor) { c, g -> theme.numpadBackgroundColor = c; theme.numpadBgGradient = g }
        safeParse(prefs.customNumpadKeyColor) { c, g -> theme.numpadKeyColor = c; theme.numpadKeyGradient = g }
        safeParse(prefs.customNumpadTextColor) { c, g -> theme.numpadTextColor = c; theme.numpadTextGradient = g }
        safeParse(prefs.customNumpadSpecialKeyColor) { c, g -> theme.numpadSpecialKeyColor = c; theme.numpadSpecialKeyGradient = g }
        safeParse(prefs.customNumpadSpecialTextColor) { c, g -> theme.numpadSpecialTextColor = c; theme.numpadSpecialTextGradient = g }
        safeParse(prefs.customBackgroundColor) { c, g -> theme.backgroundColor = c; theme.backgroundGradient = g }
        safeParse(prefs.customKeyColor) { c, g -> theme.keyColor = c; theme.keyGradient = g }
        safeParse(prefs.customSpecialKeyColor) { c, g -> theme.specialKeyColor = c; theme.specialKeyGradient = g }
        safeParse(prefs.customActiveKeyColor) { c, g -> theme.activeKeyColor = c; theme.activeKeyGradient = g }
        safeParse(prefs.customPopupBgColor) { c, g -> theme.popupBackgroundColor = c; theme.popupBgGradient = g }
        safeParse(prefs.customPopupTextColor) { c, g -> theme.popupTextColor = c; theme.popupTextGradient = g }
        safeParse(prefs.customTextColor) { c, g -> theme.textColor = c; theme.textGradient = g }
        safeParse(prefs.customSpecialTextColor) { c, g -> theme.specialTextColor = c; theme.specialTextGradient = g }
        theme.isTextureEffectEnabled = prefs.isTextureEffectEnabled
        try { theme.keyBorderColor = prefs.customKeyBorderColor.toColorInt() } catch (e: Exception) { theme.keyBorderColor = Color.TRANSPARENT }

        individualKeyColorsMap.clear(); try { val j = org.json.JSONObject(prefs.individualKeyColors); j.keys().forEach { individualKeyColorsMap[it] = parseColorString(j.getString(it)) } } catch (e: Exception) {}
        individualKeyTextColorsMap.clear(); try { val j = org.json.JSONObject(prefs.individualKeyTextColors); j.keys().forEach { individualKeyTextColorsMap[it] = parseColorString(j.getString(it)) } } catch (e: Exception) {}
        individualKeyActiveColorsMap.clear(); try { val j = org.json.JSONObject(prefs.individualKeyActiveColors); j.keys().forEach { individualKeyActiveColorsMap[it] = parseColorString(j.getString(it)) } } catch (e: Exception) {}
        individualKeyPopupBgColorsMap.clear(); try { val j = org.json.JSONObject(prefs.individualKeyPopupBgColors); j.keys().forEach { individualKeyPopupBgColorsMap[it] = parseColorString(j.getString(it)) } } catch (e: Exception) {}
        individualKeyPopupTextColorsMap.clear(); try { val j = org.json.JSONObject(prefs.individualKeyPopupTextColors); j.keys().forEach { individualKeyPopupTextColorsMap[it] = parseColorString(j.getString(it)) } } catch (e: Exception) {}

        theme.popupKeyColor = theme.keyColor
        theme.popupSpecialKeyColor = theme.specialKeyColor
        theme.popupActiveKeyColor = theme.activeKeyColor

        individualKeyColorsMap.clear()
        try { val j = JSONObject(prefs.individualKeyColors); j.keys().forEach { individualKeyColorsMap[it] = parseColorString(j.getString(it)) } } catch (e: Exception) {}

        individualKeyTextColorsMap.clear()
        try { val j = JSONObject(prefs.individualKeyTextColors); j.keys().forEach { individualKeyTextColorsMap[it] = parseColorString(j.getString(it)) } } catch (e: Exception) {}

        individualKeyActiveColorsMap.clear()
        try { val j = JSONObject(prefs.individualKeyActiveColors); j.keys().forEach { individualKeyActiveColorsMap[it] = parseColorString(j.getString(it)) } } catch (e: Exception) {}

        individualKeyPopupBgColorsMap.clear()
        try { val j = JSONObject(prefs.individualKeyPopupBgColors); j.keys().forEach { individualKeyPopupBgColorsMap[it] = parseColorString(j.getString(it)) } } catch (e: Exception) {}

        individualKeyPopupTextColorsMap.clear()
        try { val j = JSONObject(prefs.individualKeyPopupTextColors); j.keys().forEach { individualKeyPopupTextColorsMap[it] = parseColorString(j.getString(it)) } } catch (e: Exception) {}

        bgPaint.color = theme.backgroundColor

        loadWallpaper(prefs.customWallpaperUri)

        updateLayoutData()

        if (prefs.isSliding) {
            theme.keyboardHeightDp = prefs.slideHeight
            theme.keyboardWidthPercent = prefs.slideWidth
            theme.keyboardAlignment = prefs.slideAlignment
            theme.bottomPaddingDp = prefs.slideBottomPadding

            val maxHeightPx = (300f * density).toInt() + (100f * density).toInt()
            if (height < maxHeightPx) {
                requestLayout()
            }
        } else {
            theme.keyboardHeightDp = prefs.keyboardHeight
            theme.keyboardWidthPercent = prefs.keyboardWidth
            theme.keyboardAlignment = prefs.keyboardAlignment
            theme.bottomPaddingDp = prefs.bottomPadding
            theme.globalRowGapDp = prefs.globalRowGap
            val gapsMap = mutableMapOf<Int, Float>()
            try {
                val json = JSONObject(prefs.individualRowGaps)
                json.keys().forEach { k -> gapsMap[k.toInt()] = json.getDouble(k).toFloat() }
            } catch (e: Exception) {}
            theme.individualRowGapsMap = gapsMap
            requestLayout()
        }

        theme.textSizeSp = prefs.keyFontSize
        theme.keyPaddingDp = prefs.keyPadding
        theme.keyCornerRadiusDp = prefs.keyCornerRadius
        initPaints()
        initIcons()

        calculateKeyLayout(width, height)
        invalidate()
    }

    fun getResolvedSuggestionTextColor(): Int {
        val customText = individualKeyTextColorsMap["Suggestion"]
        if (customText != null) return customText.color

        val sugBgColor = individualKeyColorsMap["Suggestion"]?.color ?: theme.specialKeyColor
        val r = android.graphics.Color.red(sugBgColor)
        val g = android.graphics.Color.green(sugBgColor)
        val b = android.graphics.Color.blue(sugBgColor)
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
        return if (luminance > 0.5) android.graphics.Color.BLACK else android.graphics.Color.WHITE
    }

    private fun drawIconCenter(canvas: Canvas, rect: RectF, drawable: Drawable?, paint: Paint, applyTint: Boolean = true, scaleMultiplier: Float = 1f) {
        if (drawable == null) return

        val intrinsicW = drawable.intrinsicWidth.toFloat()
        val intrinsicH = drawable.intrinsicHeight.toFloat()
        val baseSize = paint.textSize * 0.60f * scaleMultiplier
        var halfW = baseSize.toInt()
        var halfH = baseSize.toInt()

        if (intrinsicW > 0 && intrinsicH > 0) {
            if (intrinsicW > intrinsicH) {
                val ratio = intrinsicH / intrinsicW
                halfW = baseSize.toInt()
                halfH = (baseSize * ratio).toInt()
            } else {
                val ratio = intrinsicW / intrinsicH
                halfH = baseSize.toInt()
                halfW = (baseSize * ratio).toInt()
            }
        }

        val cx = rect.centerX().toInt()
        val cy = rect.centerY().toInt()

        drawable.setBounds(cx - halfW, cy - halfH, cx + halfW, cy + halfH)

        if (applyTint) {
            if (paint.shader != null) {
                val saveCount = canvas.saveLayer(rect, null)
                androidx.core.graphics.drawable.DrawableCompat.setTintList(drawable, null)
                drawable.draw(canvas)

                val overlayPaint = Paint().apply {
                    shader = paint.shader
                    xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                }
                canvas.drawRect(rect, overlayPaint)
                canvas.restoreToCount(saveCount)
                return
            } else {
                androidx.core.graphics.drawable.DrawableCompat.setTint(drawable, paint.color)
            }
        } else {
            androidx.core.graphics.drawable.DrawableCompat.setTintList(drawable, null)
        }

        drawable.draw(canvas)
    }

    private fun getResolvedBottomPadding(): Int {
        if (isEditMode) return 0
        val density = resources.displayMetrics.density
        return (getEffectiveBottomPaddingDp() * density).toInt()
    }

    private fun isDeleteKey(key: Key?): Boolean {
        if (key == null) return false
        return key.code == -2 || (isEmojiMode && key.normalText == ".")
    }

    fun getEmojiPickerBottomMargin(): Int {
        val density = resources.displayMetrics.density

        val baseUsableHeight = getEffectiveKeyboardHeightDp() * density
        var standardTotalGaps = 8f * density
        for (i in 1..4) {
            standardTotalGaps += (if (isEmojiMode) 0f else (theme.individualRowGapsMap[i] ?: theme.globalRowGapDp)) * density
        }
        val universalKeyHeight = (baseUsableHeight - standardTotalGaps) / 5f

        val bottomPadding = getResolvedBottomPadding()
        return (universalKeyHeight * 0.85f + bottomPadding).toInt()
    }

    fun getEmojiPickerHeight(): Int {
        val density = resources.displayMetrics.density

        val baseUsableHeight = getEffectiveKeyboardHeightDp() * density
        var standardTotalGaps = 8f * density
        for (i in 1..4) {
            standardTotalGaps += (if (isEmojiMode) 0f else (theme.individualRowGapsMap[i] ?: theme.globalRowGapDp)) * density
        }
        val universalKeyHeight = (baseUsableHeight - standardTotalGaps) / 5f

        val isStandardMode = currentMode == KeyboardMode.MYANMAR || currentMode == KeyboardMode.ENGLISH
        var currentTotalGaps = 0f
        for (i in 1 until keys.size) {
            val baseGap = if (isStandardMode && !isTopRowRemoved && i == 1) 8f * density else 0f
            val userGap = if (isEmojiMode) 0f else (theme.individualRowGapsMap[i] ?: theme.globalRowGapDp) * density
            currentTotalGaps += (baseGap + userGap)
        }

        val usableHeight = (universalKeyHeight * keys.size) + currentTotalGaps

        return (usableHeight - (universalKeyHeight * 0.85f)).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val baseHeightDp = if (prefs.isSliding) 300f else getEffectiveKeyboardHeightDp()

        var totalHeightPx = 0
        if (keys.isNotEmpty()) {
            val baseUsableHeight = baseHeightDp * density
            var standardTotalGaps = 8f * density
            for (i in 1..4) {
                val userGap = if (isEmojiMode) 0f else (theme.individualRowGapsMap[i] ?: theme.globalRowGapDp)
                standardTotalGaps += userGap * density
            }
            val universalKeyHeight = (baseUsableHeight - standardTotalGaps) / 5f

            val isStandardMode = currentMode == KeyboardMode.MYANMAR || currentMode == KeyboardMode.ENGLISH
            var currentTotalGaps = 0f
            for (i in 1 until keys.size) {
                val baseGap = if (isStandardMode && !isTopRowRemoved && i == 1) 8f * density else 0f
                val userGap = if (isEmojiMode) 0f else (theme.individualRowGapsMap[i] ?: theme.globalRowGapDp) * density
                currentTotalGaps += (baseGap + userGap)
            }

            val finalUsableHeight = (universalKeyHeight * keys.size) + currentTotalGaps

            val bottomPadding = if (isEditMode) 0f else {
                (if (prefs.isSliding) 100f else getEffectiveBottomPaddingDp()) * density
            }

            totalHeightPx = finalUsableHeight.toInt() + bottomPadding.toInt()
        } else {
            val basePadding = if (isEditMode) 0f else theme.bottomPaddingDp
            totalHeightPx = ((baseHeightDp + basePadding) * density).toInt()
        }

        val widthPx = MeasureSpec.getSize(widthMeasureSpec)
        Log.d("NoxDebug", "onMeasure -> Mode: $currentMode, KeysSize: ${keys.size}, HeightPx: $totalHeightPx")

        setMeasuredDimension(widthPx, totalHeightPx)
    }

    private fun calculateKeyLayout(w: Int, h: Int) {
        if (w <= 0 || h <= 0) return

        val density = resources.displayMetrics.density
        val bottomPadding = getResolvedBottomPadding()

        val isStandardMode = currentMode == KeyboardMode.MYANMAR || currentMode == KeyboardMode.ENGLISH

        if (isEmojiMode && keys.isNotEmpty()) {
            val lastRowIndex = keys.size - 1
            val mutableKeys = keys.toMutableList()
            mutableKeys[lastRowIndex] = listOf(
                Key("Globe", "Globe", -3, 1.5f),
                Key("္", "္", 0, 1.5f),
                Key("Space", "Space", 32, 5.5f),
                Key(".", ".", 0, 1.5f)
            )
            keys = mutableKeys
        }

        val baseUsableHeight = getEffectiveKeyboardHeightDp() * density
        var standardTotalGaps = 8f * density
        for (i in 1..4) {
            val userGap = if (isEmojiMode) 0f else (theme.individualRowGapsMap[i] ?: theme.globalRowGapDp)
            standardTotalGaps += userGap * density
        }
        val universalKeyHeight = (baseUsableHeight - standardTotalGaps) / 5f

        val rowGaps = FloatArray(keys.size) { 0f }
        for (i in 1 until keys.size) {
            val baseGap = if (isStandardMode && !isTopRowRemoved && i == 1) 8f else 0f
            val userGap = if (isEmojiMode) 0f else (theme.individualRowGapsMap[i] ?: theme.globalRowGapDp)
            rowGaps[i] = (baseGap + userGap) * density
        }

        val currentTotalGaps = rowGaps.sum()
        val usableHeight = (universalKeyHeight * keys.size) + currentTotalGaps
        val keyHeight = universalKeyHeight

        val offsetY = h - bottomPadding - usableHeight

        Log.d("NoxDebug", "calculateKeyLayout -> ViewHeight(h): $h, UsableHeight: $usableHeight, OffsetY: $offsetY")

        val config = resources.configuration
        val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val isTabletOrFoldable = config.smallestScreenWidthDp >= 600
        val effectiveWidthPercent = if (isLandscape) {
            if (isTabletOrFoldable) minOf(theme.keyboardWidthPercent, 70f)
            else minOf(theme.keyboardWidthPercent, 90f)
        } else {
            if (isTabletOrFoldable) minOf(theme.keyboardWidthPercent, 85f)
            else theme.keyboardWidthPercent
        }

        val layoutWidth = w * (effectiveWidthPercent / 100f)
        val offsetX = when (theme.keyboardAlignment) {
            "LEFT" -> 0f
            "RIGHT" -> w - layoutWidth
            else -> (w - layoutWidth) / 2f
        }

        for ((rowIndex, row) in keys.withIndex()) {
            var cumulativeGap = 0f
            for (i in 1..rowIndex) {
                cumulativeGap += rowGaps[i]
            }

            val isBottomRowAndEmoji = isEmojiMode && rowIndex == keys.size - 1

            val activeKeys = if (isBottomRowAndEmoji) {
                row.filter { it.code != 10 }
            } else {
                row
            }

            val totalWeight = activeKeys.map { it.widthWeight }.sum()

            val rowUnitWidth = if (totalWeight > 10f) {
                layoutWidth / totalWeight
            } else {
                layoutWidth / 10f
            }

            var currentX = offsetX + if (isBottomRowAndEmoji || totalWeight > 10f) {
                0f
            } else {
                (10f - totalWeight) / 2f * rowUnitWidth
            }

            val currentY = offsetY + (rowIndex * keyHeight) + cumulativeGap

            for (key in row) {
                if (isBottomRowAndEmoji && key.code == 10) {
                    key.rect = RectF()
                    continue
                }

                val actualWeight = if (isBottomRowAndEmoji) {
                    when {
                        key.code == -3 -> 1.5f
                        key.normalText == "္" || key.normalText == "#12" -> 1.5f
                        key.code == 32 -> 5.5f
                        key.normalText == "." -> 1.5f
                        else -> key.widthWeight
                    }
                } else {
                    key.widthWeight
                }

                val kw = actualWeight * rowUnitWidth
                val actualKeyHeight = if (isBottomRowAndEmoji) keyHeight * 0.85f else keyHeight
                val topY = currentY + (keyHeight - actualKeyHeight)

                key.rect = RectF(currentX, topY, currentX + kw, topY + actualKeyHeight)
                currentX += kw
            }
        }

        val popupWidth = layoutWidth * 0.88f
        val firstRowTop = if (isStandardMode && !isTopRowRemoved && keys.size > 1) offsetY + keyHeight + rowGaps[1] else offsetY
        val actualHeight = (offsetY + usableHeight) - firstRowTop

        val popupHeight = actualHeight * 0.95f
        val popupLeft = offsetX + (layoutWidth - popupWidth) / 2f
        val popupTop = firstRowTop + (actualHeight - popupHeight) / 2f

        popupRect.set(popupLeft, popupTop, popupLeft + popupWidth, popupTop + popupHeight)

        val numCellWidth = popupWidth / 5f
        val numCellHeight = popupHeight / 4f
        var numIndex = 0

        for (row in 0 until 4) {
            for (col in 0 until 5) {
                val x = popupLeft + col * numCellWidth
                val y = popupTop + row * numCellHeight
                numpadKeys[numIndex++].rect = RectF(x, y, x + numCellWidth, y + numCellHeight)
            }
        }

        try {
            val firstRow = keys.firstOrNull()
            if (isEmojiMode) {
                onSuggestionBoundsChanged?.invoke(null)
            } else if (firstRow != null && firstRow.isNotEmpty() && firstRow.first().code in -102..-100) {
                val padding = theme.keyPaddingDp * resources.displayMetrics.density
                val left = firstRow.first().rect.left + padding
                val right = firstRow.last().rect.right - padding
                val top = firstRow.first().rect.top + padding
                val bottom = firstRow.first().rect.bottom - padding
                onSuggestionBoundsChanged?.invoke(RectF(left, top, right, bottom))
            } else {
                onSuggestionBoundsChanged?.invoke(null)
            }
        } catch (e: Exception) {
            android.util.Log.e("NoxDebug", "Suggestion bounds error: ${e.message}", e)
        }

    }

    private fun parseColorString(hex: String): ParsedColor {
        return try {
            if (hex.startsWith("GRADIENT:")) {
                val arr = hex.removePrefix("GRADIENT:").split(",").map { android.graphics.Color.parseColor(it) }.toIntArray()
                ParsedColor(arr[0], arr)
            } else {
                ParsedColor(android.graphics.Color.parseColor(hex), null)
            }
        } catch (e: Exception) {
            ParsedColor(Color.TRANSPARENT, null)
        }
    }

    fun setIndividualColorsForPreview(
        bgMap: Map<String, String>,
        textMap: Map<String, String>,
        activeMap: Map<String, String>,
        popBgMap: Map<String, String>,
        popTextMap: Map<String, String>
    ) {
        individualKeyColorsMap.clear(); bgMap.forEach { (k, v) -> individualKeyColorsMap[k] = parseColorString(v) }
        individualKeyTextColorsMap.clear(); textMap.forEach { (k, v) -> individualKeyTextColorsMap[k] = parseColorString(v) }
        individualKeyActiveColorsMap.clear(); activeMap.forEach { (k, v) -> individualKeyActiveColorsMap[k] = parseColorString(v) }
        individualKeyPopupBgColorsMap.clear(); popBgMap.forEach { (k, v) -> individualKeyPopupBgColorsMap[k] = parseColorString(v) }
        individualKeyPopupTextColorsMap.clear(); popTextMap.forEach { (k, v) -> individualKeyPopupTextColorsMap[k] = parseColorString(v) }
    }

    internal fun initPaints() {
        bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.backgroundColor
            style = Paint.Style.FILL
        }

        keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.keyColor; style = Paint.Style.FILL }

        keyBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.keyBorderColor
            style = Paint.Style.STROKE
            strokeWidth = 3f * resources.displayMetrics.density
        }
        initNoisePaint()

        specialKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.specialKeyColor; style = Paint.Style.FILL }
        activeKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.activeKeyColor; style = Paint.Style.FILL }

        individualKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.textColor
            textAlign = Paint.Align.CENTER
            textSize = getEffectiveTextSizeSp()
        }

        popupBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.popupBackgroundColor; style = Paint.Style.FILL }
        popupKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.popupKeyColor; style = Paint.Style.FILL }
        popupSpecialKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.popupSpecialKeyColor; style = Paint.Style.FILL }
        popupActiveKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.popupActiveKeyColor; style = Paint.Style.FILL }

        popupTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.popupTextColor
            textAlign = Paint.Align.CENTER
            textSize = getEffectiveTextSizeSp()
        }

        numpadBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.numpadBackgroundColor; style = Paint.Style.FILL }
        numpadKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.numpadKeyColor; style = Paint.Style.FILL }
        numpadTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.numpadTextColor
            textAlign = Paint.Align.CENTER
            textSize = getEffectiveTextSizeSp()
        }

        numpadSpecialKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.numpadSpecialKeyColor; style = Paint.Style.FILL }
        numpadSpecialTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.numpadSpecialTextColor
            textAlign = Paint.Align.CENTER
            textSize = getEffectiveTextSizeSp()
        }

        accentTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = getEffectiveTextSizeSp()
        }

        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#B388FF".toColorInt()
            style = Paint.Style.STROKE
            strokeWidth = 6f
            pathEffect = DashPathEffect(floatArrayOf(15f, 10f), 0f)
        }
    }

    private fun initNoisePaint() {
        if (noisePaint != null) return
        val size = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(size * size)
        val random = java.util.Random()
        for (i in pixels.indices) {
            val alpha = random.nextInt(15) + 5
            val color = if (random.nextBoolean()) Color.argb(alpha, 255, 255, 255) else Color.argb(alpha, 0, 0, 0)
            pixels[i] = color
        }
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        val shader = android.graphics.BitmapShader(bitmap, android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT)
        noisePaint = Paint().apply {
            this.shader = shader
            isAntiAlias = true
        }
    }

    internal fun initIcons() {
        val p = NoxBoardPrefs(context)

        fun getIcon(name: String, defRes: Int): Drawable? {
            if (name == "DEFAULT") return if (defRes != 0) ContextCompat.getDrawable(context, defRes) else null
            val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
            return if (resId != 0) ContextCompat.getDrawable(context, resId) else if (defRes != 0) ContextCompat.getDrawable(context, defRes) else null
        }

        iconEmoji = getIcon(p.iconStyleEmoji, R.drawable.ic_emoji)
        iconCopy = getIcon(p.iconStyleClipboard, R.drawable.ic_copy)
        iconPaste = getIcon(p.iconStyleClipboard.replace("copy", "paste").replace("outline", "board").replace("sharp", "bracket").replace("curve", "pill").replace("block", "board").replace("nodes", "pill"), R.drawable.ic_paste)

        iconEarth = getIcon(p.iconStyleGlobe, R.drawable.ic_earth)
        iconEnter = getIcon(p.iconStyleEnter, R.drawable.ic_enter)
        iconNext = getIcon(p.iconStyleEnter.replace("enter", "go").replace("return", "arrow").replace("pipe", "blades").replace("swoop", "circle").replace("slash", "chevron"), R.drawable.ic_next)
        iconSearch = getIcon(p.iconStyleEnter.replace("enter", "search").replace("return", "glass").replace("pipe", "hex").replace("swoop", "glass").replace("slash", "glass"), R.drawable.ic_search)
        iconSend = getIcon(p.iconStyleEnter.replace("enter", "send").replace("return", "plane").replace("pipe", "dart").replace("swoop", "plane").replace("slash", "beam"), R.drawable.ic_send)

        iconNumpad = getIcon(p.iconStyleNumpad, R.drawable.ic_numpad)
        iconPass = getIcon(p.iconStyleVault, R.drawable.ic_pass)
        iconSetting = getIcon(p.iconStyleSetting, R.drawable.ic_setting)

        iconShiftInactive = getIcon(p.iconStyleShift, R.drawable.ic_shift_inactive)
        iconShiftActive = getIcon(p.iconStyleShift.replace("inactive", "active"), R.drawable.ic_shift_active)
        iconShiftCapslock = getIcon(p.iconStyleShift.replace("inactive", "capslock"), R.drawable.ic_shift_capslock)

        iconSpacebar = getIcon(p.iconStyleSpacebar, 0)

        iconBackspace = getIcon(p.iconStyleBackspace, R.drawable.ic_backspace_classic_erase)
        iconSymbols = getIcon(p.iconStyleSymbols, R.drawable.ic_sym_classic_text)
        iconHideKeyboard = getIcon(p.iconStyleHideKeyboard, R.drawable.ic_hide_keys_classic_down)
    }

    private fun updateLayoutData() {
        val showNumberRow = prefs.isNumberRowEnabled && isInlineNumberRowVisible

        var rawKeys = when (currentMode) {
            KeyboardMode.MYANMAR -> KeyboardLayout.getMyanmarLayout(context, showNumberRow)
            KeyboardMode.ENGLISH -> KeyboardLayout.getEnglishLayout(context, showNumberRow)
            KeyboardMode.SYMBOLS_1 -> KeyboardLayout.getSymbolLayout(context, 1)
            KeyboardMode.SYMBOLS_2 -> KeyboardLayout.getSymbolLayout(context, 2)
            KeyboardMode.NUMBER -> com.noxtan.noxboard.layouts.NumberLayout.getLayout()
        }

        isTopRowRemoved = false
        if (!prefs.isSuggestionEnabled && prefs.isIncognitoModeEnabled) {
            if (rawKeys.isNotEmpty() && rawKeys[0].isNotEmpty() && rawKeys[0].first().code in -102..-100) {
                rawKeys = rawKeys.drop(1)
                isTopRowRemoved = true
            }
        }

        keys = rawKeys
        numpadKeys = KeyboardLayout.getNumpadLayout(isEnglish = currentMode != KeyboardMode.MYANMAR)
    }

    fun setSuggestionsActive(active: Boolean) {
        if (active && isInlineNumberRowVisible && !userForceShowNumberRow) {
            isInlineNumberRowVisible = false
            updateLayoutData()
            requestLayout()
            calculateKeyLayout(width, height)
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateKeyLayout(w, h)
    }

    fun setEnterKeyAction(actionId: Int) {
        enterKeyLabel = when (actionId) {
            EditorInfo.IME_ACTION_SEARCH -> "🔍"
            EditorInfo.IME_ACTION_GO -> "➔"
            EditorInfo.IME_ACTION_SEND -> "➤"
            EditorInfo.IME_ACTION_NEXT -> "⇥"
            else -> "⏎"
        }
        invalidate()
    }

    private fun drawGlobe(canvas: Canvas, rect: RectF, paint: Paint) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val radius = rect.width().coerceAtMost(rect.height()) * 0.28f

        val oldStyle = paint.style
        val oldWidth = paint.strokeWidth

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f

        canvas.drawCircle(cx, cy, radius, paint)
        canvas.drawLine(cx - radius, cy, cx + radius, cy, paint)
        canvas.drawLine(cx, cy - radius, cx, cy + radius, paint)
        val oval = RectF(cx - radius * 0.5f, cy - radius, cx + radius * 0.5f, cy + radius)
        canvas.drawOval(oval, paint)

        paint.style = oldStyle
        paint.strokeWidth = oldWidth
    }

    private fun drawKeypadIcon(canvas: Canvas, rect: RectF, paint: Paint) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val size = rect.width().coerceAtMost(rect.height()) * 0.22f

        val oldStyle = paint.style
        paint.style = Paint.Style.FILL

        val dotRadius = 4.5f
        val spacingX = size * 0.6f
        val spacingY = size * 0.5f

        canvas.drawCircle(cx - spacingX, cy - spacingY, dotRadius, paint)
        canvas.drawCircle(cx, cy - spacingY, dotRadius, paint)
        canvas.drawCircle(cx + spacingX, cy - spacingY, dotRadius, paint)

        canvas.drawCircle(cx - spacingX, cy, dotRadius, paint)
        canvas.drawCircle(cx, cy, dotRadius, paint)
        canvas.drawCircle(cx + spacingX, cy, dotRadius, paint)

        canvas.drawCircle(cx - spacingX, cy + spacingY, dotRadius, paint)
        canvas.drawCircle(cx, cy + spacingY, dotRadius, paint)
        canvas.drawCircle(cx + spacingX, cy + spacingY, dotRadius, paint)

        paint.style = oldStyle
    }

    private fun drawShiftIcon(canvas: Canvas, rect: RectF, paint: Paint, isShifted: Boolean, isCapsLock: Boolean) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val w = rect.width() * 0.16f
        val h = rect.height() * 0.28f

        val oldStyle = paint.style
        val oldWidth = paint.strokeWidth

        val path = Path()
        if (isShifted || isCapsLock) {
            paint.style = Paint.Style.FILL
            path.moveTo(cx, cy - h * 0.7f)
            path.lineTo(cx - w, cy)
            path.lineTo(cx - w * 0.45f, cy)
            path.lineTo(cx - w * 0.45f, cy + h * 0.55f)
            path.lineTo(cx + w * 0.45f, cy + h * 0.55f)
            path.lineTo(cx + w, cy)
            path.close()
            canvas.drawPath(path, paint)

            if (isCapsLock) {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3.8f
                val barY = cy + h * 0.75f
                canvas.drawLine(cx - w, barY, cx + w, barY, paint)
            }
        } else {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3.5f
            path.moveTo(cx, cy - h * 0.7f)
            path.lineTo(cx - w, cy)
            path.lineTo(cx - w * 0.45f, cy)
            path.lineTo(cx - w * 0.45f, cy + h * 0.55f)
            path.lineTo(cx + w * 0.45f, cy + h * 0.55f)
            path.lineTo(cx + w, cy)
            path.close()
            canvas.drawPath(path, paint)
        }

        paint.style = oldStyle
        paint.strokeWidth = oldWidth
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val density = resources.displayMetrics.density
        val bottomPadding = getResolvedBottomPadding()
        val isStandardMode = currentMode == KeyboardMode.MYANMAR || currentMode == KeyboardMode.ENGLISH

        val baseUsableHeight = getEffectiveKeyboardHeightDp() * density
        var standardTotalGaps = 8f * density
        for (i in 1..4) {
            standardTotalGaps += (theme.individualRowGapsMap[i] ?: theme.globalRowGapDp) * density
        }
        val universalKeyHeight = (baseUsableHeight - standardTotalGaps) / 5f

        var currentTotalGaps = 0f
        for (i in 1 until keys.size) {
            val baseGap = if (isStandardMode && !isTopRowRemoved && i == 1) 8f else 0f
            val userGap = theme.individualRowGapsMap[i] ?: theme.globalRowGapDp
            currentTotalGaps += (baseGap + userGap) * density
        }

        val usableHeight = (universalKeyHeight * keys.size) + currentTotalGaps
        val offsetY = height - bottomPadding - usableHeight

        if (isEmojiMode) {
            android.util.Log.d("NoxEmojiDebug", "--- KeyboardView onDraw (Emoji Mode) ---")
            android.util.Log.d("NoxEmojiDebug", "View Height: $height")
            android.util.Log.d("NoxEmojiDebug", "Usable Height: $usableHeight")
            android.util.Log.d("NoxEmojiDebug", "Bottom Padding: $bottomPadding")
            android.util.Log.d("NoxEmojiDebug", "Background starts at offsetY: $offsetY")
        }

        val bitmap = wallpaperBitmap
        if (bitmap != null) {
            canvas.withClip(0f, offsetY, width.toFloat(), height.toFloat()) {
                val scale = prefs.customWallpaperScale
                val offsetX = prefs.customWallpaperOffsetX * density
                val offsetYVal = prefs.customWallpaperOffsetY * density

                val viewWidth = width.toFloat()

                val bitmapWidth = bitmap.width.toFloat()
                val bitmapHeight = bitmap.height.toFloat()

                val scaleX = viewWidth / bitmapWidth
                val scaleY = usableHeight / bitmapHeight
                val baseScale = scaleX.coerceAtLeast(scaleY)

                val finalScale = baseScale * scale

                val centerX = viewWidth / 2f
                val centerY = offsetY + usableHeight / 2f

                translate(centerX + offsetX, centerY + offsetYVal)
                scale(finalScale, finalScale)
                drawBitmap(bitmap, -bitmapWidth / 2f, -bitmapHeight / 2f, null)

            }
        } else {
            if (theme.backgroundGradient != null) {
                bgPaint.shader = android.graphics.LinearGradient(
                    0f, offsetY, width.toFloat(), height.toFloat(),
                    theme.backgroundGradient!!, null, android.graphics.Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, offsetY, width.toFloat(), height.toFloat(), bgPaint)
                bgPaint.shader = null
            } else {
                canvas.drawRect(0f, offsetY, width.toFloat(), height.toFloat(), bgPaint)
            }
        }

        if (theme.isTextureEffectEnabled) {
            noisePaint?.let { canvas.drawRect(0f, offsetY, width.toFloat(), height.toFloat(), it) }
        }

        val padding = if (isEmojiMode) 1f * density else theme.keyPaddingDp * density
        val cornerRadius = if (isEmojiMode) 10f * density else theme.keyCornerRadiusDp * density

        if (isEditMode) {
            val firstRow = keys.firstOrNull()
            if (firstRow != null && firstRow.isNotEmpty() && firstRow.first().code in -102..-100) {
                val sLeft = firstRow.first().rect.left + padding
                val sTop = firstRow.first().rect.top + padding
                val sRight = firstRow.last().rect.right - padding
                val sBottom = firstRow.first().rect.bottom - padding
                val sRect = RectF(sLeft, sTop, sRight, sBottom)

                val customSugColor = individualKeyColorsMap["Suggestion"]
                individualKeyPaint.color = customSugColor?.color ?: theme.specialKeyColor
                val gradToUse = customSugColor?.gradient ?: theme.specialKeyGradient

                if (gradToUse != null) {
                    individualKeyPaint.shader = android.graphics.LinearGradient(
                        0f, 0f, width.toFloat(), height.toFloat(),
                        gradToUse, null, android.graphics.Shader.TileMode.CLAMP
                    )
                } else {
                    individualKeyPaint.shader = null
                }
                canvas.drawRoundRect(sRect, cornerRadius, cornerRadius, individualKeyPaint)

                if (theme.keyBorderColor != Color.TRANSPARENT) {
                    canvas.drawRoundRect(sRect, cornerRadius, cornerRadius, keyBorderPaint)
                }
            }
        }

        for ((rowIndex, row) in keys.withIndex()) {
            if (isEmojiMode && rowIndex < keys.size - 1) continue

            for ((colIndex, key) in row.withIndex()) {
                if (key.rect.isEmpty) continue

                val isSuggestionPlaceholder = key.code in -102..-100

                val cellW = key.rect.width()
                val cellH = key.rect.height()

                val drawnW = cellW * key.scaleX
                val drawnH = cellH * key.scaleY

                val emptyW = cellW - drawnW
                val emptyH = cellH - drawnH

                val startX = key.rect.left + (emptyW * key.alignX)
                val startY = key.rect.top + (emptyH * key.alignY)

                tempRect.set(
                    startX + padding,
                    startY + padding,
                    startX + drawnW - padding,
                    startY + drawnH - padding
                )

                val keyId = when {
                    (isStandardMode && !isTopRowRemoved && rowIndex == 0) -> "Suggestion"
                    key.code in -102..-100 -> "Suggestion"
                    key.code == 32 -> "Space"
                    key.code == 10 -> "Enter"
                    key.code == -2 || (isEmojiMode && key.normalText == ".") -> "Delete"
                    key.code == -1 -> "Shift"
                    key.code == -3 -> "Globe"
                    key.code == -4 || key.normalText == "ABC" || key.normalText == "#12" -> "Symbol"
                    key.code == -11 -> "Emoji"
                    key.code == -12 -> "Vault"
                    key.code == -13 -> "Clipboard"
                    key.code == -14 -> "Settings"
                    key.code == -8 -> "Numpad"
                    else -> key.normalText
                }

                val customPopupBg = individualKeyPopupBgColorsMap[keyId]
                val customPopupText = individualKeyPopupTextColorsMap[keyId]

                val isSpecialKey = when {
                    key.code == 32 -> false
                    key.code != 0 -> true
                    isEmojiMode && (key.normalText == "္" || key.normalText == "#12" || key.normalText == ".") -> true
                    isStandardMode && rowIndex == 0 && key.normalText in listOf("1","2","3","4","5","6","7","8","9","0","၁","၂","၃","၄","၅","၆","၇","၈","၉","၀") -> true
                    else -> false
                }

                val customColor = individualKeyColorsMap[keyId]

                val currentPos = Pair(rowIndex, colIndex)
                val isEditSelected = isEditMode && (editSelectedKeyPos == currentPos || editSelectedKeyPositions.contains(currentPos))
                val isSwapSelected = isEditMode && (editSwapKey1Pos == currentPos || editSwapKey2Pos == currentPos)

                if (key == activeKey && !isSuggestionPlaceholder) {
                    val customActiveColor = individualKeyActiveColorsMap[keyId]
                    if (customActiveColor != null) {
                        val oldColor = activeKeyPaint.color
                        val oldShader = activeKeyPaint.shader
                        activeKeyPaint.color = customActiveColor.color
                        if (customActiveColor.gradient != null) {
                            activeKeyPaint.shader = android.graphics.LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), customActiveColor.gradient, null, android.graphics.Shader.TileMode.CLAMP)
                        } else {
                            activeKeyPaint.shader = null
                        }
                        canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, activeKeyPaint)
                        activeKeyPaint.color = oldColor
                        activeKeyPaint.shader = oldShader
                    } else {
                        if (theme.activeKeyGradient != null) {
                            activeKeyPaint.shader = android.graphics.LinearGradient(
                                0f, 0f, width.toFloat(), height.toFloat(),
                                theme.activeKeyGradient!!, null, android.graphics.Shader.TileMode.CLAMP
                            )
                        } else {
                            activeKeyPaint.shader = null
                        }
                        canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, activeKeyPaint)
                    }
                } else if (isSuggestionPlaceholder) {
                    val customSugColor = individualKeyColorsMap["Suggestion"]
                    individualKeyPaint.color = customSugColor?.color ?: theme.specialKeyColor
                    val gradToUse = customSugColor?.gradient ?: theme.specialKeyGradient

                    if (gradToUse != null) {
                        individualKeyPaint.shader = android.graphics.LinearGradient(
                            0f, 0f, width.toFloat(), height.toFloat(),
                            gradToUse, null, android.graphics.Shader.TileMode.CLAMP
                        )
                    } else {
                        individualKeyPaint.shader = null
                    }
                    canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, individualKeyPaint)
                } else if (customColor != null) {
                    individualKeyPaint.color = customColor.color
                    if (customColor.gradient != null) {
                        individualKeyPaint.shader = android.graphics.LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), customColor.gradient, null, android.graphics.Shader.TileMode.CLAMP)
                    } else {
                        individualKeyPaint.shader = null
                    }
                    canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, individualKeyPaint)
                } else {
                    val paintToUse = if (isSpecialKey) specialKeyPaint else keyPaint
                    val gradientToUse = if (isSpecialKey) theme.specialKeyGradient else theme.keyGradient
                    if (gradientToUse != null && !isSuggestionPlaceholder) {
                        paintToUse.shader = android.graphics.LinearGradient(
                            0f, 0f, width.toFloat(), height.toFloat(),
                            gradientToUse, null, android.graphics.Shader.TileMode.CLAMP
                        )
                    } else {
                        paintToUse.shader = null
                    }
                    canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, paintToUse)
                }


                if (theme.keyBorderColor != Color.TRANSPARENT && !isSuggestionPlaceholder) {
                    canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, keyBorderPaint)
                }

                if (theme.isTextureEffectEnabled && !isSuggestionPlaceholder) {
                    noisePaint?.let { canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, it) }
                }

                if (isSwapSelected) {
                    val oldStyle = individualKeyPaint.style

                    individualKeyPaint.style = Paint.Style.FILL
                    individualKeyPaint.color = android.graphics.Color.parseColor("#4400FF00")
                    individualKeyPaint.shader = null
                    canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, individualKeyPaint)

                    val inset = 1.5f * density
                    val innerRect = RectF(
                        tempRect.left + inset,
                        tempRect.top + inset,
                        tempRect.right - inset,
                        tempRect.bottom - inset
                    )

                    individualKeyPaint.style = Paint.Style.STROKE
                    individualKeyPaint.strokeWidth = 4f * density
                    individualKeyPaint.color = android.graphics.Color.parseColor("#00C853")
                    canvas.drawRoundRect(innerRect, cornerRadius, cornerRadius, individualKeyPaint)

                    individualKeyPaint.strokeWidth = 1.5f * density
                    individualKeyPaint.color = android.graphics.Color.WHITE
                    canvas.drawRoundRect(innerRect, cornerRadius, cornerRadius, individualKeyPaint)

                    individualKeyPaint.style = oldStyle
                } else if (isEditSelected) {
                    val oldStyle = individualKeyPaint.style
                    val currentBg = customColor?.color ?: if (isSpecialKey) theme.specialKeyColor else theme.keyColor

                    val darkness = 1 - (0.299 * android.graphics.Color.red(currentBg) + 0.587 * android.graphics.Color.green(currentBg) + 0.114 * android.graphics.Color.blue(currentBg)) / 255

                    individualKeyPaint.style = Paint.Style.FILL
                    individualKeyPaint.color = if (darkness < 0.5) android.graphics.Color.parseColor("#66000000") else android.graphics.Color.parseColor("#66FFFFFF")
                    individualKeyPaint.shader = null
                    canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, individualKeyPaint)

                    val inset = 1.5f * density
                    val innerRect = RectF(
                        tempRect.left + inset,
                        tempRect.top + inset,
                        tempRect.right - inset,
                        tempRect.bottom - inset
                    )

                    individualKeyPaint.style = Paint.Style.STROKE

                    individualKeyPaint.strokeWidth = 4f * density
                    individualKeyPaint.color = if (darkness < 0.5) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    canvas.drawRoundRect(innerRect, cornerRadius, cornerRadius, individualKeyPaint)

                    individualKeyPaint.strokeWidth = 1.5f * density
                    individualKeyPaint.color = if (darkness < 0.5) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                    canvas.drawRoundRect(innerRect, cornerRadius, cornerRadius, individualKeyPaint)

                    individualKeyPaint.style = oldStyle
                }

                val isEmojiAbc = isEmojiMode && key.code == -3
                val isEmojiKaKha = isEmojiMode && (key.code == -4 || key.normalText == "္" || key.normalText == "#12" || key.normalText == "ABC" || key.normalText == "?123")
                val isEmojiDelete = isEmojiMode && key.normalText == "."

                val textToDraw = when {
                    key.code == 10 -> enterKeyLabel
                    key.code == -2 -> "⌫"
                    isEmojiAbc -> "ABC"
                    isEmojiKaKha -> "ကခဂ"
                    isEmojiDelete -> "⌫"
                    isShifted -> key.shiftText
                    else -> key.normalText
                }

                val x = tempRect.centerX()
                val y = tempRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2

                val originalTextSize = textPaint.textSize
                val originalBold = textPaint.isFakeBoldText

                val originalTextColor = textPaint.color
                val originalShader = textPaint.shader
                val customTextColorVal = individualKeyTextColorsMap[keyId]

                if (key == activeKey && !isSuggestionPlaceholder) {
                    val activeBg = individualKeyActiveColorsMap[keyId]?.color ?: theme.activeKeyColor
                    val darkness = 1 - (0.299 * android.graphics.Color.red(activeBg) + 0.587 * android.graphics.Color.green(activeBg) + 0.114 * android.graphics.Color.blue(activeBg)) / 255
                    textPaint.color = if (darkness < 0.5) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    textPaint.shader = null
                } else {
                    if (customTextColorVal != null) {
                        textPaint.color = customTextColorVal.color
                        if (customTextColorVal.gradient != null) {
                            val reversedGrad = customTextColorVal.gradient.reversedArray()
                            textPaint.shader = android.graphics.LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), reversedGrad, null, android.graphics.Shader.TileMode.CLAMP)
                        } else {
                            textPaint.shader = null
                        }
                    } else if (isSpecialKey || isSuggestionPlaceholder) {
                        textPaint.color = theme.specialTextColor
                        if (theme.specialTextGradient != null) {
                            val reversedGrad = theme.specialTextGradient!!.reversedArray()
                            textPaint.shader = android.graphics.LinearGradient(
                                0f, 0f, width.toFloat(), height.toFloat(),
                                reversedGrad, null, android.graphics.Shader.TileMode.CLAMP
                            )
                        } else {
                            textPaint.shader = null
                        }
                    } else {
                        textPaint.color = theme.textColor
                        if (theme.textGradient != null) {
                            val reversedGrad = theme.textGradient!!.reversedArray()
                            textPaint.shader = android.graphics.LinearGradient(
                                0f, 0f, width.toFloat(), height.toFloat(),
                                reversedGrad, null, android.graphics.Shader.TileMode.CLAMP
                            )
                        } else {
                            textPaint.shader = null
                        }
                    }
                }

                val isBottomEmojiKey = isEmojiMode && rowIndex == keys.size - 1
                val effectiveTextSize = getEffectiveTextSizeSp() * (if (isBottomEmojiKey) 0.85f else 1f)

                if (key.code == 10) {
                    textPaint.textSize = effectiveTextSize * 1.1f
                    textPaint.isFakeBoldText = true
                } else if (key.code == -4 || key.normalText == "ABC" || key.normalText == "#12" || isEmojiAbc || isEmojiKaKha) {
                    if (key.code == -4 && iconSymbols != null && !isEmojiMode) {
                        textPaint.textSize = effectiveTextSize
                    } else {
                        textPaint.textSize = effectiveTextSize * 0.85f
                        textPaint.isFakeBoldText = true
                    }
                } else if (isEmojiDelete) {
                    textPaint.textSize = effectiveTextSize * 0.8f
                }

                when (key.code) {
                    32 -> {
                        val spcIcon = getCustomIcon(key.customIcon) ?: iconSpacebar
                        if (spcIcon != null && !isSuggestionPlaceholder) {
                            drawIconCenter(canvas, tempRect, spcIcon, textPaint, applyTint = useCustomIconColors)
                        } else {
                            canvas.drawText(textToDraw, x, y, textPaint)
                        }

                        if (hasSelection) {
                            val density = resources.displayMetrics.density
                            val xMargin = 18f * density
                            val xRadius = 12f * density
                            val cx = tempRect.right - xMargin
                            val cy = tempRect.centerY()

                            val oldStyle = individualKeyPaint.style
                            val oldColor = individualKeyPaint.color
                            individualKeyPaint.style = Paint.Style.FILL

                            val isTextDark = (android.graphics.Color.red(textPaint.color) < 128)
                            individualKeyPaint.color = if (isTextDark) android.graphics.Color.parseColor("#22000000") else android.graphics.Color.parseColor("#33FFFFFF")

                            canvas.drawCircle(cx, cy, xRadius, individualKeyPaint)
                            individualKeyPaint.color = oldColor
                            individualKeyPaint.style = oldStyle

                            val oldTextSize = textPaint.textSize
                            val oldFakeBold = textPaint.isFakeBoldText
                            textPaint.textSize = 12f * density
                            textPaint.isFakeBoldText = true
                            canvas.drawText("✕", cx, cy - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
                            textPaint.textSize = oldTextSize
                            textPaint.isFakeBoldText = oldFakeBold
                        }
                    }
                    -11 -> {
                        val emjIcon = getCustomIcon(key.customIcon) ?: iconEmoji
                        drawIconCenter(canvas, tempRect, emjIcon, textPaint, applyTint = useCustomIconColors)
                    }
                    -3 if !isEmojiMode -> {
                        val glbIcon = getCustomIcon(key.customIcon) ?: iconEarth
                        drawIconCenter(canvas, tempRect, glbIcon, textPaint)
                    }
                    -8 -> {
                        val numIcon = getCustomIcon(key.customIcon) ?: iconNumpad
                        drawIconCenter(canvas, tempRect, numIcon, textPaint)
                    }
                    -1 -> {
                        val shiftDrawable = if (key.customIcon != null) {
                            val base = key.customIcon!!
                            val act = base.replace("inactive", "active")
                            val caps = base.replace("inactive", "capslock")
                            when {
                                isCapsLock -> getCustomIcon(caps) ?: iconShiftCapslock
                                isShifted -> getCustomIcon(act) ?: iconShiftActive
                                else -> getCustomIcon(base) ?: iconShiftInactive
                            }
                        } else {
                            when {
                                isCapsLock -> iconShiftCapslock
                                isShifted -> iconShiftActive
                                else -> iconShiftInactive
                            }
                        }
                        drawIconCenter(canvas, tempRect, shiftDrawable, textPaint)
                    }
                    -12 -> {
                        val vltIcon = getCustomIcon(key.customIcon) ?: iconPass
                        drawIconCenter(canvas, tempRect, vltIcon, textPaint, applyTint = useCustomIconColors)
                    }
                    -13 -> {
                        val clipIcon = if (key.customIcon != null) {
                            val base = key.customIcon!!
                            val pst = base.replace("copy", "paste").replace("outline", "board").replace("sharp", "bracket").replace("curve", "pill").replace("block", "board").replace("nodes", "pill")
                            if (hasSelection) getCustomIcon(base) ?: iconCopy else getCustomIcon(pst) ?: iconPaste
                        } else {
                            if (hasSelection) iconCopy else iconPaste
                        }
                        drawIconCenter(canvas, tempRect, clipIcon, textPaint)
                    }
                    -14 -> {
                        val setIcon = getCustomIcon(key.customIcon) ?: iconSetting
                        drawIconCenter(canvas, tempRect, setIcon, textPaint)
                    }
                    -2 -> {
                        val delIcon = getCustomIcon(key.customIcon) ?: iconBackspace
                        if (delIcon != null) drawIconCenter(canvas, tempRect, delIcon, textPaint)
                        else canvas.drawText(textToDraw, x, y, textPaint)
                    }
                    -4 if !isEmojiMode -> {
                        val cIcon = getCustomIcon(key.customIcon)
                        if (cIcon != null) {
                            drawIconCenter(canvas, tempRect, cIcon, textPaint, scaleMultiplier = 1.5f)
                        } else {
                            if (currentMode == KeyboardMode.SYMBOLS_1 || currentMode == KeyboardMode.SYMBOLS_2) {
                                canvas.drawText(textToDraw, x, y, textPaint)
                            } else {
                                if (iconSymbols != null) drawIconCenter(canvas, tempRect, iconSymbols, textPaint, scaleMultiplier = 1.5f)
                                else canvas.drawText(textToDraw, x, y, textPaint)
                            }
                        }
                    }
                    -5 -> {
                        val cIcon = getCustomIcon(key.customIcon) ?: iconSymbols
                        if (cIcon != null) {
                            drawIconCenter(canvas, tempRect, cIcon, textPaint, scaleMultiplier = 1.5f)
                        } else {
                            canvas.drawText(textToDraw, x, y, textPaint)
                        }
                    }
                    -16 -> {
                        val hkIcon = getCustomIcon(key.customIcon) ?: iconHideKeyboard
                        if (hkIcon != null) drawIconCenter(canvas, tempRect, hkIcon, textPaint)
                        else canvas.drawText(textToDraw, x, y, textPaint)
                    }
                    0 if isEmojiDelete -> {
                        val delIcon = getCustomIcon(key.customIcon) ?: iconBackspace
                        if (delIcon != null) drawIconCenter(canvas, tempRect, delIcon, textPaint)
                        else canvas.drawText(textToDraw, x, y, textPaint)
                    }
                    10 -> {
                        val entIcon = getCustomIcon(key.customIcon) ?: when (enterKeyLabel) {
                            "🔍" -> iconSearch
                            "➔", "⇥" -> iconNext
                            "➤" -> iconSend
                            else -> iconEnter
                        }
                        drawIconCenter(canvas, tempRect, entIcon, textPaint)
                    }
                    else -> {
                        canvas.drawText(textToDraw, x, y, textPaint)
                    }
                }

                textPaint.textSize = originalTextSize
                textPaint.isFakeBoldText = originalBold
                textPaint.color = originalTextColor
                textPaint.shader = originalShader
            }
        }

        if (isNumpadMode) {
            canvas.drawColor(dimColor)
            if (theme.numpadBgGradient != null) {
                numpadBgPaint.shader = android.graphics.LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    theme.numpadBgGradient!!, null, android.graphics.Shader.TileMode.CLAMP
                )
            } else {
                numpadBgPaint.shader = null
            }
            canvas.drawRoundRect(popupRect, 25f, 25f, numpadBgPaint)

            if (theme.keyBorderColor != Color.TRANSPARENT) {
                canvas.drawRoundRect(popupRect, 25f, 25f, keyBorderPaint)
            }

            val numPadding = 8f * density
            val numCorner = 12f * density

            for (key in numpadKeys) {
                tempRect.set(
                    key.rect.left + numPadding,
                    key.rect.top + numPadding,
                    key.rect.right - numPadding,
                    key.rect.bottom - numPadding
                )

                val isSpecial = key.code != 0 || key.normalText.length > 1
                val paintToUse = if (key == activeKey) popupActiveKeyPaint else if (isSpecial) numpadSpecialKeyPaint else numpadKeyPaint

                if (paintToUse == numpadKeyPaint && theme.numpadKeyGradient != null) {
                    paintToUse.shader = android.graphics.LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), theme.numpadKeyGradient!!, null, android.graphics.Shader.TileMode.CLAMP)
                } else if (paintToUse == numpadSpecialKeyPaint && theme.numpadSpecialKeyGradient != null) {
                    paintToUse.shader = android.graphics.LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), theme.numpadSpecialKeyGradient!!, null, android.graphics.Shader.TileMode.CLAMP)
                } else if (paintToUse == numpadKeyPaint || paintToUse == numpadSpecialKeyPaint) {
                    paintToUse.shader = null
                }

                canvas.drawRoundRect(tempRect, numCorner, numCorner, paintToUse)

                if (theme.keyBorderColor != Color.TRANSPARENT) {
                    canvas.drawRoundRect(tempRect, numCorner, numCorner, keyBorderPaint)
                }

                val textPaintToUse = if (isSpecial) numpadSpecialTextPaint else numpadTextPaint
                val textGradientToUse = if (isSpecial) theme.numpadSpecialTextGradient else theme.numpadTextGradient

                val x = tempRect.centerX()
                val y = tempRect.centerY() - (textPaintToUse.descent() + textPaintToUse.ascent()) / 2

                if (textGradientToUse != null) {
                    val reversedGrad = textGradientToUse.reversedArray()
                    textPaintToUse.shader = android.graphics.LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), reversedGrad, null, android.graphics.Shader.TileMode.CLAMP)
                } else {
                    textPaintToUse.shader = null
                }
                canvas.drawText(key.normalText, x, y, textPaintToUse)
            }
        }

        if (isAccentMode) {
            canvas.drawColor(accentDimColor)
            if (theme.popupBgGradient != null) {
                popupBgPaint.shader = android.graphics.LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    theme.popupBgGradient!!, null, android.graphics.Shader.TileMode.CLAMP
                )
            } else {
                popupBgPaint.shader = null
            }
            canvas.drawRoundRect(accentPopupRect, 25f, 25f, popupBgPaint)

            val cellPadding = 4f * density
            val cellCorner = 8f * density

            for (key in accentKeys) {
                tempRect.set(
                    key.rect.left + cellPadding,
                    key.rect.top + cellPadding,
                    key.rect.right - cellPadding,
                    key.rect.bottom - cellPadding
                )

                val paintToUse = if (key == activeAccentKey) activeKeyPaint else keyPaint
                canvas.drawRoundRect(tempRect, cellCorner, cellCorner, paintToUse)

                val x = tempRect.centerX()
                val y = tempRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2

                accentTextPaint.color = theme.popupTextColor
                canvas.drawText(key.normalText, x, y, accentTextPaint)
            }
        }
    }

    private fun showPreview(key: Key?) {
        if (!prefs.isKeyPreviewEnabled || key == null || key.code != 0 || isAccentMode || isNumpadMode || isSpaceDragging || isEmojiMode) {
            hidePreview()
            return
        }

        val text = if (isShifted) key.shiftText else key.normalText
        if (text.isEmpty()) {
            hidePreview()
            return
        }

        previewDismissRunnable?.let { previewHandler.removeCallbacks(it) }
        previewDismissRunnable = null

        val density = resources.displayMetrics.density
        val offsetDp = 110f * density

        val keyId = when {
            key.code in -102..-100 -> "Suggestion"
            key.code == 32 -> "Space"
            key.code == 10 -> "Enter"
            key.code == -2 || (isEmojiMode && key.normalText == ".") -> "Delete"
            key.code == -1 -> "Shift"
            key.code == -3 -> "Globe"
            key.code == -4 || key.normalText == "ABC" || key.normalText == "#12" -> "Symbol"
            else -> key.normalText
        }

        val customPopupBg = individualKeyPopupBgColorsMap[keyId]
        val customPopupText = individualKeyPopupTextColorsMap[keyId]

        previewOverlay?.apply {
            this.activeKeyRect = key.rect
            this.activeKeyText = text
            this.activeKeyColor = customPopupBg?.color ?: theme.popupBackgroundColor
            this.bgGradient = customPopupBg?.gradient ?: theme.popupBgGradient
            this.textColor = customPopupText?.color ?: theme.popupTextColor
            this.textGradient = customPopupText?.gradient ?: theme.popupTextGradient
            this.textSize = getEffectiveTextSizeSp()
            invalidate()
        }

        if (previewPopup?.isShowing == false) {
            val loc = IntArray(2)
            getLocationInWindow(loc)

            previewPopup?.width = width
            previewPopup?.height = height + offsetDp.toInt()
            previewPopup?.showAtLocation(this, android.view.Gravity.NO_GRAVITY, loc[0], loc[1] - offsetDp.toInt())
        }
    }

    private fun hidePreview() {
        previewDismissRunnable?.let { previewHandler.removeCallbacks(it) }

        val runnable = Runnable {
            previewOverlay?.apply {
                this.activeKeyRect = null
                this.activeKeyText = ""
                invalidate()
            }
            if (previewPopup?.isShowing == true) {
                try {
                    previewPopup?.dismiss()
                } catch (e: Exception) {}
            }
        }

        previewDismissRunnable = runnable
        previewHandler.postDelayed(runnable, 70L)
    }

    interface OnKeyboardActionListener {
        fun onKey(code: Int, text: String): Boolean
    }

    var listener: OnKeyboardActionListener? = null

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val pressedKey = findKeyAt(x, y)
                if (pressedKey != null) {
                    activeKey = pressedKey
                    invalidate()

                    showPreview(pressedKey)

                    if (isEditMode) return true

                    showPreview(pressedKey)

                    if (pressedKey.code != -9) {
                        triggerHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                        if (prefs.isSoundEnabled) {
                            (context as? NoxKeyboardService)?.playKeypressSound() ?: run {
                                val am = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
                                am.playSoundEffect(android.media.AudioManager.FX_KEYPRESS_STANDARD)
                            }
                        }
                    }

                    if (pressedKey.code == 32) {
                        if (hasSelection) {
                            val density = resources.displayMetrics.density
                            val rightHitTarget = pressedKey.rect.right - (45f * density)
                            if (x > rightHitTarget) {
                                listener?.onKey(-35, "CANCEL_SELECT")
                                triggerHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                return true
                            }
                        }

                        isSpaceDown = true
                        isSpaceDragging = false
                        isSpaceCursorActivated = false
                        isSpaceCursorCanceled = false
                        spaceDragStartX = x
                        spaceDragStartY = y
                        spaceDownTimeForDrag = System.currentTimeMillis()

                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastSpaceDownTime < SPACE_DOUBLE_TAP_TIMEOUT) {
                            isSpaceSelecting = true
                            isSpaceDoubleTapped = true
                            listener?.onKey(-98, "UNDO_SAVE")
                            lastSpaceDownTime = 0L
                        } else {
                            isSpaceSelecting = hasSelection
                            isSpaceDoubleTapped = false
                            lastSpaceDownTime = currentTime
                        }
                    } else {
                        isSpaceDown = false
                    }

                    if (isDeleteKey(pressedKey)) {
                        handleKeyPress(pressedKey)
                        startRepeatDelete()
                    } else if (pressedKey.code == 0) {
                        startLongPressDetector(pressedKey)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isSpaceDown && isSpaceCursorControlEnabled && !isSpaceCursorCanceled) {
                    val currentTime = System.currentTimeMillis()
                    val dx = x - spaceDragStartX
                    val dy = y - spaceDragStartY

                    if (isSpaceCursorActivated) {
                        if (abs(dx) > spaceDragThreshold) {
                            isSpaceDragging = true
                            val movementCode = if (isSpaceSelecting) {
                                if (dx > 0) -32 else -31
                            } else {
                                if (dx > 0) -22 else -21
                            }
                            val success = listener?.onKey(movementCode, "") ?: false
                            if (success) triggerHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                            spaceDragStartX = x
                            spaceDragStartY = y
                        } else if (abs(dy) > spaceDragThreshold) {
                            isSpaceDragging = true
                            val movementCode = if (isSpaceSelecting) {
                                if (dy > 0) -34 else -33
                            } else {
                                if (dy > 0) -24 else -23
                            }
                            val success = listener?.onKey(movementCode, "") ?: false
                            if (success) triggerHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                            spaceDragStartX = x
                            spaceDragStartY = y
                        } else if (abs(dy) > spaceDragThreshold) {
                            isSpaceDragging = true
                            val movementCode = if (isSpaceSelecting) {
                                if (dy > 0) -34 else -33
                            } else {
                                if (dy > 0) -24 else -23
                            }
                            listener?.onKey(movementCode, "")
                            triggerHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            spaceDragStartX = x
                            spaceDragStartY = y
                        }
                    } else {
                        if (currentTime - spaceDownTimeForDrag >= spaceDragDelayMs) {
                            isSpaceCursorActivated = true
                            spaceDragStartX = x
                            spaceDragStartY = y
                            triggerHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        } else {
                            if (abs(dx) > spaceDragThreshold || abs(dy) > spaceDragThreshold) {
                                isSpaceCursorCanceled = true
                            }
                        }
                    }
                } else {
                    if (isAccentMode) {
                        val currentAccent = findAccentKeyAt(x)
                        if (currentAccent != activeAccentKey) {
                            activeAccentKey = currentAccent
                            invalidate()
                        }
                    } else {
                        val currentKey = findKeyAt(x, y)
                        if (currentKey != activeKey) {
                            cancelLongPressDetector()
                            if (isDeleteKey(activeKey) && !isDeleteKey(currentKey)) {
                                stopRepeatDelete()
                            }
                            activeKey = currentKey
                            invalidate()
                            showPreview(currentKey)
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                performClick()

                if (isEditMode) {
                    hidePreview()

                    val finalKey = activeKey
                    val releasedKey = findKeyAt(x, y)
                    if (finalKey != null && finalKey == releasedKey) {
                        for ((rIndex, row) in keys.withIndex()) {
                            val kIndex = row.indexOf(finalKey)
                            if (kIndex != -1) {
                                onEditKeyClick?.invoke(rIndex, kIndex)
                                break
                            }
                        }
                    }
                    activeKey = null
                    invalidate()
                    return true
                }

                cancelLongPressDetector()
                hidePreview()

                if (isAccentMode) {
                    val finalAccent = activeAccentKey
                    if (finalAccent != null) handleKeyPress(finalAccent)
                    isAccentMode = false
                    activeAccentKey = null
                    accentKeys.clear()
                } else {
                    val finalKey = activeKey
                    if (finalKey != null) {
                        if (finalKey.code == 32) {
                            if (isSpaceDragging) {
                            } else if (isSpaceDoubleTapped) {
                                listener?.onKey(-97, "DOUBLE_SPACE_PERIOD")
                            } else {
                                handleKeyPress(finalKey)
                            }
                        } else if (!isDeleteKey(finalKey)) {
                            handleKeyPress(finalKey)
                        }
                    }
                }
                stopRepeatDelete()
                isSpaceDown = false
                isSpaceCursorActivated = false
                isSpaceCursorCanceled = false
                isSpaceDragging = false
                isSpaceSelecting = false
                isSpaceDoubleTapped = false
                activeKey = null
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                cancelLongPressDetector()
                stopRepeatDelete()
                hidePreview()
                isAccentMode = false
                isSpaceDown = false
                isSpaceCursorActivated = false
                isSpaceCursorCanceled = false
                isSpaceDragging = false
                isSpaceDoubleTapped = false
                activeKey = null
                activeAccentKey = null
                accentKeys.clear()
                invalidate()
            }
        }
        return true
    }

    private fun startLongPressDetector(key: Key) {
        cancelLongPressDetector()
        longPressRunnable = Runnable {
            val text = if (isShifted) key.shiftText else key.normalText
            val accents = KeyboardLayout.getAccents(text)

            if (accents != null) {
                triggerHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                hidePreview()
                setupAccentPopup(key, accents)
            }
        }
        longPressRunnable?.let { longPressHandler.postDelayed(it, 450L) }
    }

    private fun cancelLongPressDetector() {
        longPressRunnable?.let { longPressHandler.removeCallbacks(it) }
        longPressRunnable = null
    }

    private fun setupAccentPopup(sourceKey: Key, accents: List<String>) {
        isAccentMode = true
        accentKeys.clear()

        val density = resources.displayMetrics.density
        val cellWidth = sourceKey.rect.width() * 0.9f
        val cellHeight = sourceKey.rect.height() * 0.9f
        val popupWidth = cellWidth * accents.size

        var left = sourceKey.rect.centerX() - (popupWidth / 2f)
        if (left < 0) left = 10f * density
        if (left + popupWidth > width) left = width - popupWidth - 10f * density
        val top = sourceKey.rect.top - cellHeight - (10f * density)

        accentPopupRect.set(left, top, left + popupWidth, top + cellHeight)

        for (i in accents.indices) {
            val x = left + i * cellWidth
            val rect = RectF(x, top, x + cellWidth, top + cellHeight)
            accentKeys.add(Key(accents[i], accents[i], rect = rect))
        }
        activeAccentKey = accentKeys[0]
        invalidate()
    }

    private fun findAccentKeyAt(x: Float): Key? {
        for (key in accentKeys) {
            if (x >= key.rect.left && x <= key.rect.right) return key
        }
        if (accentKeys.isNotEmpty()) {
            if (x < accentKeys.first().rect.left) return accentKeys.first()
            if (x > accentKeys.last().rect.right) return accentKeys.last()
        }
        return null
    }

    private fun startRepeatDelete() {
        stopRepeatDelete()
        repeatRunnable = object : Runnable {
            override fun run() {
                val success = listener?.onKey(-2, "⌫") ?: false
                if (success) {
                    triggerHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                repeatHandler.postDelayed(this, 50L)
            }
        }
        repeatRunnable?.let { repeatHandler.postDelayed(it, 400L) }
    }

    private fun stopRepeatDelete() {
        repeatRunnable?.let { repeatHandler.removeCallbacks(it) }
        repeatRunnable = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRepeatDelete()
        cancelLongPressDetector()

        previewDismissRunnable?.let { previewHandler.removeCallbacks(it) }

        hidePreview()
        wallpaperBitmap?.recycle()
        wallpaperBitmap = null
    }

    private fun findKeyAt(x: Float, y: Float): Key? {
        if (isEmojiMode) {
            val bottomMargin = getEmojiPickerBottomMargin()
            if (y < height - bottomMargin) return null
        }

        val listToCheck = if (isNumpadMode) numpadKeys else keys.flatten()
        for (key in listToCheck) {
            val cellW = key.rect.width()
            val cellH = key.rect.height()
            val drawnW = cellW * key.scaleX
            val drawnH = cellH * key.scaleY
            val emptyW = cellW - drawnW
            val emptyH = cellH - drawnH
            val startX = key.rect.left + (emptyW * key.alignX)
            val startY = key.rect.top + (emptyH * key.alignY)
            val scaledRect = RectF(startX, startY, startX + drawnW, startY + drawnH)

            if (scaledRect.contains(x, y)) return key
        }
        return null
    }

    private fun handleKeyPress(key: Key) {
        val isNum = key.normalText in listOf("1","2","3","4","5","6","7","8","9","0","၁","၂","၃","၄","၅","၆","၇","၈","၉","၀")

        if (userForceShowNumberRow && !isNum && key.code >= 0) {
            userForceShowNumberRow = false
            isInlineNumberRowVisible = false
            updateLayoutData()
            requestLayout()
            calculateKeyLayout(width, height)
            invalidate()
        }

        if (isEmojiMode) {
            if (key.code == -3) {
                currentMode = KeyboardMode.ENGLISH
                updateLayoutData()
                calculateKeyLayout(width, height)
                listener?.onKey(-99, "CLOSE_EMOJI")
                invalidate()
                return
            }
            if (key.code == -4 || key.normalText == "္" || key.normalText == "#12" || key.normalText == "ABC") {
                currentMode = KeyboardMode.MYANMAR
                updateLayoutData()
                calculateKeyLayout(width, height)
                listener?.onKey(-99, "CLOSE_EMOJI")
                invalidate()
                return
            }
            if (key.normalText == ".") {
                listener?.onKey(-2, "⌫")
                return
            }
        }

        when (key.code) {
            -3 -> {
                isCapsLock = false
                isShifted = false
                currentMode = if (currentMode == KeyboardMode.MYANMAR) KeyboardMode.ENGLISH else KeyboardMode.MYANMAR
                updateLayoutData()
                requestLayout()
                calculateKeyLayout(width, height)
                invalidate()

                listener?.onKey(-3, "")
            }
            -4 -> {
                isCapsLock = false
                isShifted = false
                currentMode = if (currentMode == KeyboardMode.SYMBOLS_1 || currentMode == KeyboardMode.SYMBOLS_2) {
                    KeyboardMode.ENGLISH
                } else {
                    KeyboardMode.SYMBOLS_1
                }
                updateLayoutData()
                requestLayout()
                calculateKeyLayout(width, height)
                invalidate()
            }
            -5 -> {
                currentMode = if (currentMode == KeyboardMode.SYMBOLS_1) KeyboardMode.SYMBOLS_2 else KeyboardMode.SYMBOLS_1
                updateLayoutData()
                calculateKeyLayout(width, height)
                invalidate()
            }
            -8 -> {
                if (prefs.isNumberRowEnabled) {
                    isInlineNumberRowVisible = !isInlineNumberRowVisible
                    userForceShowNumberRow = isInlineNumberRowVisible
                    updateLayoutData()
                    requestLayout()
                    calculateKeyLayout(width, height)
                    invalidate()
                } else {
                    isNumpadMode = true
                    invalidate()
                }
            }
            -16 -> {
                listener?.onKey(-16, "")
            }
            -10 -> {
                isNumpadMode = false
                invalidate()
            }
            -1 -> {
                val currentTime = System.currentTimeMillis()
                val isEnglishMode = currentMode == KeyboardMode.ENGLISH

                if (isEnglishMode && (currentTime - lastShiftTapTime) < SHIFT_DOUBLE_TAP_TIMEOUT) {
                    isCapsLock = !isCapsLock
                    isShifted = isCapsLock
                } else {
                    if (isCapsLock) {
                        isCapsLock = false
                        isShifted = false
                    } else {
                        isShifted = !isShifted
                    }
                }
                lastShiftTapTime = currentTime
                invalidate()
            }
            else -> {
                val text = if (isShifted) key.shiftText else key.normalText
                listener?.onKey(key.code, text)

                if (isShifted && key.code == 0) {
                    if (!isCapsLock) {
                        isShifted = false
                    }
                    invalidate()
                }
            }
        }
    }

    fun setAutoShift(enable: Boolean) {
        if (isCapsLock) return
        if (isShifted != enable) {
            isShifted = enable
            invalidate()
        }
    }

    private class PreviewOverlayView(context: Context) : View(context) {
        var activeKeyRect: RectF? = null
        var activeKeyText: String = ""
        var activeKeyColor: Int = Color.BLUE
        var bgGradient: IntArray? = null
        var textColor: Int = Color.WHITE
        var textGradient: IntArray? = null
        var textSize: Float = 40f

        private val paintBg = Paint(Paint.ANTI_ALIAS_FLAG)
        private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val rect = activeKeyRect ?: return
            if (activeKeyText.isEmpty()) return

            val density = resources.displayMetrics.density
            val offsetDp = 110f * density

            val previewWidth = rect.width() * 1.5f
            val previewHeight = rect.height() * 1.6f

            val cx = rect.centerX()

            val keyTopInOverlay = rect.top + offsetDp
            val bottomY = keyTopInOverlay - (4f * density)
            val topY = bottomY - previewHeight

            var leftX = cx - (previewWidth / 2f)
            var rightX = cx + (previewWidth / 2f)

            val edgePadding = 8f * density
            val screenWidth = width.toFloat()

            if (leftX < edgePadding) {
                val shift = edgePadding - leftX
                leftX += shift
                rightX += shift
            } else if (rightX > screenWidth - edgePadding) {
                val shift = rightX - (screenWidth - edgePadding)
                leftX -= shift
                rightX -= shift
            }

            val previewRect = RectF(leftX, topY, rightX, bottomY)

            if (bgGradient != null) {
                paintBg.shader = android.graphics.LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    bgGradient!!, null, android.graphics.Shader.TileMode.CLAMP
                )
            } else {
                paintBg.shader = null
                paintBg.color = activeKeyColor
            }
            val corner = 20f * density
            canvas.drawRoundRect(previewRect, corner, corner, paintBg)

            if (textGradient != null) {
                val reversedGrad = textGradient!!.reversedArray()
                paintText.shader = android.graphics.LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    reversedGrad, null, android.graphics.Shader.TileMode.CLAMP
                )
            } else {
                paintText.shader = null
                paintText.color = textColor
            }
            paintText.textSize = textSize * 1.6f

            val textX = previewRect.centerX()
            val textY = previewRect.centerY() - (paintText.descent() + paintText.ascent()) / 2 - (4f * density)
            canvas.drawText(activeKeyText, textX, textY, paintText)
        }
    }
}