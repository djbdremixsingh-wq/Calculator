package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.UnitConverterEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertersView(
    category: UnitConverterEngine.UnitCategory,
    fromValue: String,
    fromUnitIndex: Int,
    toUnitIndex: Int,
    resultValue: String,
    onCategorySelected: (UnitConverterEngine.UnitCategory) -> Unit,
    onFromValueChanged: (String) -> Unit,
    onFromUnitIndexChanged: (Int) -> Unit,
    onToUnitIndexChanged: (Int) -> Unit,
    // Discount
    discountPrice: String,
    discountPercent: String,
    discountTax: String,
    discountResult: UnitConverterEngine.DiscountResult,
    onDiscountChanged: (String, String, String) -> Unit,
    // Tip Split
    tipBill: String,
    tipPercent: String,
    tipPeople: String,
    tipResult: UnitConverterEngine.TipSplitResult,
    onTipChanged: (String, String, String) -> Unit,
    // BMI
    bmiWeight: String,
    bmiHeight: String,
    bmiResult: UnitConverterEngine.BmiResult,
    onBmiChanged: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedToolTab by remember { mutableIntStateOf(0) } // 0: Unit, 1: Discount, 2: Tip, 3: BMI

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tool Switcher Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedToolTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            listOf("Unit Converter", "Discount & Savings", "Tip & Split Bill", "BMI Calculator").forEachIndexed { index, title ->
                Tab(
                    selected = selectedToolTab == index,
                    onClick = { selectedToolTab = index },
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp).testTag("tool_tab_$index")
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (selectedToolTab == index) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (selectedToolTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when (selectedToolTab) {
            0 -> UnitConverterSection(
                category = category,
                fromValue = fromValue,
                fromUnitIndex = fromUnitIndex,
                toUnitIndex = toUnitIndex,
                resultValue = resultValue,
                onCategorySelected = onCategorySelected,
                onFromValueChanged = onFromValueChanged,
                onFromUnitIndexChanged = onFromUnitIndexChanged,
                onToUnitIndexChanged = onToUnitIndexChanged
            )
            1 -> DiscountSection(
                price = discountPrice,
                percent = discountPercent,
                tax = discountTax,
                result = discountResult,
                onChanged = onDiscountChanged
            )
            2 -> TipSplitSection(
                bill = tipBill,
                tip = tipPercent,
                people = tipPeople,
                result = tipResult,
                onChanged = onTipChanged
            )
            3 -> BmiSection(
                weight = bmiWeight,
                height = bmiHeight,
                result = bmiResult,
                onChanged = onBmiChanged
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitConverterSection(
    category: UnitConverterEngine.UnitCategory,
    fromValue: String,
    fromUnitIndex: Int,
    toUnitIndex: Int,
    resultValue: String,
    onCategorySelected: (UnitConverterEngine.UnitCategory) -> Unit,
    onFromValueChanged: (String) -> Unit,
    onFromUnitIndexChanged: (Int) -> Unit,
    onToUnitIndexChanged: (Int) -> Unit
) {
    val units = UnitConverterEngine.unitsMap[category] ?: emptyList()
    val context = LocalContext.current

    // Category Selector
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(UnitConverterEngine.UnitCategory.values()) { cat ->
            FilterChip(
                selected = cat == category,
                onClick = { onCategorySelected(cat) },
                label = { Text(cat.title) }
            )
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // From Input
            Text("From", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(
                value = fromValue,
                onValueChange = onFromValueChanged,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("converter_from_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // From Unit Dropdown / Selector
            var expandedFrom by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedFrom,
                onExpandedChange = { expandedFrom = !expandedFrom }
            ) {
                OutlinedTextField(
                    value = units.getOrNull(fromUnitIndex)?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrom) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedFrom,
                    onDismissRequest = { expandedFrom = false }
                ) {
                    units.forEachIndexed { index, u ->
                        DropdownMenuItem(
                            text = { Text("${u.name} (${u.symbol})") },
                            onClick = {
                                onFromUnitIndexChanged(index)
                                expandedFrom = false
                            }
                        )
                    }
                }
            }

            // Swap Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        val temp = fromUnitIndex
                        onFromUnitIndexChanged(toUnitIndex)
                        onToUnitIndexChanged(temp)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap units", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // To Unit & Output
            Text("To", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)

            var expandedTo by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedTo,
                onExpandedChange = { expandedTo = !expandedTo }
            ) {
                OutlinedTextField(
                    value = units.getOrNull(toUnitIndex)?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTo) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedTo,
                    onDismissRequest = { expandedTo = false }
                ) {
                    units.forEachIndexed { index, u ->
                        DropdownMenuItem(
                            text = { Text("${u.name} (${u.symbol})") },
                            onClick = {
                                onToUnitIndexChanged(index)
                                expandedTo = false
                            }
                        )
                    }
                }
            }

            // Converted Result Display Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Result", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$resultValue ${units.getOrNull(toUnitIndex)?.symbol ?: ""}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Converter Result", resultValue))
                            Toast.makeText(context, "Result copied!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy result")
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscountSection(
    price: String,
    percent: String,
    tax: String,
    result: UnitConverterEngine.DiscountResult,
    onChanged: (String, String, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Discount & Savings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = price,
                onValueChange = { onChanged(it, percent, tax) },
                label = { Text("Original Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = percent,
                    onValueChange = { onChanged(price, it, tax) },
                    label = { Text("Discount (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = tax,
                    onValueChange = { onChanged(price, percent, it) },
                    label = { Text("Tax (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Final Price", style = MaterialTheme.typography.labelMedium)
                        Text("$${result.finalPrice}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(modifier = Modifier.height(40.dp).width(1.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You Save", style = MaterialTheme.typography.labelMedium)
                        Text("$${result.totalSavings}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun TipSplitSection(
    bill: String,
    tip: String,
    people: String,
    result: UnitConverterEngine.TipSplitResult,
    onChanged: (String, String, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tip & Split Bill Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = bill,
                onValueChange = { onChanged(it, tip, people) },
                label = { Text("Total Bill ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = tip,
                    onValueChange = { onChanged(bill, it, people) },
                    label = { Text("Tip (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = people,
                    onValueChange = { onChanged(bill, tip, it) },
                    label = { Text("Split (People)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total per Person:")
                        Text("$${result.perPersonAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
                    }
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Tip Amount:")
                        Text("$${result.tipAmount}", fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Bill w/ Tip:")
                        Text("$${result.totalBillWithTip}", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BmiSection(
    weight: String,
    height: String,
    result: UnitConverterEngine.BmiResult,
    onChanged: (String, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("BMI Health Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { onChanged(it, height) },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = height,
                    onValueChange = { onChanged(weight, it) },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Your BMI Index", style = MaterialTheme.typography.labelMedium)
                    Text(result.bmiValue, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(result.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Ideal Weight: ${result.idealWeightRange}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
