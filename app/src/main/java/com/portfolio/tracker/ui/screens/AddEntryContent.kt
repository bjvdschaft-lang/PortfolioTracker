package com.portfolio.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryContent(
    existingEntry: PortfolioEntryEntity?,
    onSave: (PortfolioEntryEntity) -> Unit,
    onCancel: () -> Unit
) {
    val isEdit = existingEntry != null
    val title = if (isEdit) "Edit Entry" else "Add Entry"

    var type by remember { mutableStateOf(existingEntry?.type ?: "Assets") }
    var category by remember { mutableStateOf(existingEntry?.category ?: "") }
    var description by remember { mutableStateOf(existingEntry?.description ?: "") }
    var amount by remember { mutableStateOf(existingEntry?.amount?.toString() ?: "") }
    var currency by remember { mutableStateOf(existingEntry?.currency ?: "EUR") }
    var convertedAmount by remember { mutableStateOf(existingEntry?.convertedAmount?.toString() ?: "") }

    var typeExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }

    val types = listOf("Assets", "Liabilities")
    val currencies = listOf("EUR", "USD", "GBP", "CHF", "JPY", "BTC", "ETH")

    var amountError by remember { mutableStateOf(false) }
    var convertedAmountError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2328)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1F2328)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF2F3F5)
                )
            )
        },
        containerColor = Color(0xFFF2F3F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type dropdown
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    types.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                type = option
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            // Category
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category (e.g. Cash, Real Estate, Mortgage)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    descriptionError = false
                },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = descriptionError,
                supportingText = if (descriptionError) {
                    { Text("Description is required") }
                } else null
            )

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    amountError = false
                },
                label = { Text("Amount *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = amountError,
                supportingText = if (amountError) {
                    { Text("Enter a valid number") }
                } else null
            )

            // Currency dropdown
            ExposedDropdownMenuBox(
                expanded = currencyExpanded,
                onExpandedChange = { currencyExpanded = it }
            ) {
                OutlinedTextField(
                    value = currency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Currency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false }
                ) {
                    currencies.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                currency = option
                                currencyExpanded = false
                            }
                        )
                    }
                }
            }

            // Converted amount (EUR)
            OutlinedTextField(
                value = convertedAmount,
                onValueChange = {
                    convertedAmount = it
                    convertedAmountError = false
                },
                label = { Text("Value in EUR *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = convertedAmountError,
                supportingText = if (convertedAmountError) {
                    { Text("Enter a valid number") }
                } else null
            )

            // Save button
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    val parsedConverted = convertedAmount.toDoubleOrNull()
                    amountError = parsedAmount == null
                    convertedAmountError = parsedConverted == null
                    descriptionError = description.isBlank()

                    if (!amountError && !convertedAmountError && !descriptionError) {
                        val timestamp = if (isEdit) {
                            existingEntry!!.dateTime
                        } else {
                            LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        }
                        onSave(
                            PortfolioEntryEntity(
                                id = existingEntry?.id ?: 0,
                                type = type,
                                category = category,
                                description = description,
                                amount = parsedAmount!!,
                                currency = currency,
                                convertedAmount = parsedConverted!!,
                                dateTime = timestamp
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text(
                    text = if (isEdit) "Save Changes" else "Add Entry",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Cancel button
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Cancel", fontSize = 16.sp)
            }
        }
    }
}
