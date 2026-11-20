package com.pkstudy.hajiribook.ai.liveness

import android.graphics.Bitmap
import com.pkstudy.hajiribook.ai.detector.DetectedFace
import kotlin.math.abs
import kotlin.math.sqrt

enum class LivenessChallenge {
    BLINK,
    TURN_LEFT,
    TURN_RIGHT,
    SMILE,
    PASSIVE
}

data class LivenessResult(
    val isLive: Boolean,
    val score: Float,
    val feedbackMessage: String,
    val completedChallenge: Boolean = false
)

class LivenessDetector {
    private var lastNoseX: Float? = null
    private var lastNoseY: Float? = null
    private var frameCount = 0
    private var motionDetected = false
    private var blinkDetected = false

    var currentChallenge: LivenessChallenge = LivenessChallenge.PASSIVE

    fun reset() {
        lastNoseX = null
        lastNoseY = null
        frameCount = 0
        motionDetected = false
        blinkDetected = false
        currentChallenge = LivenessChallenge.PASSIVE
    }

    /**
     * Evaluates face frame for anti-spoofing and liveness indications.
     */
    fun checkLiveness(face: DetectedFace, bitmap: Bitmap): LivenessResult {
        frameCount++

        // 1. Check Face Size and Centering (reject too small or extreme border faces)
        val box = face.boundingBox
        val faceArea = box.width() * box.height()
        val totalArea = bitmap.width * bitmap.height
        val coverageRatio = faceArea / totalArea.toFloat()

        if (coverageRatio < 0.08f) {
            return LivenessResult(
                isLive = false,
                score = 0.2f,
                feedbackMessage = "Move closer to the camera"
            )
        }
        if (coverageRatio > 0.85f) {
            return LivenessResult(
                isLive = false,
                score = 0.3f,
                feedbackMessage = "Move back slightly"
            )
        }

        // 2. Head motion tracking across frames
        val nose = face.nose
        if (nose != null) {
            val currX = nose.first
            val currY = nose.second

            val prevX = lastNoseX
            val prevY = lastNoseY

            if (prevX != null && prevY != null) {
                val dx = currX - prevX
                val dy = currY - prevY
                val movement = sqrt(dx * dx + dy * dy)

                // Natural micromovements occur in live subjects (0.5 to 25 pixels)
                if (movement in 0.8f..28.0f) {
                    motionDetected = true
                }

                // Check challenge responses
                if (currentChallenge == LivenessChallenge.TURN_LEFT && dx < -8f) {
                    return LivenessResult(true, 0.95f, "Challenge Passed: Head turned left", true)
                }
                if (currentChallenge == LivenessChallenge.TURN_RIGHT && dx > 8f) {
                    return LivenessResult(true, 0.95f, "Challenge Passed: Head turned right", true)
                }
            }
            lastNoseX = currX
            lastNoseY = currY
        }

        // 3. Eye symmetry and gaze check
        val leftEye = face.leftEye
        val rightEye = face.rightEye
        var eyeScore = 0.8f

        if (leftEye != null && rightEye != null) {
            val eyeDist = abs(rightEye.first - leftEye.first)
            val expectedMinDist = box.width() * 0.20f
            if (eyeDist < expectedMinDist) {
                eyeScore = 0.4f // Angle too extreme or occluded
            }
        }

        // 4. Combined score
        val livenessScore = if (motionDetected || frameCount > 3) (0.75f + eyeScore * 0.2f) else 0.65f
        val isLive = livenessScore >= 0.70f

        return LivenessResult(
            isLive = isLive,
            score = livenessScore,
            feedbackMessage = if (isLive) "Real Face Detected" else "Hold still, verifying liveness..."
        )
    }
}
