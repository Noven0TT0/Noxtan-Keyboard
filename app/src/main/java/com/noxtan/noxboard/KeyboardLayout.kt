package com.noxtan.noxboard

import android.content.Context
import com.noxtan.noxboard.layouts.EnglishLayout
import com.noxtan.noxboard.layouts.MyanmarLayout
import com.noxtan.noxboard.layouts.NumpadLayout
import com.noxtan.noxboard.layouts.SymbolLayout

object KeyboardLayout {
    private fun parseLayoutFromJson(jsonStr: String): List<List<Key>>? {
        try {
            val rootArray = org.json.JSONArray(jsonStr)
            val layout = mutableListOf<List<Key>>()
            for (i in 0 until rootArray.length()) {
                val rowArray = rootArray.getJSONArray(i)
                val row = mutableListOf<Key>()
                for (j in 0 until rowArray.length()) {
                    val keyObj = rowArray.getJSONObject(j)
                    val cIcon = keyObj.optString("customIcon", "")
                    row.add(Key(
                        normalText = keyObj.optString("normal", ""),
                        shiftText = keyObj.optString("shift", ""),
                        code = keyObj.optInt("code", 0),
                        widthWeight = keyObj.optDouble("weight", 1.0).toFloat(),
                        scaleX = keyObj.optDouble("scaleX", 1.0).toFloat(),
                        scaleY = keyObj.optDouble("scaleY", 1.0).toFloat(),
                        alignX = keyObj.optDouble("alignX", 0.5).toFloat(),
                        alignY = keyObj.optDouble("alignY", 0.5).toFloat(),
                        customIcon = if (cIcon.isNotEmpty()) cIcon else null
                    ))
                }
                layout.add(row)
            }
            return layout
        } catch (e: Exception) {
            com.noxtan.noxboard.utils.NoxLogger.logError("KeyboardLayout", "Failed to parse custom layout", e)
            return null
        }
    }

    fun layoutToJson(layout: List<List<Key>>): String {
        val rootArray = org.json.JSONArray()
        for (row in layout) {
            val rowArray = org.json.JSONArray()
            for (key in row) {
                val keyObj = org.json.JSONObject()
                keyObj.put("normal", key.normalText)
                keyObj.put("shift", key.shiftText)
                keyObj.put("code", key.code)
                keyObj.put("weight", key.widthWeight.toDouble())
                keyObj.put("scaleX", key.scaleX.toDouble())
                keyObj.put("scaleY", key.scaleY.toDouble())
                keyObj.put("alignX", key.alignX.toDouble())
                keyObj.put("alignY", key.alignY.toDouble())
                if (key.customIcon != null) {
                    keyObj.put("customIcon", key.customIcon)
                }
                rowArray.put(keyObj)
            }
            rootArray.put(rowArray)
        }
        return rootArray.toString()
    }

    fun getMyanmarLayout(context: Context, showNumberRow: Boolean): List<List<Key>> {
        val prefs = NoxBoardPrefs(context)
        val active = prefs.activeLayoutMyanmar
        if (active != "default") {
            try {
                val arr = org.json.JSONArray(prefs.savedLayoutsMyanmar)
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    if (item.optString("id") == active) {
                        val layoutJson = item.optString("layout", "")
                        if (layoutJson.isNotEmpty()) {
                            parseLayoutFromJson(layoutJson)?.let { layout ->
                                return applyNumberRowToCustom(layout, showNumberRow, isEnglish = false)
                            }
                        }
                    }
                }
            } catch (e: Exception) {}
        }
        val custom = prefs.customLayoutMyanmar
        if (custom.isNotEmpty() && active == "default") {
            parseLayoutFromJson(custom)?.let { layout ->
                return applyNumberRowToCustom(layout, showNumberRow, isEnglish = false)
            }
        }
        return MyanmarLayout.getLayout(showNumberRow)
    }

    fun getEnglishLayout(context: Context, showNumberRow: Boolean): List<List<Key>> {
        val prefs = NoxBoardPrefs(context)
        val active = prefs.activeLayoutEnglish
        if (active != "default") {
            try {
                val arr = org.json.JSONArray(prefs.savedLayoutsEnglish)
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    if (item.optString("id") == active) {
                        val layoutJson = item.optString("layout", "")
                        if (layoutJson.isNotEmpty()) {
                            parseLayoutFromJson(layoutJson)?.let { layout ->
                                return applyNumberRowToCustom(layout, showNumberRow, isEnglish = true)
                            }
                        }
                    }
                }
            } catch (e: Exception) {}
        }
        val custom = prefs.customLayoutEnglish
        if (custom.isNotEmpty() && active == "default") {
            parseLayoutFromJson(custom)?.let { layout ->
                return applyNumberRowToCustom(layout, showNumberRow, isEnglish = true)
            }
        }
        return EnglishLayout.getLayout(showNumberRow)
    }

    private fun applyNumberRowToCustom(layout: List<List<Key>>, showNumberRow: Boolean, isEnglish: Boolean): List<List<Key>> {
        if (layout.isEmpty()) return layout
        val result = layout.map { it.toMutableList() }.toMutableList()

        if (showNumberRow) {
            val normals = if (isEnglish) listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0") else listOf("၁", "၂", "၃", "၄", "၅", "၆", "၇", "၈", "၉", "၀")
            val shifts = if (isEnglish) listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0") else listOf("၁", "၂", "၃", "၄", "၅", "၆", "၇", "၈", "၉", "၀")
            result[0] = normals.indices.map { i -> Key(normals[i], shifts[i], 0, 1f) }.toMutableList()
        } else {
            result[0] = listOf(Key("", "", -100, 10.0f)).toMutableList()
        }

        return result
    }

    fun getSymbolLayout(context: Context, page: Int): List<List<Key>> {
        val prefs = NoxBoardPrefs(context)
        val active = prefs.activeLayoutSymbols

        var customLayout: List<List<Key>>? = null
        if (active != "default") {
            try {
                val arr = org.json.JSONArray(prefs.savedLayoutsSymbols)
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    if (item.optString("id") == active) {
                        val layoutJson = item.optString("layout", "")
                        if (layoutJson.isNotEmpty()) {
                            customLayout = parseLayoutFromJson(layoutJson)
                            break
                        }
                    }
                }
            } catch (e: Exception) {}
        }

        if (customLayout == null) {
            val custom = prefs.customLayoutSymbols
            if (custom.isNotEmpty() && active == "default") {
                customLayout = parseLayoutFromJson(custom)
            }
        }

        if (customLayout != null) {
            if (page == 1) {
                return customLayout
            } else {
                return customLayout.map { row ->
                    row.map { key ->
                        key.copy(normalText = key.shiftText)
                    }
                }
            }
        }

        return SymbolLayout.getLayout(page)
    }
    fun getNumpadLayout(isEnglish: Boolean = false): List<Key> = NumpadLayout.getLayout(isEnglish)

    private val accentMap = mapOf(
        "q" to listOf("q", "1"),
        "Q" to listOf("Q", "1"),
        "w" to listOf("w", "2"),
        "W" to listOf("W", "2"),
        "e" to listOf("e", "3", "è", "é", "ê", "ë"),
        "E" to listOf("E", "3", "È", "É", "Ê", "Ë"),
        "r" to listOf("r", "4"),
        "R" to listOf("R", "4"),
        "t" to listOf("t", "5"),
        "T" to listOf("T", "5"),
        "y" to listOf("y", "6", "ÿ", "ý"),
        "Y" to listOf("Y", "6", "Ÿ", "Ý"),
        "u" to listOf("u", "7", "ù", "ú", "û", "ü"),
        "U" to listOf("U", "7", "Ù", "Ú", "Û", "Ü"),
        "i" to listOf("i", "8", "ì", "í", "î", "ï"),
        "I" to listOf("I", "8", "Ì", "Í", "Î", "Ï"),
        "o" to listOf("o", "9", "ò", "ó", "ô", "ö", "õ"),
        "O" to listOf("O", "9", "Ò", "Ó", "Ô", "Õ"),
        "p" to listOf("p", "0"),
        "P" to listOf("P", "0"),
        "." to listOf("_", "/", "!", ";", ":", "?", "@", ","),
        "a" to listOf("a", "à", "á", "â", "ä", "æ", "ã", "å"),
        "A" to listOf("A", "À", "Á", "Â", "Ä", "Æ", "Ã", "Å"),
        "b" to listOf("₿"),
        "B" to listOf("₿"),
        "c" to listOf("c", "ç", "ć", "č"),
        "C" to listOf("C", "Ç", "Ć", "Č"),
        "d" to listOf("ð", "ď", "đ", "ɗ", "ḍ"),
        "D" to listOf("Ð", "Ď", "Đ", "Ɗ", "Ḍ"),
        "k" to listOf("[", "{"),
        "K" to listOf("[", "{"),
        "l" to listOf("]", "}", "£"),
        "L" to listOf("]", "}", "£"),
        "n" to listOf("n", "ñ", "ń"),
        "N" to listOf("N", "Ñ", "Ń"),
        "s" to listOf("s", "ß", "ś", "š"),
        "S" to listOf("S", "Ś", "$", "Š"),
        "z" to listOf("z", "ź", "ž", "ż"),
        "Z" to listOf("Z", "Ź", "Ž", "Ż")
    )

    fun getAccents(char: String): List<String>? {
        return accentMap[char]
    }
}