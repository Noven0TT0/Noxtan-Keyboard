package com.noxtan.noxboard.layouts

import com.noxtan.noxboard.Key
import com.noxtan.noxboard.KeyboardMode

object NumpadLayout {
    fun getLayout(mode: KeyboardMode): List<Key> {
        val numLabels = when (mode) {
            KeyboardMode.THAI -> listOf(
                "+", "๑", "๒", "๓", ":",
                "-", "๔", "๕", "๖", "บาท",
                "*", "๗", "๘", "๙", "⌫",
                "/", ",", "๐", ".", "▼"
            )
            KeyboardMode.MYANMAR -> listOf(
                "နာရီ", "၁", "၂", "၃", "သိန်း",
                "မိနစ်", "၄", "၅", "၆", "သောင်း",
                "ရက်", "၇", "၈", "၉", "⌫",
                "ခွဲ", "ထောင်", "၀", "ရာ", "▼"
            )
            else -> listOf(
                "+", "1", "2", "3", ":",
                "-", "4", "5", "6", "am",
                "*", "7", "8", "9", "⌫",
                "/", ",", "0", ".", "▼"
            )
        }
        val numCodes = listOf(
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, -2,
            0, 0, 0, 0, -10
        )
        return numLabels.indices.map { i -> Key(numLabels[i], numLabels[i], numCodes[i], 1f) }
    }
}