package com.erdevelopments.powerpath.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.data.local.DayEntity
import com.erdevelopments.powerpath.ui.components.ConfirmationDialog
import com.erdevelopments.powerpath.ui.components.PowerPathFab

@Composable
fun DaysScreen(onOpenDay: (Long) -> Unit, vm: DaysViewModel = hiltViewModel()) {
    val userId by vm.selectedUserId.collectAsStateWithLifecycle()
    val selectedDayId by vm.selectedDayId.collectAsStateWithLifecycle()
    val days by vm.days.collectAsStateWithLifecycle()

    var deleteTarget by remember { mutableStateOf<DayEntity?>(null) }
    var renameTarget by remember { mutableStateOf<DayEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Days", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))

            if (userId == null) {
                Text("Select a user first (go back to User screen).")
                return@Column
            }

            if (days.isEmpty()) {
                Text("No days yet. Tap + to add your first day.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(days) { day ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                vm.selectDay(day.id)
                                onOpenDay(day.id)
                            }
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(day.name, style = MaterialTheme.typography.titleMedium)
                                    if (selectedDayId == day.id) {
                                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                            Spacer(Modifier.width(6.dp))
                                            Text("Selected")
                                        }
                                    }
                                }

                                Row {
                                    IconButton(onClick = { renameTarget = day }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename")
                                    }
                                    IconButton(onClick = { deleteTarget = day }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        PowerPathFab(
            enabled = userId != null,
            onClick = { if (userId != null) vm.addDay() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add day")
        }
    }

    renameTarget?.let { day ->
        RenameDialog(
            title = "Rename day",
            initial = day.name,
            onDismiss = { renameTarget = null },
            onSave = { vm.renameDay(day.id, it); renameTarget = null }
        )
    }

    deleteTarget?.let { day ->
        ConfirmationDialog(
            title = "Delete day?",
            message = "This will permanently delete ${day.name} and its assigned plans.",
            onConfirm = {
                vm.deleteDay(day)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}
