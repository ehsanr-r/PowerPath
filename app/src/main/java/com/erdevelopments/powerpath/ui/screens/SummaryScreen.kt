package com.erdevelopments.powerpath.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdevelopments.powerpath.ui.components.SimpleBarChart

@Composable
fun SummaryScreen(vm: SummaryViewModel = hiltViewModel()) {
    val userId by vm.selectedUserId.collectAsStateWithLifecycle()
    val dayVolumes by vm.dayVolumes.collectAsStateWithLifecycle()

    Column(Modifier.padding(16.dp)) {
        Text("Summary", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        if (userId == null) {
            Text("Select a user first.")
            return@Column
        }

        if (dayVolumes.isEmpty()) {
            Text("No data yet. Create days, plans, and workouts.")
            return@Column
        }

        Text("Volume by Day", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        SimpleBarChart(values = dayVolumes.map { it.volume })

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dayVolumes, key = { it.dayId }) { dv ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(dv.dayName)
                        Text(String.format("%.0f", dv.volume))
                    }
                }
            }
        }
    }
}