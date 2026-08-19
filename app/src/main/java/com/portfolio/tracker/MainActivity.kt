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
import android.util.Log
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var repository: EntryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        val database = AppDatabase.getInstance(this)
        // Pass context to repository so it can sync database after operations
        repository = EntryRepository(database.entryDao(), this)
        Log.d("MainActivity", "Repository initialized with database sync capability")

        setContent {
            Log.d("MainActivity", "setContent called, creating UI")
            var currentScreen by remember { mutableStateOf("dashboard") }
            var selectedEntry by remember { mutableStateOf<PortfolioEntryEntity?>(null) }
            
            val dbEntries by repository.getAllEntries().collectAsState(initial = emptyList())
            Log.d("MainActivity", "Collected ${dbEntries.size} entries from database")

            when (currentScreen) {
                "dashboard" -> DashboardContent(
                    entries = dbEntries,
                    repository = repository,
                    onAddEntry = { currentScreen = "add_entry"; selectedEntry = null },
                    onEditEntry = { entry -> selectedEntry = entry; currentScreen = "add_entry" },
                    onViewCharts = { currentScreen = "charts" },
                    onImportData = { currentScreen = "import" },
                    onDebug = { currentScreen = "debug" },
                    onShutdown = { 
                        Log.d("MainActivity", "Shutdown called, syncing database and closing app")
                        AppDatabase.syncDatabase(this@MainActivity)
                        finish() 
                    }
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

    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy called - final database sync")
        AppDatabase.syncDatabase(this)
        super.onDestroy()
    }

    override fun onPause() {
        Log.d("MainActivity", "onPause called - syncing database before app goes to background")
        AppDatabase.syncDatabase(this)
        super.onPause()
    }

    override fun onStop() {
        Log.d("MainActivity", "onStop called - syncing database")
        AppDatabase.syncDatabase(this)
        super.onStop()
    }
}
