package com.erdevelopments.powerpath.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.local.DayPlanEntity
import com.erdevelopments.powerpath.data.local.DayPlanWorkoutEntity
import com.erdevelopments.powerpath.data.local.PlanEntity
import com.erdevelopments.powerpath.data.local.dao.DayDao
import com.erdevelopments.powerpath.data.local.dao.DayPlanDao
import com.erdevelopments.powerpath.data.local.dao.DayPlanWorkoutDao
import com.erdevelopments.powerpath.data.local.dao.PlanDao
import com.erdevelopments.powerpath.data.local.model.DayPlanItem
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutItem
import com.erdevelopments.powerpath.data.prefs.PrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DayDetailViewModel @Inject constructor(
    private val dayDao: DayDao,
    private val planDao: PlanDao,
    private val dayPlanDao: DayPlanDao,
    private val dayPlanWorkoutDao: DayPlanWorkoutDao,
    private val prefs: PrefsRepository
) : ViewModel() {

    val selectedUserId: StateFlow<Long?> =
        prefs.selectedUserId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun observeDayName(dayId: Long): StateFlow<String> =
        flow { emit(dayDao.getById(dayId)?.name ?: "Day") }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Day")

    fun observeAssignedPlans(dayId: Long): StateFlow<List<DayPlanItem>> =
        dayPlanDao.observeDayPlans(dayId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeAllPlansForUser(): StateFlow<List<PlanEntity>> =
        selectedUserId.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else planDao.observePlans(uid)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeWorkouts(dayPlanId: Long): StateFlow<List<DayPlanWorkoutItem>> =
        dayPlanWorkoutDao.observeWorkouts(dayPlanId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPlanToDay(dayId: Long, planId: Long) {
        viewModelScope.launch {
            val nextOrder = dayPlanDao.nextOrderIndex(dayId)
            dayPlanDao.insert(DayPlanEntity(dayId = dayId, planId = planId, orderIndex = nextOrder))
        }
    }

    fun removePlanFromDay(dayPlanId: Long) {
        viewModelScope.launch { dayPlanDao.deleteById(dayPlanId) }
    }

    fun toggleDone(item: DayPlanWorkoutItem, done: Boolean) {
        viewModelScope.launch {
            dayPlanWorkoutDao.upsert(
                DayPlanWorkoutEntity(
                    dayPlanId = item.dayPlanId,
                    workoutId = item.workoutId,
                    isDone = done,
                    weightKg = item.overrideWeightKg,
                    sets = item.overrideSets,
                    reps = item.overrideReps,
                    restSeconds = item.overrideRestSeconds
                )
            )
        }
    }

    fun saveOverrides(
        item: DayPlanWorkoutItem,
        weightKg: Float?,
        sets: Int?,
        reps: Int?,
        restSeconds: Int?
    ) {
        viewModelScope.launch {
            dayPlanWorkoutDao.upsert(
                DayPlanWorkoutEntity(
                    dayPlanId = item.dayPlanId,
                    workoutId = item.workoutId,
                    isDone = item.isDone,
                    weightKg = weightKg,
                    sets = sets,
                    reps = reps,
                    restSeconds = restSeconds
                )
            )
        }
    }

    fun resetToTemplate(item: DayPlanWorkoutItem) {
        viewModelScope.launch {
            // remove overrides row completely (falls back to template values)
            dayPlanWorkoutDao.delete(item.dayPlanId, item.workoutId)
        }
    }
}