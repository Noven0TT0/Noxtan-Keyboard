package com.noxtan.noxboard.layouts

import com.noxtan.noxboard.Key

object MyanmarLayout : BaseLayout() {
    fun getLayout(showNumberRow: Boolean): List<List<Key>> {
        val rows = mutableListOf<List<Key>>()

        if (showNumberRow) {
            rows.add(createRow(
                listOf("၁", "၂", "၃", "၄", "၅", "၆", "၇", "၈", "၉", "၀"),
                listOf("၁", "၂", "၃", "၄", "၅", "၆", "၇", "၈", "၉", "၀")
            ))
            val row1 = mutableListOf<Key>()
            row1.add(Key("Emoji", "Emoji", -11, 1.0f))
            row1.add(Key("Vault", "Vault", -12, 1.0f))
            row1.add(Key("Clipboard", "Clipboard", -13, 1.0f))
            row1.add(Key("Settings", "Settings", -14, 1.0f))
            row1.add(Key("Numpad", "Numpad", -8, 1.0f))
            row1.addAll(createRow(listOf("ဏ", "ရ", "ဂ", "ဝ", "ဟ"), listOf("ဩ", "ဋ္ဌ", "ဏ္ဍ", "ဋ", "ဍ"), 1.0f))
            rows.add(row1)
        } else {
            rows.add(listOf(Key("", "", -100, 10.0f)))
            val row0 = mutableListOf<Key>()
            row0.add(Key("Emoji", "Emoji", -11, 1.0f))
            row0.add(Key("Vault", "Vault", -12, 1.0f))
            row0.add(Key("Clipboard", "Clipboard", -13, 1.0f))
            row0.add(Key("Settings", "Settings", -14, 1.0f))
            row0.add(Key("Numpad", "Numpad", -8, 1.0f))
            row0.addAll(createRow(listOf("ဏ", "ရ", "ဂ", "ဝ", "ဟ"), listOf("ဩ", "ဋ္ဌ", "ဏ္ဍ", "ဋ", "ဍ"), 1.0f))
            rows.add(row0)
        }

        rows.add(createRow(
            listOf("ဆ", "တ", "န", "မ", "အ", "ပ", "က", "င", "သ", "စ"),
            listOf("ဈ", "ဎ", "ဣ", "၎င်း", "ဤ", "၌", "ဿ", "၍", "ဥ", "ဧ")
        ))
        rows.add(createRow(
            listOf("ေ", "ျ", "ိ", "်", "့", "ြ", "ု", "ူ", "း"),
            listOf("ဗ", "ှ", "ီ", "ွ", "ံ", "ဲ", "ဒ", "ဓ", "၏")
        ))

        val row3 = mutableListOf<Key>()
        row3.add(Key("Shift", "Shift", -1, 1.3f))
        row3.addAll(createRow(
            listOf("ဖ", "ထ", "ခ", "လ", "ဘ", "ည", "ာ", "ယ"),
            listOf("ဇ", "ဌ", "ဃ", "ဠ", "၊", "ဉ", "ါ", "။"),
            0.925f
        ))
        row3.add(Key("Delete", "Delete", -2, 1.3f))
        rows.add(row3)

        rows.add(listOf(
            Key("Globe", "Globe", -3, 1.5f),
            Key("္", "္", 0, 1f),
            Key("Space", "Space", 32, 5f),
            Key(".", ".", 0, 1f),
            Key("Enter", "Enter", 10, 1.5f)
        ))

        return rows
    }
}