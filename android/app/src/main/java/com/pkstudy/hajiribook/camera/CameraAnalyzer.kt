package com.pkstudy.hajiribook.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.pkstudy.hajiribook.ai.detector.DetectedFace
import com.pkstudy.hajiribook.ai.detector.TFLiteFaceDetector
import com.pkstudy.hajiribook.ai.embedding.MobileFaceNetEmbedder
import com.pkstudy.hajiribook.ai.liveness.LivenessDetector
import com.pkstudy.hajiribook.ai.liveness.LivenessResult
import com.pkstudy.hajiribook.ai.recognition.FaceRecognitionEngine
import com.pkstudy.hajiribook.ai.tracker.MultiFaceTracker
import java.util.concurrent.atomic.AtomicBoolean

data class LiveFaceResult(
    val face: DetectedFace,
    val liveness: LivenessResult,
    val studentUuid: String?,
    val isRecognized: Boolean,
    val confidence: Float,
    val frameBitmap: Bitmap
)

class CameraFaceAnalyzer(
    private val faceDetector: TFLiteFaceDetector,
    private val faceEmbedder: MobileFaceNetEmbedder,
    private val recognitionEngine: FaceRecognitionEngine,
    private val livenessDetector: LivenessDetector,
    private val faceTracker: MultiFaceTracker,
    private val onFacesProcessed: (List<LiveFaceResult>) -> Unit
) : ImageAnalysis.Analyzer {

    private val isProcessing = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessing.get()) {
            imageProxy.close()
            return
        }

        isProcessing.set(true)
        val bitmap = imageProxy.toBitmapOrNull()
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        if (bitmap == null) {
            imageProxy.close()
            isProcessing.set(false)
            return
        }

        val rotatedBitmap = if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }

        try {
            // 1. Detect all faces in frame
            val rawFaces = faceDetector.detect(rotatedBitmap)

            // 2. Multi-face tracking
            val trackedFaces = faceTracker.updateTracks(rawFaces)

            val liveResults = mutableListOf<LiveFaceResult>()

            // 3. Process each detected face independently
            for (face in trackedFaces) {
                // Anti-spoofing liveness check
                val liveness = livenessDetector.checkLiveness(face, rotatedBitmap)

                var studentUuid: String? = null
                var isRecognized = false
                var confidence = 0.0f

                // If liveness passed or verification acceptable, run face embedding
                if (liveness.isLive) {
                    val embedding = faceEmbedder.extractFromCroppedFace(rotatedBitmap, face.boundingBox)
                    val match = recognitionEngine.matchFace(embedding)
                    studentUuid = match.studentUuid
                    isRecognized = match.isRecognized
                    confidence = match.confidence
                }

                liveResults.add(
                    LiveFaceResult(
                        face = face,
                        liveness = liveness,
                        studentUuid = studentUuid,
                        isRecognized = isRecognized,
                        confidence = confidence,
                        frameBitmap = rotatedBitmap
                    )
                )
            }

            onFacesProcessed(liveResults)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
            isProcessing.set(false)
        }
    }

    private fun ImageProxy.toBitmapOrNull(): Bitmap? {
        return try {
            this.toBitmap()
        } catch (e: Exception) {
            null
        }
    }
}
