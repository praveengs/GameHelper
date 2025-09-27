package com.pgs.gamehelper.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pgs.gamehelper.data.PlayerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlayerRepository(application)
    val players: StateFlow<Set<String>> =
        repository.playersFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    fun addPlayer(name: String) {
        viewModelScope.launch { repository.addPlayer(name) }
    }

    fun removePlayer(name: String) {
        viewModelScope.launch { repository.removePlayer(name) }
    }
}