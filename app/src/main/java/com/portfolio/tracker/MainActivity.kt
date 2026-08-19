package com.portfolio.tracker

import com.portfolio.tracker.ui.screens.DebugScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.portfolio.tracker.data.database.AppDatabase
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import com.portfolio.tracker.data.repository.EntryRepository
import com.portfolio.tracker.ui.screens.DashboardContent
import com.portfolio.tracker.ui.screens.ChartsScreen
import com.portfolio.tracker.ui.screens.ImportScreen

class MainActivity : ComponentActivity() {
    private lateinit var repository: EntryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getInstance(this)
        repository = EntryRepository(database.entryDao())

        setContent {
            var currentScreen by remember { mutableStateOf("dashboard") }
            var selectedEntry by remember { mutableStateOf<PortfolioEntryEntity?>(null) }
            
            // Collect data when on dashboard, charts, or debug screens
            // Automatically stops and restarts when currentScreen changes
            val dbEntries by remember(currentScreen) {
                if (currentScreen in listOf("dashboard", "charts", "debug")) {
                    repository.getAllEntries().collectAsState(initial = emptyList())
                } else {
                    mutableStateOf(emptyList<PortfolioEntryEntity>())
                }
            }

            when (currentScreen) {
                "dashboard" -> DashboardContent(
                    entries = dbEntries,
                    repository = repository,
                    onAddEntry = { currentScreen = "add_entry"; selectedEntry = null },
                    onEditEntry = { entry -> selectedEntry = entry; currentScreen = "add_entry" },
                    onViewCharts = { currentScreen = "charts" },
                    onImportData = { currentScreen = "import" },
                    onDebug = { currentScreen = "debug" },
                    onShutdown = { finish() }
                )
                "add_entry" -> AddEntryContent(
                    repository = repository,
                    entry = selectedEntry,
                    onSave = { currentScreen = "dashboard"; selectedEntry = null },
                    onBack = { currentScreen = "dashboard"; selectedEntry = null }
                )
                "charts" -> ChartsScreen(
                    entries = dbEntries,
                    onBack = { currentScreen = "dashboard" }
                )
                "import" -> ImportScreen(
                    repository = repository,
                    onBack = { currentScreen = "dashboard" },
                    onImportComplete = { currentScreen = "dashboard" }
                )
                "debug" -> DebugScreen(
                    entries = dbEntries,
                    repository = repository,
                    onBack = { currentScreen = "dashboard" }
                )
            }
        }
    }
}
