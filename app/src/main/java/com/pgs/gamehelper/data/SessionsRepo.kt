package com.pgs.gamehelper.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pgs.gamehelper.models.MatchResult
import com.pgs.gamehelper.models.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private val Context.sessionsDataStore by preferencesDataStore("sessions")

object SessionsRepository {
    private val SESSIONS_KEY = stringPreferencesKey("sessions_json")

    // Very simple serialization (comma/semicolon delimited)
    private fun serialize(sessions: List<Session>): String {
        return sessions.joinToString("||") { s ->
            val matchResultsString = s.matchResults.map { (key, value) ->
                "$key:${value.teamAScore},${value.teamBScore},${value.shuttlesUsed}"
            }.joinToString("&")

            listOf(
                s.id,
                s.players.joinToString(","),
                s.courts,
                s.hours,
                s.gameDuration,
                s.startedAt,
                s.completedGames.joinToString(","),
                s.isLocked,
                s.reshuffleSeed,
                matchResultsString
            ).joinToString(";")
        }
    }

    private fun deserialize(serialized: String): List<Session> {
        if (serialized.isBlank()) return emptyList()
        return serialized.split("||").mapNotNull { sessionStr ->
            val parts = sessionStr.split(";")
            // Be robust for old data that has 9 parts
            if (parts.size < 9) return@mapNotNull null

            val matchResults = if (parts.size < 10 || parts[9].isBlank()) {
                emptyMap()
            } else {
                try {
                    parts[9].split("&").mapNotNull { resultStr ->
                        val resultParts = resultStr.split(":")
                        if (resultParts.size != 2) return@mapNotNull null
                        val matchId = resultParts[0]
                        val scoresAndShuttles = resultParts[1].split(",").mapNotNull { it.toIntOrNull() }
                        if (scoresAndShuttles.size != 3) return@mapNotNull null
                        matchId to MatchResult(scoresAndShuttles[0], scoresAndShuttles[1], scoresAndShuttles[2])
                    }.toMap()
                } catch (e: Exception) {
                    // In case of any parsing error, default to empty map
                    emptyMap()
                }
            }

            Session(
                id = parts[0],
                players = if (parts[1].isBlank()) emptyList() else parts[1].split(","),
                courts = parts[2].toIntOrNull() ?: 0,
                hours = parts[3].toIntOrNull() ?: 0,
                gameDuration = parts[4].toIntOrNull() ?: 15,
                startedAt = parts[5],
                completedGames = if (parts.size < 7 || parts[6].isBlank()) emptySet() else parts[6].split(",")
                    .mapNotNull { it.toIntOrNull() }.toSet(),
                isLocked = parts.getOrNull(7)?.toBoolean() ?: false,
                reshuffleSeed = parts.getOrNull(8)?.toLongOrNull() ?: 0L,
                matchResults = matchResults
            )
        }
    }


    fun getSessions(context: Context): Flow<List<Session>> {
        return context.sessionsDataStore.data.map { prefs ->
            deserialize(prefs[SESSIONS_KEY] ?: "")
        }
    }

    suspend fun saveSessions(context: Context, sessions: List<Session>) {
        context.sessionsDataStore.edit { prefs ->
            prefs[SESSIONS_KEY] = serialize(sessions)
        }
    }

    suspend fun addSession(
        context: Context,
        players: List<String>,
        courts: Int,
        hours: Int,
        gameDuration: Int
    ): Session {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val session = Session(
            id = UUID.randomUUID().toString(),
            players = players,
            courts = courts,
            hours = hours,
            gameDuration = gameDuration,
            startedAt = LocalDateTime.now().format(formatter)
        )
        val current = getSessions(context)
            .map { it }
            .firstOrNull() ?: emptyList()
        saveSessions(context, current + session)
        return session
    }

    suspend fun updateSession(context: Context, updated: Session) {
        val current = getSessions(context)
            .map { it }
            .firstOrNull() ?: emptyList()
        val newList = current.map { if (it.id == updated.id) updated else it }
        saveSessions(context, newList)
    }

    suspend fun removeSession(context: Context, sessionId: String) {
        val current = getSessions(context)
            .map { it }
            .firstOrNull() ?: emptyList()
        val newList = current.filterNot { it.id == sessionId }
        saveSessions(context, newList)
    }
}
