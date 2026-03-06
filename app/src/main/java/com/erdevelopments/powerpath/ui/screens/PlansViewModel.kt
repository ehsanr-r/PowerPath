package com.erdevelopments.powerpath.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.local.PlanEntity
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
    private val planDao: PlanDao,
    private val workoutDao: WorkoutDao,
    private val prefs: PrefsRepository
) : ViewModel() {

    val selectedUserId: StateFlow<Long?> =
        prefs.selectedUserId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ✅ enable plan creation only if at least 1 workout exists
    val hasWorkouts: StateFlow<Boolean> =
        workoutDao.observeWorkouts()
            .map { it.isNotEmpty() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ✅ plans are just per user (no day filtering)
    val plans: StateFlow<List<PlanEntity>> =
        selectedUserId.flatMapLatest { uid ->
            if (uid == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else planDao.observePlans(uid)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPlan() {
        viewModelScope.launch {
            val userId = selectedUserId.value ?: return@launch

            val count = planDao.countForUser(userId)
            val nextOrder = planDao.nextOrderIndex(userId)
            val name = "Plan ${count + 1}"

            planDao.insert(
                PlanEntity(
                    userId = userId,
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