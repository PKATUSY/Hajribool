package com.pkstudy.hajiribook.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "face_templates",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["localUuid"],
            childColumns = ["studentUuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentUuid"]),
        Index(value = ["syncStatus"])
    ]
)
data class FaceTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: Long? = null,
    val studentUuid: String,
    val sampleAngle: String = "FRONT", // FRONT, LEFT, RIGHT, UP, DOWN
    val embedding: FloatArray, // 192 or 512 dimensions MobileFaceNet vector
    val qualityScore: Float = 1.0f,
    val syncStatus: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FaceTemplateEntity
        if (id != other.id) return false
        if (studentUuid != other.studentUuid) return false
        if (sampleAngle != other.sampleAngle) return false
        if (!embedding.contentEquals(other.embedding)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + studentUuid.hashCode()
        result = 31 * result + sampleAngle.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}
