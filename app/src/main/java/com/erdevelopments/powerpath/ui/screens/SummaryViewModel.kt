package com.erdevelopments.powerpath.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.local.dao.PlanWorkoutDao
import com.erdevelopments.powerpath.data.local.dao.DayVolume
import com.erdevelopments.powerpath.data.prefs.PrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val prefs: PrefsRepository,
    private val planWorkoutDao: PlanWorkoutDao
) : ViewModel() {

    val selectedUserId: StateFlow<Long?> =
        prefs.selectedUserId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dayVolumes: StateFlow<List<DayVolume>> =
        selectedUserId.flatMapLatest { uid ->
            if (uid == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else planWorkoutDao.observeDayVolumes(uid)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}