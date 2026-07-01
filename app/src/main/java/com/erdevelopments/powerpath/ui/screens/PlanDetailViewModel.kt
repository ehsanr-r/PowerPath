package com.erdevelopments.powerpath.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.local.PlanWorkoutEntity
import com.erdevelopments.powerpath.data.local.dao.PlanDao
import com.erdevelopments.powerpath.data.local.dao.PlanWorkoutDao
import com.erdevelopments.powerpath.data.local.dao.WorkoutDao
import com.erdevelopments.powerpath.data.local.model.PlanWorkoutItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val planDao: PlanDao,
    private val workoutDao: WorkoutDao,
    private val planWorkoutDao: PlanWorkoutDao
) : ViewModel() {

    private val planId: Long = checkNotNull(savedStateHandle.get<String>("planId")).toLong()

    val planName: StateFlow<String> = flow {
        emit(planDao.getById(planId)?.name ?: "Plan")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Plan")

    val allWorkouts = workoutDao.observeWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val planItems: StateFlow<List<PlanWorkoutItem>> =
        planWorkoutDao.observePlanWorkoutItems(planId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWorkoutToPlan(workoutId: Long, weightKg: Float, sets: Int, reps: Int, restSeconds: Int) {
        viewModelScope.launch {
            val nextOrder = planWorkoutDao.nextOrderIndex(planId)
            planWorkoutDao.upsert(
                PlanWorkoutEntity(
                    planId = planId,
                    workoutId = workoutId,
                    weightKg = weightKg,
                    sets = sets,
                    reps = reps,
                    restSeconds = restSeconds,
                    orderIndex = nextOrder
                )
            )
        }
    }

    fun updatePlanWorkout(workoutId: Long, weightKg: Float, sets: Int, reps: Int, restSeconds: Int) {
        viewModelScope.launch {
            planWorkoutDao.updateValues(planId, workoutId, weightKg, sets, reps, restSeconds)
        }
    }

    fun reorderWorkouts(orderedWorkoutIds: List<Long>) {
        viewModelScope.launch {
            planWorkoutDao.reorderWorkouts(planId, orderedWorkoutIds)
        }
    }

    fun removeFromPlan(workoutId: Long) {
        viewModelScope.launch { planWorkoutDao.deleteFromPlan(planId, workoutId) }
    }
}
