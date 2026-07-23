package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkBackground

@Composable
fun ScientificKeypad(
    isDegreeMode: Boolean,
    onToggleDegreeMode: () -> Unit,
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val isDark = MaterialTheme.colorScheme.background == DarkBackground

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: DEG/RAD toggle, sin, cos, tan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                label = if (isDegreeMode) "DEG" else "RAD",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleDegreeMode()
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_deg_rad")
            )
            listOf("sin", "cos", "tan").forEach { label ->
                CalculatorButton(
                    label = "$label(",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onKeyClick("$label(")
                    },
                    modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_$label")
                )
            }
        }

        // Row 2: asin, acos, atan, backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("asin", "acos", "atan").forEach { label ->
                CalculatorButton(
                    label = "$label(",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onKeyClick("$label(")
                    },
                    modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_$label")
                )
            }
            CalculatorButton(
                label = "⌫",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("⌫")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_backspace")
            )
        }

        // Row 3: log, ln, x^y, x²
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                label = "log(",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("log(")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_log")
            )
            CalculatorButton(
                label = "ln(",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("ln(")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_ln")
            )
            CalculatorButton(
                label = "^",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("^")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_power")
            )
            CalculatorButton(
                label = "^2",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("^2")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_sq")
            )
        }

        // Row 4: √, ∛, e^x, 1/x
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                label = "sqrt(",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("sqrt(")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_sqrt")
            )
            CalculatorButton(
                label = "cbrt(",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("cbrt(")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_cbrt")
            )
            CalculatorButton(
                label = "e^",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("e^")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_exp")
            )
            CalculatorButton(
                label = "1/",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("1/(")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_inv")
            )
        }

        // Row 5: π, e, n!, abs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                label = "π",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("π")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_pi")
            )
            CalculatorButton(
                label = "e",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("e")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_e")
            )
            CalculatorButton(
                label = "!",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("!")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_fact")
            )
            CalculatorButton(
                label = "mod",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKeyClick("mod")
                },
                modifier = Modifier.weight(1f).aspectRatio(1.4f).testTag("key_mod")
            )
        }
    }
}
