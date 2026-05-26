package com.wps4pin.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wps4pin_history")

data class HistoryEntry(
    val mac: String,
    val pin: String,
    val timestamp: Long
)

class HistoryRepository(private val context: Context) {

    private val key = stringPreferencesKey("history_entries")
    private val disclaimerAcceptedKey = booleanPreferencesKey("disclaimer_accepted")

    val history: Flow<List<HistoryEntry>> = context.dataStore.data.map { prefs ->
        val raw = prefs[key] ?: ""
        if (raw.isBlank()) emptyList()
        else raw.split(";").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size == 3) {
                HistoryEntry(
                    mac = parts[0],
                    pin = parts[1],
                    timestamp = parts[2].toLongOrNull() ?: 0L
                )
            } else null
        }.sortedByDescending { it.timestamp }
    }

    suspend fun add(mac: String, pin: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[key] ?: ""
            // Remove existing entry with same MAC to avoid duplicates
            val filtered = if (current.isBlank()) ""
            else current.split(";").filterNot { entry ->
                val parts = entry.split("|")
                parts.size == 3 && parts[0] == mac
            }.joinToString(";")
            val entry = "$mac|$pin|${System.currentTimeMillis()}"
            prefs[key] = if (filtered.isBlank()) entry else "$filtered;$entry"
        }
    }

    val disclaimerAccepted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[disclaimerAcceptedKey] ?: false
    }

    suspend fun setDisclaimerAccepted() {
        context.dataStore.edit { prefs ->
            prefs[disclaimerAcceptedKey] = true
        }
    }

    suspend fun remove(timestamp: Long) {
        context.dataStore.edit { prefs ->
            val raw = prefs[key] ?: ""
            if (raw.isBlank()) return@edit
            val filtered = raw.split(";").filterNot { entry ->
                val parts = entry.split("|")
                parts.size == 3 && parts[2].toLongOrNull() == timestamp
            }.joinToString(";")
            prefs[key] = filtered
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(key)
        }
    }
}
