@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.erdevelopments.powerpath.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.data.local.PlanEntity
import com.erdevelopments.powerpath.data.local.model.DayPlanItem
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutItem
import com.erdevelopments.powerpath.ui.components.PowerPathFab

@Composable
fun DayDetailScreen(
    dayId: Long,
    vm: DayDetailViewModel = hiltViewModel()
) {
    val dayName by vm.observeDayName(dayId).collectAsStateWithLifecycle()
    val assignedPlans by vm.observeAssignedPlans(dayId).collectAsStateWithLifecycle()
    val allPlans by vm.observeAllPlansForUser().collectAsStateWithLifecycle()

    var showAddPlan by remember { mutableStateOf(false) }
    val expanded = remember { mutableStateMapOf<Long, Boolean>() }

    Scaffold(
        topBar = { TopAppBar(title = { Text(dayName) }) },
        floatingActionButton = {
            PowerPathFab(onClick = { showAddPlan = true }, enabled = allPlans.isNotEmpty()) {
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
                    items(assignedPlans, key = { it.dayPlanId }) { dp ->
                        val isOpen = expanded[dp.dayPlanId] ?: false
                        DayPlanCard(
                            dayPlan = dp,
                            expanded = isOpen,
                            onToggleExpand = { expanded[dp.dayPlanId] = !isOpen },
                            onRemove = { vm.removePlanFromDay(dp.dayPlanId) },
                            workouts = vm.observeWorkouts(dp.dayPlanId).collectAsStateWithLifecycle().value,
                            onToggleDone = { item, done -> vm.toggleDone(item, done) },
                            onSaveOverrides = { item, w, s, r, rest -> vm.saveOverrides(item, w, s, r, rest) },
                            onReset = { item -> vm.resetToTemplate(item) }
                        )
                    }
                }
            }
        }
    }

    if (showAddPlan) {
        AddPlanToDayDialog(
            plans = allPlans,
            onDismiss = { showAddPlan = false },
            onAdd = { planId ->
                vm.addPlanToDay(dayId, planId)
                showAddPlan = false
            }
        )
    }
}

@Composable
private fun DayPlanCard(
    dayPlan: DayPlanItem,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onRemove: () -> Unit,
    workouts: List<DayPlanWorkoutItem>,
    onToggleDone: (DayPlanWorkoutItem, Boolean) -> Unit,
    onSaveOverrides: (DayPlanWorkoutItem, Float?, Int?, Int?, Int?) -> Unit,
    onReset: (DayPlanWorkoutItem) -> Unit
) {
    var editTarget by remember { mutableStateOf<DayPlanWorkoutItem?>(null) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(dayPlan.planName, style = MaterialTheme.typography.titleMedium)
                    Text("${dayPlan.doneWorkouts}/${dayPlan.totalWorkouts} done", style = MaterialTheme.typography.bodySmall)
                }
                Row {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove plan from day")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                if (expanded) "Hide workouts" else "Show workouts",
                modifier = Modifier.clickable { onToggleExpand() },
                style = MaterialTheme.typography.labelLarge
            )

            if (expanded) {
                Spacer(Modifier.height(8.dp))

                if (workouts.isEmpty()) {
                    Text("This plan has no workouts (add workouts in Plans tab).")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        workouts.forEach { item ->
                            Row(
                                Modifier.fillMaxWidth().clickable { editTarget = item },
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = item.isDone,
                                        onCheckedChange = { checked -> onToggleDone(item, checked) }
                                    )
                                    Column(Modifier.padding(top = 6.dp)) {
                                        Text(item.workoutName)
                                        Text(
                                            "${item.bodyPart} • ${item.effectiveWeightKg}kg • ${item.effectiveSets}x${item.effectiveReps} • Rest ${item.effectiveRestSeconds}s",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Text(if (item.isDone) "✓" else "", modifier = Modifier.padding(top = 12.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    editTarget?.let { item ->
        EditDayWorkoutDialog(
            item = item,
            onDismiss = { editTarget = null },
            onSave = { w, s, r, rest ->
                onSaveOverrides(item, w, s, r, rest)
                editTarget = null
            },
            onReset = {
                onReset(item)
                editTarget = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlanToDayDialog(
    plans: List<PlanEntity>,
    onDismiss: () -> Unit,
    onAdd: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf(plans.firstOrNull()?.id ?: 0L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add plan to day") },
        confirmButton = {
            TextButton(enabled = selectedId != 0L, onClick = { onAdd(selectedId) }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                val selectedName = plans.firstOrNull { it.id == selectedId }?.name ?: "Select a plan"
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Plan") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    plans.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) },
                            onClick = { selectedId = p.id; expanded = false }
                        )
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
    fun Float?.toText(default: Float) = (this ?: default).toString()
    fun Int?.toText(default: Int) = (this ?: default).toString()

    var weight by remember { mutableStateOf((item.overrideWeightKg ?: item.templateWeightKg).toString()) }
    var sets by remember { mutableStateOf((item.overrideSets ?: item.templateSets).toString()) }
    var reps by remember { mutableStateOf((item.overrideReps ?: item.templateReps).toString()) }
    var rest by remember { mutableStateOf((item.overrideRestSeconds ?: item.templateRestSeconds).toString()) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit: ${item.workoutName}") },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    weight.toFloatOrNull(),
                    sets.toIntOrNull(),
                    reps.toIntOrNull(),
                    rest.toIntOrNull()
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Template: ${item.templateWeightKg}kg • ${item.templateSets}x${item.templateReps} • Rest ${item.templateRestSeconds}s",
                    style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Override weight (kg) - optional") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = sets,
                    onValueChange = { sets = it },
                    label = { Text("Override sets - optional") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Override reps - optional") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = rest,
                    onValueChange = { rest = it },
                    label = { Text("Override rest (sec) - optional") },
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