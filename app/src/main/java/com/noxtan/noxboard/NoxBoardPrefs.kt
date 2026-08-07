package com.noxtan.noxboard

import android.content.Context
import android.content.SharedPreferences

class NoxBoardPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("noxboard_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CLEAR_DICT_TRIGGER = "clear_dict_trigger"
        private const val KEY_KEYBOARD_HEIGHT = "keyboard_height"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_KEYBOARD_WIDTH = "keyboard_width"
        private const val KEY_KEYBOARD_ALIGNMENT = "keyboard_alignment"
        private const val KEY_KEY_CORNER_RADIUS = "key_corner_radius"
        private const val KEY_KEY_PADDING = "key_padding"
        private const val KEY_BOTTOM_PADDING = "bottom_padding"
        private const val KEY_IS_SLIDING = "is_sliding"
        private const val KEY_SLIDE_WIDTH = "slide_width"
        private const val KEY_SLIDE_HEIGHT = "slide_height"
        private const val KEY_SLIDE_BOTTOM_PADDING = "slide_bottom_padding"
        private const val KEY_SLIDE_ALIGNMENT = "slide_alignment"
        private const val KEY_SOUND_VOLUME = "sound_volume"
        private const val KEY_SELECTED_SOUND_PACK = "selected_sound_pack"
        private const val KEY_PREVIEW_ENABLED = "key_preview_enabled"
        private const val KEY_VIBRATION_DURATION = "vibration_duration"
        private const val KEY_VIBRATION_STRENGTH = "vibration_strength"
        private const val KEY_CUSTOM_WALLPAPER_URI = "custom_wallpaper_uri"
        private const val KEY_CUSTOM_WALLPAPER_SCALE = "custom_wallpaper_scale"
        private const val KEY_CUSTOM_WALLPAPER_OFFSET_X = "custom_wallpaper_offset_x"
        private const val KEY_CUSTOM_WALLPAPER_OFFSET_Y = "custom_wallpaper_offset_y"
        private const val KEY_CUSTOM_BG_COLOR = "custom_bg_color"
        private const val KEY_CUSTOM_KEY_COLOR = "custom_key_color"
        private const val KEY_CUSTOM_KEY_BORDER_COLOR = "custom_key_border_color"
        private const val KEY_CUSTOM_TEXT_COLOR = "custom_text_color"
        private const val KEY_CUSTOM_SPECIAL_TEXT_COLOR = "custom_special_text_color"
        private const val KEY_CUSTOM_SPECIAL_KEY_COLOR = "custom_special_key_color"
        private const val KEY_CUSTOM_ACTIVE_KEY_COLOR = "custom_active_key_color"
        private const val KEY_CUSTOM_POPUP_BG_COLOR = "custom_popup_bg_color"
        private const val KEY_CUSTOM_POPUP_TEXT_COLOR = "custom_popup_text_color"
        private const val KEY_INDIVIDUAL_KEY_COLORS = "individual_key_colors"
        private const val KEY_INDIVIDUAL_KEY_TEXT_COLORS = "individual_key_text_colors"
        private const val KEY_INDIVIDUAL_KEY_ACTIVE_COLORS = "individual_key_active_colors"
        private const val KEY_INDIVIDUAL_KEY_POPUP_BG_COLORS = "individual_key_popup_bg_colors"
        private const val KEY_INDIVIDUAL_KEY_POPUP_TEXT_COLORS = "individual_key_popup_text_colors"
        private const val KEY_NUMBER_ROW = "number_row_enabled"
        private const val KEY_MYANMAR_TYPING_STYLE = "myanmar_typing_style"
        private const val KEY_SPACE_CURSOR_CONTROL = "space_cursor_control"
        private const val KEY_SPACE_CURSOR_SENSITIVITY = "space_cursor_sensitivity"
        private const val KEY_SPACE_DRAG_DELAY = "space_drag_delay"
        private const val KEY_GLOBAL_ROW_GAP = "global_row_gap"
        private const val KEY_INDIVIDUAL_ROW_GAPS = "individual_row_gaps"
        private const val KEY_USE_CUSTOM_ICON_COLORS = "use_custom_icon_colors"
        private const val KEY_SUGGESTION_ENABLED = "suggestion_enabled"
        private const val KEY_AUTO_CORRECTION = "auto_correction"
        private const val KEY_NEXT_WORD_PREDICTION = "next_word_prediction"
        private const val KEY_AUTO_CAPITALIZATION = "auto_capitalization"
        private const val KEY_AUTO_SPACE = "auto_space"
        private const val KEY_BLOCK_OFFENSIVE = "block_offensive_words"
        private const val KEY_DOUBLE_SPACE_PERIOD = "double_space_period"
        private const val KEY_ACTIVE_THEME = "active_theme"
        private const val KEY_SAVED_THEMES = "saved_custom_themes"
        private const val KEY_ACTIVE_SIZE_PRESET = "active_size_preset"
        private const val KEY_SAVED_SIZE_PRESETS = "saved_size_presets"
        private const val KEY_ACTIVE_LAYOUT_MYANMAR = "active_layout_myanmar"
        private const val KEY_ACTIVE_LAYOUT_ENGLISH = "active_layout_english"
        private const val KEY_ACTIVE_LAYOUT_SYMBOLS = "active_layout_symbols"
        private const val KEY_SAVED_LAYOUTS_MYANMAR = "saved_layouts_myanmar"
        private const val KEY_SAVED_LAYOUTS_ENGLISH = "saved_layouts_english"
        private const val KEY_SAVED_LAYOUTS_SYMBOLS = "saved_layouts_symbols"
        private const val KEY_EDITING_THEME_ID = "editing_theme_id"
        private const val KEY_CUSTOM_LAYOUT_MYANMAR = "custom_layout_myanmar"
        private const val KEY_CUSTOM_LAYOUT_ENGLISH = "custom_layout_english"
        private const val KEY_CUSTOM_LAYOUT_SYMBOLS = "custom_layout_symbols"
        private const val KEY_DOUBLE_TAP_STATES = "double_tap_states"
        private const val KEY_TEXTURE_EFFECT = "texture_effect_enabled"
        private const val KEY_ICON_STYLE_EMOJI = "icon_style_emoji"
        private const val KEY_ICON_STYLE_ENTER = "icon_style_enter"
        private const val KEY_ICON_STYLE_GLOBE = "icon_style_globe"
        private const val KEY_ICON_STYLE_VAULT = "icon_style_vault"
        private const val KEY_ICON_STYLE_NUMPAD = "icon_style_numpad"
        private const val KEY_ICON_STYLE_CLIPBOARD = "icon_style_clipboard"
        private const val KEY_ICON_STYLE_SETTING = "icon_style_setting"
        private const val KEY_ICON_STYLE_SHIFT = "icon_style_shift"
        private const val KEY_ICON_STYLE_SPACEBAR = "icon_style_spacebar"
        private const val KEY_ICON_STYLE_BACKSPACE = "icon_style_backspace"
        private const val KEY_ICON_STYLE_SYMBOLS = "icon_style_symbols"
        private const val KEY_ICON_STYLE_HIDE_KEYBOARD = "icon_style_hide_keyboard"
    }

    var clearDictTrigger: Long
        get() = prefs.getLong(KEY_CLEAR_DICT_TRIGGER, 0L)
        set(value) = prefs.edit().putLong(KEY_CLEAR_DICT_TRIGGER, value).apply()

    var isTextureEffectEnabled: Boolean
        get() = prefs.getBoolean(KEY_TEXTURE_EFFECT, false)
        set(value) = prefs.edit().putBoolean(KEY_TEXTURE_EFFECT, value).apply()

    var editingThemeId: String?
        get() = prefs.getString(KEY_EDITING_THEME_ID, null)
        set(value) = prefs.edit().putString(KEY_EDITING_THEME_ID, value).apply()

    var savedCustomThemes: String
        get() = prefs.getString(KEY_SAVED_THEMES, "[]") ?: "[]"
        set(value) = prefs.edit().putString(KEY_SAVED_THEMES, value).apply()

    var activeSizePreset: String
        get() = prefs.getString(KEY_ACTIVE_SIZE_PRESET, "default") ?: "default"
        set(value) = prefs.edit().putString(KEY_ACTIVE_SIZE_PRESET, value).apply()

    var savedSizePresets: String
        get() = prefs.getString(KEY_SAVED_SIZE_PRESETS, "[]") ?: "[]"
        set(value) = prefs.edit().putString(KEY_SAVED_SIZE_PRESETS, value).apply()

    var activeTheme: String
        get() = prefs.getString(KEY_ACTIVE_THEME, "DEFAULT") ?: "DEFAULT"
        set(value) = prefs.edit().putString(KEY_ACTIVE_THEME, value).apply()
    var useCustomIconColors: Boolean
        get() = prefs.getBoolean(KEY_USE_CUSTOM_ICON_COLORS, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_CUSTOM_ICON_COLORS, value).apply()

    var isSpaceCursorControlEnabled: Boolean
        get() = prefs.getBoolean(KEY_SPACE_CURSOR_CONTROL, true)
        set(value) = prefs.edit().putBoolean(KEY_SPACE_CURSOR_CONTROL, value).apply()

    var doubleTapStates: String
        get() {
            val defaultJson = """{"ဆ":false,"ဖ":false,"ထ":false,"ခ":false,"ည":false,"ဏ":false,"ဂ":false,"ဝ":false,"ဟ":false,"သ":false,"ု":false,"ူ":false}"""
            return prefs.getString(KEY_DOUBLE_TAP_STATES, defaultJson) ?: defaultJson
        }
        set(value) = prefs.edit().putString(KEY_DOUBLE_TAP_STATES, value).apply()

    var spaceCursorSensitivity: Float
        get() = prefs.getFloat(KEY_SPACE_CURSOR_SENSITIVITY, 2.5f)
        set(value) = prefs.edit().putFloat(KEY_SPACE_CURSOR_SENSITIVITY, value).apply()

    var spaceDragDelay: Float
        get() = prefs.getFloat(KEY_SPACE_DRAG_DELAY, 300f)
        set(value) = prefs.edit().putFloat(KEY_SPACE_DRAG_DELAY, value).apply()

    var globalRowGap: Float
        get() = prefs.getFloat(KEY_GLOBAL_ROW_GAP, 0f)
        set(value) = prefs.edit().putFloat(KEY_GLOBAL_ROW_GAP, value).apply()

    var individualRowGaps: String
        get() = prefs.getString(KEY_INDIVIDUAL_ROW_GAPS, "{}") ?: "{}"
        set(value) = prefs.edit().putString(KEY_INDIVIDUAL_ROW_GAPS, value).apply()

    var isNumberRowEnabled: Boolean
        get() = prefs.getBoolean(KEY_NUMBER_ROW, false)
        set(value) = prefs.edit().putBoolean(KEY_NUMBER_ROW, value).apply()

    var myanmarTypingStyle: String
        get() = prefs.getString(KEY_MYANMAR_TYPING_STYLE, "UNICODE") ?: "UNICODE"
        set(value) = prefs.edit().putString(KEY_MYANMAR_TYPING_STYLE, value).apply()

    var individualKeyColors: String
        get() = prefs.getString(KEY_INDIVIDUAL_KEY_COLORS, "{}") ?: "{}"
        set(value) = prefs.edit().putString(KEY_INDIVIDUAL_KEY_COLORS, value).apply()

    var individualKeyTextColors: String
        get() = prefs.getString(KEY_INDIVIDUAL_KEY_TEXT_COLORS, "{}") ?: "{}"
        set(value) = prefs.edit().putString(KEY_INDIVIDUAL_KEY_TEXT_COLORS, value).apply()

    var individualKeyActiveColors: String
        get() = prefs.getString(KEY_INDIVIDUAL_KEY_ACTIVE_COLORS, "{}") ?: "{}"
        set(value) = prefs.edit().putString(KEY_INDIVIDUAL_KEY_ACTIVE_COLORS, value).apply()

    var individualKeyPopupBgColors: String
        get() = prefs.getString(KEY_INDIVIDUAL_KEY_POPUP_BG_COLORS, "{}") ?: "{}"
        set(value) = prefs.edit().putString(KEY_INDIVIDUAL_KEY_POPUP_BG_COLORS, value).apply()

    var individualKeyPopupTextColors: String
        get() = prefs.getString(KEY_INDIVIDUAL_KEY_POPUP_TEXT_COLORS, "{}") ?: "{}"
        set(value) = prefs.edit().putString(KEY_INDIVIDUAL_KEY_POPUP_TEXT_COLORS, value).apply()

    var customBackgroundColor: String
        get() = prefs.getString(KEY_CUSTOM_BG_COLOR, "#000000") ?: "#000000"
        set(value) = prefs.edit().putString(KEY_CUSTOM_BG_COLOR, value).apply()

    var customKeyColor: String
        get() = prefs.getString(KEY_CUSTOM_KEY_COLOR, "#1E1A2B") ?: "#1E1A2B"
        set(value) = prefs.edit().putString(KEY_CUSTOM_KEY_COLOR, value).apply()

    var customKeyBorderColor: String
        get() = prefs.getString(KEY_CUSTOM_KEY_BORDER_COLOR, "#00000000") ?: "#00000000"
        set(value) = prefs.edit().putString(KEY_CUSTOM_KEY_BORDER_COLOR, value).apply()

    var customTextColor: String
        get() = prefs.getString(KEY_CUSTOM_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF"
        set(value) = prefs.edit().putString(KEY_CUSTOM_TEXT_COLOR, value).apply()

    var customSpecialTextColor: String
        get() = prefs.getString(KEY_CUSTOM_SPECIAL_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF"
        set(value) = prefs.edit().putString(KEY_CUSTOM_SPECIAL_TEXT_COLOR, value).apply()

    var customSpecialKeyColor: String
        get() = prefs.getString(KEY_CUSTOM_SPECIAL_KEY_COLOR, "#12101C") ?: "#12101C"
        set(value) = prefs.edit().putString(KEY_CUSTOM_SPECIAL_KEY_COLOR, value).apply()

    var customActiveKeyColor: String
        get() = prefs.getString(KEY_CUSTOM_ACTIVE_KEY_COLOR, "#7C4DFF") ?: "#7C4DFF"
        set(value) = prefs.edit().putString(KEY_CUSTOM_ACTIVE_KEY_COLOR, value).apply()

    var customPopupBgColor: String
        get() = prefs.getString(KEY_CUSTOM_POPUP_BG_COLOR, "#12101C") ?: "#12101C"
        set(value) = prefs.edit().putString(KEY_CUSTOM_POPUP_BG_COLOR, value).apply()

    var customPopupTextColor: String
        get() = prefs.getString(KEY_CUSTOM_POPUP_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF"
        set(value) = prefs.edit().putString(KEY_CUSTOM_POPUP_TEXT_COLOR, value).apply()

    var customWallpaperUri: String?
        get() = prefs.getString(KEY_CUSTOM_WALLPAPER_URI, null)
        set(value) = prefs.edit().putString(KEY_CUSTOM_WALLPAPER_URI, value).apply()

    var customWallpaperScale: Float
        get() = prefs.getFloat(KEY_CUSTOM_WALLPAPER_SCALE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_CUSTOM_WALLPAPER_SCALE, value).apply()

    var customWallpaperOffsetX: Float
        get() = prefs.getFloat(KEY_CUSTOM_WALLPAPER_OFFSET_X, 0f)
        set(value) = prefs.edit().putFloat(KEY_CUSTOM_WALLPAPER_OFFSET_X, value).apply()

    var customWallpaperOffsetY: Float
        get() = prefs.getFloat(KEY_CUSTOM_WALLPAPER_OFFSET_Y, 0f)
        set(value) = prefs.edit().putFloat(KEY_CUSTOM_WALLPAPER_OFFSET_Y, value).apply()

    var isKeyPreviewEnabled: Boolean
        get() = prefs.getBoolean(KEY_PREVIEW_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_PREVIEW_ENABLED, value).apply()

    var keyboardHeight: Float
        get() = prefs.getFloat(KEY_KEYBOARD_HEIGHT, 235f)
        set(value) = prefs.edit().putFloat(KEY_KEYBOARD_HEIGHT, value).apply()

    var keyFontSize: Float
        get() {
            val savedValue = prefs.getFloat(KEY_FONT_SIZE, 16f)
            if (savedValue > 50f) {
                prefs.edit().putFloat(KEY_FONT_SIZE, 16f).apply()
                return 16f
            }
            return savedValue
        }
        set(value) = prefs.edit().putFloat(KEY_FONT_SIZE, value).apply()

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, false)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var keyboardWidth: Float
        get() = prefs.getFloat(KEY_KEYBOARD_WIDTH, 100f)
        set(value) = prefs.edit().putFloat(KEY_KEYBOARD_WIDTH, value).apply()

    var keyboardAlignment: String
        get() = prefs.getString(KEY_KEYBOARD_ALIGNMENT, "CENTER") ?: "CENTER"
        set(value) = prefs.edit().putString(KEY_KEYBOARD_ALIGNMENT, value).apply()

    var keyCornerRadius: Float
        get() = prefs.getFloat(KEY_KEY_CORNER_RADIUS, 10f)
        set(value) = prefs.edit().putFloat(KEY_KEY_CORNER_RADIUS, value).apply()

    var keyPadding: Float
        get() = prefs.getFloat(KEY_KEY_PADDING, 1f)
        set(value) = prefs.edit().putFloat(KEY_KEY_PADDING, value).apply()

    var bottomPadding: Float
        get() = prefs.getFloat(KEY_BOTTOM_PADDING, 54f)
        set(value) = prefs.edit().putFloat(KEY_BOTTOM_PADDING, value).apply()

    var isSliding: Boolean
        get() = prefs.getBoolean(KEY_IS_SLIDING, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_SLIDING, value).apply()

    var slideWidth: Float
        get() = prefs.getFloat(KEY_SLIDE_WIDTH, 100f)
        set(value) = prefs.edit().putFloat(KEY_SLIDE_WIDTH, value).apply()

    var slideHeight: Float
        get() = prefs.getFloat(KEY_SLIDE_HEIGHT, 220f)
        set(value) = prefs.edit().putFloat(KEY_SLIDE_HEIGHT, value).apply()

    var slideBottomPadding: Float
        get() = prefs.getFloat(KEY_SLIDE_BOTTOM_PADDING, 54f)
        set(value) = prefs.edit().putFloat(KEY_SLIDE_BOTTOM_PADDING, value).apply()

    var slideAlignment: String
        get() = prefs.getString(KEY_SLIDE_ALIGNMENT, "CENTER") ?: "CENTER"
        set(value) = prefs.edit().putString(KEY_SLIDE_ALIGNMENT, value).apply()

    var soundVolume: Float
        get() = prefs.getFloat(KEY_SOUND_VOLUME, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SOUND_VOLUME, value).apply()

    var selectedSoundPack: String
        get() = prefs.getString(KEY_SELECTED_SOUND_PACK, "DEFAULT") ?: "DEFAULT"
        set(value) = prefs.edit().putString(KEY_SELECTED_SOUND_PACK, value).apply()

    var vibrationDuration: Float
        get() = prefs.getFloat(KEY_VIBRATION_DURATION, 20f)
        set(value) = prefs.edit().putFloat(KEY_VIBRATION_DURATION, value).apply()

    var vibrationStrength: Float
        get() = prefs.getFloat(KEY_VIBRATION_STRENGTH, 50f)
        set(value) = prefs.edit().putFloat(KEY_VIBRATION_STRENGTH, value).apply()

    var customLayoutMyanmar: String
        get() = prefs.getString(KEY_CUSTOM_LAYOUT_MYANMAR, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_LAYOUT_MYANMAR, value).apply()

    var customLayoutEnglish: String
        get() = prefs.getString(KEY_CUSTOM_LAYOUT_ENGLISH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_LAYOUT_ENGLISH, value).apply()

    var customLayoutSymbols: String
        get() = prefs.getString(KEY_CUSTOM_LAYOUT_SYMBOLS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_LAYOUT_SYMBOLS, value).apply()

    var isSuggestionEnabled: Boolean
        get() = prefs.getBoolean(KEY_SUGGESTION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SUGGESTION_ENABLED, value).apply()

    var isIncognitoModeEnabled: Boolean
        get() = prefs.getBoolean("incognito_mode_enabled", false)
        set(value) = prefs.edit().putBoolean("incognito_mode_enabled", value).apply()

    var isAutoCorrectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CORRECTION, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CORRECTION, value).apply()

    var isNextWordPredictionEnabled: Boolean
        get() = prefs.getBoolean(KEY_NEXT_WORD_PREDICTION, true)
        set(value) = prefs.edit().putBoolean(KEY_NEXT_WORD_PREDICTION, value).apply()

    var isAutoCapitalizationEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CAPITALIZATION, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CAPITALIZATION, value).apply()

    var isAutoSpaceEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SPACE, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SPACE, value).apply()

    var isDoubleSpacePeriodEnabled: Boolean
        get() = prefs.getBoolean(KEY_DOUBLE_SPACE_PERIOD, true)
        set(value) = prefs.edit().putBoolean(KEY_DOUBLE_SPACE_PERIOD, value).apply()

    var isBlockOffensiveWordsEnabled: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_OFFENSIVE, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_OFFENSIVE, value).apply()

    var customNumpadBgColor: String
        get() = prefs.getString("custom_numpad_bg_color", "#000000") ?: "#000000"
        set(value) = prefs.edit().putString("custom_numpad_bg_color", value).apply()

    var customNumpadKeyColor: String
        get() = prefs.getString("custom_numpad_key_color", "#1E1A2B") ?: "#1E1A2B"
        set(value) = prefs.edit().putString("custom_numpad_key_color", value).apply()

    var customNumpadTextColor: String
        get() = prefs.getString("custom_numpad_text_color", "#FFFFFF") ?: "#FFFFFF"
        set(value) = prefs.edit().putString("custom_numpad_text_color", value).apply()

    var customNumpadSpecialKeyColor: String
        get() = prefs.getString("custom_numpad_special_key_color", "#12101C") ?: "#12101C"
        set(value) = prefs.edit().putString("custom_numpad_special_key_color", value).apply()

    var customNumpadSpecialTextColor: String
        get() = prefs.getString("custom_numpad_special_text_color", "#FFFFFF") ?: "#FFFFFF"
        set(value) = prefs.edit().putString("custom_numpad_special_text_color", value).apply()

    var activeLayoutMyanmar: String
        get() = prefs.getString(KEY_ACTIVE_LAYOUT_MYANMAR, "default") ?: "default"
        set(value) = prefs.edit().putString(KEY_ACTIVE_LAYOUT_MYANMAR, value).apply()

    var activeLayoutEnglish: String
        get() = prefs.getString(KEY_ACTIVE_LAYOUT_ENGLISH, "default") ?: "default"
        set(value) = prefs.edit().putString(KEY_ACTIVE_LAYOUT_ENGLISH, value).apply()

    var activeLayoutSymbols: String
        get() = prefs.getString(KEY_ACTIVE_LAYOUT_SYMBOLS, "default") ?: "default"
        set(value) = prefs.edit().putString(KEY_ACTIVE_LAYOUT_SYMBOLS, value).apply()

    var savedLayoutsMyanmar: String
        get() = prefs.getString(KEY_SAVED_LAYOUTS_MYANMAR, "[]") ?: "[]"
        set(value) = prefs.edit().putString(KEY_SAVED_LAYOUTS_MYANMAR, value).apply()

    var savedLayoutsEnglish: String
        get() = prefs.getString(KEY_SAVED_LAYOUTS_ENGLISH, "[]") ?: "[]"
        set(value) = prefs.edit().putString(KEY_SAVED_LAYOUTS_ENGLISH, value).apply()

    var savedLayoutsSymbols: String
        get() = prefs.getString(KEY_SAVED_LAYOUTS_SYMBOLS, "[]") ?: "[]"
        set(value) = prefs.edit().putString(KEY_SAVED_LAYOUTS_SYMBOLS, value).apply()

    var iconStyleEmoji: String get() = prefs.getString(KEY_ICON_STYLE_EMOJI, "DEFAULT") ?: "DEFAULT"; set(value) = prefs.edit().putString(KEY_ICON_STYLE_EMOJI, value).apply()
    var iconStyleEnter: String get() = prefs.getString(KEY_ICON_STYLE_ENTER, "DEFAULT") ?: "DEFAULT"; set(value) = prefs.edit().putString(KEY_ICON_STYLE_ENTER, value).apply()
    var iconStyleGlobe: String get() = prefs.getString(KEY_ICON_STYLE_GLOBE, "DEFAULT") ?: "DEFAULT"; set(value) = prefs.edit().putString(KEY_ICON_STYLE_GLOBE, value).apply()
    var iconStyleVault: String get() = prefs.getString(KEY_ICON_STYLE_VAULT, "DEFAULT") ?: "DEFAULT"; set(value) = prefs.edit().putString(KEY_ICON_STYLE_VAULT, value).apply()
    var iconStyleNumpad: String get() = prefs.getString(KEY_ICON_STYLE_NUMPAD, "DEFAULT") ?: "DEFAULT"; set(value) = prefs.edit().putString(KEY_ICON_STYLE_NUMPAD, value).apply()
    var iconStyleClipboard: String get() = prefs.getString(KEY_ICON_STYLE_CLIPBOARD, "DEFAULT") ?: "DEFAULT"; set(value) = prefs.edit().putString(KEY_ICON_STYLE_CLIPBOARD, value).apply()
    var iconStyleSetting: String get() = prefs.getString(KEY_ICON_STYLE_SETTING, "DEFAULT") ?: "DEFAULT"; set(value) = prefs.edit().putString(KEY_ICON_STYLE_SETTING, value).apply()
    var iconStyleShift: String get() = prefs.getString(KEY_ICON_STYLE_SHIFT, "DEFAULT") ?: "DEFAULT"; set(value) = prefs.edit().putString(KEY_ICON_STYLE_SHIFT, value).apply()
    var iconStyleSpacebar: String get() = prefs.getString(KEY_ICON_STYLE_SPACEBAR, "DEFAULT") ?: "DEFAULT"; set(value) = prefs.edit().putString(KEY_ICON_STYLE_SPACEBAR, value).apply()
    var iconStyleBackspace: String get() = prefs.getString(KEY_ICON_STYLE_BACKSPACE, "DEFAULT") ?: "DEFAULT"; set(value) = prefs.edit().putString(KEY_ICON_STYLE_BACKSPACE, value).apply()
    var iconStyleSymbols: String get() = prefs.getString(KEY_ICON_STYLE_SYMBOLS, "DEFAULT") ?: "DEFAULT"; set(value) = prefs.edit().putString(KEY_ICON_STYLE_SYMBOLS, value).apply()
    var iconStyleHideKeyboard: String get() = prefs.getString(KEY_ICON_STYLE_HIDE_KEYBOARD, "DEFAULT") ?: "DEFAULT"; set(value) = prefs.edit().putString(KEY_ICON_STYLE_HIDE_KEYBOARD, value).apply()
}