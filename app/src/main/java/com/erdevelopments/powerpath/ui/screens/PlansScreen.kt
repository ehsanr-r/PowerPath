package com.erdevelopments.powerpath.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    val plans by vm.plans.collectAsStateWithLifecycle()
    val hasWorkouts by vm.hasWorkouts.collectAsStateWithLifecycle()

    var renameTarget by remember { mutableStateOf<PlanEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Plans", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))

            if (userId == null) {
                Text("Select a user first.")
                return@Column
            }

            if (!hasWorkouts) {
                Text("Create at least 1 Workout first (Workouts tab) to start making plans.")
                Spacer(Modifier.height(12.dp))
            }

            if (plans.isEmpty()) {
                Text("No plans yet. Tap + to add one.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(plans, key = { it.id }) { plan ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onOpenPlan(plan.id) }
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    plan.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
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

        PowerPathFab(
            enabled = (userId != null) && hasWorkouts,
            onClick = { vm.addPlan() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add plan")
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
