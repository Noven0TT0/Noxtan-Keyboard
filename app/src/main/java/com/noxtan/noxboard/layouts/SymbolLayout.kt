package com.noxtan.noxboard.layouts

import com.noxtan.noxboard.Key

object SymbolLayout : BaseLayout() {
    fun getLayout(page: Int): List<List<Key>> {
        val rows = mutableListOf<List<Key>>()

        if (page == 1) {
            rows.add(createRow(
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                1f
            ))
            rows.add(createRow(
                listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/"),
                listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/"),
                1f
            ))
            rows.add(createRow(
                listOf("*", "\"", "'", ":", ";", "!", "?", "=", "%", "\\"),
                listOf("*", "\"", "'", ":", ";", "!", "?", "=", "%", "\\"),
                1f
            ))
            val row4 = mutableListOf<Key>()
            row4.add(Key("=\\<", "?123", -5, 1.5f))
            row4.addAll(createRow(
                listOf("<", ">", "[", "]", "{", "}", "~"),
                listOf("<", ">", "[", "]", "{", "}", "~"),
                1f
            ))
            row4.add(Key("⌫", "⌫", -2, 1.5f))
            rows.add(row4)
        } else {
            rows.add(createRow(
                listOf("~", "±", "×", "÷", "^", "°", "•", "○", "●", "|"),
                listOf("~", "±", "×", "÷", "^", "°", "•", "○", "●", "|"),
                1f
            ))
            rows.add(createRow(
                listOf("€", "£", "¥", "₩", "¤", "§", "©", "®", "™", "✓"),
                listOf("€", "£", "¥", "₩", "¤", "§", "©", "®", "™", "✓"),
                1f
            ))
            rows.add(createRow(
                listOf("‹", "›", "«", "»", "“", "”", "‘", "’", "—", "…"),
                listOf("‹", "›", "«", "»", "“", "”", "‘", "’", "—", "…"),
                1f
            ))
            val row4 = mutableListOf<Key>()
            row4.add(Key("?123", "=\\<", -5, 1.5f))
            row4.addAll(createRow(
                listOf("√", "π", "≠", "≤", "≥", "¡", "¿"),
                listOf("√", "π", "≠", "≤", "≥", "¡", "¿"),
                1f
            ))
            row4.add(Key("⌫", "⌫", -2, 1.5f))
            rows.add(row4)
        }

        rows.add(listOf(
            Key("🌐", "🌐", -3, 1.5f),
            Key("ABC", "ABC", -4, 1.5f),
            Key("Space", "Space", 32, 4f),
            Key(".", ".", 0, 1.5f),
            Key("↵", "↵", 10, 1.5f)
        ))

        return rows
    }
}