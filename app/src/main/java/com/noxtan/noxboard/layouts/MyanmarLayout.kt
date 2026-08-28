package com.noxtan.noxboard.layouts

import com.noxtan.noxboard.Key

object MyanmarLayout : BaseLayout() {
    fun getLayout(showNumberRow: Boolean, isClassic: Boolean = false): List<List<Key>> {
        val rows = mutableListOf<List<Key>>()

        if (isClassic) {
            rows.add(createRow(
                listOf("၁", "၂", "၃", "၄", "၅", "၆", "၇", "၈", "၉", "၀"),
                listOf("ဍ", "ဣ", "ဋ", "ဠ", "ဏ္ဍ", "ဍ", "ရ", "ဩ", "ဧ", "၏")
            ))

            rows.add(createRow(
                listOf("ဆ", "တ", "န", "မ", "အ", "ပ", "က", "င", "သ", "စ"),
                listOf("ဈ", "ဝ", "၎င်း", "ဤ", "၌", "ဥ", "၍", "ဿ", "ဏ", "ဟ")
            ))

            rows.add(createRow(
                listOf("ေ", "ျ", "ိ", "်", "့", "္", "ြ", "ု", "ူ", "း"),
                listOf("ဗ", "ှ", "ီ", "ွ", "ံ", "ဲ", "ဒ", "ဓ", "ဂ", "ဋ")
            ))

            val row4 = mutableListOf<Key>()
            row4.add(Key("Shift", "Shift", -1, 1.5f))
            row4.addAll(createRow(
                listOf("ဖ", "ထ", "ခ", "လ", "ဘ", "ည", "ာ", "ယ"),
                listOf("ဇ", "ဌ", "ဃ", "ါ", "ဋ္ဌ", "ဉ", "၊", "။"),
                1f
            ))
            row4.add(Key("Delete", "Delete", -2, 1.5f))
            rows.add(row4)

            rows.add(listOf(
                Key("Globe", "Globe", -3, 1.5f),
                Key("?123", "?123", -4, 1f),
                Key("Space", "Space", 32, 5f),
                Key(".", ".", 0, 1f),
                Key("Enter", "Enter", 10, 1.5f)
            ))

        } else {
            if (showNumberRow) {
                rows.add(createRow(
                    listOf("၁", "၂", "၃", "၄", "၅", "၆", "၇", "၈", "၉", "၀"),
                    listOf("၁", "၂", "၃", "၄", "၅", "၆", "၇", "၈", "၉", "၀")
                ))
            }

            val row0 = mutableListOf<Key>()
            row0.add(Key("Emoji", "Emoji", -11, 1.0f))
            row0.addAll(createRow(listOf("ော်", "ိုင်", "င်္", "ုံ"), listOf("<", ">", "/", "ဩ"), 1.0f))
            row0.addAll(createRow(listOf("ြီ", "ရ", "ဂ", "ဝ", "ဟ"), listOf("ဏ", "ဋ္ဌ", "ဏ္ဍ", "ဋ", "ဍ"), 1.0f))
            rows.add(row0)

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
                Key("္", "+", 0, 1f),
                Key("Space", "Space", 32, 5f),
                Key(".", ",", 0, 1f),
                Key("Enter", "Enter", 10, 1.5f)
            ))
        }

        return rows
    }
}