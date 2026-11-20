package com.pkstudy.hajiribook.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pkstudy.hajiribook.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

data class AttendanceCountResult(
    val totalCount: Int,
    val presentCount: Int,
    val absentCount: Int
)

data class DailyAttendanceSummary(
    val date: String,
    val totalCount: Int,
    val presentCount: Int,
    val absentCount: Int
)

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY time DESC")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date = :date AND className = :className ORDER BY CAST(rollNumber AS INTEGER) ASC")
    fun getAttendanceByDateAndClass(date: String, className: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentUuid = :studentUuid ORDER BY date DESC, time DESC")
    fun getAttendanceForStudent(studentUuid: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentUuid = :studentUuid AND date = :date AND session = :session LIMIT 1")
    suspend fun getAttendanceRecord(studentUuid: String, date: String, session: String): AttendanceEntity?

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'PRESENT'")
    fun getPresentCount(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'ABSENT'")
    fun getAbsentCount(date: String): Flow<Int>

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY time DESC LIMIT :limit")
    fun getRecentAttendance(date: String, limit: Int = 10): Flow<List<AttendanceEntity>>

    @Query("""
        SELECT 
            date, 
            COUNT(*) as totalCount,
            SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as presentCount,
            SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) as absentCount
        FROM attendance 
        GROUP BY date 
        ORDER BY date DESC 
        LIMIT 30
    """)
    fun getAttendanceHistorySummary(): Flow<List<DailyAttendanceSummary>>

    @Query("SELECT * FROM attendance WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSyncAttendance(): List<AttendanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(list: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Query("UPDATE attendance SET syncStatus = :status, remoteId = :remoteId WHERE localUuid = :uuid")
    suspend fun updateSyncStatus(uuid: String, status: String, remoteId: Long?)

    @Query("DELETE FROM attendance WHERE date = :date AND className = :className")
    suspend fun deleteAttendanceForClassAndDate(date: String, className: String)
}
