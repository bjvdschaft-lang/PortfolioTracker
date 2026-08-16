package com.portfolio.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.overflow.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import com.portfolio.tracker.data.repository.EntryRepository
import kotlinx.coroutines.launch

@Composable
fun DashboardContent(
    entries: List<PortfolioEntryEntity>,
    repository: EntryRepository,
    onAddEntry: () -> Unit,
    onEditEntry: (PortfolioEntryEntity) -> Unit,
    onViewCharts: () -> Unit,
    onImportData: () -> Unit,
    onDebug: () -> Unit = {}
) {
    val latestDateTime = entries.maxByOrNull { it.dateTime }?.dateTime
    val latestEntries = if (latestDateTime != null) {
        entries.filter { it.dateTime == latestDateTime }
    } else {
        emptyList()
    }

    val allDateTimes = entries.map { it.dateTime }.distinct().sortedDescending()
    var selectedDateTime by remember { mutableStateOf(latestDateTime ?: "") }
    var expandedDropdown by remember { mutableStateOf(false) }

    val displayedEntries = if (selectedDateTime.isNotEmpty()) {
        entries.filter { it.dateTime == selectedDateTime }
    } else {
        latestEntries
    }

    val assetEntries = displayedEntries.filter { it.type == "Assets" }.sortedBy { it.description }
    val liabilityEntries = displayedEntries.filter { it.type == "Liabilities" }.sortedBy { it.description }

    val totalAssets = assetEntries.sumOf { it.convertedAmount }
    val totalLiabilities = liabilityEntries.sumOf { it.convertedAmount }
    val netWorth = totalAssets - totalLiabilities
    val scope = rememberCoroutineScope()

    // Everything in one LazyColumn - ONLY scrollable container
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F3F5))
    ) {
        // Header Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF2F3F5))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Net Worth Dashboard",
                    color = Color(0xFF1F2328),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentSize(Alignment.TopStart)
                    ) {
                        Button(
                            onClick = { expandedDropdown = !expandedDropdown },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFFFFF),
                                contentColor = Color(0xFF1F2328)
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE3E5E8))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when {
                                        selectedDateTime.isEmpty() -> "Select Date"
                                        selectedDateTime == latestDateTime -> "Load most recent"
                                        else -> "Load older input to modify"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Select Date", fontSize = 12.sp) },
                                onClick = {
                                    selectedDateTime = ""
                                    expandedDropdown = false
                                }
                            )

                            Divider(color = Color(0xFFE3E5E8), thickness = 1.dp)

                            if (latestDateTime != null) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("Load most recent", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(latestDateTime, fontSize = 10.sp, color = Color(0xFF6B7280))
                                        }
                                    },
                                    onClick = {
                                        selectedDateTime = latestDateTime
                                        expandedDropdown = false
                                    }
                                )

                                Divider(color = Color(0xFFE3E5E8), thickness = 1.dp)
                            }

                            allDateTimes.filterNot { it == latestDateTime }.forEach { dateTime ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("Load older input to modify", fontSize = 11.sp)
                                            Text(dateTime, fontSize = 10.sp, color = Color(0xFF6B7280))
                                        }
                                    },
                                    onClick = {
                                        selectedDateTime = dateTime
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (selectedDateTime.isNotEmpty()) {
                    Text(
                        text = "Viewing: $selectedDateTime",
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Current Net Worth",
                                color = Color(0xFFE1BEE7),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "€ ${"%.2f".format(netWorth)}",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF03DAC6))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Assets",
                                color = Color(0xFF00695C),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "€ ${"%.2f".format(totalAssets)}",
                                color = Color(0xFF00695C),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFCF6679))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Liabilities",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "€ ${"%.2f".format(totalLiabilities)}",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddEntry,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Text("Add", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onViewCharts,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC6))
                    ) {
                        Text("Charts", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onImportData,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Import",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Text("Import", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onDebug,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280))
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "Debug",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Text("Debug", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Assets Section
        if (assetEntries.isNotEmpty()) {
            item {
                Text(
                    text = "Assets (${assetEntries.size})",
                    color = Color(0xFF03DAC6),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 12.dp, top = 8.dp)
                        .padding(horizontal = 16.dp)
                )
            }

            items(assetEntries) { entry ->
                var showDeleteDialog by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.category,
                            color = Color(0xFF6B7280),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = entry.description,
                            color = Color(0xFF1F2328),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${"%.2f".format(entry.amount)}",
                            color = Color(0xFF1F2328),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = entry.currency,
                            color = Color(0xFF6B7280),
                            fontSize = 10.sp,
                            modifier = Modifier.weight(0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "€ ${"%.2f".format(entry.convertedAmount)}",
                            color = Color(0xFF03DAC6),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.2f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        IconButton(onClick = { onEditEntry(entry) }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFCF6679), modifier = Modifier.size(14.dp))
                        }
                    }
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Entry?") },
                        text = { Text("Are you sure you want to delete '${entry.description}'?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.deleteEntry(entry)
                                        showDeleteDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679))
                            ) {
                                Text("Delete", color = Color.White)
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showDeleteDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3E5E8))
                            ) {
                                Text("Cancel", color = Color(0xFF1F2328))
                            }
                        }
                    )
                }
            }
        }

        // Liabilities Section
        if (liabilityEntries.isNotEmpty()) {
            item {
                Text(
                    text = "Liabilities (${liabilityEntries.size})",
                    color = Color(0xFFCF6679),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 12.dp, top = 24.dp)
                        .padding(horizontal = 16.dp)
                )
            }

            items(liabilityEntries) { entry ->
                var showDeleteDialog by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.category,
                            color = Color(0xFF6B7280),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = entry.description,
                            color = Color(0xFF1F2328),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${"%.2f".format(entry.amount)}",
                            color = Color(0xFF1F2328),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = entry.currency,
                            color = Color(0xFF6B7280),
                            fontSize = 10.sp,
                            modifier = Modifier.weight(0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "€ ${"%.2f".format(entry.convertedAmount)}",
                            color = Color(0xFFCF6679),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.2f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        IconButton(onClick = { onEditEntry(entry) }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFCF6679), modifier = Modifier.size(14.dp))
                        }
                    }
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Entry?") },
                        text = { Text("Are you sure you want to delete '${entry.description}'?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.deleteEntry(entry)
                                        showDeleteDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679))
                            ) {
                                Text("Delete", color = Color.White)
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showDeleteDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3E5E8))
                            ) {
                                Text("Cancel", color = Color(0xFF1F2328))
                            }
                        }
                    )
                }
            }
        }

        if (assetEntries.isEmpty() && liabilityEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No entries yet. Click 'Add' to get started!",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
