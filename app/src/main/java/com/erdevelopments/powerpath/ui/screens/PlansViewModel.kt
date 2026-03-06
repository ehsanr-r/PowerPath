package com.erdevelopments.powerpath.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.local.DayEntity
import com.erdevelopments.powerpath.data.local.PlanEntity
import com.erdevelopments.powerpath.data.local.dao.DayDao
import com.erdevelopments.powerpath.data.local.dao.PlanDao
import com.erdevelopments.powerpath.data.local.dao.WorkoutDao
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
class PlansViewModel @Inject constructor(
    private val dayDao: DayDao,
    private val planDao: PlanDao,
    private val workoutDao: WorkoutDao,
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

    val hasWorkouts: StateFlow<Boolean> =
        workoutDao.observeWorkouts()
            .map { it.isNotEmpty() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ✅ show all plans if no day selected, else filter for the day
    val plans: StateFlow<List<PlanEntity>> =
        selectedDayId.flatMapLatest { dayId ->
            if (dayId == null) planDao.observeAllPlans()
            else planDao.observePlansForDay(dayId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDay(dayId: Long) {
        viewModelScope.launch { prefs.setSelectedDay(dayId) }
    }

    fun addPlan() {
        viewModelScope.launch {
            val dayId = selectedDayId.value  // can be null

            val count = planDao.countAll()
            val nextOrder = planDao.nextOrderIndexGlobal()
            val name = "Plan ${count + 1}"

            planDao.insert(
                PlanEntity(
                    dayId = dayId,
                    name = name,
                    orderIndex = nextOrder
                )
            )
        }
    }

    fun renamePlan(planId: Long, newName: String) {
        viewModelScope.launch { planDao.updateName(planId, newName.trim()) }
    }

    fun deletePlan(plan: PlanEntity) {
        viewModelScope.launch { planDao.delete(plan) }
    }
}