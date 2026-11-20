package com.pkstudy.hajiribook.ai.recognition

import com.pkstudy.hajiribook.data.local.entity.FaceTemplateEntity
import kotlin.math.max
import kotlin.math.sqrt

data class MatchResult(
    val studentUuid: String?,
    val confidence: Float,
    val isRecognized: Boolean
)

class FaceRecognitionEngine(
    private var recognitionThreshold: Float = 0.72f
) {
    // In-memory cache of registered face templates: studentUuid -> List of embedding vectors
    private val templateCache = HashMap<String, MutableList<FloatArray>>()

    fun updateThreshold(newThreshold: Float) {
        recognitionThreshold = newThreshold
    }

    fun getThreshold(): Float = recognitionThreshold

    fun loadTemplates(templates: List<FaceTemplateEntity>) {
        templateCache.clear()
        for (template in templates) {
            val list = templateCache.getOrPut(template.studentUuid) { mutableListOf() }
            list.add(template.embedding)
        }
    }

    fun registerStudentTemplate(studentUuid: String, embedding: FloatArray) {
        val list = templateCache.getOrPut(studentUuid) { mutableListOf() }
        list.add(embedding)
    }

    /**
     * Matches a probe face embedding against all enrolled student templates using Cosine Similarity.
     */
    fun matchFace(probeEmbedding: FloatArray): MatchResult {
        if (templateCache.isEmpty()) {
            return MatchResult(studentUuid = null, confidence = 0.0f, isRecognized = false)
        }

        var bestStudentUuid: String? = null
        var highestSimilarity = -1.0f

        for ((studentUuid, enrolledEmbeddings) in templateCache) {
            for (enrolled in enrolledEmbeddings) {
                val similarity = cosineSimilarity(probeEmbedding, enrolled)
                if (similarity > highestSimilarity) {
                    highestSimilarity = similarity
                    bestStudentUuid = studentUuid
                }
            }
        }

        val isMatch = highestSimilarity >= recognitionThreshold
        return MatchResult(
            studentUuid = if (isMatch) bestStudentUuid else null,
            confidence = max(0.0f, highestSimilarity),
            isRecognized = isMatch
        )
    }

    /**
     * Cosine similarity between two float vectors.
     * Range: [-1.0, 1.0]. For normalized face embeddings, higher is better match.
     */
    fun cosineSimilarity(vecA: FloatArray, vecB: FloatArray): Float {
        if (vecA.size != vecB.size) return 0.0f
        var dot = 0.0f
        var normA = 0.0f
        var normB = 0.0f

        for (i in vecA.indices) {
            val a = vecA[i]
            val b = vecB[i]
            dot += a * b
            normA += a * a
            normB += b * b
        }

        val denominator = sqrt(normA) * sqrt(normB)
        if (denominator < 1e-12f) return 0.0f
        return dot / denominator
    }

    fun getRegisteredStudentCount(): Int = templateCache.size
}
