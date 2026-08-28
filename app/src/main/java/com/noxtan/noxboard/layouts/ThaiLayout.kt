package com.noxtan.noxboard.layouts

import com.noxtan.noxboard.Key

object ThaiLayout : BaseLayout() {
    fun getLayout(showNumberRow: Boolean): List<List<Key>> {
        val rows = mutableListOf<List<Key>>()

        if (showNumberRow) {
            rows.add(createRow(
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                listOf("๑", "๒", "๓", "๔", "๕", "๖", "๗", "๘", "๙", "๐")
            ))
        }

        rows.add(createRow(
            listOf("ภ", "ถ", "ุ", "ึ", "ค", "ต", "จ", "ข", "ช", "ๆ"),
            listOf("๑", "๒", "๓", "๔", "ู", "฿", "๕", "๖", "๗", "๘")
        ))

        rows.add(createRow(
            listOf("ไ", "ำ", "พ", "ะ", "ั", "ี", "ร", "น", "ย", "บ", "ล"),
            listOf("๙", "๐", "\"", "ฎ", "ฑ", "ธ", "ํ", "๊", "ณ", "ฯ", "ญ")
        ))

        rows.add(createRow(
            listOf("ฟ", "ห", "ก", "ด", "เ", "้", "า", "ส", "ว", "ง"),
            listOf("ฐ", "ฅ", "ฤ", "ฆ", "ฏ", "โ", "ฌ", "็", "๋", "ษ")
        ))

        val row4 = mutableListOf<Key>()
        row4.add(Key("Shift", "Shift", -1, 1.5f))
        row4.addAll(createRow(
            listOf("ผ", "ป", "แ", "อ", "ิ", "ื", "ท", "ม", "ใ", "ฝ"),
            listOf("ศ", "ซ", "ฉ", "ฮ", "ฺ", "์", "?", "ฒ", "ฬ", "ฦ"),
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

        return rows
    }
}