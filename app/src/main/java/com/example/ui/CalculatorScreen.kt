package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.ConvertersView
import com.example.ui.components.HistorySheet
import com.example.ui.components.ScientificKeypad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isDegreeMode by viewModel.isDegreeMode.collectAsStateWithLifecycle()
    val expression by viewModel.expression.collectAsStateWithLifecycle()
    val liveResult by viewModel.liveResult.collectAsStateWithLifecycle()
    val memoryValue by viewModel.memoryValue.collectAsStateWithLifecycle()

    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val favoriteList by viewModel.favoriteHistoryList.collectAsStateWithLifecycle()

    // Converter States
    val converterCategory by viewModel.converterCategory.collectAsStateWithLifecycle()
    val converterFromValue by viewModel.converterFromValue.collectAsStateWithLifecycle()
    val converterFromUnitIndex by viewModel.converterFromUnitIndex.collectAsStateWithLifecycle()
    val converterToUnitIndex by viewModel.converterToUnitIndex.collectAsStateWithLifecycle()
    val converterResult by viewModel.converterResult.collectAsStateWithLifecycle()

    // Discount States
    val discountPrice by viewModel.discountPrice.collectAsStateWithLifecycle()
    val discountPercent by viewModel.discountPercent.collectAsStateWithLifecycle()
    val discountTax by viewModel.discountTax.collectAsStateWithLifecycle()
    val discountResult by viewModel.discountResult.collectAsStateWithLifecycle()

    // Tip States
    val tipBill by viewModel.tipBill.collectAsStateWithLifecycle()
    val tipPercent by viewModel.tipPercent.collectAsStateWithLifecycle()
    val tipPeople by viewModel.tipPeople.collectAsStateWithLifecycle()
    val tipResult by viewModel.tipResult.collectAsStateWithLifecycle()

    // BMI States
    val bmiWeight by viewModel.bmiWeight.collectAsStateWithLifecycle()
    val bmiHeight by viewModel.bmiHeight.collectAsStateWithLifecycle()
    val bmiResult by viewModel.bmiResult.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "CALC",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text("Calculator", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 8.dp
            ) {
                CalculatorMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    val icon = when (mode) {
                        CalculatorMode.STANDARD -> Icons.Default.Calculate
                        CalculatorMode.SCIENTIFIC -> Icons.Default.Science
                        CalculatorMode.CONVERTERS -> Icons.Default.SquareFoot
                        CalculatorMode.HISTORY -> Icons.Default.History
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setMode(mode) },
                        icon = { Icon(icon, contentDescription = mode.label) },
                        label = { Text(mode.label) },
                        modifier = Modifier.testTag("nav_${mode.name.lowercase()}")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Display is shown for Standard & Scientific modes
            if (selectedMode == CalculatorMode.STANDARD || selectedMode == CalculatorMode.SCIENTIFIC) {
                CalculatorDisplay(
                    expression = expression,
                    liveResult = liveResult,
                    isDegreeMode = isDegreeMode,
                    memoryValue = memoryValue,
                    onBackspace = { viewModel.onKeyInput("⌫") },
                    onMemoryClear = { viewModel.memoryClear() },
                    onMemoryRecall = { viewModel.memoryRecall() },
                    onMemoryAdd = { viewModel.memoryAdd() },
                    onMemorySubtract = { viewModel.memorySubtract() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Body Content based on Mode Tab
            AnimatedContent(
                targetState = selectedMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "mode_transition",
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (selectedMode == CalculatorMode.CONVERTERS || selectedMode == CalculatorMode.HISTORY) Modifier.weight(1f) else Modifier)
            ) { mode ->
                when (mode) {
                    CalculatorMode.STANDARD -> {
                        CalculatorKeypad(
                            onKeyClick = { viewModel.onKeyInput(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    CalculatorMode.SCIENTIFIC -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScientificKeypad(
                                isDegreeMode = isDegreeMode,
                                onToggleDegreeMode = { viewModel.toggleDegreeMode() },
                                onKeyClick = { viewModel.onKeyInput(it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            CalculatorKeypad(
                                onKeyClick = { viewModel.onKeyInput(it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    CalculatorMode.CONVERTERS -> {
                        ConvertersView(
                            category = converterCategory,
                            fromValue = converterFromValue,
                            fromUnitIndex = converterFromUnitIndex,
                            toUnitIndex = converterToUnitIndex,
                            resultValue = converterResult,
                            onCategorySelected = { viewModel.setConverterCategory(it) },
                            onFromValueChanged = { viewModel.updateConverterFromValue(it) },
                            onFromUnitIndexChanged = { viewModel.setConverterFromUnit(it) },
                            onToUnitIndexChanged = { viewModel.setConverterToUnit(it) },
                            discountPrice = discountPrice,
                            discountPercent = discountPercent,
                            discountTax = discountTax,
                            discountResult = discountResult,
                            onDiscountChanged = { p, d, t -> viewModel.updateDiscountInputs(p, d, t) },
                            tipBill = tipBill,
                            tipPercent = tipPercent,
                            tipPeople = tipPeople,
                            tipResult = tipResult,
                            onTipChanged = { b, t, p -> viewModel.updateTipInputs(b, t, p) },
                            bmiWeight = bmiWeight,
                            bmiHeight = bmiHeight,
                            bmiResult = bmiResult,
                            onBmiChanged = { w, h -> viewModel.updateBmiInputs(w, h) }
                        )
                    }
                    CalculatorMode.HISTORY -> {
                        HistorySheet(
                            historyList = historyList,
                            favoriteList = favoriteList,
                            onSelectEntry = { viewModel.loadHistoryEntry(it) },
                            onDeleteEntry = { viewModel.deleteHistoryItem(it) },
                            onToggleFavorite = { id, fav -> viewModel.toggleFavoriteHistory(id, fav) },
                            onClearAll = { viewModel.clearHistory() }
                        )
                    }
                }
            }
        }
    }
}
