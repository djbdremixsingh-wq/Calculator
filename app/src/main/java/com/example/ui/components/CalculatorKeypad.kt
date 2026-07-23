package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CalculatorKeypad(
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val buttonRows = listOf(
        listOf("C", "( )", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("+/-", "0", ".", "=")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        buttonRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { label ->
                    CalculatorButton(
                        label = label,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onKeyClick(label)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .testTag("key_$label")
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    textColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 80),
        label = "button_scale"
    )

    val isDark = MaterialTheme.colorScheme.background == DarkBackground

    val defaultBg = when (label) {
        "=" -> EqualKeyBg
        "C" -> if (isDark) ClearKeyBg else Color(0xFFFEE2E2)
        "÷", "×", "-", "+", "%" -> if (isDark) OperatorKeyBg else LightOperatorKeyBg
        "( )", "+/-", "⌫" -> if (isDark) ActionKeyBg else Color(0xFFE0F2FE)
        else -> if (isDark) NumberKeyBg else LightNumberKeyBg
    }

    val defaultText = when (label) {
        "=" -> EqualKeyText
        "C" -> if (isDark) ClearKeyText else Color(0xFFDC2626)
        "÷", "×", "-", "+", "%" -> if (isDark) OperatorKeyText else LightOperatorKeyText
        "( )", "+/-", "⌫" -> if (isDark) ActionKeyText else Color(0xFF0369A1)
        else -> if (isDark) NumberKeyText else LightNumberKeyText
    }

    val bg = backgroundColor ?: defaultBg
    val textC = textColor ?: defaultText

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = textC),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = when {
                label.length > 2 -> 16.sp
                label in listOf("÷", "×", "-", "+", "=") -> 26.sp
                else -> 22.sp
            },
            fontWeight = if (label in listOf("=", "C", "÷", "×", "-", "+")) FontWeight.Bold else FontWeight.Medium,
            color = textC
        )
    }
}
