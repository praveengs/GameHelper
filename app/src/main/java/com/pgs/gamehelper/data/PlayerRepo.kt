package com.pgs.gamehelper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("players")

class PlayerRepository(private val context: Context) {
    companion object {
        private val PLAYER_SET_KEY = stringSetPreferencesKey("players_set")
    }

    val playersFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[PLAYER_SET_KEY] ?: emptySet()
    }

    suspend fun addPlayer(name: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[PLAYER_SET_KEY] ?: emptySet()
            prefs[PLAYER_SET_KEY] = current + name
        }
    }

    suspend fun removePlayer(name: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[PLAYER_SET_KEY] ?: emptySet()
            prefs[PLAYER_SET_KEY] = current - name
        }
    }
}