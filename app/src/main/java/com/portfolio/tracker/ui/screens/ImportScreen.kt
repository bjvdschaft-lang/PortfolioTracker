package com.portfolio.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.tracker.data.repository.EntryRepository
import com.portfolio.tracker.utils.ImportHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImportScreen(
    repository: EntryRepository,
    onBack: () -> Unit,
    onImportComplete: () -> Unit
) {
    var csvContent by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var importProgress by remember { mutableStateOf(0) }
    var totalEntries by remember { mutableStateOf(0) }
    var importSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F3F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
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
                text = "Import CSV Data",
                color = Color(0xFF1F2328),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Instructions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "How to Import",
                            color = Color(0xFF2563EB),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Paste your CSV data below. Expected format:\nDateTime, Type, Category, Description, Amount, Currency, Converted Amount",
                            color = Color(0xFF1565C0),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // CSV Input
            Text(
                text = "Paste CSV Data *",
                color = Color(0xFF1F2328),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = csvContent,
                onValueChange = { csvContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("Paste your CSV data here...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE3E5E8)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error Message
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

            // Success Message
            if (importSuccess) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Successfully imported $totalEntries entries!",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Progress
            if (isImporting) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Importing entries...",
                                color = Color(0xFF6200EE),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$importProgress / $totalEntries",
                                color = Color(0xFF6200EE),
                                fontSize = 12.sp
                            )
                        }
                        LinearProgressIndicator(
                            progress = if (totalEntries > 0) importProgress.toFloat() / totalEntries else 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = Color(0xFF6200EE),
                            trackColor = Color(0xFFE3E5E8)
                        )
                    }
                }
            }

            // Import Button
            Button(
                onClick = {
                    errorMessage = ""
                    if (csvContent.isBlank()) {
                        errorMessage = "Please paste CSV data"
                        return@Button
                    }

                    isImporting = true
                    importSuccess = false
                    val lines = csvContent.lines()
                        .filter { it.isNotBlank() }
                        .drop(1)
                    totalEntries = lines.size

                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                ImportHelper.importCsvData(
                                    csvContent = csvContent,
                                    repository = repository,
                                    onProgress = { current, _ ->
                                        importProgress = current
                                    }
                                )
                            }
                            isImporting = false
                            importSuccess = true
                            csvContent = ""
                        } catch (e: Exception) {
                            errorMessage = "Import failed: ${e.message}"
                            isImporting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                enabled = !isImporting
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Import",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Text("Import CSV", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (importSuccess) {
                        onImportComplete()
                    } else {
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE3E5E8),
                    contentColor = Color(0xFF1F2328)
                )
            ) {
                Text(if (importSuccess) "View Dashboard" else "Cancel", fontWeight = FontWeight.Bold)
            }
        }
    }
}