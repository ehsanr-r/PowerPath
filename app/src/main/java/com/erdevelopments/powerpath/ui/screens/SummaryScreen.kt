@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.erdevelopments.powerpath.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
            Text("Summary", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Text("Select a user first.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Summary", style = MaterialTheme.typography.titleLarge) }

        // ----------------- Section 1: Volume by day -----------------
        item {
            Text("Volume by Day", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (dayVolumes.isEmpty()) {
                Text("No data yet. Mark workouts as done in a Day to generate summary.")
            } else {
                SimpleBarChart(values = dayVolumes.map { it.volume })
            }
        }

        if (dayVolumes.isNotEmpty()) {
            items(dayVolumes, key = { it.dayId }) { dv ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(dv.dayName)
                        Text("%,.0f".format(dv.volume))
                    }
                }
            }
        }

        // ----------------- Section 2: Workout progress -----------------
        item {
            Spacer(Modifier.height(6.dp))
            Divider()
            Spacer(Modifier.height(6.dp))
            Text("Workout progress", style = MaterialTheme.typography.titleMedium)
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
                item { Text("Pick a workout to see progress across days.") }
            }
            progress.isEmpty() -> {
                item { Text("No completed entries for this workout yet. Mark it Done in Day detail.") }
            }
            else -> {
                item {
                    val barItems = progress.map { p ->
                        WorkoutBarItem(
                            dayLabel = p.dayName,
                            volume = p.totalVolume,
                            weightKg = p.maxWeightKg,
                            sets = p.totalSets,
                            reps = p.totalReps
                        )
                    }

                    Text("Volume bars (left axis = total volume)", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    val context = LocalContext.current

                    WorkoutProgressBarChart(
                        items = barItems,
                        onBarClick = { item ->
                            Toast.makeText(
                                context,
                                "${item.dayLabel}: Volume ${"%,.0f".format(item.volume)}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Text("History", style = MaterialTheme.typography.titleSmall)
                }

                items(progress, key = { it.dayId }) { p ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(p.dayName, style = MaterialTheme.typography.titleMedium)
                            Text("Max weight: ${p.maxWeightKg} kg")
                            Text("Sets: ${p.totalSets} • Total reps: ${p.totalReps}")
                            Text("Volume: %,.0f".format(p.totalVolume))
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
            Divider()
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