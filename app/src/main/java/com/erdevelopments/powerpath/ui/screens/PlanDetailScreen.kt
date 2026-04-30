package com.erdevelopments.powerpath.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.data.local.model.PlanWorkoutItem
import com.erdevelopments.powerpath.ui.components.MAX_REPS
import com.erdevelopments.powerpath.ui.components.MAX_SETS
import com.erdevelopments.powerpath.ui.components.MAX_WEIGHT_KG
import com.erdevelopments.powerpath.ui.components.PowerPathFab
import com.erdevelopments.powerpath.ui.components.FloatValueSlider
import com.erdevelopments.powerpath.ui.components.IntValueSlider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: Long,
    vm: PlanDetailViewModel = hiltViewModel()
) {
    val planName by vm.observePlanName(planId).collectAsStateWithLifecycle()
    val items by vm.planItems(planId).collectAsStateWithLifecycle()
    val allWorkouts by vm.allWorkouts.collectAsStateWithLifecycle()

    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<PlanWorkoutItem?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(planName) }) },
        floatingActionButton = {
            PowerPathFab(onClick = { if (allWorkouts.isNotEmpty()) showAdd = true }, enabled = allWorkouts.isNotEmpty(),) {
                Icon(Icons.Default.Add, contentDescription = "Add workout to plan")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Workouts in this plan", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (items.isEmpty()) {
                Text("No workouts yet. Tap + to add.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.workoutId }) { itx ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(itx.workoutName, style = MaterialTheme.typography.titleMedium)
                                Text("${itx.bodyPart} • ${itx.weightKg}kg • ${itx.sets}x${itx.reps} • Rest ${itx.restSeconds}s")

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    IconButton(onClick = { editTarget = itx }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { vm.removeFromPlan(planId, itx.workoutId) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddWorkoutToPlanDialog(
            workouts = allWorkouts.map { it.id to it.name },
            onDismiss = { showAdd = false },
            onAdd = { workoutId, weight, sets, reps, rest ->
                vm.addWorkoutToPlan(planId, workoutId, weight, sets, reps, rest)
                showAdd = false
            }
        )
    }

    editTarget?.let { target ->
        EditPlanWorkoutDialog(
            item = target,
            onDismiss = { editTarget = null },
            onSave = { w, s, r, rest ->
                vm.updatePlanWorkout(planId, target.workoutId, w, s, r, rest)
                editTarget = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWorkoutToPlanDialog(
    workouts: List<Pair<Long, String>>,
    onDismiss: () -> Unit,
    onAdd: (workoutId: Long, weightKg: Float, sets: Int, reps: Int, restSeconds: Int) -> Unit
) {
    var selectedId by remember { mutableStateOf(workouts.first().first) }
    var weight by remember { mutableFloatStateOf(10f) }
    var sets by remember { mutableIntStateOf(3) }
    var reps by remember { mutableIntStateOf(10) }
    var rest by remember { mutableStateOf("60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add workout to plan") },
        confirmButton = {
            TextButton(
                enabled = rest.toIntOrNull() != null,
                onClick = {
                    onAdd(selectedId, weight, sets, reps, rest.toInt())
                }
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    val selectedName = workouts.firstOrNull { it.first == selectedId }?.second ?: ""
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Workout") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        workouts.forEach { (id, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = { selectedId = id; expanded = false }
                            )
                        }
                    }
                }

                FloatValueSlider(
                    label = "Weight",
                    value = weight,
                    onValueChange = { weight = it },
                    valueRange = 0f..MAX_WEIGHT_KG,
                    stepSize = 0.5f,
                    valueSuffix = "kg"
                )
                IntValueSlider(
                    label = "Sets",
                    value = sets,
                    onValueChange = { sets = it },
                    valueRange = 1..MAX_SETS
                )
                IntValueSlider(
                    label = "Reps",
                    value = reps,
                    onValueChange = { reps = it },
                    valueRange = 1..MAX_REPS
                )
                OutlinedTextField(
                    value = rest,
                    onValueChange = { rest = it },
                    label = { Text("Rest (seconds)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    )
}

@Composable
private fun EditPlanWorkoutDialog(
    item: PlanWorkoutItem,
    onDismiss: () -> Unit,
    onSave: (weightKg: Float, sets: Int, reps: Int, restSeconds: Int) -> Unit
) {
    var weight by remember { mutableFloatStateOf(item.weightKg.coerceIn(0f, MAX_WEIGHT_KG)) }
    var sets by remember { mutableIntStateOf(item.sets.coerceIn(1, MAX_SETS)) }
    var reps by remember { mutableIntStateOf(item.reps.coerceIn(1, MAX_REPS)) }
    var rest by remember { mutableStateOf(item.restSeconds.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${item.workoutName}") },
        confirmButton = {
            TextButton(
                enabled = rest.toIntOrNull() != null,
                onClick = { onSave(weight, sets, reps, rest.toInt()) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FloatValueSlider(
                    label = "Weight",
                    value = weight,
                    onValueChange = { weight = it },
                    valueRange = 0f..MAX_WEIGHT_KG,
                    stepSize = 0.5f,
                    valueSuffix = "kg"
                )
                IntValueSlider(
                    label = "Sets",
                    value = sets,
                    onValueChange = { sets = it },
                    valueRange = 1..MAX_SETS
                )
                IntValueSlider(
                    label = "Reps",
                    value = reps,
                    onValueChange = { reps = it },
                    valueRange = 1..MAX_REPS
                )
                OutlinedTextField(
                    value = rest,
                    onValueChange = { rest = it },
                    label = { Text("Rest (seconds)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    )
}
