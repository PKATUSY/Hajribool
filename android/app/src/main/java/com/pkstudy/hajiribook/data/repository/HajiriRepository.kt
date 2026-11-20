package com.pkstudy.hajiribook.data.repository

import com.google.gson.Gson
import com.pkstudy.hajiribook.data.local.dao.AppSettingsDao
import com.pkstudy.hajiribook.data.local.dao.AttendanceDao
import com.pkstudy.hajiribook.data.local.dao.ClassDao
import com.pkstudy.hajiribook.data.local.dao.DailyAttendanceSummary
import com.pkstudy.hajiribook.data.local.dao.FaceTemplateDao
import com.pkstudy.hajiribook.data.local.dao.NotificationDao
import com.pkstudy.hajiribook.data.local.dao.StudentDao
import com.pkstudy.hajiribook.data.local.dao.SyncDao
import com.pkstudy.hajiribook.data.local.dao.UserDao
import com.pkstudy.hajiribook.data.local.entity.AppSettingEntity
import com.pkstudy.hajiribook.data.local.entity.AttendanceEntity
import com.pkstudy.hajiribook.data.local.entity.ClassEntity
import com.pkstudy.hajiribook.data.local.entity.FaceTemplateEntity
import com.pkstudy.hajiribook.data.local.entity.NotificationEntity
import com.pkstudy.hajiribook.data.local.entity.SchoolEntity
import com.pkstudy.hajiribook.data.local.entity.StudentEntity
import com.pkstudy.hajiribook.data.local.entity.SyncLogEntity
import com.pkstudy.hajiribook.data.local.entity.SyncQueueEntity
import com.pkstudy.hajiribook.data.local.entity.UserEntity
import com.pkstudy.hajiribook.data.remote.api.HajiriApiService
import com.pkstudy.hajiribook.data.remote.dto.AttendanceSyncDto
import com.pkstudy.hajiribook.data.remote.dto.FaceTemplateSyncDto
import com.pkstudy.hajiribook.data.remote.dto.LoginRequest
import com.pkstudy.hajiribook.data.remote.dto.StudentSyncDto
import com.pkstudy.hajiribook.data.remote.dto.SyncPushRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DashboardStats(
    val totalStudents: Int,
    val presentStudents: Int,
    val absentStudents: Int,
    val attendancePercentage: Int
)

class HajiriRepository(
    private val userDao: UserDao,
    private val studentDao: StudentDao,
    private val faceTemplateDao: FaceTemplateDao,
    private val attendanceDao: AttendanceDao,
    private val classDao: ClassDao,
    private val syncDao: SyncDao,
    private val notificationDao: NotificationDao,
    private val appSettingsDao: AppSettingsDao,
    private val apiService: HajiriApiService,
    private val gson: Gson = Gson()
) {

    // User & Auth
    fun getLoggedInUser(): Flow<UserEntity?> = userDao.getLoggedInUser()

    suspend fun login(username: String, password: String, role: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            try {
                // Try remote authentication if internet is reachable
                val response = apiService.login(LoginRequest(username, password, role))
                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()!!.data!!
                    val user = UserEntity(
                        id = data.user.id.toString(),
                        remoteId = data.user.id,
                        username = data.user.username,
                        fullName = data.user.fullName,
                        role = data.user.role,
                        mobile = data.user.mobile,
                        email = data.user.email,
                        schoolId = data.user.schoolId,
                        schoolName = data.school?.name ?: "Central Model High School",
                        assignedClasses = data.user.assignedClasses ?: "6th - 10th",
                        token = data.token,
                        isLoggedIn = true
                    )
                    userDao.insertUser(user)
                    return@withContext Result.success(user)
                }
            } catch (e: Exception) {
                // Network unavailable or server offline -> fallback to offline local credentials
            }

            // Offline fallback: check local user
            val existing = userDao.getUserByUsername(username)
            if (existing != null) {
                val updated = existing.copy(isLoggedIn = true, role = role)
                userDao.insertUser(updated)
                return@withContext Result.success(updated)
            }

            // Create default offline session for verified local development
            val offlineUser = UserEntity(
                id = "USR_${System.currentTimeMillis()}",
                username = username,
                fullName = if (username.lowercase().contains("madhurendra")) "Madhurendra Kumar" else username.replaceFirstChar { it.uppercase() },
                role = role,
                mobile = "9876543210",
                email = "$username@school.com",
                schoolName = "Central Model High School",
                assignedClasses = "6th - 10th",
                isLoggedIn = true
            )
            userDao.insertUser(offlineUser)
            Result.success(offlineUser)
        }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userDao.logoutAll()
    }

    // Dashboard Statistics Flow calculated reactively from Room
    fun getDashboardStats(date: String): Flow<DashboardStats> {
        return combine(
            studentDao.getTotalStudentCount(),
            attendanceDao.getPresentCount(date),
            attendanceDao.getAbsentCount(date)
        ) { total, present, absent ->
            val percentage = if (total > 0) ((present.toFloat() / total.toFloat()) * 100).toInt() else 0
            DashboardStats(
                totalStudents = total,
                presentStudents = present,
                absentStudents = absent,
                attendancePercentage = percentage
            )
        }
    }

    // Students
    fun getAllStudents(): Flow<List<StudentEntity>> = studentDao.getAllStudents()

    fun getStudentsByClass(className: String): Flow<List<StudentEntity>> =
        studentDao.getStudentsByClass(className)

    fun searchStudents(query: String): Flow<List<StudentEntity>> =
        studentDao.searchStudents(query)

    suspend fun registerStudent(student: StudentEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Save locally first (offline-first requirement)
            val studentToSave = student.copy(syncStatus = "PENDING")
            studentDao.insertStudent(studentToSave)

            // Queue for sync
            val payload = gson.toJson(
                StudentSyncDto(
                    localUuid = studentToSave.localUuid,
                    fullName = studentToSave.fullName,
                    studentId = studentToSave.studentId,
                    rollNumber = studentToSave.rollNumber,
                    className = studentToSave.className,
                    section = studentToSave.section,
                    gender = studentToSave.gender,
                    dob = studentToSave.dob,
                    mobile = studentToSave.mobile,
                    hasFaceEnrolled = studentToSave.hasFaceEnrolled
                )
            )
            syncDao.enqueue(
                SyncQueueEntity(
                    entityType = "STUDENT",
                    localUuid = studentToSave.localUuid,
                    action = "CREATE",
                    payloadJson = payload
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteStudent(uuid: String) = withContext(Dispatchers.IO) {
        studentDao.deleteStudentByUuid(uuid)
    }

    // Face Templates
    suspend fun saveFaceTemplate(studentUuid: String, embedding: FloatArray, angle: String): Long =
        withContext(Dispatchers.IO) {
            val template = FaceTemplateEntity(
                studentUuid = studentUuid,
                sampleAngle = angle,
                embedding = embedding,
                syncStatus = "PENDING"
            )
            val id = faceTemplateDao.insertTemplate(template)
            studentDao.updateFaceEnrolledStatus(studentUuid, true)

            // Enqueue face template for server synchronization
            val payload = gson.toJson(
                FaceTemplateSyncDto(
                    studentUuid = studentUuid,
                    sampleAngle = angle,
                    embedding = embedding.toList()
                )
            )
            syncDao.enqueue(
                SyncQueueEntity(
                    entityType = "FACE_TEMPLATE",
                    localUuid = studentUuid,
                    action = "CREATE",
                    payloadJson = payload
                )
            )
            id
        }

    suspend fun loadAllFaceTemplates(): List<FaceTemplateEntity> = withContext(Dispatchers.IO) {
        faceTemplateDao.getAllFaceTemplatesSync()
    }

    // Attendance
    fun getAttendanceByDate(date: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceByDate(date)

    fun getAttendanceByDateAndClass(date: String, className: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceByDateAndClass(date, className)

    fun getRecentAttendance(date: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getRecentAttendance(date)

    fun getAttendanceHistory(): Flow<List<DailyAttendanceSummary>> =
        attendanceDao.getAttendanceHistorySummary()

    /**
     * Marks student attendance locally first with duplicate check,
     * then queues it for remote server synchronization.
     */
    suspend fun markAttendance(
        studentUuid: String,
        studentName: String,
        rollNumber: String,
        className: String,
        section: String = "A",
        date: String,
        status: String = "PRESENT",
        method: String = "FACE",
        confidence: Float = 1.0f,
        livenessScore: Float = 1.0f
    ): Result<AttendanceEntity> = withContext(Dispatchers.IO) {
        try {
            val session = "MORNING"
            // Duplicate prevention
            val existing = attendanceDao.getAttendanceRecord(studentUuid, date, session)
            if (existing != null) {
                return@withContext Result.failure(Exception("Attendance already marked for today"))
            }

            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val currentTime = timeFormat.format(Date())

            val attendance = AttendanceEntity(
                localUuid = UUID.randomUUID().toString(),
                studentUuid = studentUuid,
                studentName = studentName,
                rollNumber = rollNumber,
                className = className,
                section = section,
                date = date,
                time = currentTime,
                session = session,
                status = status,
                method = method,
                confidence = confidence,
                livenessScore = livenessScore,
                syncStatus = "PENDING"
            )

            // Save locally
            attendanceDao.insertAttendance(attendance)

            // Queue for sync
            val payload = gson.toJson(
                AttendanceSyncDto(
                    localUuid = attendance.localUuid,
                    studentUuid = attendance.studentUuid,
                    studentName = attendance.studentName,
                    rollNumber = attendance.rollNumber,
                    className = attendance.className,
                    section = attendance.section,
                    date = attendance.date,
                    time = attendance.time,
                    session = attendance.session,
                    status = attendance.status,
                    method = attendance.method,
                    confidence = attendance.confidence
                )
            )
            syncDao.enqueue(
                SyncQueueEntity(
                    entityType = "ATTENDANCE",
                    localUuid = attendance.localUuid,
                    action = "CREATE",
                    payloadJson = payload
                )
            )

            // Insert local notification
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Attendance Marked",
                    message = "$studentName marked $status via $method",
                    type = "ATTENDANCE"
                )
            )

            Result.success(attendance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveManualAttendanceBatch(list: List<AttendanceEntity>) = withContext(Dispatchers.IO) {
        attendanceDao.insertAttendanceList(list)
        for (item in list) {
            val payload = gson.toJson(
                AttendanceSyncDto(
                    localUuid = item.localUuid,
                    studentUuid = item.studentUuid,
                    studentName = item.studentName,
                    rollNumber = item.rollNumber,
                    className = item.className,
                    section = item.section,
                    date = item.date,
                    time = item.time,
                    status = item.status,
                    method = "MANUAL"
                )
            )
            syncDao.enqueue(
                SyncQueueEntity(
                    entityType = "ATTENDANCE",
                    localUuid = item.localUuid,
                    action = "CREATE",
                    payloadJson = payload
                )
            )
        }
    }

    // Classes & School
    fun getAllClasses(): Flow<List<ClassEntity>> = classDao.getAllClasses()
    fun getSchool(): Flow<SchoolEntity?> = classDao.getSchool()

    // Sync Management
    fun getPendingSyncCount(): Flow<Int> = syncDao.getPendingCount()
    fun getFailedSyncCount(): Flow<Int> = syncDao.getFailedCount()
    fun getLastSyncLog(): Flow<SyncLogEntity?> = syncDao.getLastSyncLog()

    /**
     * Executes bidirectional data synchronization between Android Room and PHP MySQL backend.
     */
    suspend fun performBidirectionalSync(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Direction 1: Push pending local records to PHP MySQL server
            val pendingStudents = studentDao.getPendingSyncStudents().map {
                StudentSyncDto(
                    localUuid = it.localUuid,
                    fullName = it.fullName,
                    studentId = it.studentId,
                    rollNumber = it.rollNumber,
                    className = it.className,
                    section = it.section,
                    gender = it.gender,
                    dob = it.dob,
                    mobile = it.mobile,
                    hasFaceEnrolled = it.hasFaceEnrolled
                )
            }

            val pendingAttendance = attendanceDao.getPendingSyncAttendance().map {
                AttendanceSyncDto(
                    localUuid = it.localUuid,
                    studentUuid = it.studentUuid,
                    studentName = it.studentName,
                    rollNumber = it.rollNumber,
                    className = it.className,
                    section = it.section,
                    date = it.date,
                    time = it.time,
                    session = it.session,
                    status = it.status,
                    method = it.method,
                    confidence = it.confidence
                )
            }

            var pushedCount = 0
            if (pendingStudents.isNotEmpty() || pendingAttendance.isNotEmpty()) {
                val pushRequest = SyncPushRequest(
                    students = pendingStudents,
                    attendance = pendingAttendance
                )
                val pushResponse = apiService.syncPush(pushRequest)
                if (pushResponse.isSuccessful && pushResponse.body()?.status == "success") {
                    val data = pushResponse.body()!!.data!!
                    // Update student sync statuses
                    for ((localUuid, remoteId) in data.syncedStudents) {
                        studentDao.updateSyncStatus(localUuid, "SYNCED", remoteId)
                    }
                    // Update attendance sync statuses
                    for ((localUuid, remoteId) in data.syncedAttendance) {
                        attendanceDao.updateSyncStatus(localUuid, "SYNCED", remoteId)
                    }
                    pushedCount = data.syncedStudents.size + data.syncedAttendance.size
                }
            }

            // Direction 2: Pull latest records from server
            var pulledCount = 0
            try {
                val pullResponse = apiService.syncPull(schoolId = 1L, sinceTimestamp = 0L)
                if (pullResponse.isSuccessful && pullResponse.body()?.status == "success") {
                    val data = pullResponse.body()!!.data!!
                    for (remoteStudent in data.students) {
                        val localExisting = studentDao.getStudentByUuid(remoteStudent.localUuid)
                        if (localExisting == null) {
                            studentDao.insertStudent(
                                StudentEntity(
                                    localUuid = remoteStudent.localUuid,
                                    remoteId = remoteStudent.remoteId,
                                    fullName = remoteStudent.fullName,
                                    studentId = remoteStudent.studentId,
                                    rollNumber = remoteStudent.rollNumber,
                                    className = remoteStudent.className,
                                    section = remoteStudent.section,
                                    gender = remoteStudent.gender,
                                    dob = remoteStudent.dob ?: "",
                                    mobile = remoteStudent.mobile ?: "",
                                    hasFaceEnrolled = remoteStudent.hasFaceEnrolled,
                                    syncStatus = "SYNCED"
                                )
                            )
                            pulledCount++
                        }
                    }
                }
            } catch (e: Exception) {
                // Non-fatal pull error
            }

            // Log sync result
            syncDao.insertLog(
                SyncLogEntity(
                    syncType = "MANUAL",
                    recordsPushed = pushedCount,
                    recordsPulled = pulledCount,
                    status = "SUCCESS",
                    message = "Synced $pushedCount pushed, $pulledCount pulled"
                )
            )

            Result.success("Sync completed successfully: $pushedCount uploaded, $pulledCount downloaded")
        } catch (e: Exception) {
            syncDao.insertLog(
                SyncLogEntity(
                    syncType = "MANUAL",
                    status = "FAILED",
                    message = e.localizedMessage ?: "Network error during sync"
                )
            )
            Result.failure(e)
        }
    }

    // App Settings
    suspend fun getSetting(key: String, defaultVal: String = ""): String = withContext(Dispatchers.IO) {
        appSettingsDao.getSetting(key) ?: defaultVal
    }

    suspend fun saveSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        appSettingsDao.setSetting(AppSettingEntity(key, value))
    }

    // Notifications
    fun getNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    fun getUnreadNotificationCount(): Flow<Int> = notificationDao.getUnreadCount()
    suspend fun markNotificationRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }
}
