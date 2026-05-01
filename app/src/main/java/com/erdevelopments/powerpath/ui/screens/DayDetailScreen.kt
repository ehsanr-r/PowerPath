@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.erdevelopments.powerpath.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.data.local.PlanEntity
import com.erdevelopments.powerpath.data.local.model.DayPlanItem
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutItem
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutSetItem
import com.erdevelopments.powerpath.ui.components.FloatValueSlider
import com.erdevelopments.powerpath.ui.components.IntValueSlider
import com.erdevelopments.powerpath.ui.components.MAX_REPS
import com.erdevelopments.powerpath.ui.components.MAX_SETS
import com.erdevelopments.powerpath.ui.components.MAX_WEIGHT_KG

@Composable
fun DayDetailScreen(
    dayId: Long,
    vm: DayDetailViewModel = hiltViewModel()
) {
    val dayName by vm.observeDayName(dayId).collectAsStateWithLifecycle()
    val assignedPlans by vm.observeAssignedPlans(dayId).collectAsStateWithLifecycle()
    val allPlans by vm.allPlansForUser.collectAsStateWithLifecycle()

    var showAddPlan by remember { mutableStateOf(false) }
    var expandedDayPlanId by remember { mutableStateOf(-1L) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(dayName) }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (allPlans.isNotEmpty()) showAddPlan = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add plan to day")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Day activity", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (assignedPlans.isEmpty()) {
                Text("No plans assigned to this day. Tap + to add one.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
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
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))

                if (workouts.isEmpty()) {
                    Text("This plan has no workouts (add workouts in Plans tab).")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    var setsExpanded by remember(item.dayPlanId, item.workoutId) { mutableStateOf(true) }
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (item.isDone) {
        colorScheme.secondaryContainer
    } else {
        colorScheme.surface
    }
    val borderColor = if (item.isDone) {
        colorScheme.secondary
    } else {
        colorScheme.primary
    }
    val headerColor = if (item.isDone) {
        colorScheme.secondaryContainer.copy(alpha = 0.95f)
    } else {
        colorScheme.primaryContainer.copy(alpha = 0.9f)
    }
    val headerTextColor = if (item.isDone) {
        colorScheme.onSecondaryContainer
    } else {
        colorScheme.onPrimaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = headerColor,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(borderColor)
                    )
                    Checkbox(
                        checked = item.isDone,
                        onCheckedChange = { checked ->
                            vm.toggleAllSets(item, checked)
                        }
                    )
                    Column(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.workoutName,
                            style = MaterialTheme.typography.titleMedium,
                            color = headerTextColor,
                            modifier = Modifier.clickable {
                                openYoutubeSearch(context, item.workoutName)
                            }
                        )
                        Text(
                            text = item.bodyPart.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = headerTextColor.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "${formatWeight(item.effectiveWeightKg)}kg • ${item.effectiveSets} sets • ${item.effectiveReps} reps • Rest ${item.effectiveRestSeconds}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = headerTextColor.copy(alpha = 0.85f)
                        )
                    }
                }

                TextButton(onClick = { editTarget = item }) {
                    Text(
                        text = "Edit",
                        color = headerTextColor
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colorScheme.surfaceVariant.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { setsExpanded = !setsExpanded },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 8.dp,
                            vertical = 2.dp
                        )
                    ) {
                        Text(if (setsExpanded) "Hide sets" else "Show sets")
                    }
                }

                if (setsExpanded) {
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
    var weight by remember(setItem.weightKg) {
        mutableFloatStateOf(setItem.weightKg.coerceIn(0f, MAX_WEIGHT_KG))
    }
    var reps by remember(setItem.reps) { mutableIntStateOf(setItem.reps.coerceIn(1, MAX_REPS)) }
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (setItem.isDone) {
        colorScheme.primaryContainer.copy(alpha = 0.7f)
    } else {
        colorScheme.surface.copy(alpha = 0.96f)
    }
    val borderColor = if (setItem.isDone) {
        colorScheme.primary
    } else {
        colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = setItem.isDone,
                        onCheckedChange = onCheckedChange
                    )
                    Text(
                        text = "Set ${setItem.setNumber}",
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Text(
                    text = "${formatWeight(weight)}kg • $reps reps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FloatValueSlider(
                label = "Weight",
                value = weight,
                onValueChange = {
                    weight = it
                    onValueChange(weight, reps)
                },
                valueRange = 0f..MAX_WEIGHT_KG,
                stepSize = 0.5f,
                valueSuffix = "kg"
            )

            IntValueSlider(
                label = "Reps",
                value = reps,
                onValueChange = {
                    reps = it
                    onValueChange(weight, reps)
                },
                valueRange = 1..MAX_REPS
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
    var selectedId by remember(plans) { mutableStateOf(plans.firstOrNull()?.id ?: 0L) }

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
    val initialWeight = (item.overrideWeightKg ?: item.templateWeightKg).coerceIn(0f, MAX_WEIGHT_KG)
    val initialSets = (item.overrideSets ?: item.templateSets).coerceIn(1, MAX_SETS)
    val initialReps = (item.overrideReps ?: item.templateReps).coerceIn(1, MAX_REPS)
    var weight by remember { mutableFloatStateOf(initialWeight) }
    var sets by remember { mutableIntStateOf(initialSets) }
    var reps by remember { mutableIntStateOf(initialReps) }
    var rest by remember {
        mutableStateOf((item.overrideRestSeconds ?: item.templateRestSeconds).toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit: ${item.workoutName}") },
        confirmButton = {
            TextButton(
                enabled = rest.toIntOrNull() != null,
                onClick = {
                    onSave(
                        weight,
                        sets,
                        reps,
                        rest.toInt()
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Plan defaults: ${item.templateWeightKg}kg • ${item.templateSets}x${item.templateReps} • Rest ${item.templateRestSeconds}s",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = rest,
                    onValueChange = { rest = it },
                    label = { Text("Rest (seconds)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

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
                OutlinedButton(onClick = onReset) {
                    Text("Reset to plan defaults")
                }
            }
        }
    )
}

private fun formatWeight(value: Float): String {
    return if (value == value.toInt().toFloat()) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}
