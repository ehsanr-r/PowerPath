package com.erdevelopments.powerpath.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.local.WorkoutEntity
import com.erdevelopments.powerpath.data.local.dao.WorkoutDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

val BODY_PARTS = listOf(
    "Chest", "Back", "Shoulders", "Biceps", "Triceps",
    "Quads", "Hamstrings", "Glutes", "Calves",
    "Core", "Cardio", "FullBody", "Mobility"
)

@HiltViewModel
class WorkoutsViewModel @Inject constructor(
    private val workoutDao: WorkoutDao
) : ViewModel() {

    val workouts = workoutDao.observeWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWorkout(name: String, bodyPart: String, desc: String?, imageUri: String?) {
        viewModelScope.launch {
            workoutDao.insert(
                WorkoutEntity(
                    name = name.trim(),
                    bodyPart = bodyPart,
                    description = desc?.takeIf { it.isNotBlank() }?.trim(),
                    imageUri = imageUri
                )
            )
        }
    }

    fun updateWorkout(entity: WorkoutEntity) {
        viewModelScope.launch { workoutDao.update(entity) }
    }

    fun deleteWorkout(entity: WorkoutEntity) {
        viewModelScope.launch { workoutDao.delete(entity) }
    }
}