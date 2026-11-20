package com.pkstudy.hajiribook

import android.graphics.RectF
import com.pkstudy.hajiribook.ai.detector.DetectedFace
import com.pkstudy.hajiribook.ai.tracker.MultiFaceTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MultiFaceTrackerTest {

    private lateinit var tracker: MultiFaceTracker

    @Before
    fun setUp() {
        tracker = MultiFaceTracker(attendanceCooldownMs = 2000L)
    }

    @Test
    fun testFaceTrackingAssignsStableId() {
        // Frame 1
        val face1 = DetectedFace(
            boundingBox = RectF(10f, 10f, 100f, 100f),
            score = 0.95f
        )
        val trackedFrame1 = tracker.updateTracks(listOf(face1))
        val trackId1 = trackedFrame1.first().trackingId

        // Frame 2: slight movement
        val face2 = DetectedFace(
            boundingBox = RectF(12f, 11f, 102f, 101f),
            score = 0.94f
        )
        val trackedFrame2 = tracker.updateTracks(listOf(face2))
        val trackId2 = trackedFrame2.first().trackingId

        assertEquals(trackId1, trackId2)
    }

    @Test
    fun testAttendanceCooldownPreventsSpam() {
        val face = DetectedFace(
            boundingBox = RectF(10f, 10f, 100f, 100f),
            score = 0.95f
        )
        val tracked = tracker.updateTracks(listOf(face)).first()
        val trackId = tracked.trackingId

        // Before marking
        assertTrue(tracker.canMarkAttendance(trackId))

        // Record attendance
        tracker.recordAttendanceMarked(trackId, "uuid_123", "Rahul Kumar")

        // Immediately after marking -> should be blocked by cooldown
        assertFalse(tracker.canMarkAttendance(trackId))
    }
}
