@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.erdevelopments.powerpath.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.data.local.PlanEntity
import com.erdevelopments.powerpath.data.local.model.DayPlanItem
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutItem
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutSetItem
import com.erdevelopments.powerpath.data.local.model.WorkoutHistorySetItem
import com.erdevelopments.powerpath.ui.components.ConfirmationDialog
import com.erdevelopments.powerpath.ui.components.FloatValueSlider
import com.erdevelopments.powerpath.ui.components.FullscreenImageDialog
import com.erdevelopments.powerpath.ui.components.IntValueSlider
import com.erdevelopments.powerpath.ui.components.MAX_REPS
import com.erdevelopments.powerpath.ui.components.MAX_SETS
import com.erdevelopments.powerpath.ui.components.MAX_WEIGHT_KG
import com.erdevelopments.powerpath.ui.components.WorkoutImage

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
        topBar = {
            TopAppBar(
                title = { Text(dayName, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
            )
        },
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
            Text(
                "Day activity",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Manage your workout plans for this day",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    var showDeleteConfirmation by remember(dayPlan.dayPlanId) { mutableStateOf(false) }

    DayPlanCard(
        dayPlan = dayPlan,
        totalWorkouts = progress?.totalWorkouts ?: 0,
        doneWorkouts = progress?.doneWorkouts ?: 0,
        expanded = expanded,
        onToggleExpand = onToggleExpand,
        onRemove = { showDeleteConfirmation = true },
        workouts = if (expanded) workouts else emptyList()
    )

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            title = "Remove plan from day?",
            message = "This will remove ${dayPlan.planName} from this day.",
            confirmLabel = "Remove",
            onConfirm = {
                onRemove()
                showDeleteConfirmation = false
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
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
    val progress = if (totalWorkouts > 0) doneWorkouts.toFloat() / totalWorkouts else 0f
    val animatedProgress by animateFloatAsState(progress, animationSpec = tween(500), label = "progress")
    val allDone = totalWorkouts > 0 && doneWorkouts == totalWorkouts

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (allDone)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier
                .animateContentSize(animationSpec = tween(300))
                .padding(14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        dayPlan.planName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$doneWorkouts / $totalWorkouts workouts complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove plan from day",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (totalWorkouts > 0) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (allDone) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))

            TextButton(onClick = onToggleExpand) {
                Text(if (expanded) "Hide workouts" else "Show workouts")
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))

                if (workouts.isEmpty()) {
                    Text("This plan has no workouts (add workouts in Plans tab).")
                } else {
                    val pagerState = rememberPagerState(pageCount = { workouts.size })
                    val pageHeights = remember(workouts) { mutableStateMapOf<Long, Int>() }
                    val tallestPageHeight = with(LocalDensity.current) {
                        (pageHeights.values.maxOrNull() ?: 0).toDp()
                    }

                    LaunchedEffect(workouts) {
                        workouts.forEach(vm::ensureSets)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Workout ${pagerState.currentPage + 1} of ${workouts.size}",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        if (workouts.size > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(workouts.size) { index ->
                                    val isSelected = index == pagerState.currentPage
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .size(if (isSelected) 10.dp else 8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.outlineVariant
                                                }
                                            )
                                    )
                                }
                            }
                        }

                        HorizontalPager(
                            modifier = Modifier.fillMaxWidth(),
                            state = pagerState,
                            pageSpacing = 12.dp,
                            key = { page -> workouts[page].workoutId }
                        ) { page ->
                            val workout = workouts[page]
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = tallestPageHeight),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                WorkoutWithSetsCard(
                                    item = workout,
                                    modifier = Modifier.onSizeChanged { size ->
                                        pageHeights[workout.workoutId] = size.height
                                    }
                                )
                            }
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
    modifier: Modifier = Modifier,
    vm: DayDetailViewModel = hiltViewModel()
) {
    val sets by vm.observeWorkoutSets(item.dayPlanId, item.workoutId).collectAsStateWithLifecycle()
    val liftedTotalKg = sets
        .filter { it.isDone }
        .fold(0f) { acc, set -> acc + (set.weightKg * set.reps) }
    var editTarget by remember { mutableStateOf<DayPlanWorkoutItem?>(null) }
    var fullscreenImagePath by remember(item.dayPlanId, item.workoutId) { mutableStateOf<String?>(null) }
    var historyTarget by remember { mutableStateOf<DayPlanWorkoutItem?>(null) }
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
        modifier = modifier.fillMaxWidth(),
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        WorkoutImage(
                            imagePath = item.imageUri,
                            modifier = Modifier.size(56.dp),
                            onClick = { path -> fullscreenImagePath = path }
                        )
                        Checkbox(
                            checked = item.isDone,
                            onCheckedChange = { checked ->
                                vm.toggleAllSets(item, checked)
                            }
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    )
                    {
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

                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = { editTarget = item }) {
                        Text(
                            text = "Edit",
                            color = headerTextColor
                        )
                    }
                    TextButton(onClick = { historyTarget = item }) {
                        Text(
                            text = "History",
                            color = headerTextColor
                        )
                    }
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total: ${formatWeight(liftedTotalKg)}kg",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

    fullscreenImagePath?.let { path ->
        FullscreenImageDialog(
            imagePath = path,
            onDismiss = { fullscreenImagePath = null }
        )
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

    historyTarget?.let {
        WorkoutHistoryDialog(
            item = it,
            onDismiss = { historyTarget = null }
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
                stepSize = 1f,
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
                    stepSize = 1f,
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

@Composable
private fun WorkoutHistoryDialog(
    item: DayPlanWorkoutItem,
    onDismiss: () -> Unit,
    vm: DayDetailViewModel = hiltViewModel()
) {
    val history by vm.observeWorkoutHistory(item.dayPlanId, item.workoutId)
        .collectAsStateWithLifecycle()
    val totalVolume = history.fold(0f) { acc, set -> acc + (set.weightKg * set.reps) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("History: ${item.workoutName}") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (history.isEmpty()) {
                    Text("No previous completed session found for this workout in this plan.")
                } else {
                    val latest = history.first()

                    Text(
                        text = "Last session: ${latest.dayName}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${history.size} completed sets • Volume ${formatWeight(totalVolume)}kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    history.forEach { set ->
                        HistorySetRow(set)
                    }
                }
            }
        }
    )
}

@Composable
private fun HistorySetRow(set: WorkoutHistorySetItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set ${set.setNumber}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${formatWeight(set.weightKg)}kg • ${set.reps} reps",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatWeight(value: Float): String {
    return if (value == value.toInt().toFloat()) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}
