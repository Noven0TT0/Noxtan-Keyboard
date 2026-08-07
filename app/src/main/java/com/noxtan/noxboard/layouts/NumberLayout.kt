package com.noxtan.noxboard.layouts

import com.noxtan.noxboard.Key

object NumberLayout : BaseLayout() {
    fun getLayout(): List<List<Key>> {
        val weight = 2.5f
        return listOf(
            createRow(listOf("1", "2", "3", "-"), listOf("1", "2", "3", "-"), weight),
            createRow(listOf("4", "5", "6", ","), listOf("4", "5", "6", ","), weight),
            createRow(listOf("7", "8", "9", "."), listOf("7", "8", "9", "."), weight),
            listOf(
                Key("*", "*", 0, weight),
                Key("0", "0", 0, weight),
                Key("#", "#", 0, weight),
                Key("Space", "Space", 32, weight)
            ),
            listOf(
                Key("🌐", "🌐", -3, weight),
                Key("", "", -100, weight),
                Key("⌫", "⌫", -2, weight),
                Key("Enter", "Enter", 10, weight)
            )
        )
    }
}