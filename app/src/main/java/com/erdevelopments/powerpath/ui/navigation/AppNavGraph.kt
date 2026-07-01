package com.erdevelopments.powerpath.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.erdevelopments.powerpath.ui.screens.*

@Composable
fun PowerPathAppRoot(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.USER_SELECT) {

        composable(Routes.USER_SELECT) {
            UserSelectScreen(onContinue = { navController.navigate(Routes.MAIN) })
        }

        composable(Routes.MAIN) { MainScaffold(navController = navController) }

        composable(Routes.SETTINGS) { SettingsScreen() }
        composable(Routes.ABOUT) { AboutScreen() }
        composable(Routes.PROFILE) { ProfileScreen() }

        composable(Routes.PLAN_DETAIL) { backStack ->
            val planId = backStack.arguments?.getString("planId")!!.toLong()
            PlanDetailScreen(planId = planId)
        }

        composable(Routes.DAY_DETAIL) { backStack ->
            val dayId = backStack.arguments?.getString("dayId")!!.toLong()
            DayDetailScreen(dayId = dayId)
        }
    }
}
