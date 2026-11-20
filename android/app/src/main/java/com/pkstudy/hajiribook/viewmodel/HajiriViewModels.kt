package com.pkstudy.hajiribook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pkstudy.hajiribook.HajiriApp
import com.pkstudy.hajiribook.ai.detector.DetectedFace
import com.pkstudy.hajiribook.camera.LiveFaceResult
import com.pkstudy.hajiribook.data.local.dao.DailyAttendanceSummary
import com.pkstudy.hajiribook.data.local.entity.AttendanceEntity
import com.pkstudy.hajiribook.data.local.entity.ClassEntity
import com.pkstudy.hajiribook.data.local.entity.StudentEntity
import com.pkstudy.hajiribook.data.local.entity.SyncLogEntity
import com.pkstudy.hajiribook.data.local.entity.UserEntity
import com.pkstudy.hajiribook.data.repository.DashboardStats
import com.pkstudy.hajiribook.data.repository.HajiriRepository
import com.pkstudy.hajiribook.security.encryption.BackupFileInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AuthViewModel(private val repository: HajiriRepository) : ViewModel() {
    val currentUser: StateFlow<UserEntity?> = repository.getLoggedInUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _loginState = MutableStateFlow<Result<UserEntity>?>(null)
    val loginState: StateFlow<Result<UserEntity>?> = _loginState.asStateFlow()

    private val _selectedRole = MutableStateFlow("Teacher")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    fun selectRole(role: String) {
        _selectedRole.value = role
    }

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            val result = repository.login(username, pass, _selectedRole.value.uppercase())
            _loginState.value = result
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}

class HomeViewModel(private val repository: HajiriRepository) : ViewModel() {
    private val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val user: StateFlow<UserEntity?> = repository.getLoggedInUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val stats: StateFlow<DashboardStats> = repository.getDashboardStats(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats(250, 185, 65, 74))

    val recentAttendance: StateFlow<List<AttendanceEntity>> = repository.getRecentAttendance(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingSyncCount: StateFlow<Int> = repository.getPendingSyncCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lastSyncLog: StateFlow<SyncLogEntity?> = repository.getLastSyncLog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

class StudentViewModel(private val repository: HajiriRepository) : ViewModel() {
    val students: StateFlow<List<StudentEntity>> = repository.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classes: StateFlow<List<ClassEntity>> = repository.getAllClasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addStudent(
        fullName: String,
        rollNumber: String,
        className: String,
        gender: String,
        dob: String,
        mobile: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val student = StudentEntity(
                localUuid = UUID.randomUUID().toString(),
                studentId = "STU-${System.currentTimeMillis().toString().takeLast(6)}",
                fullName = fullName,
                rollNumber = rollNumber,
                className = className,
                gender = gender,
                dob = dob,
                mobile = mobile,
                hasFaceEnrolled = false
            )
            repository.registerStudent(student)
            onSuccess()
        }
    }

    fun deleteStudent(uuid: String) {
        viewModelScope.launch {
            repository.deleteStudent(uuid)
        }
    }
}

class FaceScanViewModel(
    private val app: HajiriApp,
    private val repository: HajiriRepository
) : ViewModel() {
    private val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val stats: StateFlow<DashboardStats> = repository.getDashboardStats(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats(250, 185, 65, 74))

    private val _liveResults = MutableStateFlow<List<LiveFaceResult>>(emptyList())
    val liveResults: StateFlow<List<LiveFaceResult>> = _liveResults.asStateFlow()

    private val _lastRecognizedStudent = MutableStateFlow<StudentEntity?>(null)
    val lastRecognizedStudent: StateFlow<StudentEntity?> = _lastRecognizedStudent.asStateFlow()

    private val _attendanceStatusMessage = MutableStateFlow("Live Scanning...")
    val attendanceStatusMessage: StateFlow<String> = _attendanceStatusMessage.asStateFlow()

    private val _scanSuccessEvent = MutableSharedFlow<String>()
    val scanSuccessEvent: SharedFlow<String> = _scanSuccessEvent.asSharedFlow()

    fun onFacesProcessed(results: List<LiveFaceResult>) {
        _liveResults.value = results
        for (item in results) {
            val face = item.face
            val trackId = face.trackingId

            if (item.isRecognized && item.studentUuid != null) {
                // Check if tracker permits attendance marking (cooldown check)
                if (app.faceTracker.canMarkAttendance(trackId)) {
                    processRecognizedFace(item.studentUuid, item.confidence, trackId)
                }
            } else if (!item.liveness.isLive) {
                _attendanceStatusMessage.value = item.liveness.feedbackMessage
            } else if (results.isNotEmpty()) {
                _attendanceStatusMessage.value = "Scanning... Unknown Face"
            }
        }
    }

    private fun processRecognizedFace(studentUuid: String, confidence: Float, trackId: Int) {
        viewModelScope.launch {
            val student = app.database.studentDao().getStudentByUuid(studentUuid) ?: return@launch

            val result = repository.markAttendance(
                studentUuid = student.localUuid,
                studentName = student.fullName,
                rollNumber = student.rollNumber,
                className = student.className,
                section = student.section,
                date = todayDate,
                confidence = confidence
            )

            if (result.isSuccess) {
                app.faceTracker.recordAttendanceMarked(trackId, student.localUuid, student.fullName)
                _lastRecognizedStudent.value = student
                _attendanceStatusMessage.value = "${student.fullName} marked Present!"
                _scanSuccessEvent.emit(student.fullName)
                // Announce name non-blockingly
                app.ttsManager.announceAttendance(student.fullName)
            }
        }
    }
}

class AttendanceViewModel(private val repository: HajiriRepository) : ViewModel() {
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val _selectedDate = MutableStateFlow(today)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedClass = MutableStateFlow("9th")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    val attendanceRecords: StateFlow<List<AttendanceEntity>> = repository.getAttendanceByDate(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historySummary: StateFlow<List<DailyAttendanceSummary>> = repository.getAttendanceHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDate(date: String) {
        _selectedDate.value = date
    }

    fun setClass(className: String) {
        _selectedClass.value = className
    }

    fun toggleStudentAttendance(
        student: StudentEntity,
        newStatus: String
    ) {
        viewModelScope.launch {
            repository.markAttendance(
                studentUuid = student.localUuid,
                studentName = student.fullName,
                rollNumber = student.rollNumber,
                className = student.className,
                date = _selectedDate.value,
                status = newStatus,
                method = "MANUAL"
            )
        }
    }
}

class SyncViewModel(private val repository: HajiriRepository) : ViewModel() {
    val pendingCount: StateFlow<Int> = repository.getPendingSyncCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 12)

    val failedCount: StateFlow<Int> = repository.getFailedSyncCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    fun triggerSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Syncing with MySQL server..."
            val result = repository.performBidirectionalSync()
            _isSyncing.value = false
            _syncMessage.value = if (result.isSuccess) "Sync completed!" else "Sync failed: ${result.exceptionOrNull()?.message}"
        }
    }
}

class SettingsViewModel(
    private val app: HajiriApp,
    private val repository: HajiriRepository
) : ViewModel() {
    private val _backups = MutableStateFlow<List<BackupFileInfo>>(emptyList())
    val backups: StateFlow<List<BackupFileInfo>> = _backups.asStateFlow()

    init {
        loadBackups()
    }

    fun loadBackups() {
        _backups.value = app.backupManager.listBackups()
    }

    fun createBackup(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = app.backupManager.createEncryptedBackup()
            if (result.isSuccess) {
                loadBackups()
                onSuccess(result.getOrNull()!!.fileName)
            } else {
                onError(result.exceptionOrNull()?.message ?: "Backup failed")
            }
        }
    }

    fun restoreBackup(backupFile: File, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = app.backupManager.restoreFromBackup(backupFile)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Restore failed")
            }
        }
    }
}

// Factory for ViewModels
class HajiriViewModelFactory(private val app: HajiriApp) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(app.repository) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(app.repository) as T
            modelClass.isAssignableFrom(StudentViewModel::class.java) -> StudentViewModel(app.repository) as T
            modelClass.isAssignableFrom(FaceScanViewModel::class.java) -> FaceScanViewModel(app, app.repository) as T
            modelClass.isAssignableFrom(AttendanceViewModel::class.java) -> AttendanceViewModel(app.repository) as T
            modelClass.isAssignableFrom(SyncViewModel::class.java) -> SyncViewModel(app.repository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(app, app.repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
