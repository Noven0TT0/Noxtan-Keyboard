package com.noxtan.noxboard.engines

import android.view.inputmethod.InputConnection

class VisualSmartInputEngine : MyanmarInputEngine {
    override var activeDoubleTapMap = mapOf<String, String>()

    private var lastTapKey = ""
    private var lastTapTime = 0L
    private var tapCount = 0
    private val DOUBLE_TAP_TIMEOUT = 350L
    private var wasLastTapIgnored = false

    private val maukchaConsonants = setOf("ခ", "ဂ", "င", "ဒ", "ပ", "ဝ")

    private val myanmarConsonants = setOf(
        "က", "ခ", "ဂ", "ဃ", "င", "စ", "ဆ", "ဇ", "ဈ", "ည", "ဋ", "ဌ", "ဍ", "ဎ", "ဏ",
        "တ", "ထ", "ဒ", "ဓ", "န", "ပ", "ဖ", "ဗ", "ဘ", "မ", "ယ", "ရ", "လ", "ဝ", "သ", "ဟ", "ဠ", "အ"
    )

    private val autoCorrectMap = mapOf(
        "ဟူတ်" to "ဟုတ်", "ဟုတိ" to "ဟုတ်", "တဘ်" to "တယ်", "တယိ" to "တယ်",
        "တယ်ု" to "တယို", "ထငိ" to "ထင်", "ထင်ု" to "ထငို", "မောငိ့" to "မောင့်",
        "မငိး" to "မင်း", "ထမငိး" to "ထမင်း", "က်ု" to "ကို", "မဂ်လာပါ" to "မင်္ဂလာပါ",
        "အောိ" to "အော်", "အြ" to "ဪ", "အံ့သြ" to "အံ့ဩ", "အံ့အြ" to "အံ့ဩ",
        "ကြိး" to "ကြီး", "ကြညိ့" to "ကြည့်"
    )

    override fun resetState() {
        lastTapKey = ""
        lastTapTime = 0L
        tapCount = 0
        wasLastTapIgnored = false
    }

    override fun handleKeyPress(text: String, ic: InputConnection) {
        val currentTime = System.currentTimeMillis()
        val isSameKey = (text == lastTapKey && (currentTime - lastTapTime) < DOUBLE_TAP_TIMEOUT)
        if (isSameKey) tapCount++ else { lastTapKey = text; tapCount = 1 }
        lastTapTime = currentTime

        val cachedText = ic.getTextBeforeCursor(6, 0)?.toString() ?: ""
        val textBefore1 = if (cachedText.isNotEmpty()) cachedText.last().toString() else ""

        if (activeDoubleTapMap.containsKey(text)) {

            if (tapCount == 2 && textBefore1 != text) {
                tapCount = 1
            }

            if (tapCount == 2) {
                val doubleTapText = activeDoubleTapMap[text]!!

                if (!wasLastTapIgnored) {
                    if (text == "ေ" && cachedText.endsWith("\u200Cေ")) {
                        ic.deleteSurroundingText(2, 0)
                    } else if (text == "ြ" && cachedText.endsWith("\u200Cြ")) {
                        ic.deleteSurroundingText(2, 0)
                    } else if (cachedText.endsWith(text + "ေဲ")) {
                        ic.deleteSurroundingText(text.length + 2, 0)
                        ic.commitText("ေဲ", 1)
                    } else if (cachedText.endsWith(text + "ေ")) {
                        ic.deleteSurroundingText(text.length + 1, 0)
                        ic.commitText("ေ", 1)
                    } else if (cachedText.endsWith(text)) {
                        ic.deleteSurroundingText(text.length, 0)
                    } else {
                        ic.deleteSurroundingText(1, 0)
                    }
                }

                resetState()
                handleKeyPress(doubleTapText, ic)
                return
            } else if (tapCount == 3) {
                val doubleTapText = activeDoubleTapMap[text]!!
                val deleteCount = doubleTapText.length

                val textBefore = cachedText.takeLast(deleteCount)
                if (textBefore == doubleTapText) {
                    ic.deleteSurroundingText(deleteCount, 0)
                }

                if (text == "ေ") ic.commitText("\u200Cေ\u200Cေ", 1) else ic.commitText(text + text, 1)
                return
            }
        }

        wasLastTapIgnored = false

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

        if (text == "ေ") {
            val textBefore1 = if (cachedText.isNotEmpty()) cachedText.last().toString() else ""

            if (cachedText.endsWith("င်္" + textBefore1) && myanmarConsonants.contains(textBefore1)) {
                ic.deleteSurroundingText(4, 0)
                ic.commitText("င်္" + textBefore1 + "ေ", 1)
                return
            }
            ic.commitText("\u200Cေ", 1)
            return
        }

        if (text == "င်္") {
            val textBefore1 = if (cachedText.isNotEmpty()) cachedText.last().toString() else ""

            if (cachedText.endsWith("\u200Cေ" + textBefore1) && myanmarConsonants.contains(textBefore1)) {
                ic.deleteSurroundingText(3, 0)
                ic.commitText("င်္" + textBefore1 + "ေ", 1)
                return
            } else if (cachedText.endsWith("ေ" + textBefore1) && myanmarConsonants.contains(textBefore1)) {
                ic.deleteSurroundingText(2, 0)
                ic.commitText("င်္" + textBefore1 + "ေ", 1)
                return
            } else if (myanmarConsonants.contains(textBefore1)) {
                ic.deleteSurroundingText(1, 0)
                ic.commitText("င်္" + textBefore1, 1)
                return
            }
            ic.commitText(text, 1)
            return
        }

        val medialsSet = setOf("ျ", "ြ", "ွ", "ှ", "္")
        if (medialsSet.contains(text)) {

            val misplacedVowels = setOf('ိ', 'ီ', 'ု', 'ူ', 'ဲ', 'ံ', '့', 'း', 'ာ', 'ါ')
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

            if (cachedText.endsWith("\u200Cေ") && setOf("ျ", "ြ", "ွ").contains(text)) {
                wasLastTapIgnored = true
                return
            }

            var stringBeforeSuffix = cachedText
            var suffix = ""
            var deleteLen = 0

            if (cachedText.endsWith("ေဲ")) {
                stringBeforeSuffix = cachedText.dropLast(2)
                suffix = "ေဲ"
                deleteLen = 2
            } else if (cachedText.endsWith("ေ") && !cachedText.endsWith("\u200Cေ")) {
                stringBeforeSuffix = cachedText.dropLast(1)
                suffix = "ေ"
                deleteLen = 1
            }

            var hasConsonant = false
            var cluster = ""

            for (i in stringBeforeSuffix.length - 1 downTo 0) {
                val c = stringBeforeSuffix[i].toString()
                if (myanmarConsonants.contains(c) || medialsSet.contains(c)) {
                    cluster = c + cluster
                    if (myanmarConsonants.contains(c)) {
                        hasConsonant = true
                        break
                    }
                } else {
                    break
                }
            }

            if (hasConsonant) {
                if (cluster.contains(text)) {
                    wasLastTapIgnored = true
                    return
                }

                val getMedialLevel = { m: String ->
                    when (m) {
                        "ျ", "ြ" -> 1
                        "ွ" -> 2
                        "ှ" -> 3
                        else -> 0
                    }
                }

                val newLevel = getMedialLevel(text)
                var maxClusterLevel = 0
                for (char in cluster) {
                    val lvl = getMedialLevel(char.toString())
                    if (lvl > maxClusterLevel) maxClusterLevel = lvl
                }

                if (suffix.isNotEmpty()) {
                    if (newLevel > 0 && newLevel <= maxClusterLevel) {
                        ic.commitText(text, 1)
                        return
                    } else {
                        ic.deleteSurroundingText(cluster.length + deleteLen, 0)
                        ic.commitText(cluster + text + suffix, 1)
                        return
                    }
                } else {
                    ic.commitText(text, 1)
                    return
                }
            } else {
                if (suffix.isNotEmpty()) {
                    if (setOf("ျ", "ြ", "ွ").contains(text)) {
                        wasLastTapIgnored = true
                        return
                    }

                    val charBeforeSuffix = if (stringBeforeSuffix.isNotEmpty()) stringBeforeSuffix.last().toString() else ""
                    if (charBeforeSuffix != text) {
                        ic.deleteSurroundingText(deleteLen, 0)
                        ic.commitText(text + suffix, 1)
                    } else {
                        ic.commitText(text, 1)
                    }
                    return
                } else {
                    ic.commitText(text, 1)
                    return
                }
            }
        }

        if (myanmarConsonants.contains(text)) {

            if (cachedText.endsWith("\u200Cေင်္") || cachedText.endsWith("င်္\u200Cေ")) {
                ic.deleteSurroundingText(5, 0)
                ic.commitText("င်္" + text + "ေ", 1)
                return
            } else if (cachedText.endsWith("ေင်္") || cachedText.endsWith("င်္ေ")) {
                ic.deleteSurroundingText(4, 0)
                ic.commitText("င်္" + text + "ေ", 1)
                return
            } else if (cachedText.endsWith("င်္")) {
                ic.deleteSurroundingText(3, 0)
                ic.commitText("င်္" + text, 1)
                return
            }

            if (cachedText.endsWith("\u200Cြေ")) {
                ic.deleteSurroundingText(3, 0)
                ic.commitText(text + "ြေ", 1)
                return
            }

            if (cachedText.endsWith("\u200Cေ")) {
                ic.deleteSurroundingText(2, 0)
                ic.commitText(text + "ေ", 1)
                return
            }
        }

        if (text == "်") {
            if (cachedText.endsWith("ငေ")) {
                ic.deleteSurroundingText(2, 0)
                ic.commitText("င်ေ", 1)
                return
            }
        }

        if (text == "္") {
            if (cachedText.endsWith("င်ေ")) {
                ic.deleteSurroundingText(3, 0)
                ic.commitText("င်္ေ", 1)
                return
            }
        }

        ic.commitText(text, 1)
    }
}