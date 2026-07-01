@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.erdevelopments.powerpath.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.data.local.WorkoutEntity
import com.erdevelopments.powerpath.ui.components.ConfirmationDialog
import com.erdevelopments.powerpath.ui.components.FullscreenImageDialog
import com.erdevelopments.powerpath.ui.components.WorkoutImage

@Composable
fun WorkoutsScreen(vm: WorkoutsViewModel = hiltViewModel()) {
    val workouts by vm.workouts.collectAsStateWithLifecycle()

    var showAdd by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<WorkoutEntity?>(null) }
    var editTarget by remember { mutableStateOf<WorkoutEntity?>(null) }
    var fullscreenImagePath by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Workouts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Your exercise library",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))

            if (workouts.isEmpty()) {
                Spacer(Modifier.height(32.dp))
                Text(
                    "No workouts defined.\nTap + to add one.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = workouts,
                        key = { "workout_${it.id}" }
                    ) { workout ->
                        WorkoutListItem(
                            workout = workout,
                            onEdit = { editTarget = workout },
                            onDelete = { deleteTarget = workout },
                            onImageClick = { path -> fullscreenImagePath = path }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add workout")
        }
    }

    if (showAdd) {
        AddOrEditWorkoutDialog(
            title = "New workout",
            initialWorkout = null,
            onDismiss = { showAdd = false },
            onSave = { name, parts, desc, pickedImageUri, _ ->
                vm.addWorkout(name, parts, desc, pickedImageUri)
                showAdd = false
            }
        )
    }

    editTarget?.let { workout ->
        AddOrEditWorkoutDialog(
            title = "Edit workout",
            initialWorkout = workout,
            onDismiss = { editTarget = null },
            onSave = { name, parts, desc, pickedImageUri, removeImage ->
                vm.updateWorkout(
                    oldEntity = workout,
                    name = name,
                    bodyParts = parts,
                    desc = desc,
                    pickedImageUri = pickedImageUri,
                    removeImage = removeImage
                )
                editTarget = null
            }
        )
    }

    fullscreenImagePath?.let { path ->
        FullscreenImageDialog(
            imagePath = path,
            onDismiss = { fullscreenImagePath = null }
        )
    }

    deleteTarget?.let { workout ->
        ConfirmationDialog(
            title = "Delete workout?",
            message = "This will permanently delete ${workout.name}.",
            onConfirm = {
                vm.deleteWorkout(workout)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun WorkoutListItem(
    workout: WorkoutEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onImageClick: (String) -> Unit
) {
    var descriptionExpanded by remember(workout.id) { mutableStateOf(false) }
    val context = LocalContext.current
    val bodyParts = workout.bodyPart.split(",").map { it.trim() }.filter { it.isNotBlank() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            WorkoutImage(
                imagePath = workout.imageUri,
                modifier = Modifier.size(56.dp),
                onClick = onImageClick
            )

            Spacer(Modifier.size(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        openYoutubeSearch(context, workout.name)
                    }
                )

                Spacer(Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    bodyParts.forEach { part ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    part,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = null
                        )
                    }
                }

                workout.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = if (descriptionExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable {
                            descriptionExpanded = !descriptionExpanded
                        }
                    )
                    Text(
                        text = if (descriptionExpanded) "Show less" else "Show more",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable { descriptionExpanded = !descriptionExpanded }
                    )
                }
            }

            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddOrEditWorkoutDialog(
    title: String,
    initialWorkout: WorkoutEntity?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        bodyParts: List<String>,
        description: String,
        pickedImageUri: String?,
        removeImage: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialWorkout?.name ?: "") }
    var desc by remember { mutableStateOf(initialWorkout?.description ?: "") }

    var selectedParts by remember {
        mutableStateOf(
            initialWorkout?.bodyPart
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: emptySet()
        )
    }

    var imageUri by remember { mutableStateOf<String?>(null) }
    var removeImage by remember { mutableStateOf(false) }
    var showTypeDialog by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            imageUri = uri?.toString()
            if (uri != null) removeImage = false
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && selectedParts.isNotEmpty(),
                onClick = {
                    onSave(
                        name,
                        selectedParts.toList(),
                        desc,
                        imageUri,
                        removeImage
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )

                OutlinedButton(onClick = { showTypeDialog = true }) {
                    Text(
                        if (selectedParts.isEmpty()) {
                            "Select workout types"
                        } else {
                            selectedParts.joinToString(", ")
                        }
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedParts.forEach { part ->
                        FilterChip(
                            selected = true,
                            onClick = {
                                selectedParts = selectedParts - part
                            },
                            label = { Text(part) }
                        )
                    }
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (optional)") }
                )

                if (imageUri != null) {
                    Text("New image selected")
                } else if (initialWorkout?.imageUri != null && !removeImage) {
                    Text("Current image kept")
                } else {
                    Text("No image")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Text(if (initialWorkout == null) "Pick image" else "Replace image")
                    }

                    if (initialWorkout?.imageUri != null || imageUri != null) {
                        OutlinedButton(onClick = {
                            imageUri = null
                            removeImage = true
                        }) {
                            Text("Remove image")
                        }
                    }
                }
            }
        }
    )

    if (showTypeDialog) {
        WorkoutTypesDialog(
            selectedParts = selectedParts,
            onDismiss = { showTypeDialog = false },
            onApply = {
                selectedParts = it
                showTypeDialog = false
            }
        )
    }
}

@Composable
private fun WorkoutTypesDialog(
    selectedParts: Set<String>,
    onDismiss: () -> Unit,
    onApply: (Set<String>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedParts) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select workout types") },
        confirmButton = {
            TextButton(onClick = { onApply(tempSelected) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            LazyColumn {
                items(
                    items = BODY_PARTS,
                    key = { it }
                ) { part ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempSelected =
                                    if (part in tempSelected) tempSelected - part
                                    else tempSelected + part
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = part in tempSelected,
                            onCheckedChange = { checked ->
                                tempSelected =
                                    if (checked) tempSelected + part
                                    else tempSelected - part
                            }
                        )
                        Text(part)
                    }
                }
            }
        }
    )
}
