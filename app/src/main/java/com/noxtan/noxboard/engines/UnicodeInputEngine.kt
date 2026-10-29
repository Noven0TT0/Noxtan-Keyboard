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

        val cachedText = ic.getTextBeforeCursor(6, 0)?.toString() ?: ""
        val textBefore1 = if (cachedText.isNotEmpty()) cachedText.last().toString() else ""

        if (activeDoubleTapMap.containsKey(text)) {

            if (tapCount == 2 && textBefore1 != text) {
                tapCount = 1
            }

            if (tapCount == 2) {
                val doubleTapText = activeDoubleTapMap[text]!!
                var deleteCount = if (text == "ေ") 2 else 1

                if (text == "ြ" && cachedText.endsWith("\u200Cြ")) {
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

        if (text == "ိ" || text == "ီ") {
            val misplacedChars = setOf('ု', 'ူ', 'ံ', '့', 'း')
            var count = 0
            for (i in cachedText.length - 1 downTo 0) {
                if (misplacedChars.contains(cachedText[i])) count++ else break
            }
            if (count > 0) {
                val charsToMove = cachedText.takeLast(count)
                ic.deleteSurroundingText(count, 0)
                ic.commitText(text + charsToMove, 1)
                return
            }
        }

        if (text == "ု" || text == "ူ") {
            val misplacedChars = setOf('ံ', '့', 'း')
            var count = 0
            for (i in cachedText.length - 1 downTo 0) {
                if (misplacedChars.contains(cachedText[i])) count++ else break
            }
            if (count > 0) {
                val charsToMove = cachedText.takeLast(count)
                ic.deleteSurroundingText(count, 0)
                ic.commitText(text + charsToMove, 1)
                return
            }
        }

        if (text == "ာ") {
            var shouldBeMaukcha = false

            if (cachedText.isNotEmpty()) {
                var hasMedial = false
                var isStacked = false
                var rootConsonant: String? = null
                val medials = setOf("ျ", "ြ", "ွ", "ှ")

                for (i in cachedText.length - 1 downTo 0) {
                    val char = cachedText[i].toString()

                    if (medials.contains(char)) {
                        hasMedial = true
                    } else if (myanmarConsonants.contains(char)) {
                        rootConsonant = char
                        if (i > 0 && cachedText[i - 1] == '္') {
                            val isKinsi = i > 1 && cachedText[i - 2] == '်'
                            if (!isKinsi) {
                                isStacked = true
                            }
                        }
                        break
                    }
                }

                if (rootConsonant != null && maukchaConsonants.contains(rootConsonant) && !hasMedial && !isStacked) {
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

        val medialsSet = setOf("ျ", "ြ", "ွ", "ှ", "္")
        if (medialsSet.contains(text)) {
            val misplacedVowels = setOf('ိ', 'ီ', 'ု', 'ူ', 'ေ', 'ဲ', 'ံ', '့', 'း', 'ာ', 'ါ')
            var vowelCount = 0
            for (i in cachedText.length - 1 downTo 0) {
                if (misplacedVowels.contains(cachedText[i])) vowelCount++ else break
            }
            if (vowelCount > 0) {
                val vowelsToMove = cachedText.takeLast(vowelCount)
                ic.deleteSurroundingText(vowelCount, 0)
                ic.commitText(text + vowelsToMove, 1)
                return
            }
        }

        if (text == "ြ") {
            if (textBefore1 == "ျ") {
                ic.commitText("\u200Cြ", 1)
                return
            }
        }

        ic.commitText(text, 1)
    }
}