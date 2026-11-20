package com.pkstudy.hajiribook

import com.pkstudy.hajiribook.ai.recognition.FaceRecognitionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FaceRecognitionEngineTest {

    private lateinit var engine: FaceRecognitionEngine

    @Before
    fun setUp() {
        engine = FaceRecognitionEngine(recognitionThreshold = 0.75f)
    }

    @Test
    fun testCosineSimilarityIdenticalVectors() {
        val vectorA = floatArrayOf(0.5f, 0.5f, 0.5f, 0.5f)
        val similarity = engine.cosineSimilarity(vectorA, vectorA)
        assertEquals(1.0f, similarity, 0.0001f)
    }

    @Test
    fun testCosineSimilarityOrthogonalVectors() {
        val vectorA = floatArrayOf(1.0f, 0.0f, 0.0f)
        val vectorB = floatArrayOf(0.0f, 1.0f, 0.0f)
        val similarity = engine.cosineSimilarity(vectorA, vectorB)
        assertEquals(0.0f, similarity, 0.0001f)
    }

    @Test
    fun testRecognitionMatchAboveThreshold() {
        val enrolledStudentUuid = "student_uuid_101"
        val enrolledEmbedding = floatArrayOf(0.8f, 0.6f, 0.0f, 0.0f)
        engine.registerStudentTemplate(enrolledStudentUuid, enrolledEmbedding)

        // Probe close to enrolled template
        val probeEmbedding = floatArrayOf(0.79f, 0.61f, 0.0f, 0.0f)
        val matchResult = engine.matchFace(probeEmbedding)

        assertTrue(matchResult.isRecognized)
        assertEquals(enrolledStudentUuid, matchResult.studentUuid)
        assertTrue(matchResult.confidence >= 0.75f)
    }

    @Test
    fun testUnknownFaceRejectedBelowThreshold() {
        val enrolledStudentUuid = "student_uuid_101"
        val enrolledEmbedding = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f)
        engine.registerStudentTemplate(enrolledStudentUuid, enrolledEmbedding)

        // Dissimilar probe
        val probeEmbedding = floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f)
        val matchResult = engine.matchFace(probeEmbedding)

        assertFalse(matchResult.isRecognized)
        assertEquals(null, matchResult.studentUuid)
    }
}
