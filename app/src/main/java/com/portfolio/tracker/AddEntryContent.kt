package com.portfolio.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import com.portfolio.tracker.data.repository.EntryRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val ASSET_CATEGORIES = listOf(
    "Cash & Savings", "Securities", "Securities Equity", "Securities Debt",
    "Securities Hybrid", "Principal Residence", "Other Real Estate",
    "Enterprise Capital", "Durable Goods", "Other Assets"
)

val LIABILITY_CATEGORIES = listOf("Mortgage", "Loans", "Other Liabilities")
val CURRENCIES = listOf("EUR", "USD", "GBP", "JPY", "CHF", "CAD", "AUD", "HUF", "CNY")

@Composable
fun AddEntryContent(
    repository: EntryRepository,
    entry: PortfolioEntryEntity? = null,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf(entry?.type ?: "Assets") }
    var selectedCategory by remember { mutableStateOf(entry?.category ?: ASSET_CATEGORIES[0]) }
    var description by remember { mutableStateOf(entry?.description ?: "") }
    var amount by remember { mutableStateOf(entry?.amount?.toString() ?: "") }
    var selectedCurrency by remember { mutableStateOf(entry?.currency ?: "EUR") }
    var convertedAmount by remember { mutableDoubleStateOf(entry?.convertedAmount ?: 0.0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isEditing = entry != null
    val screenTitle = if (isEditing) "Edit Entry" else "Add Entry"

    val exchangeRates = mapOf(
        "EUR" to 1.0,
        "USD" to 1.08,
        "GBP" to 0.86,
        "JPY" to 160.0,
        "CHF" to 0.95,
        "CAD" to 1.47,
        "AUD" to 1.65,
        "HUF" to 384.0,
        "CNY" to 7.85
    )

    LaunchedEffect(amount, selectedCurrency) {
        val amountValue = amount.toDoubleOrNull() ?: 0.0
        val rate = exchangeRates[selectedCurrency] ?: 1.0
        convertedAmount = amountValue / rate
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F3F5))
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFFFF))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1F2328)
                )
            }
            Text(
                text = screenTitle,
                color = Color(0xFF1F2328),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFC62828),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Text(
                text = "Type *",
                color = Color(0xFF1F2328),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Assets", "Liabilities").forEach { type ->
                    Button(
                        onClick = {
                            selectedType = type
                            selectedCategory = if (type == "Assets") ASSET_CATEGORIES[0] else LIABILITY_CATEGORIES[0]
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == type) Color(0xFF2563EB) else Color(0xFFE3E5E8),
                            contentColor = if (selectedType == type) Color.White else Color(0xFF1F2328)
                        )
                    ) {
                        Text(type, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "Category *",
                color = Color(0xFF1F2328),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            var expandedCategory by remember { mutableStateOf(false) }
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)) {
                Button(
                    onClick = { expandedCategory = !expandedCategory },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(1.dp, Color(0xFFE3E5E8)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFFFFF),
                        contentColor = Color(0xFF1F2328)
                    )
                ) {
                    Text(selectedCategory, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                    Text("▼", fontSize = 12.sp)
                }
                if (expandedCategory) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp)
                            .zIndex(1f)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            val categories = if (selectedType == "Assets") ASSET_CATEGORIES else LIABILITY_CATEGORIES
                            Column(modifier = Modifier.fillMaxWidth()) {
                                categories.forEach { category ->
                                    TextButton(
                                        onClick = {
                                            selectedCategory = category
                                            expandedCategory = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            category,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "Description *",
                color = Color(0xFF1F2328),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                placeholder = { Text("e.g., Bank Account, Home Loan") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE3E5E8)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Amount *",
                color = Color(0xFF1F2328),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE3E5E8)
                    )
                )

                var expandedCurrency by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(0.8f)) {
                    Button(
                        onClick = { expandedCurrency = !expandedCurrency },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDBEAFE),
                            contentColor = Color(0xFF1F2328)
                        )
                    ) {
                        Text(selectedCurrency, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("▼", fontSize = 12.sp)
                    }
                    if (expandedCurrency) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 50.dp)
                                .zIndex(1f)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    CURRENCIES.forEach { currency ->
                                        TextButton(
                                            onClick = {
                                                selectedCurrency = currency
                                                expandedCurrency = false
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                currency,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp),
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDBEAFE))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Converted to EUR:",
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "€ ${"%.2f".format(convertedAmount)}",
                        color = Color(0xFF1F2328),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val scope = rememberCoroutineScope()

            Button(
                onClick = {
                    errorMessage = ""
                    when {
                        description.isBlank() -> errorMessage = "Description is required"
                        amount.isBlank() -> errorMessage = "Amount is required"
                        amount.toDoubleOrNull() == null -> errorMessage = "Invalid amount"
                        amount.toDoubleOrNull() ?: 0.0 <= 0 -> errorMessage = "Amount must be greater than 0"
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    if (isEditing) {
                                        val updatedEntry = entry!!.copy(
                                            type = selectedType,
                                            category = selectedCategory,
                                            description = description,
                                            amount = amount.toDouble(),
                                            currency = selectedCurrency,
                                            convertedAmount = convertedAmount
                                        )
                                        repository.updateEntry(updatedEntry)
                                    } else {
                                        val newEntry = PortfolioEntryEntity(
                                            entryId = java.util.UUID.randomUUID().toString(),
                                            dateTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME),
                                            type = selectedType,
                                            category = selectedCategory,
                                            description = description,
                                            amount = amount.toDouble(),
                                            currency = selectedCurrency,
                                            convertedAmount = convertedAmount
                                        )
                                        repository.insertEntry(newEntry)
                                    }
                                    isLoading = false
                                    onSave()
                                } catch (e: Exception) {
                                    errorMessage = "Error saving entry: ${e.message}"
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(if (isEditing) "Update Entry" else "Save Entry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE3E5E8),
                    contentColor = Color(0xFF1F2328)
                )
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        }
    }
}