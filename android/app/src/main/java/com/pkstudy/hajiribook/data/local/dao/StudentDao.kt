package com.pkstudy.hajiribook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pkstudy.hajiribook.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY className ASC, CAST(rollNumber AS INTEGER) ASC, fullName ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE className = :className ORDER BY CAST(rollNumber AS INTEGER) ASC")
    fun getStudentsByClass(className: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE localUuid = :uuid LIMIT 1")
    suspend fun getStudentByUuid(uuid: String): StudentEntity?

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentByStudentId(studentId: String): StudentEntity?

    @Query("SELECT COUNT(*) FROM students")
    fun getTotalStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE className = :className")
    fun getStudentCountByClass(className: String): Flow<Int>

    @Query("SELECT * FROM students WHERE fullName LIKE '%' || :query || '%' OR rollNumber LIKE '%' || :query || '%' OR studentId LIKE '%' || :query || '%'")
    fun searchStudents(query: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSyncStudents(): List<StudentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("UPDATE students SET hasFaceEnrolled = :enrolled WHERE localUuid = :uuid")
    suspend fun updateFaceEnrolledStatus(uuid: String, enrolled: Boolean)

    @Query("UPDATE students SET syncStatus = :status, remoteId = :remoteId WHERE localUuid = :localUuid")
    suspend fun updateSyncStatus(localUuid: String, status: String, remoteId: Long?)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE localUuid = :uuid")
    suspend fun deleteStudentByUuid(uuid: String)
}
