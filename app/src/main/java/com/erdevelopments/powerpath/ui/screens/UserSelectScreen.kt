package com.erdevelopments.powerpath.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun UserSelectScreen(
    onContinue: () -> Unit,
    vm: UserSelectViewModel = hiltViewModel()
) {
    val users by vm.users.collectAsStateWithLifecycle()

    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add user")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Select a user", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(12.dp))

            if (users.isEmpty()) {
                Text("No users yet. Tap + to add one.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(users) { u ->
                        Card(
                            Modifier.fillMaxWidth().clickable {
                                vm.selectUser(u.id)
                                onContinue()
                            }
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(u.name, style = MaterialTheme.typography.titleMedium)
                                Text("Age: ${u.age}  •  ${u.weightKg}kg  •  ${u.heightCm}cm")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddUserDialog(
            onDismiss = { showAdd = false },
            onAdd = { name, age, weight, height ->
                vm.addUser(name, age, weight, height)
                showAdd = false
                onContinue()
            }
        )
    }
}

@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Float, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && age.toIntOrNull() != null && weight.toFloatOrNull() != null && height.toIntOrNull() != null,
                onClick = {
                    onAdd(
                        name.trim(),
                        age.toInt(),
                        weight.toFloat(),
                        height.toInt()
                    )
                }
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("New user") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(
                    value = age, onValueChange = { age = it },
                    label = { Text("Age") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it },
                    label = { Text("Weight (kg)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = height, onValueChange = { height = it },
                    label = { Text("Height (cm)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    )
}