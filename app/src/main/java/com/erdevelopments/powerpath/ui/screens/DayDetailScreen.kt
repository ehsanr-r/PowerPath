@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.erdevelopments.powerpath.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.data.local.PlanEntity
import com.erdevelopments.powerpath.data.local.model.DayPlanItem
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutItem
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutSetItem

@Composable
fun DayDetailScreen(
    dayId: Long,
    vm: DayDetailViewModel = hiltViewModel()
) {
    val dayName by vm.observeDayName(dayId).collectAsStateWithLifecycle()
    val assignedPlans by vm.observeAssignedPlans(dayId).collectAsStateWithLifecycle()
    val allPlans by vm.allPlansForUser.collectAsStateWithLifecycle()

    var showAddPlan by remember { mutableStateOf(false) }
    var expandedDayPlanId by remember { mutableLongStateOf(-1L) }

    val availablePlans = allPlans

    Scaffold(
        topBar = { TopAppBar(title = { Text(dayName) }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (availablePlans.isNotEmpty()) showAddPlan = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add plan to day")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Day activity", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (assignedPlans.isEmpty()) {
                Text("No plans assigned to this day. Tap + to add one.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(
                        items = assignedPlans,
                        key = { "day_plan_${it.dayPlanId}" }
                    ) { dayPlan ->
                        val expanded = expandedDayPlanId == dayPlan.dayPlanId

                        DayPlanCardContainer(
                            dayPlan = dayPlan,
                            expanded = expanded,
                            onToggleExpand = {
                                expandedDayPlanId =
                                    if (expanded) -1L else dayPlan.dayPlanId
                            },
                            onRemove = {
                                if (expandedDayPlanId == dayPlan.dayPlanId) {
                                    expandedDayPlanId = -1L
                                }
                                vm.removePlanFromDay(dayPlan.dayPlanId)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddPlan) {
        AddPlanToDayDialog(
            plans = availablePlans,
            onDismiss = { showAddPlan = false },
            onAdd = { planId ->
                vm.addPlanToDay(dayId, planId)
                showAddPlan = false
            }
        )
    }
}

@Composable
private fun DayPlanCardContainer(
    dayPlan: DayPlanItem,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onRemove: () -> Unit,
    vm: DayDetailViewModel = hiltViewModel()
) {
    val progress by vm.observeDayPlanProgress(dayPlan.dayPlanId).collectAsStateWithLifecycle()
    val workouts by vm.observeWorkouts(dayPlan.dayPlanId).collectAsStateWithLifecycle()

    DayPlanCard(
        dayPlan = dayPlan,
        totalWorkouts = progress?.totalWorkouts ?: 0,
        doneWorkouts = progress?.doneWorkouts ?: 0,
        expanded = expanded,
        onToggleExpand = onToggleExpand,
        onRemove = onRemove,
        workouts = if (expanded) workouts else emptyList()
    )
}

@Composable
private fun DayPlanCard(
    dayPlan: DayPlanItem,
    totalWorkouts: Int,
    doneWorkouts: Int,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onRemove: () -> Unit,
    workouts: List<DayPlanWorkoutItem>,
    vm: DayDetailViewModel = hiltViewModel()
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(dayPlan.planName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$doneWorkouts/$totalWorkouts workouts complete",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove plan from day")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (expanded) "Hide workouts" else "Show workouts",
                modifier = Modifier.clickable { onToggleExpand() },
                style = MaterialTheme.typography.labelLarge
            )

            if (expanded) {
                Spacer(Modifier.height(8.dp))

                if (workouts.isEmpty()) {
                    Text("This plan has no workouts (add workouts in Plans tab).")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        workouts.forEach { item ->
                            vm.ensureSets(item)
                            WorkoutWithSetsCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutWithSetsCard(
    item: DayPlanWorkoutItem,
    vm: DayDetailViewModel = hiltViewModel()
) {
    val sets by vm.observeWorkoutSets(item.dayPlanId, item.workoutId).collectAsStateWithLifecycle()
    var editTarget by remember { mutableStateOf<DayPlanWorkoutItem?>(null) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.weight(1f)) {
                    Checkbox(
                        checked = item.isDone,
                        onCheckedChange = { checked ->
                            vm.toggleAllSets(item, checked)
                        }
                    )
                    Column(Modifier.padding(top = 6.dp)) {
                        Text(item.workoutName)
                        Text(
                            "${item.bodyPart} • ${item.effectiveWeightKg}kg • ${item.effectiveSets}x${item.effectiveReps} • Rest ${item.effectiveRestSeconds}s",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                TextButton(onClick = { editTarget = item }) {
                    Text("Edit")
                }
            }

            sets.forEach { setItem ->
                SetRow(
                    setItem = setItem,
                    onCheckedChange = { checked ->
                        vm.toggleSet(
                            item = item,
                            setNumber = setItem.setNumber,
                            weightKg = setItem.weightKg,
                            reps = setItem.reps,
                            done = checked
                        )
                    },
                    onValueChange = { weight, reps ->
                        vm.updateSet(
                            item = item,
                            setNumber = setItem.setNumber,
                            weightKg = weight,
                            reps = reps,
                            done = setItem.isDone
                        )
                    }
                )
            }
        }
    }

    editTarget?.let {
        EditDayWorkoutDialog(
            item = it,
            onDismiss = { editTarget = null },
            onSave = { w, s, r, rest ->
                vm.saveWorkoutOverrides(it, w, s, r, rest)
                editTarget = null
            },
            onReset = {
                vm.resetToTemplate(it)
                editTarget = null
            }
        )
    }
}

@Composable
private fun SetRow(
    setItem: DayPlanWorkoutSetItem,
    onCheckedChange: (Boolean) -> Unit,
    onValueChange: (Float, Int) -> Unit
) {
    var weight by remember(setItem.weightKg) { mutableStateOf(setItem.weightKg.toString()) }
    var reps by remember(setItem.reps) { mutableStateOf(setItem.reps.toString()) }

    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = setItem.isDone,
                onCheckedChange = onCheckedChange
            )

            Text("Set ${setItem.setNumber}", modifier = Modifier.padding(top = 14.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = {
                    weight = it
                    val w = it.toFloatOrNull()
                    val r = reps.toIntOrNull()
                    if (w != null && r != null) onValueChange(w, r)
                },
                label = { Text("Weight") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = reps,
                onValueChange = {
                    reps = it
                    val w = weight.toFloatOrNull()
                    val r = it.toIntOrNull()
                    if (w != null && r != null) onValueChange(w, r)
                },
                label = { Text("Reps") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
private fun AddPlanToDayDialog(
    plans: List<PlanEntity>,
    onDismiss: () -> Unit,
    onAdd: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedId by remember(plans) { mutableLongStateOf(plans.firstOrNull()?.id ?: 0L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add plan to day") },
        confirmButton = {
            TextButton(enabled = selectedId != 0L, onClick = { onAdd(selectedId) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            if (plans.isEmpty()) {
                Text("No plans available.")
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    val selectedName =
                        plans.firstOrNull { it.id == selectedId }?.name ?: "Select a plan"

                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Plan") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        plans.forEach { plan ->
                            DropdownMenuItem(
                                text = { Text(plan.name) },
                                onClick = {
                                    selectedId = plan.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun EditDayWorkoutDialog(
    item: DayPlanWorkoutItem,
    onDismiss: () -> Unit,
    onSave: (Float?, Int?, Int?, Int?) -> Unit,
    onReset: () -> Unit
) {
    var weight by remember {
        mutableStateOf((item.overrideWeightKg ?: item.templateWeightKg).toString())
    }
    var sets by remember {
        mutableStateOf((item.overrideSets ?: item.templateSets).toString())
    }
    var reps by remember {
        mutableStateOf((item.overrideReps ?: item.templateReps).toString())
    }
    var rest by remember {
        mutableStateOf((item.overrideRestSeconds ?: item.templateRestSeconds).toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit: ${item.workoutName}") },
        confirmButton = {
            TextButton(
                enabled = weight.toFloatOrNull() != null &&
                        sets.toIntOrNull() != null &&
                        reps.toIntOrNull() != null &&
                        rest.toIntOrNull() != null,
                onClick = {
                    onSave(
                        weight.toFloat(),
                        sets.toInt(),
                        reps.toInt(),
                        rest.toInt()
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Plan defaults: ${item.templateWeightKg}kg • ${item.templateSets}x${item.templateReps} • Rest ${item.templateRestSeconds}s",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = sets,
                    onValueChange = { sets = it },
                    label = { Text("Sets") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = rest,
                    onValueChange = { rest = it },
                    label = { Text("Rest (seconds)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedButton(onClick = onReset) {
                    Text("Reset to plan defaults")
                }
            }
        }
    )
}