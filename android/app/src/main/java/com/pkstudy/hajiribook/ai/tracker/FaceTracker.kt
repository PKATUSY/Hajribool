package com.pkstudy.hajiribook.ai.tracker

import android.graphics.RectF
import com.pkstudy.hajiribook.ai.detector.DetectedFace
import kotlin.math.max
import kotlin.math.min

data class TrackedFace(
    val trackId: Int,
    var currentBox: RectF,
    var lastSeenTimestamp: Long = System.currentTimeMillis(),
    var isAttendanceMarked: Boolean = false,
    var lastAttendanceTimestamp: Long = 0L,
    var recognizedStudentUuid: String? = null,
    var recognizedStudentName: String? = null
)

class MultiFaceTracker(
    private val iouThreshold: Float = 0.40f,
    private val trackTimeoutMs: Long = 2500L,
    private val attendanceCooldownMs: Long = 8000L // 8s cooldown before re-processing same face
) {
    private var nextTrackId = 1
    private val activeTracks = mutableListOf<TrackedFace>()

    /**
     * Updates active tracks with newly detected faces from the latest camera frame.
     * Returns a list of DetectedFace objects with assigned trackingIds.
     */
    @Synchronized
    fun updateTracks(detectedFaces: List<DetectedFace>): List<DetectedFace> {
        val now = System.currentTimeMillis()
        cleanOldTracks(now)

        val updatedFaces = mutableListOf<DetectedFace>()
        val matchedTrackIndices = mutableSetOf<Int>()

        for (face in detectedFaces) {
            var bestTrackIndex = -1
            var bestIou = 0f

            for (i in activeTracks.indices) {
                if (i in matchedTrackIndices) continue
                val iou = calculateIoU(face.boundingBox, activeTracks[i].currentBox)
                if (iou > iouThreshold && iou > bestIou) {
                    bestIou = iou
                    bestTrackIndex = i
                }
            }

            val assignedTrackId: Int
            if (bestTrackIndex >= 0) {
                matchedTrackIndices.add(bestTrackIndex)
                val track = activeTracks[bestTrackIndex]
                track.currentBox = face.boundingBox
                track.lastSeenTimestamp = now
                assignedTrackId = track.trackId
            } else {
                assignedTrackId = nextTrackId++
                activeTracks.add(
                    TrackedFace(
                        trackId = assignedTrackId,
                        currentBox = face.boundingBox,
                        lastSeenTimestamp = now
                    )
                )
            }

            updatedFaces.add(face.copy(trackingId = assignedTrackId))
        }

        return updatedFaces
    }

    /**
     * Checks if attendance can be marked for this track ID (checks cooldown and duplicate status).
     */
    @Synchronized
    fun canMarkAttendance(trackId: Int): Boolean {
        val now = System.currentTimeMillis()
        val track = activeTracks.find { it.trackId == trackId } ?: return true
        if (track.isAttendanceMarked) {
            // Check if cooldown has elapsed
            if (now - track.lastAttendanceTimestamp < attendanceCooldownMs) {
                return false
            }
        }
        return true
    }

    /**
     * Flags attendance as successfully marked for this track ID.
     */
    @Synchronized
    fun recordAttendanceMarked(trackId: Int, studentUuid: String, studentName: String) {
        val track = activeTracks.find { it.trackId == trackId }
        val now = System.currentTimeMillis()
        if (track != null) {
            track.isAttendanceMarked = true
            track.lastAttendanceTimestamp = now
            track.recognizedStudentUuid = studentUuid
            track.recognizedStudentName = studentName
        }
    }

    private fun cleanOldTracks(now: Long) {
        activeTracks.removeAll { now - it.lastSeenTimestamp > trackTimeoutMs }
    }

    private fun calculateIoU(boxA: RectF, boxB: RectF): Float {
        val xA = max(boxA.left, boxB.left)
        val yA = max(boxA.top, boxB.top)
        val xB = min(boxA.right, boxB.right)
        val yB = min(boxA.bottom, boxB.bottom)

        val intersectionArea = max(0f, xB - xA) * max(0f, yB - yA)
        val boxAArea = boxA.width() * boxA.height()
        val boxBArea = boxB.width() * boxB.height()

        val unionArea = boxAArea + boxBArea - intersectionArea
        if (unionArea <= 0f) return 0f
        return intersectionArea / unionArea
    }

    fun reset() {
        activeTracks.clear()
        nextTrackId = 1
    }
}
