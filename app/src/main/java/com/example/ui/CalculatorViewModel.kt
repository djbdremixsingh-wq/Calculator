package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.CalculationHistory
import com.example.data.repository.HistoryRepository
import com.example.engine.CalculatorEngine
import com.example.engine.UnitConverterEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CalculatorMode(val label: String) {
    STANDARD("Standard"),
    SCIENTIFIC("Scientific"),
    CONVERTERS("Tools"),
    HISTORY("History")
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoryRepository
    val engine = CalculatorEngine()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HistoryRepository(database.historyDao())
    }

    val historyList: StateFlow<List<CalculationHistory>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteHistoryList: StateFlow<List<CalculationHistory>> = repository.favoriteHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI States
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _liveResult = MutableStateFlow("")
    val liveResult: StateFlow<String> = _liveResult.asStateFlow()

    private val _selectedMode = MutableStateFlow(CalculatorMode.STANDARD)
    val selectedMode: StateFlow<CalculatorMode> = _selectedMode.asStateFlow()

    private val _isDegreeMode = MutableStateFlow(true)
    val isDegreeMode: StateFlow<Boolean> = _isDegreeMode.asStateFlow()

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _lastEvaluated = MutableStateFlow(false)

    // Unit Converter State
    private val _converterCategory = MutableStateFlow(UnitConverterEngine.UnitCategory.LENGTH)
    val converterCategory = _converterCategory.asStateFlow()

    private val _converterFromValue = MutableStateFlow("1")
    val converterFromValue = _converterFromValue.asStateFlow()

    private val _converterFromUnitIndex = MutableStateFlow(1) // km
    val converterFromUnitIndex = _converterFromUnitIndex.asStateFlow()

    private val _converterToUnitIndex = MutableStateFlow(0) // m
    val converterToUnitIndex = _converterToUnitIndex.asStateFlow()

    private val _converterResult = MutableStateFlow("1,000")
    val converterResult = _converterResult.asStateFlow()

    // Discount State
    private val _discountPrice = MutableStateFlow("100")
    val discountPrice = _discountPrice.asStateFlow()

    private val _discountPercent = MutableStateFlow("20")
    val discountPercent = _discountPercent.asStateFlow()

    private val _discountTax = MutableStateFlow("0")
    val discountTax = _discountTax.asStateFlow()

    private val _discountResult = MutableStateFlow(
        UnitConverterEngine.calculateDiscount(100.0, 20.0, 0.0)
    )
    val discountResult = _discountResult.asStateFlow()

    // Tip Split State
    private val _tipBill = MutableStateFlow("120")
    val tipBill = _tipBill.asStateFlow()

    private val _tipPercent = MutableStateFlow("15")
    val tipPercent = _tipPercent.asStateFlow()

    private val _tipPeople = MutableStateFlow("4")
    val tipPeople = _tipPeople.asStateFlow()

    private val _tipResult = MutableStateFlow(
        UnitConverterEngine.calculateTipSplit(120.0, 15.0, 4)
    )
    val tipResult = _tipResult.asStateFlow()

    // BMI State
    private val _bmiWeight = MutableStateFlow("70")
    val bmiWeight = _bmiWeight.asStateFlow()

    private val _bmiHeight = MutableStateFlow("175")
    val bmiHeight = _bmiHeight.asStateFlow()

    private val _bmiResult = MutableStateFlow(
        UnitConverterEngine.calculateBmi(70.0, 175.0)
    )
    val bmiResult = _bmiResult.asStateFlow()


    fun setMode(mode: CalculatorMode) {
        _selectedMode.value = mode
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleDegreeMode() {
        _isDegreeMode.value = !_isDegreeMode.value
        engine.isDegreeMode = _isDegreeMode.value
        reEvaluateLive()
    }

    fun onKeyInput(key: String) {
        val current = _expression.value

        if (_lastEvaluated.value) {
            if (key in listOf("+", "-", "×", "÷", "%", "^")) {
                // Continue operating on previous result
                _expression.value = _liveResult.value + key
            } else if (key != "=" && key != "C" && key != "⌫") {
                _expression.value = key
            }
            _lastEvaluated.value = false
        } else {
            when (key) {
                "C" -> {
                    _expression.value = ""
                    _liveResult.value = ""
                    return
                }
                "⌫" -> {
                    if (current.isNotEmpty()) {
                        // Check if backspacing a multi-char function e.g. "sin(", "cos("
                        val newExpr = when {
                            current.endsWith("asin(") || current.endsWith("acos(") || current.endsWith("atan(") -> current.dropLast(5)
                            current.endsWith("sin(") || current.endsWith("cos(") || current.endsWith("tan(") || current.endsWith("log(") -> current.dropLast(4)
                            current.endsWith("ln(") -> current.dropLast(3)
                            else -> current.dropLast(1)
                        }
                        _expression.value = newExpr
                    }
                }
                "+/-" -> {
                    if (current.startsWith("-")) {
                        _expression.value = current.substring(1)
                    } else if (current.isNotEmpty()) {
                        _expression.value = "-($current)"
                    }
                }
                "=" -> {
                    calculateFinalResult()
                    return
                }
                else -> {
                    _expression.value = current + key
                }
            }
        }

        reEvaluateLive()
    }

    private fun reEvaluateLive() {
        val expr = _expression.value
        if (expr.isBlank()) {
            _liveResult.value = ""
            return
        }
        val result = engine.evaluate(expr)
        if (result != "Error") {
            _liveResult.value = result
        }
    }

    fun calculateFinalResult() {
        val expr = _expression.value
        if (expr.isBlank()) return

        val result = engine.evaluate(expr)
        if (result != "Error" && result != "Can't divide by zero") {
            _liveResult.value = result
            _lastEvaluated.value = true

            // Save to Room DB
            viewModelScope.launch {
                repository.insert(expr, result, _selectedMode.value.label)
            }
        } else {
            _liveResult.value = result
        }
    }

    // Memory operations
    fun memoryClear() { _memoryValue.value = 0.0 }
    fun memoryRecall() { onKeyInput(_memoryValue.value.toString()) }
    fun memoryAdd() {
        val num = _liveResult.value.toDoubleOrNull() ?: _expression.value.toDoubleOrNull()
        if (num != null) _memoryValue.value += num
    }
    fun memorySubtract() {
        val num = _liveResult.value.toDoubleOrNull() ?: _expression.value.toDoubleOrNull()
        if (num != null) _memoryValue.value -= num
    }

    // History actions
    fun clearHistory() {
        viewModelScope.launch { repository.clearAll() }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch { repository.deleteById(id) }
    }

    fun toggleFavoriteHistory(id: Long, currentFav: Boolean) {
        viewModelScope.launch { repository.toggleFavorite(id, currentFav) }
    }

    fun loadHistoryEntry(item: CalculationHistory) {
        _expression.value = item.expression
        _liveResult.value = item.result
        _selectedMode.value = CalculatorMode.STANDARD
        _lastEvaluated.value = true
    }

    // Converter actions
    fun setConverterCategory(cat: UnitConverterEngine.UnitCategory) {
        _converterCategory.value = cat
        _converterFromUnitIndex.value = 0
        _converterToUnitIndex.value = if (UnitConverterEngine.unitsMap[cat]?.size ?: 0 > 1) 1 else 0
        updateUnitConversion()
    }

    fun updateConverterFromValue(value: String) {
        _converterFromValue.value = value
        updateUnitConversion()
    }

    fun setConverterFromUnit(index: Int) {
        _converterFromUnitIndex.value = index
        updateUnitConversion()
    }

    fun setConverterToUnit(index: Int) {
        _converterToUnitIndex.value = index
        updateUnitConversion()
    }

    private fun updateUnitConversion() {
        val cat = _converterCategory.value
        val units = UnitConverterEngine.unitsMap[cat] ?: return
        val valDouble = _converterFromValue.value.toDoubleOrNull() ?: 0.0
        val fromUnit = units.getOrNull(_converterFromUnitIndex.value) ?: units.first()
        val toUnit = units.getOrNull(_converterToUnitIndex.value) ?: units.last()

        val converted = UnitConverterEngine.convertUnit(cat, valDouble, fromUnit, toUnit)
        _converterResult.value = UnitConverterEngine.formatter.format(converted)
    }

    // Discount handlers
    fun updateDiscountInputs(price: String, percent: String, tax: String) {
        _discountPrice.value = price
        _discountPercent.value = percent
        _discountTax.value = tax

        val p = price.toDoubleOrNull() ?: 0.0
        val disc = percent.toDoubleOrNull() ?: 0.0
        val t = tax.toDoubleOrNull() ?: 0.0
        _discountResult.value = UnitConverterEngine.calculateDiscount(p, disc, t)
    }

    // Tip handlers
    fun updateTipInputs(bill: String, tip: String, people: String) {
        _tipBill.value = bill
        _tipPercent.value = tip
        _tipPeople.value = people

        val b = bill.toDoubleOrNull() ?: 0.0
        val t = tip.toDoubleOrNull() ?: 0.0
        val p = people.toIntOrNull() ?: 1
        _tipResult.value = UnitConverterEngine.calculateTipSplit(b, t, p)
    }

    // BMI handlers
    fun updateBmiInputs(weight: String, height: String) {
        _bmiWeight.value = weight
        _bmiHeight.value = height

        val w = weight.toDoubleOrNull() ?: 0.0
        val h = height.toDoubleOrNull() ?: 0.0
        _bmiResult.value = UnitConverterEngine.calculateBmi(w, h)
    }
}
