package com.pkstudy.hajiribook.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pkstudy.hajiribook.data.local.entity.FaceTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FaceTemplateDao {
    @Query("SELECT * FROM face_templates")
    fun getAllFaceTemplates(): Flow<List<FaceTemplateEntity>>

    @Query("SELECT * FROM face_templates")
    suspend fun getAllFaceTemplatesSync(): List<FaceTemplateEntity>

    @Query("SELECT * FROM face_templates WHERE studentUuid = :studentUuid")
    suspend fun getTemplatesForStudent(studentUuid: String): List<FaceTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: FaceTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<FaceTemplateEntity>)

    @Query("SELECT * FROM face_templates WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSyncTemplates(): List<FaceTemplateEntity>

    @Query("UPDATE face_templates SET syncStatus = :status, remoteId = :remoteId WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: String, remoteId: Long?)

    @Query("DELETE FROM face_templates WHERE studentUuid = :studentUuid")
    suspend fun deleteTemplatesForStudent(studentUuid: String)

    @Query("SELECT COUNT(*) FROM face_templates")
    suspend fun getCount(): Int
}
