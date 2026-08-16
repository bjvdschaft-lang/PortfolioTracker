package com.portfolio.tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.portfolio.tracker.data.entity.PortfolioEntryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportContent(
    onBack: () -> Unit,
    onImported: (List<PortfolioEntryEntity>) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Data", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Import functionality coming soon",
                fontSize = 18.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}
