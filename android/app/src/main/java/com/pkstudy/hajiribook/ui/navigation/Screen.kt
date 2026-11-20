package com.pkstudy.hajiribook.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SelectRole : Screen("role")
    object Home : Screen("home")
    object FaceScan : Screen("face_scan")
    object AddStudent : Screen("add_student")
    object Students : Screen("students")
    object Attendance : Screen("attendance")
    object ManualAttendance : Screen("manual_attendance")
    object History : Screen("history")
    object Reports : Screen("reports")
    object Sync : Screen("sync")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object FaceRegister : Screen("face_register")
    object Notifications : Screen("notifications")
}
