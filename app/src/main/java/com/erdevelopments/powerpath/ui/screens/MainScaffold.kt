package com.erdevelopments.powerpath.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.erdevelopments.powerpath.ui.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val tabs = listOf(
        "days" to Icons.Default.CalendarMonth,
        "plans" to Icons.Default.List,
        "workouts" to Icons.Default.FitnessCenter,
        "summary" to Icons.Default.QueryStats
    )

    var currentTab by rememberSaveable { mutableStateOf("days") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("PowerPath", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))

                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.PROFILE)
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.SETTINGS)
                    }
                )
                NavigationDrawerItem(
                    label = { Text("About") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.ABOUT)
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("PowerPath") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    tabs.forEach { (route, icon) ->
                        NavigationBarItem(
                            selected = currentTab == route,
                            onClick = { currentTab = route },
                            icon = { Icon(icon, contentDescription = route) },
                            label = { Text(route.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentTab) {
                    "days" -> DaysScreen(onOpenDay = { dayId -> navController.navigate(Routes.dayDetail(dayId)) })
                    "plans" -> PlansScreen(onOpenPlan = { planId -> navController.navigate(Routes.planDetail(planId)) })
                    "workouts" -> WorkoutsScreen()
                    "summary" -> SummaryScreen()
                }
            }
        }
    }
}
