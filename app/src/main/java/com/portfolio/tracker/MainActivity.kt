package com.portfolio.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import com.portfolio.tracker.data.repository.EntryRepository
import com.portfolio.tracker.ui.screens.AddEntryContent
import com.portfolio.tracker.ui.screens.ChartsContent
import com.portfolio.tracker.ui.screens.DashboardContent
import com.portfolio.tracker.ui.screens.ImportContent
import com.portfolio.tracker.ui.theme.PortfolioTrackerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val repository = EntryRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PortfolioTrackerTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()

                // All entries kept in Compose state so the UI reacts to changes
                var entries by remember { mutableStateOf(repository.getAllEntries()) }

                fun refresh() { entries = repository.getAllEntries() }

                NavHost(navController = navController, startDestination = "dashboard") {

                    composable("dashboard") {
                        DashboardContent(
                            entries = entries,
                            repository = repository,
                            onAddEntry = { navController.navigate("add_entry") },
                            onEditEntry = { entry ->
                                navController.currentBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("editEntry", entry)
                                navController.navigate("edit_entry")
                            },
                            onViewCharts = { navController.navigate("charts") },
                            onImportData = { navController.navigate("import") },
                            onDebug = {},
                            onEntriesChanged = { refresh() }
                        )
                    }

                    composable("add_entry") {
                        AddEntryContent(
                            existingEntry = null,
                            onSave = { entry ->
                                scope.launch {
                                    repository.insertEntry(entry)
                                    refresh()
                                    navController.popBackStack()
                                }
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }

                    composable("edit_entry") {
                        val entry = navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<PortfolioEntryEntity>("editEntry")
                        AddEntryContent(
                            existingEntry = entry,
                            onSave = { updated ->
                                scope.launch {
                                    repository.updateEntry(updated)
                                    refresh()
                                    navController.popBackStack()
                                }
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }

                    composable("charts") {
                        ChartsContent(onBack = { navController.popBackStack() })
                    }

                    composable("import") {
                        ImportContent(
                            onBack = { navController.popBackStack() },
                            onImported = { importedEntries ->
                                scope.launch {
                                    importedEntries.forEach { repository.insertEntry(it) }
                                    refresh()
                                    navController.popBackStack()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
