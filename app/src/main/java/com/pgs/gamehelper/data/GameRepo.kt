package com.pgs.gamehelper.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.completedGamesDataStore by preferencesDataStore("completed_games")

object CompletedGamesRepository {
    private fun keyForSession(sessionId: String) = stringPreferencesKey("completed_$sessionId")
    fun getCompletedGames(context: Context, sessionId: String): Flow<Set<Int>> {
        return context.completedGamesDataStore.data.map { prefs ->
            prefs[keyForSession(sessionId)]?.split(
                ","
            )?.filter { it.isNotBlank() }?.map { it.toInt() }?.toSet() ?: emptySet()
        }
    }

    suspend fun saveCompletedGames(context: Context, sessionId: String, completed: Set<Int>) {
        context.completedGamesDataStore.edit { prefs ->
            prefs[keyForSession(sessionId)] = completed.joinToString(",")
        }
    }
}
