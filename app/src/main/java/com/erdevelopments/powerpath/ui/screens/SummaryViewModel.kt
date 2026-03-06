package com.erdevelopments.powerpath.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.local.dao.DayPlanWorkoutDao
import com.erdevelopments.powerpath.data.local.dao.DayPlanWorkoutSetDao
import com.erdevelopments.powerpath.data.local.dao.WorkoutDao
import com.erdevelopments.powerpath.data.local.model.DayVolume
import com.erdevelopments.powerpath.data.local.model.WorkoutProgressPoint
import com.erdevelopments.powerpath.data.prefs.PrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val prefs: PrefsRepository,
    private val dayPlanWorkoutDao: DayPlanWorkoutDao,
    private val dayPlanWorkoutSetDao: DayPlanWorkoutSetDao,
    private val workoutDao: WorkoutDao
) : ViewModel() {

    val selectedUserId: StateFlow<Long?> =
        prefs.selectedUserId
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Volume by day
    val dayVolumes: StateFlow<List<DayVolume>> =
        selectedUserId
            .flatMapLatest { uid ->
                if (uid == null) flowOf(emptyList())
                else dayPlanWorkoutDao.observeDayVolumes(uid)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Workouts for dropdown
    val workouts =
        workoutDao.observeWorkouts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedWorkoutId = MutableStateFlow<Long?>(null)
    val selectedWorkoutId: StateFlow<Long?> = _selectedWorkoutId.asStateFlow()

    fun selectWorkout(id: Long?) {
        _selectedWorkoutId.value = id
    }

    // Progress per day for selected workout
    val workoutProgress: StateFlow<List<WorkoutProgressPoint>> =
        combine(selectedUserId, selectedWorkoutId) { uid, wid -> uid to wid }
            .flatMapLatest { (uid, wid) ->
                if (uid == null || wid == null) {
                    flowOf(emptyList())
                } else {
                    dayPlanWorkoutSetDao.observeWorkoutProgress(uid, wid)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}