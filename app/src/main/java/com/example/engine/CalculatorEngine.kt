package com.example.engine

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

class CalculatorEngine {

    var isDegreeMode: Boolean = true

    fun evaluate(expression: String): String {
        val sanitized = sanitizeExpression(expression)
        if (sanitized.isEmpty()) return ""

        return try {
            val tokens = tokenize(sanitized)
            val result = parseExpression(tokens)
            formatResult(result)
        } catch (e: ArithmeticException) {
            "Can't divide by zero"
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun sanitizeExpression(expr: String): String {
        var s = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "PI")
            .replace("mod", "%")

        // Auto-close missing trailing parentheses
        var openCount = s.count { it == '(' }
        var closeCount = s.count { it == ')' }
        while (openCount > closeCount) {
            s += ")"
            closeCount++
        }

        // Handle implicit multiplication e.g., 2(3), 5PI, 2sin(30), (2)(3)
        s = s.replace(Regex("(\\d|\\)|PI|e)(?=\\(|sin|cos|tan|asin|acos|atan|log|ln|sqrt|cbrt|PI|e)"), "$1*")
        s = s.replace(Regex("(\\))(?=\\d)"), "$1*")

        return s
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val length = expr.length

        while (i < length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c in "+-*/^%()!" -> {
                    // Handle negative numbers or unary minus vs binary minus
                    if (c == '-') {
                        val prevToken = tokens.lastOrNull()
                        val isUnary = prevToken == null || prevToken in "+-*/^%(" || prevToken == "sin" ||
                                prevToken == "cos" || prevToken == "tan" || prevToken == "log" || prevToken == "ln" ||
                                prevToken == "sqrt" || prevToken == "cbrt" || prevToken == "asin" || prevToken == "acos" || prevToken == "atan"
                        if (isUnary) {
                            tokens.add("neg")
                            i++
                            continue
                        }
                    }
                    tokens.add(c.toString())
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < length && expr[i].isLetter()) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                else -> i++
            }
        }
        return tokens
    }

    private fun parseExpression(tokens: List<String>): Double {
        var pos = 0

        fun peek(): String? = tokens.getOrNull(pos)
        fun consume(): String = tokens[pos++]

        fun parseFactor(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end")

            return when {
                token == "neg" -> {
                    consume()
                    -parseFactor()
                }
                token == "+" -> {
                    consume()
                    parseFactor()
                }
                token == "(" -> {
                    consume()
                    val result = parseExpression(tokens) // recursive parse
                    if (peek() == ")") consume()
                    result
                }
                token == "PI" -> {
                    consume()
                    Math.PI
                }
                token == "e" -> {
                    consume()
                    Math.E
                }
                token in listOf("sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "sqrt", "cbrt") -> {
                    val func = consume()
                    val arg = if (peek() == "(") parseFactor() else parseFactor()
                    when (func) {
                        "sin" -> sin(if (isDegreeMode) Math.toRadians(arg) else arg)
                        "cos" -> cos(if (isDegreeMode) Math.toRadians(arg) else arg)
                        "tan" -> tan(if (isDegreeMode) Math.toRadians(arg) else arg)
                        "asin" -> {
                            val rad = asin(arg)
                            if (isDegreeMode) Math.toDegrees(rad) else rad
                        }
                        "acos" -> {
                            val rad = acos(arg)
                            if (isDegreeMode) Math.toDegrees(rad) else rad
                        }
                        "atan" -> {
                            val rad = atan(arg)
                            if (isDegreeMode) Math.toDegrees(rad) else rad
                        }
                        "log" -> log10(arg)
                        "ln" -> ln(arg)
                        "sqrt" -> sqrt(arg)
                        "cbrt" -> cbrt(arg)
                        else -> 0.0
                    }
                }
                else -> {
                    val number = token.toDoubleOrNull()
                        ?: throw IllegalArgumentException("Invalid number: $token")
                    consume()
                    number
                }
            }
        }

        fun parsePostfix(): Double {
            var value = parseFactor()
            while (true) {
                when (peek()) {
                    "!" -> {
                        consume()
                        value = factorial(value)
                    }
                    "%" -> {
                        consume()
                        value /= 100.0
                    }
                    else -> break
                }
            }
            return value
        }

        fun parsePower(): Double {
            var base = parsePostfix()
            if (peek() == "^") {
                consume()
                val exponent = parseFactor() // Right-associative exponent
                base = base.pow(exponent)
            }
            return base
        }

        fun parseTerm(): Double {
            var left = parsePower()
            while (true) {
                when (peek()) {
                    "*" -> {
                        consume()
                        left *= parsePower()
                    }
                    "/" -> {
                        consume()
                        val right = parsePower()
                        if (right == 0.0) throw ArithmeticException("Division by zero")
                        left /= right
                    }
                    "%" -> {
                        consume()
                        val right = parsePower()
                        left %= right
                    }
                    else -> break
                }
            }
            return left
        }

        fun parseAdditive(): Double {
            var left = parseTerm()
            while (true) {
                when (peek()) {
                    "+" -> {
                        consume()
                        left += parseTerm()
                    }
                    "-" -> {
                        consume()
                        left -= parseTerm()
                    }
                    else -> break
                }
            }
            return left
        }

        return parseAdditive()
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) throw IllegalArgumentException("Factorial undefined for non-integers")
        var result = 1.0
        for (i in 2..n.toInt()) {
            result *= i
        }
        return result
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        // Handle exact integer or float formatting
        if (value % 1.0 == 0.0 && abs(value) < 1e12) {
            val longVal = value.toLong()
            val symbols = DecimalFormatSymbols(Locale.US)
            val formatter = DecimalFormat("#,###", symbols)
            return formatter.format(longVal)
        }

        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0.########", symbols)
        val formatted = df.format(value)
        return if (formatted == "-0") "0" else formatted
    }
}
