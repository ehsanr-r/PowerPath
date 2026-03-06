package com.erdevelopments.powerpath.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("About") }) }) { padding ->
        Text("PowerPath • Offline Gym Tracker", modifier = Modifier.padding(padding).padding(16.dp))
    }
}