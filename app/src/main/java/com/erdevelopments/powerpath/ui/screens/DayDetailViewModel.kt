package com.erdevelopments.powerpath.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.local.DayPlanEntity
import com.erdevelopments.powerpath.data.local.DayPlanWorkoutEntity
import com.erdevelopments.powerpath.data.local.DayPlanWorkoutSetEntity
import com.erdevelopments.powerpath.data.local.PlanEntity
import com.erdevelopments.powerpath.data.local.dao.DayDao
import com.erdevelopments.powerpath.data.local.dao.DayPlanDao
import com.erdevelopments.powerpath.data.local.dao.DayPlanWorkoutDao
import com.erdevelopments.powerpath.data.local.dao.DayPlanWorkoutSetDao
import com.erdevelopments.powerpath.data.local.dao.PlanDao
import com.erdevelopments.powerpath.data.local.model.DayPlanItem
import com.erdevelopments.powerpath.data.local.model.DayPlanProgress
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutItem
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutSetItem
import com.erdevelopments.powerpath.data.local.model.WorkoutHistorySetItem
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
    private val dayPlanWorkoutSetDao: DayPlanWorkoutSetDao,
    private val prefs: PrefsRepository
) : ViewModel() {

    val selectedUserId: StateFlow<Long?> =
        prefs.selectedUserId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val dayNameFlows = mutableMapOf<Long, StateFlow<String>>()
    private val assignedPlanFlows = mutableMapOf<Long, StateFlow<List<DayPlanItem>>>()
    private val progressFlows = mutableMapOf<Long, StateFlow<DayPlanProgress?>>()
    private val workoutFlows = mutableMapOf<Long, StateFlow<List<DayPlanWorkoutItem>>>()
    private val setFlows = mutableMapOf<String, StateFlow<List<DayPlanWorkoutSetItem>>>()
    private val historyFlows = mutableMapOf<String, StateFlow<List<WorkoutHistorySetItem>>>()

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

    fun observeWorkoutSets(dayPlanId: Long, workoutId: Long): StateFlow<List<DayPlanWorkoutSetItem>> {
        val key = "$dayPlanId-$workoutId"
        return setFlows.getOrPut(key) {
            dayPlanWorkoutSetDao.observeSets(dayPlanId, workoutId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }
    }

    fun observeWorkoutHistory(dayPlanId: Long, workoutId: Long): StateFlow<List<WorkoutHistorySetItem>> {
        val key = "$dayPlanId-$workoutId"
        return historyFlows.getOrPut(key) {
            dayPlanWorkoutSetDao.observePreviousCompletedSession(dayPlanId, workoutId)
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

    suspend fun ensureDefaultSets(
        item: DayPlanWorkoutItem,
        preferHistory: Boolean = true
    ) {
        val current = dayPlanWorkoutSetDao.observeSets(item.dayPlanId, item.workoutId).first()
        if (current.isNotEmpty()) return

        val historyBySetNumber = if (preferHistory) {
            dayPlanWorkoutSetDao.observePreviousCompletedSession(item.dayPlanId, item.workoutId)
                .first()
                .associateBy { it.setNumber }
        } else {
            emptyMap()
        }

        val sets = (1..item.effectiveSets).map { setNo ->
            val previousSet = historyBySetNumber[setNo]
            DayPlanWorkoutSetEntity(
                dayPlanId = item.dayPlanId,
                workoutId = item.workoutId,
                setNumber = setNo,
                weightKg = previousSet?.weightKg ?: item.effectiveWeightKg,
                reps = previousSet?.reps ?: item.effectiveReps,
                isDone = false
            )
        }
        dayPlanWorkoutSetDao.upsertAll(sets)
    }

    fun ensureSets(item: DayPlanWorkoutItem) {
        viewModelScope.launch { ensureDefaultSets(item) }
    }

    fun toggleSet(
        item: DayPlanWorkoutItem,
        setNumber: Int,
        weightKg: Float,
        reps: Int,
        done: Boolean
    ) {
        viewModelScope.launch {
            ensureDefaultSets(item)
            dayPlanWorkoutSetDao.upsert(
                DayPlanWorkoutSetEntity(
                    dayPlanId = item.dayPlanId,
                    workoutId = item.workoutId,
                    setNumber = setNumber,
                    weightKg = weightKg,
                    reps = reps,
                    isDone = done
                )
            )
        }
    }

    fun updateSet(
        item: DayPlanWorkoutItem,
        setNumber: Int,
        weightKg: Float,
        reps: Int,
        done: Boolean
    ) {
        viewModelScope.launch {
            ensureDefaultSets(item)
            dayPlanWorkoutSetDao.upsert(
                DayPlanWorkoutSetEntity(
                    dayPlanId = item.dayPlanId,
                    workoutId = item.workoutId,
                    setNumber = setNumber,
                    weightKg = weightKg,
                    reps = reps,
                    isDone = done
                )
            )
        }
    }

    fun toggleAllSets(item: DayPlanWorkoutItem, done: Boolean) {
        viewModelScope.launch {
            ensureDefaultSets(item)
            val sets = observeWorkoutSets(item.dayPlanId, item.workoutId).value
            val updated = sets.map {
                DayPlanWorkoutSetEntity(
                    dayPlanId = it.dayPlanId,
                    workoutId = it.workoutId,
                    setNumber = it.setNumber,
                    weightKg = it.weightKg,
                    reps = it.reps,
                    isDone = done
                )
            }
            dayPlanWorkoutSetDao.upsertAll(updated)
        }
    }

    fun saveWorkoutOverrides(
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
                    isDone = false,
                    weightKg = weightKg,
                    sets = sets,
                    reps = reps,
                    restSeconds = restSeconds
                )
            )

            dayPlanWorkoutSetDao.deleteForWorkout(item.dayPlanId, item.workoutId)
            ensureDefaultSets(
                item.copy(
                    overrideWeightKg = weightKg,
                    overrideSets = sets,
                    overrideReps = reps,
                    overrideRestSeconds = restSeconds
                ),
                preferHistory = false
            )
        }
    }

    fun resetToTemplate(item: DayPlanWorkoutItem) {
        viewModelScope.launch {
            dayPlanWorkoutDao.delete(item.dayPlanId, item.workoutId)
            dayPlanWorkoutSetDao.deleteForWorkout(item.dayPlanId, item.workoutId)
            ensureDefaultSets(
                item.copy(
                    overrideWeightKg = null,
                    overrideSets = null,
                    overrideReps = null,
                    overrideRestSeconds = null
                ),
                preferHistory = false
            )
        }
    }
}
