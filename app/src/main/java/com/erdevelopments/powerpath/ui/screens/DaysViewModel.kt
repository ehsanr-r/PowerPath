package com.erdevelopments.powerpath.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.local.DayEntity
import com.erdevelopments.powerpath.data.local.dao.DayDao
import com.erdevelopments.powerpath.data.prefs.PrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DaysViewModel @Inject constructor(
    private val dayDao: DayDao,
    private val prefs: PrefsRepository
) : ViewModel() {

    val selectedUserId: StateFlow<Long?> =
        prefs.selectedUserId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedDayId: StateFlow<Long?> =
        prefs.selectedDayId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val days: StateFlow<List<DayEntity>> =
        selectedUserId.flatMapLatest { uid ->
            if (uid == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else dayDao.observeDays(uid)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDay(dayId: Long) {
        viewModelScope.launch { prefs.setSelectedDay(dayId) }
    }

    fun addDay() {
        viewModelScope.launch {
            val uid = selectedUserId.value ?: return@launch
            val count = dayDao.countForUser(uid)
            val nextOrder = dayDao.nextOrderIndex(uid)
            val name = "Day ${count + 1}"
            val id = dayDao.insert(DayEntity(userId = uid, name = name, orderIndex = nextOrder))
            prefs.setSelectedDay(id)
        }
    }

    fun renameDay(dayId: Long, newName: String) {
        viewModelScope.launch { dayDao.updateName(dayId, newName.trim()) }
    }

    fun deleteDay(day: DayEntity) {
        viewModelScope.launch { dayDao.delete(day) }
    }
}