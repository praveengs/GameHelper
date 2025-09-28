package com.pgs.gamehelper.models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pgs.gamehelper.data.SessionsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class SessionsViewModel(private val context: Context) : ViewModel() {

    val sessions: StateFlow<List<Session>> =
        SessionsRepository.getSessions(context)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Temp config for new session ---
    private var tempSession: Session? = null

    fun setTempSession(courts: Int, hours: Int, gameDuration: Int) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        tempSession = Session(
            "temp" + UUID.randomUUID().toString(),
            emptyList(),
            courts,
            hours,
            gameDuration,
            LocalDateTime.now().format(formatter)
        )
    }

    fun getTempSession(): Session? = tempSession

    fun addSession(
        players: List<String>,
        courts: Int,
        hours: Int,
        gameDuration: Int,
        onAdded: (Session) -> Unit
    ) {
        viewModelScope.launch {
            val session =
                SessionsRepository.addSession(context, players, courts, hours, gameDuration)
            onAdded(session)
        }
    }

    fun updateSession(updated: Session) {
        viewModelScope.launch {
            SessionsRepository.updateSession(context, updated)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SessionsViewModel(context) as T
        }
    }
}