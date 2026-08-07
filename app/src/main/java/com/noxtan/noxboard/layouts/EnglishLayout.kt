package com.noxtan.noxboard.layouts

import com.noxtan.noxboard.Key

object EnglishLayout : BaseLayout() {
    fun getLayout(showNumberRow: Boolean): List<List<Key>> {
        val rows = mutableListOf<List<Key>>()

        if (showNumberRow) {
            rows.add(createRow(
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            ))
            val row1 = mutableListOf<Key>()
            row1.add(Key("Emoji", "Emoji", -11, 1.0f))
            row1.add(Key("Vault", "Vault", -12, 1.0f))
            row1.add(Key("Clipboard", "Clipboard", -13, 1.0f))
            row1.add(Key("Settings", "Settings", -14, 1.0f))
            row1.add(Key("Numpad", "Numpad", -8, 1.0f))
            row1.addAll(createRow(listOf("?", "@", "'", "\"", "-"), listOf("!", "*", "`", "(", ")"), 1.0f))
            rows.add(row1)
        } else {
            rows.add(listOf(Key("", "", -100, 10.0f)))
            val row0 = mutableListOf<Key>()
            row0.add(Key("Emoji", "Emoji", -11, 1.0f))
            row0.add(Key("Vault", "Vault", -12, 1.0f))
            row0.add(Key("Clipboard", "Clipboard", -13, 1.0f))
            row0.add(Key("Settings", "Settings", -14, 1.0f))
            row0.add(Key("Numpad", "Numpad", -8, 1.0f))
            row0.addAll(createRow(listOf("?", "@", "'", "\"", "-"), listOf("!", "*", "`", "(", ")"), 1.0f))
            rows.add(row0)
        }

        rows.add(createRow(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
        ))
        rows.add(createRow(
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
        ))

        val row3 = mutableListOf<Key>()
        row3.add(Key("Shift", "Shift", -1, 1.5f))
        row3.addAll(createRow(
            listOf("z", "x", "c", "v", "b", "n", "m"),
            listOf("Z", "X", "C", "V", "B", "N", "M"),
            1f
        ))
        row3.add(Key("Delete", "Delete", -2, 1.5f))
        rows.add(row3)

        rows.add(listOf(
            Key("Globe", "Globe", -3, 1.5f),
            Key("#12", "#12", -4, 1f),
            Key("Space", "Space", 32, 5f),
            Key(".", ".", 0, 1f),
            Key("Enter", "Enter", 10, 1.5f)
        ))

        return rows
    }
}