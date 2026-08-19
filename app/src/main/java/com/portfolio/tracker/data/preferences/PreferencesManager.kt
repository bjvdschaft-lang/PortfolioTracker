package com.portfolio.tracker.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.portfolio.tracker.data.entity.PortfolioEntryEntity
import android.util.Log

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("portfolio_tracker", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val TAG = "PreferencesManager"

    fun saveEntry(entry: PortfolioEntryEntity) {
        try {
            val json = gson.toJson(entry)
            val entries = getAllEntriesFromPrefs().toMutableList()
            entries.removeAll { it.entryId == entry.entryId }
            entries.add(entry)
            
            val entriesJson = gson.toJson(entries)
            prefs.edit().putString("entries", entriesJson).apply()
            Log.d(TAG, "Saved entry: ${entry.description}, Total entries: ${entries.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving entry", e)
        }
    }

    fun getAllEntriesFromPrefs(): List<PortfolioEntryEntity> {
        return try {
            val json = prefs.getString("entries", null) ?: return emptyList()
            val entries = gson.fromJson(json, Array<PortfolioEntryEntity>::class.java)
            Log.d(TAG, "Loaded ${entries.size} entries from preferences")
            entries.toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading entries from preferences", e)
            emptyList()
        }
    }

    fun deleteEntry(entryId: String) {
        try {
            val entries = getAllEntriesFromPrefs().toMutableList()
            entries.removeAll { it.entryId == entryId }
            val entriesJson = gson.toJson(entries)
            prefs.edit().putString("entries", entriesJson).apply()
            Log.d(TAG, "Deleted entry: $entryId, Remaining: ${entries.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting entry", e)
        }
    }

    fun clearAll() {
        try {
            prefs.edit().clear().apply()
            Log.d(TAG, "Cleared all preferences")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing preferences", e)
        }
    }
}
