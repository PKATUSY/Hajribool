package com.pkstudy.hajiribook.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pkstudy.hajiribook.data.local.converters.Converters
import com.pkstudy.hajiribook.data.local.dao.AppSettingsDao
import com.pkstudy.hajiribook.data.local.dao.AttendanceDao
import com.pkstudy.hajiribook.data.local.dao.ClassDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        UserEntity::class,
        SchoolEntity::class,
        ClassEntity::class,
        StudentEntity::class,
        FaceTemplateEntity::class,
        AttendanceEntity::class,
        SyncQueueEntity::class,
        SyncLogEntity::class,
        NotificationEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HajiriDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun faceTemplateDao(): FaceTemplateDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun classDao(): ClassDao
    abstract fun syncDao(): SyncDao
    abstract fun notificationDao(): NotificationDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        const val DATABASE_NAME = "hajiri_book.db"

        @Volatile
        private var INSTANCE: HajiriDatabase? = null

        fun getInstance(context: Context): HajiriDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HajiriDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(database: HajiriDatabase) {
            // Seed School
            database.classDao().insertSchool(
                SchoolEntity(
                    id = 1L,
                    name = "Central Model High School",
                    code = "SCH-2026",
                    address = "Main Road, Civil Lines",
                    contactNumber = "+91 9876543210",
                    email = "admin@hajiribook.edu",
                    academicSession = "2026-2027"
                )
            )

            // Seed Classes matching UI
            val classes = listOf(
                ClassEntity(id = "CLS_6A", name = "6th", section = "A", studentCount = 45),
                ClassEntity(id = "CLS_7A", name = "7th", section = "A", studentCount = 48),
                ClassEntity(id = "CLS_8A", name = "8th", section = "A", studentCount = 52),
                ClassEntity(id = "CLS_9A", name = "9th", section = "A", studentCount = 250),
                ClassEntity(id = "CLS_10A", name = "10th", section = "A", studentCount = 55)
            )
            database.classDao().insertClasses(classes)

            // Seed default User matching screenshot (Madhurendra Kumar, Teacher)
            val defaultTeacher = UserEntity(
                id = "USR_TEACHER_1",
                remoteId = 1L,
                username = "madhurendra",
                fullName = "Madhurendra Kumar",
                role = "TEACHER",
                mobile = "9876543210",
                email = "madhurendra@school.com",
                schoolId = 1L,
                schoolName = "Central Model High School",
                assignedClasses = "6th - 10th",
                isLoggedIn = true
            )
            database.userDao().insertUser(defaultTeacher)

            // Seed sample students from the design mockup
            val sampleStudents = listOf(
                StudentEntity(
                    localUuid = "stu-uuid-001",
                    remoteId = 101L,
                    studentId = "STU-2026-012",
                    fullName = "Rahul Kumar",
                    rollNumber = "12",
                    className = "9th",
                    section = "A",
                    gender = "Male",
                    mobile = "9876543211",
                    hasFaceEnrolled = true,
                    syncStatus = "SYNCED"
                ),
                StudentEntity(
                    localUuid = "stu-uuid-002",
                    remoteId = 102L,
                    studentId = "STU-2026-013",
                    fullName = "Aman Kumar",
                    rollNumber = "13",
                    className = "9th",
                    section = "A",
                    gender = "Male",
                    mobile = "9876543212",
                    hasFaceEnrolled = true,
                    syncStatus = "SYNCED"
                ),
                StudentEntity(
                    localUuid = "stu-uuid-003",
                    remoteId = 103L,
                    studentId = "STU-2026-014",
                    fullName = "Priya Kumari",
                    rollNumber = "14",
                    className = "9th",
                    section = "A",
                    gender = "Female",
                    mobile = "9876543213",
                    hasFaceEnrolled = true,
                    syncStatus = "SYNCED"
                ),
                StudentEntity(
                    localUuid = "stu-uuid-004",
                    remoteId = 104L,
                    studentId = "STU-2026-015",
                    fullName = "Suman Kumar",
                    rollNumber = "15",
                    className = "9th",
                    section = "A",
                    gender = "Male",
                    mobile = "9876543214",
                    hasFaceEnrolled = true,
                    syncStatus = "SYNCED"
                ),
                StudentEntity(
                    localUuid = "stu-uuid-005",
                    remoteId = 105L,
                    studentId = "STU-2026-016",
                    fullName = "Rohit Kumar",
                    rollNumber = "16",
                    className = "9th",
                    section = "A",
                    gender = "Male",
                    mobile = "9876543215",
                    hasFaceEnrolled = true,
                    syncStatus = "SYNCED"
                )
            )
            database.studentDao().insertStudents(sampleStudents)

            // Seed today's attendance records matching mockup (16 Nov 2026)
            val today = "2026-11-16"
            database.attendanceDao().insertAttendance(
                AttendanceEntity(
                    localUuid = UUID.randomUUID().toString(),
                    studentUuid = "stu-uuid-001",
                    studentName = "Rahul Kumar",
                    rollNumber = "12",
                    className = "9th",
                    date = today,
                    time = "09:12:15",
                    status = "PRESENT",
                    method = "FACE",
                    confidence = 0.94f,
                    syncStatus = "SYNCED"
                )
            )
            database.attendanceDao().insertAttendance(
                AttendanceEntity(
                    localUuid = UUID.randomUUID().toString(),
                    studentUuid = "stu-uuid-002",
                    studentName = "Aman Kumar",
                    rollNumber = "13",
                    className = "9th",
                    date = today,
                    time = "09:14:02",
                    status = "PRESENT",
                    method = "FACE",
                    confidence = 0.91f,
                    syncStatus = "SYNCED"
                )
            )
            database.attendanceDao().insertAttendance(
                AttendanceEntity(
                    localUuid = UUID.randomUUID().toString(),
                    studentUuid = "stu-uuid-003",
                    studentName = "Priya Kumari",
                    rollNumber = "14",
                    className = "9th",
                    date = today,
                    time = "09:15:30",
                    status = "ABSENT",
                    method = "MANUAL",
                    syncStatus = "SYNCED"
                )
            )
            database.attendanceDao().insertAttendance(
                AttendanceEntity(
                    localUuid = UUID.randomUUID().toString(),
                    studentUuid = "stu-uuid-004",
                    studentName = "Suman Kumar",
                    rollNumber = "15",
                    className = "9th",
                    date = today,
                    time = "09:16:45",
                    status = "PRESENT",
                    method = "FACE",
                    confidence = 0.89f,
                    syncStatus = "SYNCED"
                )
            )
            database.attendanceDao().insertAttendance(
                AttendanceEntity(
                    localUuid = UUID.randomUUID().toString(),
                    studentUuid = "stu-uuid-005",
                    studentName = "Rohit Kumar",
                    rollNumber = "16",
                    className = "9th",
                    date = today,
                    time = "09:17:10",
                    status = "ABSENT",
                    method = "MANUAL",
                    syncStatus = "SYNCED"
                )
            )

            // Seed initial settings
            database.appSettingsDao().setSetting(AppSettingEntity("recognition_threshold", "0.75"))
            database.appSettingsDao().setSetting(AppSettingEntity("voice_enabled", "true"))
            database.appSettingsDao().setSetting(AppSettingEntity("voice_language", "en"))
            database.appSettingsDao().setSetting(AppSettingEntity("server_url", "https://api.hajiribook.com/api/"))
            database.appSettingsDao().setSetting(AppSettingEntity("auto_sync", "true"))
        }
    }
}
