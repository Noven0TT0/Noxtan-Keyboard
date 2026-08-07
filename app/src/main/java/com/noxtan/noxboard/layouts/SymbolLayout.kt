package com.noxtan.noxboard.layouts

import com.noxtan.noxboard.Key

object SymbolLayout : BaseLayout() {
    fun getLayout(page: Int): List<List<Key>> {
        val rows = mutableListOf<List<Key>>()

        if (page == 1) {
            rows.add(createRow(
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                listOf("ESC", "~", "±", "×", "÷", "[", "]", "{", "}", "|")
            ))
            rows.add(createRow(
                listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")"),
                listOf("⇥", "¡", "¿", "❤️", "©", "®", "‹", "^", "∨", "›")
            ))

            val row3 = mutableListOf<Key>()
            row3.add(Key("=\\<", "?123", -5, 1.5f))
            row3.addAll(createRow(
                listOf("\"", ".", ":", "?", "/", "-", "=", "+"),
                listOf("`", "°", "•", "\\", "_", "<", ">", "»"),
                0.875f
            ))
            row3.add(Key("⌫", "⌫", -2, 1.5f))
            rows.add(row3)
        } else {
            rows.add(createRow(
                listOf("ESC", "~", "±", "×", "÷", "[", "]", "{", "}", "|"),
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            ))
            rows.add(createRow(
                listOf("⇥", "¡", "¿", "❤️", "©", "®", "‹", "^", "∨", "›"),
                listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")")
            ))

            val row3 = mutableListOf<Key>()
            row3.add(Key("?123", "=\\<", -5, 1.5f))
            row3.addAll(createRow(
                listOf("`", "°", "•", "\\", "_", "<", ">", "»"),
                listOf("\"", ".", ":", "?", "/", "-", "=", "+"),
                0.875f
            ))
            row3.add(Key("⌫", "⌫", -2, 1.5f))
            rows.add(row3)
        }

        rows.add(listOf(
            Key("🌐", "🌐", -3, 1.5f),
            Key("ABC", "ABC", -4, 1f),
            Key("Space", "Space", 32, 5f),
            Key(".", ".", 0, 1f),
            Key("↵", "↵", 10, 1.5f)
        ))

        return rows
    }
}