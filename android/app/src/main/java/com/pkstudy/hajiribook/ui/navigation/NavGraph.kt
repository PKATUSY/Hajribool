package com.pkstudy.hajiribook.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pkstudy.hajiribook.HajiriApp
import com.pkstudy.hajiribook.ui.components.HajiriDrawerContent
import com.pkstudy.hajiribook.ui.screens.addstudent.AddStudentScreen
import com.pkstudy.hajiribook.ui.screens.attendance.AttendanceScreen
import com.pkstudy.hajiribook.ui.screens.faceregis.FaceRegistrationScreen
import com.pkstudy.hajiribook.ui.screens.facescan.FaceScanScreen
import com.pkstudy.hajiribook.ui.screens.history.AttendanceHistoryScreen
import com.pkstudy.hajiribook.ui.screens.home.HomeScreen
import com.pkstudy.hajiribook.ui.screens.login.LoginScreen
import com.pkstudy.hajiribook.ui.screens.manualattendance.ManualAttendanceScreen
import com.pkstudy.hajiribook.ui.screens.profile.ProfileScreen
import com.pkstudy.hajiribook.ui.screens.reports.ReportsScreen
import com.pkstudy.hajiribook.ui.screens.role.SelectRoleScreen
import com.pkstudy.hajiribook.ui.screens.settings.SettingsScreen
import com.pkstudy.hajiribook.ui.screens.splash.SplashScreen
import com.pkstudy.hajiribook.ui.screens.students.StudentListScreen
import com.pkstudy.hajiribook.ui.screens.sync.DataSyncScreen
import com.pkstudy.hajiribook.viewmodel.AttendanceViewModel
import com.pkstudy.hajiribook.viewmodel.AuthViewModel
import com.pkstudy.hajiribook.viewmodel.FaceScanViewModel
import com.pkstudy.hajiribook.viewmodel.HajiriViewModelFactory
import com.pkstudy.hajiribook.viewmodel.HomeViewModel
import com.pkstudy.hajiribook.viewmodel.StudentViewModel
import com.pkstudy.hajiribook.viewmodel.SyncViewModel
import kotlinx.coroutines.launch

@Composable
fun HajiriNavGraph(
    navController: NavHostController,
    app: HajiriApp
) {
    val factory = HajiriViewModelFactory(app)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route ?: Screen.Splash.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute == Screen.Home.route,
        drawerContent = {
            ModalDrawerSheet {
                HajiriDrawerContent(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                val authViewModel: AuthViewModel = viewModel(factory = factory)
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRole = {
                        navController.navigate(Screen.SelectRole.route)
                    }
                )
            }

            composable(Screen.SelectRole.route) {
                val authViewModel: AuthViewModel = viewModel(factory = factory)
                SelectRoleScreen(
                    onRoleSelected = { role ->
                        authViewModel.selectRole(role)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SelectRole.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigate = { route ->
                        navController.navigate(route)
                    },
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            composable(Screen.FaceScan.route) {
                val faceScanViewModel: FaceScanViewModel = viewModel(factory = factory)
                FaceScanScreen(
                    viewModel = faceScanViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AddStudent.route) {
                val studentViewModel: StudentViewModel = viewModel(factory = factory)
                AddStudentScreen(
                    viewModel = studentViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onStudentSaved = { navController.popBackStack() }
                )
            }

            composable(Screen.Students.route) {
                val studentViewModel: StudentViewModel = viewModel(factory = factory)
                StudentListScreen(
                    viewModel = studentViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddStudent = { navController.navigate(Screen.AddStudent.route) }
                )
            }

            composable(Screen.Attendance.route) {
                AttendanceScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStartFaceScan = { navController.navigate(Screen.FaceScan.route) },
                    onStartManualAttendance = { navController.navigate(Screen.ManualAttendance.route) },
                    onStartQrScan = { navController.navigate(Screen.FaceScan.route) }
                )
            }

            composable(Screen.ManualAttendance.route) {
                val studentViewModel: StudentViewModel = viewModel(factory = factory)
                ManualAttendanceScreen(
                    viewModel = studentViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                val attendanceViewModel: AttendanceViewModel = viewModel(factory = factory)
                AttendanceHistoryScreen(
                    viewModel = attendanceViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Sync.route) {
                val syncViewModel: SyncViewModel = viewModel(factory = factory)
                DataSyncScreen(
                    viewModel = syncViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateTo = { route ->
                        when (route) {
                            "sync" -> navController.navigate(Screen.Sync.route)
                            "school_info" -> navController.navigate(Screen.Home.route)
                            else -> navController.navigate(Screen.Settings.route)
                        }
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.FaceRegister.route) {
                FaceRegistrationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRegistrationComplete = { navController.popBackStack() }
                )
            }

            composable(Screen.Notifications.route) {
                val attendanceViewModel: AttendanceViewModel = viewModel(factory = factory)
                AttendanceHistoryScreen(
                    viewModel = attendanceViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
