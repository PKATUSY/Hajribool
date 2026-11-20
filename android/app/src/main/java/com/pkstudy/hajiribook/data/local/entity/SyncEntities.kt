package com.pkstudy.hajiribook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: String, // "STUDENT", "FACE_TEMPLATE", "ATTENDANCE"
    val localUuid: String,
    val action: String, // "CREATE", "UPDATE", "DELETE"
    val payloadJson: String,
    val retryCount: Int = 0,
    val status: String = "PENDING", // PENDING, IN_PROGRESS, FAILED, SYNCED
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_logs")
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val syncType: String = "AUTO", // AUTO, MANUAL
    val recordsPushed: Int = 0,
    val recordsPulled: Int = 0,
    val status: String = "SUCCESS", // SUCCESS, FAILED
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String = "INFO", // ATTENDANCE, SYNC, SECURITY, INFO
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
