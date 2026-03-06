package com.erdevelopments.powerpath.ui.navigation

object Routes {
    const val USER_SELECT = "user_select"
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val PROFILE = "profile"
    const val PLAN_DETAIL = "plan/{planId}"
    const val DAY_DETAIL = "day/{dayId}"

    fun planDetail(planId: Long) = "plan/$planId"
    fun dayDetail(dayId: Long) = "day/$dayId"
}