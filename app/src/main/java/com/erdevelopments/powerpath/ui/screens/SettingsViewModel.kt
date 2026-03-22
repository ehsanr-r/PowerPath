package com.erdevelopments.powerpath.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.backup.DatabaseBackupManager
import com.erdevelopments.powerpath.data.prefs.PrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsEvent {
    data class Message(val text: String) : SettingsEvent
    object ImportFinished : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupManager: DatabaseBackupManager,
    private val prefs: PrefsRepository
) : ViewModel() {

    private val _isBusy = MutableStateFlow(false)
    val isBusy = _isBusy.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    fun exportBackup(uri: Uri) {
        if (_isBusy.value) return

        viewModelScope.launch {
            _isBusy.value = true
            val result = backupManager.exportTo(uri)
            _isBusy.value = false

            _events.emit(
                if (result.isSuccess) {
                    SettingsEvent.Message("Backup exported successfully.")
                } else {
                    SettingsEvent.Message(
                        result.exceptionOrNull()?.message ?: "Export failed."
                    )
                }
            )
        }
    }

    fun importBackup(uri: Uri) {
        if (_isBusy.value) return

        viewModelScope.launch {
            _isBusy.value = true
            val result = backupManager.importFrom(uri)
            _isBusy.value = false

            if (result.isSuccess) {
                prefs.clearSelections()
                _events.emit(SettingsEvent.ImportFinished)
            } else {
                _events.emit(
                    SettingsEvent.Message(
                        result.exceptionOrNull()?.message ?: "Import failed."
                    )
                )
            }
        }
    }
}