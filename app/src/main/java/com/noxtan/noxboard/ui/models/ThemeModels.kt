package com.noxtan.noxboard.ui.models

data class KeyboardThemePreset(
    val id: String,
    val name: String,
    val bgHex: String,
    val keyHex: String,
    val textHex: String,
    val specKeyHex: String,
    val specTextHex: String,
    val borderHex: String = "#00000000",
    val specBorderHex: String = "#00000000",
    val popupBgHex: String = specKeyHex,
    val popupTextHex: String = specTextHex,
    val activeKeyHex: String = specTextHex,
    val sugBgHex: String = bgHex,
    val sugTextHex: String = specTextHex
)

data class CustomizerColorPreset(
    val id: String,
    val bgHex: String,
    val keyHex: String,
    val textHex: String,
    val specKeyHex: String,
    val specTextHex: String
)