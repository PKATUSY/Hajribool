package com.pkstudy.hajiribook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String, // Local or remote ID
    val remoteId: Long? = null,
    val username: String,
    val fullName: String,
    val role: String, // "ADMIN" or "TEACHER"
    val mobile: String,
    val email: String,
    val schoolId: Long? = null,
    val schoolName: String = "Central Model High School",
    val assignedClasses: String = "6th - 10th",
    val profilePhotoUri: String? = null,
    val token: String? = null,
    val isLoggedIn: Boolean = false,
    val lastLogin: Long = System.currentTimeMillis()
)
