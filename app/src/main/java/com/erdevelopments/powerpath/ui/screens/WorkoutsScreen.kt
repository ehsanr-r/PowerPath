package com.erdevelopments.powerpath.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.erdevelopments.powerpath.data.local.WorkoutEntity

@Composable
fun WorkoutsScreen(vm: WorkoutsViewModel = hiltViewModel()) {
    val workouts by vm.workouts.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

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
                    items(workouts, key = { it.id }) { w ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(12.dp)) {
                                if (!w.imageUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = w.imageUri,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                }

                                Column(Modifier.weight(1f)) {
                                    Text(w.name, style = MaterialTheme.typography.titleMedium)
                                    Text(w.bodyPart)
                                    w.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
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

    if (showAdd) {
        AddWorkoutDialog(
            onDismiss = { showAdd = false },
            onAdd = { name, part, desc, uri ->
                vm.addWorkout(name, part, desc, uri)
                showAdd = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWorkoutDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, bodyPart: String, description: String?, imageUri: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var part by remember { mutableStateOf(BODY_PARTS.first()) }
    var desc by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? -> imageUri = uri?.toString() }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New workout") },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onAdd(name, part, desc, imageUri) }
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = part,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Body part") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        BODY_PARTS.forEach { p ->
                            DropdownMenuItem(text = { Text(p) }, onClick = { part = p; expanded = false })
                        }
                    }
                }

                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description (optional)") })

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) { Text("Pick image") }

                    if (imageUri != null) {
                        Text("Selected ✓", modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
        }
    )
}