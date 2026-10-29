package com.noxtan.noxboard.engines

import java.math.BigDecimal
import java.math.RoundingMode

data class MathResult(
    val rawExpression: String,
    val resultOnly: String,
    val fullEquation: String,
    val expressionLength: Int
)

object MathEngine {
    private val mathExprRegex = Regex("([0-9၀-၉,]+(?:\\.[0-9၀-၉]+)?\\s*[+\\-*/×÷xX]\\s*)+[0-9၀-၉,]+(?:\\.[0-9၀-၉]+)?%?$")
    private val percentRegex = Regex("^([0-9.]+)([+\\-*/])([0-9.]+)%$")
    private val myanmarToEngDigits = mapOf(
        '၀' to '0', '၁' to '1', '၂' to '2', '၃' to '3', '၄' to '4',
        '၅' to '5', '၆' to '6', '၇' to '7', '၈' to '8', '၉' to '9'
    )
    private val engToMyanmarDigits = mapOf(
        '0' to '၀', '1' to '၁', '2' to '၂', '3' to '၃', '4' to '၄',
        '5' to '၅', '6' to '၆', '7' to '၇', '8' to '၈', '9' to '၉'
    )

    fun evaluate(text: String): MathResult? {
        if (text.isBlank()) return null

        val match = mathExprRegex.find(text.trimEnd()) ?: return null
        val matchedExpr = match.value.trim()

        val isMyanmar = matchedExpr.any { it in '၀'..'၉' }

        val normalized = matchedExpr.mapNotNull { ch ->
            when (ch) {
                ',', ' ' -> null
                '×', 'x', 'X' -> '*'
                '÷' -> '/'
                else -> myanmarToEngDigits[ch] ?: ch
            }
        }.joinToString("")

        try {
            val percentMatch = percentRegex.find(normalized)
            val resultValue: BigDecimal = if (percentMatch != null) {
                val (aStr, op, bStr) = percentMatch.destructured
                val a = BigDecimal(aStr)
                val b = BigDecimal(bStr)
                val percentVal = a.multiply(b).divide(BigDecimal("100"), 6, RoundingMode.HALF_UP)
                when (op) {
                    "+" -> a.add(percentVal)
                    "-" -> a.subtract(percentVal)
                    "*" -> percentVal
                    "/" -> {
                        if (percentVal.compareTo(BigDecimal.ZERO) == 0) return null
                        a.divide(percentVal, 6, RoundingMode.HALF_UP)
                    }
                    else -> return null
                }
            } else if (normalized.endsWith("%")) {
                val numStr = normalized.removeSuffix("%")
                val num = BigDecimal(numStr)
                num.divide(BigDecimal("100"), 6, RoundingMode.HALF_UP)
            } else {
                evalSimpleExpression(normalized) ?: return null
            }

            val formattedNum = resultValue.stripTrailingZeros().toPlainString()
            val finalFormatted = if (formattedNum.contains(".")) {
                val parts = formattedNum.split(".")
                val dec = if (parts[1].length > 4) parts[1].take(4) else parts[1]
                "${parts[0]}.$dec"
            } else {
                formattedNum
            }

            val finalResultOnly = if (isMyanmar) {
                finalFormatted.map { engToMyanmarDigits[it] ?: it }.joinToString("")
            } else {
                finalFormatted
            }

            val finalFullEq = "$matchedExpr = $finalResultOnly"

            return MathResult(
                rawExpression = matchedExpr,
                resultOnly = finalResultOnly,
                fullEquation = finalFullEq,
                expressionLength = match.value.length
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun evalSimpleExpression(expr: String): BigDecimal? {
        try {
            val tokens = mutableListOf<String>()
            var currentNumber = StringBuilder()

            for (i in expr.indices) {
                val c = expr[i]
                if (c.isDigit() || c == '.') {
                    currentNumber.append(c)
                } else if (c in listOf('+', '-', '*', '/')) {
                    if (currentNumber.isNotEmpty()) {
                        tokens.add(currentNumber.toString())
                        currentNumber = StringBuilder()
                    }
                    tokens.add(c.toString())
                }
            }
            if (currentNumber.isNotEmpty()) {
                tokens.add(currentNumber.toString())
            }

            if (tokens.size < 3) return null

            var i = 0
            while (i < tokens.size) {
                if (tokens[i] == "*" || tokens[i] == "/") {
                    val op = tokens[i]
                    val left = BigDecimal(tokens[i - 1])
                    val right = BigDecimal(tokens[i + 1])
                    val res = if (op == "*") {
                        left.multiply(right)
                    } else {
                        if (right.compareTo(BigDecimal.ZERO) == 0) return null
                        left.divide(right, 6, RoundingMode.HALF_UP)
                    }
                    tokens[i - 1] = res.toString()
                    tokens.removeAt(i)
                    tokens.removeAt(i)
                    i--
                }
                i++
            }

            var result = BigDecimal(tokens[0])
            i = 1
            while (i < tokens.size) {
                val op = tokens[i]
                val nextNum = BigDecimal(tokens[i + 1])
                result = if (op == "+") {
                    result.add(nextNum)
                } else {
                    result.subtract(nextNum)
                }
                i += 2
            }

            return result
        } catch (e: Exception) {
            return null
        }
    }
}