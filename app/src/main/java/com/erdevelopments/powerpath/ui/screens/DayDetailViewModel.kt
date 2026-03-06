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
import com.erdevelopments.powerpath.data.local.model.DayPlanProgress
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

    private val dayNameFlows = mutableMapOf<Long, StateFlow<String>>()
    private val assignedPlanFlows = mutableMapOf<Long, StateFlow<List<DayPlanItem>>>()
    private val progressFlows = mutableMapOf<Long, StateFlow<DayPlanProgress?>>()
    private val workoutFlows = mutableMapOf<Long, StateFlow<List<DayPlanWorkoutItem>>>()

    fun observeDayName(dayId: Long): StateFlow<String> {
        return dayNameFlows.getOrPut(dayId) {
            flow { emit(dayDao.getById(dayId)?.name ?: "Day") }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Day")
        }
    }

    fun observeAssignedPlans(dayId: Long): StateFlow<List<DayPlanItem>> {
        return assignedPlanFlows.getOrPut(dayId) {
            dayPlanDao.observeDayPlans(dayId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }
    }

    val allPlansForUser: StateFlow<List<PlanEntity>> =
        selectedUserId.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else planDao.observePlans(uid)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeDayPlanProgress(dayPlanId: Long): StateFlow<DayPlanProgress?> {
        return progressFlows.getOrPut(dayPlanId) {
            dayPlanDao.observeDayPlanProgress(dayPlanId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }
    }

    fun observeWorkouts(dayPlanId: Long): StateFlow<List<DayPlanWorkoutItem>> {
        return workoutFlows.getOrPut(dayPlanId) {
            dayPlanWorkoutDao.observeWorkouts(dayPlanId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }
    }

    fun addPlanToDay(dayId: Long, planId: Long) {
        viewModelScope.launch {
            val nextOrder = dayPlanDao.nextOrderIndex(dayId)
            dayPlanDao.insert(DayPlanEntity(dayId = dayId, planId = planId, orderIndex = nextOrder))
        }
    }

    fun removePlanFromDay(dayPlanId: Long) {
        viewModelScope.launch {
            dayPlanDao.deleteById(dayPlanId)
            progressFlows.remove(dayPlanId)
            workoutFlows.remove(dayPlanId)
        }
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
            dayPlanWorkoutDao.delete(item.dayPlanId, item.workoutId)
        }
    }
}