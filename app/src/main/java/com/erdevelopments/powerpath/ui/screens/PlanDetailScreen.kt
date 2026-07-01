package com.erdevelopments.powerpath.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.data.local.model.PlanWorkoutItem
import com.erdevelopments.powerpath.ui.components.ConfirmationDialog
import com.erdevelopments.powerpath.ui.components.FloatValueSlider
import com.erdevelopments.powerpath.ui.components.FullscreenImageDialog
import com.erdevelopments.powerpath.ui.components.IntValueSlider
import com.erdevelopments.powerpath.ui.components.MAX_REPS
import com.erdevelopments.powerpath.ui.components.MAX_SETS
import com.erdevelopments.powerpath.ui.components.MAX_WEIGHT_KG
import com.erdevelopments.powerpath.ui.components.PowerPathFab
import com.erdevelopments.powerpath.ui.components.WorkoutImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: Long,
    vm: PlanDetailViewModel = hiltViewModel()
) {
    val planName by vm.planName.collectAsStateWithLifecycle()
    val items by vm.planItems.collectAsStateWithLifecycle()
    val allWorkouts by vm.allWorkouts.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val displayedItems = remember(planId) { mutableStateListOf<PlanWorkoutItem>() }
    val itemHeights = remember(planId) { mutableStateMapOf<Long, Int>() }

    var deleteTarget by remember { mutableStateOf<PlanWorkoutItem?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<PlanWorkoutItem?>(null) }
    var fullscreenImagePath by remember { mutableStateOf<String?>(null) }
    var draggedWorkoutId by remember(planId) { mutableStateOf<Long?>(null) }
    var draggedOffsetY by remember(planId) { mutableStateOf(0f) }
    var pendingOrderedWorkoutIds by remember(planId) { mutableStateOf<List<Long>?>(null) }

    LaunchedEffect(items, draggedWorkoutId, pendingOrderedWorkoutIds) {
        val incomingWorkoutIds = items.map { it.workoutId }
        val pendingWorkoutIds = pendingOrderedWorkoutIds

        if (pendingWorkoutIds != null) {
            if (incomingWorkoutIds != pendingWorkoutIds) return@LaunchedEffect
            pendingOrderedWorkoutIds = null
        }

        if (draggedWorkoutId == null) {
            displayedItems.clear()
            displayedItems.addAll(items)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(planName) }) },
        floatingActionButton = {
            PowerPathFab(
                onClick = { if (allWorkouts.isNotEmpty()) showAdd = true },
                enabled = allWorkouts.isNotEmpty(),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add workout to plan")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Workouts in this plan", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (items.isEmpty()) {
                Text("No workouts yet. Tap + to add.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(displayedItems, key = { _, item -> item.workoutId }) { _, item ->
                        PlanWorkoutListItem(
                            item = item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged { itemHeights[item.workoutId] = it.height }
                                .graphicsLayer {
                                    translationY = if (draggedWorkoutId == item.workoutId) {
                                        draggedOffsetY
                                    } else {
                                        0f
                                    }
                                }
                                .zIndex(if (draggedWorkoutId == item.workoutId) 1f else 0f),
                            isDragging = draggedWorkoutId == item.workoutId,
                            onEdit = { editTarget = item },
                            onDelete = { deleteTarget = item },
                            onImageClick = { path -> fullscreenImagePath = path },
                            onNameClick = { openYoutubeSearch(context, item.workoutName) },
                            dragHandleModifier = Modifier.pointerInput(
                                item.workoutId,
                                displayedItems.size,
                                items.size
                            ) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedWorkoutId = item.workoutId
                                        draggedOffsetY = 0f
                                    },
                                    onDragEnd = {
                                        val updatedOrder = displayedItems.map { it.workoutId }
                                        val previousOrder = items.map { it.workoutId }
                                        if (updatedOrder != previousOrder) {
                                            pendingOrderedWorkoutIds = updatedOrder
                                            vm.reorderWorkouts(updatedOrder)
                                        }
                                        draggedWorkoutId = null
                                        draggedOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        pendingOrderedWorkoutIds = null
                                        draggedWorkoutId = null
                                        draggedOffsetY = 0f
                                        displayedItems.clear()
                                        displayedItems.addAll(items)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        draggedOffsetY += dragAmount.y

                                        while (true) {
                                            val activeWorkoutId = draggedWorkoutId ?: break
                                            val currentIndex = displayedItems.indexOfFirst {
                                                it.workoutId == activeWorkoutId
                                            }
                                            if (currentIndex == -1) break

                                            if (draggedOffsetY > 0 && currentIndex < displayedItems.lastIndex) {
                                                val nextWorkoutId =
                                                    displayedItems[currentIndex + 1].workoutId
                                                val nextHeight = (
                                                    itemHeights[nextWorkoutId]
                                                        ?: itemHeights[activeWorkoutId]
                                                        ?: 1
                                                    ).toFloat()
                                                if (draggedOffsetY <= nextHeight / 2f) break
                                                displayedItems.move(currentIndex, currentIndex + 1)
                                                draggedOffsetY -= nextHeight
                                                continue
                                            }

                                            if (draggedOffsetY < 0 && currentIndex > 0) {
                                                val previousWorkoutId =
                                                    displayedItems[currentIndex - 1].workoutId
                                                val previousHeight = (
                                                    itemHeights[previousWorkoutId]
                                                        ?: itemHeights[activeWorkoutId]
                                                        ?: 1
                                                    ).toFloat()
                                                if (-draggedOffsetY <= previousHeight / 2f) break
                                                displayedItems.move(currentIndex, currentIndex - 1)
                                                draggedOffsetY += previousHeight
                                                continue
                                            }

                                            break
                                        }
                                    }
                                )
                            }
                        )
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
                vm.addWorkoutToPlan(workoutId, weight, sets, reps, rest)
                showAdd = false
            }
        )
    }

    editTarget?.let { target ->
        EditPlanWorkoutDialog(
            item = target,
            onDismiss = { editTarget = null },
            onSave = { weight, sets, reps, rest ->
                vm.updatePlanWorkout(target.workoutId, weight, sets, reps, rest)
                editTarget = null
            }
        )
    }

    deleteTarget?.let { target ->
        ConfirmationDialog(
            title = "Remove workout from plan?",
            message = "This will remove ${target.workoutName} from $planName.",
            confirmLabel = "Remove",
            onConfirm = {
                vm.removeFromPlan(target.workoutId)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }

    fullscreenImagePath?.let { path ->
        FullscreenImageDialog(
            imagePath = path,
            onDismiss = { fullscreenImagePath = null }
        )
    }
}

@Composable
private fun PlanWorkoutListItem(
    item: PlanWorkoutItem,
    modifier: Modifier = Modifier,
    isDragging: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onImageClick: (String) -> Unit,
    onNameClick: () -> Unit,
    dragHandleModifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkoutImage(
                imagePath = item.imageUri,
                modifier = Modifier.size(56.dp),
                onClick = onImageClick
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.workoutName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable(onClick = onNameClick)
                )
                Text(item.bodyPart)
                Text("${item.weightKg}kg | ${item.sets}x${item.reps} | Rest ${item.restSeconds}s")
            }

            Row {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Reorder",
                    tint = if (isDragging) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = dragHandleModifier
                        .padding(end = 4.dp)
                        .size(24.dp)
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }
        }
    }
}

private fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
    if (fromIndex == toIndex) return
    add(toIndex, removeAt(fromIndex))
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
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    val selectedName = workouts.firstOrNull { it.first == selectedId }?.second ?: ""
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
                                    selectedId = id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

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
