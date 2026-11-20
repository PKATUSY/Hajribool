package com.pkstudy.hajiribook.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["studentId"], unique = true),
        Index(value = ["className", "section", "rollNumber"]),
        Index(value = ["syncStatus"])
    ]
)
data class StudentEntity(
    @PrimaryKey
    val localUuid: String = UUID.randomUUID().toString(),
    val remoteId: Long? = null,
    val schoolId: Long? = 1L,
    val fullName: String,
    val fatherName: String = "",
    val motherName: String = "",
    val studentId: String, // E.g., STU-2026-001
    val admissionNo: String = "",
    val rollNumber: String,
    val className: String, // E.g. "9th"
    val section: String = "A",
    val dob: String = "",
    val gender: String = "Male",
    val mobile: String = "",
    val address: String = "",
    val photoUri: String? = null,
    val hasFaceEnrolled: Boolean = false,
    val syncStatus: String = "PENDING", // PENDING, SYNCED, FAILED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
