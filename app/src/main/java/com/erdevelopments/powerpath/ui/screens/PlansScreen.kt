package com.erdevelopments.powerpath.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.data.local.PlanEntity
import com.erdevelopments.powerpath.ui.components.PowerPathFab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    onOpenPlan: (Long) -> Unit,
    vm: PlansViewModel = hiltViewModel()
) {
    val userId by vm.selectedUserId.collectAsStateWithLifecycle()
    val selectedDayId by vm.selectedDayId.collectAsStateWithLifecycle()
    val days by vm.days.collectAsStateWithLifecycle()
    val plans by vm.plans.collectAsStateWithLifecycle()

    var renameTarget by remember { mutableStateOf<PlanEntity?>(null) }
    var dayMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            PowerPathFab(onClick = { if (selectedDayId != null) vm.addPlan()  }, enabled = selectedDayId != null) {
                Icon(Icons.Default.Add, contentDescription = "Add plan")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Plans", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))

            if (userId == null) {
                Text("Select a user first.")
                return@Column
            }

            if (days.isEmpty()) {
                Text("Create a Day first (Days tab).")
                return@Column
            }

            // Day picker
            ExposedDropdownMenuBox(
                expanded = dayMenuOpen,
                onExpandedChange = { dayMenuOpen = !dayMenuOpen }
            ) {
                val selectedName = days.firstOrNull { it.id == selectedDayId }?.name ?: "Select day"
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Day") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = dayMenuOpen, onDismissRequest = { dayMenuOpen = false }) {
                    days.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d.name) },
                            onClick = {
                                vm.selectDay(d.id)
                                dayMenuOpen = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (selectedDayId == null) {
                Text("Pick a day to see its plans.")
            } else if (plans.isEmpty()) {
                Text("No plans yet. Tap + to add one.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(plans, key = { it.id }) { plan ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onOpenPlan(plan.id) }
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(plan.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                Row {
                                    IconButton(onClick = { renameTarget = plan }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename")
                                    }
                                    IconButton(onClick = { vm.deletePlan(plan) }) {
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

    renameTarget?.let { plan ->
        RenameDialog(
            title = "Rename plan",
            initial = plan.name,
            onDismiss = { renameTarget = null },
            onSave = { vm.renamePlan(plan.id, it); renameTarget = null }
        )
    }
}