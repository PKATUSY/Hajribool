package com.pkstudy.hajiribook.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "attendance",
    indices = [
        // Ensure uniqueness: One attendance per student per date per session
        Index(value = ["studentUuid", "date", "session"], unique = true),
        Index(value = ["className", "date"]),
        Index(value = ["date"]),
        Index(value = ["syncStatus"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey
    val localUuid: String = UUID.randomUUID().toString(),
    val remoteId: Long? = null,
    val studentUuid: String,
    val studentName: String,
    val rollNumber: String,
    val className: String,
    val section: String = "A",
    val date: String, // "YYYY-MM-DD" e.g., "2026-11-16"
    val time: String, // "HH:mm:ss" e.g., "09:15:30"
    val session: String = "MORNING", // MORNING, AFTERNOON
    val status: String, // PRESENT, ABSENT, LATE, HALF_DAY, LEAVE
    val method: String, // FACE, MANUAL, QR
    val confidence: Float = 1.0f,
    val livenessScore: Float = 1.0f,
    val deviceId: String = "ANDROID_DEVICE",
    val teacherId: String = "DEFAULT_TEACHER",
    val syncStatus: String = "PENDING", // PENDING, SYNCED, FAILED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
