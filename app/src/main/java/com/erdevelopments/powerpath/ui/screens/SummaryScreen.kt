@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.erdevelopments.powerpath.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.ui.components.SimpleBarChart
import com.erdevelopments.powerpath.ui.components.WorkoutBarItem
import com.erdevelopments.powerpath.ui.components.WorkoutProgressBarChart

@Composable
fun SummaryScreen(vm: SummaryViewModel = hiltViewModel()) {
    val userId by vm.selectedUserId.collectAsStateWithLifecycle()
    val dayVolumes by vm.dayVolumes.collectAsStateWithLifecycle()

    val workouts by vm.workouts.collectAsStateWithLifecycle()
    val selectedWorkoutId by vm.selectedWorkoutId.collectAsStateWithLifecycle()
    val progress by vm.workoutProgress.collectAsStateWithLifecycle()

    if (userId == null) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Select a user first.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Your training analytics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text(
                "Volume by Day",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            if (dayVolumes.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "No data yet. Mark workouts as done in a Day to generate summary.",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    SimpleBarChart(
                        values = dayVolumes.map { it.volume },
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (dayVolumes.isNotEmpty()) {
            items(dayVolumes, key = { "day_volume_${it.dayId}" }) { dv ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            dv.dayName,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "%,.0f".format(dv.volume),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(4.dp))
            Text(
                "Workout progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            WorkoutPicker(
                workouts = workouts.map { it.id to it.name },
                selectedId = selectedWorkoutId,
                onSelected = { vm.selectWorkout(it) }
            )
        }

        when {
            selectedWorkoutId == null -> {
                item {
                    Text(
                        "Pick a workout to see progress across days.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            progress.isEmpty() -> {
                item {
                    Text(
                        "No completed entries for this workout yet. Mark it Done in Day detail.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                item {
                    Text(
                        "Volume per day",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    val barItems = progress.map { p ->
                        WorkoutBarItem(
                            dayLabel = p.dayName,
                            volume = p.totalVolume,
                            weightKg = p.maxWeightKg,
                            sets = p.totalSets,
                            reps = p.totalReps
                        )
                    }
                    val context = LocalContext.current

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        WorkoutProgressBarChart(
                            items = barItems,
                            modifier = Modifier.padding(12.dp),
                            onBarClick = { item ->
                                Toast.makeText(
                                    context,
                                    "${item.dayLabel}: Volume ${"%,.0f".format(item.volume)}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "History",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                items(progress, key = { "workout_progress_${it.dayId}" }) { p ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(
                            Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                p.dayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Max weight: ${p.maxWeightKg} kg",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Sets: ${p.totalSets}  •  Total reps: ${p.totalReps}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Volume: %,.0f".format(p.totalVolume),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutPicker(
    workouts: List<Pair<Long, String>>,
    selectedId: Long?,
    onSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedName = workouts.firstOrNull { it.first == selectedId }?.second ?: "Select workout"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Workout") },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            workouts.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Clear selection") },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
        }
    }
}
