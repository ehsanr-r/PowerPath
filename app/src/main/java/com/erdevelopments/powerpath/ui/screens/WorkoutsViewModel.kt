package com.erdevelopments.powerpath.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdevelopments.powerpath.data.local.WorkoutEntity
import com.erdevelopments.powerpath.data.local.dao.WorkoutDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

val BODY_PARTS = listOf(
    "Chest", "Back", "Shoulders", "Biceps", "Triceps",
    "Quads", "Hamstrings", "Glutes", "Calves",
    "Core", "Cardio", "FullBody", "Mobility"
)

@HiltViewModel
class WorkoutsViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val workouts = workoutDao.observeWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWorkout(
        name: String,
        bodyParts: List<String>,
        desc: String?,
        pickedImageUri: String?
    ) {
        viewModelScope.launch {
            val savedImagePath = pickedImageUri?.let { saveImageToInternalStorage(Uri.parse(it)) }

            workoutDao.insert(
                WorkoutEntity(
                    name = name.trim(),
                    bodyPart = bodyParts.joinToString(", "),
                    description = desc?.takeIf { it.isNotBlank() }?.trim(),
                    imageUri = savedImagePath
                )
            )
        }
    }

    fun updateWorkout(
        oldEntity: WorkoutEntity,
        name: String,
        bodyParts: List<String>,
        desc: String?,
        pickedImageUri: String?,
        removeImage: Boolean
    ) {
        viewModelScope.launch {
            var finalImagePath = oldEntity.imageUri

            when {
                removeImage -> {
                    deleteInternalImageIfOwned(oldEntity.imageUri)
                    finalImagePath = null
                }

                pickedImageUri != null -> {
                    deleteInternalImageIfOwned(oldEntity.imageUri)
                    finalImagePath = saveImageToInternalStorage(Uri.parse(pickedImageUri))
                }
            }

            workoutDao.update(
                oldEntity.copy(
                    name = name.trim(),
                    bodyPart = bodyParts.joinToString(", "),
                    description = desc?.takeIf { it.isNotBlank() }?.trim(),
                    imageUri = finalImagePath
                )
            )
        }
    }

    fun deleteWorkout(entity: WorkoutEntity) {
        viewModelScope.launch {
            deleteInternalImageIfOwned(entity.imageUri)
            workoutDao.delete(entity)
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null

            val dir = File(context.filesDir, "workout_images")
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "workout_${UUID.randomUUID()}.jpg")

            input.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun deleteInternalImageIfOwned(path: String?) {
        if (path.isNullOrBlank()) return

        try {
            val file = File(path)
            val imagesDir = File(context.filesDir, "workout_images")

            if (file.exists() && file.parentFile?.absolutePath == imagesDir.absolutePath) {
                file.delete()
            }
        } catch (_: Exception) {
        }
    }
}