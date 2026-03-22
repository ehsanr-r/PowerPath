@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.erdevelopments.powerpath.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.erdevelopments.powerpath.data.local.WorkoutEntity
import java.io.File

@Composable
fun WorkoutsScreen(vm: WorkoutsViewModel = hiltViewModel()) {
    val workouts by vm.workouts.collectAsStateWithLifecycle()

    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<WorkoutEntity?>(null) }
    var fullscreenImagePath by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add workout")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Workouts", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (workouts.isEmpty()) {
                Text("No workouts defined. Tap + to add.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        items = workouts,
                        key = { "workout_${it.id}" }
                    ) { w ->
                        WorkoutListItem(
                            workout = w,
                            onEdit = { editTarget = w },
                            onDelete = { vm.deleteWorkout(w) },
                            onImageClick = { path -> fullscreenImagePath = path }
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddOrEditWorkoutDialog(
            title = "New workout",
            initialWorkout = null,
            onDismiss = { showAdd = false },
            onSave = { name, parts, desc, pickedImageUri, removeImage ->
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
}

@Composable
private fun WorkoutListItem(
    workout: WorkoutEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onImageClick: (String) -> Unit
) {
    var descriptionExpanded by remember(workout.id) { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkoutImage(
                imagePath = workout.imageUri,
                modifier = Modifier.size(56.dp),
                onClick = onImageClick
            )

            Spacer(Modifier.size(12.dp))

            Column(Modifier.weight(1f)) {
                Text(workout.name, style = MaterialTheme.typography.titleMedium)
                Text(workout.bodyPart)

                workout.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = if (descriptionExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
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

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
private fun WorkoutImage(
    imagePath: String?,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    val hasImage = !imagePath.isNullOrBlank()

    if (hasImage) {
        val model =
            if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) imagePath
            else File(imagePath)

        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = modifier
                .clip(CircleShape)
                .clickable { onClick(imagePath!!) },
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "—",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FullscreenImageDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    val model =
        if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) imagePath
        else File(imagePath)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    )
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
                    Text("New image selected ✓")
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