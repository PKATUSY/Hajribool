package com.pkstudy.hajiribook.data.remote.api

import com.pkstudy.hajiribook.data.remote.dto.ApiResponse
import com.pkstudy.hajiribook.data.remote.dto.AttendanceSyncDto
import com.pkstudy.hajiribook.data.remote.dto.FaceTemplateSyncDto
import com.pkstudy.hajiribook.data.remote.dto.LoginRequest
import com.pkstudy.hajiribook.data.remote.dto.LoginResponseData
import com.pkstudy.hajiribook.data.remote.dto.StudentSyncDto
import com.pkstudy.hajiribook.data.remote.dto.SyncPullResponseData
import com.pkstudy.hajiribook.data.remote.dto.SyncPushRequest
import com.pkstudy.hajiribook.data.remote.dto.SyncPushResponseData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HajiriApiService {

    @POST("auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponseData>>

    @POST("auth/logout.php")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET("students/get_students.php")
    suspend fun getStudents(
        @Query("school_id") schoolId: Long,
        @Query("since") sinceTimestamp: Long? = null
    ): Response<ApiResponse<List<StudentSyncDto>>>

    @POST("students/add_student.php")
    suspend fun addStudent(@Body student: StudentSyncDto): Response<ApiResponse<StudentSyncDto>>

    @POST("face/register_face.php")
    suspend fun registerFace(@Body template: FaceTemplateSyncDto): Response<ApiResponse<Unit>>

    @POST("attendance/mark_attendance.php")
    suspend fun markAttendance(@Body attendance: AttendanceSyncDto): Response<ApiResponse<AttendanceSyncDto>>

    @POST("sync/sync_push.php")
    suspend fun syncPush(@Body request: SyncPushRequest): Response<ApiResponse<SyncPushResponseData>>

    @GET("sync/sync_pull.php")
    suspend fun syncPull(
        @Query("school_id") schoolId: Long,
        @Query("since") sinceTimestamp: Long
    ): Response<ApiResponse<SyncPullResponseData>>
}
