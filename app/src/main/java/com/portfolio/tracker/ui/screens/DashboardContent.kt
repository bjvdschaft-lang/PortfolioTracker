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
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import com.portfolio.tracker.data.repository.EntryRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardContent(
    entries: List<PortfolioEntryEntity>,
    repository: EntryRepository,
    onAddEntry: () -> Unit,
    onEditEntry: (PortfolioEntryEntity) -> Unit,
    onViewCharts: () -> Unit,
    onImportData: () -> Unit,
    onDebug: () -> Unit = {},
    onExit: () -> Unit = {}
) {
    var showExitDialog by remember { mutableStateOf(false) }
    
    val latestDateTime = entries.maxByOrNull { it.dateTime }?.dateTime
    val latestEntries = if (latestDateTime != null) {
        entries.filter { it.dateTime == latestDateTime }
    } else {
        emptyList()
    }

    val allDateTimes = entries.map { it.dateTime }.distinct().sortedDescending()
    
    // Extract years from database entries
    val availableYears = entries.map { 
        it.dateTime.substringBefore(" ").substringBefore("T").substringBefore("-")
    }.distinct().sorted().reversed()
    
    var dropdownMode by remember { mutableStateOf("") } // "", "most_recent", "custom_date"
    var pickedMonth by remember { mutableStateOf("") } // "01" to "12"
    var pickedDay by remember { mutableStateOf("") }
    var pickedYear by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }
    var monthDropdownOpen by remember { mutableStateOf(false) }
    var yearDropdownOpen by remember { mutableStateOf(false) }

    val monthShortNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthNumbers = listOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")

    val displayedMonth = if (pickedMonth.isNotEmpty()) {
        monthShortNames.getOrNull(pickedMonth.toIntOrNull()?.minus(1) ?: -1) ?: ""
    } else {
        ""
    }

    val pickedDate = if (pickedMonth.isNotEmpty() && pickedDay.isNotEmpty() && pickedYear.isNotEmpty()) {
        String.format("%s-%s-%s", pickedYear, pickedMonth, pickedDay.padStart(2, '0'))
    } else {
        ""
    }

    // Get today's date
    val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

    // Find the exact matching date and update the display fields
    val (displayedEntries, foundDate) = if (dropdownMode == "most_recent" && latestDateTime != null) {
        Pair(entries.filter { it.dateTime == latestDateTime }, latestDateTime.substringBefore(" ").substringBefore("T"))
    } else if (dropdownMode == "custom_date" && pickedDate.isNotEmpty()) {
        // Find entries with dates before or equal to the picked date
        val filteredEntries = entries.filter { entry ->
            val entryDate = entry.dateTime.substringBefore(" ").substringBefore("T")
            entryDate <= pickedDate
        }
        
        // Find the maximum date that is <= pickedDate
        val maxDate = filteredEntries.maxByOrNull { it.dateTime }?.dateTime?.substringBefore(" ")?.substringBefore("T")
        
        if (maxDate != null) {
            val matchingEntries = entries.filter { entry ->
                entry.dateTime.startsWith(maxDate)
            }
            Pair(matchingEntries, maxDate)
        } else {
            Pair(emptyList(), "")
        }
    } else {
        // When dropdownMode is empty ("Clear screen" selected), show empty list
        Pair(emptyList(), "")
    }

    // Track whether we're viewing an older date (NOW DEFINED AFTER foundDate)
    val isViewingOlderDate = dropdownMode == "custom_date" && foundDate.isNotEmpty() && foundDate != todayDate

    // Update the input fields to reflect the found date (but only if user didn't manually input it)
    LaunchedEffect(foundDate) {
        if (foundDate.isNotEmpty() && dropdownMode == "custom_date" && pickedDate != foundDate) {
            val parts = foundDate.split("-")
            if (parts.size == 3) {
                pickedYear = parts[0]
                pickedMonth = parts[1]
                pickedDay = parts[2].toIntOrNull()?.toString() ?: parts[2]
            }
        }
    }

    val assetEntries = displayedEntries.filter { it.type == "Assets" }.sortedBy { it.description }
    val liabilityEntries = displayedEntries.filter { it.type == "Liabilities" }.sortedBy { it.description }

    val totalAssets = assetEntries.sumOf { it.convertedAmount }
    val totalLiabilities = liabilityEntries.sumOf { it.convertedAmount }
    val netWorth = totalAssets - totalLiabilities
    val scope = rememberCoroutineScope()

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Application?") },
            text = { Text("Are you sure you want to exit? Your data is automatically saved.") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onExit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text("Exit", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showExitDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3E5E8))
                ) {
                    Text("Cancel", color = Color(0xFF1F2328))
                }
            }
        )
    }

    // LazyColumn DIRECTLY - NO outer Column wrapper
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F3F5))
    ) {
        // Stats Section at Top with Exit Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Exit Button - Top Left
                Button(
                    onClick = { showExitDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(width = 60.dp, height = 40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Power,
                        contentDescription = "Exit",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }

                // Stats Cards - Centered
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 70.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Net Worth",
                                color = Color(0xFFE1BEE7),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "€ ${"%.2f".format(netWorth)}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x90A4DE6C))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Assets",
                                color = Color(0xFF2D5016),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "€ ${"%.2f".format(totalAssets)}",
                                color = Color(0xFF2D5016),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFCF6679))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Liabilities",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "€ ${"%.2f".format(totalLiabilities)}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons - Charts, Import, Debug only (removed Add)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

        // Date Selector and Dropdown - Above Assets
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown Button (LEFT)
                Box(
                    modifier = Modifier.weight(1f)
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
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (dropdownMode) {
                                    "most_recent" -> "Load most recent"
                                    "custom_date" -> "Load older input"
                                    else -> ""
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear screen", fontSize = 11.sp) },
                            onClick = {
                                dropdownMode = ""
                                pickedMonth = ""
                                pickedDay = ""
                                pickedYear = ""
                                expandedDropdown = false
                            }
                        )

                        Divider(color = Color(0xFFE3E5E8), thickness = 1.dp)

                        if (latestDateTime != null) {
                            DropdownMenuItem(
                                text = {
                                    Text("Load most recent", fontSize = 11.sp)
                                },
                                onClick = {
                                    dropdownMode = "most_recent"
                                    pickedMonth = ""
                                    pickedDay = ""
                                    pickedYear = ""
                                    expandedDropdown = false
                                }
                            )

                            Divider(color = Color(0xFFE3E5E8), thickness = 1.dp)
                        }

                        DropdownMenuItem(
                            text = {
                                Text("Load older input", fontSize = 11.sp)
                            },
                            onClick = {
                                dropdownMode = "custom_date"
                                expandedDropdown = false
                            }
                        )
                    }
                }

                // Date Input Fields (RIGHT) - Month (Dropdown), Day, Year (Dropdown)
                Row(
                    modifier = Modifier
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Month Dropdown
                    Box(
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Button(
                            onClick = { monthDropdownOpen = !monthDropdownOpen },
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
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayedMonth.ifEmpty { "Mon" },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Month",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = monthDropdownOpen,
                            onDismissRequest = { monthDropdownOpen = false }
                        ) {
                            monthShortNames.forEachIndexed { index, month ->
                                DropdownMenuItem(
                                    text = { Text(month, fontSize = 12.sp) },
                                    onClick = {
                                        pickedMonth = monthNumbers[index]
                                        monthDropdownOpen = false
                                        if (pickedMonth.isNotEmpty() && pickedDay.isNotEmpty() && pickedYear.isNotEmpty()) {
                                            dropdownMode = "custom_date"
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Day TextField
                    OutlinedTextField(
                        value = pickedDay,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || (newValue.toIntOrNull() in 1..31 && newValue.length <= 2)) {
                                pickedDay = newValue
                                if (pickedMonth.isNotEmpty() && newValue.isNotEmpty() && pickedYear.isNotEmpty()) {
                                    dropdownMode = "custom_date"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(0.8f)
                            .height(44.dp),
                        placeholder = { Text("DD", fontSize = 9.sp) },
                        singleLine = true,
                        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 12.sp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )

                    // Year Dropdown
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Button(
                            onClick = { yearDropdownOpen = !yearDropdownOpen },
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
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pickedYear.ifEmpty { "Year" },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Year",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = yearDropdownOpen,
                            onDismissRequest = { yearDropdownOpen = false }
                        ) {
                            availableYears.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year, fontSize = 12.sp) },
                                    onClick = {
                                        pickedYear = year
                                        yearDropdownOpen = false
                                        if (pickedMonth.isNotEmpty() && pickedDay.isNotEmpty() && year.isNotEmpty()) {
                                            dropdownMode = "custom_date"
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Assets Section Header with Add Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assets (${assetEntries.size})",
                    color = Color(0xFF2D5016),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onAddEntry,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Asset",
                        tint = Color(0xFF2D5016),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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
                        color = Color(0xFF2D5016),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.2f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    IconButton(onClick = { onEditEntry(entry) }, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(24.dp),
                        enabled = !isViewingOlderDate
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = if (isViewingOlderDate) Color(0xFFCCCCCC) else Color(0xFFCF6679), modifier = Modifier.size(14.dp))
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

        // Liabilities Section Header with Add Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Liabilities (${liabilityEntries.size})",
                    color = Color(0xFFCF6679),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onAddEntry,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Liability",
                        tint = Color(0xFFCF6679),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(24.dp),
                        enabled = !isViewingOlderDate
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = if (isViewingOlderDate) Color(0xFFCCCCCC) else Color(0xFFCF6679), modifier = Modifier.size(14.dp))
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

        // Save Buttons at Bottom (shown only when viewing older date)
        if (isViewingOlderDate) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            // Save under older date
                            scope.launch {
                                displayedEntries.forEach { entry ->
                                    repository.updateEntry(entry)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Text("Save under older date", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            // Save under today's date
                            scope.launch {
                                displayedEntries.forEach { entry ->
                                    val updatedEntry = entry.copy(dateTime = "$todayDate ${LocalDate.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")
                                    repository.updateEntry(updatedEntry)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D5016))
                    ) {
                        Text("Save under today's date", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
