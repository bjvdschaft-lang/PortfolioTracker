package com.portfolio.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import com.portfolio.tracker.data.repository.EntryRepository
import kotlinx.coroutines.launch

@Composable
fun DebugScreen(
    entries: List<PortfolioEntryEntity>,
    repository: EntryRepository,
    onBack: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var copyFeedback by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F3F5))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F2328))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🔧 Debug Console",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${entries.size} entries in database",
                    color = Color(0xFFB0B8C1),
                    fontSize = 12.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Database Info
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3748))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(
                            text = "DATABASE INFO",
                            color = Color(0xFF4299E1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        DebugRow("Total Entries", entries.size.toString())
                        DebugRow("Assets", entries.filter { it.type == "Assets" }.size.toString())
                        DebugRow("Liabilities", entries.filter { it.type == "Liabilities" }.size.toString())
                        DebugRow(
                            "Total Assets EUR",
                            "€ ${"%.2f".format(entries.filter { it.type == "Assets" }.sumOf { it.convertedAmount })}"
                        )
                        DebugRow(
                            "Total Liabilities EUR",
                            "€ ${"%.2f".format(entries.filter { it.type == "Liabilities" }.sumOf { it.convertedAmount })}"
                        )
                    }
                }
            }

            // Export Options
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val csvContent = buildCsvString(entries)
                            clipboardManager.setText(AnnotatedString(csvContent))
                            copyFeedback = "✓ CSV copied to clipboard!"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48BB78))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Text("Copy CSV", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF56565))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Text("Clear All", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Copy Feedback
            if (copyFeedback.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFC6F6D5))
                    ) {
                        Text(
                            text = copyFeedback,
                            color = Color(0xFF22543D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Database Entries
            item {
                Text(
                    text = "DATABASE ENTRIES",
                    color = Color(0xFF4299E1),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                )
            }

            if (entries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3748))
                    ) {
                        Text(
                            text = "No entries in database",
                            color = Color(0xFF718096),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(entries) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3748))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Entry Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.description,
                                    color = Color(0xFF63B3ED),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                // Type Badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (entry.type == "Assets") Color(0xFF48BB78) else Color(0xFFF56565),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = entry.type,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Divider(color = Color(0xFF4A5568), modifier = Modifier.padding(vertical = 8.dp))

                            // Entry Details
                            DebugRow("ID", entry.entryId, small = true)
                            DebugRow("DateTime", entry.dateTime, small = true)
                            DebugRow("Category", entry.category, small = true)
                            DebugRow("Amount", "${entry.amount} ${entry.currency}", small = true)
                            DebugRow("Converted", "€ ${"%.2f".format(entry.convertedAmount)}", small = true)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Clear Database Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Database?") },
            text = { Text("This will delete ALL ${entries.size} entries. This cannot be undone!") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            entries.forEach { repository.deleteEntry(it) }
                            showClearDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF56565))
                ) {
                    Text("Delete All", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showClearDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3E5E8))
                ) {
                    Text("Cancel", color = Color(0xFF1F2328))
                }
            }
        )
    }
}

@Composable
fun DebugRow(label: String, value: String, small: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF718096),
            fontSize = if (small) 10.sp else 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = Color(0xFFCBD5E0),
            fontSize = if (small) 10.sp else 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

fun buildCsvString(entries: List<PortfolioEntryEntity>): String {
    val header = "DateTime,Type,Category,Description,Amount,Currency,Converted Amount"
    val rows = entries.map { entry ->
        "${entry.dateTime},${entry.type},${entry.category},${entry.description},${entry.amount},${entry.currency},${entry.convertedAmount}"
    }
    return (listOf(header) + rows).joinToString("\n")
}