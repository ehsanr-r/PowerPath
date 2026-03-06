package com.erdevelopments.powerpath.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.local.DayEntity
import com.erdevelopments.powerpath.data.local.PlanEntity
import com.erdevelopments.powerpath.data.local.dao.DayDao
import com.erdevelopments.powerpath.data.local.dao.PlanDao
import com.erdevelopments.powerpath.data.prefs.PrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val dayDao: DayDao,
    private val planDao: PlanDao,
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

    val plans: StateFlow<List<PlanEntity>> =
        selectedDayId.flatMapLatest { dayId ->
            if (dayId == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else planDao.observePlans(dayId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDay(dayId: Long) {
        viewModelScope.launch { prefs.setSelectedDay(dayId) }
    }

    fun addPlan() {
        viewModelScope.launch {
            val dayId = selectedDayId.value ?: return@launch
            val count = planDao.countForDay(dayId)
            val nextOrder = planDao.nextOrderIndex(dayId)
            val name = "Plan ${count + 1}"
            planDao.insert(PlanEntity(dayId = dayId, name = name, orderIndex = nextOrder))
        }
    }

    fun renamePlan(planId: Long, newName: String) {
        viewModelScope.launch { planDao.updateName(planId, newName.trim()) }
    }

    fun deletePlan(plan: PlanEntity) {
        viewModelScope.launch { planDao.delete(plan) }
    }
}