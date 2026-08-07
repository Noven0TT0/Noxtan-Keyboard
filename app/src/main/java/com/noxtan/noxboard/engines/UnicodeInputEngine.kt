package com.noxtan.noxboard.engines

import android.view.inputmethod.InputConnection

class UnicodeInputEngine : MyanmarInputEngine {

    override var activeDoubleTapMap = mapOf<String, String>()
    private var lastTapKey = ""
    private var lastTapTime = 0L
    private var tapCount = 0

    private val DOUBLE_TAP_TIMEOUT = 350L

    private val maukchaConsonants = setOf("ခ", "ဂ", "င", "ဒ", "ပ", "ဝ")

    private val myanmarConsonants = setOf(
        "က", "ခ", "ဂ", "ဃ", "င",
        "စ", "ဆ", "ဇ", "ဈ", "ည",
        "ဋ", "ဌ", "ဍ", "ဎ", "ဏ",
        "တ", "ထ", "ဒ", "ဓ", "န",
        "ပ", "ဖ", "ဗ", "ဘ", "မ",
        "ယ", "ရ", "လ", "ဝ", "သ",
        "ဟ", "ဠ", "အ"
    )

    override fun resetState() {
        lastTapKey = ""
        lastTapTime = 0L
        tapCount = 0
    }

    override fun handleKeyPress(text: String, ic: InputConnection) {
        val currentTime = System.currentTimeMillis()

        val isSameKey = (text == lastTapKey && (currentTime - lastTapTime) < DOUBLE_TAP_TIMEOUT)
        if (isSameKey) {
            tapCount++
        } else {
            lastTapKey = text
            tapCount = 1
        }
        lastTapTime = currentTime

        if (activeDoubleTapMap.containsKey(text)) {
            if (tapCount == 2) {
                val doubleTapText = activeDoubleTapMap[text]!!
                var deleteCount = if (text == "ေ") 2 else 1

                val textBefore2 = ic.getTextBeforeCursor(2, 0)?.toString() ?: ""
                if (text == "ြ" && textBefore2.endsWith("\u200Cြ")) {
                    deleteCount = 2
                }

                ic.deleteSurroundingText(deleteCount, 0)
                ic.commitText(doubleTapText, 1)
                return
            } else if (tapCount == 3) {
                val doubleTapText = activeDoubleTapMap[text]!!
                ic.deleteSurroundingText(doubleTapText.length, 0)
                if (text == "ေ") ic.commitText("\u200Cေ\u200Cေ", 1) else ic.commitText(text + text, 1)
                return
            }
        }

        if (text == "ာ") {
            val textBeforeCursor = ic.getTextBeforeCursor(4, 0)?.toString() ?: ""
            var shouldBeMaukcha = false

            if (textBeforeCursor.isNotEmpty()) {
                var hasMedial = false
                var rootConsonant: String? = null
                val medials = setOf("ျ", "ြ", "ွ", "ှ")

                for (i in textBeforeCursor.length - 1 downTo 0) {
                    val char = textBeforeCursor[i].toString()

                    if (medials.contains(char)) {
                        hasMedial = true
                    } else if (myanmarConsonants.contains(char)) {
                        rootConsonant = char
                        break
                    }
                }

                if (rootConsonant != null && maukchaConsonants.contains(rootConsonant) && !hasMedial) {
                    shouldBeMaukcha = true
                }
            }

            if (shouldBeMaukcha) {
                ic.commitText("ါ", 1)
            } else {
                ic.commitText("ာ", 1)
            }
            return
        }

        if (text == "ြ") {
            val textBefore1 = ic.getTextBeforeCursor(1, 0)?.toString() ?: ""
            if (textBefore1 == "ျ") {
                ic.commitText("\u200Cြ", 1)
                return
            }
        }

        ic.commitText(text, 1)
    }
}