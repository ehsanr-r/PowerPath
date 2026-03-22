package com.erdevelopments.powerpath.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isBusy by vm.isBusy.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showImportWarning by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            vm.exportBackup(uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            vm.importBackup(uri)
        }
    }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is SettingsEvent.Message -> {
                    snackbarHostState.showSnackbar(event.text)
                }
                SettingsEvent.ImportFinished -> {
                    showRestartDialog = true
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Backup & Restore", style = MaterialTheme.typography.titleLarge)

            Text(
                "Export saves your current app database as a backup ZIP file.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = { exportLauncher.launch("powerpath-backup.zip") },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export database")
            }

            Text(
                "Import replaces your current app data with a backup.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedButton(
                onClick = { showImportWarning = true },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import database")
            }

            Text(
                "Important: importing will overwrite your current database and restart the app.",
                style = MaterialTheme.typography.bodySmall
            )

            if (isBusy) {
                CircularProgressIndicator()
            }
        }
    }

    if (showImportWarning) {
        AlertDialog(
            onDismissRequest = { showImportWarning = false },
            title = { Text("Import backup?") },
            text = {
                Text("This will replace your current data with the selected backup file.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportWarning = false
                        importLauncher.launch(arrayOf("*/*"))
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportWarning = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Import complete") },
            text = {
                Text("The backup was imported. The app needs to restart now.")
            },
            confirmButton = {
                TextButton(onClick = { restartApp(context) }) {
                    Text("Restart")
                }
            }
        )
    }
}

private fun restartApp(context: Context) {
    val intent = context.packageManager
        .getLaunchIntentForPackage(context.packageName)
        ?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        ?: return

    context.startActivity(intent)
    exitProcess(0)
}