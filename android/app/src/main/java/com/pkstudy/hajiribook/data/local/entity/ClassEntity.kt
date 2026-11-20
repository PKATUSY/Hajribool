package com.pkstudy.hajiribook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey
    val id: String, // E.g., "CLS_9A"
    val remoteId: Long? = null,
    val name: String, // "9th"
    val section: String = "A",
    val studentCount: Int = 0,
    val schoolId: Long = 1L
)

@Entity(tableName = "schools")
data class SchoolEntity(
    @PrimaryKey
    val id: Long = 1L,
    val name: String = "Central Model High School",
    val code: String = "SCH-2026",
    val address: String = "Main Road, Civil Lines",
    val contactNumber: String = "+91 9876543210",
    val email: String = "admin@hajiribook.edu",
    val academicSession: String = "2026-2027"
)
