package com.example.engine

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object UnitConverterEngine {

    val formatter = DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.US))

    // --- UNIT CONVERSIONS ---
    enum class UnitCategory(val title: String) {
        LENGTH("Length"),
        WEIGHT("Weight"),
        TEMPERATURE("Temperature"),
        AREA("Area"),
        VOLUME("Volume"),
        SPEED("Speed"),
        DATA("Data")
    }

    data class ConversionUnit(val name: String, val symbol: String, val toBaseFactor: Double)

    val unitsMap = mapOf(
        UnitCategory.LENGTH to listOf(
            ConversionUnit("Meters", "m", 1.0),
            ConversionUnit("Kilometers", "km", 1000.0),
            ConversionUnit("Centimeters", "cm", 0.01),
            ConversionUnit("Millimeters", "mm", 0.001),
            ConversionUnit("Miles", "mi", 1609.344),
            ConversionUnit("Yards", "yd", 0.9144),
            ConversionUnit("Feet", "ft", 0.3048),
            ConversionUnit("Inches", "in", 0.0254)
        ),
        UnitCategory.WEIGHT to listOf(
            ConversionUnit("Kilograms", "kg", 1.0),
            ConversionUnit("Grams", "g", 0.001),
            ConversionUnit("Milligrams", "mg", 0.000001),
            ConversionUnit("Pounds", "lbs", 0.45359237),
            ConversionUnit("Ounces", "oz", 0.028349523125),
            ConversionUnit("Tons", "t", 1000.0)
        ),
        UnitCategory.TEMPERATURE to listOf(
            ConversionUnit("Celsius", "°C", 1.0),
            ConversionUnit("Fahrenheit", "°F", 1.0),
            ConversionUnit("Kelvin", "K", 1.0)
        ),
        UnitCategory.AREA to listOf(
            ConversionUnit("Square Meters", "m²", 1.0),
            ConversionUnit("Square Kilometers", "km²", 1000000.0),
            ConversionUnit("Square Feet", "ft²", 0.09290304),
            ConversionUnit("Acres", "ac", 4046.8564224),
            ConversionUnit("Hectares", "ha", 10000.0)
        ),
        UnitCategory.VOLUME to listOf(
            ConversionUnit("Liters", "L", 1.0),
            ConversionUnit("Milliliters", "mL", 0.001),
            ConversionUnit("Cubic Meters", "m³", 1000.0),
            ConversionUnit("Gallons (US)", "gal", 3.785411784),
            ConversionUnit("Cups", "cup", 0.2365882365)
        ),
        UnitCategory.SPEED to listOf(
            ConversionUnit("Meters / sec", "m/s", 1.0),
            ConversionUnit("Kilometers / hr", "km/h", 0.277777778),
            ConversionUnit("Miles / hr", "mph", 0.44704),
            ConversionUnit("Knots", "kt", 0.514444)
        ),
        UnitCategory.DATA to listOf(
            ConversionUnit("Bytes", "B", 1.0),
            ConversionUnit("Kilobytes", "KB", 1024.0),
            ConversionUnit("Megabytes", "MB", 1048576.0),
            ConversionUnit("Gigabytes", "GB", 1073741824.0),
            ConversionUnit("Terabytes", "TB", 1099511627776.0)
        )
    )

    fun convertUnit(
        category: UnitCategory,
        fromValue: Double,
        fromUnit: ConversionUnit,
        toUnit: ConversionUnit
    ): Double {
        if (category == UnitCategory.TEMPERATURE) {
            // Temperature requires formula shifting
            val tempInCelsius = when (fromUnit.symbol) {
                "°C" -> fromValue
                "°F" -> (fromValue - 32.0) * 5.0 / 9.0
                "K" -> fromValue - 273.15
                else -> fromValue
            }
            return when (toUnit.symbol) {
                "°C" -> tempInCelsius
                "°F" -> (tempInCelsius * 9.0 / 5.0) + 32.0
                "K" -> tempInCelsius + 273.15
                else -> tempInCelsius
            }
        } else {
            val baseValue = fromValue * fromUnit.toBaseFactor
            return baseValue / toUnit.toBaseFactor
        }
    }

    // --- DISCOUNT CALCULATOR ---
    data class DiscountResult(
        val finalPrice: String,
        val totalSavings: String
    )

    fun calculateDiscount(originalPrice: Double, discountPercent: Double, taxPercent: Double = 0.0): DiscountResult {
        val discountAmount = originalPrice * (discountPercent / 100.0)
        val priceAfterDiscount = originalPrice - discountAmount
        val taxAmount = priceAfterDiscount * (taxPercent / 100.0)
        val finalPrice = priceAfterDiscount + taxAmount
        val savings = discountAmount

        return DiscountResult(
            finalPrice = formatter.format(finalPrice),
            totalSavings = formatter.format(savings)
        )
    }

    // --- TIP & BILL SPLITTER ---
    data class TipSplitResult(
        val tipAmount: String,
        val totalBillWithTip: String,
        val perPersonAmount: String,
        val tipPerPerson: String
    )

    fun calculateTipSplit(billAmount: Double, tipPercent: Double, numberOfPeople: Int): TipSplitResult {
        val people = if (numberOfPeople < 1) 1 else numberOfPeople
        val totalTip = billAmount * (tipPercent / 100.0)
        val totalBill = billAmount + totalTip
        val perPerson = totalBill / people
        val tipPerPerson = totalTip / people

        return TipSplitResult(
            tipAmount = formatter.format(totalTip),
            totalBillWithTip = formatter.format(totalBill),
            perPersonAmount = formatter.format(perPerson),
            tipPerPerson = formatter.format(tipPerPerson)
        )
    }

    // --- BMI CALCULATOR ---
    data class BmiResult(
        val bmiValue: String,
        val category: String,
        val idealWeightRange: String
    )

    fun calculateBmi(weightKg: Double, heightCm: Double): BmiResult {
        if (heightCm <= 0 || weightKg <= 0) {
            return BmiResult("0.0", "Invalid input", "N/A")
        }
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)

        val category = when {
            bmi < 18.5 -> "Underweight ⚠️"
            bmi < 24.9 -> "Normal weight ✅"
            bmi < 29.9 -> "Overweight ⚠️"
            else -> "Obesity 🚨"
        }

        val minIdealKg = 18.5 * (heightM * heightM)
        val maxIdealKg = 24.9 * (heightM * heightM)
        val idealRange = "${formatter.format(minIdealKg)} kg - ${formatter.format(maxIdealKg)} kg"

        return BmiResult(
            bmiValue = DecimalFormat("0.0", DecimalFormatSymbols(Locale.US)).format(bmi),
            category = category,
            idealWeightRange = idealRange
        )
    }
}
