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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                WorkoutImage(
                                    imagePath = w.imageUri,
                                    modifier = Modifier.size(56.dp),
                                    onClick = { path ->
                                        fullscreenImagePath = path
                                    }
                                )

                                Spacer(Modifier.size(12.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(w.name, style = MaterialTheme.typography.titleMedium)
                                    Text(w.bodyPart)
                                    w.description?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                Row {
                                    IconButton(onClick = { editTarget = w }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { vm.deleteWorkout(w) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
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
        AddOrEditWorkoutDialog(
            title = "New workout",
            initialWorkout = null,
            onDismiss = { showAdd = false },
            onSave = { name, part, desc, pickedImageUri, removeImage ->
                vm.addWorkout(name, part, desc, pickedImageUri)
                showAdd = false
            }
        )
    }

    editTarget?.let { workout ->
        AddOrEditWorkoutDialog(
            title = "Edit workout",
            initialWorkout = workout,
            onDismiss = { editTarget = null },
            onSave = { name, part, desc, pickedImageUri, removeImage ->
                vm.updateWorkout(
                    oldEntity = workout,
                    name = name,
                    bodyPart = part,
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
        bodyPart: String,
        description: String,
        pickedImageUri: String?,
        removeImage: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialWorkout?.name ?: "") }
    var part by remember { mutableStateOf(initialWorkout?.bodyPart ?: BODY_PARTS.first()) }
    var desc by remember { mutableStateOf(initialWorkout?.description ?: "") }

    var imageUri by remember { mutableStateOf<String?>(null) }
    var removeImage by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

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
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(name, part, desc, imageUri, removeImage)
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

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = part,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Body part") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        BODY_PARTS.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    part = p
                                    expanded = false
                                }
                            )
                        }
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
}