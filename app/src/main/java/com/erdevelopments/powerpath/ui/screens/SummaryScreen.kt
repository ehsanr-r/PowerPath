@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.erdevelopments.powerpath.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.ui.components.SimpleBarChart
import com.erdevelopments.powerpath.ui.components.SimpleLineChart
import kotlin.math.roundToInt

@Composable
fun SummaryScreen(vm: SummaryViewModel = hiltViewModel()) {
    val userId by vm.selectedUserId.collectAsStateWithLifecycle()
    val dayVolumes by vm.dayVolumes.collectAsStateWithLifecycle()

    val workouts by vm.workouts.collectAsStateWithLifecycle()
    val selectedWorkoutId by vm.selectedWorkoutId.collectAsStateWithLifecycle()
    val progress by vm.workoutProgress.collectAsStateWithLifecycle()

    // metric selector for progress chart
    var metric by rememberSaveable { mutableStateOf(ProgressMetric.VOLUME) }

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
        // ---------------- Volume by day (existing) ----------------
        item {
            Text("Summary", style = MaterialTheme.typography.titleLarge)
        }

        if (dayVolumes.isEmpty()) {
            item { Text("No data yet. Create days, plans, and mark workouts done.") }
        } else {
            item {
                Text("Volume by Day", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                SimpleBarChart(values = dayVolumes.map { it.volume })
            }

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

        // ---------------- Workout progress (new) ----------------
        item {
            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))
            Text("Workout progress", style = MaterialTheme.typography.titleMedium)
        }

        item {
            WorkoutPicker(
                workouts = workouts.map { it.id to it.name },
                selectedId = selectedWorkoutId,
                onSelected = { vm.selectWorkout(it) }
            )
        }

        if (selectedWorkoutId == null) {
            item {
                Text("Pick a workout to see your progress over days.")
            }
        } else if (progress.isEmpty()) {
            item {
                Text("No completed entries for this workout yet. Mark it Done in a Day.")
            }
        } else {
            item {
                ProgressMetricTabs(
                    selected = metric,
                    onSelected = { metric = it }
                )
            }

            item {
                val values = when (metric) {
                    ProgressMetric.WEIGHT -> progress.map { it.maxWeightKg }
                    ProgressMetric.SETS -> progress.map { it.totalSets.toFloat() }
                    ProgressMetric.REPS -> progress.map { it.totalReps.toFloat() }
                    ProgressMetric.VOLUME -> progress.map { it.totalVolume }
                }

                Text(metric.title, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))

                // Use line chart for all metrics (simple, clear)
                SimpleLineChart(values = values)
            }

            item {
                Spacer(Modifier.height(6.dp))
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

private enum class ProgressMetric(val title: String) {
    WEIGHT("Max weight (kg)"),
    SETS("Total sets"),
    REPS("Total reps"),
    VOLUME("Total volume")
}

@Composable
private fun ProgressMetricTabs(
    selected: ProgressMetric,
    onSelected: (ProgressMetric) -> Unit
) {
    val items = listOf(
        ProgressMetric.WEIGHT,
        ProgressMetric.SETS,
        ProgressMetric.REPS,
        ProgressMetric.VOLUME
    )

    TabRow(selectedTabIndex = items.indexOf(selected)) {
        items.forEachIndexed { idx, m ->
            Tab(
                selected = m == selected,
                onClick = { onSelected(m) },
                text = { Text(m.name) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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