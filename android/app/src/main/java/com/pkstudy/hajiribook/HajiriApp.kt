package com.pkstudy.hajiribook

import android.app.Application
import com.pkstudy.hajiribook.ai.detector.TFLiteFaceDetector
import com.pkstudy.hajiribook.ai.embedding.MobileFaceNetEmbedder
import com.pkstudy.hajiribook.ai.liveness.LivenessDetector
import com.pkstudy.hajiribook.ai.recognition.FaceRecognitionEngine
import com.pkstudy.hajiribook.ai.tracker.MultiFaceTracker
import com.pkstudy.hajiribook.data.local.database.HajiriDatabase
import com.pkstudy.hajiribook.data.remote.interceptor.ApiClient
import com.pkstudy.hajiribook.data.repository.HajiriRepository
import com.pkstudy.hajiribook.security.encryption.DatabaseBackupManager
import com.pkstudy.hajiribook.speech.TextToSpeechManager
import com.pkstudy.hajiribook.sync.scheduler.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HajiriApp : Application() {

    lateinit var database: HajiriDatabase
        private set

    lateinit var repository: HajiriRepository
        private set

    lateinit var faceDetector: TFLiteFaceDetector
        private set

    lateinit var faceEmbedder: MobileFaceNetEmbedder
        private set

    lateinit var recognitionEngine: FaceRecognitionEngine
        private set

    lateinit var livenessDetector: LivenessDetector
        private set

    lateinit var faceTracker: MultiFaceTracker
        private set

    lateinit var ttsManager: TextToSpeechManager
        private set

    lateinit var backupManager: DatabaseBackupManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 1. Initialize Room Database
        database = HajiriDatabase.getInstance(this)

        // 2. Initialize Remote API & Repository
        val apiService = ApiClient.create()
        repository = HajiriRepository(
            userDao = database.userDao(),
            studentDao = database.studentDao(),
            faceTemplateDao = database.faceTemplateDao(),
            attendanceDao = database.attendanceDao(),
            classDao = database.classDao(),
            syncDao = database.syncDao(),
            notificationDao = database.notificationDao(),
            appSettingsDao = database.appSettingsDao(),
            apiService = apiService
        )

        // 3. Initialize AI Inference Engines
        faceDetector = TFLiteFaceDetector(this)
        faceEmbedder = MobileFaceNetEmbedder(this)
        recognitionEngine = FaceRecognitionEngine(recognitionThreshold = 0.72f)
        livenessDetector = LivenessDetector()
        faceTracker = MultiFaceTracker()

        // 4. Load Enrolled Face Templates into memory
        CoroutineScope(Dispatchers.IO).launch {
            val templates = repository.loadAllFaceTemplates()
            recognitionEngine.loadTemplates(templates)
        }

        // 5. Initialize Voice Announcement
        ttsManager = TextToSpeechManager(this)

        // 6. Initialize Backup Manager
        backupManager = DatabaseBackupManager(this)

        // 7. Schedule Background Periodic WorkManager Sync
        SyncScheduler.schedulePeriodicSync(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        ttsManager.shutdown()
        faceDetector.close()
        faceEmbedder.close()
    }

    companion object {
        lateinit var instance: HajiriApp
            private set
    }
}
