package com.pkstudy.hajiribook.data.remote.dto

import com.google.gson.annotations.SerializedName

// Common Base Response
data class ApiResponse<T>(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("error") val error: String? = null
)

// Auth
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String? = null
)

data class LoginResponseData(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: RemoteUserDto,
    @SerializedName("school") val school: RemoteSchoolDto?
)

data class RemoteUserDto(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("role") val role: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("email") val email: String,
    @SerializedName("school_id") val schoolId: Long?,
    @SerializedName("assigned_classes") val assignedClasses: String?
)

data class RemoteSchoolDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("address") val address: String?,
    @SerializedName("academic_session") val academicSession: String?
)

// Students
data class StudentSyncDto(
    @SerializedName("local_uuid") val localUuid: String,
    @SerializedName("id") val remoteId: Long? = null,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("roll_number") val rollNumber: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("section") val section: String = "A",
    @SerializedName("gender") val gender: String,
    @SerializedName("dob") val dob: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("has_face_enrolled") val hasFaceEnrolled: Boolean = false,
    @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis()
)

// Face Templates
data class FaceTemplateSyncDto(
    @SerializedName("student_uuid") val studentUuid: String,
    @SerializedName("sample_angle") val sampleAngle: String,
    @SerializedName("embedding") val embedding: List<Float>,
    @SerializedName("quality_score") val qualityScore: Float = 1.0f
)

// Attendance
data class AttendanceSyncDto(
    @SerializedName("local_uuid") val localUuid: String,
    @SerializedName("remote_id") val remoteId: Long? = null,
    @SerializedName("student_uuid") val studentUuid: String,
    @SerializedName("student_name") val studentName: String,
    @SerializedName("roll_number") val rollNumber: String,
    @SerializedName("class_name") val className: String,
    @SerializedName("section") val section: String = "A",
    @SerializedName("attendance_date") val date: String,
    @SerializedName("attendance_time") val time: String,
    @SerializedName("session") val session: String = "MORNING",
    @SerializedName("attendance_status") val status: String,
    @SerializedName("attendance_method") val method: String,
    @SerializedName("confidence") val confidence: Float = 1.0f,
    @SerializedName("device_id") val deviceId: String = "ANDROID_DEVICE",
    @SerializedName("teacher_id") val teacherId: String? = null
)

// Batch Sync
data class SyncPushRequest(
    @SerializedName("students") val students: List<StudentSyncDto> = emptyList(),
    @SerializedName("face_templates") val faceTemplates: List<FaceTemplateSyncDto> = emptyList(),
    @SerializedName("attendance") val attendance: List<AttendanceSyncDto> = emptyList()
)

data class SyncPushResponseData(
    @SerializedName("synced_students") val syncedStudents: Map<String, Long>, // localUuid -> remoteId
    @SerializedName("synced_attendance") val syncedAttendance: Map<String, Long>,
    @SerializedName("server_timestamp") val serverTimestamp: Long
)

data class SyncPullResponseData(
    @SerializedName("students") val students: List<StudentSyncDto>,
    @SerializedName("face_templates") val faceTemplates: List<FaceTemplateSyncDto>,
    @SerializedName("attendance") val attendance: List<AttendanceSyncDto>,
    @SerializedName("server_timestamp") val serverTimestamp: Long
)
