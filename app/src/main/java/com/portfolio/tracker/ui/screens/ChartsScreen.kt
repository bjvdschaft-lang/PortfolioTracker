package com.portfolio.tracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ChartsScreen(
    entries: List<PortfolioEntryEntity>,
    onBack: () -> Unit
) {
    val totalAssets = entries
        .filter { it.type == "Assets" }
        .sumOf { it.convertedAmount }

    val totalLiabilities = entries
        .filter { it.type == "Liabilities" }
        .sumOf { it.convertedAmount }

    val netWorth = totalAssets - totalLiabilities

    // Group by category
    val assetsByCategory = entries
        .filter { it.type == "Assets" }
        .groupBy { it.category }
        .mapValues { (_, items) -> items.sumOf { it.convertedAmount } }

    val liabilitiesByCategory = entries
        .filter { it.type == "Liabilities" }
        .groupBy { it.category }
        .mapValues { (_, items) -> items.sumOf { it.convertedAmount } }

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
                text = "Charts & Analysis",
                color = Color(0xFF1F2328),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Net Worth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Net Worth", color = Color.White, fontSize = 14.sp)
                    Text(
                        text = "€ ${"%.2f".format(netWorth)}",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Pie Chart - Assets vs Liabilities
            Text(
                text = "Net Worth Breakdown",
                color = Color(0xFF1F2328),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (totalAssets > 0 || totalLiabilities > 0) {
                        PieChart(
                            assetValue = totalAssets,
                            liabilityValue = totalLiabilities,
                            modifier = Modifier
                                .size(250.dp)
                                .padding(bottom = 20.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(0xFF03DAC6))
                                )
                                Column {
                                    Text(
                                        text = "Assets",
                                        color = Color(0xFF6B7280),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "€ ${"%.2f".format(totalAssets)}",
                                        color = Color(0xFF03DAC6),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(0xFFCF6679))
                                )
                                Column {
                                    Text(
                                        text = "Liabilities",
                                        color = Color(0xFF6B7280),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "€ ${"%.2f".format(totalLiabilities)}",
                                        color = Color(0xFFCF6679),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No data available",
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            }

            // Assets by Category
            if (assetsByCategory.isNotEmpty()) {
                Text(
                    text = "Assets by Category",
                    color = Color(0xFF1F2328),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        assetsByCategory.forEach { (category, amount) ->
                            CategoryBar(
                                category = category,
                                amount = amount,
                                total = totalAssets,
                                color = Color(0xFF03DAC6)
                            )
                        }
                    }
                }
            }

            // Liabilities by Category
            if (liabilitiesByCategory.isNotEmpty()) {
                Text(
                    text = "Liabilities by Category",
                    color = Color(0xFF1F2328),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        liabilitiesByCategory.forEach { (category, amount) ->
                            CategoryBar(
                                category = category,
                                amount = amount,
                                total = totalLiabilities,
                                color = Color(0xFFCF6679)
                            )
                        }
                    }
                }
            }

            // Summary Statistics
            Text(
                text = "Summary",
                color = Color(0xFF1F2328),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    StatRow(
                        label = "Total Assets",
                        value = "€ ${"%.2f".format(totalAssets)}",
                        color = Color(0xFF03DAC6)
                    )
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    StatRow(
                        label = "Total Liabilities",
                        value = "€ ${"%.2f".format(totalLiabilities)}",
                        color = Color(0xFFCF6679)
                    )
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    StatRow(
                        label = "Net Worth",
                        value = "€ ${"%.2f".format(netWorth)}",
                        color = Color(0xFF6200EE)
                    )
                    if (totalAssets > 0) {
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        val debtRatio = (totalLiabilities / totalAssets * 100)
                        StatRow(
                            label = "Debt Ratio",
                            value = "${"%.1f".format(debtRatio)}%",
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(
    assetValue: Double,
    liabilityValue: Double,
    modifier: Modifier = Modifier
) {
    val total = assetValue + liabilityValue
    val assetPercentage = if (total > 0) assetValue / total else 0.0
    val assetAngle = assetPercentage * 360f

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Assets (teal)
        drawArc(
            color = Color(0xFF03DAC6),
            startAngle = -90f,
            sweepAngle = assetAngle.toFloat(),
            useCenter = true,
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            topLeft = androidx.compose.ui.geometry.Offset(
                centerX - radius,
                centerY - radius
            )
        )

        // Liabilities (pink)
        drawArc(
            color = Color(0xFFCF6679),
            startAngle = -90f + assetAngle.toFloat(),
            sweepAngle = (360f - assetAngle.toFloat()),
            useCenter = true,
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            topLeft = androidx.compose.ui.geometry.Offset(
                centerX - radius,
                centerY - radius
            )
        )

        // Center circle (for donut effect)
        drawCircle(
            color = Color(0xFFF2F3F5),
            radius = radius * 0.6f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
    }
}

@Composable
fun CategoryBar(
    category: String,
    amount: Double,
    total: Double,
    color: Color
) {
    val percentage = if (total > 0) (amount / total) else 0.0

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                color = Color(0xFF1F2328),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "€ ${"%.2f".format(amount)}",
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFFE3E5E8), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage.toFloat())
                    .background(color, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            )
        }

        Text(
            text = "${"%.1f".format(percentage * 100)}%",
            color = Color(0xFF6B7280),
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun StatRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF6B7280),
            fontSize = 12.sp,
        )
        Text(
            text = value,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}